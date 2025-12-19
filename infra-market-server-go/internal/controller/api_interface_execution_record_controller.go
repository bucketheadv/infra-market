package controller

import (
	"strconv"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

type ApiInterfaceExecutionRecordController struct {
	service *service.ApiInterfaceExecutionRecordService
}

func NewApiInterfaceExecutionRecordController(service *service.ApiInterfaceExecutionRecordService) *ApiInterfaceExecutionRecordController {
	return &ApiInterfaceExecutionRecordController{service: service}
}

// List 分页查询执行记录
func (c *ApiInterfaceExecutionRecordController) List(ctx *gin.Context) {
	var query dto.ApiInterfaceExecutionRecordQueryDto
	if err := ctx.ShouldBindJSON(&query); err != nil {
		ctx.JSON(400, dto.Error[interface{}]("参数校验失败", 400))
		return
	}

	result := c.service.FindPage(query)
	ctx.JSON(200, result)
}

// Detail 获取执行记录详情
func (c *ApiInterfaceExecutionRecordController) Detail(ctx *gin.Context) {
	idStr := ctx.Param("id")
	id, err := strconv.ParseUint(idStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的记录ID", 400))
		return
	}

	result := c.service.GetByID(id)
	ctx.JSON(200, result)
}

// GetByExecutorID 根据执行人ID查询
func (c *ApiInterfaceExecutionRecordController) GetByExecutorID(ctx *gin.Context) {
	executorIDStr := ctx.Param("executorId")
	executorID, err := strconv.ParseUint(executorIDStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的执行人ID", 400))
		return
	}

	limitStr := ctx.DefaultQuery("limit", "10")
	limit, _ := strconv.Atoi(limitStr)

	result := c.service.FindByExecutorID(executorID, limit)
	ctx.JSON(200, result)
}

// GetExecutionStats 获取执行统计信息
func (c *ApiInterfaceExecutionRecordController) GetExecutionStats(ctx *gin.Context) {
	interfaceIDStr := ctx.Param("interfaceId")
	interfaceID, err := strconv.ParseUint(interfaceIDStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的接口ID", 400))
		return
	}

	result := c.service.GetExecutionStats(interfaceID)
	ctx.JSON(200, result)
}

// GetExecutionCount 获取执行记录数量统计
func (c *ApiInterfaceExecutionRecordController) GetExecutionCount(ctx *gin.Context) {
	startTimeStr := ctx.Query("startTime")
	endTimeStr := ctx.Query("endTime")

	startTime, err := strconv.ParseInt(startTimeStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的开始时间", 400))
		return
	}

	endTime, err := strconv.ParseInt(endTimeStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的结束时间", 400))
		return
	}

	result := c.service.CountByTimeRange(startTime, endTime)
	ctx.JSON(200, result)
}

// CleanupOldRecords 删除指定时间之前的记录
func (c *ApiInterfaceExecutionRecordController) CleanupOldRecords(ctx *gin.Context) {
	beforeTimeStr := ctx.Query("beforeTime")
	beforeTime, err := strconv.ParseInt(beforeTimeStr, 10, 64)
	if err != nil {
		ctx.JSON(400, dto.Error[interface{}]("无效的时间", 400))
		return
	}

	result := c.service.DeleteByTimeBefore(beforeTime)
	ctx.JSON(200, result)
}
