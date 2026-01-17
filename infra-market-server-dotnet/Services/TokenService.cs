using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using InfraMarket.Server.Config;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using StackExchange.Redis;

namespace InfraMarket.Server.Services;

public class TokenService(IOptions<JwtConfig> jwtConfig, IConnectionMultiplexer redis) : ITokenService
{
    private readonly JwtConfig _jwtConfig = jwtConfig.Value;

    public string GenerateToken(ulong uid)
    {
        var key = Encoding.UTF8.GetBytes(_jwtConfig.Secret);
        var claims = new List<Claim>
        {
            new Claim(JwtRegisteredClaimNames.Sub, uid.ToString()) // 使用 "sub" claim，与 Java 版本保持一致
        };
        
        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(claims),
            Expires = DateTime.UtcNow.AddMilliseconds(_jwtConfig.Expiration),
            SigningCredentials = new SigningCredentials(
                new SymmetricSecurityKey(key),
                SecurityAlgorithms.HmacSha256Signature)
        };

        var tokenHandler = new JwtSecurityTokenHandler();
        var token = tokenHandler.CreateToken(tokenDescriptor);
        var tokenString = tokenHandler.WriteToken(token);

        // 将token存储到Redis
        var db = redis.GetDatabase();
        var expiration = TimeSpan.FromMilliseconds(_jwtConfig.Expiration);
        db.StringSet($"token:{uid}", tokenString, expiration);

        return tokenString;
    }

    public bool ValidateToken(string token)
    {
        try
        {
            // 先从 token 中提取 uid（与 Java 版本保持一致）
            var uid = GetUidFromToken(token);
            if (!uid.HasValue) return false;

            // 验证JWT token本身
            var key = Encoding.UTF8.GetBytes(_jwtConfig.Secret);
            var tokenHandler = new JwtSecurityTokenHandler();
            var validationParameters = new TokenValidationParameters
            {
                ValidateIssuerSigningKey = true,
                IssuerSigningKey = new SymmetricSecurityKey(key),
                ValidateIssuer = false,
                ValidateAudience = false,
                ValidateLifetime = true,
                ClockSkew = TimeSpan.Zero
            };

            tokenHandler.ValidateToken(token, validationParameters, out _);

            // 验证Redis中的token（与 Java 版本保持一致）
            var db = redis.GetDatabase();
            var storedToken = db.StringGet($"token:{uid.Value}");
            
            // 如果 Redis 中没有 token，返回 false（与 Java 版本保持一致）
            if (!storedToken.HasValue)
            {
                return false;
            }
            
            // 验证 Redis 中的 token 是否匹配
            return storedToken == token;

        }
        catch
        {
            return false;
        }
    }

    public ulong? GetUidFromToken(string token)
    {
        try
        {
            var tokenHandler = new JwtSecurityTokenHandler();
            var jwtToken = tokenHandler.ReadJwtToken(token);
            
            // 从 subject claim 中提取 uid，与 Java 版本保持一致
            // 优先从 Subject 属性获取（.NET JWT 库的标准方式）
            if (!string.IsNullOrEmpty(jwtToken.Subject) && ulong.TryParse(jwtToken.Subject, out var uid))
            {
                return uid;
            }
            
            // 如果 Subject 为空，尝试从 Claims 中查找 "sub" claim
            var subjectClaim = jwtToken.Claims.FirstOrDefault(c => c.Type == JwtRegisteredClaimNames.Sub);
            if (subjectClaim != null && ulong.TryParse(subjectClaim.Value, out var uidFromClaim))
            {
                return uidFromClaim;
            }
            
            return null;
        }
        catch
        {
            return null;
        }
    }
}
