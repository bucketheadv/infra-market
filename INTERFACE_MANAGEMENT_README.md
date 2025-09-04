# 接口管理功能说明

## 功能概述

接口管理功能允许用户创建、配置和执行HTTP接口请求。该功能支持动态参数配置，包括URL参数、Header参数和Body参数，并提供了丰富的输入类型和数据类型支持。

## 主要特性

### 1. 接口配置
- **接口名称**: 接口的显示名称
- **请求方法**: 支持GET、POST、PUT、DELETE、PATCH、HEAD、OPTIONS
- **接口URL**: 完整的请求地址
- **接口描述**: 接口的详细说明

### 2. 参数配置
每个参数支持以下配置项：

#### 基本配置
- **参数名**: 参数的名称
- **参数类型**: 
  - URL_PARAM: URL参数（如 ?id=123）
  - BODY_PARAM: 请求体参数
  - HEADER_PARAM: 请求头参数
- **输入类型**: 
  - TEXT: 文本框
  - SELECT: 下拉框
  - DATE: 日期选择器
  - DATETIME: 日期时间选择器
  - NUMBER: 数字输入框
  - TEXTAREA: 多行文本框
  - PASSWORD: 密码输入框
  - EMAIL: 邮箱输入框
  - URL: URL输入框
- **数据类型**: 
  - STRING: 字符串
  - INTEGER: 整数
  - LONG: 长整数
  - DOUBLE: 双精度浮点数
  - BOOLEAN: 布尔值
  - DATE: 日期
  - DATETIME: 日期时间
  - JSON: JSON对象

#### 高级配置
- **是否必填**: 参数是否为必填项
- **默认值**: 参数的默认值
- **是否可变更**: 在执行时是否可以修改参数值
- **可选值**: 当输入类型为SELECT时，配置下拉框的选项
- **参数描述**: 参数的详细说明
- **排序**: 参数在表单中的显示顺序

### 3. 接口执行
- 根据参数配置动态生成执行表单
- 支持实时参数验证
- 显示详细的响应信息（状态码、响应头、响应体、响应时间）
- 支持错误信息显示

## 使用流程

### 1. 创建接口
1. 进入"工具" -> "接口管理"
2. 点击"新增接口"按钮
3. 填写接口基本信息
4. 配置参数（可选）
5. 保存接口

### 2. 配置参数
1. 点击"添加参数"按钮
2. 选择参数类型（URL参数、Header参数、Body参数）
3. 配置输入类型和数据类型
4. 设置是否必填、默认值等
5. 如果是下拉框，配置可选值
6. 设置参数描述和排序

### 3. 执行接口
1. 在接口列表中点击"执行"按钮
2. 根据参数配置填写执行表单
3. 点击"执行接口"按钮
4. 查看执行结果

## 技术实现

### 后端实现
- **实体类**: `ApiInterface` - 存储接口基本信息
- **DTO类**: `ApiInterfaceDto`、`ApiParamDto` - 数据传输对象
- **枚举类**: `HttpMethodEnum`、`ParamTypeEnum`、`InputTypeEnum`、`DataTypeEnum`
- **服务层**: `ApiInterfaceService` - 业务逻辑处理
- **控制器**: `ApiInterfaceController` - API接口
- **数据存储**: 参数配置以JSON格式存储在数据库中

### 前端实现
- **接口列表**: `InterfaceList.vue` - 接口管理主页面
- **接口表单**: `InterfaceForm.vue` - 接口创建/编辑表单
- **接口执行**: `InterfaceExecute.vue` - 接口执行页面
- **API服务**: `interface.ts` - 前端API调用

### 数据库设计
```sql
CREATE TABLE `api_interface` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '接口名称',
    `method` VARCHAR(10) NOT NULL COMMENT '请求方法',
    `url` VARCHAR(500) NOT NULL COMMENT '接口URL',
    `description` VARCHAR(500) NULL COMMENT '接口描述',
    `params` TEXT NULL COMMENT '参数配置JSON',
    `status` INT NOT NULL DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
);
```

## 权限控制

接口管理功能需要以下权限：
- `interface:list` - 查看接口列表
- `interface:view` - 查看接口详情
- `interface:create` - 创建接口
- `interface:update` - 编辑接口
- `interface:delete` - 删除接口
- `interface:execute` - 执行接口

## 注意事项

1. **参数配置**: 参数配置以JSON格式存储，确保JSON格式正确
2. **URL参数**: GET请求不支持Body参数，会自动过滤
3. **参数验证**: 必填参数在执行时会进行验证
4. **错误处理**: 接口执行失败时会显示详细的错误信息
5. **响应格式**: 响应体会自动格式化JSON显示

## 扩展功能

未来可以考虑添加以下功能：
- 接口分组管理
- 接口版本控制
- 接口测试用例
- 接口性能监控
- 接口文档生成
- 接口导入导出
- 接口权限控制
- 接口调用统计
