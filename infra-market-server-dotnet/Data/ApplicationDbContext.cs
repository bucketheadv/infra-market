using InfraMarket.Server.Entities;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Data;

public class ApplicationDbContext(DbContextOptions<ApplicationDbContext> options) : DbContext(options)
{
    public DbSet<User> Users { get; set; }
    public DbSet<Role> Roles { get; set; }
    public DbSet<Permission> Permissions { get; set; }
    public DbSet<UserRole> UserRoles { get; set; }
    public DbSet<RolePermission> RolePermissions { get; set; }
    public DbSet<ApiInterface> ApiInterfaces { get; set; }
    public DbSet<ApiInterfaceExecutionRecord> ApiInterfaceExecutionRecords { get; set; }
    public DbSet<Activity> Activities { get; set; }
    public DbSet<ActivityTemplate> ActivityTemplates { get; set; }
    public DbSet<ActivityComponent> ActivityComponents { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // 配置表名
        modelBuilder.Entity<User>().ToTable("user_info");
        modelBuilder.Entity<Role>().ToTable("role_info");
        modelBuilder.Entity<Permission>().ToTable("permission_info");
        modelBuilder.Entity<UserRole>().ToTable("user_role");
        modelBuilder.Entity<RolePermission>().ToTable("role_permission");
        modelBuilder.Entity<ApiInterface>().ToTable("api_interface");
        modelBuilder.Entity<ApiInterfaceExecutionRecord>().ToTable("api_interface_execution_record");
        modelBuilder.Entity<Activity>().ToTable("activity");
        modelBuilder.Entity<ActivityTemplate>().ToTable("activity_template");
        modelBuilder.Entity<ActivityComponent>().ToTable("activity_component");

        // 配置User实体
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Username).IsRequired().HasMaxLength(50).HasColumnName("username");
            entity.HasIndex(e => e.Username).IsUnique();
            entity.Property(e => e.Password).IsRequired().HasMaxLength(255).HasColumnName("password");
            entity.Property(e => e.Email).HasMaxLength(100).HasColumnName("email");
            entity.HasIndex(e => e.Email);
            entity.Property(e => e.Phone).HasMaxLength(20).HasColumnName("phone");
            entity.HasIndex(e => e.Phone);
            entity.Property(e => e.Status).IsRequired().HasMaxLength(20).HasDefaultValue("active").HasColumnName("status");
            entity.HasIndex(e => e.Status);
            entity.Property(e => e.LastLoginTime).HasColumnName("last_login_time");
            entity.HasIndex(e => e.LastLoginTime);
        });

        // 配置Role实体
        modelBuilder.Entity<Role>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(50).HasColumnName("name");
            entity.HasIndex(e => e.Name);
            entity.Property(e => e.Code).IsRequired().HasMaxLength(50).HasColumnName("code");
            entity.HasIndex(e => e.Code).IsUnique();
            entity.Property(e => e.Description).HasMaxLength(255).HasColumnName("description");
            entity.Property(e => e.Status).IsRequired().HasMaxLength(20).HasDefaultValue("active").HasColumnName("status");
            entity.HasIndex(e => e.Status);
        });

        // 配置Permission实体
        modelBuilder.Entity<Permission>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(50).HasColumnName("name");
            entity.Property(e => e.Code).IsRequired().HasMaxLength(50).HasColumnName("code");
            entity.HasIndex(e => e.Code).IsUnique();
            entity.Property(e => e.Type).IsRequired().HasMaxLength(20).HasDefaultValue("menu").HasColumnName("type");
            entity.HasIndex(e => e.Type);
            entity.Property(e => e.ParentId).HasColumnName("parent_id");
            entity.HasIndex(e => e.ParentId);
            entity.Property(e => e.Path).HasMaxLength(255).HasColumnName("path");
            entity.Property(e => e.Icon).HasMaxLength(100).HasColumnName("icon");
            entity.Property(e => e.Sort).IsRequired().HasDefaultValue(0).HasColumnName("sort");
            entity.HasIndex(e => e.Sort);
            entity.Property(e => e.Status).IsRequired().HasMaxLength(20).HasDefaultValue("active").HasColumnName("status");
            entity.HasIndex(e => e.Status);
        });

        // 配置UserRole实体
        modelBuilder.Entity<UserRole>(entity =>
        {
            entity.HasKey(e => new { e.UserId, e.RoleId });
            entity.Property(e => e.UserId).HasColumnName("uid");
            entity.Property(e => e.RoleId).HasColumnName("role_id");
            entity.HasIndex(e => e.UserId);
            entity.HasIndex(e => e.RoleId);
        });

        // 配置RolePermission实体
        modelBuilder.Entity<RolePermission>(entity =>
        {
            entity.HasKey(e => new { e.RoleId, e.PermissionId });
            entity.Property(e => e.RoleId).HasColumnName("role_id");
            entity.Property(e => e.PermissionId).HasColumnName("permission_id");
            entity.HasIndex(e => e.RoleId);
            entity.HasIndex(e => e.PermissionId);
        });

        // 配置ApiInterface实体
        modelBuilder.Entity<ApiInterface>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100).HasColumnName("name");
            entity.Property(e => e.Method).IsRequired().HasMaxLength(20).HasColumnName("method");
            entity.Property(e => e.Url).IsRequired().HasMaxLength(500).HasColumnName("url");
            entity.Property(e => e.Description).HasColumnName("description");
            entity.Property(e => e.PostType).HasMaxLength(50).HasColumnName("post_type");
            entity.Property(e => e.Params).HasColumnName("params");
            entity.Property(e => e.Status).HasColumnName("status");
            entity.Property(e => e.Environment).HasMaxLength(20).HasColumnName("environment");
            entity.Property(e => e.Timeout).HasColumnName("timeout");
            entity.Property(e => e.ValuePath).HasMaxLength(255).HasColumnName("value_path");
        });

        // 配置ApiInterfaceExecutionRecord实体
        modelBuilder.Entity<ApiInterfaceExecutionRecord>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.InterfaceId).HasColumnName("interface_id");
            entity.HasIndex(e => e.InterfaceId);
            entity.Property(e => e.ExecutorId).HasColumnName("executor_id");
            entity.HasIndex(e => e.ExecutorId);
            entity.Property(e => e.ExecutorName).IsRequired().HasMaxLength(50).HasColumnName("executor_name");
            entity.HasIndex(e => e.ExecutorName);
            entity.Property(e => e.RequestParams).HasColumnName("request_params");
            entity.Property(e => e.RequestHeaders).HasColumnName("request_headers");
            entity.Property(e => e.RequestBody).HasColumnName("request_body");
            entity.Property(e => e.ResponseStatus).HasColumnName("response_status");
            entity.Property(e => e.ResponseHeaders).HasColumnName("response_headers");
            entity.Property(e => e.ResponseBody).HasColumnName("response_body");
            entity.Property(e => e.ExecutionTime).HasColumnName("execution_time");
            entity.HasIndex(e => e.ExecutionTime);
            entity.Property(e => e.Success).HasColumnName("success");
            entity.HasIndex(e => e.Success);
            entity.Property(e => e.ErrorMessage).HasColumnName("error_message");
            entity.Property(e => e.Remark).HasColumnName("remark");
            entity.Property(e => e.ClientIp).HasMaxLength(50).HasColumnName("client_ip");
            entity.Property(e => e.UserAgent).HasMaxLength(500).HasColumnName("user_agent");
        });

        // 配置Activity实体
        modelBuilder.Entity<Activity>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100).HasColumnName("name");
            entity.HasIndex(e => e.Name);
            entity.Property(e => e.Description).HasMaxLength(500).HasColumnName("description");
            entity.Property(e => e.TemplateId).IsRequired().HasColumnName("template_id");
            entity.HasIndex(e => e.TemplateId);
            entity.Property(e => e.ConfigData).HasColumnName("config_data");
            entity.Property(e => e.Status).IsRequired().HasDefaultValue(1).HasColumnName("status");
            entity.HasIndex(e => e.Status);
        });

        // 配置ActivityTemplate实体
        modelBuilder.Entity<ActivityTemplate>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100).HasColumnName("name");
            entity.HasIndex(e => e.Name);
            entity.Property(e => e.Description).HasMaxLength(500).HasColumnName("description");
            entity.Property(e => e.Fields).HasColumnName("fields");
            entity.Property(e => e.Status).IsRequired().HasDefaultValue(1).HasColumnName("status");
            entity.HasIndex(e => e.Status);
        });

        // 配置ActivityComponent实体
        modelBuilder.Entity<ActivityComponent>(entity =>
        {
            entity.HasKey(e => e.Id);
            entity.Property(e => e.Id).HasColumnName("id");
            entity.Property(e => e.CreateTime).HasColumnName("create_time");
            entity.Property(e => e.UpdateTime).HasColumnName("update_time");
            entity.Property(e => e.Name).IsRequired().HasMaxLength(100).HasColumnName("name");
            entity.HasIndex(e => e.Name);
            entity.Property(e => e.Description).HasMaxLength(500).HasColumnName("description");
            entity.Property(e => e.Fields).HasColumnName("fields");
            entity.Property(e => e.Status).IsRequired().HasDefaultValue(1).HasColumnName("status");
            entity.HasIndex(e => e.Status);
        });
    }

    public override int SaveChanges()
    {
        UpdateTimestamps();
        return base.SaveChanges();
    }

    public override Task<int> SaveChangesAsync(CancellationToken cancellationToken = default)
    {
        UpdateTimestamps();
        return base.SaveChangesAsync(cancellationToken);
    }

    private void UpdateTimestamps()
    {
        var entries = ChangeTracker.Entries()
            .Where(e => e.Entity is BaseEntity && (e.State == EntityState.Added || e.State == EntityState.Modified));

        var now = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        foreach (var entry in entries)
        {
            var entity = (BaseEntity)entry.Entity;

            switch (entry.State)
            {
                case EntityState.Added:
                    entity.CreateTime = now;
                    entity.UpdateTime = now;
                    break;
                case EntityState.Modified:
                    entity.UpdateTime = now;
                    break;
                case EntityState.Detached:
                case EntityState.Unchanged:
                case EntityState.Deleted:
                    break;
                default:
                    throw new ArgumentOutOfRangeException();
            }
        }
    }
}
