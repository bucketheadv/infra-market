package bootstrap

import (
	"context"
	"net"
	"net/http"
	"path/filepath"
	"strings"
	"time"

	"github.com/bucketheadv/infra-go/logx"
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/controller"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/router"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/go-spring/spring-core/gs"
	_ "github.com/go-spring/starter-go-redis"
	_ "github.com/go-spring/starter-gorm-mysql"
	"gorm.io/gorm"
	gormLogger "gorm.io/gorm/logger"
)

type GinServer struct {
	server *http.Server
}

// registerStartedAt 在 Register 入口记录，用于统计到 HTTP 监听就绪的耗时。
var registerStartedAt time.Time

type GormLoggerRunner struct {
	DB       *gorm.DB `autowire:""`
	LogLevel string   `value:"${gorm.log-level:=info}"`
}

func (r *GormLoggerRunner) Run() error {
	if r.DB == nil || r.DB.Config == nil {
		return nil
	}
	level := parseGormLogLevel(r.LogLevel)
	r.DB.Config.Logger = logx.NewGormLogger(logx.GormLoggerConfig{
		LoggerName:                logx.NameGorm,
		SlowThreshold:             200 * time.Millisecond,
		IgnoreRecordNotFoundError: true,
	}).LogMode(level)
	logx.Infof(context.Background(), logx.NameApp, "gorm sql log level=%s (applog logger=%s)", strings.ToLower(r.LogLevel), logx.NameGorm)
	return nil
}

func parseGormLogLevel(level string) gormLogger.LogLevel {
	switch strings.ToLower(strings.TrimSpace(level)) {
	case "silent":
		return gormLogger.Silent
	case "error":
		return gormLogger.Error
	case "warn", "warning":
		return gormLogger.Warn
	default:
		return gormLogger.Info
	}
}

func NewGinServer(engine http.Handler, cfg *config.AppConfig) *GinServer {
	return &GinServer{
		server: &http.Server{
			Addr:    ":" + cfg.Server.Port,
			Handler: engine,
		},
	}
}

func (s *GinServer) ListenAndServe(sig gs.ReadySignal) error {
	<-sig.TriggerAndWait()
	ln, err := net.Listen("tcp", s.server.Addr)
	if err != nil {
		return err
	}
	readyAt := time.Now()
	logx.Infof(context.Background(), logx.NameApp,
		"infra-market-go-spring listen ready addr=%s startupAt=%s sinceRegister=%s",
		ln.Addr().String(),
		readyAt.Format("2006-01-02 15:04:05.000"),
		readyAt.Sub(registerStartedAt).String(),
	)
	return s.server.Serve(ln)
}

func (s *GinServer) Shutdown(ctx context.Context) error {
	logx.Infof(ctx, logx.NameApp, "infra-market-go-spring shutting down")
	logx.Close()
	return s.server.Shutdown(ctx)
}

func Register() {
	registerStartedAt = time.Now()
	logx.MustLoad(filepath.Join("config", "logx.yaml"))
	logx.InstallGinWriters(logx.GinWritersConfig{})
	gs.Object(new(config.AppConfig))
	gs.Object(new(GormLoggerRunner)).AsRunner()

	registerRepositories()
	registerServices()
	registerControllers()

	gs.Provide(router.NewRouter).Export(gs.As[http.Handler]())
	gs.Provide(NewGinServer).AsServer()
}

func registerProviders(providers ...interface{}) {
	for _, provider := range providers {
		gs.Provide(provider)
	}
}

func registerRepositories() {
	registerProviders(
		repository.NewUserRepository,
		repository.NewRoleRepository,
		repository.NewPermissionRepository,
		repository.NewUserRoleRepository,
		repository.NewRolePermissionRepository,
		repository.NewApiInterfaceRepository,
		repository.NewApiInterfaceExecutionRecordRepository,
		repository.NewActivityRepository,
		repository.NewActivityTemplateRepository,
		repository.NewActivityComponentRepository,
	)
}

func registerServices() {
	registerProviders(
		service.NewTokenService,
		service.NewAuthService,
		service.NewUserService,
		service.NewRoleService,
		service.NewPermissionService,
		service.NewApiInterfaceService,
		service.NewApiInterfaceExecutionRecordService,
		service.NewDashboardService,
		service.NewActivityService,
		service.NewActivityTemplateService,
		service.NewActivityComponentService,
	)
}

func registerControllers() {
	registerProviders(
		controller.NewAuthController,
		controller.NewUserController,
		controller.NewRoleController,
		controller.NewPermissionController,
		controller.NewApiInterfaceController,
		controller.NewApiInterfaceExecutionRecordController,
		controller.NewDashboardController,
		controller.NewActivityController,
		controller.NewActivityTemplateController,
		controller.NewActivityComponentController,
	)
}
