using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace InfraMarket.Server.DTOs;

public class PermissionDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Code { get; set; } = string.Empty;
    public string Type { get; set; } = string.Empty;
    public ulong? ParentId { get; set; }
    public string? Path { get; set; }
    public string? Icon { get; set; }
    public int Sort { get; set; }
    public string Status { get; set; } = string.Empty;
    [JsonIgnore(Condition = JsonIgnoreCondition.WhenWritingNull)]
    public List<PermissionDto>? Children { get; set; }
    public ParentPermissionDto? ParentPermission { get; set; }
    public List<string> AccessPath { get; set; } = new();
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class ParentPermissionDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Code { get; set; } = string.Empty;
}

public class PermissionFormDto
{
    [Required]
    [MinLength(2)]
    [MaxLength(50)]
    public string Name { get; set; } = string.Empty;

    [Required]
    [MinLength(2)]
    [MaxLength(100)]
    public string Code { get; set; } = string.Empty;

    [Required]
    public string Type { get; set; } = string.Empty;

    public ulong? ParentId { get; set; }

    [MaxLength(200)]
    public string? Path { get; set; }

    [MaxLength(100)]
    public string? Icon { get; set; }

    [Range(0, int.MaxValue)]
    public int Sort { get; set; }
}

public class PermissionQueryDto
{
    public string? Name { get; set; }
    public string? Code { get; set; }
    public string? Type { get; set; }
    public string? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}
