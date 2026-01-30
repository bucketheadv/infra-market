using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IActivityTemplateRepository
{
    Task<ActivityTemplate?> FindByIdAsync(ulong id);
    Task<(List<ActivityTemplate> templates, long total)> PageAsync(ActivityTemplateQueryDto query);
    Task<List<ActivityTemplate>> ListAsync();
    Task CreateAsync(ActivityTemplate template);
    Task UpdateAsync(ActivityTemplate template);
    Task DeleteAsync(ulong id);
}
