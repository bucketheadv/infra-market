using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class ActivityRepository(ApplicationDbContext context) : IActivityRepository
{
    public async Task<Activity?> FindByIdAsync(ulong id)
    {
        return await context.Activities
            .Where(a => a.Id == id)
            .FirstOrDefaultAsync();
    }

    public async Task<(List<Activity> activities, long total)> PageAsync(ActivityQueryDto query)
    {
        var queryable = context.Activities.AsQueryable();

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(a => a.Name.Contains(query.Name));
        }

        if (query.TemplateId.HasValue)
        {
            queryable = queryable.Where(a => a.TemplateId == query.TemplateId.Value);
        }

        if (query.Status.HasValue)
        {
            queryable = queryable.Where(a => a.Status == query.Status.Value);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var activities = await queryable
            .OrderByDescending(a => a.CreateTime)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (activities, total);
    }

    public async Task CreateAsync(Activity activity)
    {
        context.Activities.Add(activity);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(Activity activity)
    {
        context.Activities.Update(activity);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var activity = await FindByIdAsync(id);
        if (activity != null)
        {
            context.Activities.Remove(activity);
            await context.SaveChangesAsync();
        }
    }
}
