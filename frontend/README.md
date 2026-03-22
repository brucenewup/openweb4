<p align="center">
  <img src="../assets/screenshot-dashboard-zh.png" alt="OpenWeb4 仪表盘（中文界面）" width="820" />
</p>
<p align="center"><sub>默认 Web UI：Spring Boot SPA · 中文 · 仪表盘</sub></p>

# OpenWeb4 — `frontend/`（可选子项目）

## 关于本目录

这里是 **Vite + React + TypeScript** 的实验与备用前端工程。线上/本地默认使用的界面是 Spring Boot 内置的 **Thymeleaf 单页应用**（[`spa.html`](../src/main/resources/templates/spa.html)），与 `frontend/` 无强绑定；仅在需要独立构建或替换前端时再使用本目录。

## 文档与许可

- 项目说明（中/英）：[`README.md`](../README.md) · [`README.en.md`](../README.en.md)  
- 开源协议：[`LICENSE`](../LICENSE)（MIT）

## 交流与联系

欢迎加入 Telegram 群组 **openweb4**，讨论使用问题、功能想法与贡献协作：

[**https://t.me/+fK2gVWLZako2ZGFl**](https://t.me/+fK2gVWLZako2ZGFl)

（入群链接由 Telegram 生成；若失效请到仓库 Issue 反馈更新。）

---

# React + TypeScript + Vite

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is not enabled on this template because of its impact on dev & build performances. To add it, see [this documentation](https://react.dev/learn/react-compiler/installation).

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
