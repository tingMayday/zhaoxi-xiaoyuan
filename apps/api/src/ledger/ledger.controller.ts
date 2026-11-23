import { BadRequestException, Body, Controller, Delete, Get, Param, Patch, Post, Query, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { monthBounds, shiftMonth, zonedToday } from '../common/dates';
import { CreateCategoryDto, CreateTransactionDto, UpsertBudgetDto } from './dto';

/** 记账：分类、流水、预算，以及月度汇总和上月对比。金额一律用分。 */
@Controller()
export class LedgerController {
  constructor(private prisma: PrismaService) {}

  /** 未归档的收支分类。 */
  @Get('categories')
  categories(@Req() req: { user: User }) {
    return this.prisma.category.findMany({
      where: { userId: req.user.id, archived: false },
      orderBy: [{ kind: 'asc' }, { sort: 'asc' }],
    });
  }

  /** 新建分类，排在同方向的最后。 */
  @Post('categories')
  async createCategory(@Req() req: { user: User }, @Body() dto: CreateCategoryDto) {
    const count = await this.prisma.category.count({
      where: { userId: req.user.id, kind: dto.kind },
    });
    return this.prisma.category.create({
      data: {
        userId: req.user.id,
        name: dto.name.trim(),
        kind: dto.kind,
        sort: count + 1,
      },
    });
  }

  /** 归档分类。已有流水保留，只是不再出现在可选列表里。 */
  @Patch('categories/:id')
  async archiveCategory(@Req() req: { user: User }, @Param('id') id: string) {
    const category = await this.ownCategory(req.user.id, id);
    return this.prisma.category.update({
      where: { id: category.id },
      data: { archived: true },
    });
  }

  /** 某月流水，可按收支方向和分类筛选。 */
  @Get('transactions')
  async transactions(
    @Req() req: { user: User },
    @Query('month') month?: string,
    @Query('direction') direction?: string,
    @Query('categoryId') categoryId?: string,
  ) {
    const yearMonth = month || zonedToday(req.user.timezone).slice(0, 7);
    const bounds = monthBounds(yearMonth);
    return this.prisma.transaction.findMany({
      where: {
        userId: req.user.id,
        occurredOn: { gte: bounds.start, lte: bounds.end },
        direction: direction === 'income' || direction === 'expense' ? direction : undefined,
        categoryId: categoryId || undefined,
      },
      include: { category: true },
      orderBy: [{ occurredOn: 'desc' }, { createdAt: 'desc' }],
    });
  }

  /** 记一笔。分类必须属于当前用户，且和收支方向一致。 */
  @Post('transactions')
  async createTransaction(@Req() req: { user: User }, @Body() dto: CreateTransactionDto) {
    const category = await this.ownCategory(req.user.id, dto.categoryId);
    if (category.kind !== dto.direction) {
      throw new BadRequestException('分类和收支方向不一致');
    }
    return this.prisma.transaction.create({
      data: {
        userId: req.user.id,
        occurredOn: dto.occurredOn,
        direction: dto.direction,
        amountCents: dto.amountCents,
        categoryId: category.id,
        note: dto.note?.trim() || '',
      },
      include: { category: true },
    });
  }

  /** 删除自己的一笔流水。 */
  @Delete('transactions/:id')
  async deleteTransaction(@Req() req: { user: User }, @Param('id') id: string) {
    const found = await this.prisma.transaction.findFirst({ where: { id, userId: req.user.id } });
    if (!found) throw new BadRequestException('找不到这笔记录');
    await this.prisma.transaction.delete({ where: { id } });
    return { ok: true };
  }

  /** 某月的预算。categoryKey 为 total 时是总额，否则是分类 id。 */
  @Get('budgets')
  budgets(@Req() req: { user: User }, @Query('month') month?: string) {
    const yearMonth = month || zonedToday(req.user.timezone).slice(0, 7);
    return this.prisma.budget.findMany({ where: { userId: req.user.id, yearMonth } });
  }

  /** 保存一个月的总预算或分类预算，已有则覆盖。 */
  @Post('budgets')
  upsertBudget(@Req() req: { user: User }, @Body() dto: UpsertBudgetDto) {
    return this.prisma.budget.upsert({
      where: {
        userId_yearMonth_categoryKey: {
          userId: req.user.id,
          yearMonth: dto.yearMonth,
          categoryKey: dto.categoryKey,
        },
      },
      update: { amountCents: dto.amountCents },
      create: {
        userId: req.user.id,
        yearMonth: dto.yearMonth,
        categoryKey: dto.categoryKey,
        amountCents: dto.amountCents,
      },
    });
  }

  /** 月度收入、支出、结余，以及各分类花了多少、预算用了多少。 */
  @Get('finance/summary')
  async summary(@Req() req: { user: User }, @Query('month') month?: string) {
    const yearMonth = month || zonedToday(req.user.timezone).slice(0, 7);
    const bounds = monthBounds(yearMonth);
    const [transactions, categories, budgets] = await Promise.all([
      this.prisma.transaction.findMany({
        where: { userId: req.user.id, occurredOn: { gte: bounds.start, lte: bounds.end } },
      }),
      this.prisma.category.findMany({
        where: { userId: req.user.id, archived: false },
        orderBy: { sort: 'asc' },
      }),
      this.prisma.budget.findMany({ where: { userId: req.user.id, yearMonth } }),
    ]);
    const incomeCents = sum(transactions.filter((item) => item.direction === 'income'));
    const expenseCents = sum(transactions.filter((item) => item.direction === 'expense'));
    const budgetMap = new Map(budgets.map((item) => [item.categoryKey, item.amountCents]));
    const totalBudgetCents = budgetMap.has('total') ? budgetMap.get('total')! : null;
    const expenseCategories = categories
      .filter((item) => item.kind === 'expense')
      .map((item) => ({
        id: item.id,
        name: item.name,
        amountCents: sum(transactions.filter((tx) => tx.categoryId === item.id && tx.direction === 'expense')),
        budgetCents: budgetMap.get(item.id) ?? null,
      }));
    return {
      month: yearMonth,
      incomeCents,
      expenseCents,
      balanceCents: incomeCents - expenseCents,
      totalBudgetCents,
      remainingCents: totalBudgetCents == null ? null : totalBudgetCents - expenseCents,
      usedRatio: totalBudgetCents ? expenseCents / totalBudgetCents : null,
      categories: expenseCategories,
    };
  }

  /** 和上月比收支差额。上月没有流水时 available 为 false。 */
  @Get('finance/compare')
  async compare(@Req() req: { user: User }, @Query('month') month?: string) {
    const yearMonth = month || zonedToday(req.user.timezone).slice(0, 7);
    const previous = shiftMonth(yearMonth, -1);
    const [currentTx, previousTx, categories] = await Promise.all([
      this.monthTransactions(req.user.id, yearMonth),
      this.monthTransactions(req.user.id, previous),
      this.prisma.category.findMany({ where: { userId: req.user.id } }),
    ]);
    if (previousTx.length === 0) {
      return { available: false, month: yearMonth, previous };
    }
    const currentIncome = sum(currentTx.filter((item) => item.direction === 'income'));
    const currentExpense = sum(currentTx.filter((item) => item.direction === 'expense'));
    const previousIncome = sum(previousTx.filter((item) => item.direction === 'income'));
    const previousExpense = sum(previousTx.filter((item) => item.direction === 'expense'));
    const names = new Map(categories.map((item) => [item.id, item.name]));
    const keys = new Set([
      ...currentTx.filter((item) => item.direction === 'expense').map((item) => item.categoryId),
      ...previousTx.filter((item) => item.direction === 'expense').map((item) => item.categoryId),
    ]);
    const top = [...keys]
      .map((id) => ({
        name: names.get(id) || '未分类',
        deltaCents:
          sum(currentTx.filter((item) => item.categoryId === id && item.direction === 'expense')) -
          sum(previousTx.filter((item) => item.categoryId === id && item.direction === 'expense')),
      }))
      .sort((a, b) => Math.abs(b.deltaCents) - Math.abs(a.deltaCents))
      .slice(0, 5);
    return {
      available: true,
      month: yearMonth,
      previous,
      incomeDeltaCents: currentIncome - previousIncome,
      expenseDeltaCents: currentExpense - previousExpense,
      balanceDeltaCents: currentIncome - currentExpense - (previousIncome - previousExpense),
      current: { incomeCents: currentIncome, expenseCents: currentExpense },
      previousTotals: { incomeCents: previousIncome, expenseCents: previousExpense },
      top,
    };
  }

  private monthTransactions(userId: string, yearMonth: string) {
    const bounds = monthBounds(yearMonth);
    return this.prisma.transaction.findMany({
      where: { userId, occurredOn: { gte: bounds.start, lte: bounds.end } },
    });
  }

  private async ownCategory(userId: string, id: string) {
    const category = await this.prisma.category.findFirst({
      where: { id, userId, archived: false },
    });
    if (!category) throw new BadRequestException('找不到这个分类');
    return category;
  }
}

function sum(items: { amountCents: number }[]) {
  return items.reduce((total, item) => total + item.amountCents, 0);
}
