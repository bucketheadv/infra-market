namespace InfraMarket.Server.DTOs;

public class ApiInterfaceExecutionRecordDto
{
    public ulong? Id { get; set; }
    public ulong? InterfaceId { get; set; }
    public string? InterfaceName { get; set; }
    public ulong? ExecutorId { get; set; }
    public string? ExecutorName { get; set; }
    public string? RequestParams { get; set; }
    public string? RequestHeaders { get; set; }
    public string? RequestBody { get; set; }
    public int? ResponseStatus { get; set; }
    public string? ResponseHeaders { get; set; }
    public string? ResponseBody { get; set; }
    public long? ExecutionTime { get; set; }
    public bool? Success { get; set; }
    public string? ErrorMessage { get; set; }
    public string? Remark { get; set; }
    public string? ClientIp { get; set; }
    public string? UserAgent { get; set; }
    public string? CreateTime { get; set; }
    public string? UpdateTime { get; set; }
}

public class ApiInterfaceExecutionRecordQueryDto
{
    public ulong? InterfaceId { get; set; }
    public string? Keyword { get; set; }
    public ulong? ExecutorId { get; set; }
    public string? ExecutorName { get; set; }
    public bool? Success { get; set; }
    public long? StartTime { get; set; }
    public long? EndTime { get; set; }
    public long? MinExecutionTime { get; set; }
    public long? MaxExecutionTime { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}

public class ApiInterfaceExecutionRecordStatsDto
{
    public ulong? InterfaceId { get; set; }
    public string? InterfaceName { get; set; }
    public long TotalExecutions { get; set; }
    public long SuccessExecutions { get; set; }
    public long FailedExecutions { get; set; }
    public double SuccessRate { get; set; }
    public double AvgExecutionTime { get; set; }
    public long MinExecutionTime { get; set; }
    public long MaxExecutionTime { get; set; }
    public string? LastExecutionTime { get; set; }
}
