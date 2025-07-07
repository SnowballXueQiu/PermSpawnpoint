# PermSpawnpoint

一个基于权限组的出生点插件，适用于 Paper/Spigot/Bukkit 服务器。

## 功能特性

- 🎯 **权限组出生点**: 根据玩家的权限组设置不同的出生点
- 🌍 **多世界支持**: 支持在不同世界设置出生点
- 👀 **视角控制**: 可配置玩家出生时的视角方向
- 🔒 **首次限制**: 仅在玩家首次进服时生效
- 🌐 **国际化**: 支持中文(zh_CN)和英文(en_US)
- ⚡ **高性能**: 使用 Kotlin 编写，性能优异
- 🔧 **易配置**: 简单的 YAML 配置文件

## 依赖要求

- **Vault**: 用于权限组管理
- **Java 17+**: 运行环境要求
- **Paper/Spigot/Bukkit 1.20+**: 服务端支持

## 安装方法

1. 确保服务器已安装 Vault 插件
2. 将编译好的 `PermSpawnpoint.jar` 放入 `plugins` 文件夹
3. 重启服务器
4. 编辑 `plugins/PermSpawnpoint/config.yml` 配置文件
5. 使用 `/permspawn reload` 重载配置

## 配置说明

### 基本配置 (config.yml)

```yaml
# 语言设置 (en_US, zh_CN)
language: zh_CN

# 启用调试日志
debug: false

# 出生点配置
spawn-points:
  # VIP 玩家出生点
  vip-spawn:
    world: "world"          # 世界名称
    x: 100.5               # X 坐标
    y: 64.0                # Y 坐标
    z: 200.5               # Z 坐标
    yaw: 90.0              # 水平视角 (0=南, 90=西, 180=北, 270=东)
    pitch: 0.0             # 垂直视角 (-90=向上, 0=水平, 90=向下)
    permissions:           # 权限要求
      - "group:vip"         # Vault 权限组
      - "permspawnpoint.vip" # 直接权限
    priority: 10           # 优先级 (数字越大优先级越高)
```

### 权限配置

- `group:组名` - Vault 权限组
- `权限节点` - 直接权限检查
- `[]` - 无权限要求 (所有人可用)

## 命令使用

### 主命令: `/permspawn` (别名: `/ps`, `/pspawn`)

| 命令 | 描述 | 权限 |
|------|------|------|
| `/permspawn reload` | 重载插件配置 | `permspawnpoint.admin` |
| `/permspawn setspawn <名称> <权限> <优先级>` | 设置出生点 | `permspawnpoint.admin` |
| `/permspawn list` | 列出所有出生点 | `permspawnpoint.admin` |
| `/permspawn reset <玩家>` | 重置玩家首次加入状态 | `permspawnpoint.admin` |
| `/permspawn info <玩家>` | 查看玩家出生点信息 | `permspawnpoint.admin` |

### 设置出生点示例

```bash
# 设置 VIP 出生点，需要 vip 权限组，优先级 10
/permspawn setspawn vip-spawn group:vip 10

# 设置默认出生点，无权限要求，优先级 1
/permspawn setspawn default none 1

# 设置管理员出生点，需要多个权限，优先级 20
/permspawn setspawn admin group:admin,permspawnpoint.admin 20
```

## 权限节点

| 权限 | 描述 | 默认 |
|------|------|------|
| `permspawnpoint.admin` | 管理员权限 | OP |
| `permspawnpoint.bypass` | 绕过出生点限制 | false |

## 工作原理

1. **玩家加入**: 检测玩家是否首次进服
2. **权限检查**: 查找玩家有权限访问的出生点
3. **优先级排序**: 选择优先级最高的出生点
4. **传送**: 将玩家传送到指定位置和视角
5. **标记**: 标记玩家已完成首次传送

## 国际化支持

插件支持多语言，语言文件位于 `plugins/PermSpawnpoint/lang/` 目录：

- `en_US.yml` - 英文
- `zh_CN.yml` - 中文

可以通过修改 `config.yml` 中的 `language` 设置来切换语言。

## 开发信息

- **作者**: Snowball_233
- **命名空间**: cc.vastsea
- **语言**: Kotlin
- **构建工具**: Maven
- **许可证**: MIT

## 故障排除

### 常见问题

1. **插件无法启动**
   - 检查是否安装了 Vault 插件
   - 确认 Java 版本为 17 或更高

2. **玩家没有被传送**
   - 检查玩家是否有相应权限
   - 确认出生点配置正确
   - 查看控制台日志

3. **世界未找到**
   - 确认世界名称拼写正确
   - 检查世界是否已加载

### 调试模式

在 `config.yml` 中设置 `debug: true` 可以启用详细日志输出。

## 更新日志

### v1.0.0
- 初始版本发布
- 基本出生点功能
- 权限组支持
- 国际化支持
- 视角控制

## 支持

如果遇到问题或有建议，请在 GitHub 上提交 Issue。