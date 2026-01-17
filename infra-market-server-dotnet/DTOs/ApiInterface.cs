using System.ComponentModel.DataAnnotations;

namespace InfraMarket.Server.DTOs;

public class ApiInterfaceDto
{
    public ulong? Id { get; set; }
    public string? Name { get; set; }
    public string? Method { get; set; }
    public string? Url { get; set; }
    public string? Description { get; set; }
    public int? Status { get; set; }
    public string? PostType { get; set; }
    public string? Environment { get; set; }
    public long? Timeout { get; set; }
    public string? ValuePath { get; set; }
    public List<ApiParamDto> UrlParams { get; set; } = [];
    public List<ApiParamDto> HeaderParams { get; set; } = [];
    public List<ApiParamDto> BodyParams { get; set; } = [];
    public string? CreateTime { get; set; }
    public string? UpdateTime { get; set; }
}

public class ApiInterfaceFormDto
{
    public ulong? Id { get; set; }

    [Required]
    public string? Name { get; set; }

    [Required]
    public string? Method { get; set; }

    [Required]
    public string? Url { get; set; }

    public string? Description { get; set; }
    public string? PostType { get; set; }
    public string? Environment { get; set; }
    public long? Timeout { get; set; }
    public string? ValuePath { get; set; }
    public List<ApiParamDto> UrlParams { get; set; } = [];
    public List<ApiParamDto> HeaderParams { get; set; } = [];
    public List<ApiParamDto> BodyParams { get; set; } = [];
}

public class ApiInterfaceQueryDto
{
    public string? Name { get; set; }
    public string? Method { get; set; }
    public int? Status { get; set; }
    public string? Environment { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}

public class ApiParamDto
{
    public string? Name { get; set; }
    public string? ChineseName { get; set; }
    public string? ParamType { get; set; }
    public string? InputType { get; set; }
    public string? DataType { get; set; }
    public bool? Required { get; set; }
    public object? DefaultValue { get; set; }
    public bool? Changeable { get; set; }
    public List<SelectOptionDto> Options { get; set; } = [];
    public string? Description { get; set; }
    public int? Sort { get; set; }
}

public class SelectOptionDto
{
    public string? Value { get; set; }
    public string? Label { get; set; }
}

public class ApiExecuteRequestDto
{
    [Required]
    public ulong? InterfaceId { get; set; }
    public Dictionary<string, string>? Headers { get; set; }
    public Dictionary<string, object>? UrlParams { get; set; }
    public Dictionary<string, object>? BodyParams { get; set; }
    public long? Timeout { get; set; }
    public string? Remark { get; set; }
}

public class ApiExecuteResponseDto
{
    public int Status { get; set; }
    public Dictionary<string, string>? Headers { get; set; }
    public string? Body { get; set; }
    public string? ExtractedValue { get; set; }
    public long ResponseTime { get; set; }
    public bool Success { get; set; }
    public string? Error { get; set; }
}
