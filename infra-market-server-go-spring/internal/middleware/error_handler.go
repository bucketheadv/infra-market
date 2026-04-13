package middleware

import (
	"errors"
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/enums"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
	"github.com/go-spring/log"
)

// ErrorHandler 全局错误处理中间件
func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		// 检查是否有错误
		if len(c.Errors) > 0 {
			err := c.Errors.Last()

			// 处理验证错误
			if validationErr, ok := errors.AsType[validator.ValidationErrors](err.Err); ok {
				handleValidationError(c, validationErr)
				return
			}

			// 处理其他错误
			log.Errorf(c.Request.Context(), log.TagAppDef, "Error: %v", err)
			c.JSON(http.StatusInternalServerError, dto.Error[any](
				string(enums.ErrorMessageSystemError),
				http.StatusInternalServerError,
			))
		}
	}
}

// handleValidationError 处理验证错误
func handleValidationError(c *gin.Context, err validator.ValidationErrors) {
	errorMessage := err.Error()
	if errorMessage == "" {
		errorMessage = "参数校验失败"
	}
	c.JSON(http.StatusBadRequest, dto.ErrorWithDetail[any](
		string(enums.ErrorMessageValidationFailed),
		errorMessage,
		http.StatusBadRequest,
	))
}
