# Security Policy

[中文](#中文) · [English](#english-1)

---

## 中文

### 支持的版本

我们只对**当前主分支 / 最新发布**的安全修复做优先响应。旧版本请尽快升级到当前依赖（如 Spring Boot）建议的版本。

### 报告漏洞

- **请勿**在公开 Issue 中粘贴有效 API 密钥、会话令牌或可利用的 PoC 细节（可先脱敏描述）。
- 推荐通过 [GitHub Security Advisories（Private vulnerability reporting）](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability) 私下提交；若仓库未开启该功能，可开 Issue 标题注明 `[security]` 并仅描述影响面，由维护者联系你补充细节。
- 说明影响范围（如：未认证用户、需特定配置）、受影响版本或提交范围（可选）。

### 安全部署提示（非漏洞，但强烈建议）

- 生产环境必须设置 `AI_API_KEY`、`APP_ALLOWED_ORIGINS`；勿将密钥写入仓库。
- 仅在可信反向代理后部署时考虑开启 `APP_RATE_LIMIT_TRUST_XFF`。
- AI 与链上功能用于信息与决策辅助时，请保持最小权限（例如不向自动化流程授予提币权限）。

---

## English

### Supported versions

Security fixes are prioritized for the **current main branch / latest release**. Please upgrade dependencies (e.g. Spring Boot) to supported versions.

### Reporting a vulnerability

- **Do not** post live API keys, session tokens, or full exploit details in public issues (describe impact with redaction).
- Prefer [GitHub private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability). If unavailable, open an issue titled `[security]` with high-level impact only.
- Include scope (unauthenticated vs authenticated, config required) and version/commit if known.

### Hardening (recommended)

- Set `AI_API_KEY` and `APP_ALLOWED_ORIGINS` in production; never commit secrets.
- Enable `APP_RATE_LIMIT_TRUST_XFF` only behind a trusted reverse proxy.
- For AI and on-chain workflows, use least privilege (e.g. no withdrawal permissions for automated agents).
