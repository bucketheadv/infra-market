package service

import (
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
)

// PageResultBuilder 构建分页结果的辅助函数
// entities: 实体列表
// total: 总数
// err: 错误
// convertFunc: 将实体转换为 DTO 的函数
// query: 实现了 PaginationQuery 接口的查询对象
func PageResultBuilder[E any, D any](
	entities []E,
	total int64,
	err error,
	convertFunc func(*E) D,
	query dto.PaginationQuery,
) dto.ApiData[dto.PageResult[D]] {
	if err != nil {
		return dto.Error[dto.PageResult[D]]("查询失败", http.StatusInternalServerError)
	}

	dtos := make([]D, len(entities))
	for i := range entities {
		dtos[i] = convertFunc(&entities[i])
	}

	result := dto.PageResult[D]{
		Records: dtos,
		Total:   total,
		Page:    query.GetPage(),
		Size:    query.GetSize(),
	}

	return dto.Success(result)
}

