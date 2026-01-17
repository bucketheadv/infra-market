namespace InfraMarket.Server.Entities;

public class Permission : BaseEntity
{
    public string Name { get; set; } = string.Empty;
    public string Code { get; set; } = string.Empty;
    public string Type { get; set; } = "menu";
    public ulong? ParentId { get; set; }
    public string? Path { get; set; }
    public string? Icon { get; set; }
    public int Sort { get; set; } = 0;
    public string Status { get; set; } = "active";
}
