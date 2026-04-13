package router

import (
	"github.com/bucketheadv/infra-market/internal/config"
	"github.com/bucketheadv/infra-market/internal/controller"
	"github.com/bucketheadv/infra-market/internal/middleware"
	"github.com/bucketheadv/infra-market/internal/service"
	"github.com/gin-gonic/gin"
)

func NewRouter(
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
	cfg *config.AppConfig,
) *gin.Engine {
	gin.SetMode(cfg.Server.GinMode())
	engine := gin.New()
	engine.Use(gin.Recovery())
	engine.Use(middleware.CORSMiddleware())
	engine.Use(middleware.RequestLogMiddleware())
	engine.Use(middleware.ErrorHandler())

	engine.POST("/auth/login", authController.Login)

	api := engine.Group("/")
	api.Use(middleware.AuthMiddleware(tokenService))
	{
		auth := api.Group("/auth")
		{
			auth.GET("/current/user", authController.GetCurrentUser)
			auth.GET("/user/menus", authController.GetUserMenus)
			auth.POST("/refresh/token", authController.RefreshToken)
			auth.POST("/logout", authController.Logout)
			auth.POST("/change/password", authController.ChangePassword)
		}

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

		executionRecords := api.Group("/interface/execution/record")
		{
			executionRecords.POST("/list", apiInterfaceExecutionRecordController.List)
			executionRecords.GET("/:id", apiInterfaceExecutionRecordController.Detail)
			executionRecords.GET("/executor/:executorId", apiInterfaceExecutionRecordController.GetByExecutorID)
			executionRecords.GET("/stats/:interfaceId", apiInterfaceExecutionRecordController.GetExecutionStats)
			executionRecords.GET("/count", apiInterfaceExecutionRecordController.GetExecutionCount)
			executionRecords.DELETE("/cleanup", apiInterfaceExecutionRecordController.CleanupOldRecords)
		}

		dashboard := api.Group("/dashboard")
		{
			dashboard.GET("/data", dashboardController.GetDashboardData)
		}

		activity := api.Group("/activity")
		{
			activity.GET("/list", activityController.List)
			activity.GET("/:id", activityController.Detail)
			activity.POST("", activityController.Create)
			activity.PUT("/:id", activityController.Update)
			activity.DELETE("/:id", activityController.Delete)
			activity.PUT("/:id/status", activityController.UpdateStatus)
		}

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

	return engine
}
