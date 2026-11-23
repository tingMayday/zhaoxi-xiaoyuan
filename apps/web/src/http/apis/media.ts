import { request } from '..';

export type MediaKind = 'book' | 'film' | 'series' | 'album';
export type MediaStatus = 'want' | 'doing' | 'done' | 'dropped';

export interface MediaItem {
  id: string;
  kind: MediaKind;
  title: string;
  creator: string;
  coverUrl: string;
  status: MediaStatus;
  rating: number | null;
  note: string;
}

export interface MediaStats {
  year: string;
  doneCount: number;
  averageRating: number | null;
  kinds: { kind: MediaKind; count: number }[];
}

export interface NewMedia {
  kind: MediaKind;
  title: string;
  creator: string;
  coverUrl: string;
  status: MediaStatus;
  note: string;
  rating?: number;
}

export function listMedia() {
  return request<MediaItem[]>('/api/media');
}

export function fetchMediaStats() {
  return request<MediaStats>('/api/media/stats');
}

export function createMedia(body: NewMedia) {
  return request('/api/media', { method: 'POST', json: body });
}

export function removeMedia(id: string) {
  return request(`/api/media/${id}`, { method: 'DELETE' });
}

export async function uploadCover(file: File) {
  const body = new FormData();
  body.append('file', file);
  return request<{ url: string }>('/api/media/cover', { method: 'POST', body });
}
