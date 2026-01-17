namespace InfraMarket.Server.Config;

public class JwtConfig
{
    public string Secret { get; set; } = string.Empty;
    public long Expiration { get; set; } = 259200000; // 默认3天（毫秒）
}
