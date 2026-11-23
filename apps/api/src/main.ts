/** 启动接口服务：统一前缀 /api，校验入参，并公开封面目录。 */
import { config } from 'dotenv';
import { mkdirSync } from 'fs';
import { resolve } from 'path';
import { NestFactory } from '@nestjs/core';
import { ValidationPipe } from '@nestjs/common';
import { NestExpressApplication } from '@nestjs/platform-express';
import { AppModule } from './app.module';

config({ path: resolve(process.cwd(), '.env') });

async function bootstrap() {
  const uploadDir = resolve(process.cwd(), 'uploads');
  mkdirSync(uploadDir, { recursive: true });

  const app = await NestFactory.create<NestExpressApplication>(AppModule);
  app.setGlobalPrefix('api');
  app.useStaticAssets(uploadDir, { prefix: '/api/uploads/' });
  app.enableCors();
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
    }),
  );
  await app.listen(process.env.PORT || 3000);
}

bootstrap();
