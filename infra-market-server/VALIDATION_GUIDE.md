# 校验注解使用指南

## 概述
本文档说明在 Infra Market 项目中如何正确使用 Jakarta Validation 注解进行参数校验。

## 校验注解分类

### 1. 字符串校验

#### @NotBlank
- **用途**: 验证字符串不为空且不只包含空白字符
- **适用类型**: `String`
- **示例**:
```kotlin
@field:NotBlank(message = "用户名不能为空")
val username: String
```

#### @Size
- **用途**: 验证字符串长度、集合大小或数组长度
- **适用类型**: `String`, `Collection`, `Array`
- **参数**: `min`, `max`
- **示例**:
```kotlin
@field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
val username: String

@field:Size(max = 100, message = "描述长度不能超过100个字符")
val description: String?
```

#### @Pattern
- **用途**: 使用正则表达式验证字符串格式
- **适用类型**: `String`
- **参数**: `regexp`
- **示例**:
```kotlin
@field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
val username: String

@field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
val phone: String?
```

#### @Email
- **用途**: 验证邮箱格式
- **适用类型**: `String`
- **示例**:
```kotlin
@field:Email(message = "邮箱格式不正确")
val email: String?
```

### 2. 数值校验

#### @Min
- **用途**: 验证数值的最小值
- **适用类型**: `Number` (Int, Long, Double等)
- **参数**: `value`
- **示例**:
```kotlin
@field:Min(value = 1, message = "当前页不能小于1")
val current: Int = 1

@field:Min(value = 0, message = "排序值不能小于0")
val sort: Int
```

#### @Max
- **用途**: 验证数值的最大值
- **适用类型**: `Number` (Int, Long, Double等)
- **参数**: `value`
- **示例**:
```kotlin
@field:Max(value = 100, message = "每页大小不能超过100")
val size: Int = 10
```

#### @NotNull
- **用途**: 验证对象不为null
- **适用类型**: 任何非基本类型
- **示例**:
```kotlin
@field:NotNull(message = "排序值不能为空")
val sort: Int
```

### 3. 集合校验

#### @NotEmpty
- **用途**: 验证集合不为空
- **适用类型**: `Collection`, `Array`, `Map`
- **示例**:
```kotlin
@field:NotEmpty(message = "角色ID列表不能为空")
val roleIds: List<Long>

@field:NotEmpty(message = "权限ID列表不能为空")
val permissionIds: List<Long>
```

## 常见错误和解决方案

### ❌ 错误用法

#### 1. 对数值类型使用 @Size
```kotlin
// 错误：@Size 不能用于数值类型
@field:Size(max = 100, message = "每页大小不能超过100")
val size: Int = 10
```

**解决方案**: 使用 `@Max` 注解
```kotlin
@field:Max(value = 100, message = "每页大小不能超过100")
val size: Int = 10
```

#### 2. 对可空类型使用 @NotBlank
```kotlin
// 错误：@NotBlank 不能用于可空类型
@field:NotBlank(message = "描述不能为空")
val description: String?
```

**解决方案**: 使用 `@NotBlank` 或 `@Size`
```kotlin
// 如果描述是必填的
@field:NotBlank(message = "描述不能为空")
val description: String

// 如果描述是可选的，但需要长度限制
@field:Size(max = 200, message = "描述长度不能超过200个字符")
val description: String?
```

#### 3. 对集合类型使用 @NotNull
```kotlin
// 错误：@NotNull 只检查是否为null，不检查是否为空
@field:NotNull(message = "角色ID列表不能为空")
val roleIds: List<Long>
```

**解决方案**: 使用 `@NotEmpty` 注解
```kotlin
@field:NotEmpty(message = "角色ID列表不能为空")
val roleIds: List<Long>
```

## 最佳实践

### 1. 注解选择原则
- **字符串**: 优先使用 `@NotBlank` + `@Size` + `@Pattern`
- **数值**: 使用 `@Min` + `@Max` + `@NotNull`
- **集合**: 使用 `@NotEmpty` 或 `@Size`
- **邮箱**: 使用 `@Email`
- **手机号**: 使用 `@Pattern` + 正则表达式

### 2. 错误信息规范
- 使用中文错误信息
- 信息要具体明确
- 包含具体的限制条件

### 3. 校验顺序
- 先检查非空性 (`@NotBlank`, `@NotNull`, `@NotEmpty`)
- 再检查格式和长度 (`@Pattern`, `@Size`, `@Email`)
- 最后检查数值范围 (`@Min`, `@Max`)

## 示例代码

### 完整的用户DTO示例
```kotlin
data class UserFormDto(
    @field:NotBlank(message = "用户名不能为空")
    @field:Size(min = 2, max = 20, message = "用户名长度必须在2-20个字符之间")
    @field:Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    val username: String,
    
    @field:Email(message = "邮箱格式不正确")
    val email: String?,
    
    @field:Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    val phone: String?,
    
    @field:Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    val password: String?,
    
    @field:NotEmpty(message = "角色ID列表不能为空")
    val roleIds: List<Long>
)
```

### 完整的查询DTO示例
```kotlin
data class UserQueryDto(
    @field:Size(max = 50, message = "用户名长度不能超过50个字符")
    val username: String? = null,
    
    @field:Pattern(regexp = "^(active|inactive)$", message = "状态只能是active或inactive")
    val status: String? = null,
    
    @field:Min(value = 1, message = "当前页不能小于1")
    val current: Int = 1,
    
    @field:Min(value = 1, message = "每页大小不能小于1")
    @field:Max(value = 1000, message = "每页大小不能超过1000")
    val size: Int = 10
)
```

## 注意事项

1. **类型匹配**: 确保校验注解与字段类型匹配
2. **导入语句**: 记得导入相应的校验注解
3. **测试验证**: 添加校验后要测试各种边界情况
4. **错误处理**: 确保全局异常处理器能正确处理校验失败
5. **性能考虑**: 校验注解的性能开销很小，可以放心使用

## 常见问题

### Q: 为什么我的校验注解不生效？
A: 检查以下几点：
- 是否在控制器参数前添加了 `@Valid` 注解
- 是否添加了 `spring-boot-starter-validation` 依赖
- 校验注解是否与字段类型匹配

### Q: 如何自定义校验规则？
A: 可以创建自定义校验注解和校验器，或者使用 `@Pattern` 配合正则表达式

### Q: 校验失败的错误信息在哪里配置？
A: 错误信息在 `@field:注解(message = "错误信息")` 的 `message` 参数中配置
