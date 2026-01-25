using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace InfraMarket.Server.Utils;

public static class ClaimsPrincipalExtensions
{
    public static ulong? GetUid(this ClaimsPrincipal user)
    {
        var uidClaim = user.FindFirst("uid")
                       ?? user.FindFirst(JwtRegisteredClaimNames.Sub)
                       ?? user.FindFirst(ClaimTypes.NameIdentifier);
        if (uidClaim != null && ulong.TryParse(uidClaim.Value, out var uid))
        {
            return uid;
        }
        return null;
    }
}
