/** 新建和修改日程的入参。 */
import { IsBoolean, IsIn, IsOptional, IsString, Matches, MaxLength } from 'class-validator';
import { DATE } from '../common/patterns';

export class CreateScheduleDto {
  @IsString()
  @MaxLength(80)
  title!: string;

  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  dueOn!: string;

  @IsOptional()
  @IsString()
  startsAt?: string;

  @IsOptional()
  @IsString()
  endsAt?: string;

  @IsOptional()
  @IsBoolean()
  allDay?: boolean;

  @IsOptional()
  @IsString()
  remindAt?: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;
}

export class UpdateScheduleDto {
  @IsOptional()
  @IsString()
  @MaxLength(80)
  title?: string;

  @IsOptional()
  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  dueOn?: string;

  @IsOptional()
  @IsIn(['open', 'done'])
  status?: 'open' | 'done';

  @IsOptional()
  @IsBoolean()
  allDay?: boolean;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;

  @IsOptional()
  @IsString()
  startsAt?: string | null;

  @IsOptional()
  @IsString()
  endsAt?: string | null;

  @IsOptional()
  @IsString()
  remindAt?: string | null;
}
