package config

type AppConfig struct {
	Server ServerConfig `value:"${server}"`
}

type ServerConfig struct {
	Port string `value:"${port:=8080}"`
	Mode string `value:"${mode:=release}"`
}

func (c ServerConfig) GinMode() string {
	switch c.Mode {
	case "debug":
		return "debug"
	case "test":
		return "test"
	default:
		return "release"
	}
}
