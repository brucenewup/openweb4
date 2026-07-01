import { json } from '../_shared/data'

type ChatEnv = {
  OPENAI_API_KEY?: string
  OPENAI_MODEL?: string
}

export async function onRequestPost(context: any) {
  const env = (context.env || {}) as ChatEnv
  const body = await safeJson(context.request)
  const message = String(body.message || '').trim()

  if (!message) {
    return json({ message: '请输入一段消息。' }, 400)
  }

  if (!env.OPENAI_API_KEY) {
    return json({
      message: `Cloudflare 版本已接收你的消息：“${message}”。当前未配置 OPENAI_API_KEY，所以先返回本地降级回复；部署后在 Cloudflare Pages 环境变量中设置 OPENAI_API_KEY 即可启用真实 AI 对话。`,
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

export async function onRequestOptions() {
  return json({})
}

async function safeJson(request: Request) {
  try {
    return await request.json()
  } catch {
    return {}
  }
}

