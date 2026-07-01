export const kolAuthors = [
  { name: 'Elon Musk', handle: 'elonmusk', profileUrl: 'https://x.com/elonmusk' },
  { name: 'CZ Binance', handle: 'cz_binance', profileUrl: 'https://x.com/cz_binance' },
  { name: 'Vitalik Buterin', handle: 'VitalikButerin', profileUrl: 'https://x.com/VitalikButerin' },
  { name: 'Michael Saylor', handle: 'saylor', profileUrl: 'https://x.com/saylor' },
  { name: 'The Block', handle: 'TheBlock__', profileUrl: 'https://x.com/TheBlock__' },
]

export function overviewData() {
  return {
    btc: { price: 64280.15, change24h: 2.14 },
    eth: { price: 3188.42, change24h: 1.02 },
    usdt: { price: 1.0002, change24h: 0 },
    transactions: [
      tx('0xa3f0c91b8e...', 'BTC', '128.42', '8,254,300', 'Binance', 'Unknown Wallet', '5m ago'),
      tx('0x82bc4e21d9...', 'ETH', '2,840.00', '9,055,113', 'Kraken', 'Cold Wallet', '18m ago'),
      tx('0x10fe76ab31...', 'USDT', '12,450,000', '12,452,490', 'Treasury', 'OKX', '27m ago'),
    ],
    generatedAt: new Date().toISOString(),
    source: 'cloudflare-pages-functions',
  }
}

export function newsArticles(lang: string) {
  const zh = lang !== 'en'
  return [
    {
      id: 'cf-news-1',
      title: zh ? 'Cloudflare 版本已启用 Pages Functions API' : 'Cloudflare build now serves Pages Functions APIs',
      summary: zh ? '前端和 API 已迁移到可由 Cloudflare Pages 部署的 TypeScript Functions，静态页面不再只是空壳。' : 'The frontend and API are now served through TypeScript Pages Functions for a deployable Cloudflare version.',
      source: 'OpenWeb4',
      url: 'https://developers.cloudflare.com/pages/functions/',
      publishedAt: 'Today',
    },
    {
      id: 'cf-news-2',
      title: zh ? '链上监控面板保留核心行情与大额交易视图' : 'Dashboard keeps core market and whale transaction views',
      summary: zh ? 'Cloudflare 环境下先使用边缘函数提供稳定降级数据，后续可接入 KV、D1 或外部行情源。' : 'The Cloudflare version uses edge functions with stable fallback data and can later connect KV, D1, or live market APIs.',
      source: 'OpenWeb4',
      url: 'https://developers.cloudflare.com/workers/runtime-apis/kv/',
      publishedAt: 'Today',
    },
  ]
}

export function latestTweets(lang: string) {
  const zh = lang !== 'en'
  return kolAuthors.map((author, index) => ({
    id: `cf-tweet-${author.handle}`,
    text: zh
      ? `${author.name} 监控卡片已由 Cloudflare Pages Functions 返回。生产环境可继续接入 X/RSS/AI 摘要源。`
      : `${author.name} monitoring card is served by Cloudflare Pages Functions. Production can plug in X/RSS/AI summarization next.`,
    summary: zh ? 'Cloudflare 降级数据' : 'Cloudflare fallback data',
    url: author.profileUrl,
    publishedAt: `${index + 1}h ago`,
    fetchedAt: new Date().toISOString(),
    source: 'Cloudflare',
    author,
  }))
}

export function json(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store',
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    },
  })
}

function tx(hash: string, symbol: string, amount: string, usdValue: string, fromAddr: string, toAddr: string, time: string) {
  return { hash, symbol, amount, usdValue, fromAddr, toAddr, time }
}
