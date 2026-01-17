namespace InfraMarket.Server.Config;

public class DatabaseConfig
{
    public string Host { get; set; } = "localhost";
    public string Port { get; set; } = "3306";
    public string User { get; set; } = "root";
    public string Password { get; set; } = string.Empty;
    public string Database { get; set; } = string.Empty;
}
