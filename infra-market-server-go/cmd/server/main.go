package main

import (
	"context"
	"flag"
	"net"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/bucketheadv/infra-go/logx"
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

	logx.MustLoad(filepath.Join("config", "logx.yaml"))
	logx.InstallGinWriters(logx.GinWritersConfig{})

	ctx := context.Background()
	logx.Infof(ctx, logx.NameApp, "Go 版本: %s", runtime.Version())

	cfg, err := config.Load(configPath)
	if err != nil {
		logx.Errorf(ctx, logx.NameApp, "加载配置失败: %v", err)
		os.Exit(1)
	}

	db, err := database.InitDB(cfg.Database)
	if err != nil {
		logx.Errorf(ctx, logx.NameApp, "数据库初始化失败: %v", err)
		os.Exit(1)
	}

	gin.SetMode(gin.ReleaseMode)

	r, err := router.SetupRouter(db, cfg)
	if err != nil {
		logx.Errorf(ctx, logx.NameApp, "路由初始化失败: %v", err)
		os.Exit(1)
	}

	ln, err := net.Listen("tcp", ":"+cfg.Server.Port)
	if err != nil {
		logx.Errorf(ctx, logx.NameApp, "监听端口失败: %v", err)
		os.Exit(1)
	}
	logx.Infof(ctx, logx.NameApp, "HTTP 监听 %s，启动耗时 %v", ln.Addr(), time.Since(startupBegin))
	if err := r.RunListener(ln); err != nil {
		logx.Errorf(ctx, logx.NameApp, "服务器运行异常: %v", err)
		os.Exit(1)
	}
}
