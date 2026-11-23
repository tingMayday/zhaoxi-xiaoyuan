import { BadRequestException, Body, Controller, Get, Param, Put, Query, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { addDays, zonedToday } from '../common/dates';
import { FitnessProfileDto, ReplaceWeekPlanDto, UpsertBodyLogDto } from './dto';

/** 健身：体重曲线、身体记录、减脂目标和一周安排。 */
@Controller()
export class FitnessController {
  constructor(private prisma: PrismaService) {}

  /** 一段日期内的体重点、7 日均线、BMI、目标进度和今日热量缺口。 */
  @Get('fitness/series')
  async series(
    @Req() req: { user: User },
    @Query('from') from?: string,
    @Query('to') to?: string,
  ) {
    const today = zonedToday(req.user.timezone);
    const end = to || today;
    const start = from || addDays(end, -59);
    const [profile, logs, latest] = await Promise.all([
      this.prisma.fitnessProfile.findUnique({ where: { userId: req.user.id } }),
      this.prisma.bodyLog.findMany({
        where: { userId: req.user.id, date: { gte: addDays(start, -6), lte: end } },
        orderBy: { date: 'asc' },
      }),
      this.prisma.bodyLog.findFirst({
        where: { userId: req.user.id, weightKg: { not: null } },
        orderBy: { date: 'desc' },
      }),
    ]);
    const points = logs
      .filter((item) => item.date >= start && item.date <= end)
      .map((item) => ({
        date: item.date,
        weightKg: item.weightKg,
        bodyFatPct: item.bodyFatPct,
        intakeKcal: item.intakeKcal,
        exerciseMin: item.exerciseMin,
        ma7: movingAverage(logs, item.date),
      }));
    const todayLog = logs.find((item) => item.date === today) || null;
    const currentWeight = latest?.weightKg ?? null;
    const heightM = req.user.heightCm ? req.user.heightCm / 100 : null;
    const bmi = currentWeight && heightM ? round1(currentWeight / (heightM * heightM)) : null;
    const progress =
      profile && currentWeight != null
        ? goalProgress(profile.startWeightKg, profile.targetWeightKg, currentWeight)
        : null;
    const deficit =
      profile && todayLog?.intakeKcal != null ? profile.targetIntakeKcal - todayLog.intakeKcal : null;
    return {
      today,
      profile,
      heightCm: req.user.heightCm,
      points,
      latest,
      bmi,
      progress,
      deficit,
      todayLog,
    };
  }

  /** 保存某一天的体重、体脂、摄入和运动分钟。没传的字段保持原值。 */
  @Put('body-logs/:date')
  async upsertLog(
    @Req() req: { user: User },
    @Param('date') date: string,
    @Body() dto: UpsertBodyLogDto,
  ) {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(date)) throw new BadRequestException('日期格式应为 YYYY-MM-DD');
    const existing = await this.prisma.bodyLog.findUnique({
      where: { userId_date: { userId: req.user.id, date } },
    });
    const data = {
      weightKg: pick(dto, 'weightKg', existing?.weightKg ?? null),
      bodyFatPct: pick(dto, 'bodyFatPct', existing?.bodyFatPct ?? null),
      intakeKcal: pick(dto, 'intakeKcal', existing?.intakeKcal ?? null),
      exerciseMin: pick(dto, 'exerciseMin', existing?.exerciseMin ?? null),
    };
    return this.prisma.bodyLog.upsert({
      where: { userId_date: { userId: req.user.id, date } },
      update: data,
      create: { userId: req.user.id, date, ...data },
    });
  }

  /** 保存减脂起点、目标和每日摄入目标。每人一份。 */
  @Put('fitness/profile')
  profile(@Req() req: { user: User }, @Body() dto: FitnessProfileDto) {
    return this.prisma.fitnessProfile.upsert({
      where: { userId: req.user.id },
      update: dto,
      create: { userId: req.user.id, ...dto },
    });
  }

  /** 一周安排，按星期和排序返回。 */
  @Get('week-plan')
  weekPlan(@Req() req: { user: User }) {
    return this.prisma.weekPlanItem.findMany({
      where: { userId: req.user.id },
      orderBy: [{ weekday: 'asc' }, { sort: 'asc' }],
    });
  }

  /** 用提交的列表整周替换，空项会被丢掉。 */
  @Put('week-plan')
  async replaceWeekPlan(@Req() req: { user: User }, @Body() dto: ReplaceWeekPlanDto) {
    const items = dto.items
      .map((item, index) => ({
        userId: req.user.id,
        weekday: item.weekday,
        title: item.title.trim(),
        detail: item.detail?.trim() || '',
        sort: item.sort ?? index,
      }))
      .filter((item) => item.title);
    await this.prisma.$transaction([
      this.prisma.weekPlanItem.deleteMany({ where: { userId: req.user.id } }),
      this.prisma.weekPlanItem.createMany({ data: items }),
    ]);
    return this.weekPlan(req);
  }
}

function pick<T>(dto: object, key: string, fallback: T): T {
  return Object.prototype.hasOwnProperty.call(dto, key) ? (dto as Record<string, T>)[key] : fallback;
}

function round1(value: number) {
  return Math.round(value * 10) / 10;
}

/** 含当天在内往前 7 天，只平均有体重的日子，缺日不补 0。 */
function movingAverage(logs: { date: string; weightKg: number | null }[], date: string) {
  const current = logs.find((item) => item.date === date);
  if (current?.weightKg == null) return null;
  const start = addDays(date, -6);
  const window = logs.filter((item) => item.date >= start && item.date <= date && item.weightKg != null);
  const total = window.reduce((sum, item) => sum + (item.weightKg as number), 0);
  return round1(total / window.length);
}

/** (当前 − 起点) / (目标 − 起点)。增重时分子分母同时为负，不把结果限制在 0 到 1。 */
function goalProgress(start: number, target: number, current: number) {
  if (start === target) return current === target ? 1 : 0;
  return (current - start) / (target - start);
}
