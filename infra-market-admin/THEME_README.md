# 主题系统使用说明

## 概述

本项目已集成完整的主题切换系统，支持多种预设主题，并能够将用户的主题偏好保存到个人设置中。

## 功能特性

### 🎨 预设主题
- **默认主题** 🌊 - 蓝色系，现代商务风格
- **深色主题** 🌙 - 紫色系，护眼夜间模式
- **绿色主题** 🌿 - 绿色系，清新自然风格
- **橙色主题** 🍊 - 橙色系，温暖活力风格
- **红色主题** 🌹 - 红色系，热情奔放风格

### 💾 数据持久化
- 主题选择自动保存到本地存储和Cookie
- Cookie有效期1年，确保长期保存
- 页面刷新后自动恢复上次选择的主题

### 🔄 实时切换
- 无需刷新页面，主题即时生效
- 所有组件自动适配新主题色彩
- 平滑的过渡动画效果

## 使用方法

### 1. 切换主题
1. 点击页面右上角的主题按钮（设置图标）
2. 在弹出的主题选择面板中选择喜欢的主题
3. 主题将立即生效并自动保存

### 2. 重置主题
- 在主题选择面板底部点击"重置为默认"按钮
- 将恢复到默认的蓝色主题

## 技术实现

### 架构设计
```
ThemeSelector.vue (主题选择器组件)
    ↓
useThemeStore (主题状态管理)
    ↓
CSS变量 + 主题类名
    ↓
全局样式应用
```

### 核心文件
- `src/stores/theme.ts` - 主题状态管理
- `src/components/ThemeSelector.vue` - 主题选择器组件
- `src/styles/index.css` - 主题CSS变量定义

### CSS变量系统
主题通过CSS变量实现，主要变量包括：
```css
:root {
  --primary-color: #1890ff;      /* 主色调 */
  --secondary-color: #40a9ff;    /* 辅助色 */
  --background-color: #f0f2f5;   /* 背景色 */
  --text-color: #333333;         /* 文字色 */
  --border-color: #f0f0f0;       /* 边框色 */
  --shadow-color: rgba(24, 144, 255, 0.08); /* 阴影色 */
}
```

## 自定义主题

### 添加新主题
1. 在 `src/stores/theme.ts` 中的 `themes` 数组添加新主题配置
2. 在 `src/styles/index.css` 中添加对应的主题类样式
3. 确保新主题包含所有必要的颜色变量

### 主题配置结构
```typescript
interface ThemeConfig {
  name: string           // 主题标识
  label: string          // 显示名称
  primaryColor: string   // 主色调
  secondaryColor: string // 辅助色
  backgroundColor: string // 背景色
  textColor: string      // 文字色
  borderColor: string    // 边框色
  shadowColor: string    // 阴影色
  icon: string          // 主题图标
}
```

## 本地存储

### 存储方式
- **LocalStorage**: 用于页面刷新后的快速恢复
- **Cookie**: 用于跨会话的长期保存，有效期1年

### 存储内容
```javascript
// LocalStorage
localStorage.setItem('theme', 'dark')

// Cookie
document.cookie = 'theme=dark;path=/;max-age=31536000'
```

## 注意事项

1. **浏览器兼容性** - CSS变量需要现代浏览器支持
2. **性能优化** - 主题切换通过CSS变量实现，性能良好
3. **错误处理** - 网络异常时会回退到本地存储的主题
4. **响应式设计** - 主题选择器在不同屏幕尺寸下都有良好表现

## 故障排除

### 主题不生效
1. 检查浏览器是否支持CSS变量
2. 确认主题store是否正确初始化
3. 查看控制台是否有错误信息

### 主题切换失败
1. 检查浏览器是否支持Cookie
2. 确认浏览器存储权限
3. 查看浏览器控制台错误信息

## 更新日志

- **v1.0.0** - 初始版本，支持5种预设主题
- **v1.1.0** - 优化按钮样式和下拉菜单
- **v1.2.0** - 改用Cookie存储，移除后端依赖
