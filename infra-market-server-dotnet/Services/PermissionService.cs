using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class PermissionService(
    IPermissionRepository permissionRepository) : IPermissionService
{
    public async Task<ApiData<PageResult<PermissionDto>>> GetPermissionsAsync(PermissionQueryDto query)
    {
        var (permissions, total) = await permissionRepository.PageAsync(query);

        // 收集所有需要查询的权限ID（包括当前权限和它们的祖先权限）
        var allPermissionIdSet = new HashSet<ulong>();
        foreach (var p in permissions)
        {
            allPermissionIdSet.Add(p.Id);
        }

        // 递归收集所有祖先权限ID
        var parentIdsToQuery = new List<ulong>();
        foreach (var p in permissions)
        {
            if (!p.ParentId.HasValue || allPermissionIdSet.Contains(p.ParentId.Value)) continue;
            parentIdsToQuery.Add(p.ParentId.Value);
            allPermissionIdSet.Add(p.ParentId.Value);
        }

        // 迭代查询所有祖先权限
        var allQueriedPermissions = new List<Permission>();
        while (parentIdsToQuery.Count > 0)
        {
            var parentPermissions = await permissionRepository.FindByIDsAsync(parentIdsToQuery);
            allQueriedPermissions.AddRange(parentPermissions);

            // 收集下一层父权限ID
            var nextLevelParentIds = new List<ulong>();
            foreach (var p in parentPermissions)
            {
                if (!p.ParentId.HasValue || allPermissionIdSet.Contains(p.ParentId.Value)) continue;
                nextLevelParentIds.Add(p.ParentId.Value);
                allPermissionIdSet.Add(p.ParentId.Value);
            }
            parentIdsToQuery = nextLevelParentIds;
        }

        // 构建完整的权限Map
        var allPermissionMap = permissions.ToDictionary(p => p.Id);
        foreach (var p in allQueriedPermissions)
        {
            allPermissionMap[p.Id] = p;
        }

        var parentPermissionMap = new Dictionary<ulong, Permission>();
        foreach (var p in permissions)
        {
            if (!p.ParentId.HasValue || !allPermissionMap.ContainsKey(p.ParentId.Value)) continue;
            var parentId = p.ParentId.Value;
            if (!parentPermissionMap.ContainsKey(parentId) && allPermissionMap.TryGetValue(parentId, out var parentPerm))
            {
                parentPermissionMap[parentId] = parentPerm;
            }
        }

        // 转换权限列表
        var permissionDtos = permissions.Select(p =>
        {
            var parent = p.ParentId.HasValue && parentPermissionMap.TryGetValue(p.ParentId.Value, out var parentPerm)
                ? new ParentPermissionDto
                {
                    Id = parentPerm.Id,
                    Name = parentPerm.Name,
                    Code = parentPerm.Code
                }
                : null;

            var accessPath = BuildAccessPath(p, allPermissionMap);

            return new PermissionDto
            {
                Id = p.Id,
                Name = p.Name,
                Code = p.Code,
                Type = p.Type,
                ParentId = p.ParentId,
                Path = p.Path,
                Icon = p.Icon,
                Sort = p.Sort,
                Status = p.Status,
                ParentPermission = parent,
                AccessPath = accessPath,
                CreateTime = DateTimeUtil.FormatLocalDateTime(p.CreateTime),
                UpdateTime = DateTimeUtil.FormatLocalDateTime(p.UpdateTime)
            };
        }).ToList();

        var result = new PageResult<PermissionDto>
        {
            Records = permissionDtos,
            Total = total,
            Page = query.Page ?? 1,
            Size = query.Size ?? 10
        };

        return ApiData<PageResult<PermissionDto>>.Success(result);
    }

    public async Task<ApiData<List<PermissionDto>>> GetPermissionTreeAsync()
    {
        var permissions = await permissionRepository.FindAllAsync();
        var activePermissions = permissions.Where(p => p.Status == "active").ToList();
        var tree = BuildPermissionTree(activePermissions);
        return ApiData<List<PermissionDto>>.Success(tree);
    }

    public async Task<ApiData<PermissionDto>> GetPermissionAsync(ulong id)
    {
        var permission = await permissionRepository.FindByIdAsync(id);
        if (permission == null)
        {
            return ApiData<PermissionDto>.Error("权限不存在", 404);
        }

        var permissionDto = await ConvertPermissionToDtoWithParentAsync(permission);
        return ApiData<PermissionDto>.Success(permissionDto);
    }

    public async Task<ApiData<PermissionDto>> CreatePermissionAsync(PermissionFormDto dto)
    {
        var existingPermission = await permissionRepository.FindByCodeAsync(dto.Code);
        if (existingPermission != null)
        {
            return ApiData<PermissionDto>.Error("权限编码已存在", 400);
        }

        var permission = new Permission
        {
            Name = dto.Name,
            Code = dto.Code,
            Type = dto.Type,
            ParentId = dto.ParentId,
            Path = dto.Path,
            Icon = dto.Icon,
            Sort = dto.Sort,
            Status = "active"
        };

        await permissionRepository.CreateAsync(permission);
        var permissionDto = ConvertPermissionToDto(permission, null);
        return ApiData<PermissionDto>.Success(permissionDto);
    }

    public async Task<ApiData<PermissionDto>> UpdatePermissionAsync(ulong id, PermissionFormDto dto)
    {
        var permission = await permissionRepository.FindByIdAsync(id);
        if (permission == null)
        {
            return ApiData<PermissionDto>.Error("权限不存在", 404);
        }

        var existingPermission = await permissionRepository.FindByCodeAsync(dto.Code);
        if (existingPermission != null && existingPermission.Id != permission.Id)
        {
            return ApiData<PermissionDto>.Error("权限编码已存在", 400);
        }

        permission.Name = dto.Name;
        permission.Type = dto.Type;
        permission.ParentId = dto.ParentId;
        permission.Path = dto.Path;
        permission.Icon = dto.Icon;
        permission.Sort = dto.Sort;

        await permissionRepository.UpdateAsync(permission);
        var permissionDto = ConvertPermissionToDto(permission, null);
        return ApiData<PermissionDto>.Success(permissionDto);
    }

    public async Task<ApiData<object>> DeletePermissionAsync(ulong id)
    {
        var permission = await permissionRepository.FindByIdAsync(id);
        if (permission == null)
        {
            return ApiData<object>.Error("权限不存在", 404);
        }

        if (permission.Code == "system")
        {
            return ApiData<object>.Error("不能删除系统权限", 400);
        }

        var childPermissions = await permissionRepository.FindByParentIdAsync(id);
        if (childPermissions.Count > 0)
        {
            return ApiData<object>.Error("该权限下还有子权限，无法删除", 400);
        }

        permission.Status = "deleted";
        await permissionRepository.UpdateAsync(permission);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> UpdatePermissionStatusAsync(ulong id, StatusUpdateDto dto)
    {
        var permission = await permissionRepository.FindByIdAsync(id);
        if (permission == null)
        {
            return ApiData<object>.Error("权限不存在", 404);
        }

        if (permission.Code == "system" && dto.Status == "deleted")
        {
            return ApiData<object>.Error("不能删除系统权限", 400);
        }

        if (permission.Status == "deleted" && dto.Status != "deleted")
        {
            return ApiData<object>.Error("已删除的权限不能重新启用", 400);
        }

        if (dto.Status == "deleted")
        {
            return await DeletePermissionAsync(id);
        }

        permission.Status = dto.Status;
        await permissionRepository.UpdateAsync(permission);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> BatchDeletePermissionsAsync(BatchRequest request)
    {
        foreach (var id in request.Ids)
        {
            await DeletePermissionAsync(id);
        }

        return ApiData<object>.Success(null!);
    }

    private List<PermissionDto> BuildPermissionTree(List<Permission> permissions)
    {
        var permissionMap = permissions.ToDictionary(
            p => p.Id,
            p => ConvertPermissionToDto(p, null)
        );

        var roots = (from p in permissions where p.ParentId == null select BuildPermissionWithChildren(permissionMap[p.Id], permissionMap)).ToList();

        return roots.OrderBy(r => r.Sort).ToList();
    }

    private static PermissionDto BuildPermissionWithChildren(PermissionDto permission, Dictionary<ulong, PermissionDto> permissionMap)
    {
        var children = new List<PermissionDto>();
        foreach (var childPermission in permissionMap.Values)
        {
            if (!childPermission.ParentId.HasValue || childPermission.ParentId.Value != permission.Id) continue;
            var childWithChildren = BuildPermissionWithChildren(childPermission, permissionMap);
            children.Add(childWithChildren);
        }

        permission.Children = children.Count > 0 ? children.OrderBy(c => c.Sort).ToList() : null;
        return permission;
    }

    private static PermissionDto ConvertPermissionToDto(Permission permission, List<PermissionDto>? children)
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
            Children = children is { Count: > 0 } ? children : null,
            CreateTime = DateTimeUtil.FormatLocalDateTime(permission.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(permission.UpdateTime)
        };
    }

    private async Task<PermissionDto> ConvertPermissionToDtoWithParentAsync(Permission permission)
    {
        var permissionDto = ConvertPermissionToDto(permission, null);

        if (permission.ParentId.HasValue)
        {
            var parentPermission = await permissionRepository.FindByIdAsync(permission.ParentId.Value);
            if (parentPermission != null)
            {
                permissionDto.ParentPermission = new ParentPermissionDto
                {
                    Id = parentPermission.Id,
                    Name = parentPermission.Name,
                    Code = parentPermission.Code
                };
            }
        }

        var allPermissions = await permissionRepository.FindAllAsync();
        var allPermissionMap = allPermissions.ToDictionary(p => p.Id);
        permissionDto.AccessPath = BuildAccessPath(permission, allPermissionMap);

        return permissionDto;
    }

    private static List<string> BuildAccessPath(Permission permission, Dictionary<ulong, Permission> allPermissionMap)
    {
        var path = new List<string>();
        var current = permission;

        while (true)
        {
            path.Insert(0, current.Name);
            if (current.ParentId.HasValue && allPermissionMap.TryGetValue(current.ParentId.Value, out var parent))
            {
                current = parent;
            }
            else
            {
                break;
            }
        }

        return path;
    }
}
