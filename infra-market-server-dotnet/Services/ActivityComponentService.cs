using System.Text.Json;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;
using Microsoft.Extensions.Logging;

namespace InfraMarket.Server.Services;

public class ActivityComponentService(
    IActivityComponentRepository componentRepository,
    ILogger<ActivityComponentService> logger) : IActivityComponentService
{
    public async Task<ApiData<PageResult<ActivityComponentDto>>> GetActivityComponentsAsync(ActivityComponentQueryDto query)
    {
        try
        {
            var (components, total) = await componentRepository.PageAsync(query);

            // 转换为DTO
            var componentDtos = components.Select(c => ConvertComponentToDto(c)).ToList();

            var result = new PageResult<ActivityComponentDto>
            {
                Records = componentDtos,
                Total = total,
                Page = query.Page ?? 1,
                Size = query.Size ?? 10
            };

            return ApiData<PageResult<ActivityComponentDto>>.Success(result);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动组件列表失败");
            return ApiData<PageResult<ActivityComponentDto>>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<List<ActivityComponentDto>>> GetAllActivityComponentsAsync()
    {
        try
        {
            var components = await componentRepository.ListAsync();

            // 转换为DTO
            var componentDtos = components.Select(c => ConvertComponentToDto(c)).ToList();

            return ApiData<List<ActivityComponentDto>>.Success(componentDtos);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取所有活动组件失败");
            return ApiData<List<ActivityComponentDto>>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityComponentDto>> GetActivityComponentAsync(ulong id)
    {
        try
        {
            var component = await componentRepository.FindByIdAsync(id);
            if (component == null)
            {
                logger.LogWarning("获取活动组件详情失败，组件ID: {ComponentId}，组件不存在", id);
                return ApiData<ActivityComponentDto>.Error("活动组件不存在", 404);
            }

            var componentDto = ConvertComponentToDto(component);
            return ApiData<ActivityComponentDto>.Success(componentDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动组件详情失败，组件ID: {ComponentId}", id);
            return ApiData<ActivityComponentDto>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityComponentDto>> CreateActivityComponentAsync(ActivityComponentFormDto dto)
    {
        try
        {
            // 序列化字段配置
            var fieldsJson = SerializeFields(dto.Fields);

            var component = new ActivityComponent
            {
                Name = dto.Name,
                Description = dto.Description,
                Fields = fieldsJson,
                Status = dto.Status ?? 1
            };

            await componentRepository.CreateAsync(component);

            var componentDto = ConvertComponentToDto(component);
            return ApiData<ActivityComponentDto>.Success(componentDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "创建活动组件失败");
            return ApiData<ActivityComponentDto>.Error("创建活动组件失败", 500);
        }
    }

    public async Task<ApiData<ActivityComponentDto>> UpdateActivityComponentAsync(ulong id, ActivityComponentFormDto dto)
    {
        try
        {
            var component = await componentRepository.FindByIdAsync(id);
            if (component == null)
            {
                logger.LogWarning("更新活动组件失败，组件ID: {ComponentId}，组件不存在", id);
                return ApiData<ActivityComponentDto>.Error("活动组件不存在", 404);
            }

            // 序列化字段配置
            string? fieldsJson = null;
            if (dto.Fields != null)
            {
                fieldsJson = SerializeFields(dto.Fields);
            }

            component.Name = dto.Name;
            if (dto.Description != null)
            {
                component.Description = dto.Description;
            }
            if (fieldsJson != null)
            {
                component.Fields = fieldsJson;
            }
            if (dto.Status.HasValue)
            {
                component.Status = dto.Status.Value;
            }

            await componentRepository.UpdateAsync(component);

            var componentDto = ConvertComponentToDto(component);
            return ApiData<ActivityComponentDto>.Success(componentDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动组件失败，组件ID: {ComponentId}", id);
            return ApiData<ActivityComponentDto>.Error("更新活动组件失败", 500);
        }
    }

    public async Task<ApiData<object>> DeleteActivityComponentAsync(ulong id)
    {
        try
        {
            var component = await componentRepository.FindByIdAsync(id);
            if (component == null)
            {
                logger.LogWarning("删除活动组件失败，组件ID: {ComponentId}，组件不存在", id);
                return ApiData<object>.Error("活动组件不存在", 404);
            }

            await componentRepository.DeleteAsync(id);
            return ApiData<object>.Success(null!);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "删除活动组件失败，组件ID: {ComponentId}", id);
            return ApiData<object>.Error("删除活动组件失败", 500);
        }
    }

    public async Task<ApiData<ActivityComponentDto>> UpdateActivityComponentStatusAsync(ulong id, int status)
    {
        try
        {
            var component = await componentRepository.FindByIdAsync(id);
            if (component == null)
            {
                logger.LogWarning("更新活动组件状态失败，组件ID: {ComponentId}，组件不存在", id);
                return ApiData<ActivityComponentDto>.Error("活动组件不存在", 404);
            }

            component.Status = status;
            await componentRepository.UpdateAsync(component);

            var componentDto = ConvertComponentToDto(component);
            return ApiData<ActivityComponentDto>.Success(componentDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动组件状态失败，组件ID: {ComponentId}，状态: {Status}", id, status);
            return ApiData<ActivityComponentDto>.Error("更新状态失败", 500);
        }
    }

    public async Task<ApiData<ActivityComponentDto>> CopyActivityComponentAsync(ulong id)
    {
        try
        {
            var component = await componentRepository.FindByIdAsync(id);
            if (component == null)
            {
                logger.LogWarning("复制活动组件失败，组件ID: {ComponentId}，组件不存在", id);
                return ApiData<ActivityComponentDto>.Error("活动组件不存在", 404);
            }

            var newComponent = new ActivityComponent
            {
                Name = component.Name + "_副本",
                Description = component.Description,
                Fields = component.Fields,
                Status = 0
            };

            await componentRepository.CreateAsync(newComponent);

            var componentDto = ConvertComponentToDto(newComponent);
            return ApiData<ActivityComponentDto>.Success(componentDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "复制活动组件失败，组件ID: {ComponentId}", id);
            return ApiData<ActivityComponentDto>.Error("复制活动组件失败", 500);
        }
    }

    private static ActivityComponentDto ConvertComponentToDto(ActivityComponent component)
    {
        var fields = ParseFields(component.Fields);

        return new ActivityComponentDto
        {
            Id = component.Id,
            Name = component.Name,
            Description = component.Description,
            Fields = fields,
            Status = component.Status,
            CreateTime = DateTimeUtil.FormatLocalDateTime(component.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(component.UpdateTime)
        };
    }

    private static string? SerializeFields(List<ActivityComponentFieldDto>? fields)
    {
        if (fields == null)
        {
            return null;
        }

        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull
        };
        return JsonSerializer.Serialize(fields, options);
    }

    private static List<ActivityComponentFieldDto>? ParseFields(string? fieldsJson)
    {
        if (string.IsNullOrEmpty(fieldsJson))
        {
            return null;
        }

        try
        {
            var options = new JsonSerializerOptions
            {
                PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
                DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull
            };
            return JsonSerializer.Deserialize<List<ActivityComponentFieldDto>>(fieldsJson, options);
        }
        catch
        {
            return null;
        }
    }
}
