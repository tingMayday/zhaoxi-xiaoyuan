import { request } from '..';

export interface FitnessSeries {
  today: string;
  bmi: number | null;
  progress: number | null;
  deficit: number | null;
  latest: { weightKg: number | null } | null;
  todayLog: { exerciseMin: number | null } | null;
  profile: { startWeightKg: number; targetWeightKg: number; startDate: string; targetIntakeKcal: number } | null;
  points: { date: string; weightKg: number | null; ma7: number | null }[];
}

export interface WeekPlanItem { weekday: number; title: string; detail: string }

export interface BodyLogInput {
  weightKg?: number;
  bodyFatPct?: number;
  intakeKcal?: number;
  exerciseMin?: number;
}

export function fetchSeries() {
  return request<FitnessSeries>('/api/fitness/series');
}

export function saveBodyLog(date: string, body: BodyLogInput) {
  return request(`/api/body-logs/${date}`, { method: 'PUT', json: body });
}

export function saveFitnessProfile(body: { startWeightKg: number; targetWeightKg: number; startDate: string; targetIntakeKcal: number }) {
  return request('/api/fitness/profile', { method: 'PUT', json: body });
}

export function fetchWeekPlan() {
  return request<WeekPlanItem[]>('/api/week-plan');
}

export function saveWeekPlan(items: WeekPlanItem[]) {
  return request('/api/week-plan', { method: 'PUT', json: { items } });
}
