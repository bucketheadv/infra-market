using System.Security.Cryptography;
using System.Text;

namespace InfraMarket.Server.Utils;

public static class AesUtil
{
    private const string DefaultKey = "InfraMarketSecretKey2024";

    /// <summary>
    /// 加密字符串
    /// </summary>
    public static string Encrypt(string text, string? key = null)
    {
        var encryptKey = key ?? DefaultKey;
        var keyBytes = PrepareKey(encryptKey);

        using var aes = Aes.Create();
        aes.Key = keyBytes;
        aes.Mode = CipherMode.CBC;
        aes.Padding = PaddingMode.PKCS7;
        aes.IV = new byte[aes.BlockSize / 8]; // 全零IV，与Go版本一致

        using var encryptor = aes.CreateEncryptor();
        var plaintextBytes = Encoding.UTF8.GetBytes(text);
        var ciphertextBytes = encryptor.TransformFinalBlock(plaintextBytes, 0, plaintextBytes.Length);

        return Convert.ToBase64String(ciphertextBytes);
    }

    /// <summary>
    /// 解密字符串
    /// </summary>
    private static string Decrypt(string encryptedText, string? key = null)
    {
        var decryptKey = key ?? DefaultKey;
        var keyBytes = PrepareKey(decryptKey);

        try
        {
            var ciphertextBytes = Convert.FromBase64String(encryptedText);

            if (ciphertextBytes.Length < 16)
            {
                throw new ArgumentException("Ciphertext too short");
            }

            using var aes = Aes.Create();
            aes.Key = keyBytes;
            aes.Mode = CipherMode.CBC;
            aes.Padding = PaddingMode.PKCS7;
            aes.IV = new byte[aes.BlockSize / 8]; // 全零IV，与Go版本一致

            using var decryptor = aes.CreateDecryptor();
            var plaintextBytes = decryptor.TransformFinalBlock(ciphertextBytes, 0, ciphertextBytes.Length);

            return Encoding.UTF8.GetString(plaintextBytes);
        }
        catch
        {
            throw new ArgumentException("Invalid encrypted text");
        }
    }

    /// <summary>
    /// 验证密码
    /// </summary>
    public static bool Matches(string rawPassword, string encodedPassword, string? key = null)
    {
        try
        {
            var decrypted = Decrypt(encodedPassword, key);
            return rawPassword == decrypted;
        }
        catch
        {
            return false;
        }
    }

    /// <summary>
    /// 准备密钥，确保长度为16、24或32字节
    /// </summary>
    private static byte[] PrepareKey(string key)
    {
        var keyBytes = Encoding.UTF8.GetBytes(key);

        switch (keyBytes.Length)
        {
            case < 16:
            {
                // 如果密钥太短，填充到16字节
                var paddedKey = new byte[16];
                Array.Copy(keyBytes, paddedKey, keyBytes.Length);
                return paddedKey;
            }
            case > 32:
            {
                // 如果密钥太长，截取前32字节
                var truncatedKey = new byte[32];
                Array.Copy(keyBytes, truncatedKey, 32);
                return truncatedKey;
            }
            case > 16 and < 24:
            {
                // 如果密钥长度在16-24之间，填充到24字节
                var paddedKey = new byte[24];
                Array.Copy(keyBytes, paddedKey, keyBytes.Length);
                return paddedKey;
            }
            case > 24 and < 32:
            {
                // 如果密钥长度在24-32之间，填充到32字节
                var paddedKey = new byte[32];
                Array.Copy(keyBytes, paddedKey, keyBytes.Length);
                return paddedKey;
            }
            default:
                return keyBytes;
        }
    }
}
