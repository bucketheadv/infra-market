using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IActivityComponentRepository
{
    Task<ActivityComponent?> FindByIdAsync(ulong id);
    Task<(List<ActivityComponent> components, long total)> PageAsync(ActivityComponentQueryDto query);
    Task<List<ActivityComponent>> ListAsync();
    Task CreateAsync(ActivityComponent component);
    Task UpdateAsync(ActivityComponent component);
    Task DeleteAsync(ulong id);
}
