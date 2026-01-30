using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IActivityRepository
{
    Task<Activity?> FindByIdAsync(ulong id);
    Task<(List<Activity> activities, long total)> PageAsync(ActivityQueryDto query);
    Task CreateAsync(Activity activity);
    Task UpdateAsync(Activity activity);
    Task DeleteAsync(ulong id);
}
