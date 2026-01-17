using System.ComponentModel.DataAnnotations;

namespace InfraMarket.Server.DTOs;

public class LoginRequest
{
    [Required]
    [MinLength(2)]
    [MaxLength(20)]
    public string Username { get; set; } = string.Empty;

    [Required]
    [MinLength(6)]
    [MaxLength(20)]
    public string Password { get; set; } = string.Empty;
}

public class LoginResponse
{
    public string Token { get; set; } = string.Empty;
    public UserDto User { get; set; } = new();
    public List<string> Permissions { get; set; } = new();
}

public class UserDto
{
    public ulong Id { get; set; }
    public string Username { get; set; } = string.Empty;
    public string? Email { get; set; }
    public string? Phone { get; set; }
    public string Status { get; set; } = string.Empty;
    public string? LastLoginTime { get; set; }
    public List<ulong> RoleIds { get; set; } = new();
    public string CreateTime { get; set; } = string.Empty;
    public string UpdateTime { get; set; } = string.Empty;
}

public class UserFormDto
{
    [Required]
    [MinLength(2)]
    [MaxLength(20)]
    public string Username { get; set; } = string.Empty;

    [EmailAddress]
    public string? Email { get; set; }

    public string? Phone { get; set; }

    [MinLength(6)]
    [MaxLength(20)]
    public string? Password { get; set; }

    [Required]
    [MinLength(1)]
    public List<ulong> RoleIds { get; set; } = new();
}

public class UserUpdateDto
{
    [Required]
    [MinLength(2)]
    [MaxLength(20)]
    public string Username { get; set; } = string.Empty;

    [EmailAddress]
    public string? Email { get; set; }

    public string? Phone { get; set; }

    [MinLength(6)]
    [MaxLength(20)]
    public string? Password { get; set; }

    [Required]
    [MinLength(1)]
    public List<ulong> RoleIds { get; set; } = new();
}

public class UserQueryDto
{
    public string? Username { get; set; }
    public string? Status { get; set; }
    public int? Page { get; set; }
    public int? Size { get; set; }
}

public class ChangePasswordRequest
{
    [Required]
    [MinLength(6)]
    [MaxLength(20)]
    public string OldPassword { get; set; } = string.Empty;

    [Required]
    [MinLength(6)]
    [MaxLength(20)]
    public string NewPassword { get; set; } = string.Empty;
}
