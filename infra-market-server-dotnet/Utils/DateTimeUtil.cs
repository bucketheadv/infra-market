namespace InfraMarket.Server.Utils;

public static class DateTimeUtil
{
    /// <summary>
    /// 将Unix时间戳（毫秒）转换为本地时区的日期时间字符串
    /// </summary>
    /// <param name="timestamp">Unix时间戳（毫秒）</param>
    /// <returns>格式化的日期时间字符串 "yyyy-MM-dd HH:mm:ss"</returns>
    public static string FormatLocalDateTime(long timestamp)
    {
        return DateTimeOffset.FromUnixTimeMilliseconds(timestamp)
            .ToLocalTime()
            .ToString("yyyy-MM-dd HH:mm:ss");
    }

    /// <summary>
    /// 将Unix时间戳（毫秒）转换为本地时区的日期时间字符串（可空版本）
    /// </summary>
    /// <param name="timestamp">Unix时间戳（毫秒），可为null</param>
    /// <returns>格式化的日期时间字符串 "yyyy-MM-dd HH:mm:ss"，如果timestamp为null则返回null</returns>
    public static string? FormatLocalDateTime(long? timestamp)
    {
        return !timestamp.HasValue ? null : FormatLocalDateTime(timestamp.Value);
    }
}
