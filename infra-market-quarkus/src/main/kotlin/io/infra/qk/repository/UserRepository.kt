package io.infra.qk.repository

import io.infra.qk.entity.User
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager

/**
 * 用户 Repository
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class UserRepository @Inject constructor(
    private val entityManager: EntityManager
) : PanacheRepository<User> {
    
    /**
     * 根据用户名查找用户
     */
    fun findByUsername(username: String): User? {
        return find("username", username).firstResult()
    }
    
    /**
     * 根据ID查找用户
     */
    fun findByUid(id: Long): User? {
        return find("id", id).firstResult()
    }
    
    /**
     * 根据条件分页查询用户
     */
    fun findByCondition(username: String?, status: String?, page: Int, size: Int): Pair<List<User>, Long> {
        var query = "FROM User WHERE 1=1"
        val params = mutableMapOf<String, Any>()
        
        if (!username.isNullOrBlank()) {
            query += " AND username LIKE :username"
            params["username"] = "%$username%"
        }
        
        if (!status.isNullOrBlank()) {
            query += " AND status = :status"
            params["status"] = status
        }
        
        query += " ORDER BY createTime DESC"
        
        val countQuery = query.replace("FROM User", "SELECT COUNT(*) FROM User")
        val total = entityManager.createQuery(countQuery, Long::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
        }.singleResult
        
        val users = entityManager.createQuery(query, User::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
            firstResult = (page - 1) * size
            maxResults = size
        }.resultList
        
        return Pair(users, total)
    }
    
    /**
     * 检查用户名是否存在
     */
    fun existsByUsername(username: String): Boolean {
        return count("username", username) > 0
    }
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: String): Boolean {
        return count("email", email) > 0
    }
}
