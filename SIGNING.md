# GitHub Actions 签名配置指南

## 🔐 配置 GitHub Secrets

在 GitHub 仓库的 **Settings → Secrets and variables → Actions** 中添加以下 Secrets：

### 必需 Secrets（Release 签名）

| Secret 名称 | 说明 | 示例值 |
|------------|------|--------|
| `KEYSTORE_BASE64` | Keystore 文件的 Base64 编码 | `base64 keystore.jks` |
| `KEYSTORE_PASSWORD` | Keystore 密码 | `android123` |
| `KEY_ALIAS` | 密钥别名 | `gymlog` |
| `KEY_PASSWORD` | 密钥密码 | `android123` |

### 生成 Keystore 和 Base64 编码

```bash
# 1. 生成新的 Keystore（如果还没有）
keytool -genkey -v -keystore gymlog-keystore.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias gymlog \
  -storepass android123 \
  -keypass android123 \
  -dname "CN=GymLog, OU=Development, O=GymLog, L=Shanghai, ST=Shanghai, C=CN"

# 2. 将 Keystore 转为 Base64
base64 -i gymlog-keystore.jks | pbcopy  # macOS
# 或
base64 gymlog-keystore.jks > keystore.base64  # 保存到文件

# 3. 复制 Base64 字符串到 GitHub Secrets 的 KEYSTORE_BASE64
```

### 仅使用 Debug 签名（不配置 Secrets）

如果不配置任何 Secrets，GitHub Actions 会自动构建 **Debug APK**，适合测试使用。

---

## 📦 APK 下载

构建完成后，在 GitHub Actions 页面：

1. 进入 **Actions** 标签页
2. 点击最新的构建运行
3. 在页面底部的 **Artifacts** 部分下载 `app-release.zip`
4. 解压后获取 APK 文件

---

## 🔧 本地构建命令

```bash
# Debug 版本
./gradlew assembleDebug

# Release 版本（需要配置签名）
./gradlew assembleRelease

# 清理并重新构建
./gradlew clean assembleRelease
```

---

## ⚠️ 安全提示

- **永远不要** 将 Keystore 文件提交到 Git
- `.gitignore` 已配置忽略 `*.jks` 和 `*.keystore` 文件
- 定期备份 Keystore 文件到安全位置
- Release 签名丢失将无法更新已发布的应用
