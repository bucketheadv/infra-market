using System.ComponentModel.DataAnnotations;

namespace InfraMarket.Server.Entities;

public class ApiInterface : BaseEntity
{
    public string Name { get; set; } = string.Empty;
    public string Method { get; set; } = string.Empty;
    public string Url { get; set; } = string.Empty;
    
    [MaxLength(1000)]
    public string? Description { get; set; }
    
    public string? PostType { get; set; }
    
    [MaxLength(10485760)] // 10MB
    public string? Params { get; set; }
    
    public int? Status { get; set; }
    public string? Environment { get; set; }
    public long? Timeout { get; set; }
    public string? ValuePath { get; set; }
}
