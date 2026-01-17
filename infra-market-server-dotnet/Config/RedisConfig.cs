namespace InfraMarket.Server.Config;

public class RedisConfig
{
    public string Host { get; set; } = "localhost";
    public string Port { get; set; } = "6379";
    public string Password { get; set; } = string.Empty;
    public int DB { get; set; } = 0;
}
