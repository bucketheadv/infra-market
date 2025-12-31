package service

import (
	"encoding/json"
	"fmt"
	"log"
	"net/http"
	"net/url"
	"strings"
	"time"

	"github.com/PaesslerAG/jsonpath"
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/entity"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/util"
	"github.com/go-resty/resty/v2"
)

type ApiInterfaceService struct {
	apiInterfaceRepo                *repository.ApiInterfaceRepository
	apiInterfaceExecutionRecordRepo *repository.ApiInterfaceExecutionRecordRepository
	userRepo                        *repository.UserRepository
}

func NewApiInterfaceService(
	apiInterfaceRepo *repository.ApiInterfaceRepository,
	apiInterfaceExecutionRecordRepo *repository.ApiInterfaceExecutionRecordRepository,
	userRepo *repository.UserRepository,
) *ApiInterfaceService {
	return &ApiInterfaceService{
		apiInterfaceRepo:                apiInterfaceRepo,
		apiInterfaceExecutionRecordRepo: apiInterfaceExecutionRecordRepo,
		userRepo:                        userRepo,
	}
}

// FindPage 分页查询接口
func (s *ApiInterfaceService) FindPage(query dto.ApiInterfaceQueryDto) dto.ApiData[dto.PageResult[dto.ApiInterfaceDto]] {
	interfaces, total, err := s.apiInterfaceRepo.Page(query)
	if err != nil {
		return dto.Error[dto.PageResult[dto.ApiInterfaceDto]]("查询失败", http.StatusInternalServerError)
	}

	interfaceDtos := make([]dto.ApiInterfaceDto, len(interfaces))
	for i, iface := range interfaces {
		interfaceDtos[i] = s.convertToDto(&iface)
	}

	result := dto.PageResult[dto.ApiInterfaceDto]{
		Records: interfaceDtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

// FindMostUsedInterfaces 获取最近最热门的接口
func (s *ApiInterfaceService) FindMostUsedInterfaces(days, limit int) dto.ApiData[[]dto.ApiInterfaceDto] {
	// 查询最近使用最多的接口ID
	interfaceIDs, err := s.apiInterfaceExecutionRecordRepo.FindMostUsedInterfaceIDs(days, limit)
	if err != nil {
		return dto.Error[[]dto.ApiInterfaceDto]("查询失败", http.StatusInternalServerError)
	}

	if len(interfaceIDs) == 0 {
		return dto.Success([]dto.ApiInterfaceDto{})
	}

	// 根据ID列表查询接口详情
	interfaces, err := s.apiInterfaceRepo.FindByIDs(interfaceIDs)
	if err != nil {
		return dto.Error[[]dto.ApiInterfaceDto]("查询接口详情失败", http.StatusInternalServerError)
	}

	// 按照interfaceIDs的顺序排序结果
	interfaceMap := make(map[uint64]entity.ApiInterface)
	for _, apiInterface := range interfaces {
		interfaceMap[apiInterface.ID] = apiInterface
	}

	result := make([]dto.ApiInterfaceDto, 0, len(interfaceIDs))
	for _, id := range interfaceIDs {
		if apiInterface, ok := interfaceMap[id]; ok {
			result = append(result, s.convertToDto(&apiInterface))
		}
	}

	return dto.Success(result)
}

// FindByID 根据ID查询
func (s *ApiInterfaceService) FindByID(id uint64) dto.ApiData[dto.ApiInterfaceDto] {
	apiInterface, err := s.apiInterfaceRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.ApiInterfaceDto]("接口不存在", http.StatusNotFound)
	}

	interfaceDto := s.convertToDto(apiInterface)
	return dto.Success(interfaceDto)
}

// Save 保存接口
func (s *ApiInterfaceService) Save(form dto.ApiInterfaceFormDto) dto.ApiData[dto.ApiInterfaceDto] {
	// 验证POST类型
	if err := s.validatePostType(&form); err != nil {
		return dto.Error[dto.ApiInterfaceDto](err.Error(), http.StatusBadRequest)
	}

	apiInterface := s.convertToEntity(&form)
	now := time.Now().UnixMilli()
	apiInterface.CreateTime = now
	apiInterface.UpdateTime = now
	status := 1
	apiInterface.Status = &status

	if err := s.apiInterfaceRepo.Create(apiInterface); err != nil {
		name := ""
		if form.Name != nil {
			name = *form.Name
		}
		log.Printf("创建接口失败，接口名称: %s, 错误: %v\n", name, err)
		return dto.Error[dto.ApiInterfaceDto]("创建接口失败", http.StatusInternalServerError)
	}

	interfaceDto := s.convertToDto(apiInterface)
	return dto.Success(interfaceDto)
}

// Update 更新接口
func (s *ApiInterfaceService) Update(id uint64, form dto.ApiInterfaceFormDto) dto.ApiData[dto.ApiInterfaceDto] {
	existing, err := s.apiInterfaceRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.ApiInterfaceDto]("接口不存在", http.StatusNotFound)
	}

	// 验证POST类型
	if err := s.validatePostType(&form); err != nil {
		return dto.Error[dto.ApiInterfaceDto](err.Error(), http.StatusBadRequest)
	}

	apiInterface := s.convertToEntity(&form)
	apiInterface.ID = existing.ID
	apiInterface.CreateTime = existing.CreateTime
	apiInterface.UpdateTime = time.Now().UnixMilli()
	apiInterface.Status = existing.Status

	if err := s.apiInterfaceRepo.Update(apiInterface); err != nil {
		log.Printf("更新接口失败，接口ID: %d, 错误: %v\n", id, err)
		return dto.Error[dto.ApiInterfaceDto]("更新接口失败", http.StatusInternalServerError)
	}

	interfaceDto := s.convertToDto(apiInterface)
	return dto.Success(interfaceDto)
}

// Delete 删除接口
func (s *ApiInterfaceService) Delete(id uint64) dto.ApiData[any] {
	if err := s.apiInterfaceRepo.Delete(id); err != nil {
		log.Printf("删除接口失败，接口ID: %d, 错误: %v\n", id, err)
		return dto.Error[any]("删除接口失败", http.StatusInternalServerError)
	}
	return dto.Success[any](nil)
}

// UpdateStatus 更新接口状态
func (s *ApiInterfaceService) UpdateStatus(id uint64, status int) dto.ApiData[dto.ApiInterfaceDto] {
	apiInterface, err := s.apiInterfaceRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.ApiInterfaceDto]("接口不存在", http.StatusNotFound)
	}

	apiInterface.Status = &status
	apiInterface.UpdateTime = time.Now().UnixMilli()

	if err := s.apiInterfaceRepo.Update(apiInterface); err != nil {
		log.Printf("更新接口状态失败，接口ID: %d, 状态: %d, 错误: %v\n", id, status, err)
		return dto.Error[dto.ApiInterfaceDto]("更新状态失败", http.StatusInternalServerError)
	}

	interfaceDto := s.convertToDto(apiInterface)
	return dto.Success(interfaceDto)
}

// Copy 复制接口
func (s *ApiInterfaceService) Copy(id uint64) dto.ApiData[dto.ApiInterfaceDto] {
	existing, err := s.apiInterfaceRepo.FindByID(id)
	if err != nil {
		return dto.Error[dto.ApiInterfaceDto]("接口不存在", http.StatusNotFound)
	}

	newInterface := *existing
	newInterface.ID = 0
	if existing.Name != "" {
		newInterface.Name = existing.Name + "_副本"
	}
	now := time.Now().UnixMilli()
	newInterface.CreateTime = now
	newInterface.UpdateTime = now
	status := 1
	newInterface.Status = &status

	if err := s.apiInterfaceRepo.Create(&newInterface); err != nil {
		return dto.Error[dto.ApiInterfaceDto]("复制接口失败", http.StatusInternalServerError)
	}

	interfaceDto := s.convertToDto(&newInterface)
	return dto.Success(interfaceDto)
}

// Execute 执行接口
func (s *ApiInterfaceService) Execute(req dto.ApiExecuteRequestDto, executorID uint64, clientIP, userAgent string) dto.ApiData[dto.ApiExecuteResponseDto] {
	startTime := time.Now()

	// 获取执行人姓名
	executorName := "未知用户"
	if user, err := s.userRepo.FindByUID(executorID); err == nil {
		executorName = user.Username
	}

	// 获取接口信息
	if req.InterfaceID == nil {
		return dto.Error[dto.ApiExecuteResponseDto]("接口ID不能为空", http.StatusBadRequest)
	}

	apiInterface, err := s.apiInterfaceRepo.FindByID(*req.InterfaceID)
	if err != nil {
		log.Printf("执行接口失败，接口ID: %d, 错误: %v\n", *req.InterfaceID, err)
		return dto.Error[dto.ApiExecuteResponseDto]("接口不存在", http.StatusNotFound)
	}

	// 检查接口状态
	if apiInterface.Status == nil || *apiInterface.Status != 1 {
		responseTime := time.Since(startTime).Milliseconds()
		return dto.Success(dto.ApiExecuteResponseDto{
			Status:       403,
			Success:      false,
			Error:        stringPtr("接口已禁用，无法执行"),
			ResponseTime: responseTime,
		})
	}

	// 处理参数值：JSON_OBJECT 类型需要解析为对象
	processedReq := s.processParams(apiInterface, &req)

	// 验证必填参数
	if err := s.validateRequiredParams(apiInterface, processedReq); err != nil {
		responseTime := time.Since(startTime).Milliseconds()
		return dto.Success(dto.ApiExecuteResponseDto{
			Status:       http.StatusBadRequest,
			Success:      false,
			Error:        stringPtr(err.Error()),
			ResponseTime: responseTime,
		})
	}

	// 执行HTTP请求
	response, err := s.executeHTTPRequest(apiInterface, processedReq)
	responseTime := time.Since(startTime).Milliseconds()

	if err != nil {
		// 记录失败的执行记录
		s.saveExecutionRecord(*processedReq.InterfaceID, &executorID, executorName, processedReq, &dto.ApiExecuteResponseDto{
			Status:       http.StatusInternalServerError,
			Success:      false,
			Error:        stringPtr(err.Error()),
			ResponseTime: responseTime,
		}, clientIP, userAgent, req.Remark)

		return dto.Success(dto.ApiExecuteResponseDto{
			Status:       http.StatusInternalServerError,
			Success:      false,
			Error:        stringPtr(err.Error()),
			ResponseTime: responseTime,
		})
	}

	// 提取值（如果配置了valuePath）
	if apiInterface.ValuePath != nil && *apiInterface.ValuePath != "" && response.Body != nil {
		extractedValue := s.extractValueByPath(*response.Body, *apiInterface.ValuePath)
		response.ExtractedValue = extractedValue
	}

	// 记录执行记录
	s.saveExecutionRecord(*req.InterfaceID, &executorID, executorName, processedReq, response, clientIP, userAgent, req.Remark)

	return dto.Success(*response)
}

// executeHTTPRequest 执行HTTP请求
func (s *ApiInterfaceService) executeHTTPRequest(apiInterface *entity.ApiInterface, req *dto.ApiExecuteRequestDto) (*dto.ApiExecuteResponseDto, error) {
	// 构建URL
	finalURL := apiInterface.URL
	if req.URLParams != nil && len(req.URLParams) > 0 {
		params := url.Values{}
		for k, v := range req.URLParams {
			if v != nil {
				params.Add(k, fmt.Sprintf("%v", v))
			}
		}
		if len(params) > 0 {
			separator := "?"
			if strings.Contains(finalURL, "?") {
				separator = "&"
			}
			finalURL += separator + params.Encode()
		}
	}

	// 创建HTTP客户端
	client := resty.New()

	// 设置超时时间
	timeoutSeconds := int64(60)
	if req.Timeout != nil {
		timeoutSeconds = *req.Timeout
	} else if apiInterface.Timeout != nil {
		timeoutSeconds = *apiInterface.Timeout
	}
	client.SetTimeout(time.Duration(timeoutSeconds) * time.Second)

	// 设置请求头
	headers := make(map[string]string)
	if req.Headers != nil {
		for k, v := range req.Headers {
			headers[k] = v
		}
	}

	// 构建请求
	request := client.R().SetHeaders(headers)

	// 设置请求体
	var body string
	if apiInterface.Method != "GET" && req.BodyParams != nil && len(req.BodyParams) > 0 {
		postType := "application/json"
		if apiInterface.PostType != nil {
			postType = *apiInterface.PostType
		}

		if postType == "application/x-www-form-urlencoded" {
			// 表单格式
			formData := url.Values{}
			for k, v := range req.BodyParams {
				if v != nil {
					formData.Add(k, fmt.Sprintf("%v", v))
				}
			}
			body = formData.Encode()
			request.SetHeader("Content-Type", postType)
			request.SetBody(body)
		} else {
			// JSON格式
			jsonData, err := json.Marshal(req.BodyParams)
			if err != nil {
				return nil, fmt.Errorf("序列化请求体失败: %w", err)
			}
			body = string(jsonData)
			request.SetHeader("Content-Type", "application/json")
			request.SetBody(jsonData)
		}
	}

	// 执行请求
	var resp *resty.Response
	var err error

	method := strings.ToUpper(apiInterface.Method)
	switch method {
	case "GET":
		resp, err = request.Get(finalURL)
	case "POST":
		resp, err = request.Post(finalURL)
	case "PUT":
		resp, err = request.Put(finalURL)
	case "DELETE":
		resp, err = request.Delete(finalURL)
	case "PATCH":
		resp, err = request.Patch(finalURL)
	default:
		return nil, fmt.Errorf("不支持的HTTP方法: %s", method)
	}

	if err != nil {
		return nil, err
	}

	// 构建响应
	responseHeaders := make(map[string]string)
	for k, v := range resp.Header() {
		if len(v) > 0 {
			responseHeaders[k] = v[0]
		}
	}

	bodyStr := resp.String()
	response := &dto.ApiExecuteResponseDto{
		Status:       resp.StatusCode(),
		Headers:      responseHeaders,
		Body:         &bodyStr,
		ResponseTime: resp.Time().Milliseconds(),
		Success:      resp.IsSuccess(),
	}

	if !resp.IsSuccess() {
		errMsg := bodyStr
		response.Error = &errMsg
	}

	return response, nil
}

// extractValueByPath 根据JSONPath提取值
func (s *ApiInterfaceService) extractValueByPath(jsonString, path string) *string {
	// 先将JSON字符串解析为any
	var data any
	if err := json.Unmarshal([]byte(jsonString), &data); err != nil {
		log.Printf("JSON解析失败: %v\n", err)
		return nil
	}

	// 使用JSONPath提取值
	result, err := jsonpath.Get(path, data)
	if err != nil {
		log.Printf("JSONPath提取失败: %v, 路径: %s\n", err, path)
		return nil
	}

	// 将结果转换为字符串
	var resultStr string
	switch v := result.(type) {
	case string:
		resultStr = v
	case float64, int, int64:
		resultStr = fmt.Sprintf("%v", v)
	case bool:
		resultStr = fmt.Sprintf("%v", v)
	case []any, map[string]any:
		jsonBytes, err := json.Marshal(v)
		if err != nil {
			log.Printf("JSON序列化失败: %v\n", err)
			resultStr = fmt.Sprintf("%v", v)
		} else {
			resultStr = string(jsonBytes)
		}
	default:
		resultStr = fmt.Sprintf("%v", v)
	}

	return &resultStr
}

// saveExecutionRecord 保存执行记录
func (s *ApiInterfaceService) saveExecutionRecord(
	interfaceID uint64,
	executorID *uint64,
	executorName string,
	request *dto.ApiExecuteRequestDto,
	response *dto.ApiExecuteResponseDto,
	clientIP, userAgent string,
	remark *string,
) {
	// 序列化请求参数
	requestParamsJSON, err := json.Marshal(request.URLParams)
	if err != nil {
		log.Printf("序列化请求参数失败: %v\n", err)
		requestParamsJSON = []byte("{}")
	}
	requestHeadersJSON, err := json.Marshal(request.Headers)
	if err != nil {
		log.Printf("序列化请求头失败: %v\n", err)
		requestHeadersJSON = []byte("{}")
	}
	requestBodyJSON, err := json.Marshal(request.BodyParams)
	if err != nil {
		log.Printf("序列化请求体失败: %v\n", err)
		requestBodyJSON = []byte("{}")
	}

	// 序列化响应头
	responseHeadersJSON, err := json.Marshal(response.Headers)
	if err != nil {
		log.Printf("序列化响应头失败: %v\n", err)
		responseHeadersJSON = []byte("{}")
	}

	record := &entity.ApiInterfaceExecutionRecord{
		InterfaceID:     &interfaceID,
		ExecutorID:      executorID,
		ExecutorName:    executorName,
		RequestParams:   stringPtr(string(requestParamsJSON)),
		RequestHeaders:  stringPtr(string(requestHeadersJSON)),
		RequestBody:     stringPtr(string(requestBodyJSON)),
		ResponseStatus:  &response.Status,
		ResponseHeaders: stringPtr(string(responseHeadersJSON)),
		ResponseBody:    response.Body,
		ExecutionTime:   &response.ResponseTime,
		Success:         &response.Success,
		ErrorMessage:    response.Error,
		Remark:          remark,
		ClientIP:        &clientIP,
		UserAgent:       &userAgent,
	}

	if err := s.apiInterfaceExecutionRecordRepo.Create(record); err != nil {
		log.Printf("保存接口执行记录失败: %v\n", err)
	}
}

// validatePostType 验证POST类型
func (s *ApiInterfaceService) validatePostType(form *dto.ApiInterfaceFormDto) error {
	if form.Method == nil {
		return nil
	}

	method := strings.ToUpper(*form.Method)
	if method == "POST" || method == "PUT" || method == "PATCH" {
		if form.PostType == nil || *form.PostType == "" {
			return fmt.Errorf("POST类型为必填项")
		}
	}
	return nil
}

// validateRequiredParams 验证必填参数
func (s *ApiInterfaceService) validateRequiredParams(apiInterface *entity.ApiInterface, request *dto.ApiExecuteRequestDto) error {
	// 解析参数配置
	if apiInterface.Params == nil || *apiInterface.Params == "" {
		return nil
	}

	var params []dto.ApiParamDto
	if err := json.Unmarshal([]byte(*apiInterface.Params), &params); err != nil {
		log.Printf("解析接口参数失败: %v\n", err)
		return nil
	}

	// 验证URL参数
	for _, param := range params {
		if param.ParamType != nil && *param.ParamType == "URL_PARAM" {
			if param.Required != nil && *param.Required {
				if param.Name != nil {
					if request.URLParams == nil {
						return fmt.Errorf("URL参数 %s 为必填项，不能为空", *param.Name)
					}
					if _, exists := request.URLParams[*param.Name]; !exists {
						return fmt.Errorf("URL参数 %s 为必填项，不能为空", *param.Name)
					}
				}
			}
		}
	}

	// 验证Header参数
	for _, param := range params {
		if param.ParamType != nil && *param.ParamType == "HEADER_PARAM" {
			if param.Required != nil && *param.Required {
				if param.Name != nil {
					if request.Headers == nil {
						return fmt.Errorf("Header参数 %s 为必填项，不能为空", *param.Name)
					}
					if _, exists := request.Headers[*param.Name]; !exists {
						return fmt.Errorf("Header参数 %s 为必填项，不能为空", *param.Name)
					}
				}
			}
		}
	}

	// 验证Body参数
	for _, param := range params {
		if param.ParamType != nil && *param.ParamType == "BODY_PARAM" {
			if param.Required != nil && *param.Required {
				if param.Name != nil {
					if request.BodyParams == nil {
						return fmt.Errorf("Body参数 %s 为必填项，不能为空", *param.Name)
					}
					if _, exists := request.BodyParams[*param.Name]; !exists {
						return fmt.Errorf("Body参数 %s 为必填项，不能为空", *param.Name)
					}
				}
			}
		}
	}

	return nil
}

// convertToDto 转换实体为DTO
func (s *ApiInterfaceService) convertToDto(entity *entity.ApiInterface) dto.ApiInterfaceDto {
	// 解析参数
	var urlParams, headerParams, bodyParams []dto.ApiParamDto
	if entity.Params != nil && *entity.Params != "" {
		if err := json.Unmarshal([]byte(*entity.Params), &urlParams); err != nil {
			log.Printf("解析URL参数失败: %v\n", err)
		}
		if err := json.Unmarshal([]byte(*entity.Params), &headerParams); err != nil {
			log.Printf("解析Header参数失败: %v\n", err)
		}
		if err := json.Unmarshal([]byte(*entity.Params), &bodyParams); err != nil {
			log.Printf("解析Body参数失败: %v\n", err)
		}

		// 过滤参数类型
		filteredURLParams := make([]dto.ApiParamDto, 0)
		filteredHeaderParams := make([]dto.ApiParamDto, 0)
		filteredBodyParams := make([]dto.ApiParamDto, 0)

		for _, p := range urlParams {
			if p.ParamType != nil && *p.ParamType == "URL_PARAM" {
				filteredURLParams = append(filteredURLParams, p)
			}
		}
		for _, p := range headerParams {
			if p.ParamType != nil && *p.ParamType == "HEADER_PARAM" {
				filteredHeaderParams = append(filteredHeaderParams, p)
			}
		}
		for _, p := range bodyParams {
			if p.ParamType != nil && *p.ParamType == "BODY_PARAM" {
				filteredBodyParams = append(filteredBodyParams, p)
			}
		}

		urlParams = filteredURLParams
		headerParams = filteredHeaderParams
		bodyParams = filteredBodyParams
	}

	createTime := util.Format(&entity.CreateTime)
	updateTime := util.Format(&entity.UpdateTime)

	return dto.ApiInterfaceDto{
		ID:           &entity.ID,
		Name:         &entity.Name,
		Method:       &entity.Method,
		URL:          &entity.URL,
		Description:  entity.Description,
		Status:       entity.Status,
		PostType:     entity.PostType,
		Environment:  entity.Environment,
		Timeout:      entity.Timeout,
		ValuePath:    entity.ValuePath,
		URLParams:    urlParams,
		HeaderParams: headerParams,
		BodyParams:   bodyParams,
		CreateTime:   &createTime,
		UpdateTime:   &updateTime,
	}
}

// convertToEntity 转换DTO为实体
func (s *ApiInterfaceService) convertToEntity(form *dto.ApiInterfaceFormDto) *entity.ApiInterface {
	// 合并所有参数
	allParams := make([]dto.ApiParamDto, 0)
	if form.URLParams != nil {
		allParams = append(allParams, form.URLParams...)
	}
	if form.HeaderParams != nil {
		allParams = append(allParams, form.HeaderParams...)
	}
	if form.BodyParams != nil {
		allParams = append(allParams, form.BodyParams...)
	}

	var paramsJSON *string
	if len(allParams) > 0 {
		jsonBytes, err := json.Marshal(allParams)
		if err != nil {
			log.Printf("序列化参数失败: %v\n", err)
			// 如果序列化失败，使用空字符串
			emptyStr := "[]"
			paramsJSON = &emptyStr
		} else {
			jsonStr := string(jsonBytes)
			paramsJSON = &jsonStr
		}
	}

	return &entity.ApiInterface{
		Name:        *form.Name,
		Method:      *form.Method,
		URL:         *form.URL,
		Description: form.Description,
		PostType:    form.PostType,
		Environment: form.Environment,
		Timeout:     form.Timeout,
		ValuePath:   form.ValuePath,
		Params:      paramsJSON,
	}
}

// processParams 处理参数值：根据数据类型转换
func (s *ApiInterfaceService) processParams(apiInterface *entity.ApiInterface, req *dto.ApiExecuteRequestDto) *dto.ApiExecuteRequestDto {
	// 解析参数配置
	if apiInterface.Params == nil || *apiInterface.Params == "" {
		return req
	}

	var params []dto.ApiParamDto
	if err := json.Unmarshal([]byte(*apiInterface.Params), &params); err != nil {
		return req
	}

	// 创建参数映射
	paramMap := make(map[string]dto.ApiParamDto)
	for _, p := range params {
		if p.Name != nil {
			paramMap[*p.Name] = p
		}
	}

	// 处理URL参数
	processedURLParams := make(map[string]any)
	if req.URLParams != nil {
		for k, v := range req.URLParams {
			if param, ok := paramMap[k]; ok && param.DataType != nil && *param.DataType == "JSON_OBJECT" {
				// JSON_OBJECT类型：解析为对象
				if str, ok := v.(string); ok {
					var obj any
					if err := json.Unmarshal([]byte(str), &obj); err == nil {
						processedURLParams[k] = obj
					} else {
						processedURLParams[k] = v
					}
				} else {
					processedURLParams[k] = v
				}
			} else {
				processedURLParams[k] = v
			}
		}
	}

	// 处理Header参数
	processedHeaders := make(map[string]string)
	if req.Headers != nil {
		for k, v := range req.Headers {
			if param, ok := paramMap[k]; ok && param.DataType != nil && *param.DataType == "JSON_OBJECT" {
				// JSON_OBJECT类型：解析为对象（Header通常是字符串，这里保持原样）
				processedHeaders[k] = v
			} else {
				processedHeaders[k] = v
			}
		}
	}

	// 处理Body参数
	processedBodyParams := make(map[string]any)
	if req.BodyParams != nil {
		for k, v := range req.BodyParams {
			if param, ok := paramMap[k]; ok && param.DataType != nil && *param.DataType == "JSON_OBJECT" {
				// JSON_OBJECT类型：解析为对象
				if str, ok := v.(string); ok {
					var obj any
					if err := json.Unmarshal([]byte(str), &obj); err == nil {
						processedBodyParams[k] = obj
					} else {
						processedBodyParams[k] = v
					}
				} else {
					processedBodyParams[k] = v
				}
			} else {
				processedBodyParams[k] = v
			}
		}
	}

	return &dto.ApiExecuteRequestDto{
		InterfaceID: req.InterfaceID,
		Headers:     processedHeaders,
		URLParams:   processedURLParams,
		BodyParams:  processedBodyParams,
		Timeout:     req.Timeout,
		Remark:      req.Remark,
	}
}

// stringPtr 字符串指针辅助函数
func stringPtr(s string) *string {
	return &s
}
