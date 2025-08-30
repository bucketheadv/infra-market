-- 数据库迁移脚本：为用户表添加最后登录时间字段
-- 创建时间: 2025-01-27
-- 作者: liuqinglin

USE `infra_market`;

-- 为user_info表添加last_login_time字段
ALTER TABLE `user_info` 
ADD COLUMN `last_login_time` DATETIME NULL COMMENT '最后登录时间' AFTER `status`,
ADD INDEX `idx_last_login_time` (`last_login_time`);

-- 更新现有用户的last_login_time为create_time（作为初始值）
UPDATE `user_info` 
SET `last_login_time` = `create_time` 
WHERE `last_login_time` IS NULL AND `status` != 'deleted';
