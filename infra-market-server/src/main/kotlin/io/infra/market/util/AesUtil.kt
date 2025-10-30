package io.infra.market.util

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import java.util.Base64

object AesUtil {
    
    private const val ALGORITHM = "AES"
    private const val KEY_SIZE = 256
    private const val TRANSFORMATION = "AES/ECB/PKCS5Padding"
    
    // 默认密钥，实际项目中应该从配置文件或环境变量中读取
    private const val DEFAULT_KEY = "InfraMarketSecretKey2024"

    /**
     * 生成AES密钥
     */
    fun generateKey(): String {
        val keyGenerator = KeyGenerator.getInstance(ALGORITHM)
        keyGenerator.init(KEY_SIZE)
        val secretKey = keyGenerator.generateKey()
        return Base64.getEncoder().encodeToString(secretKey.encoded)
    }
    
    /**
     * 加密字符串
     */
    fun encrypt(text: String, key: String = DEFAULT_KEY): String {
        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }
    
    /**
     * 解密字符串
     */
    fun decrypt(encryptedText: String, key: String = DEFAULT_KEY): String {
        val secretKey = SecretKeySpec(key.toByteArray(), ALGORITHM)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val encryptedBytes = Base64.getDecoder().decode(encryptedText)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }
    
    /**
     * 验证密码
     */
    fun matches(rawPassword: String, encodedPassword: String, key: String = DEFAULT_KEY): Boolean {
        return try {
            val decryptedPassword = decrypt(encodedPassword, key)
            rawPassword == decryptedPassword
        } catch (_: Exception) {
            false
        }
    }

//    @JvmStatic
//    fun main(args: Array<String>) {
//        println(encrypt("123456", "InfraMarketSecretKey2024"))
//    }
}
