using InfraMarket.Server.Data;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class UserRoleRepository(ApplicationDbContext context) : IUserRoleRepository
{
    public async Task<List<UserRole>> FindByUidAsync(ulong uid)
    {
        return await context.UserRoles
            .Where(ur => ur.UserId == uid)
            .ToListAsync();
    }

    public async Task<List<UserRole>> FindByRoleIdAsync(ulong roleId)
    {
        return await context.UserRoles
            .Where(ur => ur.RoleId == roleId)
            .ToListAsync();
    }

    public async Task<List<UserRole>> FindByUiDsAsync(List<ulong> uids)
    {
        if (uids.Count == 0) return [];
        return await context.UserRoles
            .Where(ur => uids.Contains(ur.UserId))
            .ToListAsync();
    }

    public async Task DeleteByUidAsync(ulong uid)
    {
        var userRoles = await FindByUidAsync(uid);
        context.UserRoles.RemoveRange(userRoles);
        await context.SaveChangesAsync();
    }

    public async Task CreateAsync(UserRole userRole)
    {
        context.UserRoles.Add(userRole);
        await context.SaveChangesAsync();
    }

    public async Task<long> CountByRoleIdAsync(ulong roleId)
    {
        return await context.UserRoles
            .Where(ur => ur.RoleId == roleId)
            .CountAsync();
    }
}
