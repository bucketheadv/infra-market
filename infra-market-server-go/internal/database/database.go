package database

import (
	"context"
	"fmt"

	"github.com/bucketheadv/infra-go/logx"
	"github.com/bucketheadv/infra-market/internal/config"
	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	glog "gorm.io/gorm/logger"
)

// InitDB 初始化数据库连接
func InitDB(cfg config.DatabaseConfig) (*gorm.DB, error) {
	db, err := gorm.Open(mysql.Open(cfg.DSN), &gorm.Config{
		Logger: logx.NewGormLogger(logx.GormLoggerConfig{}).LogMode(glog.Info),
	})
	if err != nil {
		return nil, fmt.Errorf("连接数据库失败: %w", err)
	}

	logx.Infof(context.Background(), logx.NameApp, "数据库连接成功")
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
