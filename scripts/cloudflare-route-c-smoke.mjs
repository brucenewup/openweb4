import { existsSync, readFileSync } from 'node:fs'
import { join } from 'node:path'
import { cwd, exit } from 'node:process'

const root = cwd()
const requiredFiles = [
  'wrangler.toml',
  'worker/index.ts',
  'frontend/dist/index.html',
  'frontend/dist/_redirects',
  'functions/api/overview.ts',
  'functions/api/news.ts',
  'functions/api/kol.ts',
  'functions/api/tweets/latest.ts',
  'functions/api/chat.ts',
]

const failures = []

for (const file of requiredFiles) {
  if (!existsSync(join(root, file))) {
    failures.push(`Missing ${file}`)
  }
}

if (existsSync(join(root, 'wrangler.toml'))) {
  const wrangler = readFileSync(join(root, 'wrangler.toml'), 'utf8')
  if (!wrangler.includes('main = "worker/index.ts"')) {
    failures.push('wrangler.toml must set main to worker/index.ts for wrangler versions upload')
  }
  if (!wrangler.includes('directory = "frontend/dist"')) {
    failures.push('wrangler.toml must point assets.directory to frontend/dist')
  }
  if (!wrangler.includes('binding = "ASSETS"')) {
    failures.push('wrangler.toml must bind static assets as ASSETS')
  }
  if (!wrangler.includes('[build]') || !wrangler.includes('npm --prefix frontend run build')) {
    failures.push('wrangler.toml must build the frontend before upload')
  }
}

const appPath = join(root, 'frontend/src/App.tsx')
if (existsSync(appPath)) {
  const app = readFileSync(appPath, 'utf8')
  if (app.includes('/ws/chat')) {
    failures.push('App.tsx must not require /ws/chat for Cloudflare Pages')
  }
  if (!app.includes("fetch('/api/chat'")) {
    failures.push('App.tsx must call /api/chat for Cloudflare-compatible chat')
  }
}

if (failures.length > 0) {
  console.error(failures.join('\n'))
  exit(1)
}

console.log('Cloudflare route C smoke checks passed')
