using System.Text.Json;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;
using Microsoft.Extensions.Logging;

namespace InfraMarket.Server.Services;

public class ActivityService(
    IActivityRepository activityRepository,
    IActivityTemplateRepository templateRepository,
    ILogger<ActivityService> logger) : IActivityService
{
    public async Task<ApiData<PageResult<ActivityDto>>> GetActivitiesAsync(ActivityQueryDto query)
    {
        try
        {
            var (activities, total) = await activityRepository.PageAsync(query);

            // 批量获取模板信息
            var templateIds = activities.Select(a => a.TemplateId).Distinct().ToList();
            var templateMap = new Dictionary<ulong, ActivityTemplate?>();
            foreach (var templateId in templateIds)
            {
                var template = await templateRepository.FindByIdAsync(templateId);
                templateMap[templateId] = template;
            }

            // 转换为DTO
            var activityDtos = activities.Select(activity =>
            {
                var template = templateMap.GetValueOrDefault(activity.TemplateId);
                var templateName = template?.Name;
                return ConvertActivityToDto(activity, templateName);
            }).ToList();

            var result = new PageResult<ActivityDto>
            {
                Records = activityDtos,
                Total = total,
                Page = query.Page ?? 1,
                Size = query.Size ?? 10
            };

            return ApiData<PageResult<ActivityDto>>.Success(result);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动列表失败");
            return ApiData<PageResult<ActivityDto>>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityDto>> GetActivityAsync(ulong id)
    {
        try
        {
            var activity = await activityRepository.FindByIdAsync(id);
            if (activity == null)
            {
                logger.LogWarning("获取活动详情失败，活动ID: {ActivityId}，活动不存在", id);
                return ApiData<ActivityDto>.Error("活动不存在", 404);
            }

            // 获取模板信息
            var template = await templateRepository.FindByIdAsync(activity.TemplateId);
            var templateName = template?.Name;

            var activityDto = ConvertActivityToDto(activity, templateName);
            return ApiData<ActivityDto>.Success(activityDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动详情失败，活动ID: {ActivityId}", id);
            return ApiData<ActivityDto>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityDto>> CreateActivityAsync(ActivityFormDto dto)
    {
        try
        {
            // 验证模板是否存在
            var template = await templateRepository.FindByIdAsync(dto.TemplateId);
            if (template == null)
            {
                return ApiData<ActivityDto>.Error("活动模板不存在", 400);
            }

            // 验证配置数据
            var validationError = ValidateConfigData(dto.ConfigData, template);
            if (validationError != null)
            {
                return ApiData<ActivityDto>.Error(validationError, 400);
            }

            // 序列化配置数据
            var configDataJson = SerializeConfigData(dto.ConfigData);

            var activity = new Activity
            {
                Name = dto.Name,
                Description = dto.Description,
                TemplateId = dto.TemplateId,
                ConfigData = configDataJson,
                Status = dto.Status ?? 1
            };

            await activityRepository.CreateAsync(activity);

            var activityDto = ConvertActivityToDto(activity, template.Name);
            return ApiData<ActivityDto>.Success(activityDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "创建活动失败");
            return ApiData<ActivityDto>.Error("创建活动失败", 500);
        }
    }

    public async Task<ApiData<ActivityDto>> UpdateActivityAsync(ulong id, ActivityFormDto dto)
    {
        try
        {
            var activity = await activityRepository.FindByIdAsync(id);
            if (activity == null)
            {
                logger.LogWarning("更新活动失败，活动ID: {ActivityId}，活动不存在", id);
                return ApiData<ActivityDto>.Error("活动不存在", 404);
            }

            // 验证模板是否存在
            if (dto.TemplateId != 0)
            {
                var validationTemplate = await templateRepository.FindByIdAsync(dto.TemplateId);
                if (validationTemplate == null)
                {
                    return ApiData<ActivityDto>.Error("活动模板不存在", 400);
                }

                // 验证配置数据
                var validationError = ValidateConfigData(dto.ConfigData, validationTemplate);
                if (validationError != null)
                {
                    return ApiData<ActivityDto>.Error(validationError, 400);
                }
            }

            // 序列化配置数据
            string? configDataJson = null;
            if (dto.ConfigData != null)
            {
                configDataJson = SerializeConfigData(dto.ConfigData);
            }

            activity.Name = dto.Name;
            if (dto.Description != null)
            {
                activity.Description = dto.Description;
            }
            if (dto.TemplateId != 0)
            {
                activity.TemplateId = dto.TemplateId;
            }
            if (configDataJson != null)
            {
                activity.ConfigData = configDataJson;
            }
            if (dto.Status.HasValue)
            {
                activity.Status = dto.Status.Value;
            }

            await activityRepository.UpdateAsync(activity);

            // 获取模板名称
            var template = await templateRepository.FindByIdAsync(activity.TemplateId);
            var templateName = template?.Name;

            var activityDto = ConvertActivityToDto(activity, templateName);
            return ApiData<ActivityDto>.Success(activityDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动失败，活动ID: {ActivityId}", id);
            return ApiData<ActivityDto>.Error("更新活动失败", 500);
        }
    }

    public async Task<ApiData<object>> DeleteActivityAsync(ulong id)
    {
        try
        {
            var activity = await activityRepository.FindByIdAsync(id);
            if (activity == null)
            {
                logger.LogWarning("删除活动失败，活动ID: {ActivityId}，活动不存在", id);
                return ApiData<object>.Error("活动不存在", 404);
            }

            await activityRepository.DeleteAsync(id);
            return ApiData<object>.Success(null!);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "删除活动失败，活动ID: {ActivityId}", id);
            return ApiData<object>.Error("删除活动失败", 500);
        }
    }

    public async Task<ApiData<ActivityDto>> UpdateActivityStatusAsync(ulong id, int status)
    {
        try
        {
            var activity = await activityRepository.FindByIdAsync(id);
            if (activity == null)
            {
                logger.LogWarning("更新活动状态失败，活动ID: {ActivityId}，活动不存在", id);
                return ApiData<ActivityDto>.Error("活动不存在", 404);
            }

            activity.Status = status;
            await activityRepository.UpdateAsync(activity);

            // 获取模板名称
            var template = await templateRepository.FindByIdAsync(activity.TemplateId);
            var templateName = template?.Name;

            var activityDto = ConvertActivityToDto(activity, templateName);
            return ApiData<ActivityDto>.Success(activityDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动状态失败，活动ID: {ActivityId}，状态: {Status}", id, status);
            return ApiData<ActivityDto>.Error("更新状态失败", 500);
        }
    }

    private static ActivityDto ConvertActivityToDto(Activity activity, string? templateName)
    {
        var configData = ParseConfigData(activity.ConfigData);

        return new ActivityDto
        {
            Id = activity.Id,
            Name = activity.Name,
            Description = activity.Description,
            TemplateId = activity.TemplateId,
            TemplateName = templateName,
            ConfigData = configData,
            Status = activity.Status,
            CreateTime = DateTimeUtil.FormatLocalDateTime(activity.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(activity.UpdateTime)
        };
    }

    private static string? SerializeConfigData(Dictionary<string, object>? configData)
    {
        if (configData == null)
        {
            return null;
        }

        var options = new JsonSerializerOptions
        {
            PropertyNamingPolicy = JsonNamingPolicy.CamelCase,
            DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull
        };
        return JsonSerializer.Serialize(configData, options);
    }

    private static Dictionary<string, object>? ParseConfigData(string? configDataJson)
    {
        if (string.IsNullOrEmpty(configDataJson))
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
            return JsonSerializer.Deserialize<Dictionary<string, object>>(configDataJson, options);
        }
        catch
        {
            return null;
        }
    }

    private static string? ValidateConfigData(Dictionary<string, object>? configData, ActivityTemplate template)
    {
        if (string.IsNullOrEmpty(template.Fields))
        {
            return null;
        }

        try
        {
            var fields = JsonSerializer.Deserialize<List<ActivityTemplateFieldDto>>(template.Fields);
            if (fields == null)
            {
                return null;
            }

            // 验证必填字段
            foreach (var field in fields)
            {
                if (field.Required == true)
                {
                    var fieldName = field.Name;
                    if (string.IsNullOrEmpty(fieldName))
                    {
                        continue;
                    }

                    if (configData == null || !configData.ContainsKey(fieldName))
                    {
                        var displayName = string.IsNullOrEmpty(field.Label) ? fieldName : $"{field.Label}（{fieldName}）";
                        return $"字段 {displayName} 为必填项，不能为空";
                    }

                    var value = configData[fieldName];
                    if (value == null)
                    {
                        var displayName = string.IsNullOrEmpty(field.Label) ? fieldName : $"{field.Label}（{fieldName}）";
                        return $"字段 {displayName} 为必填项，不能为空";
                    }

                    // 检查字符串是否为空
                    if (value is string strValue && string.IsNullOrEmpty(strValue))
                    {
                        var displayName = string.IsNullOrEmpty(field.Label) ? fieldName : $"{field.Label}（{fieldName}）";
                        return $"字段 {displayName} 为必填项，不能为空";
                    }

                    // 检查数组是否为空
                    if (value is System.Collections.ICollection collection && collection.Count == 0)
                    {
                        var displayName = string.IsNullOrEmpty(field.Label) ? fieldName : $"{field.Label}（{fieldName}）";
                        return $"字段 {displayName} 为必填项，不能为空";
                    }
                }
            }

            return null;
        }
        catch
        {
            return null;
        }
    }
}
