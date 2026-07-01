import { json, kolAuthors, latestTweets, newsArticles, overviewData } from '../functions/_shared/data'

type Env = {
  ASSETS: Fetcher
  ENVIRONMENT?: string
  OPENAI_API_KEY?: string
  OPENAI_MODEL?: string
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url)

    if (request.method === 'OPTIONS' && url.pathname.startsWith('/api/')) {
      return json({})
    }

    if (url.pathname === '/api/overview' && request.method === 'GET') {
      return json(overviewData())
    }

    if (url.pathname === '/api/news' && request.method === 'GET') {
      return json({ articles: newsArticles(url.searchParams.get('lang') || 'zh') })
    }

    if (url.pathname === '/api/kol' && request.method === 'GET') {
      return json({ authors: kolAuthors })
    }

    if (url.pathname === '/api/tweets/latest' && request.method === 'GET') {
      return json({ tweets: latestTweets(url.searchParams.get('lang') || 'zh') })
    }

    if (url.pathname === '/api/chat' && request.method === 'POST') {
      return handleChat(request, env)
    }

    if (url.pathname.startsWith('/api/')) {
      return json({ error: 'Not found' }, 404)
    }

    return env.ASSETS.fetch(request)
  },
}

async function handleChat(request: Request, env: Env) {
  const body = await safeJson(request)
  const message = String(body.message || '').trim()

  if (!message) {
    return json({ message: '请输入一段消息。' }, 400)
  }

  if (!env.OPENAI_API_KEY) {
    return json({
      message: `Cloudflare Worker 已接收你的消息：“${message}”。当前未配置 OPENAI_API_KEY，所以先返回本地降级回复；部署后在 Cloudflare Workers 环境变量中设置 OPENAI_API_KEY 即可启用真实 AI 对话。`,
    })
  }

  const upstream = await fetch('https://api.openai.com/v1/chat/completions', {
    method: 'POST',
    headers: {
      Authorization: `Bearer ${env.OPENAI_API_KEY}`,
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      model: env.OPENAI_MODEL || 'gpt-4o-mini',
      messages: [
        { role: 'system', content: 'You are a concise assistant for the OpenWeb4 crypto dashboard.' },
        { role: 'user', content: message },
      ],
      temperature: 0.4,
    }),
  })

  if (!upstream.ok) {
    return json({ message: 'AI 服务暂时不可用，请稍后再试。' }, 502)
  }

  const result = await upstream.json() as any
  return json({ message: result.choices?.[0]?.message?.content || 'AI 没有返回内容。' })
}

async function safeJson(request: Request) {
  try {
    return await request.json() as Record<string, unknown>
  } catch {
    return {}
  }
}
