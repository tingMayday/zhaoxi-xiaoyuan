/** 注册、登录和修改资料的入参。 */
import { IsEmail, IsNumber, IsOptional, IsString, Max, MaxLength, Min, MinLength } from 'class-validator';

export class RegisterDto {
  @IsEmail({}, { message: '请填写有效邮箱' })
  email!: string;

  @IsString()
  @MinLength(6, { message: '密码至少 6 位' })
  password!: string;

  @IsOptional()
  @IsString()
  @MaxLength(40)
  name?: string;
}

export class LoginDto {
  @IsEmail({}, { message: '请填写有效邮箱' })
  email!: string;

  @IsString()
  password!: string;
}

export class UpdateMeDto {
  @IsOptional()
  @IsString()
  @MaxLength(40)
  name?: string;

  @IsOptional()
  @IsString()
  @MaxLength(80)
  timezone?: string;

  @IsOptional()
  @IsNumber()
  @Min(50)
  @Max(250)
  heightCm?: number;
}
