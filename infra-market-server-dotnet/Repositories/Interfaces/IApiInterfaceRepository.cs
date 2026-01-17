using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;

namespace InfraMarket.Server.Repositories.Interfaces;

public interface IApiInterfaceRepository
{
    Task<ApiInterface?> FindByIdAsync(ulong id);
    Task<List<ApiInterface>> FindByIDsAsync(List<ulong> ids);
    Task<(List<ApiInterface> interfaces, long total)> PageAsync(ApiInterfaceQueryDto query);
    Task CreateAsync(ApiInterface apiInterface);
    Task UpdateAsync(ApiInterface apiInterface);
    Task DeleteAsync(ulong id);
    Task<long> CountAsync();
    Task<long> CountBeforeDateAsync(long timestamp);
}
