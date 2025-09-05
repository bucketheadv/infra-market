-- 基础设施市场数据库初始化脚本
-- 创建时间: 2025-08-30
-- 作者: liuqinglin

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `infra_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `infra_market`;

-- 用户信息表
CREATE TABLE IF NOT EXISTS `user_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `email` VARCHAR(100) NULL COMMENT '邮箱',
    `phone` VARCHAR(20) NULL COMMENT '手机号',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用，deleted-已删除',
    `last_login_time` DATETIME NULL COMMENT '最后登录时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_status` (`status`),
    KEY `idx_last_login_time` (`last_login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- 角色信息表
CREATE TABLE IF NOT EXISTS `role_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) NULL COMMENT '角色描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用，deleted-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_name` (`name`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色信息表';

-- 权限信息表
CREATE TABLE IF NOT EXISTS `permission_info` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '权限名称',
    `code` VARCHAR(50) NOT NULL COMMENT '权限编码',
    `type` VARCHAR(20) NOT NULL DEFAULT 'menu' COMMENT '权限类型：menu-菜单，button-按钮',
    `parent_id` BIGINT NULL COMMENT '父权限ID',
    `path` VARCHAR(255) NULL COMMENT '路径',
    `icon` VARCHAR(100) NULL COMMENT '图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态：active-启用，inactive-禁用，deleted-已删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限信息表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS `user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS `role_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 插入初始数据

-- 插入管理员用户
INSERT INTO `user_info` (`username`, `password`, `email`, `phone`, `status`) VALUES
('admin', 'rNBNQUWYDUjYu7j8MP0hZg==', 'admin@infra-market.com', '13800138000', 'active'),
('user1', 'rNBNQUWYDUjYu7j8MP0hZg==', 'user1@infra-market.com', '13800138001', 'active'),
('user2', 'rNBNQUWYDUjYu7j8MP0hZg==', 'user2@infra-market.com', '13800138002', 'active');

-- 插入角色
INSERT INTO `role_info` (`name`, `code`, `description`, `status`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 'active'),
('管理员', 'ADMIN', '系统管理员，拥有大部分权限', 'active'),
('普通用户', 'USER', '普通用户，拥有基本权限', 'active'),
('访客', 'GUEST', '访客用户，只有查看权限', 'active');

-- 插入权限
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`) VALUES
-- 系统管理
('系统管理', 'system:manage', 'menu', NULL, '/system', 'SettingOutlined', 1, 'active'),
('用户管理', 'user:manage', 'menu', 1, '/system/users', 'UserOutlined', 1, 'active'),
('用户列表', 'user:list', 'button', 2, NULL, NULL, 1, 'active'),
('用户创建', 'user:create', 'button', 2, NULL, NULL, 2, 'active'),
('用户编辑', 'user:update', 'button', 2, NULL, NULL, 3, 'active'),
('用户删除', 'user:delete', 'button', 2, NULL, NULL, 4, 'active'),
('用户状态', 'user:status', 'button', 2, NULL, NULL, 5, 'active'),
('角色管理', 'role:manage', 'menu', 1, '/system/roles', 'TeamOutlined', 2, 'active'),
('角色列表', 'role:list', 'button', 8, NULL, NULL, 1, 'active'),
('角色创建', 'role:create', 'button', 8, NULL, NULL, 2, 'active'),
('角色编辑', 'role:update', 'button', 8, NULL, NULL, 3, 'active'),
('角色删除', 'role:delete', 'button', 8, NULL, NULL, 4, 'active'),
('角色状态', 'role:status', 'button', 8, NULL, NULL, 5, 'active'),
('权限管理', 'permission:manage', 'menu', 1, '/system/permissions', 'SafetyCertificateOutlined', 3, 'active'),
('权限列表', 'permission:list', 'button', 14, NULL, NULL, 1, 'active'),
('权限创建', 'permission:create', 'button', 14, NULL, NULL, 2, 'active'),
('权限编辑', 'permission:update', 'button', 14, NULL, NULL, 3, 'active'),
('权限删除', 'permission:delete', 'button', 14, NULL, NULL, 4, 'active'),
('权限状态', 'permission:status', 'button', 14, NULL, NULL, 5, 'active');

-- 插入用户角色关联
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin -> 超级管理员
(2, 2), -- user1 -> 管理员
(3, 3); -- user2 -> 普通用户

-- 插入角色权限关联
-- 超级管理员拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 1, id FROM `permission_info` WHERE status = 'active';

-- 管理员拥有系统管理权限（除了超级管理员专用权限）
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 2, id FROM `permission_info` WHERE status = 'active' AND code LIKE 'system:%';

-- 普通用户拥有基本查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 3, id FROM `permission_info` WHERE status = 'active' AND code IN (
    'system:manage', 'user:list', 'role:list', 'permission:list'
);

-- 访客只有系统管理查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 4, id FROM `permission_info` WHERE status = 'active' AND code IN (
    'system:manage', 'user:list', 'role:list', 'permission:list'
);

-- 接口管理表
CREATE TABLE IF NOT EXISTS `api_interface` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '接口名称',
    `method` VARCHAR(10) NOT NULL COMMENT '请求方法：GET、POST、PUT、DELETE等',
    `url` VARCHAR(500) NOT NULL COMMENT '接口URL',
    `description` VARCHAR(500) NULL COMMENT '接口描述',
    `post_type` VARCHAR(50) NULL COMMENT 'POST类型：application/json、application/x-www-form-urlencoded',
    `params` TEXT NULL COMMENT '参数配置JSON',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `environment` VARCHAR(50) NULL COMMENT '接口环境，用于标识接口所属的环境，如测试环境、正式环境',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_method` (`method`),
    KEY `idx_status` (`status`),
    KEY `idx_environment` (`environment`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口管理表';

-- 插入工具相关权限
-- 先插入工具菜单
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`) VALUES
('工具', 'tool:manage', 'menu', NULL, '/tools', 'ToolOutlined', 2, 'active');

-- 插入接口管理菜单（作为工具的子菜单）
SET @tool_manage_id = (SELECT id FROM `permission_info` WHERE code = 'tool:manage');
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`) VALUES
('接口管理', 'interface:manage', 'menu', @tool_manage_id, '/tools/interface', 'ApiOutlined', 1, 'active');

-- 插入接口管理相关的按钮权限
SET @interface_manage_id = (SELECT id FROM `permission_info` WHERE code = 'interface:manage');
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`) VALUES
('接口列表', 'interface:list', 'button', @interface_manage_id, NULL, NULL, 1, 'active'),
('接口查看', 'interface:view', 'button', @interface_manage_id, NULL, NULL, 2, 'active'),
('接口创建', 'interface:create', 'button', @interface_manage_id, NULL, NULL, 3, 'active'),
('接口编辑', 'interface:update', 'button', @interface_manage_id, NULL, NULL, 4, 'active'),
('接口删除', 'interface:delete', 'button', @interface_manage_id, NULL, NULL, 5, 'active'),
('接口执行', 'interface:execute', 'button', @interface_manage_id, NULL, NULL, 6, 'active');

-- 更新角色权限关联，为超级管理员和管理员添加工具权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 1, id FROM `permission_info` WHERE status = 'active' AND (code LIKE 'tool:%' OR code LIKE 'interface:%');

INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 2, id FROM `permission_info` WHERE status = 'active' AND (code LIKE 'tool:%' OR code LIKE 'interface:%');

-- 为普通用户添加接口查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 3, id FROM `permission_info` WHERE status = 'active' AND code IN (
    'tool:manage', 'interface:manage', 'interface:list', 'interface:view'
);

-- 为访客添加接口查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`) 
SELECT 4, id FROM `permission_info` WHERE status = 'active' AND code IN (
    'tool:manage', 'interface:manage', 'interface:list', 'interface:view'
);

