using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Services.Interfaces;

public interface IApiInterfaceExecutionRecordService
{
    Task<ApiData<PageResult<ApiInterfaceExecutionRecordDto>>> ListAsync(ApiInterfaceExecutionRecordQueryDto query);
    Task<ApiData<ApiInterfaceExecutionRecordDto>> DetailAsync(ulong id);
    Task<ApiData<List<ApiInterfaceExecutionRecordDto>>> GetByExecutorIdAsync(ulong executorId, int limit);
    Task<ApiData<ApiInterfaceExecutionRecordStatsDto>> GetExecutionStatsAsync(ulong interfaceId);
    Task<ApiData<long>> GetExecutionCountAsync(long startTime, long endTime);
    Task<ApiData<long>> CleanupOldRecordsAsync(long beforeTime);
}
