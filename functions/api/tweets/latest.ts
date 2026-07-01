import { json, latestTweets } from '../../_shared/data'

export async function onRequestGet(context: any) {
  const url = new URL(context.request.url)
  const lang = url.searchParams.get('lang') || 'zh'
  return json({ tweets: latestTweets(lang) })
}

export async function onRequestOptions() {
  return json({})
}

