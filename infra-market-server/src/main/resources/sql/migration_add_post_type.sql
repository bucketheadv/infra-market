-- 数据库迁移脚本：为api_interface表添加post_type字段
-- 创建时间: 2025-01-27
-- 说明: 为接口管理表添加POST类型字段，支持application/json和application/x-www-form-urlencoded格式

USE `infra_market`;

-- 添加post_type字段
ALTER TABLE `api_interface` 
ADD COLUMN `post_type` VARCHAR(50) NULL COMMENT 'POST类型：application/json、application/x-www-form-urlencoded' 
AFTER `description`;

-- 为现有数据设置默认值（如果需要的话）
-- UPDATE `api_interface` SET `post_type` = 'application/json' WHERE `method` IN ('POST', 'PUT', 'PATCH') AND `post_type` IS NULL;
