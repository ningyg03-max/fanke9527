# 工具脚本

## sjpush —— 一键提交并推送

```sh
sjpush "改动说明"      # 有说明
sjpush                # 不写 → 自动生成「更新 2026-10-20 23:30」
```

做的事：
1. 没改动就退出
2. 展示变更清单
3. 提交 + 推送
4. **回查 GitHub 文件树**，确认敏感文件（keystore / apk / smali）没上传

凭据从环境变量 `GITHUB_TOKEN` 取，用完即从 remote URL 移除 —— 不会留在 `.git/config`。

> 本仓库副本是给以后在别处重建环境参考用的；
> 实际调用的是 `/usr/local/bin/sjpush`。
