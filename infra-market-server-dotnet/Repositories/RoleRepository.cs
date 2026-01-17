using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class RoleRepository(ApplicationDbContext context) : IRoleRepository
{
    public async Task<Role?> FindByCodeAsync(string code)
    {
        return await context.Roles
            .Where(r => r.Code == code && r.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<Role?> FindByIdAsync(ulong id)
    {
        return await context.Roles
            .Where(r => r.Id == id && r.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<List<Role>> FindByIDsAsync(List<ulong> ids)
    {
        if (ids.Count == 0) return [];
        return await context.Roles
            .Where(r => ids.Contains(r.Id) && r.Status != "deleted")
            .ToListAsync();
    }

    public async Task<(List<Role> roles, long total)> PageAsync(RoleQueryDto query)
    {
        var queryable = context.Roles.Where(r => r.Status != "deleted");

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(r => r.Name.Contains(query.Name));
        }

        if (!string.IsNullOrEmpty(query.Code))
        {
            queryable = queryable.Where(r => r.Code.Contains(query.Code));
        }

        if (!string.IsNullOrEmpty(query.Status))
        {
            queryable = queryable.Where(r => r.Status == query.Status);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var roles = await queryable
            .OrderBy(r => r.Id)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (roles, total);
    }

    public async Task CreateAsync(Role role)
    {
        context.Roles.Add(role);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Role role)
    {
        context.Roles.Update(role);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var role = await FindByIdAsync(id);
        if (role != null)
        {
            role.Status = "deleted";
            await UpdateAsync(role);
        }
    }

    public async Task<long> CountAsync()
    {
        return await context.Roles
            .Where(r => r.Status != "deleted")
            .CountAsync();
    }

    public async Task<long> CountBeforeDateAsync(long timestamp)
    {
        return await context.Roles
            .Where(r => r.Status != "deleted" && r.CreateTime <= timestamp)
            .CountAsync();
    }

    public async Task<List<Role>> FindByStatusAsync(string status)
    {
        return await context.Roles
            .Where(r => r.Status == status)
            .ToListAsync();
    }
}
