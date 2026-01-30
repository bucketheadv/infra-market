package dto

// ActivityDto 活动信息DTO
type ActivityDto struct {
	ID           uint64                `json:"id"`
	Name         string                `json:"name"`
	Description  *string               `json:"description"`
	TemplateID   uint64                `json:"templateId"`
	TemplateName *string               `json:"templateName"`
	ConfigData   map[string]any `json:"configData"`
	Status       int                   `json:"status"`
	CreateTime   string                `json:"createTime"`
	UpdateTime   string                `json:"updateTime"`
}

// ActivityFormDto 活动创建/更新表单
type ActivityFormDto struct {
	Name        string                 `json:"name" binding:"required,min=1,max=100"`
	Description *string                `json:"description" binding:"omitempty,max=500"`
	TemplateID  uint64                 `json:"templateId" binding:"required,min=1"`
	ConfigData  map[string]any `json:"configData"`
	Status      *int                   `json:"status" binding:"omitempty,oneof=0 1"`
}

// ActivityQueryDto 活动查询DTO
type ActivityQueryDto struct {
	Name       *string `form:"name"`
	TemplateID *uint64 `form:"templateId"`
	Status     *int    `form:"status" binding:"omitempty,oneof=0 1"`
	Pagination
}

// ActivityTemplateDto 活动模板信息DTO
type ActivityTemplateDto struct {
	ID          uint64                      `json:"id"`
	Name        string                      `json:"name"`
	Description *string                     `json:"description"`
	Fields      []ActivityTemplateFieldDto   `json:"fields"`
	Status      int                         `json:"status"`
	CreateTime  string                      `json:"createTime"`
	UpdateTime  string                      `json:"updateTime"`
}

// ActivityTemplateFieldDto 活动模板字段配置DTO
type ActivityTemplateFieldDto struct {
	Name         string                           `json:"name"`
	Label        *string                          `json:"label"`
	Type         *string                          `json:"type"`
	InputType    *string                          `json:"inputType"`
	Required     *bool                            `json:"required"`
	DefaultValue any                      `json:"defaultValue"`
	Description  *string                          `json:"description"`
	Sort         *int                             `json:"sort"`
	Options      []SelectOptionDto                `json:"options"`
	Multiple     *bool                            `json:"multiple"`
	Min          any                      `json:"min"`
	Max          any                      `json:"max"`
	MinLength    *int                             `json:"minLength"`
	MaxLength    *int                             `json:"maxLength"`
	Pattern      *string                          `json:"pattern"`
	Placeholder  *string                          `json:"placeholder"`
	ItemType     *ActivityTemplateFieldDto         `json:"itemType"`
	Properties   map[string]ActivityTemplateFieldDto `json:"properties"`
	ComponentID  *uint64                          `json:"componentId"`
	IsArray      *bool                            `json:"isArray"`
	AllowDynamic *bool                            `json:"allowDynamic"`
}

// ActivityTemplateFormDto 活动模板创建/更新表单
type ActivityTemplateFormDto struct {
	Name        string                      `json:"name" binding:"required,min=1,max=100"`
	Description *string                     `json:"description" binding:"omitempty,max=500"`
	Fields      []ActivityTemplateFieldDto   `json:"fields"`
	Status      *int                        `json:"status" binding:"omitempty,oneof=0 1"`
}

// ActivityTemplateQueryDto 活动模板查询DTO
type ActivityTemplateQueryDto struct {
	Name   *string `form:"name"`
	Status *int    `form:"status" binding:"omitempty,oneof=0 1"`
	Pagination
}

// ActivityComponentDto 活动组件信息DTO
type ActivityComponentDto struct {
	ID          uint64                      `json:"id"`
	Name        string                      `json:"name"`
	Description *string                     `json:"description"`
	Fields      []ActivityComponentFieldDto  `json:"fields"`
	Status      int                         `json:"status"`
	CreateTime  string                      `json:"createTime"`
	UpdateTime  string                      `json:"updateTime"`
}

// ActivityComponentFieldDto 活动组件字段/组件配置DTO
type ActivityComponentFieldDto struct {
	Name         string                           `json:"name"`
	Label        *string                          `json:"label"`
	Type         *string                          `json:"type"`
	InputType    *string                          `json:"inputType"`
	Required     *bool                            `json:"required"`
	DefaultValue any                      `json:"defaultValue"`
	Description  *string                          `json:"description"`
	Sort         *int                             `json:"sort"`
	Options      []SelectOptionDto                `json:"options"`
	Multiple     *bool                            `json:"multiple"`
	Min          any                      `json:"min"`
	Max          any                      `json:"max"`
	MinLength    *int                             `json:"minLength"`
	MaxLength    *int                             `json:"maxLength"`
	Pattern      *string                          `json:"pattern"`
	Placeholder  *string                          `json:"placeholder"`
	ItemType     *ActivityComponentFieldDto       `json:"itemType"`
	Properties   map[string]ActivityComponentFieldDto `json:"properties"`
	ComponentID  *uint64                          `json:"componentId"`
	IsArray      *bool                            `json:"isArray"`
	AllowDynamic *bool                            `json:"allowDynamic"`
}

// ActivityComponentFormDto 活动组件创建/更新表单
type ActivityComponentFormDto struct {
	Name        string                      `json:"name" binding:"required,min=1,max=100"`
	Description *string                     `json:"description" binding:"omitempty,max=500"`
	Fields      []ActivityComponentFieldDto  `json:"fields"`
	Status      *int                        `json:"status" binding:"omitempty,oneof=0 1"`
}

// ActivityComponentQueryDto 活动组件查询DTO
type ActivityComponentQueryDto struct {
	Name   *string `form:"name"`
	Status *int    `form:"status" binding:"omitempty,oneof=0 1"`
	Pagination
}
