using InfraMarket.Server.Config;
using InfraMarket.Server.Data;
using InfraMarket.Server.Middleware;
using InfraMarket.Server.Repositories;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services;
using InfraMarket.Server.Services.Interfaces;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using StackExchange.Redis;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// 配置服务
var configuration = builder.Configuration;
var services = builder.Services;

// 配置数据库
var connectionString = $"Server={configuration["Database:Host"]};Port={configuration["Database:Port"]};Database={configuration["Database:Database"]};User={configuration["Database:User"]};Password={configuration["Database:Password"]};";
services.AddDbContext<ApplicationDbContext>(options =>
    options.UseMySql(connectionString, ServerVersion.AutoDetect(connectionString)));

// Redis配置在Service注册时处理

// 配置JWT认证
var jwtSecret = configuration["JWT:Secret"] ?? throw new InvalidOperationException("JWT Secret not configured");
var key = Encoding.UTF8.GetBytes(jwtSecret);
services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuerSigningKey = true,
        IssuerSigningKey = new SymmetricSecurityKey(key),
        ValidateIssuer = false,
        ValidateAudience = false,
        ValidateLifetime = true,
        ClockSkew = TimeSpan.Zero
    };
    options.Events = new JwtBearerEvents
    {
        OnTokenValidated = async context =>
        {
            var tokenService = context.HttpContext.RequestServices.GetRequiredService<ITokenService>();
            var token = context.Request.Headers["Authorization"].ToString().Replace("Bearer ", "");
            if (!string.IsNullOrEmpty(token) && !tokenService.ValidateToken(token))
            {
                context.Fail("Token验证失败");
            }
            await Task.CompletedTask;
        }
    };
});

// 注册配置
services.Configure<JwtConfig>(configuration.GetSection("JWT"));
services.Configure<DatabaseConfig>(configuration.GetSection("Database"));
services.Configure<RedisConfig>(configuration.GetSection("Redis"));

// 注册Repository
services.AddScoped<IUserRepository, UserRepository>();
services.AddScoped<IRoleRepository, RoleRepository>();
services.AddScoped<IPermissionRepository, PermissionRepository>();
services.AddScoped<IUserRoleRepository, UserRoleRepository>();
services.AddScoped<IRolePermissionRepository, RolePermissionRepository>();
services.AddScoped<IApiInterfaceRepository, ApiInterfaceRepository>();
services.AddScoped<IApiInterfaceExecutionRecordRepository, ApiInterfaceExecutionRecordRepository>();

// 注册Service
services.AddSingleton<IConnectionMultiplexer>(sp =>
{
    var config = sp.GetRequiredService<IConfiguration>();
    var redisConnectionString = $"{config["Redis:Host"]}:{config["Redis:Port"]}";
    return ConnectionMultiplexer.Connect(redisConnectionString);
});

services.AddScoped<ITokenService, TokenService>();
services.AddScoped<IAuthService, AuthService>();
services.AddScoped<IUserService, UserService>();
services.AddScoped<IRoleService, RoleService>();
services.AddScoped<IPermissionService, PermissionService>();
services.AddScoped<IApiInterfaceService, ApiInterfaceService>();
services.AddScoped<IApiInterfaceExecutionRecordService, ApiInterfaceExecutionRecordService>();
services.AddScoped<IDashboardService, DashboardService>();

// 注册Controller，添加全局路由前缀 /api
services.AddControllers(_ =>
{
    // 确保控制器被正确发现
})
.AddJsonOptions(options =>
{
    // 使用camelCase命名策略，与Go版本保持一致
    options.JsonSerializerOptions.PropertyNamingPolicy = System.Text.Json.JsonNamingPolicy.CamelCase;
    options.JsonSerializerOptions.DefaultIgnoreCondition = System.Text.Json.Serialization.JsonIgnoreCondition.WhenWritingNull;
})
.ConfigureApiBehaviorOptions(options =>
{
    // 禁用自动模型验证，使用自定义验证
    options.SuppressModelStateInvalidFilter = true;
});

// 配置CORS
services.AddCors(options =>
{
    options.AddDefaultPolicy(policy =>
    {
        policy.AllowAnyOrigin()
              .AllowAnyMethod()
              .AllowAnyHeader();
    });
});

var app = builder.Build();

// 配置HTTP请求管道
if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
}

app.UseCors();
app.UseMiddleware<ErrorHandlingMiddleware>();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();

// 启动服务器
var port = configuration["Server:Port"] ?? "8080";
app.Run($"http://localhost:{port}");
