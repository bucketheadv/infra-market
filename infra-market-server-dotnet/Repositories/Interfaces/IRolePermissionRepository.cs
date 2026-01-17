using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IRolePermissionRepository
{
    Task<List<RolePermission>> FindByRoleIdAsync(ulong roleId);
    Task<List<RolePermission>> FindByRoleIDsAsync(List<ulong> roleIds);
    Task DeleteByRoleIdAsync(ulong roleId);
    Task CreateAsync(RolePermission rolePermission);
    Task<long> CountByPermissionIdAsync(ulong permissionId);
}
