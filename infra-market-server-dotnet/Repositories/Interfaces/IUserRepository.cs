using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IUserRepository
{
    Task<User?> FindByUidAsync(ulong uid);
    Task<List<User>> FindByUiDsAsync(List<ulong> uids);
    Task<User?> FindByUsernameAsync(string username);
    Task<User?> FindByEmailAsync(string email);
    Task<User?> FindByPhoneAsync(string phone);
    Task<(List<User> users, long total)> PageAsync(UserQueryDto query);
    Task CreateAsync(User user);
    Task UpdateAsync(User user);
    Task DeleteAsync(ulong uid);
    Task<List<User>> GetRecentLoginUsersAsync(int limit);
    Task<long> CountAsync();
    Task<long> CountBeforeDateAsync(long timestamp);
}
