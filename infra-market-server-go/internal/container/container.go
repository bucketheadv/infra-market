package container

import (
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/controller"
	"github.com/bucketheadv/infra-market/internal/middleware"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
	"github.com/go-redis/redis/v8"
	"go.uber.org/dig"
	"gorm.io/gorm"
)

// Container 依赖注入容器
type Container struct {
	container *dig.Container
}

// NewContainer 创建新的依赖注入容器
func NewContainer(db *gorm.DB, cfg *config.Config) (*Container, error) {
	c := dig.New()
	container := &Container{container: c}

	// 注册外部依赖
	providers := []any{
		func() *gorm.DB { return db },
		func() *config.Config { return cfg },
	}
	if err := container.mustProvide(providers...); err != nil {
		return nil, err
	}

	// 注册所有依赖
	if err := container.registerDependencies(); err != nil {
		return nil, err
	}

	return container, nil
}

// mustProvide 批量注册提供者，如果任何一个失败则返回错误
func (c *Container) mustProvide(providers ...any) error {
	for _, provider := range providers {
		if err := c.container.Provide(provider); err != nil {
			return err
		}
	}
	return nil
}

// registerDependencies 注册所有依赖
// dig 会根据构造函数参数自动解析和注入依赖关系
// 依赖关系：
//   - Repository 依赖 *gorm.DB (已在 NewContainer 中注册)
//   - TokenService 依赖 *redis.Client (通过 ProvideRedisClient 提供，依赖 *config.Config)
//   - Service 依赖 Repository 和 *gorm.DB (dig 自动注入)
//   - Controller 依赖 Service (dig 自动注入)
func (c *Container) registerDependencies() error {
	// 注册 Redis 客户端提供者
	// 依赖: *config.Config -> *redis.Client
	if err := c.mustProvide(c.ProvideRedisClient); err != nil {
		return err
	}

	// 注册 Repository 层
	// 所有 Repository 构造函数接收 *gorm.DB 参数，dig 会自动注入
	repositories := []any{
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
	}
	if err := c.mustProvide(repositories...); err != nil {
		return err
	}

	// 注册 Service 层
	// Service 构造函数接收 Repository 和 *gorm.DB 参数，dig 会自动注入
	services := []any{
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
	}
	if err := c.mustProvide(services...); err != nil {
		return err
	}

	// 注册 Controller 层
	// Controller 构造函数接收 Service 参数，dig 会自动注入
	controllers := []any{
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
	}
	if err := c.mustProvide(controllers...); err != nil {
		return err
	}

	return nil
}

// ProvideRedisClient 提供 Redis 客户端
func (c *Container) ProvideRedisClient(cfg *config.Config) *redis.Client {
	return redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Host + ":" + cfg.Redis.Port,
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})
}

// Invoke 调用函数并自动注入依赖
func (c *Container) Invoke(fn any) error {
	return c.container.Invoke(fn)
}

// SetupRouter 设置路由（通过依赖注入）
func (c *Container) SetupRouter() (*gin.Engine, error) {
	var router *gin.Engine
	err := c.Invoke(func(
		authController *controller.AuthController,
		userController *controller.UserController,
		roleController *controller.RoleController,
		permissionController *controller.PermissionController,
		apiInterfaceController *controller.ApiInterfaceController,
		apiInterfaceExecutionRecordController *controller.ApiInterfaceExecutionRecordController,
		dashboardController *controller.DashboardController,
		activityController *controller.ActivityController,
		activityTemplateController *controller.ActivityTemplateController,
		activityComponentController *controller.ActivityComponentController,
		tokenService *service.TokenService,
	) {
		router = gin.Default()

		// 全局中间件
		router.Use(middleware.CORSMiddleware())
		router.Use(middleware.ErrorHandler())

		// 登录接口（不需要鉴权）
		router.POST("/auth/login", authController.Login)

		// 需要鉴权的路由组
		api := router.Group("/")
		api.Use(middleware.AuthMiddleware(tokenService))
		{
			// 认证相关路由（除登录外都需要鉴权）
			auth := api.Group("/auth")
			{
				auth.GET("/current/user", authController.GetCurrentUser)
				auth.GET("/user/menus", authController.GetUserMenus)
				auth.POST("/refresh/token", authController.RefreshToken)
				auth.POST("/logout", authController.Logout)
				auth.POST("/change/password", authController.ChangePassword)
			}

			// 业务路由
			// 用户管理
			users := api.Group("/users")
			{
				users.GET("", userController.GetUsers)
				users.GET("/:id", userController.GetUser)
				users.POST("", userController.CreateUser)
				users.PUT("/:id", userController.UpdateUser)
				users.DELETE("/:id", userController.DeleteUser)
				users.PATCH("/:id/status", userController.UpdateUserStatus)
				users.POST("/:id/reset/password", userController.ResetPassword)
				users.POST("/batch/delete", userController.BatchDeleteUsers)
			}

			// 角色管理
			roles := api.Group("/roles")
			{
				roles.GET("", roleController.GetRoles)
				roles.GET("/all", roleController.GetAllRoles)
				roles.GET("/:id", roleController.GetRole)
				roles.POST("", roleController.CreateRole)
				roles.PUT("/:id", roleController.UpdateRole)
				roles.DELETE("/:id", roleController.DeleteRole)
				roles.PATCH("/:id/status", roleController.UpdateRoleStatus)
				roles.DELETE("/batch", roleController.BatchDeleteRoles)
			}

			// 权限管理
			permissions := api.Group("/permissions")
			{
				permissions.GET("", permissionController.GetPermissions)
				permissions.GET("/tree", permissionController.GetPermissionTree)
				permissions.GET("/:id", permissionController.GetPermission)
				permissions.POST("", permissionController.CreatePermission)
				permissions.PUT("/:id", permissionController.UpdatePermission)
				permissions.DELETE("/:id", permissionController.DeletePermission)
				permissions.PATCH("/:id/status", permissionController.UpdatePermissionStatus)
				permissions.DELETE("/batch", permissionController.BatchDeletePermissions)
			}

			// 接口管理
			interfaces := api.Group("/interface")
			{
				interfaces.GET("/list", apiInterfaceController.List)
				interfaces.GET("/most/used", apiInterfaceController.GetMostUsed)
				interfaces.GET("/:id", apiInterfaceController.Detail)
				interfaces.POST("", apiInterfaceController.Create)
				interfaces.PUT("/:id", apiInterfaceController.Update)
				interfaces.DELETE("/:id", apiInterfaceController.Delete)
				interfaces.PUT("/:id/status", apiInterfaceController.UpdateStatus)
				interfaces.POST("/:id/copy", apiInterfaceController.Copy)
				interfaces.POST("/execute", apiInterfaceController.Execute)
			}

			// 执行记录管理
			executionRecords := api.Group("/interface/execution/record")
			{
				executionRecords.POST("/list", apiInterfaceExecutionRecordController.List)
				executionRecords.GET("/:id", apiInterfaceExecutionRecordController.Detail)
				executionRecords.GET("/executor/:executorId", apiInterfaceExecutionRecordController.GetByExecutorID)
				executionRecords.GET("/stats/:interfaceId", apiInterfaceExecutionRecordController.GetExecutionStats)
				executionRecords.GET("/count", apiInterfaceExecutionRecordController.GetExecutionCount)
				executionRecords.DELETE("/cleanup", apiInterfaceExecutionRecordController.CleanupOldRecords)
			}

			// 仪表盘
			dashboard := api.Group("/dashboard")
			{
				dashboard.GET("/data", dashboardController.GetDashboardData)
			}

			// 活动管理
			activity := api.Group("/activity")
			{
				activity.GET("/list", activityController.List)
				activity.GET("/:id", activityController.Detail)
				activity.POST("", activityController.Create)
				activity.PUT("/:id", activityController.Update)
				activity.DELETE("/:id", activityController.Delete)
				activity.PUT("/:id/status", activityController.UpdateStatus)
			}

			// 活动模板管理
			activityTemplate := api.Group("/activity/template")
			{
				activityTemplate.GET("/list", activityTemplateController.List)
				activityTemplate.GET("/all", activityTemplateController.GetAll)
				activityTemplate.GET("/:id", activityTemplateController.Detail)
				activityTemplate.POST("", activityTemplateController.Create)
				activityTemplate.PUT("/:id", activityTemplateController.Update)
				activityTemplate.DELETE("/:id", activityTemplateController.Delete)
				activityTemplate.PUT("/:id/status", activityTemplateController.UpdateStatus)
				activityTemplate.POST("/:id/copy", activityTemplateController.Copy)
			}

			// 活动组件管理
			activityComponent := api.Group("/activity/component")
			{
				activityComponent.GET("/list", activityComponentController.List)
				activityComponent.GET("/all", activityComponentController.GetAll)
				activityComponent.GET("/:id", activityComponentController.Detail)
				activityComponent.POST("", activityComponentController.Create)
				activityComponent.PUT("/:id", activityComponentController.Update)
				activityComponent.DELETE("/:id", activityComponentController.Delete)
				activityComponent.PUT("/:id/status", activityComponentController.UpdateStatus)
				activityComponent.POST("/:id/copy", activityComponentController.Copy)
			}
		}
	})

	if err != nil {
		return nil, err
	}

	return router, nil
}
