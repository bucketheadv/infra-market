using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IUserRoleRepository
{
    Task<List<UserRole>> FindByUidAsync(ulong uid);
    Task<List<UserRole>> FindByRoleIdAsync(ulong roleId);
    Task<List<UserRole>> FindByUiDsAsync(List<ulong> uids);
    Task DeleteByUidAsync(ulong uid);
    Task CreateAsync(UserRole userRole);
    Task<long> CountByRoleIdAsync(ulong roleId);
}
