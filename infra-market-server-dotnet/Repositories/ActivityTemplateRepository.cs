using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class ActivityTemplateRepository(ApplicationDbContext context) : IActivityTemplateRepository
{
    public async Task<ActivityTemplate?> FindByIdAsync(ulong id)
    {
        return await context.ActivityTemplates
            .Where(t => t.Id == id)
            .FirstOrDefaultAsync();
    }

    public async Task<(List<ActivityTemplate> templates, long total)> PageAsync(ActivityTemplateQueryDto query)
    {
        var queryable = context.ActivityTemplates.AsQueryable();

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(t => t.Name.Contains(query.Name));
        }

        if (query.Status.HasValue)
        {
            queryable = queryable.Where(t => t.Status == query.Status.Value);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var templates = await queryable
            .OrderByDescending(t => t.CreateTime)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (templates, total);
    }

    public async Task<List<ActivityTemplate>> ListAsync()
    {
        return await context.ActivityTemplates
            .OrderByDescending(t => t.CreateTime)
            .ToListAsync();
    }

    public async Task CreateAsync(ActivityTemplate template)
    {
        context.ActivityTemplates.Add(template);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(ActivityTemplate template)
    {
        context.ActivityTemplates.Update(template);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var template = await FindByIdAsync(id);
        if (template != null)
        {
            context.ActivityTemplates.Remove(template);
            await context.SaveChangesAsync();
        }
    }
}
