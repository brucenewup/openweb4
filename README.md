# OpenWeb4 — Web3 AI 链上监控

Spring Boot 应用：链上数据监控（BTC/ETH/USDT）、行情新闻、大V推文聚合、AI 智能问答（通过本服务调用 OpenAI 兼容流式 API）。支持中英双语界面与 SPA 单页路由体验。

## 功能

- **仪表盘**：实时价格、24h 涨跌、大户资金流向图、近期大额交易列表
- **行情新闻**：RSS 抓取（CoinDesk、CoinTelegraph），定时更新，中文自动翻译
- **大V推文**：KOL Twitter 动态聚合（RSSHub / Nitter RSS），可配置账号列表
- **行情预测**：基于市场数据和趋势分析，预测未来价格走势或市场变化。
- **行情指数**：通过综合多个市场数据，反映整体行情表现的指标，常用于评估市场健康状态。
- **AI 问答**：前端通过 WebSocket 与本服务通信，本服务再调用 OpenAI 兼容流式 API（例如 DeepSeek），仅支持问答，并尽量防止命令注入
- **单页应用（SPA）**：`#/dashboard`、`#/news`、`#/kol-tweets`、`#/market-forecast`、`#/market-indices`、`#/ai-chat` 路由无整页刷新切换
- **防滥用与安全**：按 IP 限流（刷新与 AI 对话），移除调试密钥接口，日志不输出 Authorization

## 技术栈

- Java 17+, Spring Boot 3.3.x
- Thymeleaf, TailwindCSS, Chart.js
- 国际化：`?lang=zh` / `?lang=en`
- AI 后端：OpenAI 兼容流式 API，配置 `app.ai.*`（环境变量 `AI_*`）

## 最近更新（2026-03）

- 升级运行时到 **Spring Boot 3.3.x + Java 17**，完成 `jakarta.*` 迁移并通过测试
- 修复公开仓库安全问题：删除硬编码 key 与临时调试接口，收紧默认错误信息暴露
- 修复前端聊天消息潜在 XSS 风险：用户消息渲染改为 `textContent`
- 补齐控制器测试与 API 断言（含调试接口下线检查）

## 运行

```bash
mvn spring-boot:run
```

运行前请至少配置 AI 相关环境变量（否则 AI 功能会报错提示未配置）。
请确认本地 Java 版本为 17 或更高（`java -version`）。

示例（macOS/Linux）：
```bash
export AI_API_KEY="YOUR_AI_API_KEY"
export APP_ALLOWED_ORIGINS="http://localhost:8080,http://127.0.0.1:8080"

# 可选：如果你使用的供应商不是默认的 gmn.chuangzuoli.com
# export AI_BASE_URL="https://api.deepseek.com/v1"
# export AI_MODEL="deepseek-chat"
# export AI_MAX_TOKENS="1024"

mvn spring-boot:run
```

默认端口 8080。访问：

- http://localhost:8080/dashboard
- http://localhost:8080/news
- http://localhost:8080/kol-tweets
- http://localhost:8080/ai-chat

## 配置（生产必须设置）

| 环境变量 | 说明 | 默认 |
|----------|------|------|
| `SERVER_PORT` | 服务端口 | 8080 |
| `AI_API_KEY` | AI API Key（**生产必须设置**，不要提交到仓库） | 空 |
| `AI_BASE_URL` | AI API 根地址（如 `https://api.deepseek.com/v1`，用于拼接 `/chat/completions`） |https://api.deepseek.com/v1 |
| `AI_MODEL` | 模型名 | deepseek  |
| `AI_MAX_TOKENS` | 单次回复最大 token | 1024 |
| `APP_ALLOWED_ORIGINS` | WebSocket 允许的 Origin | http://127.0.0.1:8080,http://localhost:8080 |
| `APP_MAX_CHAT_MESSAGE_LENGTH` | 单条消息最大长度 | 500 |
| `APP_TWEET_HANDLES` | KOL 推特 handle 列表（逗号分隔） | elonmusk,cz_binance,VitalikButerin,CryptoHayes,saylor |
| `THYMELEAF_CACHE` | 模板缓存 | true |

## 测试

```bash
mvn test
```

## 打包与生产

```bash
mvn -DskipTests package
java -jar target/openweb4-1.0.0.jar
```

生产环境建议在启动前设置环境变量：
```bash
export AI_API_KEY="YOUR_AI_API_KEY"
export APP_ALLOWED_ORIGINS="https://your-domain.com"
export SERVER_PORT="8080"

java -jar target/openweb4-1.0.0.jar
```

生产环境务必设置 `AI_API_KEY` 和 `APP_ALLOWED_ORIGINS`；不要将密钥写入代码或提交到 Git。若仓库曾包含过密钥，应在提供商处**轮换/吊销**该密钥。
