# Infra Market Server (.NET)

基于 ASP.NET Core 9.0 的基础设施市场服务端实现。

## 技术栈

- **框架**: ASP.NET Core 9.0
- **ORM**: Entity Framework Core 9.0
- **数据库**: MySQL (使用 Pomelo.EntityFrameworkCore.MySql)
- **认证**: JWT Bearer Token
- **缓存**: Redis (StackExchange.Redis)
- **密码加密**: BCrypt.Net-Next

## 项目结构

```
infra-market-server-dotnet/
├── Controllers/          # 控制器层
│   ├── AuthController.cs
│   └── UserController.cs
├── Services/            # 服务层
│   ├── AuthService.cs
│   ├── TokenService.cs
│   └── UserService.cs
├── Repositories/        # 数据访问层
│   ├── UserRepository.cs
│   ├── RoleRepository.cs
│   └── ...
├── Entities/            # 实体类
│   ├── User.cs
│   ├── Role.cs
│   └── ...
├── DTOs/                # 数据传输对象
│   ├── Auth.cs
│   ├── User.cs
│   └── ...
├── Data/                # 数据库上下文
│   └── ApplicationDbContext.cs
├── Config/              # 配置类
│   ├── JwtConfig.cs
│   └── ...
├── Middleware/         # 中间件
│   └── ErrorHandlingMiddleware.cs
├── Program.cs           # 程序入口
└── appsettings.json     # 配置文件
```

## 功能模块

### 已实现
- ✅ 用户认证（登录、登出、刷新Token）
- ✅ 用户管理（CRUD、状态管理、密码重置）
- ✅ JWT Token 生成和验证
- ✅ Redis Token 存储
- ✅ 错误处理中间件
- ✅ CORS 支持

### 待实现
- ⏳ 角色管理
- ⏳ 权限管理
- ⏳ API接口管理
- ⏳ API执行记录
- ⏳ 仪表盘数据

## 配置说明

编辑 `appsettings.json` 文件：

```json
{
  "Server": {
    "Port": "8080"
  },
  "Database": {
    "Host": "localhost",
    "Port": "3306",
    "User": "root",
    "Password": "your_password",
    "Database": "infra_market"
  },
  "JWT": {
    "Secret": "your-jwt-secret-key-change-this-in-production",
    "Expiration": 259200000
  },
  "Redis": {
    "Host": "localhost",
    "Port": "6379",
    "Password": "",
    "DB": 0
  }
}
```

## 运行项目

### 前置要求
- .NET 9.0 SDK
- MySQL 数据库
- Redis 服务器

### 运行步骤

1. 安装依赖：
```bash
dotnet restore
```

2. 配置数据库连接字符串（修改 `appsettings.json`）

3. 运行数据库迁移（如果需要）：
```bash
dotnet ef migrations add InitialCreate
dotnet ef database update
```

4. 启动项目：
```bash
dotnet run
```

项目将在 `http://localhost:8080` 启动。

## API 接口

### 认证接口

- `POST /auth/login` - 用户登录
- `GET /auth/current/user` - 获取当前用户信息
- `GET /auth/user/menus` - 获取用户菜单
- `POST /auth/refresh/token` - 刷新Token
- `POST /auth/logout` - 用户登出
- `POST /auth/change/password` - 修改密码

### 用户管理接口

- `GET /users` - 获取用户列表（支持分页）
- `GET /users/{id}` - 获取用户详情
- `POST /users` - 创建用户
- `PUT /users/{id}` - 更新用户
- `DELETE /users/{id}` - 删除用户
- `PATCH /users/{id}/status` - 更新用户状态
- `POST /users/{id}/reset/password` - 重置密码
- `POST /users/batch/delete` - 批量删除用户

## 开发说明

### 添加新功能

1. 在 `Entities/` 中创建实体类
2. 在 `DTOs/` 中创建DTO类
3. 在 `Repositories/` 中创建Repository接口和实现
4. 在 `Services/` 中创建Service接口和实现
5. 在 `Controllers/` 中创建Controller
6. 在 `Program.cs` 中注册依赖

### 数据库迁移

```bash
# 创建迁移
dotnet ef migrations add MigrationName

# 应用迁移
dotnet ef database update

# 回滚迁移
dotnet ef database update PreviousMigrationName
```

## 注意事项

1. 确保 MySQL 和 Redis 服务已启动
2. 修改 JWT Secret 为安全的随机字符串
3. 生产环境请使用 HTTPS
4. 密码使用 BCrypt 加密存储

## 许可证

与主项目保持一致。
