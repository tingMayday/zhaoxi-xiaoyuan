/** 身体记录、减脂目标和一周安排的入参。 */
import { Type } from 'class-transformer';
import { ArrayMaxSize, IsArray, IsInt, IsNumber, IsOptional, IsString, Matches, Max, MaxLength, Min, ValidateNested } from 'class-validator';
import { DATE } from '../common/patterns';

export class UpsertBodyLogDto {
  @IsOptional()
  @IsNumber()
  @Min(20)
  @Max(400)
  weightKg?: number | null;

  @IsOptional()
  @IsNumber()
  @Min(1)
  @Max(70)
  bodyFatPct?: number | null;

  @IsOptional()
  @IsInt()
  @Min(0)
  @Max(20000)
  intakeKcal?: number | null;

  @IsOptional()
  @IsInt()
  @Min(0)
  @Max(2000)
  exerciseMin?: number | null;
}

export class FitnessProfileDto {
  @IsNumber()
  @Min(20)
  @Max(400)
  startWeightKg!: number;

  @IsNumber()
  @Min(20)
  @Max(400)
  targetWeightKg!: number;

  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  startDate!: string;

  @IsInt()
  @Min(0)
  @Max(20000)
  targetIntakeKcal!: number;
}

export class WeekItemDto {
  @IsInt()
  @Min(1)
  @Max(7)
  weekday!: number;

  @IsString()
  @MaxLength(40)
  title!: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  detail?: string;

  @IsOptional()
  @IsInt()
  sort?: number;
}

export class ReplaceWeekPlanDto {
  @IsArray()
  @ArrayMaxSize(40)
  @ValidateNested({ each: true })
  @Type(() => WeekItemDto)
  items!: WeekItemDto[];
}
