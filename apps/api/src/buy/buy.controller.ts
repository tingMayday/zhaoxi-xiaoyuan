import { BadRequestException, Body, Controller, Delete, Get, Param, Patch, Post, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { zonedToday } from '../common/dates';
import { CreateBuyDto, UpdateBuyDto } from './dto';

/** 待买：清单、加入、改成买到和删除。 */
@Controller('buy-items')
export class BuyController {
  constructor(private prisma: PrismaService) {}

  /** 全部待买和已买，并给出未买数量、预计金额和本月买到数。 */
  @Get()
  async list(@Req() req: { user: User }) {
    const today = zonedToday(req.user.timezone);
    const month = today.slice(0, 7);
    const items = await this.prisma.buyItem.findMany({
      where: { userId: req.user.id },
      orderBy: [{ status: 'asc' }, { createdAt: 'desc' }],
    });
    const open = items.filter((item) => item.status === 'open');
    return {
      items,
      openCount: open.length,
      estimateCents: open.reduce((sum, item) => sum + Math.round(item.estimateCents * item.quantity), 0),
      boughtThisMonth: items.filter((item) => item.status === 'bought' && item.boughtOn?.startsWith(month)).length,
    };
  }

  /** 加入清单。估价按单价的分保存。 */
  @Post()
  create(@Req() req: { user: User }, @Body() dto: CreateBuyDto) {
    return this.prisma.buyItem.create({
      data: {
        userId: req.user.id,
        title: dto.title.trim(),
        quantity: dto.quantity ?? 1,
        estimateCents: dto.estimateCents ?? 0,
        note: dto.note?.trim() || '',
      },
    });
  }

  /** 修改条目。标成买到时如果没写日期，就用今天。 */
  @Patch(':id')
  async update(@Req() req: { user: User }, @Param('id') id: string, @Body() dto: UpdateBuyDto) {
    const current = await this.own(req.user.id, id);
    const status = dto.status ?? current.status;
    let boughtOn = dto.boughtOn === undefined ? current.boughtOn : dto.boughtOn;
    if (status === 'bought' && !boughtOn) boughtOn = zonedToday(req.user.timezone);
    if (status === 'open') boughtOn = null;
    return this.prisma.buyItem.update({
      where: { id },
      data: {
        title: dto.title?.trim(),
        quantity: dto.quantity,
        estimateCents: dto.estimateCents,
        note: dto.note?.trim(),
        status,
        boughtOn,
      },
    });
  }

  /** 从清单里删除。 */
  @Delete(':id')
  async remove(@Req() req: { user: User }, @Param('id') id: string) {
    await this.own(req.user.id, id);
    await this.prisma.buyItem.delete({ where: { id } });
    return { ok: true };
  }

  private async own(userId: string, id: string) {
    const item = await this.prisma.buyItem.findFirst({ where: { id, userId } });
    if (!item) throw new BadRequestException('找不到这件物品');
    return item;
  }
}
