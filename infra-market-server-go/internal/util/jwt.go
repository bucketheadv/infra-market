package util

import (
	"errors"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

const (
	secretKey      = "infra-market-jwt-secret-key-2024-very-long-secret-key-for-security"
	expirationTime = 3 * 24 * 60 * 60 * 1000 // 3天（毫秒）
)

type Claims struct {
	UID      uint64 `json:"uid"`
	Username string `json:"username"`
	jwt.RegisteredClaims
}

// GenerateToken 生成JWT token
func GenerateToken(uid uint64, username string) (string, error) {
	now := time.Now()
	expiration := now.Add(time.Duration(expirationTime) * time.Millisecond)

	claims := &Claims{
		UID:      uid,
		Username: username,
		RegisteredClaims: jwt.RegisteredClaims{
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
	return claims.UID, nil
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
