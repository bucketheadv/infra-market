using InfraMarket.Server.Data;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class RolePermissionRepository(ApplicationDbContext context) : IRolePermissionRepository
{
    public async Task<List<RolePermission>> FindByRoleIdAsync(ulong roleId)
    {
        return await context.RolePermissions
            .Where(rp => rp.RoleId == roleId)
            .ToListAsync();
    }

    public async Task<List<RolePermission>> FindByRoleIDsAsync(List<ulong> roleIds)
    {
        if (roleIds.Count == 0) return [];
        return await context.RolePermissions
            .Where(rp => roleIds.Contains(rp.RoleId))
            .ToListAsync();
    }

    public async Task DeleteByRoleIdAsync(ulong roleId)
    {
        var rolePermissions = await FindByRoleIdAsync(roleId);
        context.RolePermissions.RemoveRange(rolePermissions);
        await context.SaveChangesAsync();
    }

    public async Task CreateAsync(RolePermission rolePermission)
    {
        context.RolePermissions.Add(rolePermission);
        await context.SaveChangesAsync();
    }

    public async Task<long> CountByPermissionIdAsync(ulong permissionId)
    {
        return await context.RolePermissions
            .Where(rp => rp.PermissionId == permissionId)
            .CountAsync();
    }
}
