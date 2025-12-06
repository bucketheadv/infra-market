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
    `last_login_time` BIGINT NULL COMMENT '最后登录时间（毫秒时间戳）',
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
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
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
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
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
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
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
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
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_role_id` (`role_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 插入初始数据

-- 插入管理员用户
INSERT INTO `user_info` (`username`, `password`, `email`, `phone`, `status`, `create_time`, `update_time`) VALUES
('admin', 'rNBNQUWYDUjYu7j8MP0hZg==', 'admin@infra-market.com', '13800138000', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user1', 'rNBNQUWYDUjYu7j8MP0hZg==', 'user1@infra-market.com', '13800138001', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('user2', 'rNBNQUWYDUjYu7j8MP0hZg==', 'user2@infra-market.com', '13800138002', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入角色
INSERT INTO `role_info` (`name`, `code`, `description`, `status`, `create_time`, `update_time`) VALUES
('超级管理员', 'SUPER_ADMIN', '系统超级管理员，拥有所有权限', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('管理员', 'ADMIN', '系统管理员，拥有大部分权限', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('普通用户', 'USER', '普通用户，拥有基本权限', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('访客', 'GUEST', '访客用户，只有查看权限', 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入权限
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`, `create_time`, `update_time`) VALUES
-- 系统管理
('系统管理', 'system:manage', 'menu', NULL, '/system', 'SettingOutlined', 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户管理', 'user:manage', 'menu', 1, '/system/users', 'UserOutlined', 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户列表', 'user:list', 'button', 2, NULL, NULL, 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户创建', 'user:create', 'button', 2, NULL, NULL, 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户编辑', 'user:update', 'button', 2, NULL, NULL, 3, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户删除', 'user:delete', 'button', 2, NULL, NULL, 4, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('用户状态', 'user:status', 'button', 2, NULL, NULL, 5, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色管理', 'role:manage', 'menu', 1, '/system/roles', 'TeamOutlined', 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色列表', 'role:list', 'button', 8, NULL, NULL, 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色创建', 'role:create', 'button', 8, NULL, NULL, 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色编辑', 'role:update', 'button', 8, NULL, NULL, 3, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色删除', 'role:delete', 'button', 8, NULL, NULL, 4, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('角色状态', 'role:status', 'button', 8, NULL, NULL, 5, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限管理', 'permission:manage', 'menu', 1, '/system/permissions', 'SafetyCertificateOutlined', 3, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限列表', 'permission:list', 'button', 14, NULL, NULL, 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限创建', 'permission:create', 'button', 14, NULL, NULL, 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限编辑', 'permission:update', 'button', 14, NULL, NULL, 3, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限删除', 'permission:delete', 'button', 14, NULL, NULL, 4, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('权限状态', 'permission:status', 'button', 14, NULL, NULL, 5, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入用户角色关联
INSERT INTO `user_role` (`user_id`, `role_id`, `create_time`, `update_time`) VALUES
(1, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000), -- admin -> 超级管理员
(2, 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000), -- user1 -> 管理员
(3, 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000); -- user2 -> 普通用户

-- 插入角色权限关联
-- 超级管理员拥有所有权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 1, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active';

-- 管理员拥有系统管理权限（除了超级管理员专用权限）
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 2, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND code LIKE 'system:%';

-- 普通用户拥有基本查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 3, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND code IN (
    'system:manage', 'user:list', 'role:list', 'permission:list'
);

-- 访客只有系统管理查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 4, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND code IN (
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
    `timeout` BIGINT NULL COMMENT '超时时间（秒），接口执行时的超时时间，默认60（60秒）',
    `value_path` VARCHAR(500) NULL COMMENT '取值路径，用于从响应结果中提取特定值的JSONPath表达式',
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_name` (`name`),
    KEY `idx_method` (`method`),
    KEY `idx_status` (`status`),
    KEY `idx_environment` (`environment`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口管理表';

-- 插入工具相关权限
-- 先插入工具菜单
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`, `create_time`, `update_time`) VALUES
('工具', 'tool:manage', 'menu', NULL, '/tools', 'ToolOutlined', 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入接口管理菜单（作为工具的子菜单）
SET @tool_manage_id = (SELECT id FROM `permission_info` WHERE code = 'tool:manage');
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`, `create_time`, `update_time`) VALUES
('接口管理', 'interface:manage', 'menu', @tool_manage_id, '/tools/interface', 'ApiOutlined', 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入图片处理菜单（作为工具的子菜单）
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`, `create_time`, `update_time`) VALUES
('图片处理', 'image:manage', 'menu', @tool_manage_id, '/tools/image', 'PictureOutlined', 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入接口管理相关的按钮权限
SET @interface_manage_id = (SELECT id FROM `permission_info` WHERE code = 'interface:manage');
INSERT INTO `permission_info` (`name`, `code`, `type`, `parent_id`, `path`, `icon`, `sort`, `status`, `create_time`, `update_time`) VALUES
('接口列表', 'interface:list', 'button', @interface_manage_id, NULL, NULL, 1, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('接口查看', 'interface:view', 'button', @interface_manage_id, NULL, NULL, 2, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('接口创建', 'interface:create', 'button', @interface_manage_id, NULL, NULL, 3, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('接口编辑', 'interface:update', 'button', @interface_manage_id, NULL, NULL, 4, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('接口删除', 'interface:delete', 'button', @interface_manage_id, NULL, NULL, 5, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('接口执行', 'interface:execute', 'button', @interface_manage_id, NULL, NULL, 6, 'active', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 更新角色权限关联，为超级管理员和管理员添加工具权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 1, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND (code LIKE 'tool:%' OR code LIKE 'interface:%' OR code LIKE 'image:%');

INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 2, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND (code LIKE 'tool:%' OR code LIKE 'interface:%' OR code LIKE 'image:%');

-- 为普通用户添加接口查看权限和图片处理权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 3, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND code IN (
    'tool:manage', 'interface:manage', 'interface:list', 'interface:view', 'image:manage'
);

-- 为访客添加接口查看权限
INSERT INTO `role_permission` (`role_id`, `permission_id`, `create_time`, `update_time`) 
SELECT 4, id, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000 FROM `permission_info` WHERE status = 'active' AND code IN (
    'tool:manage', 'interface:manage', 'interface:list', 'interface:view'
);

-- 接口执行记录表
CREATE TABLE IF NOT EXISTS `api_interface_execution_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `interface_id` BIGINT NOT NULL COMMENT '接口ID',
    `executor_id` BIGINT NOT NULL COMMENT '执行人ID',
    `executor_name` VARCHAR(50) NOT NULL COMMENT '执行人姓名',
    `request_params` LONGTEXT NULL COMMENT '请求参数JSON',
    `request_headers` LONGTEXT NULL COMMENT '请求头JSON',
    `request_body` LONGTEXT NULL COMMENT '请求体JSON',
    `response_status` INT NULL COMMENT '响应状态码',
    `response_headers` LONGTEXT NULL COMMENT '响应头JSON',
    `response_body` LONGTEXT NULL COMMENT '响应体JSON',
    `execution_time` BIGINT NULL COMMENT '执行时间（毫秒）',
    `success` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否成功：1-成功，0-失败',
    `error_message` TEXT NULL COMMENT '错误信息',
    `remark` TEXT NULL COMMENT '备注',
    `client_ip` VARCHAR(50) NULL COMMENT '客户端IP',
    `user_agent` VARCHAR(500) NULL COMMENT '用户代理',
    `create_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '创建时间（毫秒时间戳）',
    `update_time` BIGINT NOT NULL DEFAULT (FLOOR(UNIX_TIMESTAMP(NOW(3)) * 1000)) COMMENT '更新时间（毫秒时间戳）',
    PRIMARY KEY (`id`),
    KEY `idx_interface_id` (`interface_id`),
    KEY `idx_executor_id` (`executor_id`),
    KEY `idx_executor_name` (`executor_name`),
    KEY `idx_success` (`success`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_execution_time` (`execution_time`),
    CONSTRAINT `fk_execution_record_interface` FOREIGN KEY (`interface_id`) REFERENCES `api_interface` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_execution_record_executor` FOREIGN KEY (`executor_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='接口执行记录表';

