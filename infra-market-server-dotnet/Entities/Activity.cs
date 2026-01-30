namespace InfraMarket.Server.Entities;

public class Activity : BaseEntity
{
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public ulong TemplateId { get; set; }
    public string? ConfigData { get; set; }
    public int Status { get; set; } = 1;
}

public class ActivityTemplate : BaseEntity
{
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string? Fields { get; set; }
    public int Status { get; set; } = 1;
}

public class ActivityComponent : BaseEntity
{
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public string? Fields { get; set; }
    public int Status { get; set; } = 1;
}
