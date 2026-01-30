using System.ComponentModel.DataAnnotations;
using System.Text.Json;

namespace InfraMarket.Server.DTOs;

public class ActivityDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public ulong TemplateId { get; set; }
    public string? TemplateName { get; set; }
    public Dictionary<string, object>? ConfigData { get; set; }
    public int Status { get; set; }
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class ActivityFormDto
{
    [Required]
    [MinLength(1)]
    [MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(500)]
    public string? Description { get; set; }

    [Required]
    [Range(1, ulong.MaxValue)]
    public ulong TemplateId { get; set; }

    public Dictionary<string, object>? ConfigData { get; set; }

    [Range(0, 1)]
    public int? Status { get; set; }
}

public class ActivityQueryDto
{
    public string? Name { get; set; }
    public ulong? TemplateId { get; set; }
    [Range(0, 1)]
    public int? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}

public class ActivityTemplateDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public List<ActivityTemplateFieldDto>? Fields { get; set; }
    public int Status { get; set; }
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class ActivityTemplateFieldDto
{
    public string Name { get; set; } = string.Empty;
    public string? Label { get; set; }
    public string? Type { get; set; }
    public string? InputType { get; set; }
    public bool? Required { get; set; }
    public object? DefaultValue { get; set; }
    public string? Description { get; set; }
    public int? Sort { get; set; }
    public List<SelectOptionDto>? Options { get; set; }
    public bool? Multiple { get; set; }
    public object? Min { get; set; }
    public object? Max { get; set; }
    public int? MinLength { get; set; }
    public int? MaxLength { get; set; }
    public string? Pattern { get; set; }
    public string? Placeholder { get; set; }
    public ActivityTemplateFieldDto? ItemType { get; set; }
    public Dictionary<string, ActivityTemplateFieldDto>? Properties { get; set; }
    public ulong? ComponentId { get; set; }
    public bool? IsArray { get; set; }
    public bool? AllowDynamic { get; set; }
}

public class ActivityTemplateFormDto
{
    [Required]
    [MinLength(1)]
    [MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(500)]
    public string? Description { get; set; }

    public List<ActivityTemplateFieldDto>? Fields { get; set; }

    [Range(0, 1)]
    public int? Status { get; set; }
}

public class ActivityTemplateQueryDto
{
    public string? Name { get; set; }
    [Range(0, 1)]
    public int? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}

public class ActivityComponentDto
{
    public ulong Id { get; set; }
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public List<ActivityComponentFieldDto>? Fields { get; set; }
    public int Status { get; set; }
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class ActivityComponentFieldDto
{
    public string Name { get; set; } = string.Empty;
    public string? Label { get; set; }
    public string? Type { get; set; }
    public string? InputType { get; set; }
    public bool? Required { get; set; }
    public object? DefaultValue { get; set; }
    public string? Description { get; set; }
    public int? Sort { get; set; }
    public List<SelectOptionDto>? Options { get; set; }
    public bool? Multiple { get; set; }
    public object? Min { get; set; }
    public object? Max { get; set; }
    public int? MinLength { get; set; }
    public int? MaxLength { get; set; }
    public string? Pattern { get; set; }
    public string? Placeholder { get; set; }
    public ActivityComponentFieldDto? ItemType { get; set; }
    public Dictionary<string, ActivityComponentFieldDto>? Properties { get; set; }
    public ulong? ComponentId { get; set; }
    public bool? IsArray { get; set; }
    public bool? AllowDynamic { get; set; }
}

public class ActivityComponentFormDto
{
    [Required]
    [MinLength(1)]
    [MaxLength(100)]
    public string Name { get; set; } = string.Empty;

    [MaxLength(500)]
    public string? Description { get; set; }

    public List<ActivityComponentFieldDto>? Fields { get; set; }

    [Range(0, 1)]
    public int? Status { get; set; }
}

public class ActivityComponentQueryDto
{
    public string? Name { get; set; }
    [Range(0, 1)]
    public int? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}
