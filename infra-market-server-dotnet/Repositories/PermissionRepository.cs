using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class PermissionRepository(ApplicationDbContext context) : IPermissionRepository
{
    public async Task<Permission?> FindByCodeAsync(string code)
    {
        return await context.Permissions
            .Where(p => p.Code == code && p.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<Permission?> FindByIdAsync(ulong id)
    {
        return await context.Permissions
            .Where(p => p.Id == id && p.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<List<Permission>> FindByIDsAsync(List<ulong> ids)
    {
        if (ids.Count == 0) return [];
        return await context.Permissions
            .Where(p => ids.Contains(p.Id) && p.Status != "deleted")
            .ToListAsync();
    }

    public async Task<List<Permission>> FindByParentIdAsync(ulong parentId)
    {
        return await context.Permissions
            .Where(p => p.ParentId == parentId && p.Status != "deleted")
            .ToListAsync();
    }

    public async Task<List<Permission>> FindAllAsync()
    {
        return await context.Permissions
            .Where(p => p.Status != "deleted")
            .OrderBy(p => p.Sort)
            .ThenBy(p => p.Id)
            .ToListAsync();
    }

    public async Task<(List<Permission> permissions, long total)> PageAsync(PermissionQueryDto query)
    {
        var queryable = context.Permissions.Where(p => p.Status != "deleted");

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(p => p.Name.Contains(query.Name));
        }

        if (!string.IsNullOrEmpty(query.Code))
        {
            queryable = queryable.Where(p => p.Code.Contains(query.Code));
        }

        if (!string.IsNullOrEmpty(query.Type))
        {
            queryable = queryable.Where(p => p.Type == query.Type);
        }

        if (!string.IsNullOrEmpty(query.Status))
        {
            queryable = queryable.Where(p => p.Status == query.Status);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var permissions = await queryable
            .OrderBy(p => p.Id)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (permissions, total);
    }

    public async Task CreateAsync(Permission permission)
    {
        context.Permissions.Add(permission);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Permission permission)
    {
        context.Permissions.Update(permission);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var permission = await FindByIdAsync(id);
        if (permission != null)
        {
            permission.Status = "deleted";
            await UpdateAsync(permission);
        }
    }

    public async Task<long> CountAsync()
    {
        return await context.Permissions
            .Where(p => p.Status != "deleted")
            .CountAsync();
    }

    public async Task<long> CountBeforeDateAsync(long timestamp)
    {
        return await context.Permissions
            .Where(p => p.Status != "deleted" && p.CreateTime <= timestamp)
            .CountAsync();
    }

    public async Task<List<Permission>> FindByStatusAsync(string status)
    {
        return await context.Permissions
            .Where(p => p.Status == status)
            .OrderBy(p => p.Sort)
            .ThenBy(p => p.Id)
            .ToListAsync();
    }
}
