import { ExecutionContext, Injectable, SetMetadata, UnauthorizedException } from '@nestjs/common';
import { Reflector } from '@nestjs/core';
import { AuthGuard } from '@nestjs/passport';

/** 标在不需要登录的接口上，例如注册和健康检查。 */
export const IS_PUBLIC = 'isPublic';
export const Public = () => SetMetadata(IS_PUBLIC, true);

/** 默认拦住所有接口。带 Public 标记的直接放行，其余检查 Bearer 令牌。 */

@Injectable()
export class JwtAuthGuard extends AuthGuard('jwt') {
  constructor(private reflector: Reflector) {
    super();
  }

  canActivate(context: ExecutionContext) {
    const isPublic = this.reflector.getAllAndOverride<boolean>(IS_PUBLIC, [
      context.getHandler(),
      context.getClass(),
    ]);
    if (isPublic) return true;
    return super.canActivate(context);
  }
}

/** 确认令牌解析出的是一个用户，否则按未登录处理。 */
export function assertUser(user: unknown): asserts user is {
  id: string;
  email: string;
  name: string;
  timezone: string;
  heightCm: number | null;
  passwordHash: string;
} {
  if (!user || typeof user !== 'object' || !('id' in user)) {
    throw new UnauthorizedException('请先登录');
  }
}
