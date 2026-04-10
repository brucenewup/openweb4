# SPA 重构计划

## 目标

将6个独立的 Thymeleaf 页面合并为真正的单页应用，消除页面跳转闪屏。

## 方案

### 1. 路由：Hash-based (#/dashboard, #/news, ...)

- 不需要修改服务端路由配置
- 浏览器前进/后退自然支持
- 刷新页面自动恢复到正确的子页面

### 2. 可伸缩侧边栏

- 展开 220px / 收起 60px（仅显示图标）
- 收起/展开状态保存到 localStorage
- 过渡动画 300ms

### 3. 6个页面渲染器（纯 JS，无框架）


| 页面              | API 端点                         | 特殊处理                                          |
| --------------- | ------------------------------ | --------------------------------------------- |
| dashboard       | /api/overview                  | Chart.js 柱状图                                  |
| news            | /api/news?page=&size=          | 分页 + 自动刷新（与 KOL 推文一致；未传 page/size 时全量）        |
| kol-tweets      | /api/tweets/latest?page=&size= | 分页卡片；未传 page/size 时返回全量（兼容旧客户端）；服务端按优先大V+时间排序 |
| market-forecast | /api/market-forecast           | Chart.js 折线图                                  |
| market-indices  | /api/market-indices            | 指数卡片网格                                        |
| ai-chat         | /api/chat/stream               | SSE 流式、保持聊天记录                                 |


### 4. 页面切换动画

- fadeIn 0.3s ease

### 5. API 端点补全

- /api/market-forecast 增加 powerLawFairValue, currentVsPowerLaw, drawdownFromAth, dataSource
- /api/market-indices 增加 dataSource

### 6. 文件变更

- 新建 `templates/spa.html` — 单页应用主模板
- 修改 `DashboardController` — `/` 和 `/dashboard` 都指向 spa.html
- 修改 `MarketController` — 补全 API 字段
- 保留旧模板文件（不删除，但不再是主入口）

---

## 功能路线图（Feature Roadmap）

> 来源：2026-03-20 规划会议 + 代码审查发现
> 更新：2026-03-20 23:10

### 已修复（2026-03-20）

- **大V推文「加载失败」**：根因是冷缓存时 `GET /api/tweets/latest` 同步跑完整轮 RSS，耗时过长导致浏览器/连接中断，`spa.html` 的 `fetchJson` 进入 `catch` 显示 `load_fail`。现改为冷启动先快速返回静态大V卡片并 **后台异步** `fetchLatestTweets()` 填充缓存，首屏 API 快速返回 JSON。

### 已修复（2026-03-21，开源安全）

- 移除 `ApiController` 中硬编码的 AI 密钥与临时调试接口（`/api/debug/proxy-test`），避免公开仓库泄露凭证。
- 移除 `aiHttpClient` 的请求/响应头日志拦截器，避免日志中包含 `Authorization` 等敏感信息。
- 修复 `spa.html` 聊天用户消息渲染的潜在 XSS 风险（用户消息改为 `textContent` 渲染）。
- 降低生产异常详情暴露（`server.error.include-message` 默认值收紧）。

### P0 — 核心价值，最优先


| #   | 功能             | 说明                                                            | 依赖         | 状态 |
| --- | -------------- | ------------------------------------------------------------- | ---------- | --- |
| 1   | **接入真实鲸鱼 API** | WhaleTrackingService 当前为模拟数据，接入 Whale Alert / Lookonchain API | API Key    | ✅ 已完成 |
| 2   | **恐贪指数**       | 接入 Alternative.me Fear & Greed Index（免费无需 Key），展示在仪表盘         | 无          | ✅ 已完成 |
| 3   | **AI 每日市场简报**  | 每天早上定时调用 AI 生成加密市场摘要，推送到仪表盘                                   | AI_API_KEY | ✅ 已完成 |


### P1 — 高价值


| #   | 功能             | 说明                                      | 依赖       | 状态 |
| --- | -------------- | --------------------------------------- | -------- | --- |
| 4   | **永续合约多空比面板**  | 接入 Binance / OKX 多空比数据，展示主流合约情绪         | 无（公开API） | ✅ 已完成 |
| 5   | **Token 解锁日历** | 接入 Token Unlocks / Vesting 数据，展示近期解锁事件  | API Key  | ✅ 已完成 |
| 6   | **合约审计速览**     | 快速显示主流 DeFi 协议审计状态（DeFiSafety / CertiK） | 无        | ✅ 已完成 |


### P2 — 中优先


| #   | 功能            | 说明                                                                                   | 依赖            | 状态 |
| --- | ------------- | ------------------------------------------------------------------------------------ | ------------- | --- |
| 7   | **山寨币热度雷达**   | 展示近期交易量/搜索热度异常上涨的山寨币                                                                 | CoinGecko API | ✅ 已完成 |
| 8   | **KOL 情绪分析**  | 对已抓取 KOL 推文做情绪评分（正面/负面/中性）                                                           | AI_API_KEY    | ✅ 已完成 |
| 9   | **加密市值热力图**   | 类 TradingView 市值/涨跌热力图（可用 D3.js 实现）                                                  | CoinGecko API | ✅ 已完成 |
| 10  | **交易所能力对比卡片** | 基于 Binance / OKX / Gate 等公开资料，做"链支持 / 合约 / Earn / Alpha / BSC 生态"维度的静态对比卡片，帮助用户快速选平台 | 公开资料 / 人工维护   | ✅ 已完成 |


### URL 分析结论（2026-03-22）

> 评估链接：`https://www.binance.com/zh-CN/square/post/302373026304626`
> 主题要点：该文核心是 **6 家交易所（Binance/OKX/Bitget/Bybit/Gate/Coinbase）的 Skills 能力对比**，偏 AI Agent + 交易执行工具链。

**最适合接入 openweb4 的项：**

- ✅ **优先接入「交易所能力对比卡片（只读版）」**（与现有 P2#10 一致，升级为“可维护的数据源驱动卡片”）

**选择理由（为什么是它）：**

1. **与当前产品定位最一致**：openweb4 当前是“监控/资讯/分析”产品，不是自动化交易终端；能力对比属于信息展示，和现有仪表盘、新闻、指数页面一致。
2. **接入风险最低**：不需要用户 API Key，不涉及下单/资金权限，不引入高风险交易动作。
3. **实现成本可控**：可先用静态配置 + 人工维护上线 MVP，再逐步演进为半自动更新。
4. **与现有架构匹配**：遵循 `Service -> Controller -> /api/* -> SPA 页面` 的现有模式，复用缓存与定时刷新思路。

**暂不优先项（来自同一 URL 语境）：**

- ❌ 直接接入“交易执行型 Skills（自动下单/策略执行）”：偏离当前产品边界，且会引入 API 权限、安全审计、风控与合规负担。
- ⚠️ 直接接第三方 Agent SDK：当前仓库是 Spring Boot 主体，跨栈集成成本较高，先做只读能力层更稳。

**落地计划（可执行）**

1. **数据模型**：新增 `ExchangeCapability`（交易所名、现货/合约、Earn、API/Agent 支持、生态标签、更新时间、来源链接）。
2. **后端服务**：新增 `ExchangeCapabilityService`，先读取本地配置（YAML/JSON）并提供缓存。
3. **API 端点**：新增 `GET /api/exchange-capabilities`（支持 `lang`，可后续加 `source=binance-square`）。
4. **前端页面**：在 SPA 增加“交易所能力对比”页面（卡片或表格），支持中英字段展示。
5. **调度策略（可选）**：后续再加 `DataFetchScheduler` 定时拉取公开资料并校验字段完整性。

**验收标准（DoD）**

- 页面可稳定展示 6 家交易所对比数据（至少包含文中主维度）。
- `/api/exchange-capabilities` 返回结构化 JSON，空数据时有兜底内容。
- 不要求任何交易 API Key；不包含下单/资金类写操作接口。
- 在 `PLAN.md` 保持该项为“只读信息能力”，后续如做交易能力需单开安全评审。

### P3 — 长期探索


| #   | 功能              | 说明                   | 依赖   |
| --- | --------------- | -------------------- | ---- |
| 10  | **链上 PvP 预测市场** | 用户预测价格方向，链上结算（需智能合约） | Web3 |


---

## 前端重构待办（Frontend Refactor Backlog）

> 当时因 Claude Code 余额不足被阻塞，现改为直接 write tool 实现


| #   | 任务               | 说明                                                                         | 状态               |
| --- | ---------------- | -------------------------------------------------------------------------- | ---------------- |
| 11  | **侧边栏可伸缩**       | 已支持折叠/展开与 `localStorage` 记忆；移动端“默认折叠”未强制实现，待补                              | ✅ 已部分完成（移动端默认待补） |
| 12  | **消除所有页面刷新**     | 已使用 hash 路由 + 运行时渲染（语言切换与页面切换不触发整页刷新），并带 fade 动画                           | ✅ 已完成            |
| 13  | **修复顶层 Hook 违规** | React 备用前端：当前自定义 hook 内使用 `useState/useEffect`，避免在模块顶层直接调用；如仍有控制台警告再定位对应组件 | ✅ 已修复/不再适用       |


---

## 代码质量待办（Tech Debt Backlog）

> 来源：2026-03-20 代码审查


| #   | 问题                                      | 建议方案                                                    | 优先级 |
| --- | --------------------------------------- | ------------------------------------------------------- | --- |
| 14  | **DataFetchScheduler 无重试**              | 加 @Retryable 或指数退避                                      | 中   |
| 15  | **application.yml thymeleaf 配置复核**      | 当前仍渲染 `spa.html` 等模板；仅在确有冗余时再精简，并重点检查 `THYMELEAF_CACHE` | 低   |
| 16  | **WsSlot / FixedWindowLimiter.Slot 重复** | 抽成公共 RateLimitSlot 类                                    | 低   |
| 17  | **ChatWebSocketHandler.sessions 无过期清理** | 断连 session 不自动移除，需加清理逻辑                                 | 低   |


