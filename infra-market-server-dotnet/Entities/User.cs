namespace InfraMarket.Server.Entities;

public class User : BaseEntity
{
    public string Username { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string? Phone { get; set; }
    public string Status { get; set; } = "active";
    public long? LastLoginTime { get; set; }
}
