import { json, kolAuthors } from '../_shared/data'

export async function onRequestGet() {
  return json({ authors: kolAuthors })
}

export async function onRequestOptions() {
  return json({})
}

