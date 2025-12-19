package router

import (
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/controller"
	"github.com/bucketheadv/infra-market/internal/middleware"
	"github.com/bucketheadv/infra-market/internal/repository"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
	"github.com/go-redis/redis/v8"
	"gorm.io/gorm"
)

// SetupRouter 设置路由
func SetupRouter(db *gorm.DB, cfg *config.Config) *gin.Engine {
	r := gin.Default()

	// 初始化Redis客户端
	redisClient := redis.NewClient(&redis.Options{
		Addr:     cfg.Redis.Host + ":" + cfg.Redis.Port,
		Password: cfg.Redis.Password,
		DB:       cfg.Redis.DB,
	})

	// 初始化Repository
	userRepo := repository.NewUserRepository(db)
	roleRepo := repository.NewRoleRepository(db)
	permissionRepo := repository.NewPermissionRepository(db)
	userRoleRepo := repository.NewUserRoleRepository(db)
	rolePermissionRepo := repository.NewRolePermissionRepository(db)
	apiInterfaceRepo := repository.NewApiInterfaceRepository(db)
	apiInterfaceExecutionRecordRepo := repository.NewApiInterfaceExecutionRecordRepository(db)

	// 初始化Service
	tokenService := service.NewTokenService(redisClient)
	authService := service.NewAuthService(userRepo, userRoleRepo, rolePermissionRepo, permissionRepo, tokenService)
	userService := service.NewUserService(userRepo, userRoleRepo)

	// 全局中间件
	r.Use(middleware.CORSMiddleware())
	r.Use(middleware.ErrorHandler())

	// 认证路由（不需要token）
	auth := r.Group("/auth")
	{
		authController := controller.NewAuthController(authService)
		auth.POST("/login", authController.Login)
		auth.GET("/current/user", middleware.AuthMiddleware(tokenService), authController.GetCurrentUser)
		auth.GET("/user/menus", middleware.AuthMiddleware(tokenService), authController.GetUserMenus)
		auth.POST("/refresh/token", middleware.AuthMiddleware(tokenService), authController.RefreshToken)
		auth.POST("/logout", middleware.AuthMiddleware(tokenService), authController.Logout)
		auth.POST("/change/password", middleware.AuthMiddleware(tokenService), authController.ChangePassword)
	}

	// 初始化更多Service
	roleRepo = repository.NewRoleRepository(db)
	permissionRepo = repository.NewPermissionRepository(db)
	rolePermissionRepo = repository.NewRolePermissionRepository(db)
	apiInterfaceRepo = repository.NewApiInterfaceRepository(db)
	apiInterfaceExecutionRecordRepo = repository.NewApiInterfaceExecutionRecordRepository(db)

	roleService := service.NewRoleService(roleRepo, rolePermissionRepo, userRoleRepo)
	permissionService := service.NewPermissionService(permissionRepo, rolePermissionRepo)
	apiInterfaceService := service.NewApiInterfaceService(apiInterfaceRepo, apiInterfaceExecutionRecordRepo, userRepo)
	apiInterfaceExecutionRecordService := service.NewApiInterfaceExecutionRecordService(apiInterfaceExecutionRecordRepo)
	dashboardService := service.NewDashboardService(userRepo, roleRepo, permissionRepo, apiInterfaceRepo)

	// 需要认证的路由
	api := r.Group("/")
	api.Use(middleware.AuthMiddleware(tokenService))
	{
		// 用户管理
		users := api.Group("/users")
		{
			userController := controller.NewUserController(userService)
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
			roleController := controller.NewRoleController(roleService)
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
			permissionController := controller.NewPermissionController(permissionService)
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
		interfaces := api.Group("/api/interface")
		{
			apiInterfaceController := controller.NewApiInterfaceController(apiInterfaceService)
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
		executionRecords := api.Group("/api/interface/execution/record")
		{
			recordController := controller.NewApiInterfaceExecutionRecordController(apiInterfaceExecutionRecordService)
			executionRecords.POST("/list", recordController.List)
			executionRecords.GET("/:id", recordController.Detail)
			executionRecords.GET("/executor/:executorId", recordController.GetByExecutorID)
			executionRecords.GET("/stats/:interfaceId", recordController.GetExecutionStats)
			executionRecords.GET("/count", recordController.GetExecutionCount)
			executionRecords.DELETE("/cleanup", recordController.CleanupOldRecords)
		}

		// 仪表盘
		dashboard := api.Group("/dashboard")
		{
			dashboardController := controller.NewDashboardController(dashboardService)
			dashboard.GET("/data", dashboardController.GetDashboardData)
		}
	}

	return r
}
