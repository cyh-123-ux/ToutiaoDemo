# 今日头条 Demo

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Android Min SDK](https://img.shields.io/badge/Android%20Min%20SDK-24+-brightgreen)](https://developer.android.com/about/versions/nougat)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)](https://kotlinlang.org/)

一个基于现代 Android 技术栈开发的仿今日头条新闻客户端 Demo，旨在展示高质量的移动应用开发实践，涵盖新闻列表展示、分类浏览、详情查看、搜索、用户认证等核心功能。
## 🎬 演示视频
- 功能演示：[ToutiaoDemo-演示]
## 🌟 核心特性
- 遵循 MVVM 架构模式，分层清晰、低耦合、高可维护性
- 基于 Jetpack Compose 实现声明式 UI，体验流畅、开发高效
- 支持新闻分类切换、下拉刷新/上拉加载更多
- 本地缓存策略（Room），网络异常时兜底展示数据
- 完整的用户认证流程（登录/注册/退出）
- 搜索功能支持关键词匹配、加载状态管控
- 遵循 Material3 设计规范，界面简洁美观

## 🛠 技术栈
| 类别         | 技术选型                                                                 |
|--------------|--------------------------------------------------------------------------|
| 编程语言     | Kotlin（空安全、协程）                                                   |
| UI 框架      | Jetpack Compose                                                          |
| 架构组件     | ViewModel、Lifecycle                                                     |
| 网络请求     | Retrofit + OkHttp（Logging Interceptor 日志调试）                        |
| 本地存储     | Room 数据库（类型安全的本地持久化）                                      |
| 图片加载     | Coil（Compose 适配、缓存策略）                                           |
| 异步处理     | Kotlin Coroutines（替代回调，简化异步逻辑）                              |
| 导航管理     | Jetpack Navigation（统一页面跳转、参数传递）                             |
| 设计规范     | Material3                                                                |

## 📱 界面展示
### 1. 登录/注册页
- 支持登录/注册标签切换，表单校验（密码长度、非空等）
- 加载状态联动输入框禁用，错误信息实时红色文本反馈
- 登录成功自动跳转首页，未登录拦截需登录页面

### 2. 首页（新闻列表）
- 顶部主题色导航栏 + 横向分类标签（推荐/本地/财经/娱乐）
- 卡片式新闻列表（标题/来源/时间/缩略图），无图自动适配
- 加载中/空数据/错误状态提示，上拉加载更多（防抖动延迟）

### 3. 新闻详情页
- 标题（加粗）+ 来源时间（灰色小字）+ 新闻主图（多字段降级加载）
- WebView 加载新闻正文，无链接时兜底提示
- 本地缓存优先加载，接口失败仍展示兜底数据

### 4. 搜索页
- 搜索框支持软键盘搜索键触发，加载中进度提示
- 无结果文本提示，有结果复用新闻卡片展示
- 协程处理搜索逻辑，避免 UI 阻塞

### 5. 个人中心页
- 已登录：展示头像（加载进度）、用户名、手机号、收藏/已读统计、退出按钮
- 未登录：居中提示登录，一键跳转登录页

## 🚀 快速开始
### 环境要求
- Android Studio Flamingo 或更高版本
- Gradle 8.0+
- Android 7.0（API 24）及以上设备

### 编译运行
```bash
# 克隆仓库
# git@github.com:cyh-123-ux/ToutiaoDemo.git
# 打开项目
# 1. 启动 Android Studio
# 2. 选择 "Open an existing project"
# 3. 选中克隆后的目录

# 运行项目
# 1. 连接 Android 设备或启动模拟器
# 2. 点击 Android Studio 顶部 "Run 'app'" 按钮
