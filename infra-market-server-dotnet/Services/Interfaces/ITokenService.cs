namespace InfraMarket.Server.Services.Interfaces;

public interface ITokenService
{
    string GenerateToken(ulong uid);
    bool ValidateToken(string token);
    ulong? GetUidFromToken(string token);
}
