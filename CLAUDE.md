# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

**Focus** 是一款本地 RSS 阅读器 Android 应用，已暂停维护。项目支持 RSS/Atom feed 订阅、文件夹管理、夜间模式、阅读增强、OPML 导入/导出等功能。

**包名：** `com.ihewro.focus`
**当前版本：** 2.17 (versionCode 31)
**SDK 版本：** minSdk 21, targetSdk 28, compileSdk 28

## 构建命令

### Gradle 构建
```bash
# 构建调试版本
./gradlew assembleDebug

# 构建发布版本
./gradlew assembleRelease

# 清理构建产物
./gradlew clean
```

### APK 输出位置
构建完成后 APK 位于：`app/release/` 或 `app/build/outputs/apk/`

APK 命名格式：`focus-{versionName}-{buildType}-{abi}.apk`

支持架构：`x86`（模拟器）、`arm64-v8a`（64位手机）

## 技术栈

### 核心框架
- **数据库：** LitePal 3.0.0（轻量级 ORM）
- **网络请求：** OkHttp 3.12.0 + Retrofit 2.6.1
- **依赖注入：** ButterKnife 8.8.1
- **事件总线：** EventBus 3.1.1

### UI 组件
- **侧边栏：** MaterialDrawer 6.0.9
- **下拉刷新：** SmartRefreshLayout 1.1.0
- **弹窗：** MaterialDialogs 0.9.6.0、XPopup 1.8.4
- **主题切换：** skin-support 3.1.1
- **WebView：** AgentWeb 4.0.3-beta

### 工具库
- **HTML 解析：** Jsoup 1.10.1
- **图片加载：** Universal Image Loader 1.9.5
- **日志：** ALog 1.8.0
- **Toast：** Toasty 1.4.0
- **工具集合：** Guava 27.1-android

## 项目架构

### 核心数据模型
数据库配置位于 `app/src/main/assets/litepal.xml`，当前数据库版本 33。

主要数据实体（`bean/` 目录）：
- `Feed`：RSS 源信息
- `FeedItem`：RSS 文章条目
- `FeedFolder`：RSS 源文件夹
- `FeedRequest`：feed 请求记录
- `Collection`/`CollectionFolder`：收藏相关
- `UserPreference`：用户偏好设置

### 核心功能模块

**RSS 解析（`util/`）：**
- `FeedParser`：解析 RSS/Atom XML 格式
- `AtomParser`：专门处理 Atom 格式
- `OPMLReadHelper`：OPML 文件导入
- `OPMLCreateHelper`：OPML 文件导出

**界面层（`activity/`）：**
- `MainActivity`：主界面（侧边栏 + Tab + Feed 列表）
- `FeedListActivity`：Feed 源列表
- `PostDetailActivity`：文章详情页
- `FeedManageActivity`：Feed 管理
- `SettingActivity`：设置页面

**后台任务（`task/`）：**
- `RequestFeedListDataService`：请求 Feed 数据的 Service
- `TimingService`：定时更新服务
- `AutoUpdateReceiver`：自动更新广播接收器
- `FixDataTask`/`RecoverDataTask`：数据修复和恢复

### WebView 内容处理
文章详情使用 WebView 渲染，相关资源：
- `app/src/main/assets/css/webview.css`：文章样式
- `app/src/main/assets/js/webview.js`：WebView 交互脚本
- `util/HtmlUtil`：HTML 内容处理
- `util/WebViewUtil`：WebView 配置

## 重要注意事项

### 网络安全配置
项目使用自定义网络安全配置：`@xml/network_security_config`
- 支持明文流量（HTTP）
- TLS 配置通过 `util/Tls12SocketFactory` 处理

### 多主题支持
使用 skin-support 框架，日/夜间模式切换。主题资源位于：
- `src/main/res/`：日间模式
- `src/main/res-night/`：夜间模式

### 异常处理
全局异常恢复使用 `Recovery` 框架，崩溃后恢复到 `MainActivity`。

### 侧滑返回
使用 `SlideBack` 库实现边缘侧滑返回功能。

### 权限要求
- `INTERNET`：网络请求
- `READ/WRITE_EXTERNAL_STORAGE`：OPML 导入导出、图片缓存
- `ACCESS_NETWORK_STATE`：网络状态检测
- `FOREGROUND_SERVICE`：后台服务
- `ACCESS_WIFI_STATE`：WiFi 状态

## 开发提示

### 日志输出
使用 `ALog` 工具类，在 `MyApplication.java` 中配置。`BuildConfig.DEBUG` 控制日志开关。

### 图片加载
通过 `ImageLoaderManager` 初始化 Universal Image Loader。

### EventBus 通信
组件间通信使用 EventBus，主要事件类型为 `EventMessage`。

### 数据库操作
所有数据模型继承 LitePal 基类，使用 LitePal API 进行 CRUD 操作。
