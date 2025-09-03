# Infra Market Quarkus

基于 Quarkus 框架的基础设施市场项目，使用 Kotlin 语言开发。

## 技术栈

- **框架**: Quarkus 3.26.1
- **语言**: Kotlin 2.2.10
- **数据库**: MySQL + Hibernate ORM + Panache
- **缓存**: Redis
- **安全**: JWT (SmallRye JWT)
- **构建工具**: Maven
- **其他**: Joda Time, Hutool

## 项目结构

```
src/main/kotlin/io/infra/market/
├── controller/          # 控制器层
│   ├── AuthController.kt
│   ├── UserController.kt
│   ├── RoleController.kt
│   ├── PermissionController.kt
│   └── DashboardController.kt
├── service/            # 服务层
│   ├── AuthService.kt
│   ├── UserService.kt
│   ├── RoleService.kt
│   ├── PermissionService.kt
│   └── DashboardService.kt
├── repository/         # 数据访问层
│   ├── UserRepository.kt
│   ├── RoleRepository.kt
│   ├── PermissionRepository.kt
│   ├── UserRoleRepository.kt
│   └── RolePermissionRepository.kt
├── entity/            # 实体类
│   ├── User.kt
│   ├── Role.kt
│   ├── Permission.kt
│   ├── UserRole.kt
│   └── RolePermission.kt
├── dto/               # 数据传输对象
│   ├── ApiResponse.kt
│   ├── AuthDto.kt
│   ├── UserDto.kt
│   ├── RoleDto.kt
│   └── PermissionDto.kt
├── enums/             # 枚举类
│   ├── StatusEnum.kt
│   └── PermissionTypeEnum.kt
├── util/              # 工具类
│   ├── DateTimeUtil.kt
│   ├── AesUtil.kt
│   └── JwtUtil.kt
└── InfraMarketQuarkusApplication.kt
```

## 功能特性

### 1. 用户管理
- 用户创建、查询、更新、删除
- 用户状态管理
- 用户角色分配

### 2. 角色管理
- 角色创建、查询、更新、删除
- 角色状态管理
- 角色权限分配

### 3. 权限管理
- 权限创建、查询、更新、删除
- 权限树形结构
- 权限类型（菜单、按钮）

### 4. 认证授权
- JWT Token 认证
- 用户登录/登出
- 权限验证
- 菜单权限控制

### 5. 仪表板
- 系统统计信息
- 最近用户列表
- 系统运行状态

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库配置

在 `application.properties` 中配置数据库连接：

```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.username=root
quarkus.datasource.password=123456
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/infra_market
```

### 3. Redis 配置

```properties
quarkus.redis.hosts=redis://localhost:6379
```

### 4. 运行项目

```bash
# 开发模式
mvn quarkus:dev

# 构建项目
mvn clean package

# 运行 JAR 文件
java -jar target/quarkus-app/quarkus-run.jar
```

## API 接口

### 认证接口

- `POST /auth/login` - 用户登录
- `GET /auth/current/user` - 获取当前用户信息
- `GET /auth/user/menus` - 获取用户菜单
- `POST /auth/refresh/token` - 刷新 Token
- `POST /auth/logout` - 用户登出
- `POST /auth/change/password` - 修改密码

### 用户管理接口

- `GET /user/list` - 获取用户列表
- `GET /user/{id}` - 获取用户详情
- `POST /user` - 创建用户
- `PUT /user/{id}` - 更新用户
- `DELETE /user/{id}` - 删除用户
- `PUT /user/{id}/status` - 更新用户状态

### 角色管理接口

- `GET /role/list` - 获取角色列表
- `GET /role/{id}` - 获取角色详情
- `POST /role` - 创建角色
- `PUT /role/{id}` - 更新角色
- `DELETE /role/{id}` - 删除角色
- `PUT /role/{id}/status` - 更新角色状态

### 权限管理接口

- `GET /permission/list` - 获取权限列表
- `GET /permission/tree` - 获取权限树
- `GET /permission/{id}` - 获取权限详情
- `POST /permission` - 创建权限
- `PUT /permission/{id}` - 更新权限
- `DELETE /permission/{id}` - 删除权限
- `PUT /permission/{id}/status` - 更新权限状态

### 仪表板接口

- `GET /dashboard` - 获取仪表板数据

## 开发模式

项目支持 Quarkus 开发模式，提供热重载、实时配置更新等功能：

```bash
mvn quarkus:dev
```

访问 http://localhost:8080/q/dev 查看开发控制台。

## 健康检查

项目集成了健康检查功能：

- 健康检查根路径: `/health`
- 存活检查: `/health/live`
- 就绪检查: `/health/ready`

## 指标监控

项目集成了 Micrometer 指标监控：

- 应用指标: `/q/metrics`
- 健康指标: `/q/health`

## 构建原生镜像

```bash
# 构建原生镜像
mvn clean package -Pnative

# 运行原生镜像
./target/infra-market-quarkus-1.0-SNAPSHOT-runner
```

## 注意事项

1. 首次运行时会自动创建数据库表结构
2. 默认管理员账号: admin / 123456
3. JWT Token 有效期为 24 小时
4. 密码使用 MD5 加密存储
5. 时间日期处理统一使用 Joda Time

## 许可证

MIT License
