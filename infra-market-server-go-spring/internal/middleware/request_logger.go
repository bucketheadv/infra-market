package middleware

import (
	"time"

	"github.com/gin-gonic/gin"
	"github.com/go-spring/log"
)

func RequestLogMiddleware() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()
		c.Next()

		latency := time.Since(start)
		log.Infof(c.Request.Context(), log.TagAppDef,
			"[REQ] method=%s path=%s status=%d latency=%s ip=%s",
			c.Request.Method,
			c.Request.URL.Path,
			c.Writer.Status(),
			latency.String(),
			c.ClientIP(),
		)
	}
}
