import { json, newsArticles } from '../_shared/data'

export async function onRequestGet(context: any) {
  const url = new URL(context.request.url)
  const lang = url.searchParams.get('lang') || 'zh'
  return json({ articles: newsArticles(lang) })
}

export async function onRequestOptions() {
  return json({})
}

