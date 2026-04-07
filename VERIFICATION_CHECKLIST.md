# OpenWeb4 P0 功能验证清单

## 验证环境准备

### 1. 环境变量配置
```bash
export AI_API_KEY=your_openai_compatible_api_key
export WHALE_ALERT_API_KEY=your_whale_alert_api_key  # 可选
export SERVER_PORT=8080
```

### 2. 启动应用
```bash
mvn spring-boot:run
```

### 3. 检查启动日志
应看到以下日志：
```
✅ AI configuration validated: model=gpt-5.1, baseUrl=https://gmn.chuangzuoli.com/v1
⚠️  WHALE_ALERT_API_KEY is not configured! Whale tracking will use cached data only.
```

## 功能验证清单

### ✅ 1. Whale Alert API 集成
**测试步骤**:
1. 访问 http://localhost:8080/#/dashboard
2. 查看"近期大额交易"表格
3. 点击"刷新"按钮

**预期结果**:
- 如果配置了 WHALE_ALERT_API_KEY：显示最近 1 小时的大额交易
- 如果未配置：显示缓存数据或空列表
- 无 NullPointerException 或资源泄漏

**验证点**:
- [ ] 页面正常加载
- [ ] 刷新按钮可用
- [ ] 无控制台错误
- [ ] 后端日志无 ERROR

### ✅ 2. AI 市场简报生成
**测试步骤**:
1. 访问 http://localhost:8080/#/market-briefing
2. 点击"刷新"按钮
3. 等待 AI 生成简报（约 5-10 秒）

**预期结果**:
- 显示包含市场行情、新闻、KOL 观点的简报
- 简报内容为中文
- 生成时间显示正确

**验证点**:
- [ ] 简报正常生成
- [ ] 内容格式正确
- [ ] 缓存机制生效（24小时内重复访问返回缓存）
- [ ] 无 JSON 解析错误

### ✅ 3. 定时任务重试机制
**测试步骤**:
1. 临时断开网络连接
2. 等待 1 分钟（触发定时任务）
3. 查看后端日志

**预期结果**:
```
WARN  - Failed to fetch crypto prices (attempt 1/3): ...
WARN  - Failed to fetch crypto prices (attempt 2/3): ...
WARN  - Failed to fetch crypto prices (attempt 3/3): ...
ERROR - All 3 retries exhausted for crypto prices
```

**验证点**:
- [ ] 重试 3 次
- [ ] 指数退避生效（1s, 2s, 3s）
- [ ] 最终失败记录 ERROR 日志
- [ ] 应用不崩溃

### ✅ 4. 缓存过期机制
**测试步骤**:
1. 访问 http://localhost:8080/api/overview
2. 记录 BTC 价格
3. 等待 6 分钟
4. 再次访问同一接口

**预期结果**:
- 第一次请求：调用 CoinGecko API
- 5 分钟内：返回缓存数据
- 6 分钟后：重新调用 API 获取最新数据

**验证点**:
- [ ] 缓存命中率正常
- [ ] 5 分钟后缓存过期
- [ ] 无内存泄漏（长时间运行后内存稳定）

### ✅ 5. 配置校验器
**测试步骤**:
1. 不设置 AI_API_KEY 环境变量
2. 启动应用
3. 查看启动日志

**预期结果**:
```
ERROR - ⚠️  AI_API_KEY is not configured! AI chat feature will not work properly.
ERROR -     Please set environment variable: export AI_API_KEY=your_api_key
```

**验证点**:
- [ ] 启动时显示配置错误
- [ ] 提供清晰的配置指引
- [ ] 应用仍可启动（降级运行）

### ✅ 6. 前端错误处理
**测试步骤**:
1. 访问 http://localhost:8080/#/dashboard
2. 停止后端服务
3. 点击"刷新"按钮

**预期结果**:
- 显示友好的错误提示（中英文）
- 不显示原始 HTTP 状态码
- 页面不崩溃

**验证点**:
- [ ] 错误提示友好
- [ ] 支持中英文
- [ ] 页面可恢复

## 性能验证

### 内存泄漏检查
```bash
# 运行 1 小时后检查内存
jmap -heap <pid>
```

**预期结果**:
- 堆内存使用稳定
- 无持续增长趋势
- Caffeine Cache 条目数 ≤ 20

### 并发压测
```bash
# 使用 ab 工具压测
ab -n 1000 -c 10 http://localhost:8080/api/overview
```

**预期结果**:
- 无 500 错误
- 响应时间 < 500ms (P95)
- 无资源泄漏

## 回归测试

### 原有功能验证
- [ ] 实时行情页面正常
- [ ] 链上监控页面正常
- [ ] 合约监控页面正常
- [ ] 行情新闻页面正常
- [ ] 大V推文页面正常
- [ ] AI 对话功能正常

## 验证结论

### 通过标准
- 所有 ✅ 项全部通过
- 无 P0/P1 级别 bug
- 性能指标达标
- 原有功能无回归

### 验证签名
- **验证人**: _____________
- **验证时间**: _____________
- **验证环境**: _____________
- **验证结果**: ☐ 通过  ☐ 不通过

---

**备注**: 
- 本清单基于代码审查发现的问题制定
- 所有修复已在 commit 01e7f17 中完成
- 建议在生产环境部署前完成全部验证
