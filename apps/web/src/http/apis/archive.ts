import { request } from '..';

export type ArchiveKind = 'finance' | 'health' | 'schedule' | 'buy';

export interface ArchiveEntry {
  kind: ArchiveKind;
  title: string;
  detail: string;
  value: string;
}

export interface ArchiveDay {
  date: string;
  items: ArchiveEntry[];
}

export interface ArchiveBoard {
  today: string;
  days: ArchiveDay[];
}

export function fetchArchive() {
  return request<ArchiveBoard>('/api/archive');
}
