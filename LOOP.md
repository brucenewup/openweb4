# Loop Configuration — OpenWeb4 Daily Triage

## Active Loops

| Pattern | Cadence | Status | Command |
|---------|---------|--------|---------|
| Daily Triage | 1d | L1 report-only | 每天 09:00 检查 Maven 构建、测试失败、依赖更新 |

## Human Gates

- No auto-fix until L2 checklist complete
- All high-risk paths: human review required
- 依赖更新需人工审核（特别是 Spring Boot、安全相关）
- AI 问答功能变更需测试后再上线

## Budget

- Max sub-agent spawns per run: 0 (L1) / 2 (L2)
- Max tokens/day: 100k (see `loop-budget.md`)
- Append each run to `loop-run-log.md`; use `loop-budget` skill at start/end
- Kill switch: `loop-pause-all` — pause schedulers and notify human
- Estimate: `npx @cobusgreyling/loop-cost --pattern daily-triage`

## OpenWeb4-Specific Checks

Loop 每天会检查：
- **Maven 构建状态**：`mvn clean test` 是否通过
- **依赖安全**：`mvn dependency:tree` 检查过期/安全漏洞
- **API 健康**：Web3 API（BTC/ETH）、AI API（OpenAI 兼容）是否正常
- **RSS 更新**：CoinDesk、CoinTelegraph 新闻是否正常抓取
- **日志异常**：application.log 中的 ERROR/WARN
- **性能指标**：启动时间、响应时间是否正常

## Links

- Pattern: [daily-triage](../../patterns/daily-triage.md)
- Checklist: [loop-design-checklist](../../docs/loop-design-checklist.md)