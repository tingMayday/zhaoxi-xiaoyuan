import { BadRequestException, Injectable, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcryptjs';
import { PrismaService } from '../common/prisma.service';
import { LoginDto, RegisterDto, UpdateMeDto } from './dto';

/** 新用户自带的分类，之后可以在记账里归档。 */
const DEFAULT_CATEGORIES: { name: string; kind: 'income' | 'expense'; sort: number }[] = [
  { name: '餐饮', kind: 'expense', sort: 1 },
  { name: '交通', kind: 'expense', sort: 2 },
  { name: '购物', kind: 'expense', sort: 3 },
  { name: '居住', kind: 'expense', sort: 4 },
  { name: '娱乐', kind: 'expense', sort: 5 },
  { name: '医疗', kind: 'expense', sort: 6 },
  { name: '其他', kind: 'expense', sort: 7 },
  { name: '工资', kind: 'income', sort: 1 },
  { name: '其他收入', kind: 'income', sort: 2 },
];

/** 返回给前端的资料，去掉密码哈希。 */
export function publicUser(user: {
  id: string;
  email: string;
  name: string;
  timezone: string;
  heightCm: number | null;
}) {
  return {
    id: user.id,
    email: user.email,
    name: user.name,
    timezone: user.timezone,
    heightCm: user.heightCm,
  };
}

@Injectable()
export class AuthService {
  constructor(
    private prisma: PrismaService,
    private jwt: JwtService,
  ) {}

  private async tokenFor(user: { id: string; email: string }) {
    return this.jwt.signAsync({ sub: user.id, email: user.email });
  }

  async register(dto: RegisterDto) {
    const email = dto.email.trim().toLowerCase();
    const exists = await this.prisma.user.findUnique({ where: { email } });
    if (exists) throw new BadRequestException('这个邮箱已经注册过了');
    const passwordHash = await bcrypt.hash(dto.password, 10);
    const user = await this.prisma.user.create({
      data: {
        email,
        passwordHash,
        name: dto.name?.trim() || '院主',
        categories: { create: DEFAULT_CATEGORIES },
      },
    });
    return { token: await this.tokenFor(user), user: publicUser(user) };
  }

  async login(dto: LoginDto) {
    const email = dto.email.trim().toLowerCase();
    const user = await this.prisma.user.findUnique({ where: { email } });
    if (!user) throw new UnauthorizedException('邮箱或密码不对');
    const ok = await bcrypt.compare(dto.password, user.passwordHash);
    if (!ok) throw new UnauthorizedException('邮箱或密码不对');
    return { token: await this.tokenFor(user), user: publicUser(user) };
  }

  async updateMe(
    userId: string,
    dto: UpdateMeDto,
  ) {
    const user = await this.prisma.user.update({
      where: { id: userId },
      data: {
        name: dto.name?.trim(),
        timezone: dto.timezone?.trim(),
        heightCm: dto.heightCm,
      },
    });
    return publicUser(user);
  }
}
