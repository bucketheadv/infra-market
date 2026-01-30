using System.Text.Json;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;
using Microsoft.Extensions.Logging;

namespace InfraMarket.Server.Services;

public class ActivityTemplateService(
    IActivityTemplateRepository templateRepository,
    ILogger<ActivityTemplateService> logger) : IActivityTemplateService
{
    public async Task<ApiData<PageResult<ActivityTemplateDto>>> GetActivityTemplatesAsync(ActivityTemplateQueryDto query)
    {
        try
        {
            var (templates, total) = await templateRepository.PageAsync(query);

            // 转换为DTO
            var templateDtos = templates.Select(t => ConvertTemplateToDto(t)).ToList();

            var result = new PageResult<ActivityTemplateDto>
            {
                Records = templateDtos,
                Total = total,
                Page = query.Page ?? 1,
                Size = query.Size ?? 10
            };

            return ApiData<PageResult<ActivityTemplateDto>>.Success(result);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动模板列表失败");
            return ApiData<PageResult<ActivityTemplateDto>>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<List<ActivityTemplateDto>>> GetAllActivityTemplatesAsync()
    {
        try
        {
            var templates = await templateRepository.ListAsync();

            // 转换为DTO
            var templateDtos = templates.Select(t => ConvertTemplateToDto(t)).ToList();

            return ApiData<List<ActivityTemplateDto>>.Success(templateDtos);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取所有活动模板失败");
            return ApiData<List<ActivityTemplateDto>>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityTemplateDto>> GetActivityTemplateAsync(ulong id)
    {
        try
        {
            var template = await templateRepository.FindByIdAsync(id);
            if (template == null)
            {
                logger.LogWarning("获取活动模板详情失败，模板ID: {TemplateId}，模板不存在", id);
                return ApiData<ActivityTemplateDto>.Error("活动模板不存在", 404);
            }

            var templateDto = ConvertTemplateToDto(template);
            return ApiData<ActivityTemplateDto>.Success(templateDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "获取活动模板详情失败，模板ID: {TemplateId}", id);
            return ApiData<ActivityTemplateDto>.Error("查询失败", 500);
        }
    }

    public async Task<ApiData<ActivityTemplateDto>> CreateActivityTemplateAsync(ActivityTemplateFormDto dto)
    {
        try
        {
            // 序列化字段配置
            var fieldsJson = SerializeFields(dto.Fields);

            var template = new ActivityTemplate
            {
                Name = dto.Name,
                Description = dto.Description,
                Fields = fieldsJson,
                Status = dto.Status ?? 1
            };

            await templateRepository.CreateAsync(template);

            var templateDto = ConvertTemplateToDto(template);
            return ApiData<ActivityTemplateDto>.Success(templateDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "创建活动模板失败");
            return ApiData<ActivityTemplateDto>.Error("创建活动模板失败", 500);
        }
    }

    public async Task<ApiData<ActivityTemplateDto>> UpdateActivityTemplateAsync(ulong id, ActivityTemplateFormDto dto)
    {
        try
        {
            var template = await templateRepository.FindByIdAsync(id);
            if (template == null)
            {
                logger.LogWarning("更新活动模板失败，模板ID: {TemplateId}，模板不存在", id);
                return ApiData<ActivityTemplateDto>.Error("活动模板不存在", 404);
            }

            // 序列化字段配置
            string? fieldsJson = null;
            if (dto.Fields != null)
            {
                fieldsJson = SerializeFields(dto.Fields);
            }

            template.Name = dto.Name;
            if (dto.Description != null)
            {
                template.Description = dto.Description;
            }
            if (fieldsJson != null)
            {
                template.Fields = fieldsJson;
            }
            if (dto.Status.HasValue)
            {
                template.Status = dto.Status.Value;
            }

            await templateRepository.UpdateAsync(template);

            var templateDto = ConvertTemplateToDto(template);
            return ApiData<ActivityTemplateDto>.Success(templateDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动模板失败，模板ID: {TemplateId}", id);
            return ApiData<ActivityTemplateDto>.Error("更新活动模板失败", 500);
        }
    }

    public async Task<ApiData<object>> DeleteActivityTemplateAsync(ulong id)
    {
        try
        {
            var template = await templateRepository.FindByIdAsync(id);
            if (template == null)
            {
                logger.LogWarning("删除活动模板失败，模板ID: {TemplateId}，模板不存在", id);
                return ApiData<object>.Error("活动模板不存在", 404);
            }

            await templateRepository.DeleteAsync(id);
            return ApiData<object>.Success(null!);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "删除活动模板失败，模板ID: {TemplateId}", id);
            return ApiData<object>.Error("删除活动模板失败", 500);
        }
    }

    public async Task<ApiData<ActivityTemplateDto>> UpdateActivityTemplateStatusAsync(ulong id, int status)
    {
        try
        {
            var template = await templateRepository.FindByIdAsync(id);
            if (template == null)
            {
                logger.LogWarning("更新活动模板状态失败，模板ID: {TemplateId}，模板不存在", id);
                return ApiData<ActivityTemplateDto>.Error("活动模板不存在", 404);
            }

            template.Status = status;
            await templateRepository.UpdateAsync(template);

            var templateDto = ConvertTemplateToDto(template);
            return ApiData<ActivityTemplateDto>.Success(templateDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "更新活动模板状态失败，模板ID: {TemplateId}，状态: {Status}", id, status);
            return ApiData<ActivityTemplateDto>.Error("更新状态失败", 500);
        }
    }

    public async Task<ApiData<ActivityTemplateDto>> CopyActivityTemplateAsync(ulong id)
    {
        try
        {
            var template = await templateRepository.FindByIdAsync(id);
            if (template == null)
            {
                logger.LogWarning("复制活动模板失败，模板ID: {TemplateId}，模板不存在", id);
                return ApiData<ActivityTemplateDto>.Error("活动模板不存在", 404);
            }

            var newTemplate = new ActivityTemplate
            {
                Name = template.Name + "_副本",
                Description = template.Description,
                Fields = template.Fields,
                Status = 0
            };

            await templateRepository.CreateAsync(newTemplate);

            var templateDto = ConvertTemplateToDto(newTemplate);
            return ApiData<ActivityTemplateDto>.Success(templateDto);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "复制活动模板失败，模板ID: {TemplateId}", id);
            return ApiData<ActivityTemplateDto>.Error("复制活动模板失败", 500);
        }
    }

    private static ActivityTemplateDto ConvertTemplateToDto(ActivityTemplate template)
    {
        var fields = ParseFields(template.Fields);

        return new ActivityTemplateDto
        {
            Id = template.Id,
            Name = template.Name,
            Description = template.Description,
            Fields = fields,
            Status = template.Status,
            CreateTime = DateTimeUtil.FormatLocalDateTime(template.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(template.UpdateTime)
        };
    }

    private static string? SerializeFields(List<ActivityTemplateFieldDto>? fields)
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

    private static List<ActivityTemplateFieldDto>? ParseFields(string? fieldsJson)
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
            return JsonSerializer.Deserialize<List<ActivityTemplateFieldDto>>(fieldsJson, options);
        }
        catch
        {
            return null;
        }
    }
}
