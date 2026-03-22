# 会议室预约管理系统 - 前端

基于 Vue3 + Vite + Element Plus + ECharts 的现代化多租户会议室预约管理系统。

## 技术栈
- Vue 3 (Composition API)
- Vite 5
- Vue Router 4
- Pinia 状态管理
- Axios HTTP
- Element Plus UI
- ECharts 图表
- DM Sans + Sora 字体

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器（默认 http://localhost:3000）
npm run dev

# 构建生产版本
npm run build
```

## 后端配置

默认代理地址：`http://localhost:8080`

修改 `vite.config.js` 中的 `proxy.target` 来更改后端地址。

## 项目结构

```
src/
├── api/           # Axios API 封装
│   ├── request.js    # axios 实例 + 拦截器
│   ├── auth.js       # 登录/认证
│   ├── room.js       # 会议室 CRUD
│   ├── reservation.js # 预约管理
│   ├── equipment.js  # 配件管理
│   ├── approval.js   # 审批管理
│   └── statistics.js # 统计数据
├── router/        # Vue Router 路由配置
├── store/         # Pinia 状态管理
├── layouts/       # 主布局 (侧边栏 + 顶栏)
├── views/         # 页面组件
│   ├── login/        # 登录页
│   ├── dashboard/    # 首页仪表盘
│   ├── meetingRoom/  # 会议室管理
│   ├── reservation/  # 预约管理
│   ├── equipment/    # 配件管理
│   ├── approval/     # 审批管理
│   └── statistics/   # 数据统计
└── styles/        # 全局样式
```

## 接口对应
| 功能 | 接口 |
|------|------|
| 会议室列表 | GET /room/list |
| 新增会议室 | POST /room/add |
| 编辑会议室 | PUT /room/update |
| 删除会议室 | DELETE /room/delete/:id |
| 预约列表 | GET /reservation/list |
| 新增预约 | POST /reservation/add |
| 审批列表 | GET /approval/list |
| 审批通过 | POST /approval/pass/:id |
| 审批拒绝 | POST /approval/reject/:id |
| 配件列表 | GET /equipment/list |
| 新增配件 | POST /equipment/add |
| 编辑配件 | PUT /equipment/update |
| 删除配件 | DELETE /equipment/delete/:id |

## 默认账号
- 账号: admin
- 密码: 123456
