# Infra Market Admin

基于 Vue 3 + TypeScript + Ant Design Vue 的管理后台前端项目。

## 技术栈

- **框架**: Vue 3
- **语言**: TypeScript
- **UI组件库**: Ant Design Vue 4.x
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **构建工具**: Vite
- **HTTP客户端**: Axios

## 功能特性

- ✅ 用户认证与授权
- ✅ 用户管理
- ✅ 角色管理
- ✅ 权限管理
- ✅ 响应式布局
- ✅ 路由守卫
- ✅ 权限控制

## 项目结构

```
src/
├── api/                 # API接口
│   ├── auth.ts         # 认证相关API
│   ├── user.ts         # 用户管理API
│   ├── role.ts         # 角色管理API
│   └── permission.ts   # 权限管理API
├── components/         # 公共组件
├── layouts/           # 布局组件
│   └── MainLayout.vue # 主布局
├── router/            # 路由配置
│   └── index.ts
├── stores/            # 状态管理
│   └── auth.ts        # 认证状态
├── types/             # TypeScript类型定义
│   └── index.ts
├── utils/             # 工具函数
│   └── request.ts     # HTTP请求工具
├── views/             # 页面组件
│   ├── auth/          # 认证相关页面
│   ├── user/          # 用户管理页面
│   └── permission/    # 权限管理页面
├── styles/            # 样式文件
│   └── index.css
├── App.vue            # 根组件
└── main.ts            # 入口文件
```

## 开发环境

### 环境要求

- Node.js >= 16
- npm >= 8

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 配置说明

### 后端API配置

在 `vite.config.ts` 中配置代理：

```typescript
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080', // 后端服务地址
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, ''),
    },
  },
}
```

### 环境变量

创建 `.env` 文件：

```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=Infra Market Admin
```

## 使用说明

### 登录

1. 访问登录页面 `/login`
2. 输入用户名和密码
3. 登录成功后自动跳转到仪表盘

### 用户管理

- 查看用户列表
- 创建新用户
- 编辑用户信息
- 重置用户密码
- 启用/禁用用户
- 删除用户

### 角色管理

- 查看角色列表
- 创建新角色
- 编辑角色信息
- 分配权限
- 启用/禁用角色
- 删除角色

### 权限管理

- 查看权限列表
- 创建新权限
- 编辑权限信息
- 设置权限层级
- 启用/禁用权限
- 删除权限

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

### 代码规范

- 使用 TypeScript 进行类型检查
- 遵循 ESLint 规则
- 使用 Prettier 格式化代码

### 组件规范

- 使用 Composition API
- 组件名使用 PascalCase
- 文件名使用 PascalCase

### API规范

- 统一使用 `request` 工具发送请求
- 响应数据格式统一
- 错误处理统一

## 部署

### 构建

```bash
npm run build
```

### 部署到服务器

将 `dist` 目录下的文件部署到 Web 服务器即可。

### Nginx配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 常见问题

### 1. 跨域问题

确保后端已配置 CORS 或使用代理配置。

### 2. 权限问题

检查用户角色和权限配置是否正确。

### 3. 路由问题

确保路由配置正确，特别是动态路由参数。

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交更改
4. 推送到分支
5. 创建 Pull Request

## 许可证

MIT License
