using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class ApiInterfaceExecutionRecordService(
    IApiInterfaceExecutionRecordRepository repository,
    IApiInterfaceRepository apiInterfaceRepository) : IApiInterfaceExecutionRecordService
{
    public async Task<ApiData<PageResult<ApiInterfaceExecutionRecordDto>>> ListAsync(ApiInterfaceExecutionRecordQueryDto query)
    {
        var (records, total) = await repository.PageAsync(query);
        var recordDtos = records.Select(ConvertToDto).ToList();

        var result = new PageResult<ApiInterfaceExecutionRecordDto>
        {
            Records = recordDtos,
            Total = total,
            Page = query.Page ?? 1,
            Size = query.Size ?? 10
        };

        return ApiData<PageResult<ApiInterfaceExecutionRecordDto>>.Success(result);
    }

    public async Task<ApiData<ApiInterfaceExecutionRecordDto>> DetailAsync(ulong id)
    {
        var record = await repository.FindByIdAsync(id);
        if (record == null)
        {
            return ApiData<ApiInterfaceExecutionRecordDto>.Error("执行记录不存在", 404);
        }

        var dto = await ConvertToDtoWithInterfaceNameAsync(record);
        return ApiData<ApiInterfaceExecutionRecordDto>.Success(dto);
    }

    public async Task<ApiData<List<ApiInterfaceExecutionRecordDto>>> GetByExecutorIdAsync(ulong executorId, int limit)
    {
        var records = await repository.FindByExecutorIdAsync(executorId, limit);
        var recordDtos = records.Select(ConvertToDto).ToList();
        return ApiData<List<ApiInterfaceExecutionRecordDto>>.Success(recordDtos);
    }

    public async Task<ApiData<ApiInterfaceExecutionRecordStatsDto>> GetExecutionStatsAsync(ulong interfaceId)
    {
        var query = new ApiInterfaceExecutionRecordQueryDto
        {
            InterfaceId = interfaceId
        };

        var (records, _) = await repository.PageAsync(query);

        if (records.Count == 0)
        {
            var apiInterface = await apiInterfaceRepository.FindByIdAsync(interfaceId);
            return ApiData<ApiInterfaceExecutionRecordStatsDto>.Success(new ApiInterfaceExecutionRecordStatsDto
            {
                InterfaceId = interfaceId,
                InterfaceName = apiInterface?.Name
            });
        }

        var totalExecutions = records.Count;
        var successCount = records.Count(r => r.Success == true);
        var failedExecutions = totalExecutions - successCount;
        var successRate = totalExecutions > 0 ? (double)successCount / totalExecutions * 100 : 0.0;

        var executionTimes = records.Where(r => r.ExecutionTime.HasValue).Select(r => r.ExecutionTime!.Value).ToList();
        var avgExecutionTime = executionTimes.Count > 0 ? executionTimes.Average() : 0.0;
        var minExecutionTime = executionTimes.Count > 0 ? executionTimes.Min() : 0;
        var maxExecutionTime = executionTimes.Count > 0 ? executionTimes.Max() : 0;

        var lastRecord = records.OrderByDescending(r => r.CreateTime).FirstOrDefault();
        var lastExecutionTime = DateTimeUtil.FormatLocalDateTime(lastRecord?.CreateTime);

        var interfaceInfo = await apiInterfaceRepository.FindByIdAsync(interfaceId);

        var stats = new ApiInterfaceExecutionRecordStatsDto
        {
            InterfaceId = interfaceId,
            InterfaceName = interfaceInfo?.Name,
            TotalExecutions = totalExecutions,
            SuccessExecutions = successCount,
            FailedExecutions = failedExecutions,
            SuccessRate = successRate,
            AvgExecutionTime = avgExecutionTime,
            MinExecutionTime = minExecutionTime,
            MaxExecutionTime = maxExecutionTime,
            LastExecutionTime = lastExecutionTime
        };

        return ApiData<ApiInterfaceExecutionRecordStatsDto>.Success(stats);
    }

    public async Task<ApiData<long>> GetExecutionCountAsync(long startTime, long endTime)
    {
        var count = await repository.CountByTimeRangeAsync(startTime, endTime);
        return ApiData<long>.Success(count);
    }

    public async Task<ApiData<long>> CleanupOldRecordsAsync(long beforeTime)
    {
        var deletedCount = await repository.DeleteByTimeBeforeAsync(beforeTime);
        return ApiData<long>.Success(deletedCount);
    }

    private static ApiInterfaceExecutionRecordDto ConvertToDto(ApiInterfaceExecutionRecord record)
    {
        // 确保返回的是 {} 而不是 [] 或 null
        string NormalizeJson(string? value)
        {
            if (string.IsNullOrWhiteSpace(value) || value == "null" || value == "[]")
            {
                return "{}";
            }
            return value;
        }

        return new ApiInterfaceExecutionRecordDto
        {
            Id = record.Id,
            InterfaceId = record.InterfaceId,
            ExecutorId = record.ExecutorId,
            ExecutorName = record.ExecutorName,
            RequestParams = NormalizeJson(record.RequestParams),
            RequestHeaders = NormalizeJson(record.RequestHeaders),
            RequestBody = NormalizeJson(record.RequestBody),
            ResponseStatus = record.ResponseStatus,
            ResponseHeaders = NormalizeJson(record.ResponseHeaders),
            ResponseBody = record.ResponseBody,
            ExecutionTime = record.ExecutionTime,
            Success = record.Success,
            ErrorMessage = record.ErrorMessage,
            Remark = record.Remark,
            ClientIp = record.ClientIp,
            UserAgent = record.UserAgent,
            CreateTime = DateTimeUtil.FormatLocalDateTime(record.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(record.UpdateTime)
        };
    }

    private async Task<ApiInterfaceExecutionRecordDto> ConvertToDtoWithInterfaceNameAsync(ApiInterfaceExecutionRecord record)
    {
        var dto = ConvertToDto(record);

        // 获取接口名称
        if (!record.InterfaceId.HasValue) return dto;
        var apiInterface = await apiInterfaceRepository.FindByIdAsync(record.InterfaceId.Value);
        dto.InterfaceName = apiInterface?.Name;

        return dto;
    }
}
