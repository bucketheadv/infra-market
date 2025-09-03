# Infra Market Quarkus 拦截器使用说明

## 概述

本项目已经成功移植了 `infra-market-server` 中的加密算法和拦截器功能，包括：

- AES 加密工具类
- JWT Token 验证
- 认证拦截器
- 权限拦截器
- 权限注解

## 新增功能

### 1. AES 加密工具类 (`AesUtil`)

位置：`src/main/kotlin/io/infra/qk/util/AesUtil.kt`

功能：
- 生成 AES 密钥
- 加密/解密字符串
- 密码验证

使用示例：
```kotlin
// 加密
val encrypted = AesUtil.encrypt("123456")
// 解密
val decrypted = AesUtil.decrypt(encrypted)
// 验证密码
val isValid = AesUtil.matches("123456", encrypted)
```

### 2. 认证拦截器 (`AuthInterceptor`)

位置：`src/main/kotlin/io/infra/qk/interceptor/AuthInterceptor.kt`

功能：
- 验证 JWT Token
- 自动设置用户ID到 ThreadLocal
- 处理未授权请求

### 3. 权限拦截器 (`PermissionInterceptor`)

位置：`src/main/kotlin/io/infra/qk/interceptor/PermissionInterceptor.kt`

功能：
- 根据路径自动检查权限
- 支持路径级别的权限控制
- 处理权限不足的情况

### 4. 权限注解 (`@RequiresPermission`)

位置：`src/main/kotlin/io/infra/qk/annotation/RequiresPermission.kt`

功能：
- 标记需要特定权限的方法
- 支持单个或多个权限验证
- 可配置权限描述

使用示例：
```kotlin
@RequiresPermission("user:manage", "用户管理权限")
fun createUser(user: User): User {
    // 方法实现
}

@RequiresPermission("admin:access,user:manage", "管理员或用户管理权限", true)
fun adminOrUserOperation(): String {
    // 方法实现
}
```

### 5. 权限检查服务 (`PermissionCheckService`)

位置：`src/main/kotlin/io/infra/qk/service/PermissionCheckService.kt`

功能：
- 检查用户权限
- 支持单个、多个权限验证
- 获取用户所有权限

### 6. Token 服务 (`TokenService`)

位置：`src/main/kotlin/io/infra/qk/service/TokenService.kt`

功能：
- 验证 JWT Token
- 刷新 Token
- 使 Token 失效

## 配置说明

### 1. Jackson 配置

新增了 `JacksonConfig` 类来配置 `ObjectMapper`，支持：
- Kotlin 模块
- Java 时间模块
- 自定义序列化配置

### 2. 拦截器配置

拦截器使用 Quarkus 的 `@Provider` 和 `@PreMatching` 注解自动注册，无需额外配置。

## 使用流程

### 1. 用户登录

1. 用户提供用户名和密码
2. 验证成功后生成 JWT Token
3. 返回 Token 给客户端

### 2. 请求认证

1. 客户端在请求头中添加 `Authorization: Bearer <token>`
2. `AuthInterceptor` 自动验证 Token
3. 验证成功后设置用户ID到 ThreadLocal

### 3. 权限验证

1. `PermissionInterceptor` 根据请求路径检查权限
2. 使用 `PermissionCheckService` 验证用户权限
3. 权限不足时返回 403 错误

## 测试接口

新增了测试控制器 `TestController`，提供以下测试接口：

- `GET /api/test/auth` - 测试认证功能
- `GET /api/test/permission` - 测试权限功能（需要 `test:permission` 权限）
- `GET /api/test/public` - 测试公开接口

## 注意事项

1. **Token 格式**：必须在请求头中使用 `Bearer <token>` 格式
2. **权限路径**：受保护的路径会自动进行权限验证
3. **ThreadLocal 清理**：认证拦截器会自动清理 ThreadLocal，避免内存泄漏
4. **错误处理**：认证失败返回 401，权限不足返回 403

## 扩展说明

如需添加新的权限检查逻辑，可以：

1. 在 `PermissionInterceptor` 中添加新的路径规则
2. 在 `PermissionCheckService` 中添加新的权限检查方法
3. 使用 `@RequiresPermission` 注解标记需要权限的方法

## 依赖要求

确保 `pom.xml` 中包含以下依赖：

```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-smallrye-jwt</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm-panache-kotlin</artifactId>
</dependency>
```

## 总结

现在 `infra-market-quarkus` 项目已经具备了完整的认证和权限控制功能，与 `infra-market-server` 保持一致。所有功能都遵循了项目的代码规范，使用 Kotlin 语言和 Quarkus 框架的最佳实践。
