# Infra Market 项目

这是一个基于 Kotlin + Spring Boot + MyBatis-Flex 的基础设施市场项目，包含后端服务和前端管理界面。

## 项目结构

```
infra-market/
├── infra-market-server/     # 后端服务 (Kotlin + Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   └── io/infra/market/
│   │   │   │       ├── controller/    # 控制器层
│   │   │   │       ├── service/       # 服务层
│   │   │   │       ├── dao/           # 数据访问层
│   │   │   │       │   ├── dao/       # DAO实现类
│   │   │   │       │   ├── entity/    # 实体类
│   │   │   │       │   └── mapper/    # Mapper接口
│   │   │   │       └── InfraMarketApplication.kt
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   └── pom.xml
├── infra-market-admin/      # 前端管理界面 (Vue 3 + TypeScript)
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 公共组件
│   │   ├── layouts/        # 布局组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # 状态管理
│   │   ├── types/          # TypeScript类型定义
│   │   ├── utils/          # 工具函数
│   │   ├── views/          # 页面组件
│   │   │   ├── auth/       # 认证相关页面
│   │   │   ├── user/       # 用户管理页面
│   │   │   └── permission/ # 权限管理页面
│   │   ├── styles/         # 样式文件
│   │   ├── App.vue         # 根组件
│   │   └── main.ts         # 入口文件
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
├── pom.xml                 # 父级Maven配置
└── .cursorrules            # Cursor开发规则
```

## 技术栈

### 后端技术栈
- **语言**: Kotlin
- **框架**: Spring Boot
- **ORM**: MyBatis-Flex
- **数据库**: MySQL
- **构建工具**: Maven
- **其他**: Hutool, Apache POI

### 前端技术栈
- **框架**: Vue 3
- **语言**: TypeScript
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **构建工具**: Vite
- **HTTP客户端**: Axios

## 功能特性

### 后端功能
- ✅ 用户认证与授权
- ✅ 用户管理
- ✅ 角色管理
- ✅ 权限管理
- ✅ RESTful API
- ✅ 数据库操作
- ✅ 异常处理

### 前端功能
- ✅ 用户认证与授权
- ✅ 用户管理界面
- ✅ 角色管理界面
- ✅ 权限管理界面
- ✅ 响应式布局
- ✅ 路由守卫
- ✅ 权限控制

## 快速开始

### 环境要求
- JDK 17+
- Node.js 16+
- MySQL 8.0+

### 后端启动

1. 配置数据库连接
```bash
cd infra-market-server
# 修改 src/main/resources/application.properties 中的数据库配置
```

2. 启动后端服务
```bash
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 安装依赖
```bash
cd infra-market-admin
npm install
```

2. 启动开发服务器
```bash
npm run dev
```

前端应用将在 `http://localhost:3000` 启动

## API接口

### 认证相关
- `POST /auth/login` - 用户登录
- `POST /auth/logout` - 用户登出
- `GET /auth/current-user` - 获取当前用户信息

### 用户管理
- `GET /users` - 获取用户列表
- `POST /users` - 创建用户
- `PUT /users/{id}` - 更新用户
- `DELETE /users/{id}` - 删除用户
- `PATCH /users/{id}/status` - 更新用户状态

### 角色管理
- `GET /roles` - 获取角色列表
- `POST /roles` - 创建角色
- `PUT /roles/{id}` - 更新角色
- `DELETE /roles/{id}` - 删除角色

### 权限管理
- `GET /permissions` - 获取权限列表
- `GET /permissions/tree` - 获取权限树
- `POST /permissions` - 创建权限
- `PUT /permissions/{id}` - 更新权限
- `DELETE /permissions/{id}` - 删除权限

## 权限控制

系统采用基于角色的访问控制（RBAC）模型：

1. **用户**：系统使用者
2. **角色**：权限的集合
3. **权限**：具体的操作权限

权限编码格式：`模块:操作`，例如：
- `user:list` - 用户列表查看权限
- `user:create` - 用户创建权限
- `user:update` - 用户更新权限
- `user:delete` - 用户删除权限

## 开发规范

### 后端开发规范
- 遵循Kotlin最佳实践
- 使用Spring Boot自动配置
- 合理使用MyBatis-Flex的DSL查询
- 保持代码简洁和可读性

### 前端开发规范
- 使用TypeScript进行类型检查
- 遵循ESLint规则
- 使用Composition API
- 组件名使用PascalCase

## 部署

### 后端部署
```bash
cd infra-market-server
mvn clean package
java -jar target/infra-market-server-0.3.4-RELEASE.jar
```

### 前端部署
```bash
cd infra-market-admin
npm run build
# 将 dist 目录部署到 Web 服务器
```

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
