# OpenWeb4 — Web3 AI 链上监控

Spring Boot 应用：链上数据监控（BTC/ETH/USDT）、行情新闻、大V推文聚合、AI 智能问答（通过本服务调用 OpenAI 兼容流式 API）。支持中英双语界面。

## 功能

- **仪表盘**：实时价格、24h 涨跌、大户资金流向图、近期大额交易列表
- **行情新闻**：RSS 抓取（CoinDesk、CoinTelegraph），定时更新，中文自动翻译
- **大V推文**：KOL Twitter 动态聚合（RSSHub / Nitter RSS），可配置账号列表
- **AI 问答**：前端通过 WebSocket 与本服务通信，本服务再调用 OpenAI 兼容流式 API（例如 DeepSeek），仅支持问答，并尽量防止命令注入

## 技术栈

- Java 11+, Spring Boot 2.7.x（生产建议 Java 17 + Spring Boot 3.x）
- Thymeleaf, TailwindCSS, Chart.js
- 国际化：`?lang=zh` / `?lang=en`
- AI 后端：OpenAI 兼容流式 API，配置 `app.ai.*`（环境变量 `AI_*`）

## 运行

```bash
mvn spring-boot:run
```

运行前请至少配置 AI 相关环境变量（否则 AI 功能会报错提示未配置）。

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
| `AI_BASE_URL` | AI API 根地址（如 `https://api.deepseek.com/v1`，用于拼接 `/chat/completions`） | https://gmn.chuangzuoli.com/v1 |
| `AI_MODEL` | 模型名 | gpt-5.1 |
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
