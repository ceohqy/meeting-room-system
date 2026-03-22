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

## 🖼 项目截图
<img width="2553" height="1336" alt="ed0e27d9-800f-4073-84bd-880e615e7391" src="https://github.com/user-attachments/assets/3082fda5-1b29-43b5-b8de-a91786dc2a1c" />
<img width="2559" height="1346" alt="b5140253-3eef-496f-a3db-b869ab22562d" src="https://github.com/user-attachments/assets/33575e01-e92b-4012-9dbc-02fdc9f7b7e7" />
<img width="2559" height="1347" alt="b9724ffd-28b6-4fb7-9f88-fdb49c2f5131" src="https://github.com/user-attachments/assets/4af7f801-c097-4674-acad-1092980c294c" />
<img width="2559" height="1344" alt="72afdf0c-6f95-490a-9cdb-91e4c344f731" src="https://github.com/user-attachments/assets/5a16b0de-a065-486c-b20e-acfc9621145e" />

<img width="2554" height="1338" alt="88c4d40a-cc5c-47a7-9dbf-0ff60566d673" src="https://github.com/user-attachments/assets/253ed0b9-cf92-4192-b3ea-6902d9582840" />


<img width="2558" height="1344" alt="ee06d6fb-60ef-4f61-91e0-b1b8e2bcde4f" src="https://github.com/user-attachments/assets/40877592-4abc-4288-8359-ecb18820e3f3" />
<img width="2559" height="1327" alt="77c7d141-bf5a-4fa1-bf31-d358ec5632cd" src="https://github.com/user-attachments/assets/25893ea2-55a7-427c-a142-d3c4f24598d0" />



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

🔹 技术亮点
前后端分离架构（Vue3 + Spring Boot）
基于 Vite 构建，提升开发效率
模块化前端设计（api / router / store / views）
RESTful API 设计规范
🔹 业务亮点
多租户数据隔离设计
时间冲突检测机制（核心业务逻辑）
完整的预约 + 审批流程闭环
