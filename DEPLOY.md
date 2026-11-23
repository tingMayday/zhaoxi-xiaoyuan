# 朝夕小院 · 宝塔部署说明

面向阿里云 ECS + **宝塔 Linux 面板**、约 **1 GiB 内存** 的机器。  
推荐：**本机构建 → 上传产物 → 服务器只跑 MySQL + Nginx + Node**。

公网示例：`http://8.137.88.43`（以你实例为准）。

---

## 一、阿里云与宝塔准备

1. **安全组**放行：`22`、`80`、`443`、宝塔面板端口（常见 `8888`）。
2. 浏览器打开面板，登录宝塔。
3. **软件商店**安装：
   - Nginx
   - MySQL 5.7 或 8.0（1G 内存务必把 MySQL 内存调低，避免把机器拖死）
   - PM2 管理器（或 Node 版本管理，装 **Node 20 LTS**）
4. （可选）系统盘加 **1～2G swap**，防止偶发 OOM。

---

## 二、本机打包（Windows 推荐）

在项目根目录 `f:\projects\zhaoxi-xiaoyuan`：

```powershell
npm install
npm run build -w @zhaoxi/api
npm run build -w @zhaoxi/web
cd apps\api
npx prisma generate
```

确认存在：

- `apps/api/dist/main.js`
- `apps/web/dist/index.html`

把整个项目（或至少含 `apps/`、`package.json`、`package-lock.json`、`node_modules`）上传到服务器，例如：

`/www/wwwroot/zhaoxi-xiaoyuan`

上传方式任选：宝塔文件管理、Git、SFTP。  
**1G 内存机器不建议在服务器上跑 Vite 完整构建。**

若只上传源码、在服务器安装依赖，可用：

```bash
cd /www/wwwroot/zhaoxi-xiaoyuan
npm ci --omit=dev
cd apps/api && npx prisma generate
```

（前提是本机已生成好 `apps/api/dist` 与 `apps/web/dist` 一并上传。）

---

## 三、数据库

1. 宝塔 → 数据库 → 添加库：`zhaoxi_xiaoyuan`，创建专用用户。
2. 在服务器编辑 `apps/api/.env`（勿提交到 Git）：

```env
DATABASE_URL="mysql://用户:密码@127.0.0.1:3306/zhaoxi_xiaoyuan"
JWT_SECRET="换成足够长的随机字符串"
PORT=3000
```

3. 同步表结构：

```bash
cd /www/wwwroot/zhaoxi-xiaoyuan/apps/api
npx prisma migrate deploy
```

4. （可选）演示数据：

```bash
npx prisma db seed
```

演示账号：`demo@zhaoxi.local` / `demo1234`

---

## 四、PM2 启动接口

工作目录必须是 **`apps/api`**（`.env` 与 `uploads/` 都按这个目录解析）。

### 方式 A：配置文件

使用仓库里的 `deploy/pm2.ecosystem.cjs`（路径按实际上传目录改）。

```bash
cd /www/wwwroot/zhaoxi-xiaoyuan
pm2 start deploy/pm2.ecosystem.cjs
pm2 save
```

### 方式 B：宝塔界面

- 项目名称：`zhaoxi-api`
- 启动文件：`/www/wwwroot/zhaoxi-xiaoyuan/apps/api/dist/main.js`
- 运行目录：`/www/wwwroot/zhaoxi-xiaoyuan/apps/api`

确认：

```bash
curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:3000/api/today
```

未登录应返回 `401`，说明 Nest 已起来。

封面目录：`apps/api/uploads/`，部署更新时**不要清空**该目录。

---

## 五、Nginx 网站

1. 宝塔 → 网站 → 添加站点  
   - 域名：公网 IP 或你的域名  
   - 根目录：`/www/wwwroot/zhaoxi-xiaoyuan/apps/web/dist`
2. 打开站点 → **配置文件**，按 `deploy/nginx-baota.conf` 改 `root` 与两个 `location`。
3. 保存并重载 Nginx。
4. 有域名后：站点 → SSL → 申请 Let’s Encrypt。

---

## 六、验收

浏览器访问 `http://你的公网IP`：

1. 能打开登录页  
2. 注册或演示账号登录  
3. 记一笔账、刷新 `/今日` 不 404  
4. 书影音上传封面后图片能显示  

---

## 七、以后更新

本机重新构建后，上传覆盖：

- `apps/api/dist`
- `apps/web/dist`
- 若改了 Prisma schema：上传 `prisma/` 后在服务器执行 `npx prisma migrate deploy`

然后：

```bash
pm2 restart zhaoxi-api
```

前端静态文件覆盖后一般立刻生效，无需重启 PM2。

---

## 常见问题

| 现象 | 处理 |
|------|------|
| 页面开了但接口失败 | 检查 `/api/` 是否反代到 `127.0.0.1:3000`，PM2 是否在跑 |
| 刷新中文路由 404 | `location /` 需 `try_files ... /index.html` |
| 封面上传后看不到 | 确认 cwd 是 `apps/api`，`uploads` 可写且未被删 |
| 服务器卡住 | MySQL 内存过大或本机构建太重；减 MySQL 配置、加 swap、改在本机构建 |
| Prisma 连库失败 | 检查 `.env` 里账号密码与宝塔数据库是否一致 |

---

## 八、安卓 App

工程在 `apps/android`（Kotlin + Jetpack Compose），**不走 npm workspaces**。

- 默认 API：`https://cct.cdytlj.com`
- 可在 `apps/android/local.properties` 覆盖：`API_BASE_URL=https://你的域名`（参考 `local.properties.example`）
- 用 Android Studio 打开 `apps/android` 目录，Sync 后运行 `app`
- 命令行（需本机已装 JDK 17 + Android SDK）：

```bash
cd apps/android
./gradlew :app:assembleDebug
```

登录后底部五 Tab：今日已对接 `/api/today`；记账 / 习惯 / 健身 / 更多为占位，后续迭代补齐。
