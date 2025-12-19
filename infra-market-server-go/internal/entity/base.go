package entity

import (
	"time"

	"gorm.io/gorm"
)

// BaseEntity 基础实体，包含公共字段
type BaseEntity struct {
	ID         uint64 `gorm:"primaryKey;autoIncrement;column:id" json:"id"`
	CreateTime int64  `gorm:"column:create_time;not null;default:0" json:"createTime"`
	UpdateTime int64  `gorm:"column:update_time;not null;default:0" json:"updateTime"`
}

// BeforeCreate 创建前钩子
func (b *BaseEntity) BeforeCreate(tx *gorm.DB) error {
	now := time.Now().UnixMilli()
	b.CreateTime = now
	b.UpdateTime = now
	return nil
}

// BeforeUpdate 更新前钩子
func (b *BaseEntity) BeforeUpdate(tx *gorm.DB) error {
	b.UpdateTime = time.Now().UnixMilli()
	return nil
}
