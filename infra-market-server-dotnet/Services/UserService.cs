using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using InfraMarket.Server.Services.Interfaces;
using InfraMarket.Server.Utils;

namespace InfraMarket.Server.Services;

public class UserService(
    IUserRepository userRepository,
    IUserRoleRepository userRoleRepository) : IUserService
{
    public async Task<ApiData<PageResult<UserDto>>> GetUsersAsync(UserQueryDto query)
    {
        var (users, total) = await userRepository.PageAsync(query);
        var uids = users.Select(u => u.Id).ToList();
        var userRoles = await userRoleRepository.FindByUiDsAsync(uids);
        var userRoleMap = userRoles.GroupBy(ur => ur.UserId)
            .ToDictionary(g => g.Key, g => g.Select(ur => ur.RoleId).ToList());

        var userDtos = users.Select(user =>
        {
            var roleIds = userRoleMap.GetValueOrDefault(user.Id, []);
            return ConvertUserToDto(user, roleIds);
        }).ToList();

        var result = new PageResult<UserDto>
        {
            Records = userDtos,
            Total = total,
            Page = query.Page ?? 1,
            Size = query.Size ?? 10
        };

        return ApiData<PageResult<UserDto>>.Success(result);
    }

    public async Task<ApiData<UserDto>> GetUserAsync(ulong id)
    {
        var user = await userRepository.FindByUidAsync(id);
        if (user == null)
        {
            return ApiData<UserDto>.Error("用户不存在", 404);
        }

        var userRoles = await userRoleRepository.FindByUidAsync(id);
        var roleIds = userRoles.Select(ur => ur.RoleId).ToList();
        var userDto = ConvertUserToDto(user, roleIds);

        return ApiData<UserDto>.Success(userDto);
    }

    public async Task<ApiData<UserDto>> CreateUserAsync(UserFormDto dto)
    {
        var existingUser = await userRepository.FindByUsernameAsync(dto.Username);
        if (existingUser != null)
        {
            return ApiData<UserDto>.Error("用户名已存在", 400);
        }

        var user = new User
        {
            Username = dto.Username,
            Email = dto.Email,
            Phone = dto.Phone,
            Status = "active",
            Password = AesUtil.Encrypt(dto.Password ?? "123456")
        };

        await userRepository.CreateAsync(user);

        // 创建用户角色关联
        foreach (var roleId in dto.RoleIds)
        {
            await userRoleRepository.CreateAsync(new UserRole
            {
                UserId = user.Id,
                RoleId = roleId
            });
        }

        var userDto = ConvertUserToDto(user, dto.RoleIds);
        return ApiData<UserDto>.Success(userDto);
    }

    public async Task<ApiData<UserDto>> UpdateUserAsync(ulong id, UserUpdateDto dto)
    {
        var user = await userRepository.FindByUidAsync(id);
        if (user == null)
        {
            return ApiData<UserDto>.Error("用户不存在", 404);
        }

        user.Username = dto.Username;
        user.Email = dto.Email;
        user.Phone = dto.Phone;

        if (!string.IsNullOrEmpty(dto.Password))
        {
            user.Password = AesUtil.Encrypt(dto.Password);
        }

        await userRepository.UpdateAsync(user);

        // 更新用户角色关联
        await userRoleRepository.DeleteByUidAsync(id);
        foreach (var roleId in dto.RoleIds)
        {
            await userRoleRepository.CreateAsync(new UserRole
            {
                UserId = id,
                RoleId = roleId
            });
        }

        var userDto = ConvertUserToDto(user, dto.RoleIds);
        return ApiData<UserDto>.Success(userDto);
    }

    public async Task<ApiData<object>> DeleteUserAsync(ulong id)
    {
        await userRepository.DeleteAsync(id);
        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> UpdateUserStatusAsync(ulong id, StatusUpdateDto dto)
    {
        var user = await userRepository.FindByUidAsync(id);
        if (user == null)
        {
            return ApiData<object>.Error("用户不存在", 404);
        }

        user.Status = dto.Status;
        await userRepository.UpdateAsync(user);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> ResetPasswordAsync(ulong id)
    {
        var user = await userRepository.FindByUidAsync(id);
        if (user == null)
        {
            return ApiData<object>.Error("用户不存在", 404);
        }

        user.Password = AesUtil.Encrypt("123456");
        await userRepository.UpdateAsync(user);

        return ApiData<object>.Success(null!);
    }

    public async Task<ApiData<object>> BatchDeleteUsersAsync(BatchRequest request)
    {
        foreach (var id in request.Ids)
        {
            await userRepository.DeleteAsync(id);
        }

        return ApiData<object>.Success(null!);
    }

    private static UserDto ConvertUserToDto(User user, List<ulong> roleIds)
    {
        return new UserDto
        {
            Id = user.Id,
            Username = user.Username,
            Email = user.Email,
            Phone = user.Phone,
            Status = user.Status,
            LastLoginTime = DateTimeUtil.FormatLocalDateTime(user.LastLoginTime),
            RoleIds = roleIds,
            CreateTime = DateTimeUtil.FormatLocalDateTime(user.CreateTime),
            UpdateTime = DateTimeUtil.FormatLocalDateTime(user.UpdateTime)
        };
    }
}
