# SalaryJump · 开发者交接文档

> **新会话请先读这份文档** —— 它包含：全部功能索引、代码地图、构建流程、已知坑。
> 读完即可定位任何功能并开始改动，无需翻阅记忆日志。
> 详细功能说明见 [README.md](README.md)，版本历史见 [CHANGELOG.md](CHANGELOG.md)。

---

> ⚠️ **2026-10-12 工程曾被清空并重建** —— 先读 [RECOVERY-NOTE.md](RECOVERY-NOTE.md)，
> 了解源码来源（从 3.61 APK 反编译 + smali 核对）与测试套件的重建情况。

---

## ⚡ 5 分钟上手（新会话先看这段）

### 这是什么

`实时工资计数器` Android 应用。**纯 Java 手写构建**（无 Gradle），
源码在 `work-java/`，用 apktool 打资源包 + d8 编 dex + apksigner 签名。

当前 **3.82**（versionCode 106）· 63 个类 / 19986 行 · 测试 **1399 项全过**

### 开工前必做三件事

```sh
cd /var/minis/workspace/salaryjump

sh tests/run.sh                    # ① 先确认基线是绿的（1285 项）
rg -n "你要改的东西" work-java/     # ② 定位代码，别猜
```
③ 读 §4「标准改动流程」—— 那是踩出来的，不是规范模板

### 改完必做（顺序不能省）

```sh
sh /opt/tools/build.sh work-java /tmp/vXX-dex                                    # 编译（看类数对不对）
sh tests/run.sh                                                                   # 测试（必须全过）
java -Xmx420m -jar /opt/tools/apktool-patched.jar b -f -o /tmp/vXX-base.apk work  # 打包
sh /opt/tools/pack.sh /tmp/vXX-base.apk /tmp/vXX-dex/classes.dex /tmp/vXX-out.apk # 换 dex + 签名
java -jar /opt/tools/apksigner.jar verify -v /tmp/vXX-out.apk | rg "v2|v3"        # 签名
python3 /opt/tools/zipcheck.py /tmp/vXX-out.apk | rg "resources.arsc:"            # STORE + 4 对齐
cp /tmp/vXX-out.apk /var/minis/attachments/SalaryJump-<版本>.apk                   # 交付
```

### 五条铁律（每条都是血换的）

| # | 铁律 | 违反的后果 |
|---|---|---|
| 1 | **同一个业务概念只能有一个计算入口** | 存钱罐在两个页面显示不同数字（§5.11） |
| 2 | **规则抽成纯函数再写测试** | 内联在 UI 里的校验出错，测试全绿也发现不了（§5.5） |
| 3 | **补丁脚本：全部预检通过才落盘** | 中途失败留下半成品中间态（§4） |
| 4 | **Dialog 里发起结果跳转，先确认宿主 Activity** | 回调去错地方，功能静默失效（§5.12） |
| 5 | **改完文档**（CHANGELOG + README 版本表 + 本文件 §6） | 下个会话拿过期信息做判断 |

### 最容易踩的坑（按频率）

1. **改 `MainActivity` 的锚点要带缩进** —— 它和内部类里有同名方法（`matchWrap()` 等）
2. **新增 `SalarySnapshot.state` 取值要检查所有消费方** —— 主卡/小组件/日历/月报
3. **反编译产物有 jadx 压平陷阱** —— 改完跑 §5.5 的三个检测脚本
4. **写归档行时别覆盖别的功能口径** —— 加班曾覆盖出勤 3 倍
5. **渲染循环里别重复查存储** —— 用 `DataCache`，日历卡顿就是这么来的

### 遇到问题时

| 情况 | 去哪看 |
|---|---|
| 不知道功能在哪实现 | §2 功能地图 |
| 不知道某个存储键是什么 | §3 数据存储速查 |
| 想加功能，不知道从哪下手 | §4 标准改动流程 |
| 改了但没生效 / 崩溃 | §5 已知坑（按类型分）|
| 想知道当前做到哪了 | §6 当前状态 |
| 想知道某个改动为什么这么设计 | CHANGELOG.md 对应版本 |

---

## 0. 一句话

实时工资计数器 + 喝水提醒的 Android 应用（反编译改造，纯 Java 手写构建）。
包名 `com.salaryjump.app` · minSdk 26 / targetSdk 34 · 当前 **3.82**（versionCode 106）

---

## 1. 环境与构建（沙箱内）

```
项目根：/var/minis/workspace/salaryjump/

源码：work-java/com/salaryjump/app/   ← 改这里（51 文件 / 17038 行）
资源：work/res/                        ← 布局/颜色/Manifest
测试：tests/                           ← 回归测试（1036 项）
交付：/var/minis/attachments/SalaryJump-<版本>.apk
```

**标准构建三步**（缺一不可）：
```sh
sh /opt/tools/build.sh work-java /tmp/vXX-dex          # 1. Java → dex
java -Xmx420m -jar /opt/tools/apktool-patched.jar b -f -o /tmp/vXX-base.apk work   # 2. 资源打包
sh /opt/tools/pack.sh /tmp/vXX-base.apk /tmp/vXX-dex/classes.dex /tmp/vXX-out.apk  # 3. 换 dex + 对齐 + 签名
```

**发版前检查清单**：
```sh
sh tests/run.sh                                              # 测试全过
java -jar /opt/tools/apksigner.jar verify -v out.apk | rg "v2|v3"   # 签名
python3 /opt/tools/zipcheck.py out.apk | rg "resources.arsc:"       # STORE+4对齐
# 版本号：work/apktool.yml 的 versionCode +1 / versionName
```

---

## 2. 功能地图（全部功能 → 实现位置）

### 主界面（MainActivity.java · 4800 行，含 3 个内部类）

| 功能 | 关键方法/字段 |
|---|---|
| 实时工资计数器 | `SalaryCalculator.calculate()`（唯一计价入口）+ `refreshDisplay()` |
| 记录胶囊行（加班/请假/出勤/外快） | `updateCaps()` + `styleCap()`（三态样式）|
| 打工小人 | `workerPetView` + `WorkerPetView.setState()`；点击 `poke()` |
| 累计收入 | `updateCumulativeCard()` + `cumulativeTotal()` |
| 目标存钱罐 | `updateGoalView()` + `GoalStore`（**多目标**）+ `confirmGoalDone()` |
| 主卡排版 | `HeroFit`（保证金额不被小人挤掉）+ `fitAmountText()` |
| 崩溃弹窗 | `showPendingCrash()`（启动时弹完整堆栈）|
| 设置对话框引擎 | 内部类 `SettingsDialog`（section 模式复用）|

### 页面（各 Activity）

| 页面 | 文件 | 功能 |
|---|---|---|
| 工资日历 | `SalaryCalendarActivity` | **日历网格**（日薪/加班/外快/补班）· 点选详情 · 长按标记 · 补记 |
| 月度战报 | `MonthlyReportActivity` | 月合计 · 统计 · 存钱罐进度 · 「年度」入口 |
| 年度总览 | `YearlyReportActivity` | 全年收入 + 12 月柱状图 + 统计 + 假期使用 |
| 假期账户 | `LeaveAccountsActivity` | 6 种假期余额维护（年度类 + 调休）|
| 加班与出勤规则 | `WorkRulesActivity` | 周末模式/倍率/宽限 · 调休账户 |
| 喝水统计 | `WaterStatsActivity` | 今日/近7天/近30天 |
| 喝水记录 | `WaterRecordsActivity` | 列表 + **改时间** + 删除 |
| 设置入口 | `SettingsActivity` | 三分类卡片 → 全屏子页 |
| 加班与请假说明 | `GuideActivity` | 功能说明（文案须与实现同步）|

### 核心逻辑类

| 类 | 职责 |
|---|---|
| `SalaryHistory` | 工资日历归档（**保留 1500 天**）· `adjust` 增量 |
| `ArchiveSync` | **归档同步中枢**：补记/重算/对账 · `expectedCents` 口径 |
| `MonthBackfill` | **月度补录** · `statDays`（日均口径）· `goalProgress`（存钱罐）|
| `ExtraIncome` | 外快 · `apply`（已归档日同步）|
| `LeaveRecord` / `LeaveAccounts` | 请假记录 / 假期账户数据层 |
| `HolidayWork` / `WorkRules` / `WorkdayHelper` | 出勤记录 / 规则 / 工作日判定 |
| `OvertimeLog` / `OvertimeCalc` | 加班打卡 / 加班费计算 |
| `DayBreakdown` | **每日金额拆分**（日薪/加班/外快/补录）—— 日历格子用 |
| `YearlyStats` | 年度聚合（加班次数与总额，从金额反推）|
| `DataCache` | **读取缓存**（避免重复解析历史字符串）|
| `GoalStore` | **存钱罐多目标**：列表读写 / 迁移 / 冻结 / 进度区间 |
| `SectionSave` | 设置分区 → 保存范围的映射 |
| `CalendarGrid` | **共用日历网格**（工资日历与战报同一套渲染）|

### 提醒系统

| 提醒 | 调度类 | 接收器 |
|---|---|---|
| 喝水 | `WaterReminderScheduler`（含 `isDrinkingDay` 感知出勤 · 自定义时段）| `WaterReminderReceiver` |
| 打卡 | `CheckInReminder` | `CheckInReceiver` |
| 发薪日 | `PaydayReminder` | `PaydayReceiver` |

### 小组件

| 类 | 职责 |
|---|---|
| `SalaryWidgetProvider` | RemoteViews 渲染 · 小人相位（`WorkerPet.phaseFrom`）|
| `WidgetUpdateService` | 前台服务驱动刷新（工作 15s / 空闲 60s）|
| `WidgetText` | 状态文案（纯逻辑可测）|

### 打工小人

| 类 | 职责 |
|---|---|
| `WorkerPet` | **16 姿势**绘制（12 日常 + 3 反应 + 撒花 + 捂耳朵）+ 动画 + 疲劳叠加 |
| `WorkerPetView` | 主卡 View（动画驱动）· `renderBitmap`（小组件）· `poke/celebrate` |
| `PetState` | 状态推导：疲劳 / 发薪日 / 工位判定 |
| `PetQuotes` | **语录池**（56 条：自动 40 + 点击 16），可单独测试 |
| `PetSettings` | 显示 / 尺寸档位 / 工位场景 |
| `HeroFit` | 主卡排版计算（保证金额不被小人挤掉） |

### 视觉系统

| 类 | 职责 |
|---|---|
| `UiKit` | 极光包装 `aurora()` · 玻璃卡 `glassCard()` · 渐变/胶囊 |
| `MotionKit` | 呼吸点/辉光/数字滚动/入场/按压 |
| `AuroraBackdrop` | 极光氛围层 |
| `SheenBar` | 扫光进度条 |

### 安全与工具

`PrivacyLock`（PIN+指纹）· `BackupTool`（手动导出/恢复）· `NetDiag` · `CrashLogger` ·
`Contexts`（宿主解析）· `StatutoryHolidays`/`HolidayUpdater`（节假日）· `SeedBackfill`（一次性补录）

**自动备份**：`BackupScheduler`（目录授权 + 闹钟 + 通知 + 执行）·
`BackupPlan`（文件名/周期/保留策略，纯逻辑）· `BackupReceiver`（闹钟触发 + 开机重排）

---

## 3. 数据存储速查（SharedPreferences `salary_jump`）

| 键 | 含义 |
|---|---|
| `daily_salary_bits` / `work_start` / `work_end` / `break_start` / `break_end` / `work_mode` | 工资与工作时间 |
| `salary_history` | 工资日历：`yyyyMMdd:金额分;...`（保留 1500 天）|
| `cum_total_bits` / `cum_days` / `cum_last_day` | 累计总额 / 已结算天数 / 归档点（**不变量：cum_total = Σ 已归档行**）|
| `month_backfill` | 月度补录：`yyyyMM,金额分,备注;...` |
| `goal_list` | 存钱罐**多目标**：`名称\|目标分\|开始日\|达标日\|确认日\|完成金额分`（`;` 分隔）|
| `goal_name` 等 4 个旧键 | 迁移后**保留不删**，作回退保险 |
| `pet_visible` / `pet_size` / `pet_scene` | 小人：显示 / 尺寸档位 / 工位场景 |
| `backup_auto_enabled` / `backup_tree_uri` / `backup_tree_label` | 自动备份：开关 / 目录 URI / 目录名 |
| `backup_period` / `backup_keep` / `backup_hour` / `backup_minute` | 周期 / 保留份数 / 时间 |
| `backup_last_at` / `backup_last_state` | 上次备份时间 / 结果 |
| `goal_celebrated` / `payday_celebrated_day` | 庆祝去重标记 |
| `leave_records` | 请假：`yyyyMMdd,比例,半天,名称;...` |
| `la_*_quota` / `la_*_adjust` / `la_comp_initial` | 假期账户（年度类 + 调休期初）|
| `holiday_work` | 出勤：`yyyyMMdd,倍率,调休;...` |
| `rule_weekend_mode` / `rule_weekend_x10` / `rule_statutory_x10` | 加班规则 |
| `ot_day` / `ot_start` / `ot_end` | 加班打卡（只存最近一次）|
| `extra_income` | 外快：`yyyyMMdd,金额分,备注;...` |
| `water_records` / `water_history` / `water_consumed` | 喝水记录 / 归档 / 总量 |
| `water_window_mode` / `water_win_start` / `water_win_end` / `water_skip_break` / `water_weekend` | 喝水时段（自定义）|
| `drink_ml_<id>` / `drink_name_<id>` / `drink_unit_<id>` / `drink_kcal_<id>` / `drink_sugar_<id>` | 饮品配置 |
| `checkin_enabled` / `checkin_hour` / `checkin_minute` | 打卡提醒 |

---

## 4. 标准改动流程（每次照做）

```
1. 侦察     rg 定位目标代码；读上下文（含所有调用点）
2. 补丁     Python 脚本：全部锚点预检 → 一次性落盘（原子性！）
3. 编译     javac 全量编译（阿里）→ 类数应匹配
4. 测试     sh tests/run.sh → 全过（新增功能要加测试组）
5. 构建     三步构建 → apksigner verify → zipcheck
6. 核对     jadx 反编译抽查关键改动（防 jadx 优化造成的误判）
7. 文档     CHANGELOG + README（版本号/版本表/尾注/测试数）+ memory
8. 交付     复制到 /var/minis/attachments/SalaryJump-<版本>.apk
```

**补丁脚本铁律**（血泪教训）：
- 锚点必须**精确到上下文**（多处同名方法要用缩进/注释区分）
- **全部预检通过才落盘**；或落盘后立即 `rg -c` 校验
- 脚本重跑前**先检查现状**（防重复应用）

---

## 5. 已知坑（按踩坑频率排序）

### 5.1 补丁脚本类
1. **部分落盘**：脚本中途 assert 失败但前面已写入 → 中间态。用「全部预检 → 一次性写」模式。
2. **重复应用**：修正后重跑应先检查现状。
3. **整方法替换**：用「找到起点 → 括号匹配找终点」而非字符串结束标记。
4. **多处同名锚点**：`matchWrap()` 等在 MainActivity 和内部类各有一份 → 锚点带缩进。

### 5.2 Android 绘制类
5. **Paint alpha 陷阱**：`setColor(0)` + `setShader` → 画不出来（alpha 调制）。
6. **配色按位置分组**：画在身体上的（五官）可用深色；画在背景上的（四肢/装饰）深色模式必须提亮。
7. **LinearLayout 不自动换行**：N 列网格必须手动分行（每 N 个开新行容器）。
8. **两行文字按钮 ≥ 60dp**：46dp 会裁掉第二行。项目标准是 `timeButtonParams`（60dp）。
9. **浏览器全屏截图全黑/空白**：大视口或后台标签页 RAF 节流 → 重置视口 / 加看门狗。

### 5.3 数据一致性类
10. **状态机消费方**：新增 `SalarySnapshot.state` 取值时，检查**所有消费方**（主卡/小组件/日历/月报）。
11. **归档守卫**：新功能写归档行时，不能让其他功能的口径覆盖（如加班的"日薪+加班费"曾覆盖出勤 3 倍）。
12. **保留期**：新增数据入口时检查所有出口的保留期（补录历史月份 vs 400 天裁剪曾冲突 → 已改 1500 天）。
13. **派生数据对账**：累计 = Σ 已归档行（不变量）；出现漂移用对账修复。

### 5.4 工具链类
14. **jadx 数字优化**：`calendar.get(7)` 可能显示为 `calendar.get(HOLIDAY_WORK)` 等常量名 → 别误判。
15. **R.java 手工维护**：新增资源 ID 必须同步改三处（ids.xml + public.xml + R.java）。
16. **R.java/android.jar**：`-bootclasspath /opt/tools/android.jar`，测试桩在 `tests/stubs/`。

---

### 5.5 ⚠️ 反编译恢复的代码：警惕「不可测的边界逻辑」

**案例**：重建工程时 jadx 把设置页的输入范围校验反着翻译 ——
把「越界才报错」变成「无条件报错」，导致点保存必弹「容量请设置在 10 到 2000 ml 之间」。
原始 APK 并没有这个问题，是恢复过程引入的回归。

**为什么没被发现**：这段校验内联在 `SettingsDialog` 里，依赖 Android UI，
回归测试覆盖不到 —— 测试全绿，bug 照样在。

**jadx 压平的三类模式**（都要查）：
1. **提前返回的守卫被压平**：`if (越界) { 报错; return; }` → 变成
   `if (合法) { 成功路径 } 报错; return;`（成功路径末尾的 `goto` 丢了）
2. **if/else 被压平**：`if (a) { X = A; } else { X = B; }` → `if (a) { X = A; } X = B;`
3. **循环条件被折进循环体**：`for (i…) { i = (…) ? i+1 : 0; 报错; return; }`

**对策**：
1. 边界规则一律抽成**纯函数**（`DrinkTypes.mlInRange/kcalInRange/targetInRange`、
   `WaterReminderSettings.modeFor`）
2. **发现一处压平 bug 后，必须全工程搜同类模式** —— 只修眼前一处会反复挨打
   （v77 只修了内层循环，v78 又发现外层守卫 + if/else）
3. 改完跑两个检测器：
   - `python3 /tmp/audit_guards.py` —— 找「未被 if/catch 守卫的 showError」
   - `python3 /tmp/audit_flatten.py` —— 找「if 块内赋值 + 紧跟无条件覆盖」
   - `python3 /tmp/audit_loops.py` —— 找「循环体内给循环变量赋值」
4. 更彻底：比对原始 APK 与重建 APK 的**方法级 smali 指令序列**（`/tmp/smali_diff.py`）；
   用 `/tmp/cfg.py` 把某个方法渲染成「基本块 + 跳转边」人工核对
5. 备用反编译器：CFR（`/tmp/cfr.jar`，需先 dex2jar 转 class）可与 jadx 交叉验证

### 5.6 ⚠️ 「按需创建控件」的界面，保存必须按同一维度分发

**案例**：设置界面各分区的控件是按需创建的 ——「薪跳」才建 `salaryInput`，
「工具与安全」不建。但 `save()` 只区分了 1 / 2 / 其余，
**section 3（工具与安全）落进「全部保存」**，去读没创建的 `salaryInput` → 空指针。

原版就有这个 bug，只是「工具与安全」里原来没有可保存的字段，没人点那个保存按钮。
**加了「打工小人」分区之后才被踩出来。**

**对策**：分区 → 保存范围 的对应关系抽成纯函数 `SectionSave.targetFor(section)`，
三个 save 方法各自加判空防御，并用 AS 组测试锁住规则。

### 5.7 ⚠️ 渲染循环里别重复查存储

日历要显示 31 天的拆分，最初每天各查 2 次存储（`has` + `centsOf`），
而每次查询都要把整个历史字符串重新解析一遍 → 手机上 23–39ms，超过一帧（16ms）。

**对策**：`DataCache` 统一缓存（读一次解析一份，写入时 `invalidate()`）。
日历提速至 3–5ms。**关键不是算得快，而是少算。**

同类风险点：任何「在循环 / 每秒刷新里调用存储读取」的地方。

### 5.8 ⚠️ 历史加班靠金额反推（打卡只存最后一次）

`OvertimeLog` 只有 `ot_day` / `ot_start` / `ot_end` 三个 key，**换一天打卡会覆盖上一条**，
历史加班记录留不下来。

所以年度统计里的加班次数/总额走 `YearlyStats.overtimeSummary()`，
从每日金额反推：`加班费 = 入账 − expectedCents`，且**只在工作日/出勤日**才算
（休息日的 expectedCents 为 0，不加守卫会误判）。

### 5.9 ⚠️ 存钱罐多目标：冻结范围必须限定到完成日

`GoalStore` 支持一列目标。两个关键点：

1. **冻结范围**：完成目标时只冻结 `[开始日, 确认日]` 这段的收入。
   若用「开始日 → 现在」会把它和下一个目标的区间重叠、总收入被算两遍。
   新目标起算日一律取**确认日次日**。
2. **写回要改列表里那一份**：`save(context, load(context))` 是错的 ——
   `load()` 会重新从存储读，刚改的对象不在列表里，改动直接丢失。
   正确做法：改 `load()` 出来的 list，再 `save`。

存储：`goal_list = 名称|目标分|开始日|达标日|确认日|完成金额分`（`;` 分隔多条）。
旧 key（`goal_name` 等）**保留不删**，迁移幂等。

### 5.10 ⚠️ 自动备份与 Android 路径限制

**「让用户输路径」在 Android 10+ 做不到** —— 分区存储禁止 App 写任意路径。
唯一干净的做法是 **SAF 目录授权**：`ACTION_OPEN_DOCUMENT_TREE` 选一次文件夹，
`takePersistableUriPermission` 永久记住，之后自动写。

- `BackupPlan` —— 纯逻辑（文件名/周期/保留策略），有完整测试
- `BackupScheduler` —— 目录授权 + 闹钟 + 通知 + 执行
- `BackupReceiver` —— 闹钟触发 + 开机/改时间后重排

**定时不可靠是系统限制**（国产 ROM 杀后台），所以加了
`catchUpIfDue()`：每次打开 App 检查「距上次备份超过周期了吗」，超了立刻补跑。

**保留策略只删本 App 的备份文件**（按 `SalaryJump-yyyyMMdd.json` 识别），
别人的文件绝不碰 —— 这条有专门的测试。

### 5.11 ⚠️ 同一个业务概念只能有一个计算入口

踩过三次：

| 场景 | 后果 |
|---|---|
| 存钱罐进度 | 主界面用 `goalProgress(起算日)`，战报用 `totalCents()`（全部历史）→ 数字对不上 |
| 备份 JSON | 手动导出与自动备份各写一份构造 → 格式会走样 |
| 备份目录接管 | MainActivity 与 SettingsActivity 各写一套 → 其中一个永远不触发 |

**规则**：抽成一个函数 / 一个类，两边都调它。已经这么做的：
`CalendarGrid`（日历网格）、`GoalStore`（存钱罐）、`BackupPlan`（备份）、
`SectionSave`（分区保存）。

### 5.12 ⚠️ Dialog 里 startActivityForResult 要看清宿主

`SettingsDialog` 是 `MainActivity` 的内部类，但**实例是在 `SettingsActivity` 里创建的**。
所以在 Dialog 里发起 `startActivityForResult` 时，回调会回到 **SettingsActivity**，
而不是 MainActivity —— 这类错误没有编译期报错，很容易踩。

**规则**：Dialog 里要发起结果跳转时，先确认「这个 Dialog 是谁 new 出来的」，
然后把 `onActivityResult` 的处理写在**那个 Activity** 里。
拿 Activity 用 `Contexts.anyActivityOf(activityContext)`。

---

## 6. 当前状态（每次改动后更新）

- **版本**：3.90（versionCode 114）
- **测试**：**1399 项全过**（`sh tests/run.sh`）
- **规模**：63 文件 / 19834 行
- **最近改动**：v100 修「昨天工资没入账」（归档抽成 DailyArchive）

**最近几个版本**

| 版本 | 内容 |
|---|---|
| 3.74 | 存钱罐多目标 + 完成确认（`GoalStore`） |
| 3.75 | 自动备份（选一次文件夹 + 周期 + 保留份数） |
| 3.76 | 修「选完备份文件夹后不生效」 |
| 3.77 | 小人语录扩充 15 → 56 条 + 新增 6 类状态 |
| 3.78 | 费率栏跟着状态变（`RateDisplay`）|
| 3.79 | 修「调休出勤」被误显示成 1.5 倍 |
| 3.80 | 周末小人白天不冒 ZZZ + 文案两行 |
| 3.81 | 修两个会随日期爆炸的测试 |
| 3.82 | 主卡加「本周一眼」（v3.83 已删除）|
| 3.83 | 工作节奏移到「今日已赚」同一行 |
| 3.84 | 主卡收紧：删时钟 |
| 3.85 | 修底部空白（分隔线 + 50dp 固定高度）|
| 3.86 | 倒计时改「时:分:秒」|
| 3.87/3.88 | 主卡 + 小组件改「本月累计」|
| 3.89 | 主卡「已结算天数」也改成本月 |
| 3.90 | 修「昨天工资没入账」：归档抽成 DailyArchive |

**评估过但否决的**（详见 `mockup/rejected/README.md`）

- **小人拿游戏机（下班/周末）** —— 会让「下班喜悦」变成「沉迷手机」，收益不值
- **侧视小人 + 墙上电视 + 马里奥** —— 马里奥其实能看清（这点估错了），
  但**小人是椭圆蛋，没有侧面特征**，做真侧视等于重画角色；而且只在这一个场景侧脸，会显得不一致

**用户提过、尚未做的**

- 计时计价（把任意时段折算成钱）
- 变化视角（「比昨天多 ¥43」这类对比，而非绝对值）
- 战报长图（可分享的月度战报图片）
- 通知栏实时金额
- **历史日薪精确化** —— 现在改工资设置会让历史日期的「日薪」跟着变；
  要精确需存工资历史 + 写迁移
- 备份加密（明文 JSON 放公共目录，别的 App 理论上可读）

**备份与存档**

| 位置 | 说明 |
|---|---|
| `/var/minis/shared/salaryjump/` | **持久化备份**（工作区被清空也不丢） |
| `/var/minis/workspace/salaryjump/` | 工作目录 |
| `/tmp/*.bak-*.java` | 单文件快照（沙箱重启后失效，勿依赖） |

### 5.ae 测试里不要有「今天必须满足某条件」的隐含假设（2026-10-20）

写「距下月发薪 > 15 天」「3 天后发薪 = 3」这类断言时，代码在当时那天恰好成立就过了 ——
但**隔几天就挂**，而且挂掉时第一反应是「代码坏了」，白排查一轮。

实际踩了两次：
- `next >= 15` —— 写死的字面量，10/17 能过（正好 15 天），10/20 挂（12 天）
- `Math.min(28, todayDay + 3)` —— **每月 26 号之后必挂**（被截断成 28 号）

**做法**：从 `Calendar` 推算期望值（`getActualMaximum(DAY_OF_MONTH)` 等），
或者给跨月场景准备一个「不跨月」的替代断言分支。

### 5.af 补丁脚本失败要当失败处理（2026-10-20）

脚本里 `assert` 失败 → `sys.exit(1)` → **文件不会被写**，
但脚本前面已经打印了一堆 `OK:` —— 那些替换只存在**内存变量**里。
看到 OK 就以为改好了，实际上文件一个字没变。

**这次踩了两次**：「删时钟」「删分隔线」都以为改了，`rg` 一查还在。

**规则**：改完必须 `rg` 复核实际文件。脚本输出只能说明「内存里改过了」。

### 5.ag 固定高度的容器是空白的隐形来源（2026-10-20）

```java
new LinearLayout.LayoutParams(0, dp(50.0f), 1.0f)   // 容器 50dp
// 里面只有两行文字：13.5sp + 10.5sp ≈ 24dp
// → 26dp 空白无处可去，全堆在卡片底部
```

用户报「底部有空」，查了三层才找全：孤儿分隔线（31dp）+ 固定高度（26dp）。

**规则**：内容行数可能变的地方，高度用 `wrap_content`（`-2`），别写死 dp。

### 5.ah ⚠️ 「每天该做一次」的事不能寄生在显示函数里（2026-10-21）

**事故**：归档昨天的工资 `cumRollover()` 只被 `cumulativeTotal()` / `cumulativeDays()`
顺带调用。v3.87 主卡改用新的 `monthlyTotal()` 后，那两个方法没人调了 →
**归档彻底停摆，昨天的工资一直不入账**。而且没有任何报错，测试全绿。

**规则**：
- 「每天该做一次」的事（归档、清理、同步）**在 onResume 里显式调用**，
  不要把它藏在某个「读数据顺便写一下」的函数里
- 换显示函数时，要查一遍**旧函数里有没有副作用**（它可能不只是读）

**验证**：`rg 'cumRollover|cumulativeTotal|cumulativeDays'` 确认调用链还在。
