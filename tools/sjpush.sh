#!/bin/sh
# ─────────────────────────────────────────────────────────────
# sjpush —— 把 SalaryJump 的改动提交并推送到 GitHub
#
#   用法：  sjpush "改动说明"
#           sjpush            （不写说明 → 自动生成一句）
#
# 做的事：
#   ① 检查有没有改动，没有就退出
#   ② 展示变更清单（让你看清推什么）
#   ③ 提交 + 推送（网络不稳会自动重试）
#   ④ 回查 GitHub 实际文件树，确认敏感文件没上传
#
# 凭据：从环境变量 GITHUB_TOKEN 取，用完即从 remote URL 移除，
#       不会留在 .git/config 里。
# ─────────────────────────────────────────────────────────────
set -u

REPO="/var/minis/workspace/salaryjump"
REMOTE="https://github.com/ningyg03-max/fanke9527.git"
BRANCH="master"
MSG="$*"

cd "$REPO" || exit 1

CHANGES=$(git status --porcelain)
# 未推送的提交数（工作区干净但之前推失败过 → 这里不为 0）
UNPUSHED=$(git rev-list --count origin/master..HEAD 2>/dev/null || echo 0)

# ── ① 什么都没变、也没待推的提交 ──
if [ -z "$CHANGES" ] && [ "$UNPUSHED" -eq 0 ]; then
    echo "没有改动，也不需要推送。"
    exit 0
fi

# ── ② 展示变更 ──
if [ -n "$CHANGES" ]; then
    echo "═══ 待提交的改动 ═══"
    git status --short | head -30
    TOTAL=$(printf '%s\n' "$CHANGES" | wc -l)
    [ "$TOTAL" -gt 30 ] && echo "  …共 $TOTAL 个文件"
    echo
fi

# ── ③ 提交（有改动才提交）──
if [ -n "$CHANGES" ]; then
    [ -z "$MSG" ] && MSG="更新 $(date '+%Y-%m-%d %H:%M')"
    git add -A
    git commit -q -m "$MSG" || { echo "✗ 提交失败"; exit 1; }
    echo "✓ 已提交：$(git log --oneline -1)"
else
    echo "工作区无改动，但有 $UNPUSHED 个提交待推送："
    git log --oneline origin/master..HEAD | head -5
fi

# ── ④ 推送（token 临时写入，无论成败都移除）──
if [ -z "${GITHUB_TOKEN:-}" ]; then
    echo "✗ GITHUB_TOKEN 未设置，无法推送。提交已保存在本地。"
    exit 1
fi

git remote set-url origin "https://ningyg03-max:${GITHUB_TOKEN}@github.com/ningyg03-max/fanke9527.git"
cleanup() { cd "$REPO" && git remote set-url origin "$REMOTE"; }
trap cleanup EXIT INT TERM

echo "推送中…（网络不稳会自动重试）"
OK=0
N=1
while [ "$N" -le 4 ]; do
    # 注意：不能用管道套 git push，否则退出码会被 tail 吞掉
    if git push -q origin "$BRANCH" 2>/tmp/_sjpush_err.txt; then
        OK=1
        break
    fi
    echo "  第 $N 次失败：$(tail -1 /tmp/_sjpush_err.txt | cut -c1-80)"
    [ "$N" -lt 4 ] && sleep 5
    N=$((N + 1))
done

if [ "$OK" -ne 1 ]; then
    echo
    echo "✗ 推送失败（已重试 4 次）。"
    echo "  提交已保存在本地，网络恢复后重跑 sjpush 即可。"
    echo "  注：此时不会重复提交（工作区已干净，会直接退出）。"
    exit 1
fi
echo "✓ 已推送"

# ── ⑤ 回查远程（确认敏感文件没上去）──
echo
echo "═══ 远程核对 ═══"
GOT=0
for i in 1 2 3; do
    if curl -sS -m 30 -H "Authorization: Bearer $GITHUB_TOKEN" \
        "https://api.github.com/repos/ningyg03-max/fanke9527/git/trees/${BRANCH}?recursive=1" \
        -o /tmp/_sjpush_tree.json 2>/dev/null && [ -s /tmp/_sjpush_tree.json ]; then
        GOT=1
        break
    fi
    sleep 3
done

if [ "$GOT" -ne 1 ]; then
    echo "  （拿不到远程树，跳过核对 —— 但推送本身已成功）"
else
    python3 - <<'PY'
import json, sys
try:
    d = json.load(open('/tmp/_sjpush_tree.json'))
except Exception:
    print("  （解析失败，跳过核对）")
    sys.exit(0)
files = [x['path'] for x in d.get('tree', []) if x['type'] == 'blob']
print("  远程文件数：%d" % len(files))
bad = [p for p in files
       if 'keystore/README.txt' in p or p.endswith('.keystore')
       or p.endswith('.apk') or 'work/smali' in p or '/out/' in p]
if bad:
    print("  ✗✗ 敏感文件泄露：")
    for p in bad[:10]:
        print("     ", p)
    sys.exit(1)
print("  ✓ 敏感文件均未上传")
PY
fi

echo
echo "  仓库：https://github.com/ningyg03-max/fanke9527"
