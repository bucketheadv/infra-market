# Infra Market Vert.x 实现

这是基于 Vert.x 和 Kotlin 实现的 Infra Market 服务器，完全独立于原有的 Spring Boot 实现，不依赖任何 infra 三方包。

## 技术栈

- **框架**: Vert.x 5.0.6
- **语言**: Kotlin 2.3.0
- **数据库**: MySQL (使用 Vert.x MySQL Client)
- **缓存**: Redis (使用 Vert.x Redis Client)
- **JWT**: io.jsonwebtoken
- **JSON**: Jackson

## 项目结构

```
src/main/kotlin/io/infra/market/vertx/
├── config/              # 配置类
├── dto/                 # 数据传输对象
├── entity/              # 实体类
├── enums/               # 枚举类
├── middleware/          # 中间件（认证、权限等）
├── repository/          # 数据访问层（DAO）
├── router/              # 路由处理器
├── service/             # 业务逻辑层
└── util/                # 工具类
```

## 已实现功能

### 1. 认证模块 (Auth)
- ✅ 用户登录
- ✅ 获取当前用户信息
- ✅ 获取用户菜单
- ✅ 刷新Token
- ✅ 用户登出
- ✅ 修改密码

### 2. 用户管理 (User)
- ✅ 用户列表查询（支持分页和筛选）
- ✅ 用户详情查询
- ✅ 创建用户
- ✅ 更新用户
- ✅ 删除用户
- ✅ 更新用户状态

### 3. 仪表盘 (Dashboard)
- ✅ 获取仪表盘统计数据
- ✅ 最近登录用户列表
- ✅ 系统信息

## 配置说明

应用支持从配置文件加载配置，配置文件加载优先级（从高到低）：

1. **系统属性指定的配置文件路径**：`-Dvertx.config.file=/path/to/config.json`
2. **当前目录下的 `application.json`**
3. **classpath 下的 `application.json`**

### 配置文件格式

配置文件使用 JSON 格式，示例配置如下：

```json
{
  "server": {
    "port": 8080
  },
  "database": {
    "host": "localhost",
    "port": 3306,
    "database": "infra_market",
    "username": "root",
    "password": "your_password",
    "maxPoolSize": 10,
    "charset": "utf8mb4",
    "collation": "utf8mb4_unicode_ci"
  },
  "redis": {
    "host": "localhost",
    "port": 6379
  },
  "jwt": {
    "secretKey": "your-jwt-secret-key-change-this-in-production",
    "expirationTime": 259200000
  },
  "aes": {
    "defaultKey": "your-aes-key-change-this-in-production"
  }
}
```

### 配置项说明

- **server.port**: HTTP 服务器端口，默认 8080
- **database**: 数据库连接配置
  - **host**: 数据库主机，默认 localhost
  - **port**: 数据库端口，默认 3306
  - **database**: 数据库名称，默认 infra_market
  - **username**: 数据库用户名，默认 root
  - **password**: 数据库密码
  - **maxPoolSize**: 连接池最大连接数，默认 10
  - **charset**: 字符集，默认 utf8mb4
  - **collation**: 排序规则，默认 utf8mb4_unicode_ci
- **redis**: Redis 连接配置
  - **host**: Redis 主机，默认 localhost
  - **port**: Redis 端口，默认 6379
- **jwt**: JWT 配置
  - **secretKey**: JWT 密钥（生产环境必须修改）
  - **expirationTime**: Token 过期时间（毫秒），默认 259200000（3天）
- **aes**: AES 加密配置
  - **defaultKey**: AES 默认密钥（生产环境必须修改）

### 使用配置文件

1. **复制示例配置文件**：
   ```bash
   cp src/main/resources/application.example.json src/main/resources/application.json
   ```

2. **修改配置**：编辑 `application.json`，填入实际的数据库和 Redis 配置

3. **运行应用**：应用会自动从配置文件加载配置

### 通过系统属性指定配置文件

```bash
java -Dvertx.config.file=/path/to/your/config.json -jar target/infra-market-server-vertx-0.9.1-RELEASE.jar
```

## 运行方式

### 1. 编译项目

```bash
mvn clean package
```

### 2. 运行应用

```bash
java -jar target/infra-market-server-vertx-0.9.1-RELEASE.jar
```

或者直接运行主类：

```bash
mvn exec:java -Dexec.mainClass="io.infra.market.vertx.InfraMarketVertxApplication"
```

## API 接口

### 认证接口

- `POST /auth/login` - 用户登录
- `GET /auth/current/user` - 获取当前用户信息（需要认证）
- `GET /auth/user/menus` - 获取用户菜单（需要认证）
- `POST /auth/refresh/token` - 刷新Token（需要认证）
- `POST /auth/logout` - 用户登出（需要认证）
- `POST /auth/change/password` - 修改密码（需要认证）

### 用户管理接口

- `GET /users` - 获取用户列表（需要认证）
- `GET /users/:id` - 获取用户详情（需要认证）
- `POST /users` - 创建用户（需要认证）
- `PUT /users/:id` - 更新用户（需要认证）
- `DELETE /users/:id` - 删除用户（需要认证）
- `PATCH /users/:id/status` - 更新用户状态（需要认证）

### 仪表盘接口

- `GET /dashboard/data` - 获取仪表盘数据（需要认证）

### 健康检查

- `GET /health` - 健康检查接口

## 认证方式

所有需要认证的接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <token>
```

或者在查询参数中传递：

```
?token=<token>
```

## 注意事项

1. 本项目完全独立于原有的 Spring Boot 实现，不依赖任何 infra 三方包
2. 使用 Vert.x 的异步编程模型，所有数据库操作都是异步的
3. 使用 Vert.x SQL Client 进行数据库访问，支持连接池
4. 使用 Vert.x Redis Client 进行 Redis 操作
5. JWT Token 默认有效期为 3 天
6. 密码使用 AES 加密存储

## 待实现功能

以下功能可以参考原有 Spring Boot 实现进行补充：

- [ ] 角色管理（Role）
- [ ] 权限管理（Permission）
- [ ] 接口管理（ApiInterface）
- [ ] 接口执行记录（ApiInterfaceExecutionRecord）

## 开发说明

1. 所有数据库操作都使用 Vert.x 的 Future 异步模型
2. 服务层方法返回 `Future<ApiData<T>>`
3. 路由处理器使用 `onSuccess` 和 `onFailure` 处理异步结果
4. 使用 Jackson 进行 JSON 序列化和反序列化
5. 使用 SLF4J + Logback 进行日志记录

