# 末端驿站包裹管理系统

> Parcel Station Package Management System

一个轻量级的末端驿站包裹管理原型系统，用于快递物流"最后一百米"环节的包裹接收、存放与交付管理。

GitHub: [https://github.com/Koyzell/packageControlSystem](https://github.com/Koyzell/packageControlSystem)

---

## 一、项目简介

本系统模拟菜鸟驿站、丰巢等末端快递站点的日常运营场景，为驿站工作人员提供包裹入库、查询、取件、逾期监控等核心功能。系统采用前后端分离架构，后端提供 RESTful API，前端为纯静态页面，零工具链依赖。

### 核心功能

- **包裹入库**：录入运单号、收件人手机号、快递公司、货架位置，系统自动生成取件码
- **多维度查询**：支持按手机号、取件码、运单号三种方式查询包裹
- **取件管理**：管理员代客取件、用户自助取件，记录出库时间
- **逾期预警**：入库超过 48 小时未取件自动标记"滞留"，红色高亮显示
- **统计看板**：实时展示今日入库、今日取件、滞留数量、待取总数
- **动态配置**：货架和快递公司支持界面新增和删除，即时生效
- **操作日志**：入库、取件操作自动记录，可追溯
- **用户认证**：JWT 登录认证，管理员与普通用户角色分离

---

## 二、技术栈

| 层级   | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 3.5.16 |
| 持久层  | MyBatis | 3.0.4 |
| 数据库  | MySQL | 8.0 |
| 认证   | JWT (jjwt) | 0.12.6 |
| 工具库  | Lombok | — |
| 前端   | 原生 HTML5 + CSS3 + JavaScript (ES6) | — |
| UI 框架 | Bootstrap 5.3 + Bootstrap Icons (CDN) | — |
| 构建工具 | Maven | 3.9 |
| JDK  | Java 17 | — |

---

## 三、项目结构

```
packageControllSystem/
├── pom.xml                                          # Maven 依赖配置
├── CLAUDE.md                                        # Claude Code 项目上下文
├── README.md                                        # 本文件
├── src/
│   ├── main/
│   │   ├── java/com/ustb/
│   │   │   ├── PackageControllSystemApplication.java  # 启动入口
│   │   │   ├── common/
│   │   │   │   └── Result.java                        # 统一响应封装 {code, message, data}
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java                     # BCryptPasswordEncoder Bean
│   │   │   │   ├── DataInitializer.java                # 启动时初始化种子用户
│   │   │   │   ├── JwtProperties.java                  # JWT 配置属性 (secret/expiration)
│   │   │   │   ├── JwtUtil.java                        # JWT 生成与解析工具
│   │   │   │   ├── LoginInterceptor.java               # JWT 认证拦截器
│   │   │   │   └── WebMvcConfig.java                   # CORS + 拦截器注册
│   │   │   ├── controller/
│   │   │   │   ├── AdminController.java                # 管理员 API (/api/admin/**)
│   │   │   │   ├── AuthController.java                 # 登录 API (/api/auth/login)
│   │   │   │   ├── MetaController.java                 # 货架/快递公司管理
│   │   │   │   └── UserController.java                 # 用户 API (/api/user/**)
│   │   │   ├── dto/
│   │   │   │   ├── CheckInRequest.java                 # 入库请求体 (含校验)
│   │   │   │   ├── DashboardVO.java                    # 统计面板数据
│   │   │   │   ├── LoginRequest.java                   # 登录请求体
│   │   │   │   ├── LoginResponse.java                  # 登录响应 (token/username/role)
│   │   │   │   ├── OperationLogVO.java                 # 操作日志视图
│   │   │   │   ├── PackageVO.java                      # 包裹响应 (含 overdue 计算)
│   │   │   │   └── PageResult.java                     # 分页结果封装
│   │   │   ├── entity/
│   │   │   │   ├── CourierEntity.java                  # 快递公司实体
│   │   │   │   ├── OperationLogEntity.java             # 操作日志实体
│   │   │   │   ├── PackageEntity.java                  # 包裹实体
│   │   │   │   ├── ShelfEntity.java                    # 货架实体
│   │   │   │   └── UserEntity.java                     # 用户实体
│   │   │   ├── enums/
│   │   │   │   └── PackageStatus.java                  # AWAITING_PICKUP / PICKED_UP
│   │   │   ├── exception/
│   │   │   │   ├── BusinessException.java              # 业务异常
│   │   │   │   └── GlobalExceptionHandler.java         # 全局异常处理
│   │   │   ├── mapper/
│   │   │   │   ├── CourierMapper.java                  # 快递公司 Mapper
│   │   │   │   ├── OperationLogMapper.java             # 操作日志 Mapper
│   │   │   │   ├── PackageMapper.java                  # 包裹 Mapper (核心)
│   │   │   │   ├── ShelfMapper.java                    # 货架 Mapper
│   │   │   │   └── UserMapper.java                     # 用户 Mapper
│   │   │   └── service/
│   │   │       ├── PackageService.java                 # 服务接口
│   │   │       └── impl/
│   │   │           └── PackageServiceImpl.java         # 核心业务逻辑
│   │   └── resources/
│   │       ├── application.yaml                       # 应用配置 (MySQL/JWT)
│   │       ├── schema.sql                              # 建库建表 SQL + 种子数据
│   │       ├── test-data.sql                           # 30 条开发测试数据
│   │       └── mapper/
│   │           └── PackageMapper.xml                    # 复杂 SQL（分页查询）
│   │       └── static/
│   │           ├── index.html                          # 首页（智能路由）
│   │           ├── login.html                          # 登录页
│   │           ├── admin.html                          # 管理员工作台
│   │           ├── user.html                           # 用户取件页
│   │           └── css/
│   │               └── style.css                       # 自定义样式
│   └── test/
│       ├── java/com/ustb/
│       │   └── PackageControllerTest.java              # 19 条测试用例
│       └── resources/
│           ├── application.yaml                        # H2 测试配置
│           └── schema.sql                              # H2 建表 SQL
```

---

## 四、API 接口

所有接口统一返回格式：

```json
{
    "code": 200,
    "message": "success",
    "data": { ... }
}
```

### 4.1 认证接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|:---:|------|
| POST | `/api/auth/login` | 公开 | 登录，请求体 `{username, password}`，返回 `{token, username, role}` |

### 4.2 管理员接口（需 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/admin/packages/check-in` | 包裹入库，返回包含取件码的包裹信息 |
| GET | `/api/admin/packages` | 分页查询包裹列表（参数：`page`, `pageSize`, `keyword`, `status`） |
| GET | `/api/admin/packages/overdue` | 查询逾期包裹（>48h + 待取件） |
| PUT | `/api/admin/packages/{id}/pickup` | 管理员代客取件 |
| GET | `/api/admin/dashboard` | 统计面板（今日入库/取件/滞留/待取数） |
| GET | `/api/admin/logs` | 最近 20 条操作日志 |
| GET | `/api/admin/shelves` | 货架列表 |
| POST | `/api/admin/shelves` | 新增货架 |
| DELETE | `/api/admin/shelves?name=xxx` | 删除货架 |
| GET | `/api/admin/couriers` | 快递公司列表 |
| POST | `/api/admin/couriers` | 新增快递公司|
| DELETE | `/api/admin/couriers?name=xxx` | 删除快递公司 |

### 4.3 用户接口（需登录，ADMIN 或 USER 均可）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/packages?keyword=xxx` | 按手机号/取件码/运单号查询待取件包裹 |
| PUT | `/api/user/packages/{id}/pickup` | 用户确认取件 |

### 4.4 认证说明

除 `/api/auth/login` 外，所有 `/api/**` 请求需携带 `Authorization: Bearer <token>` 请求头。管理员接口额外要求 `role=ADMIN`。

---

## 五、数据库设计

### 5.1 E-R 概览

```
packages ─────────────────────────────────────────────
│ id │ tracking_number │ pickup_code │ recipient_phone │
│ courier_company │ shelf_location │ status │
│ check_in_time │ check_out_time │ created_at │ updated_at │

users ─────────────────────
│ id │ username │ password │ role │

operation_logs ───────────────────────────────────────
│ id │ package_id │ tracking_number │ operation_type │
│ operator │ details │ created_at │

shelves ─────────────────
│ id │ name │

couriers ────────────────
│ id │ name │
```

### 5.2 packages 表（核心）

| 字段 | 类型 | 约束                                         | 说明 |
|------|------|--------------------------------------------|------|
| id | BIGINT | PK, AUTO_INCREMENT                         | 主键 |
| tracking_number | VARCHAR(64) | UNIQUE, NOT NULL                           | 运单号 |
| recipient_phone | VARCHAR(20) | NOT NULL, INDEX                            | 收件人手机号 |
| courier_company | VARCHAR(32) | NOT NULL                                   | 快递公司 |
| shelf_location | VARCHAR(32) | NOT NULL                                   | 货架位置 |
| pickup_code | VARCHAR(20) | UNIQUE                                     | 取件码，格式 `YYMMDD-L-NNN` |
| status | VARCHAR(16) | NOT NULL, DEFAULT 'AWAITING_PICKUP', INDEX | `AWAITING_PICKUP` / `PICKED_UP` |
| check_in_time | DATETIME | NOT NULL, INDEX                            | 入库时间 |
| check_out_time | DATETIME | NULL                                       | 出库时间 |
| created_at | DATETIME | NOT NULL                                   | 创建时间 |
| updated_at | DATETIME | NOT NULL, ON UPDATE                        | 更新时间 |

### 5.3 users 表

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | BIGINT | PK, AUTO_INCREMENT | 主键 |
| username | VARCHAR(32) | UNIQUE, NOT NULL | 用户名 |
| password | VARCHAR(128) | NOT NULL | BCrypt 加密密码 |
| role | VARCHAR(16) | NOT NULL, DEFAULT 'USER' | ADMIN / USER |

预置账号：`admin / admin123`（管理员）、`user / user123`（普通用户）

### 5.4 operation_logs 表

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| package_id | BIGINT | 关联包裹 ID |
| tracking_number | VARCHAR(64) | 运单号（冗余） |
| operation_type | VARCHAR(16) | CHECK_IN / PICK_UP |
| operator | VARCHAR(32) | 操作人用户名 |
| details | VARCHAR(256) | 操作详情 |
| created_at | DATETIME | 操作时间 |

### 5.5 设计要点

- **逾期不存储**：逾期状态由 `checkInTime + 48h < now` 实时计算，避免数据不一致
- **取件码规则**：`YYMMDD` + 字母 A-Z（每组 999 个序号）+ 三位序号，单日容量 25,974 件。`pickup_code` 设 UNIQUE 约束，INSERT 碰撞时自动重新生成取件码重试（最多 5 次）
- **并发安全**：`checkIn()` 和 `pickup()` 加 `@Transactional` 保证包裹入库 + 操作日志的原子性。取件采用乐观锁：`UPDATE ... WHERE status = 'AWAITING_PICKUP'`，影响行数为 0 则视为已被取走
- **MyBatis 下划线映射**：`map-underscore-to-camel-case: true` 自动将数据库 `snake_case` 映射到 Java `camelCase`
- **运单号唯一**：数据库层面 UNIQUE 约束防止重复入库，并发 INSERT 冲突时捕获 `DuplicateKeyException` 返回 409

---

## 六、系统功能说明

### 6.1 页面导航

```
index.html（智能路由）
    ├── 无 token → login.html
    ├── role=ADMIN → admin.html
    └── role=USER  → user.html
```

### 6.2 管理员工作台（admin.html）

- **统计看板**：顶部 4 个卡片实时展示今日入库数、今日取件数、当前滞留数、待取总数
- **包裹入库**：表单填写运单号、手机号、快递公司（下拉选择，弹窗增删）、货架位置（下拉选择，弹窗增删），提交后自动生成取件码
- **包裹列表**：支持关键词搜索、状态筛选（全部/待取件/已取件/逾期滞留）、分页浏览
- **逾期标识**：入库超 48 小时的行显示红色左边框 + 红色底纹 + 红色"滞留"标签
- **操作日志**：页面底部展示最近 20 条操作记录

### 6.3 用户取件页（user.html）

- **查询包裹**：输入手机号、取件码或运单号，查询名下所有待取件包裹
- **确认取件**：点击按钮确认取件，包裹状态变为"已取件"，记录出库时间
- **逾期提示**：如有滞留包裹，同样显示红色警告标识

### 6.4 测试覆盖（19 条用例）

| 类别 | 用例 | 说明 |
|------|------|------|
| 正常流程 | TC-1 ~ TC-4 | 入库（含取件码验证）、手机号查询、取件、逾期检测 |
| 异常场景 | TC-5 ~ TC-10 | 重复运单号、无效手机号、空字段、重复取件、空查询、不存在的包裹 |
| 取件码 | TC-11 ~ TC-13 | 取件码格式校验、按取件码查询、按运单号查询 |
| 统计/日志 | TC-14 ~ TC-15 | Dashboard 统计、操作日志 |
| 认证 | TC-16 ~ TC-17 | 登录成功、登录失败 |
| 分页 | TC-18 | 分页参数验证 |

---

## 七、快速开始

### 环境要求

- JDK 17+
- MySQL 8.0+
- Maven 3.9+

### 步骤

1. **克隆项目**

```bash
git clone https://github.com/Koyzell/packageControlSystem.git
cd packageControlSystem
```

2. **创建数据库**

在 MySQL 中执行 `src/main/resources/sql/schema.sql`，建库建表。

3. **配置数据库连接**

修改 `src/main/resources/application.yaml` 中的数据库用户名和密码：

```yaml
spring:
  datasource:
    username: root
    password: 你的密码
```

4. **导入测试数据（可选）**

```bash
mysql -u root -p < src/main/resources/test-data.sql
```

5. **启动应用**

```bash
mvn spring-boot:run
```

6. **访问系统**

浏览器打开 `http://localhost:8080`，使用 `admin / admin123 / user / user123` 登录。

### 运行测试

```bash
mvn test
```