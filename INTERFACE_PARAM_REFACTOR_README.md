# 接口管理参数配置重构说明

## 重构概述

本次重构将接口管理的参数配置从单一参数列表拆分为三个独立的参数类型：URL参数、Header参数、Body参数，并增加了POST类型选择功能，支持JSON和表单格式的请求体构造。

## 主要变更

### 1. 后端变更

#### 1.1 新增枚举类
- **PostTypeEnum.kt**: 新增POST类型枚举，支持JSON和FORM_URLENCODED两种格式

#### 1.2 DTO更新
- **ApiInterfaceDto**: 增加`postType`字段，将`params`拆分为`urlParams`、`headerParams`、`bodyParams`
- **ApiInterfaceFormDto**: 同样增加`postType`字段和分离的参数配置
- **ApiExecuteRequestDto**: 增加`postType`、`urlParams`、`bodyParams`字段

#### 1.3 实体类更新
- **ApiInterface**: 增加`postType`字段

#### 1.4 服务层更新
- **ApiInterfaceService**: 
  - 更新`convertToDto`方法，支持参数分离
  - 更新`convertToEntity`方法，支持参数合并
  - 更新`execute`方法，支持不同POST类型的body构造

#### 1.5 数据库变更
- 为`api_interface`表添加`post_type`字段
- 提供数据库迁移脚本

### 2. 前端变更

#### 2.1 类型定义更新
- **interface.ts**: 更新所有接口类型定义，支持新的参数结构

#### 2.2 组件重构
- **InterfaceForm.vue**: 完全重构参数配置部分
  - 拆分为三个独立的参数配置区域
  - 增加POST类型选择
  - 创建ParamForm子组件
- **ParamForm.vue**: 新增参数表单组件，复用参数配置逻辑
- **InterfaceExecute.vue**: 更新执行逻辑，支持新的参数结构

#### 2.3 新增功能
- POST类型选择：JSON格式、表单格式
- 参数类型分离：URL参数、Header参数、Body参数
- 不同POST类型的body构造逻辑

## 功能特性

### 1. 参数类型分离
- **URL参数**: 用于GET请求的查询参数
- **Header参数**: 用于HTTP请求头
- **Body参数**: 用于POST/PUT/PATCH请求的请求体

### 2. POST类型支持
- **JSON格式**: 默认格式，请求体为JSON字符串
- **表单格式**: 请求体为application/x-www-form-urlencoded格式

### 3. 智能显示
- GET请求不显示Body参数配置
- POST/PUT/PATCH请求显示POST类型选择
- 根据请求方法动态显示相关配置项

## 数据库迁移

### 新增字段
```sql
ALTER TABLE `api_interface` 
ADD COLUMN `post_type` VARCHAR(20) NULL COMMENT 'POST类型：JSON、FORM_URLENCODED' 
AFTER `description`;
```

### 迁移脚本
执行 `migration_add_post_type.sql` 脚本为现有数据库添加新字段。

## 使用说明

### 1. 创建接口
1. 填写接口基本信息（名称、方法、URL、描述）
2. 选择请求方法
3. 如果是POST/PUT/PATCH请求，选择POST类型
4. 分别配置URL参数、Header参数、Body参数

### 2. 参数配置
- 每个参数可以设置：参数名、输入类型、数据类型、是否必填、是否可变更、默认值、描述等
- 支持下拉框类型的参数，可以配置可选值
- 参数按类型分组显示，便于管理

### 3. 接口执行
- 系统会根据配置的参数类型和POST类型自动构造请求
- URL参数会拼接到URL中
- Header参数会添加到请求头
- Body参数会根据POST类型构造请求体

## 兼容性说明

- 现有接口数据会自动迁移到新的参数结构
- 旧的参数配置会按类型自动分类
- 向后兼容，不影响现有功能

## 技术实现

### 1. 参数存储
- 后端仍使用JSON格式存储所有参数
- 前端按类型分离显示和编辑
- 数据转换在服务层完成

### 2. 请求构造
- URL参数：拼接到URL查询字符串
- Header参数：添加到HTTP请求头
- Body参数：根据POST类型构造请求体

### 3. 类型安全
- 使用TypeScript确保类型安全
- 枚举类确保数据一致性
- 参数验证确保数据完整性

## 注意事项

1. 执行数据库迁移前请备份数据
2. 新功能需要重新部署前后端代码
3. 建议在测试环境先验证功能正常
4. 现有接口数据会自动迁移，无需手动处理

## 后续优化

1. 支持更多POST类型（如multipart/form-data）
2. 增加参数模板功能
3. 支持参数分组和排序
4. 增加参数验证规则配置
