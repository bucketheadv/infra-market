using System.ComponentModel.DataAnnotations;

namespace InfraMarket.Server.Entities;

public class ApiInterfaceExecutionRecord : BaseEntity
{
    public ulong? InterfaceId { get; set; }
    public ulong? ExecutorId { get; set; }
    public string ExecutorName { get; set; } = string.Empty;
    
    [MaxLength(10485760)] // 10MB
    public string? RequestParams { get; set; }
    
    [MaxLength(1048576)] // 1MB
    public string? RequestHeaders { get; set; }
    
    [MaxLength(10485760)] // 10MB
    public string? RequestBody { get; set; }
    
    public int? ResponseStatus { get; set; }
    
    [MaxLength(1048576)] // 1MB
    public string? ResponseHeaders { get; set; }
    
    [MaxLength(10485760)] // 10MB
    public string? ResponseBody { get; set; }
    
    public long? ExecutionTime { get; set; }
    public bool? Success { get; set; }
    
    [MaxLength(5000)]
    public string? ErrorMessage { get; set; }
    
    [MaxLength(1000)]
    public string? Remark { get; set; }
    
    [MaxLength(50)]
    public string? ClientIp { get; set; }
    
    [MaxLength(500)]
    public string? UserAgent { get; set; }
}
