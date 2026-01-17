using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class ApiInterfaceRepository(ApplicationDbContext context) : IApiInterfaceRepository
{
    public async Task<ApiInterface?> FindByIdAsync(ulong id)
    {
        return await context.ApiInterfaces.FindAsync(id);
    }

    public async Task<List<ApiInterface>> FindByIDsAsync(List<ulong> ids)
    {
        if (ids.Count == 0) return [];
        return await context.ApiInterfaces
            .Where(a => ids.Contains(a.Id) && a.Status == 1)
            .ToListAsync();
    }

    public async Task<(List<ApiInterface> interfaces, long total)> PageAsync(ApiInterfaceQueryDto query)
    {
        var queryable = context.ApiInterfaces.AsQueryable();

        if (!string.IsNullOrEmpty(query.Name))
        {
            queryable = queryable.Where(a => a.Name.Contains(query.Name));
        }

        if (!string.IsNullOrEmpty(query.Method))
        {
            queryable = queryable.Where(a => a.Method == query.Method);
        }

        if (query.Status.HasValue)
        {
            queryable = queryable.Where(a => a.Status == query.Status);
        }

        if (!string.IsNullOrEmpty(query.Environment))
        {
            queryable = queryable.Where(a => a.Environment == query.Environment);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var interfaces = await queryable
            .OrderByDescending(a => a.CreateTime)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (interfaces, total);
    }

    public async Task CreateAsync(ApiInterface apiInterface)
    {
        context.ApiInterfaces.Add(apiInterface);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(ApiInterface apiInterface)
    {
        context.ApiInterfaces.Update(apiInterface);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong id)
    {
        var apiInterface = await FindByIdAsync(id);
        if (apiInterface != null)
        {
            context.ApiInterfaces.Remove(apiInterface);
            await context.SaveChangesAsync();
        }
    }

    public async Task<long> CountAsync()
    {
        return await context.ApiInterfaces.CountAsync();
    }

    public async Task<long> CountBeforeDateAsync(long timestamp)
    {
        return await context.ApiInterfaces
            .Where(a => a.CreateTime <= timestamp)
            .CountAsync();
    }
}
