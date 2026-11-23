/** 分类、流水和预算的入参。金额单位是分。 */
import { IsIn, IsInt, IsOptional, IsString, Matches, MaxLength, Min } from 'class-validator';
import { DATE, MONTH } from '../common/patterns';

export class CreateCategoryDto {
  @IsString()
  @MaxLength(20)
  name!: string;

  @IsIn(['income', 'expense'])
  kind!: 'income' | 'expense';
}

export class CreateTransactionDto {
  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  occurredOn!: string;

  @IsIn(['income', 'expense'])
  direction!: 'income' | 'expense';

  @IsInt()
  @Min(1)
  amountCents!: number;

  @IsString()
  categoryId!: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;
}

export class UpsertBudgetDto {
  @Matches(MONTH, { message: '月份格式应为 YYYY-MM' })
  yearMonth!: string;

  @IsString()
  categoryKey!: string;

  @IsInt()
  @Min(0)
  amountCents!: number;
}
