using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IApiInterfaceExecutionRecordRepository
{
    Task<ApiInterfaceExecutionRecord?> FindByIdAsync(ulong id);
    Task<(List<ApiInterfaceExecutionRecord> records, long total)> PageAsync(ApiInterfaceExecutionRecordQueryDto query);
    Task<List<ApiInterfaceExecutionRecord>> FindByExecutorIdAsync(ulong executorId, int limit);
    Task CreateAsync(ApiInterfaceExecutionRecord record);
    Task<long> CountByTimeRangeAsync(long startTime, long endTime);
    Task<long> DeleteByTimeBeforeAsync(long beforeTime);
    Task<List<ulong>> FindMostUsedInterfaceIDsAsync(int days, int limit);
}
