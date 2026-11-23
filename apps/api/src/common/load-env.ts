/** 在读取配置前先载入 apps/api/.env。 */
import { config } from 'dotenv';
import { resolve } from 'path';

config({ path: resolve(process.cwd(), '.env') });
