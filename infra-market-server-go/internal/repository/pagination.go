package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"gorm.io/gorm"
)

// PaginateQuery 执行分页查询的通用函数
// db: 已构建好的查询构建器
// page: 页码指针（从1开始），如果为nil则使用默认值1
// size: 每页大小指针，如果为nil则使用默认值10
// orderBy: 排序字段，如 "create_time DESC" 或 "id ASC"
// result: 结果切片，必须是指针类型
// 返回: 结果列表, 总数, 错误
func PaginateQuery[T any](
	db *gorm.DB,
	page, size *int,
	orderBy string,
	result *[]T,
) ([]T, int64, error) {
	var total int64

	// 获取总数
	if err := db.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 获取分页参数（带默认值）
	p := dto.GetPage(page)
	s := dto.GetSize(size)

	// 计算偏移量
	offset := (p - 1) * s

	// 执行分页查询
	err := db.Order(orderBy).Offset(offset).Limit(s).Find(result).Error
	if err != nil {
		return nil, 0, err
	}

	return *result, total, nil
}
