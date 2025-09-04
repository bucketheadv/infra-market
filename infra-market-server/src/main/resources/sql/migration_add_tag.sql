-- 添加标签字段到api_interface表
ALTER TABLE api_interface ADD COLUMN tag VARCHAR(20) COMMENT '接口标签：TEST-测试，PRODUCTION-正式';

-- 创建索引以提高查询性能
CREATE INDEX idx_api_interface_tag ON api_interface(tag);
