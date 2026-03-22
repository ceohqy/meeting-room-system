```
# 🏢 多租户会议室预约系统

## 📌 项目简介
本项目是一个基于 Vue3 + Spring Boot 的前后端分离会议室预约系统，支持多租户隔离，实现会议室预约、审批及时间冲突检测等功能。

适用于企业内部会议室资源管理，提高会议资源使用效率。

---

## 🧱 技术架构

### 前端（frontend）
- Vue3（Composition API）
- Vite（构建工具）
- Vue Router（路由管理）
- 状态管理（store）
- Axios（接口请求）
- Element Plus（UI组件）

前端采用模块化设计：

src/
├── api/ # 接口封装
├── router/ # 路由管理
├── store/ # 状态管理
├── views/ # 页面
├── layouts/ # 布局
├── components/ # 公共组件


---

### 后端（backend）
- Spring Boot
- Maven 构建
- RESTful API 设计

目录结构：

src/main/java/com/meeting/


👉 分层：
- controller（接口层）
- service（业务层）
- mapper/dao（数据层）
- entity（实体类）

---

### 数据库
- MySQL
- 初始化脚本：`database/init_data.sql`

---

## ⭐ 核心功能

### 1️⃣ 多租户管理（核心设计）
- 不同租户数据隔离
- 每个租户独立会议室资源

---

### 2️⃣ 会议室管理
- 会议室增删改查
- 支持容量、设备等信息管理

---

### 3️⃣ 会议预约
- 用户选择时间段预约会议室
- 填写会议主题、用途等信息

---

### 4️⃣ 时间冲突检测（重点🔥）
- 检测同一会议室时间是否冲突
- 避免重复预约
- 提高系统可靠性

---

### 5️⃣ 审批流程
- 管理员审批预约
- 状态流转：待审批 → 已通过 / 已拒绝

---

### 6️⃣ 预约记录管理
- 查询历史预约
- 支持筛选与搜索

---

## 🖼 项目截图（强烈建议补）

> 在这里放你的系统界面截图（非常加分）

---

## ⚙️ 项目结构


meeting-room-system/
├── frontend/ # Vue3 前端项目
├── backend/ # Spring Boot 后端
├── database/ # SQL脚本
└── README.md


---

## 🚀 启动方式

### 1️⃣ 启动后端

​```bash
cd backend
mvn spring-boot:run
2️⃣ 启动前端
cd frontend
npm install
npm run dev
3️⃣ 数据库初始化

执行：

database/init_data.sql
```

frontend/   前端项目
backend/    后端服务
database/   数据库脚本

```
---

## 🧪 启动方式

### 后端
```

cd backend
mvn spring-boot:run

```
### 前端
```

cd frontend
npm install
npm run dev

```
### 数据库
执行：
```

database/init_data.sql

```
---

## 💡 项目亮点

- 采用前后端分离架构，接口设计清晰
- 实现会议时间冲突检测逻辑
- 多租户设计，提高系统扩展性
```