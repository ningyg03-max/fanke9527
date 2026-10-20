SalaryJump 签名证书说明
========================

⚠️ 真正的 keystore 文件与口令**不入库**（见 .gitignore）。
   需要的同学请向工程维护者索取，或自己生成一把新的（但老用户的覆盖安装会失败）。

## 为什么必须用同一把证书

Android 要求「覆盖安装」的 APK 签名与已安装版本一致。
换证书 → 用户必须先卸载（数据全丢）。

## 生成（仅首次）

```sh
keytool -genkeypair -v \
  -keystore salaryjump-release.keystore \
  -alias salaryjump -keyalg RSA -keysize 2048 -validity 10950 \
  -storepass "$SIGN_PASS" -keypass "$SIGN_PASS" \
  -dname "CN=SalaryJump, OU=Mobile, O=SalaryJump, L=Beijing, ST=Beijing, C=CN"
```

口令建议放在环境变量 `SIGN_PASS` 里，不要写进脚本或文档。

## 签名

```sh
apksigner sign --ks salaryjump-release.keystore \
  --ks-pass env:SIGN_PASS --key-pass env:SIGN_PASS \
  --ks-key-alias salaryjump \
  --v1-signing-enabled true --v2-signing-enabled true --v3-signing-enabled true \
  --out 输出.apk 输入.apk
```

本工程的工具链里另有封装脚本（`/opt/tools/pack.sh`），同样是读环境变量。

## 备份

**一定要单独备份 keystore 文件本身 + 口令**（不要和仓库放一起）。
丢了 = 以后所有更新都得让用户卸载重装。
