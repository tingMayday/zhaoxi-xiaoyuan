import { BadRequestException, Body, Controller, Delete, Get, Param, Patch, Post, Query, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { addDays, zonedToday } from '../common/dates';
import { CreateScheduleDto, UpdateScheduleDto } from './dto';

/** 日程：区间列表、新建、改状态和删除。 */
@Controller('schedule')
export class ScheduleController {
  constructor(private prisma: PrismaService) {}

  /** 默认从今天前 30 天到后 60 天，另外带回所有未完成项，方便算逾期。 */
  @Get()
  async list(
    @Req() req: { user: User },
    @Query('from') from?: string,
    @Query('to') to?: string,
  ) {
    const today = zonedToday(req.user.timezone);
    const start = from || addDays(today, -30);
    const end = to || addDays(today, 60);
    const items = await this.prisma.scheduleItem.findMany({
      where: { userId: req.user.id, dueOn: { gte: start, lte: end } },
      orderBy: [{ dueOn: 'asc' }, { createdAt: 'asc' }],
    });
    const open = await this.prisma.scheduleItem.findMany({
      where: { userId: req.user.id, status: 'open' },
      orderBy: { dueOn: 'asc' },
    });
    const nextEnd = addDays(today, 7);
    return {
      today,
      items,
      overdueItems: open.filter((item) => item.dueOn < today),
      counts: {
        todayOpen: open.filter((item) => item.dueOn === today).length,
        todayTotal: items.filter((item) => item.dueOn === today).length,
        todayDone: items.filter((item) => item.dueOn === today && item.status === 'done').length,
        overdue: open.filter((item) => item.dueOn < today).length,
        next7: open.filter((item) => item.dueOn > today && item.dueOn <= nextEnd).length,
      },
    };
  }

  /** 新建日程。没有开始时间时当作全天。 */
  @Post()
  create(@Req() req: { user: User }, @Body() dto: CreateScheduleDto) {
    const startsAt = parseOptionalDate(dto.startsAt);
    const endsAt = parseOptionalDate(dto.endsAt);
    const remindAt = parseOptionalDate(dto.remindAt);
    if (startsAt && endsAt && endsAt < startsAt) throw new BadRequestException('结束时间要晚于开始时间');
    return this.prisma.scheduleItem.create({
      data: {
        userId: req.user.id,
        title: dto.title.trim(),
        dueOn: dto.dueOn,
        allDay: dto.allDay ?? !startsAt,
        startsAt,
        endsAt,
        remindAt,
        note: dto.note?.trim() || '',
      },
    });
  }

  /** 修改内容或把状态改成完成、未完成。 */
  @Patch(':id')
  async update(@Req() req: { user: User }, @Param('id') id: string, @Body() dto: UpdateScheduleDto) {
    await this.own(req.user.id, id);
    return this.prisma.scheduleItem.update({
      where: { id },
      data: {
        title: dto.title?.trim(),
        dueOn: dto.dueOn,
        status: dto.status,
        allDay: dto.allDay,
        note: dto.note?.trim(),
        startsAt: dto.startsAt === undefined ? undefined : parseOptionalDate(dto.startsAt),
        endsAt: dto.endsAt === undefined ? undefined : parseOptionalDate(dto.endsAt),
        remindAt: dto.remindAt === undefined ? undefined : parseOptionalDate(dto.remindAt),
      },
    });
  }

  /** 删除自己的一条日程。 */
  @Delete(':id')
  async remove(@Req() req: { user: User }, @Param('id') id: string) {
    await this.own(req.user.id, id);
    await this.prisma.scheduleItem.delete({ where: { id } });
    return { ok: true };
  }

  private async own(userId: string, id: string) {
    const item = await this.prisma.scheduleItem.findFirst({ where: { id, userId } });
    if (!item) throw new BadRequestException('找不到这个事项');
    return item;
  }
}

function parseOptionalDate(value?: string | null) {
  if (value == null || value === '') return null;
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) throw new BadRequestException('时间格式不正确');
  return date;
}
