package service

import (
	"context"
	"fmt"
	"time"

	"github.com/bucketheadv/infra-market/internal/util"
	"github.com/go-redis/redis/v8"
)

type TokenService struct {
	redisClient *redis.Client
}

func NewTokenService(redisClient *redis.Client) *TokenService {
	return &TokenService{redisClient: redisClient}
}

const (
	tokenPrefix     = "token:"
	tokenExpireTime = 3 * 24 * 60 * 60 // 3天（秒）
)

// SaveToken 保存token到Redis
func (s *TokenService) SaveToken(uid uint64, token string) {
	key := fmt.Sprintf("%s%d", tokenPrefix, uid)
	s.redisClient.Set(context.Background(), key, token, time.Duration(tokenExpireTime)*time.Second)
}

// GetToken 从Redis获取token
func (s *TokenService) GetToken(uid uint64) (string, error) {
	key := fmt.Sprintf("%s%d", tokenPrefix, uid)
	return s.redisClient.Get(context.Background(), key).Result()
}

// ValidateToken 验证token是否有效
func (s *TokenService) ValidateToken(token string) bool {
	uid, err := util.GetUIDFromToken(token)
	if err != nil {
		return false
	}

	// 验证JWT token本身
	if !util.ValidateToken(token) {
		return false
	}

	// 验证Redis中的token
	storedToken, err := s.GetToken(uid)
	if err != nil {
		return false
	}

	return storedToken == token
}

// DeleteToken 删除token
func (s *TokenService) DeleteToken(uid uint64) {
	key := fmt.Sprintf("%s%d", tokenPrefix, uid)
	s.redisClient.Del(context.Background(), key)
}

// RefreshToken 刷新token
func (s *TokenService) RefreshToken(uid uint64, username string) (string, error) {
	token, err := util.GenerateToken(uid, username)
	if err != nil {
		return "", err
	}
	s.SaveToken(uid, token)
	return token, nil
}
