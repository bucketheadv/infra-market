using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class ApiInterfaceExecutionRecordRepository(ApplicationDbContext context) : IApiInterfaceExecutionRecordRepository
{
    public async Task<ApiInterfaceExecutionRecord?> FindByIdAsync(ulong id)
    {
        return await context.ApiInterfaceExecutionRecords.FindAsync(id);
    }

    public async Task<(List<ApiInterfaceExecutionRecord> records, long total)> PageAsync(ApiInterfaceExecutionRecordQueryDto query)
    {
        var queryable = context.ApiInterfaceExecutionRecords.AsQueryable();

        if (query.InterfaceId.HasValue)
        {
            queryable = queryable.Where(r => r.InterfaceId == query.InterfaceId);
        }

        if (!string.IsNullOrEmpty(query.Keyword))
        {
            var keyword = $"%{query.Keyword}%";
            queryable = queryable.Where(r =>
                (EF.Functions.Like(r.ExecutorName, keyword)) ||
                (r.ErrorMessage != null && EF.Functions.Like(r.ErrorMessage, keyword)) ||
                (r.Remark != null && EF.Functions.Like(r.Remark, keyword)));
        }

        if (query.ExecutorId.HasValue)
        {
            queryable = queryable.Where(r => r.ExecutorId == query.ExecutorId);
        }

        if (!string.IsNullOrEmpty(query.ExecutorName))
        {
            queryable = queryable.Where(r => r.ExecutorName.Contains(query.ExecutorName));
        }

        if (query.Success.HasValue)
        {
            queryable = queryable.Where(r => r.Success == query.Success);
        }

        if (query.MinExecutionTime.HasValue)
        {
            queryable = queryable.Where(r => r.ExecutionTime >= query.MinExecutionTime);
        }

        if (query.MaxExecutionTime.HasValue)
        {
            queryable = queryable.Where(r => r.ExecutionTime <= query.MaxExecutionTime);
        }

        if (query.StartTime.HasValue)
        {
            queryable = queryable.Where(r => r.CreateTime >= query.StartTime);
        }

        if (query.EndTime.HasValue)
        {
            queryable = queryable.Where(r => r.CreateTime <= query.EndTime);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var records = await queryable
            .OrderByDescending(r => r.Id)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (records, total);
    }

    public async Task<List<ApiInterfaceExecutionRecord>> FindByExecutorIdAsync(ulong executorId, int limit)
    {
        return await context.ApiInterfaceExecutionRecords
            .Where(r => r.ExecutorId == executorId)
            .OrderByDescending(r => r.CreateTime)
            .Take(limit)
            .ToListAsync();
    }

    public async Task CreateAsync(ApiInterfaceExecutionRecord record)
    {
        context.ApiInterfaceExecutionRecords.Add(record);
        await context.SaveChangesAsync();
    }

    public async Task<long> CountByTimeRangeAsync(long startTime, long endTime)
    {
        return await context.ApiInterfaceExecutionRecords
            .Where(r => r.CreateTime >= startTime && r.CreateTime <= endTime)
            .CountAsync();
    }

    public async Task<long> DeleteByTimeBeforeAsync(long beforeTime)
    {
        var records = await context.ApiInterfaceExecutionRecords
            .Where(r => r.CreateTime < beforeTime)
            .ToListAsync();
        
        context.ApiInterfaceExecutionRecords.RemoveRange(records);
        await context.SaveChangesAsync();
        
        return records.Count;
    }

    public async Task<List<ulong>> FindMostUsedInterfaceIDsAsync(int days, int limit)
    {
        var startTime = DateTimeOffset.UtcNow.AddDays(-days).ToUnixTimeMilliseconds();

        var results = await context.ApiInterfaceExecutionRecords
            .Where(r => r.CreateTime >= startTime)
            .GroupBy(r => r.InterfaceId)
            .Select(g => new { InterfaceId = g.Key, Count = g.Count() })
            .OrderByDescending(x => x.Count)
            .Take(limit)
            .ToListAsync();

        return results.Where(r => r.InterfaceId.HasValue).Select(r => r.InterfaceId!.Value).ToList();
    }
}
