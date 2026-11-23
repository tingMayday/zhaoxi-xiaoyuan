# 朝夕小院 · Android

Kotlin + Jetpack Compose，对接线上 API `https://cct.cdytlj.com`。

## 打开工程

用 **Android Studio**（建议 Ladybug / 2024.2+）打开本目录 `apps/android`，等待 Gradle Sync。

若需改 API 地址，复制 `local.properties.example` 为 `local.properties` 并修改：

```properties
API_BASE_URL=https://cct.cdytlj.com
```

`sdk.dir` 一般由 Android Studio 自动写入。

## 运行

- 菜单 Run → `app`，或
- `./gradlew :app:assembleDebug`（需 JDK 17 与 Android SDK）

## 当前范围

- 登录 / 注册（JWT 存 DataStore）
- 底部导航：今日、记账、习惯、健身、更多
- 今日页：分数、寄语、四格统计（`GET /api/today`）
- 其余 Tab 为占位页
