package service

import (
	"gorm.io/gorm"
)

// TransactionFunc 事务函数类型
type TransactionFunc func(tx *gorm.DB) error

// WithTransaction 在事务中执行函数
// 如果函数返回错误，事务会自动回滚；否则提交事务
func WithTransaction(db *gorm.DB, fn TransactionFunc) error {
	tx := db.Begin()
	if tx.Error != nil {
		return tx.Error
	}

	if err := fn(tx); err != nil {
		tx.Rollback()
		return err
	}

	return tx.Commit().Error
}
