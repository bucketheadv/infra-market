package io.infra.qk.repository

import io.infra.qk.entity.Role
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext

/**
 * 角色 Repository
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class RoleRepository @Inject constructor(
    private val entityManager: EntityManager
) : PanacheRepository<Role> {
    
    /**
     * 根据ID查找角色
     */
    override fun findById(id: Long): Role? {
        return find("id", id).firstResult()
    }
    
    /**
     * 根据角色编码查找角色
     */
    fun findByRoleCode(roleCode: String): Role? {
        return find("code", roleCode).firstResult()
    }
    
    /**
     * 根据条件分页查询角色
     */
    fun findByCondition(roleName: String?, roleCode: String?, status: String?, page: Int, size: Int): Pair<List<Role>, Long> {
        var query = "FROM Role WHERE 1=1"
        val params = mutableMapOf<String, Any>()
        
        if (!roleName.isNullOrBlank()) {
            query += " AND name LIKE :roleName"
            params["roleName"] = "%$roleName%"
        }
        
        if (!roleCode.isNullOrBlank()) {
            query += " AND code LIKE :roleCode"
            params["roleCode"] = "%$roleCode%"
        }
        
        if (!status.isNullOrBlank()) {
            query += " AND status = :status"
            params["status"] = status
        }
        
        query += " ORDER BY createTime DESC"
        
        val countQuery = query.replace("FROM Role", "SELECT COUNT(*) FROM Role")
        val total = entityManager.createQuery(countQuery, Long::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
        }.singleResult
        
        val roles = entityManager.createQuery(query, Role::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
            firstResult = (page - 1) * size
            maxResults = size
        }.resultList
        
        return Pair(roles, total)
    }
    
    /**
     * 检查角色编码是否存在
     */
    fun existsByRoleCode(roleCode: String): Boolean {
        return count("roleCode", roleCode) > 0
    }
    
    /**
     * 查找所有激活状态的角色
     */
    fun findAllActive(): List<Role> {
        return find("status", "active").list()
    }
}
