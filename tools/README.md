# 工具脚本

## `sjpush` —— 一键提交并推送代码

```sh
sjpush "改动说明"      # 有说明
sjpush                # 不写 → 自动生成「更新 2026-10-20 23:30」
```

做的事：
1. 没改动就退出
2. 展示变更清单
3. 提交 + 推送（**网络不稳自动重试 4 次**）
4. **回查 GitHub 文件树**，确认敏感文件（keystore / apk / smali）没上传

凭据从环境变量 `GITHUB_TOKEN` 取，用完即从 remote URL 移除 —— 不会留在 `.git/config`。

支持「工作区干净但有未推送提交」的情况（推失败过再重跑不会漏）。

> 注意：**不能用 `git push | tail`** —— 管道会吞掉退出码，失败也会打印成功。

---

## `sjrelease` —— 发布 APK 到 GitHub Releases

```sh
sjrelease <apk 路径> <版本号> <versionCode>
sjrelease /var/minis/attachments/SalaryJump-3.90-xxx.apk 3.90 114
```

做的事：
1. 自动从 `CHANGELOG.md` 最新一节取更新说明
2. 创建 tag `v<版本号>` + Release
3. 上传 APK 作为 asset（同名旧文件先删）
4. 打印下载地址

### 为什么不把 APK 放进 git 仓库

`.gitignore` 里排除了 `*.apk`，这是**故意的**：

| | 塞进 git | GitHub Releases |
|---|---|---|
| 仓库体积 | 每版永久+205KB，**删不掉**（需重写历史） | 不占 git，独立存储 |
| 下载入口 | 要点进目录翻 | 仓库首页右侧「Releases」直达 |
| 更新说明 | 无 | 有专门页面 |
| git clone 速度 | 越来越慢 | 不受影响 |

一个安装包 205KB 看着不大，但 **20 个版本就是 4MB 纯二进制**，
而且 `git clone` 会把这些历史全部拉下来。

---

## 环境前提

- `git`（沙箱默认没装，`apk add git`）
- 环境变量 `GITHUB_TOKEN`（classic token，勾 `repo` 权限）
- 网络：`github.com` 间歇性超时，`api.github.com` 稳定 —— 两个脚本都做了重试
