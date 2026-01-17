package util

import (
	"errors"
	"fmt"
	"strconv"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

const (
	secretKey      = "infra-market-jwt-secret-key-2024-very-long-secret-key-for-security"
	expirationTime = 3 * 24 * 60 * 60 * 1000 // 3天（毫秒）
)

type Claims struct {
	Username string `json:"username"`
	jwt.RegisteredClaims
}

// GenerateToken 生成JWT token
func GenerateToken(uid uint64, username string) (string, error) {
	now := time.Now()
	expiration := now.Add(time.Duration(expirationTime) * time.Millisecond)

	claims := &Claims{
		Username: username,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   fmt.Sprintf("%d", uid), // 使用 subject claim 存储 uid，与 Java 版本保持一致
			IssuedAt:  jwt.NewNumericDate(now),
			ExpiresAt: jwt.NewNumericDate(expiration),
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	return token.SignedString([]byte(secretKey))
}

// GetUIDFromToken 从token中获取用户ID
func GetUIDFromToken(tokenString string) (uint64, error) {
	claims, err := getClaimsFromToken(tokenString)
	if err != nil {
		return 0, err
	}
	// 从 subject claim 中提取 uid，与 Java 版本保持一致
	uid, err := strconv.ParseUint(claims.Subject, 10, 64)
	if err != nil {
		return 0, err
	}
	return uid, nil
}

// ValidateToken 验证token是否有效
func ValidateToken(tokenString string) bool {
	_, err := getClaimsFromToken(tokenString)
	return err == nil
}

// getClaimsFromToken 从token中获取Claims
func getClaimsFromToken(tokenString string) (*Claims, error) {
	token, err := jwt.ParseWithClaims(tokenString, &Claims{}, func(token *jwt.Token) (any, error) {
		return []byte(secretKey), nil
	})

	if err != nil {
		return nil, err
	}

	if claims, ok := token.Claims.(*Claims); ok && token.Valid {
		return claims, nil
	}

	return nil, errors.New("invalid token")
}
