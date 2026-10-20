# 打工小人 · 完整行为矩阵

> v3.62 校对版。修改 `WorkerPet.resolvePose` / `reactionFor` 前请对照本表，确保无回归。
> 回归测试在 `tests/` 的 AO 组（姿势推导 17 项）与 AP 组（反应选择 + 连戳档位）。

## 一、优先级顺序（resolvePose 的判定链）

```
1. state 8（请假）      → LEAVE（全天，无视星期/时间）
2. state 7（休息日出勤） → 下班前 HOLIDAY_WORK ｜ 下班后 OFF_WORK
3. state 6（加班中）     → OVERTIME（优先于星期氛围！）
4. state 0（休息日）     → 周日晚（下班锚点后）SUNDAY ｜ 否则 WEEKEND
5. dow == 2（周一）     → MONDAY（**仅工作时间**：state 2/4）
6. dow == 6 && ≥13:00（周五）→ FRIDAY（**仅 state 4/5**）
7. 其他                  → 按时段姿势（COMMUTE/WORK_AM/LUNCH/WORK_PM/OFF_WORK）
```

**关键规则**：
- 加班中（6）**永远显示 OVERTIME** —— 不被周一/周五氛围覆盖（v3.55 修复）
- 休息日出勤（7）下班后显示 OFF_WORK（v3.54 修复）
- 「下班锚点」= 设置里的 `work_end`（未设置回退 17:00）——影响 state 7 与 SUNDAY

## 二、完整矩阵

### 加班中（state 6）
| 场景 | 显示 |
|---|---|
| 周三晚 | OVERTIME |
| 周一晚 | OVERTIME（v3.55 修复前是 MONDAY ✗）|
| 周五晚 | OVERTIME（v3.55 修复前是 FRIDAY ✗）|

### 休息日出勤（state 7）
| 场景 | 显示 |
|---|---|
| 任意日 下班前 | HOLIDAY_WORK（苦命出勤）|
| 任意日 下班后 | OFF_WORK（下班快乐）|

### 工作日各时段（周二/三/四）
| 场景 | 显示 |
|---|---|
| 上班前 | COMMUTE |
| 上午 | WORK_AM |
| 午休 | LUNCH |
| 下午 | WORK_PM |
| 下班后 | OFF_WORK |

### 周一（氛围日）
| 场景 | 显示 |
|---|---|
| 上午 / 下午 / 下班后 | MONDAY（"周一全天丧"是设计特色）|
| **加班中** | OVERTIME |

### 周五（氛围日）
| 场景 | 显示 |
|---|---|
| 上午 | WORK_AM |
| 下午（≥13:00）/ 下班后 | FRIDAY（欢呼）|
| **加班中** | OVERTIME |

### 休息日未出勤（state 0）
| 场景 | 显示 |
|---|---|
| 周六白天 / 周日白天 | WEEKEND（躺平）|
| 周日 ≥下班锚点 | SUNDAY（焦虑）|

### 请假（state 8）
| 场景 | 显示 |
|---|---|
| 任意日全天 | LEAVE（家里躺）|

## 三、已知的设计取舍（待用户确认可调整）

| 项 | 当前行为 | 备注 |
|---|---|---|
| 周一下班后 | 仍显示 MONDAY（面瘫）| "周一全天丧"设计特色；可改成 OFF_WORK |
| 周五下午 13:00 起 | 显示 FRIDAY（提前欢呼）| 13:00 硬编码；可按需调整 |
| 周日焦虑锚点 | 用下班时间（默认 17:00）| v3.55 统一为 work_end |

---

## 四、点击反应（v3.62 起：跟着状态走）

`WorkerPet.reactionFor(pose, fatigue)` —— 不再是纯随机。

| 当前姿势 | 反应 | 效果 |
|---|---|---|
| OVERTIME / WORK_AM / WORK_PM | `REACT_SURPRISED` | 双手举起 + `!` |
| LUNCH / WEEKEND / LEAVE / SUNDAY / POKE_ANNOYED | `REACT_SHY` | 左右摇 + 脸红 |
| OFF_WORK / FRIDAY / HOLIDAY_WORK | `REACT_CHEER` | 挥手 + 星光 |
| COMMUTE / MONDAY / 其它 | 随机三种之一 | — |
| **任意姿势 + 疲劳 ≥3** | `REACT_SURPRISED` | 蔫，不庆祝 |

## 五、连戳彩蛋（v3.62 新增）

`WorkerPet.pokeStage(count)` —— 1.2 秒内连续点击才计数，超时清零。

| 连戳次数 | 档位 | 反应 | 文案 |
|---|---|---|---|
| 1–2 | 0 | 普通反应 | `PetState.pokeText()` |
| 3–7 | 1 | `POKE_ANNOYED`（捂耳朵） | 「别戳了别戳了」 |
| ≥8 | 2 | `POKE_ANNOYED` | 「你有这功夫，不如去干活」 |

## 六、显示设置（v3.62 新增）

`PetSettings`（存 `salary_jump` 偏好）：

| KEY | 默认 | 说明 |
|---|---|---|
| `pet_visible` | `true` | 关掉后整列收起，金额居中 |
| `pet_size` | `1`（中 = 104dp） | 0/1/2 → 88 / 104 / 120 dp |
| `pet_scene` | `true` | 是否画书桌台灯 |

`MainActivity.applyPetSettings()` 在 `onResume()` 里应用。
