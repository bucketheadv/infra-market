using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IRoleRepository
{
    Task<Role?> FindByCodeAsync(string code);
    Task<Role?> FindByIdAsync(ulong id);
    Task<List<Role>> FindByIDsAsync(List<ulong> ids);
    Task<(List<Role> roles, long total)> PageAsync(RoleQueryDto query);
    Task CreateAsync(Role role);
    Task UpdateAsync(Role role);
    Task DeleteAsync(ulong id);
    Task<long> CountAsync();
    Task<long> CountBeforeDateAsync(long timestamp);
    Task<List<Role>> FindByStatusAsync(string status);
}
