# 参数校验功能说明

## 概述
为用户、权限、角色等后端接口增加了完整的参数校验功能，确保数据的有效性和安全性。

## 技术实现

### 1. 依赖配置
在 `pom.xml` 中添加了 Spring Boot Validation 依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

### 2. 校验注解使用

#### 用户相关校验
- **UserFormDto**: 用户名、邮箱、手机号、密码、角色ID列表的格式和长度校验（创建用户时使用）
- **UserUpdateDto**: 用户名、邮箱、手机号、角色ID列表的格式和长度校验（更新用户时使用，密码字段可选）
- **UserQueryDto**: 查询参数的分页和状态校验
- **LoginRequest**: 登录用户名和密码的格式校验

#### 权限相关校验
- **PermissionFormDto**: 权限名称、编码、类型、路径、图标、排序值的格式和长度校验
- **PermissionQueryDto**: 查询参数的类型和状态校验

#### 角色相关校验
- **RoleFormDto**: 角色名称、编码、描述、权限ID列表的格式和长度校验
- **RoleQueryDto**: 查询参数的分页和状态校验

#### 其他校验
- **BatchRequest**: 批量操作ID列表的非空校验
- **ChangePasswordRequest**: 密码修改的长度和格式校验
- **StatusUpdateDto**: 状态更新值的格式校验

### 3. 校验规则

#### 字符串长度校验
- 用户名: 2-20字符
- 权限名称: 2-50字符
- 角色名称: 2-50字符
- 权限编码: 2-100字符
- 角色编码: 2-100字符
- 密码: 6-20字符

#### 格式校验
- 用户名: 只能包含字母、数字和下划线
- 权限编码: 只能包含字母、数字和冒号
- 角色编码: 只能包含字母、数字和下划线
- 邮箱: 标准邮箱格式
- 手机号: 中国大陆手机号格式 (1[3-9]xxxxxxxxx)
- 权限类型: 只能是 menu、button 或 api
- 状态值: 只能是 active 或 inactive

#### 数值校验
- 分页参数: 当前页和每页大小不能小于1，每页大小不能超过1000
- 排序值: 不能小于0
- ID列表: 非空

### 4. 控制器配置
在所有需要校验的接口参数前添加了 `@Valid` 注解：
- 创建和更新接口: `@Valid @RequestBody`
- 查询接口: `@Valid` 查询参数
- 批量操作接口: `@Valid @RequestBody`
- 状态更新接口: `@Valid @RequestBody`

### 5. 异常处理
创建了 `GlobalExceptionHandler` 全局异常处理器：
- 处理参数校验失败异常 (`MethodArgumentNotValidException`)
- 处理参数类型不匹配异常 (`MethodArgumentTypeMismatchException`)
- 处理运行时异常和其他异常
- 返回统一的错误响应格式

## 使用示例

### 创建用户
```bash
POST /users
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "phone": "13800138000",
  "password": "123456",
  "roleIds": [1, 2]
}
```

如果参数校验失败，会返回类似以下的错误响应：
```json
{
  "code": 400,
  "message": "参数校验失败",
  "data": "用户名长度必须在2-20个字符之间; 邮箱格式不正确"
}
```

### 查询权限
```bash
GET /permissions?name=test&type=menu&current=1&size=10
```

如果查询参数校验失败，会返回相应的错误信息。

## 注意事项

1. **校验顺序**: 校验在控制器方法执行前进行，确保无效数据不会进入业务逻辑
2. **错误信息**: 所有校验错误信息都使用中文，便于用户理解
3. **性能影响**: 校验开销很小，不会显著影响接口性能
4. **扩展性**: 可以轻松添加新的校验规则和自定义校验器

## 维护说明

如需添加新的校验规则：
1. 在相应的DTO类中添加校验注解
2. 在控制器中添加 `@Valid` 注解
3. 测试校验功能是否正常工作

如需修改校验规则：
1. 修改DTO类中的校验注解参数
2. 更新相关的错误提示信息
3. 确保前端也相应更新验证逻辑
