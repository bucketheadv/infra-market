package config

import (
	"fmt"
	"os"

	"github.com/pelletier/go-toml/v2"
)

// Config 应用配置
type Config struct {
	Server   ServerConfig   `toml:"server"`
	Database DatabaseConfig `toml:"database"`
	JWT      JWTConfig      `toml:"jwt"`
	Redis    RedisConfig    `toml:"redis"`
}

// ServerConfig 服务器配置
type ServerConfig struct {
	Port string `toml:"port"`
}

// DatabaseConfig 数据库配置
type DatabaseConfig struct {
	Host     string `toml:"host"`
	Port     string `toml:"port"`
	User     string `toml:"user"`
	Password string `toml:"password"`
	Database string `toml:"database"`
}

// JWTConfig JWT配置
type JWTConfig struct {
	Secret     string `toml:"secret"`
	Expiration int64  `toml:"expiration"` // 毫秒
}

// RedisConfig Redis配置
type RedisConfig struct {
	Host     string `toml:"host"`
	Port     string `toml:"port"`
	Password string `toml:"password"`
	DB       int    `toml:"db"`
}

// Load 从配置文件加载配置
func Load(configPath string) (*Config, error) {
	// 读取配置文件
	data, err := os.ReadFile(configPath)
	if err != nil {
		return nil, fmt.Errorf("无法读取配置文件 %s: %w", configPath, err)
	}

	// 解析 TOML 配置
	cfg := &Config{}
	if err := toml.Unmarshal(data, cfg); err != nil {
		return nil, fmt.Errorf("解析配置文件失败: %w", err)
	}

	return cfg, nil
}
