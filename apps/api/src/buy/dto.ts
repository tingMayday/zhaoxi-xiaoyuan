/** 待买条目的入参。estimateCents 是单价，单位为分。 */
import { IsIn, IsInt, IsNumber, IsOptional, IsString, Matches, MaxLength, Min } from 'class-validator';
import { DATE } from '../common/patterns';

export class CreateBuyDto {
  @IsString()
  @MaxLength(80)
  title!: string;

  @IsOptional()
  @IsNumber()
  @Min(0.01)
  quantity?: number;

  @IsOptional()
  @IsInt()
  @Min(0)
  estimateCents?: number;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;
}

export class UpdateBuyDto {
  @IsOptional()
  @IsString()
  @MaxLength(80)
  title?: string;

  @IsOptional()
  @IsNumber()
  @Min(0.01)
  quantity?: number;

  @IsOptional()
  @IsInt()
  @Min(0)
  estimateCents?: number;

  @IsOptional()
  @IsIn(['open', 'bought'])
  status?: 'open' | 'bought';

  @IsOptional()
  @Matches(DATE, { message: '日期格式应为 YYYY-MM-DD' })
  boughtOn?: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  note?: string;
}
