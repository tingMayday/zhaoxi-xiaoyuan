const base = process.env.API || 'http://127.0.0.1:3000';

async function request(path, options = {}, token) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;
  const response = await fetch(base + path, { ...options, headers });
  const text = await response.text();
  const data = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(`${response.status} ${path} ${JSON.stringify(data)}`);
  }
  return data;
}

function assert(condition, message) {
  if (!condition) throw new Error(message);
}

async function main() {
  const login = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email: 'demo@zhaoxi.local', password: 'demo1234' }),
  });
  const token = login.token;
  const before = await request('/api/today', {}, token);
  const categories = await request('/api/categories', {}, token);
  const food = categories.find((item) => item.name === '餐饮' && item.kind === 'expense');
  assert(food, '缺少餐饮分类');

  await request('/api/transactions', {
    method: 'POST',
    body: JSON.stringify({
      occurredOn: before.date,
      direction: 'expense',
      amountCents: 1250,
      categoryId: food.id,
      note: '验收支出',
    }),
  }, token);
  const afterLedger = await request('/api/today', {}, token);
  assert(afterLedger.finance.expenseCents === before.finance.expenseCents + 1250, '今日支出没有加上 12.50');

  const habit = await request('/api/habits', {
    method: 'POST',
    body: JSON.stringify({ name: '验收计数', kind: 'count', unit: '次', dailyTarget: 3 }),
  }, token);
  await request(`/api/habits/${habit.id}/logs/${before.date}`, {
    method: 'PUT',
    body: JSON.stringify({ value: 3 }),
  }, token);
  const habits = await request('/api/habits', {}, token);
  const saved = habits.habits.find((item) => item.id === habit.id);
  const todayLog = saved.logs.find((item) => item.date === before.date);
  assert(todayLog && todayLog.value >= saved.dailyTarget, '习惯热力图当天没有完成');

  await request(`/api/body-logs/${before.date}`, {
    method: 'PUT',
    body: JSON.stringify({ weightKg: 59.2, intakeKcal: 1500, exerciseMin: 20 }),
  }, token);
  const fitness = await request('/api/fitness/series', {}, token);
  assert(fitness.bmi && fitness.bmi > 15 && fitness.bmi < 40, 'BMI 没有算出来');
  assert(fitness.points.some((item) => item.date === before.date && item.ma7), '7 日均线没有连上');
  assert(fitness.progress != null && fitness.progress > 0, '目标进度没有算出来');
  assert(fitness.deficit === 300, `热量缺口应为 300，实际 ${fitness.deficit}`);

  await request('/api/schedule', {
    method: 'POST',
    body: JSON.stringify({ title: '验收事项', dueOn: before.date }),
  }, token);
  await request('/api/buy-items', {
    method: 'POST',
    body: JSON.stringify({ title: '验收物品', estimateCents: 1000 }),
  }, token);
  await request('/api/media', {
    method: 'POST',
    body: JSON.stringify({ kind: 'film', title: '验收电影', status: 'done', rating: 4, note: '看完了' }),
  }, token);
  const today = await request('/api/today', {}, token);
  const stats = await request('/api/media/stats', {}, token);
  assert(today.schedule.todayOpen >= 1, '今日日程没有出现');
  assert(today.buy.openCount >= 1, '待买没有出现');
  assert(stats.doneCount >= 2, '年度看完数量没有增加');
  assert(today.media.yearDone === stats.doneCount, '今日与年度统计不一致');

  console.log(JSON.stringify({
    expense: today.finance.expenseCents,
    habit: saved.name,
    bmi: fitness.bmi,
    ma7: fitness.points.find((item) => item.date === before.date).ma7,
    progress: fitness.progress,
    deficit: fitness.deficit,
    yearDone: stats.doneCount,
    score: today.score,
  }, null, 2));
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
