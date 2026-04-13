package bootstrap

import (
	"context"
	"net/http"
	"strings"

	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/controller"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/router"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/go-spring/log"
	"github.com/go-spring/spring-core/gs"
	_ "github.com/go-spring/starter-go-redis"
	_ "github.com/go-spring/starter-gorm-mysql"
	"gorm.io/gorm"
	gormLogger "gorm.io/gorm/logger"
)

type GinServer struct {
	server *http.Server
}

type GormLoggerRunner struct {
	DB       *gorm.DB `autowire:""`
	LogLevel string   `value:"${gorm.log-level:=info}"`
}

func (r *GormLoggerRunner) Run() error {
	if r.DB == nil || r.DB.Config == nil {
		return nil
	}
	level := parseGormLogLevel(r.LogLevel)
	r.DB.Config.Logger = gormLogger.Default.LogMode(level)
	log.Infof(context.Background(), log.TagAppDef, "gorm sql log level=%s", strings.ToLower(r.LogLevel))
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
	log.Infof(context.Background(), log.TagAppDef, "infra-market-go-spring listening at %s", s.server.Addr)
	<-sig.TriggerAndWait()
	return s.server.ListenAndServe()
}

func (s *GinServer) Shutdown(ctx context.Context) error {
	log.Infof(ctx, log.TagAppDef, "infra-market-go-spring shutting down")
	return s.server.Shutdown(ctx)
}

func Register() {
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
