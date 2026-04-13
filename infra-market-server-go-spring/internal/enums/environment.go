package enums

// Environment 接口环境枚举
type Environment string

const (
	EnvironmentTest       Environment = "TEST"
	EnvironmentProduction Environment = "PRODUCTION"
)

func (e Environment) Code() string {
	return string(e)
}

func EnvironmentFromCode(code string) *Environment {
	environments := map[string]Environment{
		"TEST":       EnvironmentTest,
		"PRODUCTION": EnvironmentProduction,
	}
	if env, ok := environments[code]; ok {
		return &env
	}
	return nil
}
