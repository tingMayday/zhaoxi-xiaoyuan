/** 前端接口统一出口。页面从这里引入，不必关心具体文件。 */

// 请求客户端：附带登录令牌，处理 401 与错误信息
export { getToken, request, setToken } from '.';

// 账号：登录、注册、读取和修改个人资料
export { fetchProfile, login, register, updateProfile } from './apis/auth';
export type { AuthSession, Profile } from './apis/auth';

// 待买：清单、加入、标为买到、修改、删除
export { fetchBuyItems, createBuyItem, markBought, updateBuyItem, removeBuyItem } from './apis/buy';
export type { BuyBoard, BuyItem, BuyPatch } from './apis/buy';

// 健身：体重曲线、身体记录、减脂目标和一周安排
export { fetchSeries, fetchWeekPlan, saveBodyLog, saveFitnessProfile, saveWeekPlan } from './apis/fitness';
export type { BodyLogInput, FitnessSeries, WeekPlanItem } from './apis/fitness';

// 习惯：列表、新建、当日打卡、归档
export { archiveHabit, createHabit, listHabits, saveHabitLog } from './apis/habits';
export type { Habit, HabitBoard } from './apis/habits';

// 记账：分类、月度汇总与对比、流水和预算
export {
  archiveCategory,
  createCategory,
  createTransaction,
  fetchCompare,
  fetchSummary,
  listCategories,
  listTransactions,
  removeTransaction,
  saveBudget,
} from './apis/ledger';
export type { Category, FinanceSummary, MonthCompare, NewTransaction, Transaction } from './apis/ledger';

// 收藏：书影音列表、年度统计、新建、修改、删除和封面上传
export { createMedia, fetchMediaStats, listMedia, updateMedia, removeMedia, uploadCover } from './apis/media';
export type { MediaItem, MediaKind, MediaPatch, MediaStats, MediaStatus, NewMedia } from './apis/media';

// 日程：区间列表、新建、修改、删除
export {
  createSchedule,
  fetchSchedule,
  removeSchedule,
  scheduleClock,
  scheduleDateTimeLocal,
  scheduleIsoToLocal,
  scheduleLocalToIso,
  updateSchedule,
} from './apis/schedule';
export type { ScheduleBoard, ScheduleItem, SchedulePatch, ScheduleWrite } from './apis/schedule';


// 时光档案：按日期折起的收支、身体、日程和待买
export { fetchArchive } from './apis/archive';
export type { ArchiveBoard, ArchiveDay, ArchiveEntry, ArchiveKind } from './apis/archive';

// 今日：首页聚合的分数、收支、习惯、日程和体重
export { fetchToday } from './apis/today';
export type { TodayHabit, TodayOverview, TodayRecent, TodayTask } from './apis/today';
