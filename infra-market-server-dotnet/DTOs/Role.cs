using System.ComponentModel.DataAnnotations;

namespace InfraMarket.Server.DTOs;

public class RoleDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Code { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string Status { get; set; } = string.Empty;
    public List<ulong> PermissionIds { get; set; } = new();
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class RoleFormDto
{
    [Required]
    [MinLength(2)]
    [MaxLength(50)]
    public string Name { get; set; } = string.Empty;

    [Required]
    [MinLength(2)]
    [MaxLength(100)]
    public string Code { get; set; } = string.Empty;

    [MaxLength(200)]
    public string? Description { get; set; }

    [Required]
    [MinLength(1)]
    public List<ulong> PermissionIds { get; set; } = new();
}

public class RoleQueryDto
{
    public string? Name { get; set; }
    public string? Code { get; set; }
    public string? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}
