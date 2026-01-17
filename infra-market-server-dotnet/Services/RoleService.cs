using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class RoleService(
    ApplicationDbContext context,
    IRoleRepository roleRepository,
    IRolePermissionRepository rolePermissionRepository,
    IUserRoleRepository userRoleRepository) : IRoleService
{
    public async Task<ApiData<PageResult<RoleDto>>> GetRolesAsync(RoleQueryDto query)
    {
        var (roles, total) = await roleRepository.PageAsync(query);
        var roleIds = roles.Select(r => r.Id).ToList();
        var rolePermissions = await rolePermissionRepository.FindByRoleIDsAsync(roleIds);
        var rolePermissionMap = rolePermissions.GroupBy(rp => rp.RoleId)
            .ToDictionary(g => g.Key, g => g.Select(rp => rp.PermissionId).ToList());

        var roleDtos = roles.Select(role =>
        {
            var permissionIds = rolePermissionMap.GetValueOrDefault(role.Id, []);
            return ConvertRoleToDto(role, permissionIds);
        }).ToList();

        var result = new PageResult<RoleDto>
        {
            Records = roleDtos,
            Total = total,
            Page = query.Page ?? 1,
            Size = query.Size ?? 10
        };

        return ApiData<PageResult<RoleDto>>.Success(result);
    }

    public async Task<ApiData<List<RoleDto>>> GetAllRolesAsync()
    {
        var roles = await roleRepository.FindByStatusAsync("active");
        var roleIds = roles.Select(r => r.Id).ToList();
        var rolePermissions = await rolePermissionRepository.FindByRoleIDsAsync(roleIds);
        var rolePermissionMap = rolePermissions.GroupBy(rp => rp.RoleId)
            .ToDictionary(g => g.Key, g => g.Select(rp => rp.PermissionId).ToList());

        var roleDtos = roles.Select(role =>
        {
            var permissionIds = rolePermissionMap.GetValueOrDefault(role.Id, []);
            return ConvertRoleToDto(role, permissionIds);
        }).ToList();

        return ApiData<List<RoleDto>>.Success(roleDtos);
    }

    public async Task<ApiData<RoleDto>> GetRoleAsync(ulong id)
    {
        var role = await roleRepository.FindByIdAsync(id);
        if (role == null)
        {
            return ApiData<RoleDto>.Error("角色不存在", 404);
        }

        var rolePermissions = await rolePermissionRepository.FindByRoleIdAsync(id);
        var permissionIds = rolePermissions.Select(rp => rp.PermissionId).ToList();
        var roleDto = ConvertRoleToDto(role, permissionIds);

        return ApiData<RoleDto>.Success(roleDto);
    }

    public async Task<ApiData<RoleDto>> CreateRoleAsync(RoleFormDto dto)
    {
        var existingRole = await roleRepository.FindByCodeAsync(dto.Code);
        if (existingRole != null)
        {
            return ApiData<RoleDto>.Error("角色编码已存在", 400);
        }

        var role = new Role
        {
            Name = dto.Name,
            Code = dto.Code,
            Description = dto.Description,
            Status = "active"
        };

        await using var transaction = await context.Database.BeginTransactionAsync();
        try
        {
            await roleRepository.CreateAsync(role);

            foreach (var permissionId in dto.PermissionIds)
            {
                await rolePermissionRepository.CreateAsync(new RolePermission
                {
                    RoleId = role.Id,
                    PermissionId = permissionId
                });
            }

            await transaction.CommitAsync();
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }

        var roleDto = ConvertRoleToDto(role, dto.PermissionIds);
        return ApiData<RoleDto>.Success(roleDto);
    }

    public async Task<ApiData<RoleDto>> UpdateRoleAsync(ulong id, RoleFormDto dto)
    {
        var role = await roleRepository.FindByIdAsync(id);
        if (role == null)
        {
            return ApiData<RoleDto>.Error("角色不存在", 404);
        }

        var existingRole = await roleRepository.FindByCodeAsync(dto.Code);
        if (existingRole != null && existingRole.Id != role.Id)
        {
            return ApiData<RoleDto>.Error("角色编码已存在", 400);
        }

        role.Name = dto.Name;
        role.Description = dto.Description;

        await using var transaction = await context.Database.BeginTransactionAsync();
        try
        {
            await roleRepository.UpdateAsync(role);
            await rolePermissionRepository.DeleteByRoleIdAsync(id);

            foreach (var permissionId in dto.PermissionIds)
            {
                await rolePermissionRepository.CreateAsync(new RolePermission
                {
                    RoleId = id,
                    PermissionId = permissionId
                });
            }

            await transaction.CommitAsync();
        }
        catch
        {
            await transaction.RollbackAsync();
            throw;
        }

        var roleDto = ConvertRoleToDto(role, dto.PermissionIds);
        return ApiData<RoleDto>.Success(roleDto);
    }

    public async Task<ApiData<object>> DeleteRoleAsync(ulong id)
    {
        var role = await roleRepository.FindByIdAsync(id);
        if (role == null)
        {
            return ApiData<object>.Error("角色不存在", 404);
        }

        if (role.Code == "admin")
        {
            return ApiData<object>.Error("不能删除系统角色", 400);
        }

        var userCount = await userRoleRepository.CountByRoleIdAsync(id);
        if (userCount > 0)
        {
            return ApiData<object>.Error("该角色下还有用户，无法删除", 400);
        }

        role.Status = "deleted";
        await roleRepository.UpdateAsync(role);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> UpdateRoleStatusAsync(ulong id, StatusUpdateDto dto)
    {
        var role = await roleRepository.FindByIdAsync(id);
        if (role == null)
        {
            return ApiData<object>.Error("角色不存在", 404);
        }

        if (role.Code == "admin" && dto.Status == "deleted")
        {
            return ApiData<object>.Error("不能删除系统角色", 400);
        }

        if (role.Status == "deleted" && dto.Status != "deleted")
        {
            return ApiData<object>.Error("已删除的角色不能重新启用", 400);
        }

        if (dto.Status == "deleted")
        {
            return await DeleteRoleAsync(id);
        }

        role.Status = dto.Status;
        await roleRepository.UpdateAsync(role);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> BatchDeleteRolesAsync(BatchRequest request)
    {
        foreach (var id in request.Ids)
        {
            await DeleteRoleAsync(id);
        }

        return ApiData<object>.Success(null!);
    }

    private static RoleDto ConvertRoleToDto(Role role, List<ulong> permissionIds)
    {
        return new RoleDto
        {
            Id = role.Id,
            Name = role.Name,
            Code = role.Code,
            Description = role.Description,
            Status = role.Status,
            PermissionIds = permissionIds,
            CreateTime = DateTimeUtil.FormatLocalDateTime(role.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(role.UpdateTime)
        };
    }
}
