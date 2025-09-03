package io.infra.qk.repository

import io.infra.qk.entity.Permission
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.persistence.EntityManager

/**
 * 权限 Repository
 * @author liuqinglin
 * Date: 2025/8/30
 */
@ApplicationScoped
class PermissionRepository @Inject constructor(
    private val entityManager: EntityManager
) : PanacheRepository<Permission> {
    
    /**
     * 根据ID查找权限
     */
    override fun findById(id: Long): Permission? {
        return find("id", id).firstResult()
    }
    
    /**
     * 根据权限编码查找权限
     */
    fun findByPermissionCode(permissionCode: String): Permission? {
        return find("code", permissionCode).firstResult()
    }
    
    /**
     * 根据父权限ID查找子权限
     */
    fun findByParentId(parentId: Long): List<Permission> {
        return find("parentId", parentId).list()
    }
    
    /**
     * 根据条件分页查询权限
     */
    fun findByCondition(permissionName: String?, permissionCode: String?, permissionType: String?, parentId: Long?, status: String?, page: Int, size: Int): Pair<List<Permission>, Long> {
        var query = "FROM Permission WHERE 1=1"
        val params = mutableMapOf<String, Any>()
        
        if (!permissionName.isNullOrBlank()) {
            query += " AND name LIKE :permissionName"
            params["permissionName"] = "%$permissionName%"
        }
        
        if (!permissionCode.isNullOrBlank()) {
            query += " AND code LIKE :permissionCode"
            params["permissionCode"] = "%$permissionCode%"
        }
        
        if (!permissionType.isNullOrBlank()) {
            query += " AND type = :permissionType"
            params["permissionType"] = permissionType
        }
        
        if (parentId != null) {
            query += " AND parentId = :parentId"
            params["parentId"] = parentId
        }
        
        if (!status.isNullOrBlank()) {
            query += " AND status = :status"
            params["status"] = status
        }
        
        query += " ORDER BY sort ASC, createTime DESC"
        
        val countQuery = query.replace("FROM Permission", "SELECT COUNT(*) FROM Permission")
        val total = entityManager.createQuery(countQuery, Long::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
        }.singleResult
        
        val permissions = entityManager.createQuery(query, Permission::class.java).apply {
            params.forEach { (key, value) -> setParameter(key, value) }
            firstResult = (page - 1) * size
            maxResults = size
        }.resultList
        
        return Pair(permissions, total)
    }
    
    /**
     * 检查权限编码是否存在
     */
    fun existsByPermissionCode(permissionCode: String): Boolean {
        return count("code", permissionCode) > 0
    }
}
