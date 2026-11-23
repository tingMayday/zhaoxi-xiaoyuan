const { PrismaClient } = require('@prisma/client');
const bcrypt = require('bcryptjs');

const prisma = new PrismaClient();

function addDays(iso, days) {
  const [y, m, d] = iso.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1, d));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

function shiftMonth(yearMonth, delta) {
  const [y, m] = yearMonth.split('-').map(Number);
  const date = new Date(Date.UTC(y, m - 1 + delta, 1));
  return `${date.getUTCFullYear()}-${String(date.getUTCMonth() + 1).padStart(2, '0')}`;
}

async function main() {
  const today = new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date());
  const month = today.slice(0, 7);
  const previous = shiftMonth(month, -1);

  const existing = await prisma.user.findUnique({ where: { email: 'demo@zhaoxi.local' } });
  if (existing) {
    await prisma.transaction.deleteMany({ where: { userId: existing.id } });
    await prisma.budget.deleteMany({ where: { userId: existing.id } });
    await prisma.habitLog.deleteMany({ where: { userId: existing.id } });
    await prisma.habit.deleteMany({ where: { userId: existing.id } });
    await prisma.bodyLog.deleteMany({ where: { userId: existing.id } });
    await prisma.fitnessProfile.deleteMany({ where: { userId: existing.id } });
    await prisma.weekPlanItem.deleteMany({ where: { userId: existing.id } });
    await prisma.scheduleItem.deleteMany({ where: { userId: existing.id } });
    await prisma.buyItem.deleteMany({ where: { userId: existing.id } });
    await prisma.mediaItem.deleteMany({ where: { userId: existing.id } });
    await prisma.category.deleteMany({ where: { userId: existing.id } });
    await prisma.user.delete({ where: { id: existing.id } });
  }

  const user = await prisma.user.create({
    data: {
      email: 'demo@zhaoxi.local',
      passwordHash: bcrypt.hashSync('demo1234', 10),
      name: '院主',
      timezone: 'Asia/Shanghai',
      heightCm: 170,
      categories: {
        create: [
          { name: '餐饮', kind: 'expense', sort: 1 },
          { name: '交通', kind: 'expense', sort: 2 },
          { name: '购物', kind: 'expense', sort: 3 },
          { name: '居住', kind: 'expense', sort: 4 },
          { name: '娱乐', kind: 'expense', sort: 5 },
          { name: '医疗', kind: 'expense', sort: 6 },
          { name: '其他', kind: 'expense', sort: 7 },
          { name: '工资', kind: 'income', sort: 1 },
          { name: '其他收入', kind: 'income', sort: 2 },
        ],
      },
    },
    include: { categories: true },
  });

  const category = (name) => user.categories.find((item) => item.name === name);
  const food = category('餐饮');
  const traffic = category('交通');
  const salary = category('工资');

  await prisma.transaction.createMany({
    data: [
      { userId: user.id, occurredOn: `${previous}-03`, direction: 'expense', amountCents: 8600, categoryId: food.id, note: '上月晚饭' },
      { userId: user.id, occurredOn: `${previous}-12`, direction: 'expense', amountCents: 2400, categoryId: traffic.id, note: '上月地铁' },
      { userId: user.id, occurredOn: `${previous}-01`, direction: 'income', amountCents: 1200000, categoryId: salary.id, note: '上月工资' },
      { userId: user.id, occurredOn: `${month}-02`, direction: 'expense', amountCents: 4200, categoryId: food.id, note: '早餐' },
      { userId: user.id, occurredOn: `${month}-06`, direction: 'expense', amountCents: 12800, categoryId: food.id, note: '周末采买' },
      { userId: user.id, occurredOn: addDays(today, -2), direction: 'expense', amountCents: 1800, categoryId: traffic.id, note: '出门' },
      { userId: user.id, occurredOn: `${month}-01`, direction: 'income', amountCents: 1200000, categoryId: salary.id, note: '本月工资' },
    ],
  });

  await prisma.budget.create({
    data: { userId: user.id, yearMonth: month, categoryKey: 'total', amountCents: 400000 },
  });

  const water = await prisma.habit.create({
    data: { userId: user.id, name: '喝水', kind: 'count', unit: '杯', dailyTarget: 8, sort: 1 },
  });
  const rise = await prisma.habit.create({
    data: { userId: user.id, name: '早起', kind: 'check', dailyTarget: 1, sort: 2 },
  });
  await prisma.habit.create({
    data: { userId: user.id, name: '睡眠', kind: 'value', unit: '小时', dailyTarget: 7.5, sort: 3 },
  });

  const logs = [];
  for (let i = 1; i <= 12; i += 1) {
    logs.push({ habitId: water.id, userId: user.id, date: addDays(today, -i), value: i % 3 === 0 ? 8 : 5 });
    if (i % 2 === 0) logs.push({ habitId: rise.id, userId: user.id, date: addDays(today, -i), value: 1 });
  }
  await prisma.habitLog.createMany({ data: logs });

  await prisma.fitnessProfile.create({
    data: {
      userId: user.id,
      startWeightKg: 60.5,
      targetWeightKg: 55,
      startDate: addDays(today, -21),
      targetIntakeKcal: 1800,
    },
  });
  const weights = [60.4, 60.2, 60.1, 59.9, 59.8, 59.7, 59.6, 59.5];
  await prisma.bodyLog.createMany({
    data: weights.map((weightKg, index) => ({
      userId: user.id,
      date: addDays(today, -(weights.length - index)),
      weightKg,
      bodyFatPct: 22,
      intakeKcal: 1700,
      exerciseMin: index % 2 === 0 ? 30 : 0,
    })),
  });

  await prisma.weekPlanItem.createMany({
    data: [
      { userId: user.id, weekday: 1, title: '快走', detail: '四十分钟', sort: 0 },
      { userId: user.id, weekday: 3, title: '力量', detail: '徒手深蹲与俯卧撑', sort: 0 },
      { userId: user.id, weekday: 5, title: '休息日拉伸', detail: '', sort: 0 },
    ],
  });

  await prisma.scheduleItem.createMany({
    data: [
      { userId: user.id, title: '交水电费', dueOn: addDays(today, -1), status: 'open' },
      { userId: user.id, title: '整理书桌', dueOn: today, status: 'open' },
      { userId: user.id, title: '去菜市场', dueOn: addDays(today, 2), status: 'open' },
    ],
  });

  await prisma.buyItem.create({
    data: { userId: user.id, title: '橄榄油', quantity: 1, estimateCents: 6800, status: 'open' },
  });

  await prisma.mediaItem.createMany({
    data: [
      {
        userId: user.id,
        kind: 'film',
        title: '小森林',
        creator: '森淳一',
        status: 'done',
        rating: 5,
        note: '想把日子过得慢一点。',
        finishedOn: `${today.slice(0, 4)}-01-08`,
      },
      {
        userId: user.id,
        kind: 'book',
        title: '阴翳礼赞',
        creator: '谷崎润一郎',
        status: 'doing',
        note: '夜里读几页。',
      },
    ],
  });

  console.log('seeded demo@zhaoxi.local / demo1234');
}

main()
  .catch((error) => {
    console.error(error);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
