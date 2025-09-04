package io.infra.market.repository.entity

import com.mybatisflex.annotation.Table
import io.infra.structure.db.model.activerecord.BaseActiveRecordEntity

/**
 * 权限实体类
 * 
 * 用于定义系统中的权限信息，支持树形结构的权限管理。
 * 权限可以包括菜单权限、按钮权限、接口权限等不同类型。
 * 通过parentId实现权限的层级关系，支持多级权限树。
 * 
 * @author liuqinglin
 * @date 2025/8/30
 * @since 1.0.0
 */
@Table("permission_info")
data class Permission(
    /**
     * 权限名称
     * 权限的显示名称，如"用户管理"、"添加用户"等
     */
    var name: String? = null,
    
    /**
     * 权限编码
     * 权限的唯一标识码，用于程序内部识别，如"USER_MANAGE"、"USER_ADD"等
     */
    var code: String? = null,
    
    /**
     * 权限类型
     * 可选值：menu(菜单)、button(按钮)、api(接口)、data(数据)
     * 默认为menu
     */
    var type: String = "menu",
    
    /**
     * 父权限ID
     * 用于构建权限树，顶级权限的parentId为null
     */
    var parentId: Long? = null,
    
    /**
     * 权限路径
     * 对于菜单类型权限，表示前端路由路径
     * 对于接口类型权限，表示API接口路径
     */
    var path: String? = null,
    
    /**
     * 权限图标
     * 用于前端菜单显示的图标，支持图标类名或图标路径
     */
    var icon: String? = null,
    
    /**
     * 排序号
     * 用于控制权限在菜单中的显示顺序，数值越小排序越靠前
     * 默认为0
     */
    var sort: Int = 0,
    
    /**
     * 权限状态
     * 可选值：active(激活)、inactive(未激活)、disabled(禁用)
     * 默认为active
     */
    var status: String = "active"
) : BaseActiveRecordEntity<Permission, Long>()
