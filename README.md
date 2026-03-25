# 玩Android

一个基于 [WanAndroid 开放 API](https://www.wanandroid.com/blog/show/2) 的 Android 学习项目，应用名称为“玩Android”。

项目当前实现了首页 Banner、文章列表、知识体系、作者搜索、登录注册、Cookie 持久化登录、主题切换以及文章 `WebView` 详情页，适合作为 Android 客户端开发练手项目。

## 项目特性

- 首页：支持 Banner 轮播、文章流展示、下拉刷新和上拉分页
- 文章：独立文章列表页，支持分页加载和网页详情跳转
- 体系：支持知识体系树展示、二级分类文章浏览、按作者昵称搜索文章
- 我的：支持登录、注册、退出登录、用户信息展示
- 登录态：基于 Cookie 持久化保存登录状态
- 主题：支持跟随系统、浅色模式、深色模式切换
- 详情页：内置 `WebView`，并对部分外部恶意跳转做了拦截

## 技术栈

- Kotlin
- Android View + Fragment
- Jetpack Compose（当前主要用于设置页）
- ViewModel
- Retrofit + Gson
- OkHttp + CookieJar
- Material Components
- RecyclerView / ViewPager2 / SwipeRefreshLayout

## 接口来源

- 开放 API 文档：<https://www.wanandroid.com/blog/show/2>
- 接口基础地址：`https://www.wanandroid.com/`

项目当前实际使用到的接口包括：

- `GET /banner/json`
- `GET /article/list/{page}/json`
- `GET /tree/json`
- `POST /user/login`
- `POST /user/register`
- `GET /user/logout/json`
- `GET /user/lg/userinfo/json`

## 项目结构

```text
app/src/main/java/com/dawn/fade
├── data        # 数据模型、网络层、仓库、Cookie 与会话管理
├── main        # 首页、文章、体系、我的等主功能模块
├── page        # 设置页
├── theme       # 主题设置与应用初始化
├── ui          # Compose 主题资源
└── web         # WebView 详情页
```

## 运行环境

- Android Studio
- JDK 11
- minSdk 24
- targetSdk 35

## 本地运行

1. 克隆项目到本地
2. 使用 Android Studio 打开项目根目录
3. 等待 Gradle 同步完成
4. 连接模拟器或真机后直接运行 `app`

## 说明

- 本项目主要用于学习和交流，数据内容来自 WanAndroid 开放接口
- 如需用于二次开发，请遵循 WanAndroid 接口文档及站点相关规范
