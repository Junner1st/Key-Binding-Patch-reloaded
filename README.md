# Key Binding Patch (1.21.1 NeoForge)

> 支持组合键与多按键绑定的按键系统补丁

## 📖 简介

Key Binding Patch 是一个深度改造 Minecraft 按键绑定系统的客户端 Mod，支持：

- **多按键绑定同时触发**：绑定相同按键的多个按键绑定现在会一起被触发
- **任意组合键**：支持使用 Ctrl、Shift 和 Alt 以外的按键作为组合键（例如 `Z + D`）
- **复数组合键**：支持为按键绑定设置多个组合键（例如 `Alt + V + D`）
- **复制按键绑定**：为同一个功能设置多组触发按键（影子按键）
- **按下/抬起回调**：为按键绑定添加按下/抬起事件的回调 API（供其它开发者使用）
- **游戏内可视化配置**：在按键绑定界面直接通过 `+` / `-` 按钮添加或删除影子按键，无需编辑配置文件

## 🔧 环境要求

| 项目 | 版本 |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.0 |

## 🚀 安装

1. 安装 [NeoForge](https://neoforged.net/) 21.1.x（≥ 21.1.0）客户端
2. 将 `key_binding_patch-1.21.1-1.0.0.jar` 放入 `.minecraft/mods/` 文件夹
3. 启动游戏

## 🎮 使用说明

### 组合键设置
进入 **选项 → 控制 → 按键绑定**，选中目标功能后依次按下组合键即可录制。

### 影子按键（复制绑定）
在 **按键绑定界面**，每个功能条目左侧有操作按钮：
- **`+`**（普通功能旁）：为该功能创建一个影子按键（多一组触发按键）
- **`-`**（影子按键旁）：删除该影子按键

添加后立即生效，滚动位置保持不变，重启游戏后仍保留。

### 配置文件
影子按键配置保存在 `.minecraft/config/key_binding_patch-client.toml` 的 `shadow_key_bindings` 列表中，也可以手动编辑。

## 🧩 开发者 API

```java
// 创建支持组合键的按键绑定
KBPMod.newBuilder("key.example")
    .withKey(InputConstants.KEY_SPACE)
    .withCmbKeys(...)          // 附加组合键
    .withKeyModifier(KeyModifier.CONTROL)
    .buildAndRegis();          // 构建并注册

// 添加按下 / 抬起回调
IPatchedKeyBinding kb = KBPMod.getPatched(keyMapping);
kb.addPressCallback(() -> { /* 按下时 */ });
kb.addReleaseCallback(() -> { /* 抬起时 */ });
```

## 📝 版权与致谢

- 本 Mod 由 **Sparrow_prime** 发布。
- 本 Mod 的 **开发与移植** 由 **DeepSeek**（AI 代码助手）完成。
- 本 Mod 的 **功能创意** 源自 [Giant_Salted_Fish](https://github.com/Giant-Salted-Fish) 的原版 **Key-Binding-Patch** 项目，特此致谢（原作已停更）。
- 原项目：https://github.com/Giant-Salted-Fish/Key-Binding-Patch

## 📄 许可证

All Rights Reserved
