import {
  BadRequestException,
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Patch,
  Post,
  Query,
  Req,
  UploadedFile,
  UseInterceptors,
} from '@nestjs/common';
import { FileInterceptor } from '@nestjs/platform-express';
import { diskStorage } from 'multer';
import { extname, join } from 'path';
import { randomBytes } from 'crypto';
import { User } from '@prisma/client';
import { PrismaService } from '../common/prisma.service';
import { zonedToday } from '../common/dates';
import { CreateMediaDto, UpdateMediaDto } from './dto';

const ALLOWED = new Set(['.jpg', '.jpeg', '.png', '.webp', '.gif']);

/** 收藏：书影音列表、年度统计、封面上传和删除。 */
@Controller('media')
export class MediaController {
  constructor(private prisma: PrismaService) {}

  /** 按状态或类型筛选。非法的筛选值会被忽略。 */
  @Get()
  list(@Req() req: { user: User }, @Query('status') status?: string, @Query('kind') kind?: string) {
    return this.prisma.mediaItem.findMany({
      where: {
        userId: req.user.id,
        status: isStatus(status) ? status : undefined,
        kind: isKind(kind) ? kind : undefined,
      },
      orderBy: [{ finishedOn: 'desc' }, { createdAt: 'desc' }],
    });
  }

  /** 某年看完的数量、平均星级，以及书、电影、剧集、专辑各多少。 */
  @Get('stats')
  async stats(@Req() req: { user: User }, @Query('year') yearText?: string) {
    const today = zonedToday(req.user.timezone);
    const year = yearText || today.slice(0, 4);
    const items = await this.prisma.mediaItem.findMany({
      where: { userId: req.user.id, status: 'done', finishedOn: { startsWith: year } },
    });
    const rated = items.filter((item) => item.rating != null);
    const kinds = ['book', 'film', 'series', 'album'].map((kind) => ({
      kind,
      count: items.filter((item) => item.kind === kind).length,
    }));
    return {
      year,
      doneCount: items.length,
      averageRating: rated.length
        ? Math.round((rated.reduce((sum, item) => sum + (item.rating as number), 0) / rated.length) * 10) / 10
        : null,
      kinds,
    };
  }

  /** 上传封面，限制 5MB，只接受常见图片。返回可访问的地址。 */
  @Post('cover')
  @UseInterceptors(
    FileInterceptor('file', {
      storage: diskStorage({
        destination: join(process.cwd(), 'uploads'),
        filename: (_req, file, cb) => {
          const ext = extname(file.originalname).toLowerCase();
          cb(null, `${Date.now()}-${randomBytes(4).toString('hex')}${ext}`);
        },
      }),
      limits: { fileSize: 5 * 1024 * 1024 },
      fileFilter: (_req, file, cb) => {
        const ext = extname(file.originalname).toLowerCase();
        if (!ALLOWED.has(ext)) {
          cb(new BadRequestException('封面只接受 jpg、png、webp、gif'), false);
          return;
        }
        cb(null, true);
      },
    }),
  )
  upload(@UploadedFile() file?: Express.Multer.File) {
    if (!file) throw new BadRequestException('请选择图片');
    return { url: `/api/uploads/${file.filename}` };
  }

  /** 收入收藏。状态是看完且没写日期时，完成日用今天。 */
  @Post()
  create(@Req() req: { user: User }, @Body() dto: CreateMediaDto) {
    const status = dto.status || 'want';
    return this.prisma.mediaItem.create({
      data: {
        userId: req.user.id,
        kind: dto.kind,
        title: dto.title.trim(),
        creator: dto.creator?.trim() || '',
        coverUrl: dto.coverUrl?.trim() || '',
        status,
        rating: dto.rating ?? null,
        note: dto.note?.trim() || '',
        finishedOn: status === 'done' ? dto.finishedOn || zonedToday(req.user.timezone) : dto.finishedOn || null,
      },
    });
  }

  /** 修改收藏。改成看完且没有完成日时补上今天。 */
  @Patch(':id')
  async update(@Req() req: { user: User }, @Param('id') id: string, @Body() dto: UpdateMediaDto) {
    const current = await this.own(req.user.id, id);
    const status = dto.status ?? current.status;
    let finishedOn = dto.finishedOn === undefined ? current.finishedOn : dto.finishedOn;
    if (status === 'done' && !finishedOn) finishedOn = zonedToday(req.user.timezone);
    if (status !== 'done' && dto.status && dto.finishedOn === undefined) finishedOn = null;
    return this.prisma.mediaItem.update({
      where: { id },
      data: {
        kind: dto.kind,
        title: dto.title?.trim(),
        creator: dto.creator?.trim(),
        coverUrl: dto.coverUrl?.trim(),
        status,
        rating: dto.rating === undefined ? undefined : dto.rating,
        note: dto.note?.trim(),
        finishedOn,
      },
    });
  }

  /** 删除一条收藏。封面文件仍留在上传目录。 */
  @Delete(':id')
  async remove(@Req() req: { user: User }, @Param('id') id: string) {
    await this.own(req.user.id, id);
    await this.prisma.mediaItem.delete({ where: { id } });
    return { ok: true };
  }

  private async own(userId: string, id: string) {
    const item = await this.prisma.mediaItem.findFirst({ where: { id, userId } });
    if (!item) throw new BadRequestException('找不到这条收藏');
    return item;
  }
}

function isStatus(value?: string): value is 'want' | 'doing' | 'done' | 'dropped' {
  return value === 'want' || value === 'doing' || value === 'done' || value === 'dropped';
}

function isKind(value?: string): value is 'book' | 'film' | 'series' | 'album' {
  return value === 'book' || value === 'film' || value === 'series' || value === 'album';
}
