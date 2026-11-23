import { Controller, Get, Query, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { addDays, monthBounds, quoteFor, zonedToday } from '../common/dates';

/** 今日首页要的一份汇总，不另存数据，现查各模块。 */
@Controller('today')
export class TodayController {
  constructor(private prisma: PrismaService) {}

  /**
   * 今日分数满分 100：习惯 40、日程 30、有记录 15、没有逾期 15。
   * 还没有任何习惯或日程时，对应那一项直接给满分。
   * 有记录指当天记过账、体重或摄入；一个人还没有任何这类记录时也给这 15 分。
   */
  @Get()
  async today(@Req() req: { user: User }, @Query('date') date?: string) {
    const today = date && /^\d{4}-\d{2}-\d{2}$/.test(date) ? date : zonedToday(req.user.timezone);
    const month = today.slice(0, 7);
    const bounds = monthBounds(month);
    const userId = req.user.id;
    const [
      monthTx,
      budget,
      habits,
      todayLogs,
      schedule,
      latestWeight,
      todayBody,
      everTx,
      everBody,
      buyOpen,
      mediaDone,
    ] = await Promise.all([
      this.prisma.transaction.findMany({
        where: { userId, occurredOn: { gte: bounds.start, lte: bounds.end } },
        include: { category: true },
        orderBy: { createdAt: 'desc' },
      }),
      this.prisma.budget.findUnique({
        where: { userId_yearMonth_categoryKey: { userId, yearMonth: month, categoryKey: 'total' } },
      }),
      this.prisma.habit.findMany({
        where: { userId, archived: false },
        include: { logs: { where: { date: today } } },
      }),
      this.prisma.habitLog.findMany({ where: { userId, date: today } }),
      this.prisma.scheduleItem.findMany({ where: { userId } }),
      this.prisma.bodyLog.findFirst({
        where: { userId, weightKg: { not: null } },
        orderBy: { date: 'desc' },
      }),
      this.prisma.bodyLog.findUnique({ where: { userId_date: { userId, date: today } } }),
      this.prisma.transaction.count({ where: { userId } }),
      this.prisma.bodyLog.count({ where: { userId, OR: [{ weightKg: { not: null } }, { intakeKcal: { not: null } }] } }),
      this.prisma.buyItem.findMany({ where: { userId, status: 'open' } }),
      this.prisma.mediaItem.findMany({
        where: { userId, status: 'done', finishedOn: { startsWith: today.slice(0, 4) } },
      }),
    ]);

    const expenseCents = monthTx.filter((item) => item.direction === 'expense').reduce((sum, item) => sum + item.amountCents, 0);
    const habitDone = habits.filter((habit) => {
      const log = todayLogs.find((item) => item.habitId === habit.id) || habit.logs[0];
      return !!log && log.value >= habit.dailyTarget;
    }).length;
    const todaySchedule = schedule.filter((item) => item.dueOn === today);
    const todayDone = todaySchedule.filter((item) => item.status === 'done').length;
    const overdue = schedule.filter((item) => item.status === 'open' && item.dueOn < today).length;
    const next7 = schedule.filter((item) => item.status === 'open' && item.dueOn > today && item.dueOn <= addDays(today, 7)).length;
    const loggedToday = monthTx.some((item) => item.occurredOn === today) || todayBody?.weightKg != null || todayBody?.intakeKcal != null;

    let score = 0;
    const habitPoints = habits.length === 0 ? 40 : Math.round((40 * habitDone) / habits.length);
    const schedulePoints = todaySchedule.length === 0 ? 30 : Math.round((30 * todayDone) / todaySchedule.length);
    const recordPoints = everTx + everBody === 0 || loggedToday ? 15 : 0;
    const overduePoints = overdue === 0 ? 15 : 0;
    score = habitPoints + schedulePoints + recordPoints + overduePoints;

    const rated = mediaDone.filter((item) => item.rating != null);
    return {
      date: today,
      quote: quoteFor(today),
      score,
      breakdown: { habitPoints, schedulePoints, recordPoints, overduePoints },
      finance: {
        expenseCents,
        totalBudgetCents: budget?.amountCents ?? null,
        remainingCents: budget ? budget.amountCents - expenseCents : null,
      },
      habits: {
        done: habitDone,
        total: habits.length,
        items: habits.map((habit) => {
          const log = todayLogs.find((item) => item.habitId === habit.id) || habit.logs[0];
          return {
            id: habit.id,
            name: habit.name,
            kind: habit.kind,
            unit: habit.unit,
            dailyTarget: habit.dailyTarget,
            value: log?.value ?? 0,
          };
        }),
      },
      schedule: {
        todayOpen: todaySchedule.filter((item) => item.status === 'open').length,
        todayTotal: todaySchedule.length,
        overdue,
        next7,
        items: [...todaySchedule]
          .sort((a, b) => Number(a.status === 'done') - Number(b.status === 'done'))
          .map((item) => ({
            id: item.id,
            title: item.title,
            note: item.note,
            status: item.status,
            time: item.startsAt ? clock(item.startsAt, req.user.timezone) : '',
          })),
      },
      recent: recentEntries(monthTx, schedule, buyOpen, latestWeight, today, req.user.timezone),
      fitness: {
        weightKg: latestWeight?.weightKg ?? null,
        weightDate: latestWeight?.date ?? null,
      },
      buy: {
        openCount: buyOpen.length,
        estimateCents: buyOpen.reduce((sum, item) => sum + Math.round(item.estimateCents * item.quantity), 0),
      },
      media: {
        yearDone: mediaDone.length,
        averageRating: rated.length
          ? Math.round((rated.reduce((sum, item) => sum + (item.rating as number), 0) / rated.length) * 10) / 10
          : null,
      },
    };
  }
}

function dayOf(date: Date, timeZone: string) {
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

function clock(date: Date, timeZone: string) {
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

function recentEntries(
  transactions: { id: string; occurredOn: string; direction: string; amountCents: number; note: string; category: { name: string } }[],
  schedule: { id: string; title: string; dueOn: string; note: string; startsAt: Date | null; status: string }[],
  buys: { id: string; title: string; createdAt: Date; estimateCents: number; quantity: number }[],
  weight: { date: string; weightKg: number | null } | null,
  today: string,
  timeZone: string,
) {
  const rows: { id: string; date: string; kind: string; title: string; detail: string; value: string }[] = [];
  for (const item of transactions.slice(0, 6)) {
    const sign = item.direction === 'expense' ? '-' : '+';
    rows.push({
      id: item.id,
      date: item.occurredOn,
      kind: '财务',
      title: item.note || item.category.name,
      detail: item.category.name,
      value: `${sign}¥${(item.amountCents / 100).toFixed(2)}`,
    });
  }
  const pastSchedule = schedule
    .filter((row) => row.dueOn <= today)
    .sort((a, b) => (a.dueOn < b.dueOn ? 1 : -1))
    .slice(0, 4);
  for (const item of pastSchedule) {
    rows.push({
      id: item.id,
      date: item.dueOn,
      kind: '日程',
      title: item.title,
      detail: item.note || (item.status === 'done' ? '已完成' : '待完成'),
      value: item.startsAt ? clock(item.startsAt, timeZone) : '',
    });
  }
  for (const item of buys.slice(0, 3)) {
    rows.push({
      id: item.id,
      date: dayOf(item.createdAt, timeZone),
      kind: '待买',
      title: item.title,
      detail: '待买',
      value: item.estimateCents > 0 ? `¥${(item.estimateCents / 100).toFixed(2)}` : '',
    });
  }
  if (weight?.weightKg != null) {
    rows.push({
      id: `weight-${weight.date}`,
      date: weight.date,
      kind: '健康',
      title: '体重',
      detail: '身体记录',
      value: `${weight.weightKg} kg`,
    });
  }
  return rows.sort((a, b) => (a.date < b.date ? 1 : -1)).slice(0, 6);
}
