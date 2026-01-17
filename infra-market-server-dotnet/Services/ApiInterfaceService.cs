using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using RestSharp;

namespace InfraMarket.Server.Services;

public class ApiInterfaceService(
    IApiInterfaceRepository apiInterfaceRepository,
    IApiInterfaceExecutionRecordRepository executionRecordRepository,
    IUserRepository userRepository,
    ILogger<ApiInterfaceService> logger) : IApiInterfaceService
{
    public async Task<ApiData<PageResult<ApiInterfaceDto>>> ListAsync(ApiInterfaceQueryDto query)
    {
        var (interfaces, total) = await apiInterfaceRepository.PageAsync(query);
        var interfaceDtos = interfaces.Select(ConvertToDto).ToList();

        var result = new PageResult<ApiInterfaceDto>
        {
            Records = interfaceDtos,
            Total = total,
            Page = query.Page ?? 1,
            Size = query.Size ?? 10
        };

        return ApiData<PageResult<ApiInterfaceDto>>.Success(result);
    }

    public async Task<ApiData<List<ApiInterfaceDto>>> GetMostUsedAsync(int days, int limit)
    {
        var interfaceIds = await executionRecordRepository.FindMostUsedInterfaceIDsAsync(days, limit);
        if (interfaceIds.Count == 0)
        {
            return ApiData<List<ApiInterfaceDto>>.Success([]);
        }

        var interfaces = await apiInterfaceRepository.FindByIDsAsync(interfaceIds);
        var interfaceMap = interfaces.ToDictionary(i => i.Id);

        var result = interfaceIds
            .Where(id => interfaceMap.ContainsKey(id))
            .Select(id => ConvertToDto(interfaceMap[id]))
            .ToList();

        return ApiData<List<ApiInterfaceDto>>.Success(result);
    }

    public async Task<ApiData<ApiInterfaceDto>> DetailAsync(ulong id)
    {
        var apiInterface = await apiInterfaceRepository.FindByIdAsync(id);
        if (apiInterface == null)
        {
            return ApiData<ApiInterfaceDto>.Error("接口不存在", 404);
        }

        var dto = ConvertToDto(apiInterface);
        return ApiData<ApiInterfaceDto>.Success(dto);
    }

    public async Task<ApiData<ApiInterfaceDto>> CreateAsync(ApiInterfaceFormDto dto)
    {
        var newInterface = new ApiInterface
        {
            Name = dto.Name ?? string.Empty,
            Method = dto.Method ?? string.Empty,
            Url = dto.Url ?? string.Empty,
            Description = dto.Description,
            PostType = dto.PostType,
            Params = SerializeParams(dto.UrlParams, dto.HeaderParams, dto.BodyParams),
            Status = 1,
            Environment = dto.Environment,
            Timeout = dto.Timeout,
            ValuePath = dto.ValuePath
        };

        await apiInterfaceRepository.CreateAsync(newInterface);
        var result = ConvertToDto(newInterface);
        return ApiData<ApiInterfaceDto>.Success(result);
    }

    public async Task<ApiData<ApiInterfaceDto>> UpdateAsync(ulong id, ApiInterfaceFormDto dto)
    {
        var apiInterface = await apiInterfaceRepository.FindByIdAsync(id);
        if (apiInterface == null)
        {
            return ApiData<ApiInterfaceDto>.Error("接口不存在", 404);
        }

        apiInterface.Name = dto.Name ?? apiInterface.Name;
        apiInterface.Method = dto.Method ?? apiInterface.Method;
        apiInterface.Url = dto.Url ?? apiInterface.Url;
        apiInterface.Description = dto.Description;
        apiInterface.PostType = dto.PostType;
        apiInterface.Params = SerializeParams(dto.UrlParams, dto.HeaderParams, dto.BodyParams);
        apiInterface.Environment = dto.Environment;
        apiInterface.Timeout = dto.Timeout;
        apiInterface.ValuePath = dto.ValuePath;

        await apiInterfaceRepository.UpdateAsync(apiInterface);
        var result = ConvertToDto(apiInterface);
        return ApiData<ApiInterfaceDto>.Success(result);
    }

    public async Task<ApiData<object>> DeleteAsync(ulong id)
    {
        await apiInterfaceRepository.DeleteAsync(id);
        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> UpdateStatusAsync(ulong id, int status)
    {
        var apiInterface = await apiInterfaceRepository.FindByIdAsync(id);
        if (apiInterface == null)
        {
            return ApiData<object>.Error("接口不存在", 404);
        }

        apiInterface.Status = status;
        await apiInterfaceRepository.UpdateAsync(apiInterface);
        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<ApiInterfaceDto>> CopyAsync(ulong id)
    {
        var sourceInterface = await apiInterfaceRepository.FindByIdAsync(id);
        if (sourceInterface == null)
        {
            return ApiData<ApiInterfaceDto>.Error("接口不存在", 404);
        }

        var copy = new ApiInterface
        {
            Name = $"{sourceInterface.Name}_副本",
            Method = sourceInterface.Method,
            Url = sourceInterface.Url,
            Description = sourceInterface.Description,
            PostType = sourceInterface.PostType,
            Params = sourceInterface.Params,
            Status = 1,
            Environment = sourceInterface.Environment,
            Timeout = sourceInterface.Timeout,
            ValuePath = sourceInterface.ValuePath
        };

        await apiInterfaceRepository.CreateAsync(copy);
        var result = ConvertToDto(copy);
        return ApiData<ApiInterfaceDto>.Success(result);
    }

    public async Task<ApiData<ApiExecuteResponseDto>> ExecuteAsync(ApiExecuteRequestDto request, ulong executorId, string clientIp, string userAgent)
    {
        var apiInterface = await apiInterfaceRepository.FindByIdAsync(request.InterfaceId!.Value);
        if (apiInterface == null)
        {
            return ApiData<ApiExecuteResponseDto>.Error("接口不存在", 404);
        }

        var user = await userRepository.FindByUidAsync(executorId);
        var executorName = user?.Username ?? "未知用户";

        var startTime = DateTimeOffset.UtcNow;
        var response = await ExecuteHttpRequestAsync(apiInterface, request);
        var endTime = DateTimeOffset.UtcNow;
        var responseTime = (endTime - startTime).TotalMilliseconds;

        // 保存执行记录
        var record = new ApiInterfaceExecutionRecord
        {
            InterfaceId = apiInterface.Id,
            ExecutorId = executorId,
            ExecutorName = executorName,
            RequestParams = SerializeToJsonObject(request.UrlParams),
            RequestHeaders = SerializeToJsonObject(request.Headers),
            RequestBody = SerializeToJsonObject(request.BodyParams),
            ResponseStatus = response.Status,
            ResponseHeaders = SerializeToJsonObject(response.Headers),
            ResponseBody = response.Body,
            ExecutionTime = (long)responseTime,
            Success = response.Success,
            ErrorMessage = response.Error,
            Remark = request.Remark,
            ClientIp = clientIp,
            UserAgent = userAgent
        };

        await executionRecordRepository.CreateAsync(record);

        return ApiData<ApiExecuteResponseDto>.Success(response);
    }

    private async Task<ApiExecuteResponseDto> ExecuteHttpRequestAsync(ApiInterface apiInterface, ApiExecuteRequestDto request)
    {
        // 构建URL
        var finalUrl = apiInterface.Url;
        if (request.UrlParams is { Count: > 0 })
        {
            var queryParams = (from param in request.UrlParams let value = ConvertJsonElement(param.Value)?.ToString() ?? "" where !string.IsNullOrEmpty(value) select $"{Uri.EscapeDataString(param.Key)}={Uri.EscapeDataString(value)}").ToList();
            if (queryParams.Count > 0)
            {
                var separator = finalUrl.Contains('?') ? "&" : "?";
                finalUrl += separator + string.Join("&", queryParams);
            }
        }

        // 设置超时时间
        var timeoutSeconds = 60L;
        if (request.Timeout != null)
        {
            timeoutSeconds = request.Timeout.Value;
        }
        else if (apiInterface.Timeout != null)
        {
            timeoutSeconds = apiInterface.Timeout.Value;
        }

        // 创建HTTP客户端
        var options = new RestClientOptions
        {
            Timeout = TimeSpan.FromSeconds(timeoutSeconds)
        };
        var client = new RestClient(options);

        // 设置请求头
        var restRequest = new RestRequest(finalUrl, GetMethod(apiInterface.Method));
        if (request.Headers is { Count: > 0 })
        {
            foreach (var header in request.Headers)
            {
                restRequest.AddHeader(header.Key, header.Value);
            }
        }

        // 设置请求体
        if (apiInterface.Method != "GET" && request.BodyParams is { Count: > 0 })
        {
            var postType = apiInterface.PostType ?? "application/json";
            if (postType == "application/x-www-form-urlencoded")
            {
                // 表单格式
                var formData = (from param in request.BodyParams let value = ConvertJsonElement(param.Value)?.ToString() ?? "" where !string.IsNullOrEmpty(value) select $"{Uri.EscapeDataString(param.Key)}={Uri.EscapeDataString(value)}").ToList();
                var bodyString = string.Join("&", formData);
                restRequest.AddStringBody(bodyString, "application/x-www-form-urlencoded");
            }
            else
            {
                // JSON格式
                var jsonData = JsonConvert.SerializeObject(request.BodyParams);
                restRequest.AddStringBody(jsonData, "application/json");
            }
        }

        // 执行请求
        var startTime = DateTimeOffset.UtcNow;
        RestResponse response;
        try
        {
            response = await client.ExecuteAsync(restRequest);
        }
        catch (Exception ex)
        {
            var errorEndTime = DateTimeOffset.UtcNow;
            var errorResponseTime = (errorEndTime - startTime).TotalMilliseconds;
            logger.LogError(ex, "执行HTTP请求失败: {Url}", finalUrl);
            return new ApiExecuteResponseDto
            {
                Status = 0,
                ResponseTime = (long)errorResponseTime,
                Success = false,
                Error = ex.Message
            };
        }

        var endTime = DateTimeOffset.UtcNow;
        var responseTime = (endTime - startTime).TotalMilliseconds;

        // 构建响应
        var responseHeaders = new Dictionary<string, string>();
        if (response.Headers != null)
        {
            foreach (var header in response.Headers)
            {
                if (string.IsNullOrEmpty(header.Name)) continue;
                var headerValue = header.Value;
                if (!string.IsNullOrEmpty(headerValue))
                {
                    responseHeaders[header.Name] = headerValue;
                }
            }
        }
        
        if (response.ContentHeaders != null)
        {
            foreach (var header in response.ContentHeaders)
            {
                if (string.IsNullOrEmpty(header.Name)) continue;
                // 处理不同类型的Value：可能是IEnumerable<string>或其他类型
                string? headerValue = null;
                object valueObj = header.Value;
                    
                // 先检查是否为string类型
                if (valueObj is string strValue)
                {
                    headerValue = strValue;
                }
                // 然后检查是否为IEnumerable<string>（排除string，因为string实现了IEnumerable<char>）
                else if (valueObj.GetType() != typeof(string) && valueObj is IEnumerable<string> stringValues)
                {
                    var valuesList = stringValues.ToList();
                    if (valuesList.Count > 0)
                    {
                        headerValue = valuesList[0];
                    }
                }
                else
                {
                    headerValue = valueObj.ToString();
                }
                    
                if (!string.IsNullOrEmpty(headerValue))
                {
                    responseHeaders[header.Name] = headerValue;
                }
            }
        }

        var body = response.Content ?? "";

        // 提取值（如果配置了valuePath）
        string? extractedValue = null;
        if (!string.IsNullOrEmpty(apiInterface.ValuePath) && !string.IsNullOrEmpty(body))
        {
            extractedValue = ExtractValueByPath(body, apiInterface.ValuePath);
        }

        var result = new ApiExecuteResponseDto
        {
            Status = (int)response.StatusCode,
            Headers = responseHeaders,
            Body = body,
            ExtractedValue = extractedValue,
            ResponseTime = (long)responseTime,
            Success = response.IsSuccessStatusCode
        };

        if (!response.IsSuccessStatusCode)
        {
            result.Error = body;
        }

        return result;
    }

    private static Method GetMethod(string method)
    {
        return method.ToUpper() switch
        {
            "GET" => Method.Get,
            "POST" => Method.Post,
            "PUT" => Method.Put,
            "DELETE" => Method.Delete,
            "PATCH" => Method.Patch,
            _ => Method.Get
        };
    }

    private static string? SerializeParams(List<ApiParamDto>? urlParams, List<ApiParamDto>? headerParams, List<ApiParamDto>? bodyParams)
    {
        var allParams = new List<ApiParamDto>();
        allParams.AddRange(urlParams ?? []);
        allParams.AddRange(headerParams ?? []);
        allParams.AddRange(bodyParams ?? []);

        return allParams.Count == 0 ? null : JsonConvert.SerializeObject(allParams);
    }

    private static ApiInterfaceDto ConvertToDto(ApiInterface apiInterface)
    {
        var dto = new ApiInterfaceDto
        {
            Id = apiInterface.Id,
            Name = apiInterface.Name,
            Method = apiInterface.Method,
            Url = apiInterface.Url,
            Description = apiInterface.Description,
            Status = apiInterface.Status,
            PostType = apiInterface.PostType,
            Environment = apiInterface.Environment,
            Timeout = apiInterface.Timeout,
            ValuePath = apiInterface.ValuePath,
            CreateTime = DateTimeUtil.FormatLocalDateTime(apiInterface.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(apiInterface.UpdateTime)
        };

        // 解析参数 - 与 Go 版本保持一致：存储格式是数组，需要根据 ParamType 过滤
        if (string.IsNullOrEmpty(apiInterface.Params)) return dto;
        var allParams = TryDeserializeParams(apiInterface.Params);
        if (allParams == null) return dto;
        dto.UrlParams = allParams.Where(p => p.ParamType == "URL_PARAM").ToList();
        dto.HeaderParams = allParams.Where(p => p.ParamType == "HEADER_PARAM").ToList();
        dto.BodyParams = allParams.Where(p => p.ParamType == "BODY_PARAM").ToList();

        return dto;
    }

    private static string SerializeToJsonObject<T>(Dictionary<string, T>? dict)
    {
        if (dict == null) return "{}";
        
        try
        {
            // 处理 JsonElement：将 System.Text.Json 的 JsonElement 转换为实际值
            var processedDict = new Dictionary<string, object?>();
            foreach (var kvp in dict)
            {
                processedDict[kvp.Key] = ConvertJsonElement(kvp.Value);
            }
            
            var json = JsonConvert.SerializeObject(processedDict);
            return string.IsNullOrWhiteSpace(json) || json is "null" or "[]" ? "{}" : json;
        }
        catch
        {
            return "{}";
        }
    }

    private static object? ConvertJsonElement(object? value)
    {
        if (value == null) return null;
        
        // 如果是 JsonElement（System.Text.Json），需要转换为实际值
        var type = value.GetType();
        if (type.Name == "JsonElement")
        {
            try
            {
                // 使用 System.Text.Json 序列化 JsonElement，然后使用 Newtonsoft.Json 反序列化
                var jsonBytes = System.Text.Json.JsonSerializer.SerializeToUtf8Bytes(value);
                var jsonString = System.Text.Encoding.UTF8.GetString(jsonBytes);
                return JsonConvert.DeserializeObject<object>(jsonString);
            }
            catch
            {
                // 如果转换失败，返回字符串表示
                return value.ToString();
            }
        }

        switch (value)
        {
            // 如果是字典或列表，递归处理
            case System.Collections.IDictionary dict:
            {
                var result = new Dictionary<string, object?>();
                foreach (System.Collections.DictionaryEntry entry in dict)
                {
                    var key = entry.Key.ToString() ?? "";
                    result[key] = ConvertJsonElement(entry.Value);
                }
                return result;
            }
            case System.Collections.IEnumerable enumerable and not string:
            {
                return (from object? item in enumerable select ConvertJsonElement(item)).ToList();
            }
            default:
                return value;
        }
    }

    private static List<ApiParamDto>? TryDeserializeParams(string? paramsJson)
    {
        if (string.IsNullOrWhiteSpace(paramsJson)) return null;

        // 尝试解析为数组格式（新格式，与 Go 版本一致）
        try
        {
            return JsonConvert.DeserializeObject<List<ApiParamDto>>(paramsJson);
        }
        catch
        {
            // 兼容旧的对象格式
            try
            {
                var paramsObj = JsonConvert.DeserializeObject<dynamic>(paramsJson);
                if (paramsObj == null) return null;

                var allParams = new List<ApiParamDto>();
                var urlParams = JsonConvert.DeserializeObject<List<ApiParamDto>>(paramsObj.urlParams?.ToString() ?? "[]");
                var headerParams = JsonConvert.DeserializeObject<List<ApiParamDto>>(paramsObj.headerParams?.ToString() ?? "[]");
                var bodyParams = JsonConvert.DeserializeObject<List<ApiParamDto>>(paramsObj.bodyParams?.ToString() ?? "[]");
                
                if (urlParams != null) allParams.AddRange(urlParams);
                if (headerParams != null) allParams.AddRange(headerParams);
                if (bodyParams != null) allParams.AddRange(bodyParams);
                
                return allParams;
            }
            catch
            {
                return null;
            }
        }
    }

    /// <summary>
    /// 根据JSONPath提取值（与Go版本逻辑一致）
    /// </summary>
    private static string? ExtractValueByPath(string jsonString, string path)
    {
        if (string.IsNullOrWhiteSpace(jsonString) || string.IsNullOrWhiteSpace(path))
        {
            return null;
        }

        // 先将JSON字符串解析为对象
        object? data;
        try
        {
            data = JsonConvert.DeserializeObject<object>(jsonString);
            if (data == null)
            {
                return null;
            }
        }
        catch
        {
            // JSON解析失败，返回null（与Go版本行为一致）
            return null;
        }

        // 使用JSONPath提取值
        try
        {
            // 将数据转换为JToken以便使用SelectToken
            JToken? token;
            if (data is JToken jToken)
            {
                token = jToken;
            }
            else
            {
                token = JToken.FromObject(data);
            }

            // 处理JSONPath格式：去掉开头的 $ 或 $.
            var jsonPath = path.Trim();
            if (jsonPath.StartsWith("$."))
            {
                jsonPath = jsonPath.Substring(2);
            }
            else if (jsonPath.StartsWith("$"))
            {
                jsonPath = jsonPath.Substring(1);
                if (jsonPath.StartsWith("."))
                {
                    jsonPath = jsonPath.Substring(1);
                }
            }

            // 使用SelectToken提取值（支持JSONPath格式）
            var result = token.SelectToken(jsonPath);
            if (result == null)
            {
                return null;
            }

            // 将结果转换为字符串（与Go版本逻辑一致）
            return result.Type switch
            {
                JTokenType.String => result.ToString(),
                JTokenType.Integer or JTokenType.Float => result.ToString(),
                JTokenType.Boolean => result.ToString(),
                JTokenType.Object or JTokenType.Array => JsonConvert.SerializeObject(result),
                _ => result.ToString()
            };
        }
        catch
        {
            // JSONPath提取失败，返回null（与Go版本行为一致）
            return null;
        }
    }
}
