using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class AuthService(
    IUserRepository userRepository,
    IUserRoleRepository userRoleRepository,
    IRolePermissionRepository rolePermissionRepository,
    IPermissionRepository permissionRepository,
    ITokenService tokenService) : IAuthService
{
    public async Task<ApiData<LoginResponse>> LoginAsync(LoginRequest request)
    {
        var user = await userRepository.FindByUsernameAsync(request.Username);
        if (user == null || !AesUtil.Matches(request.Password, user.Password))
        {
            return ApiData<LoginResponse>.Error("用户名或密码错误", 400);
        }

        if (user.Status != "active")
        {
            return ApiData<LoginResponse>.Error("用户已被禁用", 400);
        }

        // 更新登录时间
        user.LastLoginTime = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();
        await userRepository.UpdateAsync(user);

        // 获取用户权限
        var permissions = await GetUserPermissionsAsync(user.Id);

        // 生成JWT token
        var token = tokenService.GenerateToken(user.Id);

        // 获取用户角色
        var userRoles = await userRoleRepository.FindByUidAsync(user.Id);
        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();

        var userDto = ConvertUserToDto(user, roleIds);
        var response = new LoginResponse
        {
            Token = token,
            User = userDto,
            Permissions = permissions.Select(p => p.Code).ToList()
        };

        return ApiData<LoginResponse>.Success(response);
    }

    public async Task<ApiData<LoginResponse>> GetCurrentUserAsync(ulong uid)
    {
        var user = await userRepository.FindByUidAsync(uid);
        if (user == null)
        {
            return ApiData<LoginResponse>.Error("用户不存在", 404);
        }

        if (user.Status != "active")
        {
            return ApiData<LoginResponse>.Error("用户已被禁用", 400);
        }

        var userRoles = await userRoleRepository.FindByUidAsync(uid);
        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();
        var permissions = await GetUserPermissionsAsync(uid);

        var userDto = ConvertUserToDto(user, roleIds);
        var response = new LoginResponse
        {
            Token = string.Empty,
            User = userDto,
            Permissions = permissions.Select(p => p.Code).ToList()
        };

        return ApiData<LoginResponse>.Success(response);
    }

    public async Task<ApiData<List<PermissionDto>>> GetUserMenusAsync(ulong uid)
    {
        var user = await userRepository.FindByUidAsync(uid);
        if (user == null)
        {
            return ApiData<List<PermissionDto>>.Error("用户不存在", 404);
        }

        if (user.Status != "active")
        {
            return ApiData<List<PermissionDto>>.Error("用户已被禁用", 400);
        }

        // 获取用户的所有权限（包括按钮权限）
        var allUserPermissions = await GetUserPermissionListAsync(uid);

        if (allUserPermissions.Count == 0)
        {
            return ApiData<List<PermissionDto>>.Success([]);
        }

        // 收集所有需要查询的父级权限ID
        var parentIdsToQuery = new HashSet<ulong>();
        foreach (var permission in allUserPermissions)
        {
            if (permission.ParentId.HasValue)
            {
                parentIdsToQuery.Add(permission.ParentId.Value);
            }
        }

        // 递归收集所有父级权限ID
        var allParentIds = await CollectAllParentIdsAsync(parentIdsToQuery);

        // 批量获取所有父级权限
        var allParentPermissions = new List<Permission>();
        if (allParentIds.Count > 0)
        {
            allParentPermissions = await permissionRepository.FindByIDsAsync(allParentIds.ToList());
        }

        // 构建权限ID到权限对象的映射
        var permissionMap = allParentPermissions.ToDictionary(p => p.Id);

        // 收集所有需要的菜单权限ID（包括父级链）
        var allMenuPermissionIds = new HashSet<ulong>();

        // 从按钮权限向上收集父级菜单
        foreach (var permission in allUserPermissions)
        {
            if (permission.Type != "button" || !permission.ParentId.HasValue) continue;
            var currentParentId = permission.ParentId;
            while (currentParentId.HasValue)
            {
                allMenuPermissionIds.Add(currentParentId.Value);
                // 从 permissionMap 中查找父级权限
                if (permissionMap.TryGetValue(currentParentId.Value, out var parent) && parent.ParentId.HasValue)
                {
                    currentParentId = parent.ParentId;
                }
                else
                {
                    break;
                }
            }
        }

        // 从菜单权限向上收集父级菜单
        foreach (var permission in allUserPermissions)
        {
            if (permission is not { Type: "menu", ParentId: not null }) continue;
            // 向上收集父级菜单
            var currentParentId = permission.ParentId;
            while (currentParentId.HasValue)
            {
                allMenuPermissionIds.Add(currentParentId.Value);
                // 从 permissionMap 中查找父级权限
                if (permissionMap.TryGetValue(currentParentId.Value, out var parent) && parent.ParentId.HasValue)
                {
                    currentParentId = parent.ParentId;
                }
                else
                {
                    break;
                }
            }
        }

        // 合并所有需要的权限ID（包括所有父级权限和用户直接拥有的权限）
        foreach (var id in allParentIds)
        {
            allMenuPermissionIds.Add(id);
        }
        foreach (var permission in allUserPermissions)
        {
            allMenuPermissionIds.Add(permission.Id);
        }

        // 批量获取所有需要的权限详情
        var allPermissions = await permissionRepository.FindByIDsAsync(allMenuPermissionIds.ToList());

        // 只保留菜单类型的权限
        var menuPermissions = allPermissions.Where(p => p.Type == "menu").ToList();

        // 构建权限树
        var tree = BuildPermissionTree(menuPermissions);

        return ApiData<List<PermissionDto>>.Success(tree);
    }

    public async Task<ApiData<LoginResponse>> RefreshTokenAsync(ulong uid)
    {
        var user = await userRepository.FindByUidAsync(uid);
        if (user == null)
        {
            return ApiData<LoginResponse>.Error("用户不存在", 404);
        }

        var token = tokenService.GenerateToken(uid);
        var userRoles = await userRoleRepository.FindByUidAsync(uid);
        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();
        var permissions = await GetUserPermissionsAsync(uid);

        var userDto = ConvertUserToDto(user, roleIds);
        var response = new LoginResponse
        {
            Token = token,
            User = userDto,
            Permissions = permissions.Select(p => p.Code).ToList()
        };

        return ApiData<LoginResponse>.Success(response);
    }

    public Task<ApiData<object>> LogoutAsync(ulong uid)
    {
        // Token删除在TokenService中处理
        return Task.FromResult(ApiData<object>.Success(null!));
    }

    public async Task<ApiData<object>> ChangePasswordAsync(ulong uid, ChangePasswordRequest request)
    {
        var user = await userRepository.FindByUidAsync(uid);
        if (user == null)
        {
            return ApiData<object>.Error("用户不存在", 404);
        }

        if (!AesUtil.Matches(request.OldPassword, user.Password))
        {
            return ApiData<object>.Error("原密码错误", 400);
        }

        user.Password = AesUtil.Encrypt(request.NewPassword);
        await userRepository.UpdateAsync(user);

        return ApiData<object>.Success(null!);
    }

    private async Task<List<Permission>> GetUserPermissionsAsync(ulong userId)
    {
        var userRoles = await userRoleRepository.FindByUidAsync(userId);
        if (userRoles.Count == 0) return [];

        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();
        var rolePermissions = await rolePermissionRepository.FindByRoleIDsAsync(roleIds);
        var permissionIds = rolePermissions.Select(rp => rp.PermissionId).Distinct().ToList();

        if (permissionIds.Count == 0) return [];

        var permissions = await permissionRepository.FindByIDsAsync(permissionIds);
        return permissions.Where(p => p.Status == "active").ToList();
    }

    private async Task<List<Permission>> GetUserPermissionListAsync(ulong userId)
    {
        var userRoles = await userRoleRepository.FindByUidAsync(userId);
        if (userRoles.Count == 0) return [];

        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();
        var rolePermissions = await rolePermissionRepository.FindByRoleIDsAsync(roleIds);
        var permissionIds = rolePermissions.Select(rp => rp.PermissionId).Distinct().ToList();

        if (permissionIds.Count == 0) return [];

        var permissions = await permissionRepository.FindByIDsAsync(permissionIds);
        return permissions;
    }

    private async Task<HashSet<ulong>> CollectAllParentIdsAsync(HashSet<ulong> parentIds)
    {
        var allParentIds = new HashSet<ulong>(parentIds);

        if (parentIds.Count == 0)
        {
            return allParentIds;
        }

        var parentPermissions = await permissionRepository.FindByIDsAsync(parentIds.ToList());

        // 收集这些父级权限的父级ID
        var nextLevelParentIds = new HashSet<ulong>();
        foreach (var p in parentPermissions)
        {
            if (p.ParentId.HasValue && !allParentIds.Contains(p.ParentId.Value))
            {
                nextLevelParentIds.Add(p.ParentId.Value);
            }
        }

        // 如果有新的父级ID，递归收集
        if (nextLevelParentIds.Count <= 0) return allParentIds;
        var recursiveParents = await CollectAllParentIdsAsync(nextLevelParentIds);
        foreach (var id in recursiveParents)
        {
            allParentIds.Add(id);
        }

        return allParentIds;
    }

    private List<PermissionDto> BuildPermissionTree(List<Permission> permissions)
    {
        // 先转换为DTO
        var permissionMap = permissions.ToDictionary(
            p => p.Id,
            ConvertPermissionToDto
        );

        // 构建树形结构
        var roots = (from p in permissions where p.ParentId == null select BuildPermissionWithChildren(permissionMap[p.Id], permissionMap)).ToList();

        // 按sort字段排序根节点
        roots = roots.OrderBy(r => r.Sort).ToList();

        return roots;
    }

    private static PermissionDto BuildPermissionWithChildren(PermissionDto permission, Dictionary<ulong, PermissionDto> permissionMap)
    {
        var children = new List<PermissionDto>();

        // 查找所有以当前权限为父级的权限
        foreach (var childPermission in permissionMap.Values)
        {
            if (!childPermission.ParentId.HasValue || childPermission.ParentId.Value != permission.Id) continue;
            // 递归构建子权限
            var childWithChildren = BuildPermissionWithChildren(childPermission, permissionMap);
            children.Add(childWithChildren);
        }

        // 按sort字段排序子权限
        children = children.OrderBy(c => c.Sort).ToList();

        // 返回带有子权限的新PermissionDto
        permission.Children = children.Count > 0 ? children : null;
        return permission;
    }

    private static UserDto ConvertUserToDto(User user, List<ulong> roleIds)
    {
        return new UserDto
        {
            Id = user.Id,
            Username = user.Username,
            Email = user.Email,
            Phone = user.Phone,
            Status = user.Status,
            LastLoginTime = DateTimeUtil.FormatLocalDateTime(user.LastLoginTime),
            RoleIds = roleIds,
            CreateTime = DateTimeUtil.FormatLocalDateTime(user.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(user.UpdateTime)
        };
    }

    private static PermissionDto ConvertPermissionToDto(Permission permission)
    {
        return new PermissionDto
        {
            Id = permission.Id,
            Name = permission.Name,
            Code = permission.Code,
            Type = permission.Type,
            ParentId = permission.ParentId,
            Path = permission.Path,
            Icon = permission.Icon,
            Sort = permission.Sort,
            Status = permission.Status,
            CreateTime = DateTimeUtil.FormatLocalDateTime(permission.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(permission.UpdateTime)
        };
    }
}
