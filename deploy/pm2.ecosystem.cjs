/** 宝塔 PM2 管理器：添加项目时可选用「配置文件」指向本文件。 */
module.exports = {
  apps: [
    {
      name: 'zhaoxi-api',
      cwd: '/www/wwwroot/zhaoxi-xiaoyuan/apps/api',
      script: 'dist/main.js',
      instances: 1,
      autorestart: true,
      max_memory_restart: '300M',
      env: {
        NODE_ENV: 'production',
      },
    },
  ],
};
