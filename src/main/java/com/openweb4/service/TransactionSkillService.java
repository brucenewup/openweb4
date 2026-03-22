package com.openweb4.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class TransactionSkillService {

    public Map<String, Object> getComparison(Locale locale) {
        boolean en = locale != null && "en".equalsIgnoreCase(locale.getLanguage());
        return Map.of(
                "updatedAt", LocalDateTime.now(),
                "dataSource", en
                        ? "Binance Square post + exchange official docs (manually curated)"
                        : "Binance Square 文章 + 各交易所官方资料（人工整理）",
                "intro", intro(en),
                "exchangeSkills", exchangeSkills(en),
                "thirdPartySkills", thirdPartySkills(en),
                "notes", notes(en)
        );
    }

    private Map<String, Object> intro(boolean en) {
        if (en) {
            return Map.of(
                    "whyTitle", "Why install exchange skills for your little lobster",
                    "whyPoints", List.of(
                            "Get faster market context instead of manually switching between apps.",
                            "Reduce signal latency via one-screen comparison across exchanges.",
                            "Build safer workflows with explicit permission boundaries."
                    )
            );
        }
        return Map.of(
                "whyTitle", "为什么要给你的小龙虾安装交易所 Skills",
                "whyPoints", List.of(
                        "一屏获取多交易所关键信息，减少来回切页。",
                        "更快识别市场变化与机会，降低信息延迟。",
                        "通过权限分层做更安全的 AI 工作流。"
                )
        );
    }

    private List<Map<String, Object>> exchangeSkills(boolean en) {
        return List.of(
                Map.of(
                        "exchange", "Binance",
                        "officialSkill", en ? "Binance Skill" : "Binance Skill",
                        "link", "https://www.binance.com/zh-CN/skills",
                        "installCommand", "/skill install binance-skill",
                        "installDifficulty", en ? "Easy" : "低",
                        "strengths", en ? "Broad ecosystem, mature APIs, rich product coverage" : "生态广、API 成熟、产品覆盖全面",
                        "security", en ? "API permission granularity, IP whitelist, key separation" : "API 权限粒度、IP 白名单、读写隔离",
                        "scope", en ? "Spot/Futures/Earn/On-chain ecosystem" : "现货/合约/Earn/链上生态",
                        "score", 9
                ),
                Map.of(
                        "exchange", "OKX",
                        "officialSkill", en ? "OKX Skill" : "OKX Skill",
                        "link", "https://www.okx.com/web3",
                        "installCommand", "/skill install okx-skill",
                        "installDifficulty", en ? "Medium" : "中",
                        "strengths", en ? "Multi-chain tooling and strategy feature depth" : "多链能力强，策略功能深",
                        "security", en ? "Sub-account controls, risk checks" : "子账户权限控制、风控检查",
                        "scope", en ? "Spot/Futures/On-chain data" : "现货/合约/链上数据",
                        "score", 8
                ),
                Map.of(
                        "exchange", "Bitget",
                        "officialSkill", en ? "Bitget Skill" : "Bitget Skill",
                        "link", "https://www.bitget.com/api-doc/common/intro",
                        "installCommand", "/skill install bitget-skill",
                        "installDifficulty", en ? "Easy-Medium" : "低-中",
                        "strengths", en ? "Copy-trading ecosystem and retail-friendly UX" : "跟单生态和零售用户体验较强",
                        "security", en ? "Permission scopes, API management" : "权限范围管理、API 管理",
                        "scope", en ? "Spot/Futures/Copy-trading" : "现货/合约/跟单",
                        "score", 7
                ),
                Map.of(
                        "exchange", "Bybit",
                        "officialSkill", en ? "Bybit Skill" : "Bybit Skill",
                        "link", "https://bybit-exchange.github.io/docs/v5/intro",
                        "installCommand", "/skill install bybit-skill",
                        "installDifficulty", en ? "Medium" : "中",
                        "strengths", en ? "Derivatives workflows and active trader tooling" : "衍生品链路完整，活跃交易工具多",
                        "security", en ? "API key permissions and account isolation" : "API Key 权限隔离、账户隔离",
                        "scope", en ? "Spot/Futures/Options (region dependent)" : "现货/合约/期权（地区相关）",
                        "score", 8
                ),
                Map.of(
                        "exchange", "Gate.io",
                        "officialSkill", en ? "Gate Skill" : "Gate Skill",
                        "link", "https://www.gate.com/docs/developers/apiv4/",
                        "installCommand", "/skill install gate-skill",
                        "installDifficulty", en ? "Medium" : "中",
                        "strengths", en ? "Wide token coverage and long-tail market data" : "币种覆盖广，长尾市场丰富",
                        "security", en ? "API permission controls and account safeguards" : "API 权限控制与账户保护",
                        "scope", en ? "Spot/Futures/Earn" : "现货/合约/Earn",
                        "score", 7
                ),
                Map.of(
                        "exchange", "Coinbase",
                        "officialSkill", en ? "Coinbase Developer / Agentic Stack" : "Coinbase Developer / Agentic Stack",
                        "link", "https://docs.cdp.coinbase.com/",
                        "installCommand", "/skill install coinbase-cdp-skill",
                        "installDifficulty", en ? "Medium-High" : "中-高",
                        "strengths", en ? "Compliance readiness and agent wallet/payments orientation" : "合规能力强，Agent 钱包/支付方向清晰",
                        "security", en ? "Strong compliance controls and account-level protections" : "合规审计完善，账户保护机制强",
                        "scope", en ? "Spot/Wallet/Payments" : "现货/钱包/支付",
                        "score", 8
                )
        );
    }

    private List<Map<String, Object>> thirdPartySkills(boolean en) {
        return List.of(
                Map.of(
                        "skill", "XClaw",
                        "link", "https://github.com/mookim-eth/xclaw-skill",
                        "installCommand", "/skill install xclaw",
                        "category", en ? "KOL Intelligence" : "KOL 情报",
                        "coreCapability", en ? "KOL signal extraction, token-efficient summaries" : "KOL 喊单抓取、脱脂数据省 Token",
                        "integrationFit", en ? "High (fits existing KOL feed)" : "高（与现有 KOL 聚合高度匹配）",
                        "risk", en ? "Medium" : "中"
                ),
                Map.of(
                        "skill", "CoinMarketCap Skill",
                        "link", "https://clawhub.ai/u/bryan-cmc",
                        "installCommand", "/skill install coinmarketcap-skill",
                        "category", en ? "Market Data" : "市场数据",
                        "coreCapability", en ? "30k+ tokens, fear & greed, spot & derivatives metrics" : "3 万+ 币种历史数据、恐惧贪婪与现货/衍生品指标",
                        "integrationFit", en ? "High" : "高",
                        "risk", en ? "Low" : "低"
                ),
                Map.of(
                        "skill", "RootData Crypto",
                        "link", "https://clawhub.ai/rdquanyu/rootdata-crypto",
                        "installCommand", "/skill install rootdata-crypto",
                        "category", en ? "Project Fundamentals" : "项目基本面",
                        "coreCapability", en ? "Project profiles, funding, institutional holdings" : "项目信息、融资背景、机构持仓",
                        "integrationFit", en ? "Medium" : "中",
                        "risk", en ? "Low-Medium" : "低-中"
                ),
                Map.of(
                        "skill", "PolyClaw",
                        "link", "https://clawhub.ai/chainstacklabs/polyclaw",
                        "installCommand", "clawhub install polyclaw",
                        "category", en ? "Prediction Market" : "预测市场",
                        "coreCapability", en ? "Polymarket CLOB: browse, trade, positions, LLM hedge discovery" : "Polymarket CLOB：浏览、交易、持仓与 LLM 对冲发现",
                        "integrationFit", en ? "Medium (mature on OpenClaw)" : "高（OpenClaw 上较成熟的预测市场技能）",
                        "risk", en ? "High (trading + third-party skill)" : "高（交易与第三方技能）"
                ),
                Map.of(
                        "skill", "Almanak SDK",
                        "link", "https://github.com/almanak-co/sdk",
                        "installCommand", "/skill install almanak-sdk",
                        "category", en ? "Yield Strategy" : "收益策略",
                        "coreCapability", en ? "Cross-chain APY scan & strategy backtest (12 chains)" : "跨 12 链 APY 扫描与策略回测",
                        "integrationFit", en ? "Medium" : "中",
                        "risk", en ? "Medium-High" : "中-高"
                ),
                Map.of(
                        "skill", "OpenZeppelin Skills",
                        "link", "https://github.com/OpenZeppelin/openzeppelin-skills",
                        "installCommand", "/skill install openzeppelin-skills",
                        "category", en ? "Contract Security" : "合约安全",
                        "coreCapability", en ? "Scan unfamiliar contracts for risks; deploy with templates" : "交互前扫描陌生合约风险；亦可部署合约",
                        "integrationFit", en ? "High (pre-flight checks)" : "高（交互前体检）",
                        "risk", en ? "Low-Medium" : "低-中"
                )
        );
    }

    private List<String> notes(boolean en) {
        if (en) {
            return List.of(
                    "OpenWeb4 should keep this module read-only (monitoring/decision support) instead of auto-trading execution.",
                    "Do not grant withdrawal permissions to any AI workflow.",
                    "Start with exchange capability comparison + third-party skill awareness, then iterate to deeper data integrations."
            );
        }
        return List.of(
                "OpenWeb4 当前建议保持只读定位（监控/决策辅助），不做自动下单执行。",
                "任何 AI 流程都不应授予提币权限。",
                "建议先做交易所能力对比与第三方 Skill 认知，再迭代到更深层数据接入。"
        );
    }
}
