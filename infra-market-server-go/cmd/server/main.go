package main

import (
	"flag"
	"log"

	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/database"
	"github.com/bucketheadv/infra-market/internal/router"
)

func main() {
	// 解析命令行参数
	var configPath string
	flag.StringVar(&configPath, "config", "config.toml", "配置文件路径")
	flag.StringVar(&configPath, "c", "config.toml", "配置文件路径（简写）")
	flag.Parse()

	// 加载配置
	cfg, err := config.Load(configPath)
	if err != nil {
		log.Fatalf("加载配置失败: %v", err)
	}

	// 初始化数据库
	db, err := database.InitDB(cfg.Database)
	if err != nil {
		log.Fatalf("数据库初始化失败: %v", err)
	}

	// 初始化路由
	r := router.SetupRouter(db, cfg)

	// 启动服务器
	if err := r.Run(":" + cfg.Server.Port); err != nil {
		log.Fatalf("服务器启动失败: %v", err)
	}
}
