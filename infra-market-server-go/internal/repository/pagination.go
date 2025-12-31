package repository

import (
	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/util"
	"gorm.io/gorm"
)

// ApplyCommonFilters 应用通用查询条件（Name、Code、Status）
func ApplyCommonFilters(db *gorm.DB, name, code, status *string) *gorm.DB {
	if util.IsNotBlank(name) {
		db = db.Where("name LIKE ?", "%"+*name+"%")
	}
	if util.IsNotBlank(code) {
		db = db.Where("code LIKE ?", "%"+*code+"%")
	}
	if util.IsNotBlank(status) {
		db = db.Where("status = ?", *status)
	}
	return db
}

// PaginateQuery 执行分页查询的通用函数
// db: 已构建好的查询构建器
// query: 实现了 PaginationQuery 接口的查询对象
// orderBy: 排序字段，如 "create_time DESC" 或 "id ASC"
// result: 结果切片，必须是指针类型
// 返回: 结果列表, 总数, 错误
func PaginateQuery[T any](
	db *gorm.DB,
	query dto.PaginationQuery,
	orderBy string,
	result *[]T,
) ([]T, int64, error) {
	var total int64

	// 获取总数
	if err := db.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	// 获取分页参数（带默认值）
	page := query.GetPage()
	size := query.GetSize()

	// 计算偏移量
	offset := (page - 1) * size

	// 执行分页查询
	err := db.Order(orderBy).Offset(offset).Limit(size).Find(result).Error
	if err != nil {
		return nil, 0, err
	}

	return *result, total, nil
}
