# SalaryJump

> 实时工资计数器 · Android 应用

**当前版本：3.90**（versionCode 114） · 包名 `com.salaryjump.app` · minSdk 26 / targetSdk 34

一个把「上班摸鱼时看着钱涨」做成正经工具的 App。

主界面实时滚动显示今天已经赚了多少钱；围绕这个核心，长出了一整套
**把时间和钱对上**的功能：工资日历（日薪/加班费/外快逐日拆解）、加班打卡、
请假与假期账户、月度战报与年度总览、多目标存钱罐、喝水提醒与饮品统计、
打工小人、隐私锁与自动备份。

> 从 v1（手写 smali 改包）一路做到 v87（100 个版本），
> 当前规模 63 个类 / 19834 行，1399 项回归测试全绿。

---

## 目录

- [一、功能说明](#一功能说明)
- [二、技术架构](#二技术架构)
- [三、代码结构](#三代码结构)
- [四、数据存储](#四数据存储)
- [五、构建与打包](#五构建与打包)
- [六、测试](#六测试)
- [七、签名与发布](#七签名与发布)
- [八、常见维护任务](#八常见维护任务)
- [九、已知坑（维护必读）](#九已知坑维护必读)
- [十、版本历史](#十版本历史)

---

## 一、功能说明

### 1. 实时工资计数器（核心）

按「日薪 + 上下班时间 + 午休时段」把一天切成若干状态，实时计算已赚金额：

| 状态 | 含义 | 计价方式 |
|---|---|---|
| 0 休息日 | 非工作日 | 不计 |
| 1 还没上班 | 上班时间前 | 0 |
| 2 上午 | 上午工作时间 | 按秒累加 |
| 3 午休中 | 午休时段 | 暂停累加 |
| 4 下午 | 下午工作时间 | 按秒累加 |
| 5 今日已下班 | 下班后 | 封顶为日薪 |
| 6 加班中 | 下班后仍在打卡 | 按加班倍率累加 |
| 7 休息日出勤 | 休息日标记了出勤 | 按出勤倍率 |
| 8 请假 | 当天请了假 | 按请假比例 |

主卡显示：**今日已赚 / 今日目标 / 每小时 / 每分钟 / 每秒**，以及一行工作节奏小字、
一条进度条、四个功能胶囊（加班 / 请假 / 出勤 / 外快，点按操作、长按看详情）。

> 默认：日薪 300，上班 08:00，下班 17:00，午休 11:30–12:30。

### 2. 打工小人

主卡右侧的小人，**16 种姿势**，随时间和状态自动切换：

- **日常姿势**：通勤 / 上午 / 午休 / 下午 / 下班 / 加班 / 休息日 / 休息日出勤 / 请假 / 周一 / 周五 / 周日晚
- **临时反应**（点击触发，2–3 秒）：惊讶 / 欢呼 / 害羞 / 撒花庆祝
- **连戳彩蛋**：1.2 秒内连戳 3 次 → 捂耳朵「别戳了别戳了」；8 次 → 「你有这功夫，不如去干活」
- **疲劳叠加**：按连续工作天数分 4 级（汗滴 → 黑眼圈+翘发 → 冒烟），点小人会告诉你「疲劳 X/3」
- **工位场景**：工作时画书桌台灯，下班后 2 小时宽限内仍显示（防止加班忘打卡），之后只剩小人
- **可配置**：显示 / 不显示 · 尺寸小(88dp)/中(104dp)/大(120dp) · 是否画工位
- **动态语录**：点击时说一句，内容取决于真实状态（连上 10 天 / 发薪日 / 请假 / 存钱罐进度）

### 3. 喝水提醒 + 饮品体系

- 工作时段内按间隔提醒（**30 / 45 / 60 / 90 / 120** 分钟，默认 90）
- 三种提醒方式：**仅铃音 / 仅震动 / 铃音和震动**
- **感知出勤**：节假日/周末标记出勤后，当天照常提醒
- **独立时段**：可单独设定喝水提醒的时间窗（不必跟随上下班），可选是否跳过午休、是否周末也提醒
- 通知带快捷操作：**已喝 XX ml** / **10 分钟后**

**饮品体系**：
- **内置 8 种** + 自定义（名称 ≤8 字 + 单位 + 容量），上限 32 种
- **参考表**：内置 ~21 条常见饮品的热量与含糖数据（可增删改），添加饮品时可从参考表选
- **热量统计**：按每 100ml 计，记录每条饮品的热量和含糖标记
- **喝过的饮品不可删**（会破坏历史记录），没喝过的可直接删
- **单位可改**（杯/瓶/罐…），容量可改

### 4. 工资日历（核心视图）

整月日历网格，每格显示当天的**日薪 / 加班费 / 外快**：

- **加班日显示两行**：日薪（绿）+ 加班费（橙）
- **有外快显示「外 N」**（蓝）
- **补班日标「补班」** —— 被安排上班的周末（1 倍工资，没有额外补贴，最容易看糊涂）
- **点某天** → 下方显示完整拆分（日薪 / 加班费·时长 / 外快·备注 / 补录 / 当天到手）
- **长按或点「操作」** → 标记出勤 / 请假 / 记外快 / 删除记录
- **补记**：给过去某天补录工资（占位行）或整月补录

### 5. 月度战报 / 年度总览

**月度战报**：本月累计 · 计薪天数 · 日均 · 请假天数 · 最高的一天 · 存钱罐 ·
每日明细（日历网格）· 已完成的目标
**年度总览**：全年总收入 · 每月收入柱状图 · 全年统计（8 项）· 假期账户

全年统计：计薪天数 · 请假天数 · 休息日出勤 · **加班次数** · **加班费总额** ·
外快总额 · 最佳月份 · 日均

> 加班次数/总额是**从每日金额反推**的（`入账 − 应得 > 0`），
> 因为打卡记录只留最后一次，历史加班没存下来。

### 6. 存钱罐（支持多目标）

- **目标**：名称 + 金额 + 进度起算日
- **三种状态**：
  - **进行中** → 百分比 · 里程碑条 · 已攒/还差/预计完成
  - **已达成**（≥100%）→ 显示达标日 + **「✅ 已买到，确认完成」按钮**
  - **已完成** → 进历史（起止日期 · 用时 · 目标 vs 实际 · 超出多少）
- **必须确认才能开下一个目标** —— 否则两个目标会同时在攒，钱算不清
- 新目标的起算日**自动接在完成日次日**，两段统计区间**不重叠**
- 已完成的目标把实际金额**冻结**，以后补录历史也不会让旧数字跳变
- 界面隐藏金额时（`cumHidden`）存钱罐同步遮蔽

### 7. 加班 / 请假 / 外快 / 补录

| 功能 | 说明 |
|---|---|
| **加班打卡** | 主界面点胶囊开始/结束；按「日薪 ÷ 工作时长 × 倍率」算加班费 |
| **加班规则** | 工作日倍率（默认 1.5）、宽限分钟数、周末模式（调休 / 计薪）、法定倍率 |
| **请假** | 按天标记，可选假别（年假/病假/事假/育儿假…）与计薪比例、是否半天 |
| **假期账户** | 每种假别的年度额度与已用天数（年假 5 天、育儿假等） |
| **外快** | 记一笔额外收入（带备注），计入当天收益 |
| **月度补录** | 给过去某个月补一笔收入（如换工作前的历史），按天摊到日历上 |

### 8. 智能工作日

三种模式：**每天** / **周一至周五** / **智能（默认）**

**内置节假日数据**（`WorkdayHelper.BUILTIN`）覆盖 2025–2026 的法定节假日与调休：
- 格式 `yyyyMMdd:标志:名称`，标志 `1` = 休、`0` = 班（调休上班）
- 例 `20261010:0:国庆节` → 2026-10-10（周六）是补班日
- 未收录年份自动回退「周一至周五」

**可选读日历**：扫描手机日历全天事件，标题含「班」= 工作日、含「休/假」= 休息日（15 分钟缓存）

**在线更新**：设置里可「更新节假日」，从网络拉最新数据（会显示分阶段进度与失败原因）

### 9. 提醒体系

| 提醒 | 说明 |
|---|---|
| **喝水提醒** | 见上 |
| **打卡提醒** | 工作日问加班、休息日问出勤；已打卡/已请假时不打扰 |
| **发薪日提醒** | 发薪当天与前一天 10:00 提醒，通知里可直接打开上月战报 |

### 10. 数据安全

- **隐私锁**：指纹 / 密码保护，进入 App 前验证；支持弱密码检测
- **手动备份**：导出/恢复全部数据（JSON）
- **自动备份**：**选一次文件夹**（SAF 授权，永久记住）+ 周期（每天/每周/每月）+
  时间 + 保留份数（7/30）；文件名带日期形成历史版本；
  **失败会发通知**，设置里显示上次备份结果
- **崩溃记录**：启动时若有未看过的崩溃，**直接弹完整堆栈**（不再是静默的）
- **网络诊断**：排查「更新节假日失败」

### 11. 桌面小组件

显示：状态文字 / 计时器 / 实时金额 / 进度条 / 今日喝水 / 累计收入 / 下次提醒 / 快捷喝水按钮 / 打工小人。

- 刷新间隔：**工作时段 15 秒**，非工作时段 **60 秒**（熄屏跳过渲染省电）
- 由前台服务 `WidgetUpdateService` 驱动；没有小组件时自动停止

### 12. 界面

- **Aurora Glass** 视觉：翡翠渐变主卡 + 渐变背景 + 大圆角毛玻璃
- **深色模式**：跟随系统，全页面统一主题（小人配色也分昼夜）
- **沉浸式** edge-to-edge，适配状态栏与手势条
- 金额遮蔽：一键隐藏所有金额（防偷窥），存钱罐/累计同步

---

## 二、技术架构

### 技术栈

| 项 | 选型 | 说明 |
|---|---|---|
| 语言 | Java 8 | `-source 8 -target 8`，无 kotlin |
| 平台 | Android SDK | minSdk 26（Android 8.0）/ targetSdk 34 |
| 依赖 | **零第三方库** | 全部使用 framework 原生 API |
| 构建 | **非 Gradle** | 见下方说明 |
| 存储 | SharedPreferences | 无数据库、无网络权限 |
| UI | 纯代码构建 | 无 XML 布局（除小组件 `widget_salary.xml`） |

### 为什么没有 Gradle 工程？

本项目**改造自一个已发布的 APK**（反编译 → 修改 Java 源码 → 重新编译打包），
而不是从头新建的 Android Studio 工程。因此：

- **没有 Gradle**，构建走自定义工具链（javac + d8 + apktool + apksigner）
- **源码有两份历史形态**：
  - `work-java/` — **Java 源码（当前使用，改这里）**
  - `src/`、`work_bak*/` — 早期手写 smali 形态（v1–v5，仅作历史参考，不要再改）
- 资源（布局、图标、字符串）仍通过 **apktool** 回编译

> 维护约定：**改代码改 `work-java/`，改资源改 `work/`**，两者最后合成 APK。

### 运行时组件

```
MainActivity ───────────── 主界面（含 SalarySettings / SalaryCalculator / SettingsDialog / 主题）
  ├─ SettingsActivity ───── 设置页外壳（分区内容由 SettingsDialog 构建）
  ├─ SalaryCalendarActivity  工资日历（日历网格 + 点选详情 + 补记）
  ├─ MonthlyReportActivity   月度战报
  ├─ YearlyReportActivity    年度总览
  ├─ WorkRulesActivity       加班与出勤规则
  ├─ LeaveAccountsActivity   假期账户
  ├─ WaterStatsActivity      喝水统计
  ├─ WaterRecordsActivity    喝水记录
  └─ GuideActivity           加班与请假说明

WaterReminderReceiver ──── 提醒广播（提醒触发 / 快捷记录 / 开机重排）
  └─ WaterReminderScheduler  闹钟调度与记录存储
  └─ DrinkTypes ───────────── 饮品体系（内置 + 自定义 + 参考表）
  └─ WaterHistory ─────────── 历史归档

PaydayReceiver / CheckInReceiver  发薪日 / 打卡提醒
BackupReceiver ─────────── 自动备份（闹钟触发 / 开机重排）
  └─ BackupScheduler ──────── 目录授权 + 闹钟 + 通知 + 执行
  └─ BackupPlan ───────────── 文件名 / 周期 / 保留策略（纯逻辑）

SalaryWidgetProvider ───── 小组件（RemoteViews 渲染）
  └─ WidgetUpdateService ──── 前台服务，定时刷新

（无 Binder 通信，全部经 SharedPreferences 共享状态）
```

### 关键机制

| 机制 | 实现要点 |
|---|---|
| 主题 | `MainActivity.applyTheme(Context)`，静态可变颜色字段，所有页面共用 |
| 实时刷新 | `Handler.postDelayed` 每 1000ms 重算（原版 100ms，已优化） |
| 闹钟 | `setExactAndAllowWhileIdle`（API 31+ 需 `canScheduleExactAlarms`） |
| 通知 | 3 个独立 `NotificationChannel`（声音/震动/两者），重要性 DEFAULT |
| 数据过期 | 喝水数据按天自动归档，`ensureCurrentDay` 惰性触发 |
| 累计结算 | `cumRollover` 惰性结算（启动时补齐遗漏的工作日，上限 4000 天防死循环） |

---

## 三、代码结构

源文件全部位于 `work-java/com/salaryjump/app/`：

**总计 63 个文件 / 19834 行**

**核心界面**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `MainActivity.java` | 5769 | 主界面 + 设置模型 + 计价核心 + 设置弹窗 + 主题 |
| `UiKit.java` | 320 | 绘制工具箱：渐变 / 毛玻璃卡 / 胶囊 / 图标（纯代码，无 XML） |
| `MotionKit.java` | 311 | 动画与交互反馈：入场、按压、数字滚动 |
| `SettingsActivity.java` | 241 | 设置页外壳（各分区在 SettingsDialog 里构建） |
| `AuroraBackdrop.java` | 158 | Aurora 渐变背景 |
| `SheenBar.java` | 144 | 进度条控件（带流光） |

**工资计算**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `SalaryHistory.java` | 232 | 按天归档的工资（`yyyyMMdd:分`）+ 月/年聚合 |
| `LeaveRecord.java` | 227 | 请假记录（假别 / 计薪比例 / 半天） |
| `DayBreakdown.java` | 217 | **每日金额拆分**：日薪 / 加班费 / 外快 / 补录（日历格子用） |
| `ExtraIncome.java` | 185 | 外快记录（带备注） |
| `MonthBackfill.java` | 184 | 月度补录 + 存钱罐进度计算 |
| `ArchiveSync.java` | 179 | 归档行与事件的同步、重算、自愈 |
| `HolidayWork.java` | 173 | 休息日出勤的倍率与结算 |
| `WorkRules.java` | 111 | 加班与出勤规则（倍率 / 宽限 / 周末模式） |
| `LeaveAccounts.java` | 98 | 假期额度账户（年假 / 育儿假…） |
| `OvertimeLog.java` | 89 | 加班打卡（**只存最后一次**） |
| `YearlyStats.java` | 47 | 年度聚合（加班次数与总额） |
| `OvertimeCalc.java` | 26 | 加班费计算（日薪 ÷ 工时 × 倍率） |

**存钱罐**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `GoalStore.java` | 469 | **多目标**：列表读写 / 迁移 / 冻结 / 进度区间 |

**打工小人**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `WorkerPet.java` | 1175 | **纯 Canvas 绘制**：16 种姿势 + 4 级疲劳 + 反应 |
| `PetState.java` | 279 | 小人状态推导：疲劳 / 发薪日 / 语录 / 工位判定 |
| `WorkerPetView.java` | 264 | 小人视图：动画循环、工位可见性、点击反应 |
| `PetSettings.java` | 86 | 小人显示设置（显示 / 尺寸 / 工位场景） |
| `HeroFit.java` | 69 | **主卡排版计算**（保证金额不被小人挤掉） |

**喝水体系**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `DrinkTypes.java` | 883 | 饮品体系：8 内置 + 自定义 + 参考表 + 输入范围 |
| `WaterReminderScheduler.java` | 375 | 提醒调度 + 记录读写 + 日切归档 |
| `WaterStatsActivity.java` | 296 | 喝水统计页（今日 / 近 7 天 / 近 30 天） |
| `WaterRecordsActivity.java` | 285 | 喝水记录页（列表 / 删除 / 改时间） |
| `WaterReminderReceiver.java` | 118 | 提醒广播：通知、快捷记录、开机重排 |
| `WaterHistory.java` | 103 | 饮水历史归档（保留 365 天） |
| `WaterReminderSettings.java` | 67 | 提醒设置（开关 / 间隔 / 目标 / 模式 / 时段） |

**工作日 / 节假日**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `HolidayUpdater.java` | 437 | 节假日在线更新（含分阶段进度） |
| `WorkdayHelper.java` | 255 | 工作日判定：内置节假日 + 日历 + **补班识别** |
| `StatutoryHolidays.java` | 69 | 法定节假日清单（判定三倍工资） |

**数据安全 / 备份**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `BackupScheduler.java` | 383 | **自动备份**：目录授权 + 闹钟 + 通知 + 执行 |
| `PrivacyLock.java` | 192 | 隐私锁（指纹 / 密码 + 弱密码检测） |
| `BackupTool.java` | 177 | 手动导出 / 恢复（JSON） |
| `BackupPlan.java` | 175 | 备份纯逻辑：文件名 / 周期 / 保留策略 |
| `CrashLogger.java` | 129 | 崩溃记录（启动时弹完整堆栈） |
| `NetDiag.java` | 120 | 网络诊断 |
| `Contexts.java` | 39 | 从包装过的 Context 里找出宿主 Activity |
| `BackupReceiver.java` | 32 | 备份闹钟触发 + 开机重排 |

**提醒**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `PaydayReminder.java` | 175 | 发薪日提醒（当天 + 前一天） |
| `CheckInReminder.java` | 152 | 打卡提醒（工作日问加班 / 休息日问出勤） |
| `PaydayReceiver.java` | 107 | 发薪日广播 |
| `CheckInReceiver.java` | 99 | 打卡提醒广播 |

**小组件**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `SalaryWidgetProvider.java` | 221 | 小组件渲染 + 刷新节奏 + 点击响应 |
| `WidgetUpdateService.java` | 124 | 前台服务，驱动小组件刷新 |
| `WidgetText.java` | 31 | 小组件文案拼接 |

**子页面**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `SalaryCalendarActivity.java` | 727 | **工资日历**（日历网格 + 点选详情 + 补记） |
| `MonthlyReportActivity.java` | 474 | 月度战报 |
| `LeaveAccountsActivity.java` | 447 | 假期账户页 |
| `YearlyReportActivity.java` | 430 | 年度总览 |
| `GuideActivity.java` | 378 | 加班与请假说明 |
| `WorkRulesActivity.java` | 331 | 加班与出勤规则设置页 |
| `ExtraIncomeDialog.java` | 172 | 外快记账弹窗 |

**其它**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `SeedBackfill.java` | 83 | 首装历史数据种子 |
| `R.java` | 70 | 资源 ID（apktool 生成，勿手改） |
| `DataCache.java` | 67 | **读取缓存**（避免重复解析历史字符串） |
| `SectionSave.java` | 38 | 设置分区 → 保存范围的映射 |

**未归类**

| 文件 | 行数 | 职责 |
|---|---:|---|
| `CalendarGrid.java` | 193 | |

> **维护重点**：`MainActivity` 是最大的文件（含设置模型、计价核心、设置弹窗、主题系统）。
> `SalaryCalculator.calculate()` 是所有金额计算的**唯一入口**（App 与小组件共用）。


---

## 四、数据存储

全部使用两个 SharedPreferences 文件，**无数据库**。

### `salary_jump`（主）

| 键 | 类型 | 含义 |
|---|---|---|
| `daily_salary_bits` | long | 日薪（double 的位模式） |
| `work_start` / `work_end` | int | 上班/下班时间（分钟数，如 480 = 08:00） |
| `break_start` / `break_end` | int | 午休起止（分钟数） |
| `work_mode` | int | 工作制：0 每天 / 1 周一至五 / 2 智能 |
| `use_calendar` | bool | 是否读手机日历 |
| `water_enabled` | bool | 喝水提醒开关 |
| `water_interval` | int | 提醒间隔（分钟） |
| `water_target` | int | 每日目标（ml） |
| `water_cup` | int | 每次喝水（ml，即白开水容量） |
| `water_mode` | int | 提醒方式：0 仅铃音 / 1 仅震动 / 2 两者 |
| `water_day` | string | 当前数据归属日期（`yyyy-MM-dd`） |
| `water_records` | string | 当日记录：`时间戳,毫升,饮品ID;...` |
| `water_consumed` | int | 当日总量（旧字段，用于迁移兼容） |
| `water_history` | string | 历史归档：`yyyyMMdd:饮品ID:毫升:次数;...` |
| `drink_ml_<id>` | int | 各饮品容量（id=0 除外，白开水用 `water_cup`） |
| `custom_drinks` | string | 自定义饮品：`id,名称,单位;...` |
| `drink_next_id` | int | 下一个自定义饮品 ID（只增不减，不复用） |
| `hidden_drinks` | string | 被移除（隐藏）的内置饮品 id，逗号分隔，如 `4,6` |
| `cum_last_day` | int | 累计已结算到哪天（`yyyyMMdd`） |
| `cum_total_bits` | long | 累计总额（double 的位模式） |
| `cum_days` | int | 已累计天数 |
| `exact_alarm_prompted` | bool | 精确闹钟引导是否已弹过（防重复打扰） |
| `holiday_data` | string | 在线更新的节假日数据（覆盖内置） |
| `holiday_updated_at` | long | 节假日数据更新时间 |
| `statutory_data` | string | 法定节假日清单（用于判定法定三倍） |
| **存钱罐** | | |
| `goal_list` | string | 目标列表：`名称\|目标分\|开始日\|达标日\|确认日\|完成金额分`（`;` 分隔多条） |
| `goal_name` 等 4 个旧键 | — | **迁移后保留不删**，作为回退保险 |
| **打工小人** | | |
| `pet_visible` | bool | 是否显示小人（默认 true） |
| `pet_size` | int | 尺寸档位 0/1/2 → 88/104/120 dp（默认 1） |
| `pet_scene` | bool | 是否画工位场景（默认 true） |
| **自动备份** | | |
| `backup_auto_enabled` | bool | 自动备份开关 |
| `backup_tree_uri` | string | 备份文件夹的 SAF URI（持久化授权） |
| `backup_tree_label` | string | 目录显示名 |
| `backup_period` | int | 周期 0 每天 / 1 每周 / 2 每月 |
| `backup_keep` | int | 保留份数档位 0→7 份 / 1→30 份 |
| `backup_hour` / `backup_minute` | int | 备份时间（默认 22:00） |
| `backup_last_at` | long | 上次备份时间 |
| `backup_last_state` | string | 上次备份结果文案 |
| **其他** | | |
| `external_income` | string | 外快记录 |
| `leave_records` | string | 请假记录 |
| `overtime_*` | — | 加班打卡（`ot_day`/`ot_start`/`ot_end`，**只存最后一次**） |
| `water_win_start` / `water_win_end` | int | 独立喝水时段 |
| `water_window_mode` | int | 喝水时段模式 0 跟随工作 / 1 自定义 |
| `water_skip_break` | bool | 自定义时段是否跳过午休 |
| `water_weekend` | bool | 自定义时段周末是否也提醒 |
| `drink_unit_<id>` | string | 饮品单位（杯/瓶/罐…） |
| `salary_history` | string | 按天归档的工资：`yyyyMMdd:分;...` |
| `month_backfill` | string | 月度补录记录 |
| `crash_shown_at` | long | 崩溃弹窗已展示到哪个时间（避免重复打扰） |

### `salary_jump_widget`

无数据，仅用作前台服务的通知频道 ID。

### 数据格式约定（改动前务必注意）

- **饮品 ID 稳定性**：自定义饮品 ID 从 8 起自增、**删除后不复用**。
  这样历史记录不会串位；被删饮品的历史记录显示为「已删除饮品」。
- **记录格式向后兼容**：`water_records` 读取时允许 2 字段（`时间戳,毫升`，视为白开水）。
- **金额用位模式存储**：`double` 通过 `Double.doubleToRawLongBits` 存为 long。

---

## 五、构建与打包

### 工具链

全部位于 `/opt/tools/`（沙箱内），均已安装配置完毕：

| 工具 | 路径 | 用途 |
|---|---|---|
| JDK 17 | `/usr/lib/jvm/java-17-openjdk/` | javac / java |
| android.jar | `/opt/tools/android.jar` | API 35 编译时类库 |
| d8 | `/opt/tools/d8/d8.jar` | class → dex |
| apktool | `/opt/tools/apktool-patched.jar` | 资源/Manifest 编解码 |
| aapt2 | `/opt/tools/aapt2_x86_64` + `aapt2-wrap.sh` | 经 `qemu-x86_64` 运行（apktool 调用） |
| zipalign | `/opt/tools/zipalign.py` | 纯 Python 对齐器 |
| apksigner | `/opt/tools/apksigner.jar` | 签名 |
| jadx | `/opt/tools/jadx/bin/jadx` | 反编译（复核用） |
| build.sh | `/opt/tools/build.sh` | Java → dex |
| pack.sh | `/opt/tools/pack.sh` | dex 换入 + 对齐 + 签名 |

### 完整构建流程（三步）

```sh
cd /var/minis/workspace/salaryjump

# ① Java 源码 → classes.dex
sh /opt/tools/build.sh work-java /tmp/build
#   产出 /tmp/build/classes.dex

# ② 资源 + Manifest → 基础 APK（不改资源也要跑，它负责整体打包）
java -Xmx420m -jar /opt/tools/apktool-patched.jar b -f -o /tmp/base.apk work

# ③ 换入 dex + 对齐 + 签名
sh /opt/tools/pack.sh /tmp/base.apk /tmp/build/classes.dex /tmp/SalaryJump.apk
#   产出已签名的 /tmp/SalaryJump.apk
```

### 版本号

改 `work/apktool.yml`：

```yaml
versionInfo:
  versionCode: 11        # 每次 +1（覆盖安装必需递增）
  versionName: 2.0
```

### 发布前验证

```sh
# 签名校验（应显示 v2/v3 true）
java -jar /opt/tools/apksigner.jar verify -v --print-certs /tmp/SalaryJump.apk

# 对齐校验（resources.arsc 必须是 STORE=True 且 4-aligned=True）
python3 /opt/tools/zipcheck.py /tmp/SalaryJump.apk | grep resources.arsc

# 元数据核对（版本、权限、标签）
qemu-x86_64 /opt/tools/aapt2_x86_64 dump badging /tmp/SalaryJump.apk | head -5

# 可选：反编译复核改动是否真的进了包
/opt/tools/jadx/bin/jadx -d /tmp/check --no-res /tmp/SalaryJump.apk
```

---

## 六、测试

### 回归测试（推荐每次改完都跑）

```sh
sh tests/run.sh
```

纯 JVM 测试，用 `tests/stubs/` 下的 Android API 桩替代框架，
**直接编译并运行 `work-java/` 里的真实业务逻辑**，不需要设备。

**当前 1285 项全部通过**，51 个分组：

| 组 | 内容 |
|---|---|
| A–J | 饮品基础 / 自定义 / 记录格式 / 归档 / 迁移 / 工作日 / 边界 / 删除规则 / 改名 / 热量 |
| K–L | 参考表（可编辑 + 老用户升级） |
| M–O | 加班计算 / 工资日历归档 / 加班打卡与跨天 |
| P–S | 法定出勤 / 崩溃记录 / 更新阶段 / 宿主 Activity 解析 |
| T–W | 请假 / 隐私锁+发薪日 / 加班规则 / 外快 |
| X–AB | 归档同步 / 行同步 / 自愈预写行 / 假期账户 / 打卡提醒 |
| AC–AH | 小组件文案 / 补记 / 种子 / 补建重算 / 记录改时间 / 提醒感知出勤 |
| AJ–AM | 独立喝水时段 / 饮品单位 / 月度补录 / 存钱罐起算 |
| AN–AP | 小人状态 / 姿势推导 / 设置与点击反应 |
| AQ–AT | 输入范围 / 主卡排版 / 分区保存分发 / 每日金额拆分 |
| AU–AY | 读取缓存正确性 / 年度加班统计 / 补班日 / 存钱罐多目标 / 自动备份 |

**测试原则**：规则抽成**纯函数**再测（不依赖 Android）。
这是踩过坑才形成的习惯 —— 内联在 UI 层的校验逻辑测试碰不到，
反编译恢复时出错也发现不了。


### 手工验证清单（发版前）

**基础**

- [ ] App 冷启动不崩溃，金额正常滚动
- [ ] 深色模式切换正常（含小人配色）
- [ ] 覆盖安装不丢数据

**设置**（每个分区都点一次保存 —— 分区控件是按需创建的，容易漏）

- [ ] 薪跳设置 → 保存生效
- [ ] 喝水设置 → 保存生效（容量/热量校验不应误报）
- [ ] 工具与安全 → 保存**不崩溃**
- [ ] 打工小人 → 改尺寸/开关，保存生效

**工资日历**

- [ ] 日历网格显示日薪 / 加班费（橙）/ 外快（蓝）/ 补班标记
- [ ] 点某天 → 下方详情拆分正确
- [ ] 长按 → 操作菜单可弹出
- [ ] 切换月份正常，历史月份也能看

**存钱罐**

- [ ] 目标进度与主界面一致（战报页也是同一个口径）
- [ ] 攒够后出现「确认完成」按钮
- [ ] 确认后进「已完成的目标」，能开新目标

**备份**

- [ ] 手动导出 → 文件在
- [ ] 选自动备份文件夹 → **自动重开设置页且已生效**
- [ ] 「立即备份一次」→ 通知 + 文件落到该目录

**喝水 / 小组件**

- [ ] 喝水记录 → 统计页数据一致
- [ ] 小组件在 15 秒内刷新

---

## 七、签名与发布

### 签名证书

| 项 | 值 |
|---|---|
| Keystore | `/opt/tools/salaryjump-release.keystore` |
| 别名 | `salaryjump` |
| 口令 | 见 `/var/minis/workspace/salaryjump/keystore/README.txt` |
| 主体 | `CN=SalaryJump, OU=Mobile, O=SalaryJump, L=Beijing, ST=Beijing, C=CN` |
| SHA-256 | `25e0b776444726e80aa652d082b5bd3cb3ab0e9391060e8e9ea88a0747f4fbff` |

> `pack.sh` 已内置签名参数，正常构建无需手动签。

### 重要约束

1. **必须始终使用同一把证书**。换了证书，用户必须**先卸载再安装**，且数据会全部丢失。
2. 早期版本（v1–v3）用的是 Android 公开调试证书，会导致系统弹「有危险」的安装警告；
   v4 起改用上述专属证书，问题已解决。
3. 每次发布**递增 `versionCode`**，否则无法覆盖安装。

### 交付

APK 放 `/var/minis/attachments/`（用户在聊天里可直接下载），
同时在 `workspace/salaryjump/` 留档一份。

---

## 八、常见维护任务

### 改界面颜色 / 深色模式配色

改 `MainActivity.applyTheme(Context)` 里的两组颜色（night / 非 night 分支）。

**关键约定（清新版起）**：
- 调色板是 `static` **可变字段**（运行时随主题切换），**不要用 `static final` 缓存颜色**（见「已知坑 2」）。
- 渐变/柔光/胶囊按钮的绘制统一在 `UiKit` 里；页面背景用 `MainActivity.bgGradient()`，
  卡片用 `MainActivity.softCard()`，次级按钮用 `MainActivity.softPill()` —— 其他页面共用。
- 主视觉卡（hero）配色是 `HERO_COLORS` 数组，页面底色是 `BG_COLORS`，胶囊按钮是 `PILL_COLORS`。
- 主卡上的文字颜色是**固定的白色系常量**（`HERO_TEXT` / `HERO_SUB` / `HERO_SUB2`），
  因为主卡永远是深色渐变底，不随明暗主题变化。

### 改卡片布局

在 `MainActivity.buildInterface()` 里。注意卡片顺序由 `root.addView()` 的调用顺序决定，
`buildCumulativeCard()` 目前在最后调用。

### 加一种内置饮品

在 `DrinkTypes`：`NAMES`、`UNITS`、`DEFAULTS` 三个数组要**同步扩容**，
`BUILTIN` 常量 +1，并检查 `MAX` 是否够用（ID 空间 8 起）。

> 注意：改 `BUILTIN` 会让**已被用户改过容量的内置饮品**沿用旧键，无迁移问题，
> 但会更动自定义饮品的 ID 起点，存在极小概率的数据错位风险 —— 尽量用「自定义饮品」机制替代。

### 更新节假日数据（**每年 11 月前后必做**）

国务院通常在前一年 11 月公布次年安排。更新 `WorkdayHelper.BUILTIN` 字符串：

```java
// 格式：yyyyMMdd:标志:名称;...     标志 1=休 / 0=班（调休）
"20260101:1:元旦;20260104:0:元旦;..."
```

**数据源**（已验证可靠，二选一）：
- GitHub `NateScarlet/holiday-cn`（源自 gov.cn 公告）
- iCloud 中国大陆节假日日历 `calendars.icloud.com/holidays/cn_zh.ics`（含「休/班」标记）

更新后**跑一遍 `tests/run.sh`**，并同步更新测试里的日期断言。

### 改默认设置值

- 工资/时间：`MainActivity.SalarySettings` 的字段默认值
- 喝水：`WaterReminderSettings` 的字段默认值

> 注意：默认值只对**首次安装**生效，老用户已有存储值不会变。

### 加一个页面

1. 写 `XxxActivity.java`
2. 在 `work/AndroidManifest.xml` 注册：`<activity android:name=".XxxActivity" android:exported="false" android:label="..."/>`
3. 页面里记得调 `MainActivity.applyTheme(this)` 接入深色模式

### 排查「提醒不响/不振」

见 `notes/water-reminder-alert-analysis.md`（完整分诊流程）。要点：
- 频道是**懒创建**的 —— 系统通知设置里看不到「喝水提醒」类别 = 从未触发过提醒
- **频道参数创建后 App 改不动**，被静音只能换新频道 ID 或用户手动改回

---

## 九、已知坑（维护必读）

### 1. ⚠️ 打包必须 zipalign

Android 11+ 要求 `resources.arsc` 以 STORE 方式存储且 4 字节对齐，
否则安装时只提示「应用未安装」，**没有任何错误细节**。
`pack.sh` 已包含该步骤，手工打包时不要漏。

### 2. ⚠️ 主题色不要静态缓存

早期写法 `private static final int X = MainActivity.COLOR_Y;` 会踩坑：
静态字段初始化早于 `applyTheme()`，会读到**浅色模式的旧值**。
正确做法：直接用 `MainActivity.COLOR_Y` 引用（每次读取）或在 `onCreate` 里赋值。

### 2b. ⚠️ `COLOR_SURFACE` 曾被当成 `-1`

`COLOR_SURFACE` 在浅色模式下是 `0xffffffff`，而 Java 里 `int` 的 `-1` 也是全 `F` 位，
早期多个位置把它当 `-1`（= `MATCH_PARENT`）用 —— **浅色模式下碰巧能用、深色模式下必然出错**。
`WaterRecordsActivity` 里还剩两处这种写法（已改回字面量 `-1`）。
新增代码请一律使用 `MainActivity.MATCH` 或字面量 `-1`，不要用颜色值占位。

### 3. ⚠️ `-1` 的双重含义

`COLOR_SURFACE` 曾被当作 `MATCH_PARENT`（-1）的字面量用在若干处布局参数里，
v8 已拆出 `MainActivity.MATCH = -1` 常量并替换干净。
改主题相关代码时注意区分 **「颜色值」** 与 **「布局常量」**。

### 4. ⚠️ jadx 反编译有歧义

jadx 对某些复杂循环/嵌套 if 会渲染出**看似错误**的代码（甚至标 "Code decompiled incorrectly"）。
下结论前请用 apktool 的 smali 或原始字节码**双重确认**。
本项目代码已是手写 Java，一般无此问题，但复核旧版本时要注意。

### 5. apksigner 报 v1 签名 false 是正常的

原始包与重打包包均显示 `Verified using v1 scheme (JAR signing): false`，
v2/v3 为 true。这是正常行为（该 App 从 minSdk 26 起，v1 非必需），不影响安装。

### 6. 通知频道创建后不可变

Android 8+ 中，频道的**声音/振动/重要性**在创建后由系统与用户接管，
App 代码无法再改回来。若需调整已被用户修改过的频道，只能**换新频道 ID**。

### 7. ⚠️ 同一个业务概念只能有一个计算入口

踩过三次：

| 场景 | 后果 |
|---|---|
| 存钱罐进度 | 主界面用 `goalProgress(起算日)`，战报用 `totalCents()`（全部历史）→ 数字对不上 |
| 备份 JSON | 手动导出与自动备份各写一份构造 → 格式会走样 |
| 备份目录接管 | MainActivity 与 SettingsActivity 各写一套 → 其中一个永远不触发 |

**规则**：抽成一个函数 / 一个类，两边都调它。日历网格（`CalendarGrid`）、
存钱罐（`GoalStore`）、备份（`BackupPlan`）都是这么处理的。

### 8. ⚠️ Dialog 里 `startActivityForResult` 要看清宿主

`SettingsDialog` 是 `MainActivity` 的内部类，但**实例是在 `SettingsActivity` 里创建的**。
所以回调会回到 **SettingsActivity**，不是 MainActivity —— 而且**没有编译期报错**。

发起前先确认「这个 Dialog 是谁 new 出来的」，把 `onActivityResult` 写在**那个 Activity** 里。
拿宿主用 `Contexts.anyActivityOf(activityContext)`。

### 9. ⚠️ 反编译恢复的代码，警惕「不可测的边界逻辑」

jadx 压平控制流有三种模式，都会产出**看着正常、跑起来错**的代码：

| 模式 | 表现 |
|---|---|
| 提前返回守卫 | `if (越界) { 报错; return; }` → `if (合法) { 成功路径 } 报错; return;` |
| if/else 被压平 | `if (a) { X = A; } else { X = B; }` → `if (a) { X = A; } X = B;` |
| 循环条件折进循环体 | `for (…) { i = (…) ? i+1 : 0; 报错; return; }` |

**对策**：① 规则抽成纯函数再写测试 ② 改完跑三个检测脚本
（`audit_guards.py` / `audit_flatten.py` / `audit_loops.py`）③ 关键方法用 `cfg.py` 渲染控制流人工核对。

### 10. ⚠️ 渲染循环里别重复查存储

`SalaryHistory.load()` 会重新解析整个历史字符串（3000 字符 → 200 个对象），
而 `has()` / `centsOf()` 各调一次。日历渲染 31 天原来要查 62 次 → 手机 23–39ms（一帧只有 16ms）。

**对策**：`DataCache` 缓存（读一次解析一份，写入时 `invalidate()`）。改用后降到 3–5ms。

### 11. Shell 环境注意（BusyBox ash）

- 不支持花括号展开 `{a,b,c}` → 用空格分隔 + for 循环
- 写文件用 `file_write` 工具，避免 heredoc 的解析问题
- 搜索文件用 `rg`，不要用 `grep -r`

---

## 十、版本历史

详见 [CHANGELOG.md](CHANGELOG.md)。摘要：

| 版本 | versionCode | 主题 |
|---|---:|---|
| 1.7-optimized (v1–v4) | 8 | 安全修复、性能优化、权限引导、专属签名证书 |
| 1.7 (v5) | 8 | 累计收入（App + 小组件） |
| 1.8 | 9 | 智能工作日（内置节假日 + 日历） |
| 1.9 | 10 | 饮品品类 + 喝水统计 |
| 2.0 | 11 | UI 优化（7 项）+ 自定义饮品 + 深色模式 |
| 2.1–2.8 | 12–19 | 清新视觉改版 · 删除规则 · 改名 · 热量含糖 · 参考表 · 啤酒数据 |
| 3.0–3.6 | 20–28 | 多页重构 · 节假日在线更新 · 崩溃记录 · 沉浸式 · Aurora Glass |
| 3.7–3.36 | 29–57 | 隐私锁 · 月度战报 · 累计遮蔽 · 补录体系 · 打卡提醒 · 打工小人登场 |
| 3.37–3.5x | 58–80 | 小人动画/状态全组合 · 工位判定 · 独立喝水时段 · 常喝频次 · 月度补录 |
| **3.6x** | **81–100** | 见下 |
| 3.61 | 85 | 周一/周五氛围覆盖修复 |
| 3.62 | 86 | 小人优化六项（尺寸/反应/疲劳提示/触觉/设置项/连戳彩蛋） |
| 3.63 | 87 | 修重建引入的「保存误报容量」 |
| 3.64 | 88 | 补齐同类：喝水总量误报 + 提醒方式存不进去 |
| 3.65 | 89 | 小人不再挤掉金额（`HeroFit` 排版计算） |
| 3.66 | 90 | 消除选「大」档崩溃隐患 + **崩溃不再静默** |
| 3.67 | 91 | 修「工具与安全里点保存」空指针崩溃 |
| 3.68 | 92 | 工资日历改为日历网格（日薪/加班/外快） |
| 3.69 | 93 | 日历加外快显示（三行三色 + 真蓝） |
| 3.70 | 94 | **读取缓存**（日历提速 7.5 倍）+ 战报复用日历网格 |
| 3.71 | 95 | 修战报存钱罐口径（原用全部历史总额） |
| 3.72 | 96 | 年度统计加「加班次数 / 加班费总额」 |
| 3.73 | 97 | 日历补班标记 + 年度统计收敛成 2 行 |
| 3.74 | 98 | **存钱罐多目标 + 完成确认**（`GoalStore`，含无缝迁移） |
| 3.75 | 99 | **自动备份**（选一次文件夹 + 周期 + 保留份数） |
| 3.76 | 100 | 修「选完备份文件夹后不生效」：回调要落在 SettingsActivity |
| 3.77 | 101 | 小人语录扩充 15 → 56 条（`PetQuotes`）+ 新增 6 类状态 |
| 3.78 | 102 | 费率栏跟着状态变（`RateDisplay`）：不赚的时候灰掉 |
| 3.79 | 103 | 修「调休出勤」被误显示成 1.5 倍（0 是有意义的 0） |
| 3.80 | 104 | 周末小人白天不再睡觉（只深夜冒 ZZZ）+ 文案允许两行 |
| 3.81 | 105 | 修两个会随日期爆炸的测试（发薪倒数硬编码天数） |
| 3.82 | 106 | 主卡加「本周一眼」（已在下版删除）|
| 3.83 | 107 | 删掉「本周一眼」+ 工作节奏移到「今日已赚」同一行 |
| 3.84 | 108 | 主卡收紧：删时钟（分隔线那步实际未生效）|
| 3.85 | 109 | 修底部空白：删分隔线 + addStat 容器改 wrap_content |
| 3.86 | 110 | 倒计时改「时:分:秒」（原来只到分钟，最后 59 秒不动）|
| 3.87/3.88 | 111/112 | 主卡 + 小组件改「本月累计」|
| 3.89 | 113 | 主卡「已结算 N 天」也改成本月（口径统一）|
| **3.90** | **114** | **修「昨天的工资没入账」：归档抽成 `DailyArchive` 并显式调用** |

> 完整逐条记录见 [CHANGELOG.md](CHANGELOG.md)（v2–v87）。

### 目录说明

```
salaryjump/
├── work-java/           ← 【改这里】Java 源码（当前使用）
├── work/                ← 【改这里】apktool 工程（资源/Manifest）
│   ├── res/
│   ├── AndroidManifest.xml
│   └── apktool.yml      ← 版本号在这里
├── tests/               ← 回归测试（stubs + AllTest + run.sh）
├── java-src/            原始 APK 的反编译源码（参考）
├── src/                 早期手写 smali（v1–v5，历史参考，勿改）
├── work_bak*/           早期 smali 工程快照
├── work-java-bak-v5~v7/ 各版本 Java 源码快照
├── data/                节假日原始数据（JSON / 压缩格式）
├── notes/               分析报告
├── mockup/              UI 对比模拟图
├── keystore/            签名凭据说明
├── CHANGELOG.md         详细改动记录
└── SalaryJump-*.apk     各版本交付存档
```

---

*最后更新：2026-10-09 · 对应版本 2.0*
