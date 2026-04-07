package database

import (
	"fmt"
	"log"

	"github.com/bucketheadv/infra-market/internal/config"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

// InitDB 初始化数据库连接
func InitDB(cfg config.DatabaseConfig) (*gorm.DB, error) {
	db, err := gorm.Open(mysql.Open(cfg.DSN), &gorm.Config{
		Logger: logger.Default.LogMode(logger.Info),
	})
	if err != nil {
		return nil, fmt.Errorf("连接数据库失败: %w", err)
	}

	// 自动迁移（已关闭）
	// if err := autoMigrate(db); err != nil {
	// 	return nil, fmt.Errorf("数据库迁移失败: %w", err)
	// }

	log.Println("数据库连接成功")
	return db, nil
}

// autoMigrate 自动迁移数据库表（已关闭）
// func autoMigrate(db *gorm.DB) error {
// 	return db.AutoMigrate(
// 		&entity.User{},
// 		&entity.Role{},
// 		&entity.Permission{},
// 		&entity.UserRole{},
// 		&entity.RolePermission{},
// 		&entity.ApiInterface{},
// 		&entity.ApiInterfaceExecutionRecord{},
// 	)
// }
