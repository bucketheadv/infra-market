namespace InfraMarket.Server.DTOs;

public class ApiData<T>
{
    public int Code { get; set; }
    public string Message { get; set; } = string.Empty;
    public T? Data { get; set; }

    public static ApiData<T> Success(T data)
    {
        return new ApiData<T>
        {
            Code = 200,
            Message = "success",
            Data = data
        };
    }

    public static ApiData<T> Error(string message, int code)
    {
        return new ApiData<T>
        {
            Code = code,
            Message = message
        };
    }
}

public class PageResult<T>
{
    public List<T> Records { get; set; } = [];
    public long Total { get; set; }
    public int Page { get; set; }
    public int Size { get; set; }
}

public class BatchRequest
{
    public List<ulong> Ids { get; set; } = [];
}

public class StatusUpdateDto
{
    public string Status { get; set; } = string.Empty;
}
