package io.infra.market.service

import io.infra.market.dto.LoginRequest
import io.infra.market.enums.StatusEnum
import io.infra.market.repository.dao.UserDao
import io.infra.market.repository.dao.UserRoleDao
import io.infra.market.repository.dao.RolePermissionDao
import io.infra.market.repository.dao.PermissionDao
import io.infra.market.repository.entity.User
import io.infra.market.util.AesUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.springframework.boot.test.context.SpringBootTest
import java.util.Date

@SpringBootTest
class AuthServiceTest {

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var userRoleDao: UserRoleDao

    @Mock
    private lateinit var rolePermissionDao: RolePermissionDao

    @Mock
    private lateinit var permissionDao: PermissionDao

    @Mock
    private lateinit var tokenService: TokenService

    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        authService = AuthService(userDao, userRoleDao, rolePermissionDao, permissionDao, tokenService)
    }

    @Test
    fun `test login updates last login time`() {
        // 准备测试数据
        val username = "testuser"
        val password = "password123"
        val encryptedPassword = AesUtil.encrypt(password)
        
        val user = User(
            id = 1L,
            username = username,
            password = encryptedPassword,
            email = "test@example.com",
            phone = "13800138000",
            status = StatusEnum.ACTIVE.code,
            lastLoginTime = null
        )

        val loginRequest = LoginRequest(username, password)

        // 模拟DAO方法
        `when`(userDao.findByUsername(username)).thenReturn(user)
        `when`(userRoleDao.findByUserId(1L)).thenReturn(emptyList())
        `when`(tokenService.saveToken(anyLong(), anyString())).thenReturn(Unit)

        // 执行登录
        val result = authService.login(loginRequest)

        // 验证结果
        assert(result.success)
        
        // 验证登录时间被更新
        verify(userDao).updateById(any<User>())
        
        // 验证更新后的用户对象包含新的登录时间
        val updateCaptor = argumentCaptor<User>()
        verify(userDao).updateById(updateCaptor.capture())
        val updatedUser = updateCaptor.firstValue
        
        assert(updatedUser.lastLoginTime != null)
        assert(updatedUser.lastLoginTime!!.time > 0)
    }
}
