import { useState, useEffect, useRef } from 'react'
import './App.css'

// ─── Types ─────────────────────────────────────────────────────
interface PriceData { price: number | null; change24h: number | null }
interface Tx { hash: string; symbol: string; amount: string; usdValue: string; fromAddr: string; toAddr: string; time: string }
interface Article { id: string; title: string; summary: string; source: string; url: string; publishedAt: string }
interface TweetAuthor { name: string; handle: string; profileUrl: string }
interface TweetItem { id: string; text: string; summary?: string; url: string; publishedAt: string; source?: string; fetchedAt?: string; author: TweetAuthor }

// ─── Theme (inside App, not module top-level) ──────────────────
type Theme = 'light' | 'dark' | 'system'
function useTheme() {
  const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
  const [theme, setTheme] = useState<Theme>(() => {
    const saved = localStorage.getItem('theme') as Theme
    return saved || 'system'
  })
  const resolved = theme === 'system' ? (prefersDark ? 'dark' : 'light') : theme

  useEffect(() => {
    document.documentElement.dataset.theme = resolved
  }, [resolved])

  const changeTheme = (t: Theme) => {
    setTheme(t)
    localStorage.setItem('theme', t)
    const r = t === 'system' ? (prefersDark ? 'dark' : 'light') : t
    document.documentElement.dataset.theme = r
  }

  return { theme, resolved, changeTheme }
}

// ─── Sidebar collapse ───────────────────────────────────────────
function useSidebar() {
  const [collapsed, setCollapsed] = useState(() => localStorage.getItem('sidebar-collapsed') === 'true')

  const toggle = () => {
    setCollapsed(prev => {
      const next = !prev
      localStorage.setItem('sidebar-collapsed', String(next))
      return next
    })
  }

  return { collapsed, toggle }
}

// ─── Lang ─────────────────────────────────────────────────────
function useLang() {
  const [lang, setLang] = useState(() => document.documentElement.lang || 'zh')

  const changeLang = (l: string) => {
    setLang(l)
    document.documentElement.lang = l
    localStorage.setItem('lang', l)
  }

  return { lang, changeLang }
}

// ─── Page: Dashboard ─────────────────────────────────────────
function Dashboard() {
  const [btc, setBtc] = useState<PriceData>({ price: null, change24h: null })
  const [eth, setEth] = useState<PriceData>({ price: null, change24h: null })
  const [usdt, setUsdt] = useState<PriceData>({ price: null, change24h: null })
  const [txs, setTxs] = useState<Tx[]>([])
  const [loading, setLoading] = useState(true)

  const fetchData = () => {
    setLoading(true)
    fetch('/api/overview')
      .then(r => r.json())
      .then(d => {
        setBtc({ price: d.btc?.price, change24h: d.btc?.change24h })
        setEth({ price: d.eth?.price, change24h: d.eth?.change24h })
        setUsdt({ price: d.usdt?.price, change24h: d.usdt?.change24h })
        setTxs((d.transactions || []).slice(0, 20))
        setLoading(false)
      })
      .catch(() => setLoading(false))
  }

  useEffect(() => { fetchData() }, [])

  return (
    <main className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">仪表盘</h1>
          <p className="page-sub">BTC · ETH · USDT 实时行情</p>
        </div>
        <button className="btn-outline" onClick={fetchData}>↻ 刷新</button>
      </div>

      <div className="price-grid">
        {[
          { label: '比特币', symbol: '₿', data: btc, fmt: (v: number) => '$' + v.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 }), changeFmt: (v: number) => (v >= 0 ? '+' : '') + v.toFixed(2) + '%' },
          { label: '以太坊', symbol: 'Ξ', data: eth, fmt: (v: number) => '$' + v.toLocaleString('en', { minimumFractionDigits: 2, maximumFractionDigits: 2 }), changeFmt: (v: number) => (v >= 0 ? '+' : '') + v.toFixed(2) + '%' },
          { label: '泰达币', symbol: '₮', data: usdt, fmt: (v: number) => '$' + v.toLocaleString('en', { minimumFractionDigits: 4, maximumFractionDigits: 4 }), changeFmt: () => '—' },
        ].map(({ label, symbol, data, fmt, changeFmt }) => (
          <div className="card" key={label}>
            <div className="price-header">
              <span className="price-sym">{symbol}</span>
              <span className="price-label">{label}</span>
            </div>
            {loading ? (
              <div className="skeleton h-8 w-32" />
            ) : data.price ? (
              <div className="price-value">{fmt(data.price)}</div>
            ) : (
              <div className="price-value muted">--</div>
            )}
            {data.change24h != null && (
              <div className={`price-change ${data.change24h >= 0 ? 'up' : 'down'}`}>
                {data.change24h >= 0 ? '▲' : '▼'} {changeFmt(data.change24h)} <span className="change-label">24h</span>
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="card mt-6">
        <h2 className="section-title">近期大额交易</h2>
        {loading ? (
          <div className="space-y-3 mt-4">
            {[1,2,3].map(i => <div key={i} className="skeleton h-12 w-full" />)}
          </div>
        ) : txs.length === 0 ? (
          <p className="muted text-center py-8">暂无交易记录</p>
        ) : (
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>时间</th><th>资产</th><th className="right">数量</th><th className="right">美元价值</th><th>发送方</th><th>接收方</th>
                </tr>
              </thead>
              <tbody>
                {txs.map((tx, i) => (
                  <tr key={i}>
                    <td className="muted">{tx.time}</td>
                    <td><span className="badge">{tx.symbol}</span></td>
                    <td className="right mono">{tx.amount}</td>
                    <td className="right accent-text">${tx.usdValue}</td>
                    <td className="addr">{tx.fromAddr}</td>
                    <td className="addr">{tx.toAddr}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </main>
  )
}

// ─── Page: News ───────────────────────────────────────────────
function News({ lang }: { lang: string }) {
  const [articles, setArticles] = useState<Article[]>([])
  const [loading, setLoading] = useState(true)

  const fetchNews = (force = false) => {
    setLoading(true)
    const suffix = force ? `&refresh=1&_=${Date.now()}` : ''
    fetch(`/api/news?lang=${lang}${suffix}`)
      .then(r => r.json())
      .then(d => setArticles(d.articles || []))
      .catch(() => setArticles([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchNews() }, [lang])

  return (
    <main className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">行情新闻</h1>
          <p className="page-sub">每日自动更新</p>
        </div>
        <button className="btn-primary" onClick={() => fetchNews(true)}>↻ 刷新</button>
      </div>
      {loading ? (
        <div className="space-y-4 mt-4">
          {[1,2,3].map(i => <div key={i} className="skeleton h-32 w-full" />)}
        </div>
      ) : articles.length === 0 ? (
        <div className="card text-center py-16">
          <p className="muted">暂无新闻，点击刷新获取</p>
        </div>
      ) : (
        <div className="news-list mt-4">
          {articles.map(a => (
            <a key={a.id} href={a.url} target="_blank" rel="noopener noreferrer" className="news-card card">
              <div className="news-meta">
                <span className="badge">{a.source}</span>
                <span className="muted text-xs">{a.publishedAt}</span>
              </div>
              <h3 className="news-title">{a.title}</h3>
              <p className="news-summary">{a.summary}</p>
            </a>
          ))}
        </div>
      )}
    </main>
  )
}

// ─── Page: KOL Tweets ───────────────────────────────────────
function KolTweets({ lang, kolAuthors, subscribed, onToggle }: {
  lang: string
  kolAuthors: TweetAuthor[]
  subscribed: string[]
  onToggle: (handle: string) => void
}) {
  const [tweets, setTweets] = useState<TweetItem[]>([])
  const [loading, setLoading] = useState(true)
  const [showKolPanel, setShowKolPanel] = useState(false)

  const fetchTweets = (force = false) => {
    setLoading(true)
    const suffix = force ? `&refresh=1&_=${Date.now()}` : ''
    fetch(`/api/tweets/latest?lang=${lang}${suffix}`)
      .then(r => r.json())
      .then(d => setTweets(d.tweets || []))
      .catch(() => setTweets([]))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchTweets() }, [lang])

  return (
    <main className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">大V推文</h1>
          <p className="page-sub">大V最新一条推文 · 点击作者打开主页</p>
        </div>
        <div className="page-header-actions">
          <button className="btn-outline" onClick={() => fetchTweets(true)}>↻ 刷新</button>
          <button className="btn-kol-toggle" onClick={() => setShowKolPanel(p => !p)}>
            {showKolPanel ? '收起列表' : '订阅管理'}
          </button>
        </div>
      </div>

      {/* KOL 订阅管理面板 */}
      {showKolPanel && (
        <div className="kol-panel card">
          <h3 className="kol-panel-title">订阅列表（{subscribed.length}/{kolAuthors.length}）</h3>
          <div className="kol-grid">
            {kolAuthors.map(author => {
              const isSubscribed = subscribed.includes(author.handle)
              return (
                <div
                  key={author.handle}
                  className={`kol-item ${isSubscribed ? 'subscribed' : ''}`}
                  onClick={() => onToggle(author.handle)}
                >
                  <span className="kol-name">{author.name}</span>
                  <span className="kol-handle">@{author.handle}</span>
                  <span className={`kol-dot ${isSubscribed ? 'on' : 'off'}`} />
                </div>
              )
            })}
          </div>
        </div>
      )}

      {loading ? (
        <div className="space-y-4 mt-4">
          {[1,2,3].map(i => <div key={i} className="skeleton h-32 w-full" />)}
        </div>
      ) : tweets.length === 0 ? (
        <div className="card text-center py-16">
          <p className="muted">暂无大V推文，点击刷新获取</p>
        </div>
      ) : (
        <div className="news-list mt-4">
          {tweets.map(t => (
            <a key={t.id} href={t.url} target="_blank" rel="noopener noreferrer" className="news-card card tweet-card">
              <div className="news-meta tweet-meta">
                <div className="tweet-author-wrap">
                  {t.author.profileUrl ? (
                    <span
                      className="tweet-author"
                      onClick={(e) => { e.preventDefault(); e.stopPropagation(); window.open(t.author.profileUrl, '_blank', 'noopener,noreferrer') }}
                    >
                      {t.author.name}
                      <span className="tweet-handle">@{t.author.handle}</span>
                    </span>
                  ) : (
                    <span className="tweet-author">
                      {t.author.name}
                      {t.author.handle ? <span className="tweet-handle">@{t.author.handle}</span> : null}
                    </span>
                  )}
                </div>
                <div className="tweet-time-wrap">
                  <span className="muted text-xs">发布 {t.publishedAt}</span>
                  {t.fetchedAt ? <span className="muted text-xs">抓取 {t.fetchedAt}</span> : null}
                </div>
              </div>
              <div className="tweet-source-row"><span className="badge">{t.source || 'X'}</span></div>
              {t.source === 'Profile' && (t.summary || '').trim() ? (
                <p className="muted text-sm" style={{ margin: '0 0 12px', lineHeight: 1.55, borderLeft: '2px solid rgba(139,92,246,0.45)', paddingLeft: 10 }}>{t.summary}</p>
              ) : null}
              <div style={{ marginTop: 4, paddingTop: 14, borderTop: '1px solid rgba(0,240,255,0.12)' }}>
                <div className="muted text-xs" style={{ letterSpacing: '0.08em', marginBottom: 8, fontFamily: 'JetBrains Mono, monospace' }}>
                  {lang === 'en' ? 'Latest post' : '最近一条'}
                </div>
                <p className="news-summary" style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                  {(t.text || '').trim() || t.summary || ''}
                </p>
                {t.source === 'AI Generated' && (t.summary || '').trim() && (t.summary || '').trim() !== (t.text || '').trim() ? (
                  <>
                    <div className="muted text-xs" style={{ marginTop: 10, letterSpacing: '0.06em' }}>
                      {lang === 'en' ? 'Summary (ZH)' : '中文摘要'}
                    </div>
                    <p className="muted text-sm" style={{ margin: '4px 0 0', lineHeight: 1.55 }}>{t.summary}</p>
                  </>
                ) : null}
              </div>
            </a>
          ))}
        </div>
      )}
    </main>
  )
}

// ─── Page: AI Chat ────────────────────────────────────────────
function AiChat() {
  const [msgs, setMsgs] = useState<{role:'ai'|'user'; text: string; error?: boolean}[]>([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [connected, setConnected] = useState(false)
  const wsRef = useRef<WebSocket | null>(null)
  const bottomRef = useRef<HTMLDivElement>(null)
  const textareaRef = useRef<HTMLTextAreaElement>(null)

  const retryCountRef = useRef(0)
  const MAX_RETRIES = 5

  const connect = () => {
    const proto = location.protocol === 'https:' ? 'wss:' : 'ws:'
    const ws = new WebSocket(proto + '//' + location.host + '/ws/chat')
    wsRef.current = ws

    ws.onopen = () => { setConnected(true); retryCountRef.current = 0 }
    ws.onclose = ws.onerror = () => {
      setConnected(false)
      if (retryCountRef.current < MAX_RETRIES) {
        const delay = Math.min(1000 * Math.pow(2, retryCountRef.current), 16000)
        retryCountRef.current++
        setTimeout(connect, delay)
      } else {
        setMsgs(prev => [...prev, { role: 'ai', text: '连接已断开，请刷新页面重试。', error: true }])
      }
    }
    ws.onmessage = ev => {
      try {
        const d = JSON.parse(ev.data)
        if (d.type === 'chunk') {
          setMsgs(prev => {
            const last = prev[prev.length - 1]
            if (last?.role === 'ai' && !last.error) {
              const updated = [...prev]; updated[updated.length - 1] = { ...last, text: last.text + d.chunk }; return updated
            }
            return [...prev, { role: 'ai', text: d.chunk }]
          })
          bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
        }
        if (d.type === 'done') { setSending(false) }
        if (d.type === 'error') {
          setMsgs(prev => [...prev.filter(m => !(m.role === 'ai' && !m.text)), { role: 'ai', text: d.message || '出错了', error: true }])
          setSending(false)
        }
      } catch (_) { setSending(false) }
    }
  }

  useEffect(() => { connect(); return () => wsRef.current?.close() }, [])

  const send = () => {
    const text = input.trim()
    if (!text || sending || !connected) return
    setMsgs(prev => [...prev, { role: 'user', text }])
    setInput('')
    setSending(true)
    wsRef.current?.send(JSON.stringify({ type: 'chat', message: text }))
    setTimeout(() => textareaRef.current?.focus(), 0)
  }

  const onKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); send() }
  }

  const autoResize = () => {
    const ta = textareaRef.current
    if (ta) { ta.style.height = 'auto'; ta.style.height = Math.min(ta.scrollHeight, 120) + 'px' }
  }

  return (
    <main className="page chat-page">
      <div className="page-header">
        <div>
          <h1 className="page-title">新对话</h1>
          <p className="page-sub">支持多轮对话</p>
        </div>
        <div className={`status-badge ${connected ? 'connected' : 'disconnected'}`}>
          <span className="dot" />
          {connected ? '已连接' : '未连接'}
        </div>
      </div>

      <div className="chat-msgs">
        {msgs.length === 0 && (
          <div className="msg-ai">你好！我是 AI 助手，可以回答各种问题。</div>
        )}
        {msgs.map((m, i) => (
          <div key={i} className={`msg-wrap ${m.role}`}>
            <div className={`bubble ${m.error ? 'error' : ''}`}>{m.text}</div>
          </div>
        ))}
        {sending && (
          <div className="msg-wrap ai">
            <div className="bubble typing">
              <span/><span/><span/>
            </div>
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className="chat-input-bar">
        <textarea
          ref={textareaRef}
          className="chat-input"
          placeholder="输入消息，Enter 发送，Shift+Enter 换行"
          rows={1}
          value={input}
          onChange={e => { setInput(e.target.value); autoResize() }}
          onKeyDown={onKeyDown}
          disabled={!connected}
        />
        <button className="send-btn" onClick={send} disabled={!input.trim() || sending || !connected}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/>
          </svg>
        </button>
      </div>
    </main>
  )
}

// ─── Nav items config ─────────────────────────────────────────
type Page = 'dashboard' | 'news' | 'kol-tweets' | 'ai-chat'

const NAV_ITEMS: { page: Page; label: string; icon: React.ReactNode }[] = [
  { page: 'dashboard', label: '仪表盘', icon: <svg key="d" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg> },
  { page: 'news', label: '行情新闻', icon: <svg key="n" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M4 22h16a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2H8a2 2 0 0 0-2 2v16a2 2 0 0 1-2 2Zm0 0a2 2 0 0 1-2-2v-9c0-1.1.9-2 2-2h2"/><path d="M18 14h-8"/><path d="M15 18h-5"/><path d="M10 6h8v4h-8V6Z"/></svg> },
  { page: 'kol-tweets', label: '大V推文', icon: <svg key="t" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg> },
  { page: 'ai-chat', label: 'AI 对话', icon: <svg key="c" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg> },
]

// ─── App ───────────────────────────────────────────────────────
export default function App() {
  const [page, setPage] = useState<Page>('dashboard')
  const [fadeKey, setFadeKey] = useState(0)
  const { theme, changeTheme } = useTheme()
  const { collapsed, toggle } = useSidebar()
  const { lang, changeLang } = useLang()

  // KOL 订阅状态
  const [kolAuthors, setKolAuthors] = useState<TweetAuthor[]>([])
  const [subscribed, setSubscribed] = useState<string[]>(() => {
    try { return JSON.parse(localStorage.getItem('kol-subscribed') || '[]') } catch { return [] }
  })

  // 加载 KOL 列表
  useEffect(() => {
    fetch('/api/kol')
      .then(r => r.json())
      .then(d => {
        const authors: TweetAuthor[] = d.authors || []
        setKolAuthors(authors)
        // 如果本地没有订阅记录，默认全选
        if (subscribed.length === 0) {
          const all = authors.map((a: TweetAuthor) => a.handle)
          setSubscribed(all)
          localStorage.setItem('kol-subscribed', JSON.stringify(all))
        }
      })
      .catch(() => {})
  }, [])

  const toggleKol = (handle: string) => {
    setSubscribed(prev => {
      const next = prev.includes(handle) ? prev.filter(h => h !== handle) : [...prev, handle]
      localStorage.setItem('kol-subscribed', JSON.stringify(next))
      return next
    })
  }

  const navigate = (p: Page) => {
    if (p !== page) {
      setPage(p)
      setFadeKey(k => k + 1)
    }
  }

  return (
    <div className={`app ${collapsed ? 'sidebar-collapsed' : ''}`}>
      {/* Sidebar */}
      <nav className="sidebar">
        <div className="sidebar-top">
          <div className="sidebar-logo">
            <div className="logo-icon">💱</div>
            {!collapsed && <span className="logo-text">链上监控</span>}
          </div>

          <button className="sidebar-toggle" onClick={toggle} title={collapsed ? '展开侧边栏' : '收起侧边栏'}>
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              {collapsed
                ? <><polyline points="9 18 15 12 9 6"/></>
                : <><polyline points="15 18 9 12 15 6"/></>
              }
            </svg>
          </button>

          <div className="sidebar-nav">
            {NAV_ITEMS.map(({ page: p, label, icon }) => (
              <button key={p} className={`nav-btn ${page === p ? 'active' : ''}`} onClick={() => navigate(p)} title={label}>
                <span className="nav-icon">{icon}</span>
                {!collapsed && <span>{label}</span>}
              </button>
            ))}
          </div>
        </div>

        <div className="sidebar-footer">
          {!collapsed && (
            <select
              className="theme-select"
              value={theme}
              onChange={e => changeTheme(e.target.value as Theme)}
            >
              <option value="light">☀️ 浅色</option>
              <option value="dark">🌙 深色</option>
              <option value="system">💻 跟随系统</option>
            </select>
          )}
          <div className="lang-switch">
            <button className={`lang-btn ${lang === 'zh' ? 'active' : ''}`} onClick={() => changeLang('zh')}>中</button>
            <span>·</span>
            <button className={`lang-btn ${lang === 'en' ? 'active' : ''}`} onClick={() => changeLang('en')}>EN</button>
          </div>
        </div>
      </nav>

      {/* Content - 只渲染当前页面，避免全部 remount */}
      <div className="content" key={fadeKey}>
        {page === 'dashboard' && <Dashboard />}
        {page === 'news' && <News lang={lang} />}
        {page === 'kol-tweets' && <KolTweets lang={lang} kolAuthors={kolAuthors} subscribed={subscribed} onToggle={toggleKol} />}
        {page === 'ai-chat' && <AiChat />}
      </div>
    </div>
  )
}
