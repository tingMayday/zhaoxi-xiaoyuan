/** 新建和修改收藏的入参。评分是 1 到 5。 */
import { IsIn, IsInt, IsOptional, IsString, Matches, Max, MaxLength, Min } from 'class-validator';
import { DATE } from '../common/patterns';

export class CreateMediaDto {
  @IsIn(['book', 'film', 'series', 'album'])
  kind!: 'book' | 'film' | 'series' | 'album';

  @IsString()
  @MaxLength(80)
  title!: string;

  @IsOptional()
  @IsString()
  @MaxLength(80)
  creator?: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  coverUrl?: string;

  @IsOptional()
  @IsIn(['want', 'doing', 'done', 'dropped'])
  status?: 'want' | 'doing' | 'done' | 'dropped';

  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(5)
  rating?: number;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;

  @IsOptional()
  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  finishedOn?: string;
}

export class UpdateMediaDto {
  @IsOptional()
  @IsIn(['book', 'film', 'series', 'album'])
  kind?: 'book' | 'film' | 'series' | 'album';

  @IsOptional()
  @IsString()
  @MaxLength(80)
  title?: string;

  @IsOptional()
  @IsString()
  @MaxLength(80)
  creator?: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  coverUrl?: string;

  @IsOptional()
  @IsIn(['want', 'doing', 'done', 'dropped'])
  status?: 'want' | 'doing' | 'done' | 'dropped';

  @IsOptional()
  @IsInt()
  @Min(1)
  @Max(5)
  rating?: number | null;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;

  @IsOptional()
  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  finishedOn?: string | null;
}