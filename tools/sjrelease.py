#!/usr/bin/env python3
"""给 SalaryJump 发一个 GitHub Release 并上传 APK。

用法: python3 release.py <apk路径> <版本号如3.89> <versionCode如113>
"""
import json
import os
import sys
import urllib.request
import urllib.parse
import urllib.error

REPO = "ningyg03-max/fanke9527"
TOKEN = os.environ.get("GITHUB_TOKEN", "")

if len(sys.argv) < 4:
    print("用法: release.py <apk> <versionName> <versionCode>")
    sys.exit(1)

apk_path, ver_name, ver_code = sys.argv[1], sys.argv[2], sys.argv[3]
tag = "v%s" % ver_name


def api(method, path, body=None, raw=None, content_type="application/json"):
    url = "https://api.github.com" + path
    data = None
    if body is not None:
        data = json.dumps(body).encode("utf-8")
    elif raw is not None:
        data = raw
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Authorization", "Bearer " + TOKEN)
    req.add_header("Accept", "application/vnd.github+json")
    if data is not None:
        req.add_header("Content-Type", content_type)
    try:
        with urllib.request.urlopen(req, timeout=120) as r:
            return r.status, json.loads(r.read().decode("utf-8") or "{}")
    except urllib.error.HTTPError as e:
        return e.code, {"error": e.read().decode("utf-8", "replace")[:400]}
    except Exception as e:
        return 0, {"error": str(e)}


# ── ① 读 CHANGELOG 最新一节作为说明 ──
notes = ""
try:
    chl = open("/var/minis/workspace/salaryjump/CHANGELOG.md", encoding="utf-8").read()
    idx = chl.find("\n## v")
    if idx >= 0:
        end = chl.find("\n## v", idx + 3)
        notes = chl[idx + 1:end if end > 0 else idx + 3000].strip()
except Exception:
    pass
if not notes:
    notes = "SalaryJump %s (versionCode %s)" % (ver_name, ver_code)

# ── ② 创建 / 复用 release ──
status, rel = api("GET", "/repos/%s/releases/tags/%s" % (REPO, tag))
if status == 200:
    print("Release %s 已存在，复用 (id=%s)" % (tag, rel.get("id")))
else:
    body = {
        "tag_name": tag,
        "name": "SalaryJump %s" % ver_name,
        "body": notes,
        "draft": False,
        "prerelease": False,
    }
    status, rel = api("POST", "/repos/%s/releases" % REPO, body)
    if status not in (200, 201):
        print("✗ 创建 release 失败:", status, rel)
        sys.exit(1)
    print("✓ Release 已创建: %s" % rel.get("html_url"))

release_id = rel.get("id")
upload_url = rel.get("upload_url", "")
if not upload_url:
    status, rel = api("GET", "/repos/%s/releases/%s" % (REPO, release_id))
    upload_url = rel.get("upload_url", "")

base_upload = upload_url.split("{")[0]

# ── ③ 上传 asset（已存在就先删）──
asset_name = os.path.basename(apk_path)
status, assets = api("GET", "/repos/%s/releases/%s/assets" % (REPO, release_id))
if status == 200 and isinstance(assets, list):
    for a in assets:
        if a.get("name") == asset_name:
            api("DELETE", "/repos/%s/releases/assets/%s" % (REPO, a["id"]))
            print("  已删除同名旧 asset")

data = open(apk_path, "rb").read()
url = "%s?name=%s" % (base_upload, urllib.parse.quote(asset_name))
req = urllib.request.Request(url, data=data, method="POST")
req.add_header("Authorization", "Bearer " + TOKEN)
req.add_header("Content-Type", "application/vnd.android.package-archive")
req.add_header("Accept", "application/vnd.github+json")
try:
    with urllib.request.urlopen(req, timeout=300) as r:
        res = json.loads(r.read().decode("utf-8"))
    print("✓ APK 已上传: %s (%d bytes)" % (res.get("name"), res.get("size", 0)))
    print("  下载地址: %s" % res.get("browser_download_url"))
except urllib.error.HTTPError as e:
    print("✗ 上传失败:", e.code, e.read().decode("utf-8", "replace")[:300])
    sys.exit(1)
except Exception as e:
    print("✗ 上传失败:", e)
    sys.exit(1)

print()
print("Release 页面: %s" % rel.get("html_url", ""))
