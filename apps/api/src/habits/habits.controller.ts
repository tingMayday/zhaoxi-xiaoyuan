import { BadRequestException, Body, Controller, Get, Param, Patch, Post, Put, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { lastDays, zonedToday } from '../common/dates';
import { CreateHabitDto, HabitLogDto, UpdateHabitDto } from './dto';

/** 习惯：列表、新建、修改或归档，以及某一天的打卡。同一天再次提交会覆盖。 */
@Controller('habits')
export class HabitsController {
  constructor(private prisma: PrismaService) {}

  /** 未归档的习惯，附带近 30 天打卡，给热力图用。 */
  @Get()
  async list(@Req() req: { user: User }) {
    const today = zonedToday(req.user.timezone);
    const days = lastDays(today, 30);
    const habits = await this.prisma.habit.findMany({
      where: { userId: req.user.id, archived: false },
      orderBy: [{ sort: 'asc' }, { name: 'asc' }],
      include: {
        logs: {
          where: { date: { gte: days[0], lte: today } },
          orderBy: { date: 'asc' },
        },
      },
    });
    return { today, days, habits };
  }

  /** 新建习惯。勾选类目标固定为 1，计数和数值才使用每日目标。 */
  @Post()
  create(@Req() req: { user: User }, @Body() dto: CreateHabitDto) {
    return this.prisma.habit.create({
      data: {
        userId: req.user.id,
        name: dto.name.trim(),
        kind: dto.kind,
        unit: dto.kind === 'check' ? '' : dto.unit?.trim() || '',
        dailyTarget: dto.kind === 'check' ? 1 : dto.dailyTarget || 1,
      },
    });
  }

  /** 改名称、单位、目标，或归档。归档后不再出现，历史打卡留下。 */
  @Patch(':id')
  async update(@Req() req: { user: User }, @Param('id') id: string, @Body() dto: UpdateHabitDto) {
    await this.own(req.user.id, id);
    return this.prisma.habit.update({
      where: { id },
      data: {
        name: dto.name?.trim(),
        unit: dto.unit?.trim(),
        dailyTarget: dto.dailyTarget,
        archived: dto.archived,
      },
    });
  }

  /** 记下某一天的完成值。同一习惯同一天只有一条。 */
  @Put(':id/logs/:date')
  async log(
    @Req() req: { user: User },
    @Param('id') id: string,
    @Param('date') date: string,
    @Body() dto: HabitLogDto,
  ) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new BadRequestException('日期格式应为 YYYY-MM-DD');
    await this.own(req.user.id, id);
    return this.prisma.habitLog.upsert({
      where: { habitId_date: { habitId: id, date } },
      update: { value: dto.value },
      create: { habitId: id, userId: req.user.id, date, value: dto.value },
    });
  }

  private async own(userId: string, id: string) {
    const habit = await this.prisma.habit.findFirst({ where: { id, userId } });
    if (!habit) throw new BadRequestException('找不到这个习惯');
    return habit;
  }
}
