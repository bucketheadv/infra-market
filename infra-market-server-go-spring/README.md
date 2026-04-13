# infra-market-server-go-spring

该模块在 `infra-market-server-go` 原有业务代码基础上迁移而来，目标是保持业务逻辑与接口行为一致，并将启动与依赖管理切换为 `go-spring` 风格。

## 迁移说明

- 业务分层保持不变：`controller -> service -> repository -> gorm`
- 路由与接口保持一致（用户、角色、权限、接口管理、执行记录、仪表盘、活动等）
- 中间件保持并增强：`CORS`、`Auth`、`ErrorHandler`、`RequestLogMiddleware`
- 配置格式切换为 `config/app.properties`

## go-spring 使用点

- 使用 `gs.Provide(...)` 注册 Repository / Service / Controller / Router / Server
- 通过构造函数参数自动完成依赖注入
- 通过 `starter-gorm-mysql` 自动注入 `*gorm.DB`
- 通过 `starter-go-redis` 自动注入 `*redis.Client`
- 使用 `gs.Provide(NewGinServer).AsServer()` 托管服务生命周期
- 通过 `gs.Property(...)` 指定 `./config` 为配置目录
- 关闭 go-spring 内置的简单 HTTP/pprof server，仅保留业务 Gin server
- 使用 `gs.Run()` 启动应用

## 目录结构

```text
infra-market-server-go-spring/
├── go.mod
├── go.sum
├── main.go
├── config/
│   ├── app.properties
│   └── app.properties.example
└── internal/
    ├── config/
    ├── controller/
    ├── dto/
    ├── entity/
    ├── enums/
    ├── middleware/
    ├── repository/
    ├── router/
    ├── service/
    └── util/
```

## 启动方式

1. 准备配置文件

```bash
cd infra-market-server-go-spring
cp config/app.properties.example config/app.properties
```

2. 按需修改 `config/app.properties`

- `server.port` 服务端口
- `server.mode` `debug/test/release`
- `gorm.log-level` SQL 日志级别（`info/warn/error/silent`）
- `spring.gorm.main.url` MySQL 连接
- `spring.go-redis.main.*` Redis 连接

3. 拉取依赖并启动

```bash
go mod tidy
go run .
```

## 说明

- 当前模块默认与原项目保持相同业务接口，不额外改动业务返回结构
- 若本机设置了 `GOFLAGS=-mod=readonly`，首次拉取新依赖前请先允许更新 `go.sum`
