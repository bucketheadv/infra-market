package util

import (
	"crypto/aes"
	"crypto/cipher"
	"encoding/base64"
	"errors"
)

const defaultKey = "InfraMarketSecretKey2024"

// Encrypt 加密字符串
func Encrypt(text string, key ...string) (string, error) {
	encryptKey := defaultKey
	if len(key) > 0 && key[0] != "" {
		encryptKey = key[0]
	}

	// 确保密钥长度为16、24或32字节（AES-128、AES-192、AES-256）
	keyBytes := []byte(encryptKey)
	if len(keyBytes) < 16 {
		// 如果密钥太短，填充到16字节
		for len(keyBytes) < 16 {
			keyBytes = append(keyBytes, 0)
		}
	} else if len(keyBytes) > 32 {
		// 如果密钥太长，截取前32字节
		keyBytes = keyBytes[:32]
	}

	block, err := aes.NewCipher(keyBytes)
	if err != nil {
		return "", err
	}

	plaintext := []byte(text)

	// 使用ECB模式（需要手动实现，因为Go标准库不直接支持ECB）
	// 为了简化，这里使用CBC模式
	iv := make([]byte, aes.BlockSize)
	stream := cipher.NewCBCEncrypter(block, iv)

	// PKCS5Padding
	plaintext = pkcs5Padding(plaintext, aes.BlockSize)

	ciphertext := make([]byte, len(plaintext))
	stream.CryptBlocks(ciphertext, plaintext)

	return base64.StdEncoding.EncodeToString(ciphertext), nil
}

// Decrypt 解密字符串
func Decrypt(encryptedText string, key ...string) (string, error) {
	decryptKey := defaultKey
	if len(key) > 0 && key[0] != "" {
		decryptKey = key[0]
	}

	// 确保密钥长度为16、24或32字节
	keyBytes := []byte(decryptKey)
	if len(keyBytes) < 16 {
		for len(keyBytes) < 16 {
			keyBytes = append(keyBytes, 0)
		}
	} else if len(keyBytes) > 32 {
		keyBytes = keyBytes[:32]
	}

	block, err := aes.NewCipher(keyBytes)
	if err != nil {
		return "", err
	}

	ciphertext, err := base64.StdEncoding.DecodeString(encryptedText)
	if err != nil {
		return "", err
	}

	if len(ciphertext) < aes.BlockSize {
		return "", errors.New("ciphertext too short")
	}

	iv := make([]byte, aes.BlockSize)
	stream := cipher.NewCBCDecrypter(block, iv)

	plaintext := make([]byte, len(ciphertext))
	stream.CryptBlocks(plaintext, ciphertext)

	// PKCS5Unpadding
	plaintext = pkcs5Unpadding(plaintext)

	return string(plaintext), nil
}

// Matches 验证密码
func Matches(rawPassword, encodedPassword string, key ...string) bool {
	decrypted, err := Decrypt(encodedPassword, key...)
	if err != nil {
		return false
	}
	return rawPassword == decrypted
}

// pkcs5Padding PKCS5填充
func pkcs5Padding(data []byte, blockSize int) []byte {
	padding := blockSize - len(data)%blockSize
	padtext := make([]byte, padding)
	for i := range padtext {
		padtext[i] = byte(padding)
	}
	return append(data, padtext...)
}

// pkcs5Unpadding PKCS5去填充
func pkcs5Unpadding(data []byte) []byte {
	length := len(data)
	unpadding := int(data[length-1])
	return data[:(length - unpadding)]
}
