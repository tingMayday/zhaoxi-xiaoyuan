/** 把登录、数据库和各业务接口装到同一个应用里。 */
import './common/load-env';
import { Module } from '@nestjs/common';
import { APP_GUARD } from '@nestjs/core';
import { JwtModule } from '@nestjs/jwt';
import { PrismaService } from './common/prisma.service';
// 登录与令牌
import { JwtStrategy } from './auth/jwt.strategy';
import { JwtAuthGuard } from './auth/auth.guard';
import { AuthService } from './auth/auth.service';
import { AuthController } from './auth/auth.controller';
// 各业务接口。每个目录里是控制器和该模块的入参校验。
import { LedgerController } from './ledger/ledger.controller';
import { HabitsController } from './habits/habits.controller';
import { FitnessController } from './fitness/fitness.controller';
import { ScheduleController } from './schedule/schedule.controller';
import { BuyController } from './buy/buy.controller';
import { MediaController } from './media/media.controller';
import { TodayController } from './today/today.controller';
import { ArchiveController } from './archive/archive.controller';

@Module({
  imports: [
    JwtModule.register({
      secret: process.env.JWT_SECRET || 'zhaoxi-xiaoyuan-dev-secret',
      signOptions: { expiresIn: '30d' },
    }),
  ],
  controllers: [
    AuthController,
    LedgerController,
    HabitsController,
    FitnessController,
    ScheduleController,
    BuyController,
    MediaController,
    TodayController,
    ArchiveController,
  ],
  providers: [
    PrismaService,
    JwtStrategy,
    AuthService,
    { provide: APP_GUARD, useClass: JwtAuthGuard },
  ],
})
export class AppModule {}
