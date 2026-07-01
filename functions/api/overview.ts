import { json, overviewData } from '../_shared/data'

export async function onRequestGet() {
  return json(overviewData())
}

export async function onRequestOptions() {
  return json({})
}

