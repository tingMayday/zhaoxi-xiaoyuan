import { Controller, Get, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { addDays, zonedToday } from '../common/dates';

type ArchiveKind = 'finance' | 'health' | 'schedule' | 'buy';

interface ArchiveEntry {
  kind: ArchiveKind;
  title: string;
  detail: string;
  value: string;
}

/** 时光档案：把收支、身体、日程和待买按日期折在一起。 */
@Controller('archive')
export class ArchiveController {
  constructor(private prisma: PrismaService) {}

  /** 今天往前 90 天到往后 30 天。待买未买的归到创建日，已买的归到买到日。 */
  @Get()
  async list(@Req() req: { user: User }) {
    const today = zonedToday(req.user.timezone);
    const from = addDays(today, -90);
    const to = addDays(today, 30);
    const userId = req.user.id;
    const [transactions, bodies, schedules, buys] = await Promise.all([
      this.prisma.transaction.findMany({
        where: { userId, occurredOn: { gte: from, lte: today } },
        include: { category: true },
        orderBy: { createdAt: 'desc' },
      }),
      this.prisma.bodyLog.findMany({
        where: { userId, date: { gte: from, lte: today } },
        orderBy: { date: 'desc' },
      }),
      this.prisma.scheduleItem.findMany({
        where: { userId, dueOn: { gte: from, lte: to } },
        orderBy: { createdAt: 'asc' },
      }),
      this.prisma.buyItem.findMany({
        where: { userId },
        orderBy: { createdAt: 'desc' },
      }),
    ]);

    const grouped = new Map<string, ArchiveEntry[]>();
    const push = (date: string, entry: ArchiveEntry) => {
      if (date < from || date > to) return;
      const list = grouped.get(date) || [];
      list.push(entry);
      grouped.set(date, list);
    };

    for (const item of transactions) {
      const direction = item.direction === 'expense' ? '支出' : '收入';
      const sign = item.direction === 'expense' ? '-' : '+';
      push(item.occurredOn, {
        kind: 'finance',
        title: item.note || item.category.name,
        detail: `${item.category.name} · ${direction}`,
        value: `${sign}¥${yuan(item.amountCents)}`,
      });
    }

    for (const item of bodies) {
      if (item.weightKg == null && item.bodyFatPct == null && item.intakeKcal == null && item.exerciseMin == null) continue;
      const bits = [
        item.bodyFatPct == null ? '' : `体脂 ${item.bodyFatPct}%`,
        item.exerciseMin == null ? '' : `${item.exerciseMin} 分钟运动`,
        item.intakeKcal == null ? '' : `摄入 ${item.intakeKcal} kcal`,
      ].filter(Boolean);
      push(item.date, {
        kind: 'health',
        title: item.weightKg == null ? '身体记录' : '体重',
        detail: bits.join(' · ') || '记下了今天的身体',
        value: item.weightKg == null ? '' : `${trimNumber(item.weightKg)} kg`,
      });
    }

    for (const item of schedules) {
      const clock = item.startsAt ? timeLabel(item.startsAt, req.user.timezone) : '';
      push(item.dueOn, {
        kind: 'schedule',
        title: item.title,
        detail: [item.note, clock, item.status === 'done' ? '已完成' : '待完成'].filter(Boolean).join(' · '),
        value: clock,
      });
    }

    for (const item of buys) {
      const date = item.status === 'bought' && item.boughtOn ? item.boughtOn : dayLabel(item.createdAt, req.user.timezone);
      const quantity = item.quantity === 1 ? '' : `${trimNumber(item.quantity)} · `;
      push(date, {
        kind: 'buy',
        title: item.title,
        detail: `${quantity}${item.status === 'bought' ? '已买到' : '待买'}`,
        value: item.estimateCents > 0 ? `¥${yuan(item.estimateCents)}` : '',
      });
    }

    const days = [...grouped.entries()]
      .sort((a, b) => (a[0] < b[0] ? 1 : -1))
      .map(([date, items]) => ({ date, items }));

    return { today, days };
  }
}

function yuan(cents: number) {
  return (cents / 100).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function trimNumber(value: number) {
  return Number.isInteger(value) ? String(value) : String(value);
}

function dayLabel(date: Date, timeZone: string) {
  try {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).format(date);
  } catch {
    return date.toISOString().slice(0, 10);
  }
}

function timeLabel(date: Date, timeZone: string) {
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      timeZone,
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    }).format(date);
  } catch {
    return '';
  }
}
