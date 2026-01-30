using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class ActivityComponentRepository(ApplicationDbContext context) : IActivityComponentRepository
{
    public async Task<ActivityComponent?> FindByIdAsync(ulong id)
    {
        return await context.ActivityComponents
            .Where(c => c.Id == id)
            .FirstOrDefaultAsync();
    }

    public async Task<(List<ActivityComponent> components, long total)> PageAsync(ActivityComponentQueryDto query)
    {
        var queryable = context.ActivityComponents.AsQueryable();

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(c => c.Name.Contains(query.Name));
        }

        if (query.Status.HasValue)
        {
            queryable = queryable.Where(c => c.Status == query.Status.Value);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var components = await queryable
            .OrderByDescending(c => c.CreateTime)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (components, total);
    }

    public async Task<List<ActivityComponent>> ListAsync()
    {
        return await context.ActivityComponents
            .OrderByDescending(c => c.CreateTime)
            .ToListAsync();
    }

    public async Task CreateAsync(ActivityComponent component)
    {
        context.ActivityComponents.Add(component);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(ActivityComponent component)
    {
        context.ActivityComponents.Update(component);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var component = await FindByIdAsync(id);
        if (component != null)
        {
            context.ActivityComponents.Remove(component);
            await context.SaveChangesAsync();
        }
    }
}
