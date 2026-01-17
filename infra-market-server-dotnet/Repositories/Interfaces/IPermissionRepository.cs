using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IPermissionRepository
{
    Task<Permission?> FindByCodeAsync(string code);
    Task<Permission?> FindByIdAsync(ulong id);
    Task<List<Permission>> FindByIDsAsync(List<ulong> ids);
    Task<List<Permission>> FindByParentIdAsync(ulong parentId);
    Task<List<Permission>> FindAllAsync();
    Task<(List<Permission> permissions, long total)> PageAsync(PermissionQueryDto query);
    Task CreateAsync(Permission permission);
    Task UpdateAsync(Permission permission);
    Task DeleteAsync(ulong id);
    Task<long> CountAsync();
    Task<long> CountBeforeDateAsync(long timestamp);
    Task<List<Permission>> FindByStatusAsync(string status);
}
