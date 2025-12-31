package dto

// Pagination 分页参数（统一使用 Page 和 Size）
type Pagination struct {
	Page *int `form:"page" binding:"omitempty,min=1"`
	Size *int `form:"size" binding:"omitempty,min=1,max=1000"`
}

// GetPage 获取页码，如果为nil则返回默认值1
func (p *Pagination) GetPage() int {
	if p.Page == nil {
		return 1
	}
	return *p.Page
}

// GetSize 获取每页大小，如果为nil则返回默认值10
func (p *Pagination) GetSize() int {
	if p.Size == nil {
		return 10
	}
	return *p.Size
}

// PaginationQuery 分页查询接口
type PaginationQuery interface {
	GetPage() int
	GetSize() int
}
