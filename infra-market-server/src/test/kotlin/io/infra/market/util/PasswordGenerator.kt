package io.infra.market.util

fun main() {
    val password = "123456"
    val encryptedPassword = AesUtil.encrypt(password)
    println("原始密码: $password")
    println("AES加密后: $encryptedPassword")
    
    // 验证解密
    val decryptedPassword = AesUtil.decrypt(encryptedPassword)
    println("解密后: $decryptedPassword")
    println("验证结果: ${password == decryptedPassword}")
}
