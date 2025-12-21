# 事务需求分析

## 需要加事务的操作

### 1. UserService.CreateUser
**位置**: `internal/service/user_service.go:83`
**原因**: 创建用户 + 创建多个用户角色关联
**操作**:
- `userRepo.Create(user)` - 创建用户
- 循环 `userRoleRepo.Create(userRole)` - 创建多个用户角色关联

**风险**: 如果创建用户成功但创建角色关联失败，会导致数据不一致

---

### 2. UserService.UpdateUser
**位置**: `internal/service/user_service.go:141`
**原因**: 更新用户 + 删除旧角色关联 + 创建新角色关联
**操作**:
- `userRepo.Update(user)` - 更新用户
- `userRoleRepo.DeleteByUID(id)` - 删除旧角色关联
- 循环 `userRoleRepo.Create(userRole)` - 创建新角色关联

**风险**: 如果中间任何一步失败，会导致用户和角色关联数据不一致

---

### 3. UserService.BatchDeleteUsers
**位置**: `internal/service/user_service.go:274`
**原因**: 批量删除用户（循环中多次更新）
**操作**:
- 循环 `userRepo.Update(&user)` - 批量更新用户状态为 deleted

**风险**: 如果部分用户删除成功，部分失败，会导致数据不一致

---

### 4. RoleService.CreateRole
**位置**: `internal/service/role_service.go:111`
**原因**: 创建角色 + 创建多个角色权限关联
**操作**:
- `roleRepo.Create(role)` - 创建角色
- 循环 `rolePermissionRepo.Create(rolePermission)` - 创建多个角色权限关联

**风险**: 如果创建角色成功但创建权限关联失败，会导致数据不一致

---

### 5. RoleService.UpdateRole
**位置**: `internal/service/role_service.go:144`
**原因**: 更新角色 + 删除旧权限关联 + 创建新权限关联
**操作**:
- `roleRepo.Update(role)` - 更新角色
- `rolePermissionRepo.DeleteByRoleID(id)` - 删除旧权限关联
- 循环 `rolePermissionRepo.Create(rolePermission)` - 创建新权限关联

**风险**: 如果中间任何一步失败，会导致角色和权限关联数据不一致

---

## 实现方案

### 方案1: 在 Service 层添加 db 字段（推荐）

1. 修改 Service 结构体，添加 `db *gorm.DB` 字段
2. 在需要事务的方法中使用 `WithTransaction`
3. 在事务中创建新的 repository 实例（使用事务 db）

### 方案2: 修改 Repository 方法支持事务 db

1. 修改 Repository 方法，接受可选的 `*gorm.DB` 参数
2. 如果提供事务 db，使用事务 db；否则使用默认 db

### 方案3: 在 Repository 层添加事务支持方法

1. 为每个 Repository 添加 `WithTx(*gorm.DB)` 方法
2. 返回使用事务 db 的新实例

---

## 建议

优先使用**方案1**，因为：
- 改动最小
- 事务逻辑集中在 Service 层
- 符合业务逻辑分层原则
