# 前端（frontend）

流浪动物救助管理系统的前端，Vue 3 + Element Plus + Vite + Pinia + ECharts。

## 开发

```bash
npm install
npm run dev          # 开发环境，默认 80 端口，通过 /dev-api 代理到后端 8080
```

## 构建

```bash
npm run build:prod   # 生产构建，输出到 dist/
```

## 目录说明

| 目录 | 说明 |
| --- | --- |
| `src/api` | 接口定义，按业务模块分目录 |
| `src/views` | 页面。`views/cat` 是业务页面（档案、医疗、TNR、领养、财务物资等），`views/system` / `views/monitor` 是系统管理 |
| `src/components` | 通用组件（上传、编辑器、分页、富文本等） |
| `src/layout` | 布局骨架（侧边栏、顶栏、标签页、底部版权） |
| `src/router` | 路由与动态菜单拼装 |
| `src/store` | Pinia 状态（用户、权限、设置、应用） |
| `src/assets` | 样式、图标、logo |
| `src/utils` | 请求封装、字典、权限校验、草稿缓存等 |

## 移动端

手机访问 `/m` 即为移动端工作台（汇总今日提醒），扫码打开 `/m/cat/:archiveNo` 是单只猫的档案页。
