using InfraMarket.Server.Data;
using InfraMarket.Server.DTOs;
using InfraMarket.Server.Entities;
using InfraMarket.Server.Repositories.Interfaces;
using Microsoft.EntityFrameworkCore;

namespace InfraMarket.Server.Repositories;

public class UserRepository(ApplicationDbContext context) : IUserRepository
{
    public async Task<User?> FindByUidAsync(ulong uid)
    {
        return await context.Users
            .Where(u => u.Id == uid && u.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<List<User>> FindByUiDsAsync(List<ulong> uids)
    {
        return await context.Users
            .Where(u => uids.Contains(u.Id) && u.Status != "deleted")
            .ToListAsync();
    }

    public async Task<User?> FindByUsernameAsync(string username)
    {
        return await context.Users
            .Where(u => u.Username == username && u.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<User?> FindByEmailAsync(string email)
    {
        return await context.Users
            .Where(u => u.Email == email && u.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<User?> FindByPhoneAsync(string phone)
    {
        return await context.Users
            .Where(u => u.Phone == phone && u.Status != "deleted")
            .FirstOrDefaultAsync();
    }

    public async Task<(List<User> users, long total)> PageAsync(UserQueryDto query)
    {
        var queryable = context.Users.Where(u => u.Status != "deleted");

        if (!string.IsNullOrEmpty(query.Username))
        {
            queryable = queryable.Where(u => u.Username.Contains(query.Username));
        }

        if (!string.IsNullOrEmpty(query.Status))
        {
            queryable = queryable.Where(u => u.Status == query.Status);
        }

        var total = await queryable.CountAsync();

        var page = query.Page ?? 1;
        var size = query.Size ?? 10;
        var skip = (page - 1) * size;

        var users = await queryable
            .OrderBy(u => u.Id)
            .Skip(skip)
            .Take(size)
            .ToListAsync();

        return (users, total);
    }

    public async Task CreateAsync(User user)
    {
        context.Users.Add(user);
        await context.SaveChangesAsync();
    }

    public async Task UpdateAsync(User user)
    {
        context.Users.Update(user);
        await context.SaveChangesAsync();
    }

    public async Task DeleteAsync(ulong uid)
    {
        var user = await FindByUidAsync(uid);
        if (user != null)
        {
            user.Status = "deleted";
            await UpdateAsync(user);
        }
    }

    public async Task<List<User>> GetRecentLoginUsersAsync(int limit)
    {
        return await context.Users
            .Where(u => u.Status != "deleted" && u.LastLoginTime != null)
            .OrderByDescending(u => u.LastLoginTime)
            .Take(limit)
            .ToListAsync();
    }

    public async Task<long> CountAsync()
    {
        return await context.Users
            .Where(u => u.Status != "deleted")
            .CountAsync();
    }

    public async Task<long> CountBeforeDateAsync(long timestamp)
    {
        return await context.Users
            .Where(u => u.Status != "deleted" && u.CreateTime <= timestamp)
            .CountAsync();
    }
}
