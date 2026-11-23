/** 习惯和当日打卡的入参。kind 为 check、count 或 value。 */
import { IsBoolean, IsIn, IsNumber, IsOptional, IsString, MaxLength, Min } from 'class-validator';

export class CreateHabitDto {
  @IsString()
  @MaxLength(30)
  name!: string;

  @IsIn(['check', 'count', 'value'])
  kind!: 'check' | 'count' | 'value';

  @IsOptional()
  @IsString()
  @MaxLength(10)
  unit?: string;

  @IsOptional()
  @IsNumber()
  @Min(0.01)
  dailyTarget?: number;
}

export class UpdateHabitDto {
  @IsOptional()
  @IsString()
  @MaxLength(30)
  name?: string;

  @IsOptional()
  @IsString()
  @MaxLength(10)
  unit?: string;

  @IsOptional()
  @IsNumber()
  @Min(0.01)
  dailyTarget?: number;

  @IsOptional()
  @IsBoolean()
  archived?: boolean;
}

export class HabitLogDto {
  @IsNumber()
  @Min(0)
  value!: number;
}
