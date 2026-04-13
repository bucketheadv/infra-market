package main

import (
	"github.com/bucketheadv/infra-market/internal/bootstrap"
	"github.com/go-spring/spring-core/gs"
)

func main() {
	gs.Property("spring.app.config-local.dir", "./config")
	gs.EnableSimpleHttpServer(false)
	gs.EnableSimplePProfServer(false)
	bootstrap.Register()
	gs.Run()
}
