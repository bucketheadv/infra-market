package middleware

import (
	"log"
	"net/http"

	"github.com/bucketheadv/infra-market/internal/dto"
	"github.com/bucketheadv/infra-market/internal/enums"
	"github.com/gin-gonic/gin"
	"github.com/go-playground/validator/v10"
)

// ErrorHandler 全局错误处理中间件
func ErrorHandler() gin.HandlerFunc {
	return func(c *gin.Context) {
		c.Next()

		// 检查是否有错误
		if len(c.Errors) > 0 {
			err := c.Errors.Last()

			// 处理验证错误
			if validationErr, ok := err.Err.(validator.ValidationErrors); ok {
				handleValidationError(c, validationErr)
				return
			}

			// 处理其他错误
			log.Printf("Error: %v", err)
			c.JSON(http.StatusInternalServerError, dto.Error[interface{}](
				string(enums.ErrorMessageSystemError),
				http.StatusInternalServerError,
			))
		}
	}
}

// handleValidationError 处理验证错误
func handleValidationError(c *gin.Context, err validator.ValidationErrors) {
	errors := make(map[string]string)
	for _, e := range err {
		errors[e.Field()] = e.Tag()
	}

	errorMessage := "参数校验失败"
	c.JSON(http.StatusBadRequest, dto.ErrorWithDetail[interface{}](
		string(enums.ErrorMessageValidationFailed),
		errorMessage,
		http.StatusBadRequest,
	))
}
