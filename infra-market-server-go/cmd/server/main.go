package main

import (
	"context"
	"flag"
	"net"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/bucketheadv/infra-go/applog"
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/database"
	"github.com/bucketheadv/infra-market/internal/router"
	"github.com/gin-gonic/gin"
)

func main() {
	var configPath string
	flag.StringVar(&configPath, "config", "config.toml", "配置文件路径")
	flag.StringVar(&configPath, "c", "config.toml", "配置文件路径（简写）")
	flag.Parse()
	startupBegin := time.Now()

	applog.MustLoad(filepath.Join("config", "applog.yaml"))
	applog.InstallGinWriters(applog.GinWritersConfig{})

	ctx := context.Background()
	applog.Infof(ctx, applog.NameApp, "Go 版本: %s", runtime.Version())

	cfg, err := config.Load(configPath)
	if err != nil {
		applog.Errorf(ctx, applog.NameApp, "加载配置失败: %v", err)
		os.Exit(1)
	}

	db, err := database.InitDB(cfg.Database)
	if err != nil {
		applog.Errorf(ctx, applog.NameApp, "数据库初始化失败: %v", err)
		os.Exit(1)
	}

	gin.SetMode(gin.ReleaseMode)

	r, err := router.SetupRouter(db, cfg)
	if err != nil {
		applog.Errorf(ctx, applog.NameApp, "路由初始化失败: %v", err)
		os.Exit(1)
	}

	ln, err := net.Listen("tcp", ":"+cfg.Server.Port)
	if err != nil {
		applog.Errorf(ctx, applog.NameApp, "监听端口失败: %v", err)
		os.Exit(1)
	}
	applog.Infof(ctx, applog.NameApp, "HTTP 监听 %s，启动耗时 %v", ln.Addr(), time.Since(startupBegin))
	if err := r.RunListener(ln); err != nil {
		applog.Errorf(ctx, applog.NameApp, "服务器运行异常: %v", err)
		os.Exit(1)
	}
}
