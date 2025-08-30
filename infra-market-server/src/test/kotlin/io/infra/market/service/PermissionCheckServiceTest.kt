package io.infra.market.service

import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.repository.entity.UserRole
import io.infra.market.repository.entity.RolePermission
import io.infra.market.repository.entity.Permission
import io.infra.market.enums.StatusEnum
import io.infra.market.util.AuthThreadLocal
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.AfterEach
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class PermissionCheckServiceTest {

    @Mock
    private lateinit var userRoleDao: UserRoleDao

    @Mock
    private lateinit var rolePermissionDao: RolePermissionDao

    @Mock
    private lateinit var permissionDao: PermissionDao

    private lateinit var permissionCheckService: PermissionCheckService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        permissionCheckService = PermissionCheckService(userRoleDao, rolePermissionDao, permissionDao)
    }

    @AfterEach
    fun tearDown() {
        AuthThreadLocal.clearCurrentUserId()
    }

    @Test
    fun `test hasPermission with valid permission`() {
        // 设置当前用户ID
        AuthThreadLocal.setCurrentUserId(1L)
        
        // 模拟用户角色
        val userRoles = listOf(
            UserRole(userId = 1L, roleId = 1L)
        )
        `when`(userRoleDao.findByUserId(1L)).thenReturn(userRoles)
        
        // 模拟角色权限
        val rolePermissions = listOf(
            RolePermission(roleId = 1L, permissionId = 1L)
        )
        `when`(rolePermissionDao.findByRoleIds(listOf(1L))).thenReturn(rolePermissions)
        
        // 模拟权限
        val permissions = listOf(
            Permission(id = 1L, code = "user:list", status = StatusEnum.ACTIVE.code)
        )
        `when`(permissionDao.findByIds(listOf(1L))).thenReturn(permissions)
        
        // 测试权限检查
        val hasPermission = permissionCheckService.hasPermission("user:list")
        
        // 验证结果
        assert(hasPermission) { "用户应该拥有user:list权限" }
    }

    @Test
    fun `test hasPermission with invalid permission`() {
        // 设置当前用户ID
        AuthThreadLocal.setCurrentUserId(1L)
        
        // 模拟用户角色
        val userRoles = listOf(
            UserRole(userId = 1L, roleId = 1L)
        )
        `when`(userRoleDao.findByUserId(1L)).thenReturn(userRoles)
        
        // 模拟角色权限
        val rolePermissions = listOf(
            RolePermission(roleId = 1L, permissionId = 1L)
        )
        `when`(rolePermissionDao.findByRoleIds(listOf(1L))).thenReturn(rolePermissions)
        
        // 模拟权限
        val permissions = listOf(
            Permission(id = 1L, code = "user:list", status = StatusEnum.ACTIVE.code)
        )
        `when`(permissionDao.findByIds(listOf(1L))).thenReturn(permissions)
        
        // 测试权限检查
        val hasPermission = permissionCheckService.hasPermission("user:create")
        
        // 验证结果
        assert(!hasPermission) { "用户不应该拥有user:create权限" }
    }

    @Test
    fun `test hasPermission with no user roles`() {
        // 设置当前用户ID
        AuthThreadLocal.setCurrentUserId(1L)
        
        // 模拟用户没有角色
        `when`(userRoleDao.findByUserId(1L)).thenReturn(emptyList())
        
        // 测试权限检查
        val hasPermission = permissionCheckService.hasPermission("user:list")
        
        // 验证结果
        assert(!hasPermission) { "没有角色的用户不应该有任何权限" }
    }

    @Test
    fun `test hasPermission with inactive permission`() {
        // 设置当前用户ID
        AuthThreadLocal.setCurrentUserId(1L)
        
        // 模拟用户角色
        val userRoles = listOf(
            UserRole(userId = 1L, roleId = 1L)
        )
        `when`(userRoleDao.findByUserId(1L)).thenReturn(userRoles)
        
        // 模拟角色权限
        val rolePermissions = listOf(
            RolePermission(roleId = 1L, permissionId = 1L)
        )
        `when`(rolePermissionDao.findByRoleIds(listOf(1L))).thenReturn(rolePermissions)
        
        // 模拟已禁用的权限
        val permissions = listOf(
            Permission(id = 1L, code = "user:list", status = StatusEnum.INACTIVE.code)
        )
        `when`(permissionDao.findByIds(listOf(1L))).thenReturn(permissions)
        
        // 测试权限检查
        val hasPermission = permissionCheckService.hasPermission("user:list")
        
        // 验证结果
        assert(!hasPermission) { "已禁用的权限不应该生效" }
    }
}
