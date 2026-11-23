export type Kind = 'book' | 'film' | 'series' | 'album';
export type Status = 'want' | 'doing' | 'done' | 'dropped';

export const kindLabel: Record<Kind, string> = { book: '图书', film: '电影', series: '剧集', album: '专辑' };

export function coverOf(kind: Kind, coverUrl?: string) {
  return coverUrl?.trim() ? coverUrl : `/covers/${kind}.svg`;
}
export const statusLabel: Record<Status, string> = { want: '想看', doing: '在看', done: '看完', dropped: '搁置' };
