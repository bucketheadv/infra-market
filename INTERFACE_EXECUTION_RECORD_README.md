# 接口执行记录功能

## 功能概述

为接口执行功能增加了执行记录表，用于记录每次接口执行的详细信息，包括执行人、参数、返回值等数据。支持大文本字段存储，适用于参数和返回值可能长度很大的场景。

## 数据库设计

### 表结构：`api_interface_execution_record`

```sql
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
    `client_ip` VARCHAR(50) NULL COMMENT '客户端IP',
    `user_agent` VARCHAR(500) NULL COMMENT '用户代理',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
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
```

### 设计特点

1. **大文本支持**：使用 `LONGTEXT` 类型存储请求参数、请求头、请求体、响应头、响应体等可能很大的数据
2. **完整记录**：记录执行的所有关键信息，包括请求和响应的完整数据
3. **性能优化**：添加了必要的索引，支持按接口、执行人、时间等维度快速查询
4. **外键约束**：与接口表和用户表建立外键关系，保证数据一致性
5. **级联删除**：当接口或用户被删除时，相关的执行记录也会被自动删除

## 代码结构

### 1. 实体类
- **文件**：`ApiInterfaceExecutionRecord.kt`
- **功能**：定义执行记录的数据结构
- **特点**：继承 `BaseActiveRecordEntity`，支持 MyBatis-Flex 的 ActiveRecord 模式

### 2. DTO类
- **文件**：`ApiInterfaceExecutionRecordDto.kt`
- **包含**：
  - `ApiInterfaceExecutionRecordDto`：执行记录数据传输对象
  - `ApiInterfaceExecutionRecordQueryDto`：查询条件DTO
  - `ApiInterfaceExecutionRecordStatsDto`：统计信息DTO

### 3. 数据访问层
- **Mapper**：`ApiInterfaceExecutionRecordMapper.kt`
- **DAO**：`ApiInterfaceExecutionRecordDao.kt`
- **功能**：提供基础的CRUD操作和自定义查询方法

### 4. 业务逻辑层
- **Service**：`ApiInterfaceExecutionRecordService.kt`
- **功能**：提供执行记录的业务逻辑处理

### 5. 控制器层
- **Controller**：`ApiInterfaceExecutionRecordController.kt`
- **功能**：提供执行记录的RESTful API

## 核心功能

### 1. 自动记录执行记录
- 在 `ApiInterfaceService.execute()` 方法中自动记录每次接口执行
- 记录成功和失败两种情况
- 记录失败不影响主流程

### 2. 查询功能
- **分页查询**：支持多条件筛选的分页查询
- **按接口查询**：获取指定接口的执行记录
- **按执行人查询**：获取指定用户的执行记录
- **详情查询**：获取执行记录的详细信息

### 3. 统计功能
- **执行统计**：统计接口的执行次数、成功率、平均执行时间等
- **数量统计**：根据时间范围统计执行记录数量

### 4. 数据清理
- **历史数据清理**：删除指定时间之前的执行记录
- **权限控制**：只有具有删除权限的用户才能执行清理操作

## API接口

### 1. 分页查询执行记录
```
POST /api/interface/execution-record/list
```

### 2. 根据ID查询执行记录详情
```
GET /api/interface/execution-record/{id}
```

### 3. 根据接口ID查询执行记录
```
GET /api/interface/execution-record/interface/{interfaceId}?limit=10
```

### 4. 根据执行人ID查询执行记录
```
GET /api/interface/execution-record/executor/{executorId}?limit=10
```

### 5. 获取接口执行统计信息
```
GET /api/interface/execution-record/stats/{interfaceId}
```

### 6. 获取执行记录数量统计
```
GET /api/interface/execution-record/count?startTime=1234567890&endTime=1234567890
```

### 7. 删除历史执行记录
```
DELETE /api/interface/execution-record/cleanup?beforeTime=1234567890
```

## 权限控制

所有API接口都需要相应的权限验证：
- `interface:view`：查看执行记录
- `interface:delete`：删除执行记录

## 使用场景

1. **接口调试**：查看接口的历史执行记录，分析请求参数和响应结果
2. **性能监控**：统计接口的执行时间和成功率，监控接口性能
3. **问题排查**：当接口出现问题时，可以查看执行记录进行问题定位
4. **使用统计**：统计接口的使用情况，了解哪些接口被频繁调用
5. **数据审计**：记录谁在什么时候执行了哪些接口，满足审计要求

## 注意事项

1. **存储空间**：由于记录了完整的请求和响应数据，存储空间消耗较大，建议定期清理历史数据
2. **性能影响**：记录执行记录会有轻微的性能开销，但不会影响主流程
3. **数据安全**：执行记录可能包含敏感信息，需要做好数据安全保护
4. **外键约束**：删除接口或用户时，相关的执行记录会被级联删除

## 扩展建议

1. **数据压缩**：对于大文本字段，可以考虑压缩存储
2. **异步记录**：将执行记录改为异步记录，减少对主流程的影响
3. **数据分表**：当数据量很大时，可以考虑按时间分表
4. **缓存优化**：对频繁查询的统计信息进行缓存
5. **告警功能**：当接口执行失败率过高时，自动发送告警通知
