using System.Net;
using System.Text.Json;
using InfraMarket.Server.DTOs;

namespace InfraMarket.Server.Middleware;

public class ErrorHandlingMiddleware(
    RequestDelegate next,
    ILogger<ErrorHandlingMiddleware> logger)
{
    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await next(context);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "An unhandled exception occurred");
            await HandleExceptionAsync(context, ex);
        }
    }

    private static Task HandleExceptionAsync(HttpContext context, Exception exception)
    {
        var code = HttpStatusCode.InternalServerError;
        var message = "服务器内部错误";

        if (exception is ArgumentException or ArgumentNullException)
        {
            code = HttpStatusCode.BadRequest;
            message = exception.Message;
        }

        var result = JsonSerializer.Serialize(ApiData<object>.Error(message, (int)code));
        context.Response.ContentType = "application/json";
        context.Response.StatusCode = (int)code;
        return context.Response.WriteAsync(result);
    }
}
