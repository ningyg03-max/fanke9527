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
#   ③ 提交 + 推送
#   ④ 回查 GitHub 实际文件树，确认敏感文件没上传
#
# 凭据：从环境变量 GITHUB_TOKEN 取，用完即从 remote URL 移除，
#       不会留在 .git/config 里。
# ─────────────────────────────────────────────────────────────
set -e

REPO="/var/minis/workspace/salaryjump"
REMOTE="https://github.com/ningyg03-max/fanke9527.git"
BRANCH="master"
MSG="$*"

cd "$REPO"

# ── ① 有改动吗 ──
if [ -z "$(git status --porcelain)" ]; then
    echo "没有改动，不需要推送。"
    exit 0
fi

# ── ② 展示变更 ──
echo "═══ 待提交的改动 ═══"
git status --short | head -30
TOTAL=$(git status --porcelain | wc -l)
[ "$TOTAL" -gt 30 ] && echo "  …共 $TOTAL 个文件"
echo

# ── ③ 提交 ──
[ -z "$MSG" ] && MSG="更新 $(date '+%Y-%m-%d %H:%M')"
git add -A
git commit -q -m "$MSG"
echo "✓ 已提交：$(git log --oneline -1)"

# ── ④ 推送（token 临时写入，用完移除）──
if [ -z "$GITHUB_TOKEN" ]; then
    echo "✗ GITHUB_TOKEN 未设置，无法推送。提交已保存在本地。"
    exit 1
fi

git remote set-url origin "https://ningyg03-max:${GITHUB_TOKEN}@github.com/ningyg03-max/fanke9527.git"
cleanup() {
    cd "$REPO" && git remote set-url origin "$REMOTE"
}
trap cleanup EXIT INT TERM

echo "推送中…"
git push -q origin "$BRANCH" 2>&1 | tail -3
echo "✓ 已推送"

# ── ⑤ 回查远程（确认敏感文件没上去）──
echo
echo "═══ 远程核对 ═══"
curl -sS -H "Authorization: Bearer $GITHUB_TOKEN" \
    "https://api.github.com/repos/ningyg03-max/fanke9527/git/trees/${BRANCH}?recursive=1" \
    > /tmp/_sjpush_tree.json 2>/dev/null || true

python3 - <<'PY'
import json, sys
try:
    d = json.load(open('/tmp/_sjpush_tree.json'))
except Exception:
    print("  （拿不到远程树，跳过核对）")
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

echo
echo "  仓库：https://github.com/ningyg03-max/fanke9527"
