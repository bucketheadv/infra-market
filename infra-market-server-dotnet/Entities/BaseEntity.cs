namespace InfraMarket.Server.Entities;

public abstract class BaseEntity
{
    public ulong Id { get; set; }
    public long CreateTime { get; set; }
    public long UpdateTime { get; set; }
}
