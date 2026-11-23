import { Body, Controller, Get, Patch, Post, Req } from '@nestjs/common';
import { User } from '@prisma/client';
import { AuthService, publicUser } from './auth.service';
import { Public } from './auth.guard';
import { LoginDto, RegisterDto, UpdateMeDto } from './dto';

/** 注册、登录和个人资料。除健康检查外，登录相关接口不需要令牌。 */
@Controller()
export class AuthController {
  constructor(private auth: AuthService) {}

  /** 新建账号，并带上一组默认收支分类。 */
  @Public()
  @Post('auth/register')
  register(@Body() dto: RegisterDto) {
    return this.auth.register(dto);
  }

  /** 校验邮箱密码，返回 30 天令牌。 */
  @Public()
  @Post('auth/login')
  login(@Body() dto: LoginDto) {
    return this.auth.login(dto);
  }

  /** 给部署检查用，不涉及用户数据。 */
  @Public()
  @Get('health')
  health() {
    return { ok: true };
  }

  /** 当前登录用户，不含密码。 */
  @Get('me')
  me(@Req() req: { user: User }) {
    return publicUser(req.user);
  }

  /** 修改称呼、时区和身高。身高用于计算 BMI。 */
  @Patch('me')
  updateMe(@Req() req: { user: User }, @Body() dto: UpdateMeDto) {
    return this.auth.updateMe(req.user.id, dto);
  }
}
