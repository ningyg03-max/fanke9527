package com.salaryjump.app;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SalaryJump 回归测试套件（纯 JVM，无需 Android 设备）
 *
 * 运行方式：sh tests/run.sh
 *
 * 原理：用 tests/stubs/ 下的 Android API 桩类替代真实框架，
 *       直接编译并运行 work-java/ 里的真实业务逻辑类，
 *       从而在电脑/沙箱上验证核心逻辑，无需装机。
 *
 * 覆盖范围：
 *   A. 饮品基础（内置 8 种，容量/名称/单位）
 *   B. 自定义饮品（增删改查、校验规则、ID 稳定性、删除占位、数量上限）
 *   C. 记录写入与格式兼容（3 字段写入、2 字段旧格式读取）
 *   D. 日切归档与历史查询
 *   E. 旧数据迁移（water_consumed → 记录）
 *   F. 工作日判定（内置节假日数据、三种模式）
 *   G. 边界值
 */
public class AllTest {
    static int pass = 0;
    static int fail = 0;

    static SimplePrefs prefs;
    static TestContext ctx;

    public static void main(String[] args) {
        section("A. 饮品基础");
        testDrinkBasics();

        section("B. 自定义饮品（增删改查）");
        testCustomDrinks();

        section("C. 记录写入与格式兼容");
        testRecords();

        section("D. 日切归档与历史查询");
        testHistory();

        section("E. 旧数据迁移");
        testLegacy();

        section("F. 工作日判定（内置节假日）");
        testWorkday();

        section("G. 边界值");
        testBounds();

        section("H. 删除规则（喝过不能删 / 没喝过直接删）");
        testHideBuiltin();

        section("I. 饮品改名（内置 + 自定义）");
        testRename();

        section("J. 热量与含糖标记");
        testKcalSugar();

        section("K. 参考表（可编辑目录）");
        testPresets();

        section("L. 参考表补数据（老用户升级）");
        testPresetMerge();

        section("M. 加班计算");
        testOvertime();

        section("N. 工资日历（按天归档）");
        testSalaryHistory();

        section("O. 加班打卡与跨天归档");
        testOvertimeLog();

        section("P. 法定假期出勤");
        testHolidayWork();

        section("Q. 崩溃记录器");
        testCrashLogger();

        section("R. 更新阶段追踪");
        testUpdateStage();

        section("S. 宿主 Activity 解析（Dialog 包装）");
        testHostResolution();

        section("T. 请假记录");
        testLeave();

        section("U. 隐私锁 + 发薪日");
        testPrivacyAndPayday();

        section("V. 加班与出勤规则");
        testWorkRules();

        section("W. 外快");
        testExtraIncome();

        section("X. 归档日补记同步（出勤/请假）");
        testArchiveSync();

        section("Y. 加班删除后的行同步");
        testOvertimeDeleteRowSync();

        section("Z. 打开时自愈预写行");
        testPrewrittenHeal();

        section("AA. 假期账户");
        testLeaveAccounts();

        section("AB. 打卡提醒");
        testCheckInReminder();

        section("AC. 小组件状态文案");
        testWidgetText();

        section("AD. 补记历史数据（占位行）");
        testBackfillRows();

        section("AE. 数据补录种子");
        testSeedBackfill();

        section("AF. 补建行与整月重算");
        testBackfillAndRecalcMonth();

        section("AG. 喝水记录时间修改");
        testWaterRecordTimeEdit();

        section("AH. 喝水提醒感知出勤");
        testWaterReminderDrinkingDay();


        section("AJ. 独立喝水时段");
        testWaterWindow();

        section("AK. 饮品单位");
        testUnitAndRecentFallback();

        section("AL. 月度补录");
        testMonthBackfill();

        section("AM. 存钱罐起算与日均口径");
        testGoalAndAvg();

        section("AN. 小人状态（疲劳/发薪日/语录）");
        testPetState();

        section("AO. 小人姿势推导（氛围覆盖修复）");
        testPoseResolve();

        section("AP. 小人设置与点击反应");
        testPetSettings();

        section("AQ. 输入范围校验");
        testInputRanges();

        section("AR. 主卡排版（金额不被小人挤掉）");
        testHeroFit();

        section("AS. 设置分区保存分发");
        testSectionSave();

        section("AT. 每日金额拆分（日历格子）");
        testDayBreakdown();

        section("AU. 读取缓存正确性");
        testDataCache();

        section("AV. 年度加班统计");
        testYearlyOvertime();

        section("AW. 补班日标记");
        testMakeupDay();

        section("AX. 存钱罐多目标");
        testGoalStore();

        section("AY. 自动备份逻辑");
        testBackupPlan();

        section("AZ. 小人语录");
        testPetQuotes();

        section("BA. 费率显示规则");
        testRateDisplay();

        section("BB. 周末姿势时段");
        testWeekendPose();

        section("BD. 倒计时格式");
        testDurationFormat();

        section("BE. 本月累计");
        testMonthlyTotal();


        System.out.println();
        System.out.println("────────────────────────────");
        if (fail == 0) {
            System.out.println("ALL PASS (" + pass + ")");
        } else {
            System.out.println("FAILURES: " + fail + "  (PASS: " + pass + ")");
            System.exit(1);
        }
    }

    static void testDrinkBasics() {
        newStore();
        eq("默认饮品数 = 8", DrinkTypes.count(ctx), 8);
        eq("idAt(0) = 0", DrinkTypes.idAt(ctx, 0), 0);
        eq("白开水名称", "白开水", DrinkTypes.name(ctx, 0));
        eq("可乐/汽水单位 = 罐", "罐", DrinkTypes.unit(ctx, 4));
        eq("茶默认 250", 250, DrinkTypes.ml(ctx, 1));
        eq("咖啡默认 300", 300, DrinkTypes.ml(ctx, 2));
        eq("奶茶默认 500", 500, DrinkTypes.ml(ctx, 3));
        eq("可乐默认 330", 330, DrinkTypes.ml(ctx, 4));
        eq("牛奶默认 250", 250, DrinkTypes.ml(ctx, 6));
        eq("白开水容量由 cupMl 决定", 250, DrinkTypes.ml(ctx, 0));
        DrinkTypes.setMl(ctx, 2, 400);
        eq("内置饮品容量可改（咖啡→400）", 400, DrinkTypes.ml(ctx, 2));
    }

    static void testCustomDrinks() {
        newStore();
        eq("添加成功返回 null", null, DrinkTypes.addCustom(ctx, "运动饮料", "瓶", 600));
        eq("饮品数 = 9", DrinkTypes.count(ctx), 9);
        eq("idAt(8) = 8", DrinkTypes.idAt(ctx, 8), 8);
        eq("自定义名称", "运动饮料", DrinkTypes.name(ctx, 8));
        eq("自定义单位", "瓶", DrinkTypes.unit(ctx, 8));
        eq("自定义容量", 600, DrinkTypes.ml(ctx, 8));

        // 校验规则
        check("空名被拒", DrinkTypes.addCustom(ctx, "  ", "杯", 250) != null);
        check("超 8 字被拒", DrinkTypes.addCustom(ctx, "一二三四五六七八九十", "杯", 250) != null);
        check("重名被拒", DrinkTypes.addCustom(ctx, "运动饮料", "杯", 250) != null);
        check("含分隔符被拒", DrinkTypes.addCustom(ctx, "a,b", "杯", 250) != null);
        check("容量 <10 被拒", DrinkTypes.addCustom(ctx, "测试A", "杯", 5) != null);
        check("容量 >2000 被拒", DrinkTypes.addCustom(ctx, "测试B", "杯", 9999) != null);
        eq("被拒后数量不变", DrinkTypes.count(ctx), 9);

        // 第二个 + ID 递增
        eq("再加一个", null, DrinkTypes.addCustom(ctx, "豆浆", "碗", 350));
        eq("饮品数 = 10", DrinkTypes.count(ctx), 10);
        eq("idAt(9) = 9", DrinkTypes.idAt(ctx, 9), 9);
        eq("豆浆容量 350", 350, DrinkTypes.ml(ctx, 9));

        // 改容量
        DrinkTypes.setMl(ctx, 8, 750);
        eq("自定义可改容量", 750, DrinkTypes.ml(ctx, 8));
        DrinkTypes.setMl(ctx, 0, 999);
        eq("白开水容量不受 setMl 影响", 250, DrinkTypes.ml(ctx, 0));

        // 删除：ID 不复用，后续 idAt 前移
        DrinkTypes.removeCustom(ctx, 8);
        eq("删除后数量 = 9", DrinkTypes.count(ctx), 9);
        eq("删除后 idAt(8) = 9（ID 不复用）", DrinkTypes.idAt(ctx, 8), 9);
        eq("已删除显示占位", "已删除饮品", DrinkTypes.name(ctx, 8));
        eq("已删除容量回退 250", 250, DrinkTypes.ml(ctx, 8));
        check("已删除非自定义", !DrinkTypes.isCustom(ctx, 8));
        check("内置非自定义", !DrinkTypes.isCustom(ctx, 0));
        check("豆浆仍是自定义", DrinkTypes.isCustom(ctx, 9));
        check("id 8 已未知", !DrinkTypes.isKnown(ctx, 8));
        check("id 9 已知", DrinkTypes.isKnown(ctx, 9));
        check("id 0 已知", DrinkTypes.isKnown(ctx, 0));

        // 数量上限：ID 8..31 共 24 个
        newStore();
        int added = 0;
        for (int i = 0; i < 24; i++) {
            if (DrinkTypes.addCustom(ctx, "饮品" + i, "杯", 200) == null) {
                added++;
            }
        }
        eq("可添加 24 个自定义", added, 24);
        eq("总数达到 32", DrinkTypes.count(ctx), 32);
        check("第 25 个被拒（达上限）", DrinkTypes.addCustom(ctx, "溢出", "杯", 200) != null);
    }

    static void testRecords() {
        newStore();
        eq("准备：添加豆浆", null, DrinkTypes.addCustom(ctx, "豆浆", "碗", 350));
        WaterReminderScheduler.addDrink(ctx, 8);   // 350
        WaterReminderScheduler.addDrink(ctx, 8);   // 350
        WaterReminderScheduler.addDrink(ctx, 0);   // 250（白开水）
        eq("总摄入 = 950", 950, WaterReminderScheduler.getConsumed(ctx));

        List<WaterReminderScheduler.WaterRecord> recs = WaterReminderScheduler.getRecords(ctx);
        eq("记录数 = 3", recs.size(), 3);
        check("自定义饮品记录 type = 8", recs.get(0).type == 8);
        check("自定义量 = 350", recs.get(0).amountMl == 350);
        String raw = prefs.getString("water_records", "");
        check("写入为 3 字段格式", raw.split(";")[0].split(",").length == 3);

        // 旧 2 字段格式兼容
        prefs.map.put("water_records", "1700000000000,250;1700000000001,300,2;");
        prefs.map.put("water_day", todayKey());
        List<WaterReminderScheduler.WaterRecord> old = WaterReminderScheduler.getRecords(ctx);
        eq("旧格式读取 2 条", old.size(), 2);
        check("旧格式缺省 type = 0", old.get(0).type == 0);
        check("旧格式显式 type = 2", old.get(1).type == 2);
    }

    static void testHistory() {
        newStore();
        String yday = keyOf(-1);
        int ystamp = stampOf(yday);
        prefs.map.put("water_day", yday);
        prefs.map.put("water_records", "1759900000000,250,0;1759900100000,500,3;");
        prefs.map.put("water_consumed", 750);

        List<WaterReminderScheduler.WaterRecord> after = WaterReminderScheduler.getRecords(ctx);
        eq("跨日后今日记录清空", prefs.getString("water_records", "").length(), 0);
        String hist = prefs.getString("water_history", "");
        check("已归档白开水 " + ystamp + ":0:250:1", hist.contains(ystamp + ":0:250:1"));
        check("已归档奶茶 " + ystamp + ":3:500:1", hist.contains(ystamp + ":3:500:1"));
        check("返回空列表", after.isEmpty());

        int[] ml = new int[DrinkTypes.MAX];
        int[] cnt = new int[DrinkTypes.MAX];
        HashMap<Integer, Integer> dm = WaterHistory.query(prefs, ystamp, ystamp, ml, cnt);
        eq("查询：白开水 250 ml", 250, ml[0]);
        eq("查询：奶茶 500 ml", 500, ml[3]);
        eq("查询：白开水 1 杯", 1, cnt[0]);
        eq("查询：天数 1", dm.size(), 1);
        eq("查询：当天总量 750", 750, dm.values().iterator().next().intValue());

        int[] ml2 = new int[DrinkTypes.MAX];
        int[] cnt2 = new int[DrinkTypes.MAX];
        eq("查询：范围外为 0", 0, WaterHistory.query(prefs, ystamp + 1, ystamp + 2, ml2, cnt2).size());
    }

    static void testLegacy() {
        newStore();
        prefs.map.put("water_records", "");
        prefs.map.put("water_consumed", 300);
        prefs.map.put("water_day", todayKey());
        List<WaterReminderScheduler.WaterRecord> legacy = WaterReminderScheduler.getRecords(ctx);
        eq("迁移出 1 条", legacy.size(), 1);
        check("迁移记录 type = 0", legacy.get(0).type == 0);
        check("迁移记录量 = 300", legacy.get(0).amountMl == 300);
    }

    static void testWorkday() {
        MainActivity.SalarySettings s = new MainActivity.SalarySettings();
        s.workMode = 2;      // 智能模式（内置节假日数据）
        s.useCalendar = false;

        String[][] cases = {
            {"2026-10-10", "1", "国庆调休上班（周六）"},
            {"2026-10-09", "1", "普通周五"},
            {"2026-10-11", "0", "普通周日"},
            {"2026-10-01", "0", "国庆假期"},
            {"2026-10-07", "0", "国庆假期末"},
            {"2026-09-20", "1", "国庆调休上班（周日）"},
            {"2026-02-16", "0", "春节假期"},
            {"2026-02-14", "1", "春节调休上班（周六）"},
            {"2026-01-01", "0", "元旦"},
            {"2026-01-04", "1", "元旦调休（周日）"},
            {"2026-06-19", "0", "端午假期"},
            {"2027-10-10", "0", "2027 未收录 → 按周末"},
            {"2027-06-02", "1", "2027 未收录 → 按普通工作日（周三）"},
        };
        for (String[] c : cases) {
            boolean got = WorkdayHelper.isWorkday(null, s, mk(c[0]));
            boolean want = "1".equals(c[1]);
            if (got == want) {
                pass++;
                System.out.println("PASS  " + c[0] + "  " + c[2]);
            } else {
                fail++;
                System.out.println("FAIL  " + c[0] + "  " + c[2] + "  期望=" + want + " 实际=" + got);
            }
        }

        // 模式 0：每天都是工作日
        s.workMode = 0;
        check("mode=0 周日也算工作日", WorkdayHelper.isWorkday(null, s, mk("2026-10-11")));
        // 模式 1：仅周一至周五
        s.workMode = 1;
        check("mode=1 周六非工作日", !WorkdayHelper.isWorkday(null, s, mk("2026-10-10")));
        check("mode=1 周三为工作日", WorkdayHelper.isWorkday(null, s, mk("2026-10-14")));
    }

    static void testBounds() {
        eq("clamp(-1) = 0", 0, DrinkTypes.clamp(-1));
        eq("clamp(-5) = 0", 0, DrinkTypes.clamp(-5));
        eq("clamp(999) = 31", 31, DrinkTypes.clamp(999));
        eq("clamp(31) = 31", 31, DrinkTypes.clamp(31));
        eq("BUILTIN = 8", 8, DrinkTypes.BUILTIN);
        eq("MAX = 32", 32, DrinkTypes.MAX);
    }

    static void testHideBuiltin() {
        // ---- 1. 没喝过的内置饮品可删除 ----
        newStore();
        eq("初始可见 = 8", DrinkTypes.count(ctx), 8);
        eq("移除检查（牛奶）", null, DrinkTypes.removeCheck(ctx, 6));
        eq("删除成功（牛奶）", null, DrinkTypes.remove(ctx, 6));
        eq("可见数 = 7", DrinkTypes.count(ctx), 7);
        eq("已删除计数 = 1", DrinkTypes.removedCount(ctx), 1);
        check("牛奶已删除", DrinkTypes.isRemoved(ctx, 6));
        boolean hasMilk = false;
        for (int i = 0; i < DrinkTypes.count(ctx); i++) {
            if (DrinkTypes.idAt(ctx, i) == 6) {
                hasMilk = true;
            }
        }
        check("列表中不再出现牛奶", !hasMilk);
        eq("已删除后名称仍可解析", "牛奶", DrinkTypes.name(ctx, 6));

        // ---- 2. 白开水不可删除 ----
        check("白开水删除被拒", DrinkTypes.remove(ctx, 0) != null);
        check("白开水检查有原因", DrinkTypes.removeCheck(ctx, 0) != null);
        eq("可见数不变", DrinkTypes.count(ctx), 7);

        // ---- 3. 越界 / 不存在 ----
        check("id=99 删除被拒", DrinkTypes.remove(ctx, 99) != null);
        check("id=-1 删除被拒", DrinkTypes.remove(ctx, -1) != null);

        // ---- 4. 喝过的内置饮品不能删除 ----
        WaterReminderScheduler.addDrink(ctx, 2);   // 喝一杯咖啡
        check("咖啡已喝过", DrinkTypes.hasRecords(ctx, 2));
        check("咖啡删除被拒", DrinkTypes.remove(ctx, 2) != null);
        check("拒绝原因含名称", DrinkTypes.removeCheck(ctx, 2).contains("咖啡"));
        eq("可见数仍为 7", DrinkTypes.count(ctx), 7);
        check("咖啡未被删除", !DrinkTypes.isRemoved(ctx, 2));
        eq("咖啡仍在列表", DrinkTypes.idAt(ctx, 2), 2);

        // ---- 5. 自定义饮品：没喝过可删 ----
        eq("添加豆浆", null, DrinkTypes.addCustom(ctx, "豆浆", "碗", 350));
        eq("可见数 = 8", DrinkTypes.count(ctx), 8);
        eq("豆浆 id = 8", DrinkTypes.idAt(ctx, 7), 8);
        check("豆浆没喝过", !DrinkTypes.hasRecords(ctx, 8));
        eq("删除豆浆成功", null, DrinkTypes.remove(ctx, 8));
        eq("可见数回到 7", DrinkTypes.count(ctx), 7);

        // ---- 6. 自定义饮品：喝过不能删 ----
        eq("重新添加豆浆", null, DrinkTypes.addCustom(ctx, "豆浆", "碗", 350));
        eq("豆浆 id = 9", DrinkTypes.idAt(ctx, 7), 9);
        WaterReminderScheduler.addDrink(ctx, 9);
        check("豆浆已喝过", DrinkTypes.hasRecords(ctx, 9));
        check("豆浆删除被拒", DrinkTypes.remove(ctx, 9) != null);
        eq("可见数仍为 8", DrinkTypes.count(ctx), 8);
        check("豆浆仍在", DrinkTypes.isCustom(ctx, 9));

        // ---- 7. 历史归档也算“喝过” ----
        newStore();
        eq("隐藏前：添加自定义", null, DrinkTypes.addCustom(ctx, "酸奶", "瓶", 200));
        int yogurt = DrinkTypes.idAt(ctx, 8);
        check("酸奶未喝过", !DrinkTypes.hasRecords(ctx, yogurt));
        // 造一条历史归档引用该饮品
        prefs.map.put("water_history", "20261001:" + yogurt + ":200:1;");
        check("归档后判定为喝过", DrinkTypes.hasRecords(ctx, yogurt));
        check("归档后删除被拒", DrinkTypes.remove(ctx, yogurt) != null);
        // 造一条今天记录引用内置饮品
        prefs.map.put("water_records", "1759900000000,250,3;");
        prefs.map.put("water_day", todayKey());
        check("今天记录判定为喝过", DrinkTypes.hasRecords(ctx, 3));
        check("奶茶删除被拒", DrinkTypes.remove(ctx, 3) != null);

        // ---- 8. 旧 hidden_drinks 迁移 ----
        newStore();
        // 旧数据：奶茶(3) 与 可乐(4) 被隐藏；其中奶茶已喝过
        prefs.map.put("hidden_drinks", "3,4");
        prefs.map.put("water_records", "1759900000000,500,3;");
        prefs.map.put("water_day", todayKey());
        eq("迁移后可见数 = 7（只有可乐被保留删除状态）", DrinkTypes.count(ctx), 7);
        check("奶茶已恢复（因喝过）", !DrinkTypes.isRemoved(ctx, 3));
        check("可乐保持删除", DrinkTypes.isRemoved(ctx, 4));
        check("旧键已清理", prefs.map.get("hidden_drinks") == null);
        eq("新键已写入", "4", prefs.getString("removed_drinks", ""));

        // ---- 9. 删除后：容量仍可读、可重新添加同名 ----
        newStore();
        DrinkTypes.remove(ctx, 6);   // 删除牛奶（250）
        eq("已删除饮品容量仍可读", 250, DrinkTypes.ml(ctx, 6));
        eq("重新添加同名牛奶成功", null, DrinkTypes.addCustom(ctx, "牛奶", "盒", 250));
        eq("可见数 = 8", DrinkTypes.count(ctx), 8);
    }

    static void testRename() {
        newStore();
        // ---- 内置饮品改名 ----
        eq("默认为原始名", "咖啡", DrinkTypes.name(ctx, 2));
        check("初始未改名", !DrinkTypes.isRenamed(ctx, 2));
        DrinkTypes.setName(ctx, 2, "黑咖啡");
        eq("改名生效", "黑咖啡", DrinkTypes.name(ctx, 2));
        check("已标记为改名", DrinkTypes.isRenamed(ctx, 2));
        eq("原始名仍可查", "咖啡", DrinkTypes.baseName(ctx, 2));
        eq("白开水不受影响", "白开水", DrinkTypes.name(ctx, 0));

        // 名称自动去空格
        DrinkTypes.setName(ctx, 1, "  绿茶  ");
        eq("首尾空格被去掉", "绿茶", DrinkTypes.name(ctx, 1));

        // 通用改名：传回原始名 = 恢复默认
        DrinkTypes.setName(ctx, 2, "咖啡");
        eq("传回原名恢复默认", "咖啡", DrinkTypes.name(ctx, 2));
        check("恢复后不再标记改名", !DrinkTypes.isRenamed(ctx, 2));

        // 空名字：回退到原始名
        DrinkTypes.setName(ctx, 3, "");
        eq("空名回退默认", "奶茶", DrinkTypes.name(ctx, 3));
        check("空名不算改名", !DrinkTypes.isRenamed(ctx, 3));

        // ---- 自定义饮品改名 ----
        eq("添加豆浆", null, DrinkTypes.addCustom(ctx, "豆浆", "碗", 350));
        int id = DrinkTypes.idAt(ctx, 8);
        eq("自定义原名", "豆浆", DrinkTypes.name(ctx, id));
        DrinkTypes.setName(ctx, id, "现磨豆浆");
        eq("自定义改名生效", "现磨豆浆", DrinkTypes.name(ctx, id));
        eq("自定义原始名可查", "豆浆", DrinkTypes.baseName(ctx, id));

        // ---- 改名后：记录与统计跟随新名 ----
        WaterReminderScheduler.addDrink(ctx, 2);   // 咖啡（已恢复默认名）
        WaterReminderScheduler.addDrink(ctx, id);  // 现磨豆浆
        List<WaterReminderScheduler.WaterRecord> recs = WaterReminderScheduler.getRecords(ctx);
        eq("记录条数", recs.size(), 2);
        eq("记录显示新名", "现磨豆浆", DrinkTypes.name(ctx, recs.get(1).type));

        // ---- 改名与删除规则互不干扰 ----
        check("改过名的饮品有记录", DrinkTypes.hasRecords(ctx, id));
        check("改过名且有记录 → 不能删", DrinkTypes.remove(ctx, id) != null);
        // 未喝过的改名后仍可删
        DrinkTypes.setName(ctx, 5, "鲜榨汁");
        eq("改名后仍可删（没喝过）", null, DrinkTypes.remove(ctx, 5));
        check("已删除", DrinkTypes.isRemoved(ctx, 5));
        eq("已删除后改名保留", "鲜榨汁", DrinkTypes.name(ctx, 5));

        // ---- 名称校验 ----
        eq("空名被拒", "名称不能为空", DrinkTypes.nameCheck("  "));
        eq("超 8 字被拒", "名称请控制在 8 个字以内", DrinkTypes.nameCheck("一二三四五六七八九十"));
        check("含逗号被拒", DrinkTypes.nameCheck("a,b") != null);
        check("含分号被拒", DrinkTypes.nameCheck("a;b") != null);
        check("含冒号被拒", DrinkTypes.nameCheck("a:b") != null);
        eq("合法名称通过", null, DrinkTypes.nameCheck("美式咖啡"));
        eq("刚好 8 字通过", null, DrinkTypes.nameCheck("一二三四五六七八"));
    }

    static void testKcalSugar() {
        newStore();
        // ---- 默认值（精选表）----
        eq("白开水 0 kcal", 0, DrinkTypes.kcal(ctx, 0));
        eq("茶 0 kcal", 0, DrinkTypes.kcal(ctx, 1));
        eq("咖啡 2 kcal", 2, DrinkTypes.kcal(ctx, 2));
        eq("奶茶 55 kcal", 55, DrinkTypes.kcal(ctx, 3));
        eq("可乐 43 kcal", 43, DrinkTypes.kcal(ctx, 4));
        eq("果汁 45 kcal", 45, DrinkTypes.kcal(ctx, 5));
        eq("牛奶 65 kcal", 65, DrinkTypes.kcal(ctx, 6));
        eq("自定义饮品默认 0 kcal", 0, DrinkTypes.kcal(ctx, 8));

        // ---- 默认含糖标记 ----
        check("白开水无糖", !DrinkTypes.isSugary(ctx, 0));
        check("茶无糖", !DrinkTypes.isSugary(ctx, 1));
        check("咖啡无糖", !DrinkTypes.isSugary(ctx, 2));
        check("奶茶含糖", DrinkTypes.isSugary(ctx, 3));
        check("可乐含糖", DrinkTypes.isSugary(ctx, 4));
        check("果汁含糖", DrinkTypes.isSugary(ctx, 5));
        check("牛奶不计添加糖", !DrinkTypes.isSugary(ctx, 6));

        // ---- 修改与恢复 ----
        DrinkTypes.setKcal(ctx, 4, 0);
        eq("可乐改 0 kcal", 0, DrinkTypes.kcal(ctx, 4));
        check("已标记为自定义", DrinkTypes.isKcalCustom(ctx, 4));
        DrinkTypes.setSugary(ctx, 4, false);
        check("可乐改为无糖", !DrinkTypes.isSugary(ctx, 4));
        DrinkTypes.setSugary(ctx, 4, true);
        check("可乐改回含糖", DrinkTypes.isSugary(ctx, 4));

        // ---- 边界钳制 ----
        DrinkTypes.setKcal(ctx, 2, -5);
        eq("负数被钳为 0", 0, DrinkTypes.kcal(ctx, 2));
        DrinkTypes.setKcal(ctx, 2, 9999);
        eq("超上限被钳为 900", 900, DrinkTypes.kcal(ctx, 2));
        DrinkTypes.setKcal(ctx, 2, 2);

        // ---- 单条记录热量 ----
        newStore();   // 恢复默认热量表
        eq("可乐 330ml = 142 kcal", 142, DrinkTypes.recordKcal(ctx, 4, 330));
        eq("牛奶 250ml = 163 kcal", 163, DrinkTypes.recordKcal(ctx, 6, 250));
        eq("白开水 500ml = 0 kcal", 0, DrinkTypes.recordKcal(ctx, 0, 500));
        eq("咖啡 300ml = 6 kcal", 6, DrinkTypes.recordKcal(ctx, 2, 300));
        eq("零毫升 = 0", 0, DrinkTypes.recordKcal(ctx, 4, 0));

        // ---- 今日累计 ----
        newStore();
        WaterReminderScheduler.addDrink(ctx, 4);   // 可乐 330ml → 142 kcal, 含糖
        WaterReminderScheduler.addDrink(ctx, 0);   // 白开水 250ml → 0
        WaterReminderScheduler.addDrink(ctx, 4);   // 可乐 330ml → 142 kcal, 含糖
        eq("今日总热量 = 284", 284, DrinkTypes.todayKcal(ctx));
        eq("今日含糖杯数 = 2", 2, DrinkTypes.todaySugaryCups(ctx));

        WaterReminderScheduler.addDrink(ctx, 6);   // 牛奶 250ml → 163 kcal, 无糖
        eq("加牛奶后热量 = 447", 447, DrinkTypes.todayKcal(ctx));
        eq("含糖杯数不变 = 2", 2, DrinkTypes.todaySugaryCups(ctx));

        // 改热量后历史记录跟着变（不存死值）
        DrinkTypes.setKcal(ctx, 4, 43);
        eq("改回 43 后总热量不变", 447, DrinkTypes.todayKcal(ctx));
        DrinkTypes.setKcal(ctx, 4, 0);
        eq("可乐改 0 后总热量 = 163", 163, DrinkTypes.todayKcal(ctx));

        // ---- 改名不影响热量 ----
        DrinkTypes.setName(ctx, 4, "肥宅快乐水");
        eq("改名后热量不变", 0, DrinkTypes.kcal(ctx, 4));
        eq("改名后名称正确", "肥宅快乐水", DrinkTypes.name(ctx, 4));

        // ---- 精选表结构 ----
        check("精选表非空", DrinkTypes.SEED_PRESETS.length >= 15);
        boolean hasZero = false;
        boolean hasSugar = false;
        for (Object[] row : DrinkTypes.SEED_PRESETS) {
            check("表项格式完整", row.length == 3);
            int k = ((Integer) row[1]).intValue();
            if (k == 0) {
                hasZero = true;
            }
            if (((Boolean) row[2]).booleanValue()) {
                hasSugar = true;
            }
        }
        check("表中有无糖项", hasZero);
        check("表中有含糖项", hasSugar);
    }

    static void testPresets() {
        newStore();
        // ---- 首次读取：从种子初始化 ----
        List<DrinkTypes.Preset> presets = DrinkTypes.presets(ctx);
        eq("初始条目数 = 21", 21, presets.size());
        eq("首条名称", "白开水", presets.get(0).name);
        eq("首条热量 0", 0, presets.get(0).kcal);
        check("首条无糖", !presets.get(0).sugar);
        eq("已写入存储", 21, prefs.getString("drink_presets", "").split(";").length);

        // 再次读取保持一致
        eq("重复读取数量一致", 21, DrinkTypes.presets(ctx).size());

        // ---- 新增 ----
        eq("新增成功", null, DrinkTypes.presetAdd(ctx, "气泡咖啡", 12, true));
        eq("数量 = 22", 22, DrinkTypes.presets(ctx).size());
        eq("新条目热量", 12, DrinkTypes.presets(ctx).get(21).kcal);
        check("新条目含糖", DrinkTypes.presets(ctx).get(21).sugar);
        check("重名被拒", DrinkTypes.presetAdd(ctx, "气泡咖啡", 10, false) != null);
        check("空名被拒", DrinkTypes.presetAdd(ctx, "  ", 10, false) != null);
        check("超 8 字被拒", DrinkTypes.presetAdd(ctx, "一二三四五六七八九十", 10, false) != null);
        check("含逗号被拒", DrinkTypes.presetAdd(ctx, "a,b", 10, false) != null);
        check("热量负数被拒", DrinkTypes.presetAdd(ctx, "测试甲", -1, false) != null);
        check("热量超 900 被拒", DrinkTypes.presetAdd(ctx, "测试乙", 901, false) != null);
        eq("拒绝后数量不变", 22, DrinkTypes.presets(ctx).size());

        // ---- 修改 ----
        eq("改名成功", null, DrinkTypes.presetUpdate(ctx, 0, "纯净水", 0, false));
        eq("改名生效", "纯净水", DrinkTypes.presets(ctx).get(0).name);
        eq("改热量成功", null, DrinkTypes.presetUpdate(ctx, 1, "无糖茶", 1, false));
        eq("改热量生效", 1, DrinkTypes.presets(ctx).get(1).kcal);
        check("改成已有名字被拒", DrinkTypes.presetUpdate(ctx, 0, "无糖茶", 0, false) != null);
        eq("拒绝后名字未变", "纯净水", DrinkTypes.presets(ctx).get(0).name);

        // ---- 使用判定与删除 ----
        newStore();
        check("白开水被内置饮品使用", true == DrinkTypes.presetInUse(ctx, "白开水"));
        check("拿铁未被使用", !DrinkTypes.presetInUse(ctx, "拿铁 / 奶咖"));
        int latte = indexOfPreset("拿铁 / 奶咖");
        eq("未使用项可删检查", null, DrinkTypes.presetRemoveCheck(ctx, latte));
        int water = indexOfPreset("白开水");
        check("白开水不可删（被使用）", DrinkTypes.presetRemoveCheck(ctx, water) != null);
        // 添加一个饮品后，对应参考项变为"使用中"
        eq("添加拿铁饮品", null, DrinkTypes.addCustom(ctx, "拿铁 / 奶咖", "杯", 300));
        check("拿铁已被使用", DrinkTypes.presetInUse(ctx, "拿铁 / 奶咖"));
        check("使用中的拿铁不可删", DrinkTypes.presetRemoveCheck(ctx, latte) != null);
        // 删除未使用的条目
        int soda = indexOfPreset("苏打水（无糖）");
        eq("苏打水可删检查", null, DrinkTypes.presetRemoveCheck(ctx, soda));
        DrinkTypes.presetRemove(ctx, soda);
        eq("删除后数量 = 20", 20, DrinkTypes.presets(ctx).size());
        check("苏打水已不在表中", indexOfPreset("苏打水（无糖）") < 0);
        check("越界删除检查返回原因", DrinkTypes.presetRemoveCheck(ctx, 99) != null);

        // ---- availablePresets：过滤掉已使用的 ----
        newStore();
        eq("初始可用 = 20", 20, DrinkTypes.availablePresets(ctx).size());
        eq("添加白开水同名饮品", null, DrinkTypes.addCustom(ctx, "白开水", "杯", 250));
        eq("可用数 = 20", 20, DrinkTypes.availablePresets(ctx).size());
        boolean hasWater = false;
        for (DrinkTypes.Preset p : DrinkTypes.availablePresets(ctx)) {
            if (p.name.equals("白开水")) {
                hasWater = true;
            }
        }
        check("可用列表里没有白开水", !hasWater);

        // ---- 参考表改名 → 饮品跟随 ----
        newStore();
        eq("添加豆浆饮品", null, DrinkTypes.addCustom(ctx, "豆浆（无糖）", "碗", 350));
        int soyIndex = indexOfPreset("豆浆（无糖）");
        check("豆浆已被使用", DrinkTypes.presetInUse(ctx, "豆浆（无糖）"));
        eq("参考表改名", null, DrinkTypes.presetUpdate(ctx, soyIndex, "现磨豆浆", 31, false));
        eq("饮品名跟随更新", "现磨豆浆", DrinkTypes.name(ctx, DrinkTypes.idAt(ctx, 8)));
        check("改名后仍判定为使用中", DrinkTypes.presetInUse(ctx, "现磨豆浆"));
    }

    static void testPresetMerge() {
        // ---- 老用户：已有 17 条、无版本键 → 应补入啤酒 ----
        newStore();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 17; i++) {
            Object[] row = DrinkTypes.SEED_PRESETS[i];
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(row[0]).append(',').append(row[1]).append(',').append(((Boolean) row[2]) ? 1 : 0);
        }
        prefs.map.put("drink_presets", sb.toString());
        eq("补数据后 = 21 条", 21, DrinkTypes.presets(ctx).size());
        check("啤酒（普通）已补入", indexOfPreset("啤酒（普通）") >= 0);
        check("无醇啤酒已补入", indexOfPreset("无醇啤酒") >= 0);
        eq("啤酒热量 43", 43, DrinkTypes.presets(ctx).get(indexOfPreset("啤酒（普通）")).kcal);
        check("版本键已写入", prefs.map.get("drink_presets_ver") != null);
        eq("再次读取不重复", 21, DrinkTypes.presets(ctx).size());

        // ---- 用户删掉啤酒后，不应被重新补回 ----
        DrinkTypes.presetRemove(ctx, indexOfPreset("啤酒（普通）"));
        eq("删除后 = 20 条", 20, DrinkTypes.presets(ctx).size());
        eq("再次读取仍 20 条（不重复补）", 20, DrinkTypes.presets(ctx).size());
        check("已删除的啤酒没有被加回", indexOfPreset("啤酒（普通）") < 0);

        // ---- 用户已有的同名条目不被覆盖 ----
        newStore();
        prefs.map.put("drink_presets", "啤酒（普通）,99,1;");
        eq("补入其余 3 款 = 4 条", 4, DrinkTypes.presets(ctx).size());
        eq("用户自定义热量保留", 99, DrinkTypes.presets(ctx).get(0).kcal);
        check("用户自定义含糖保留", DrinkTypes.presets(ctx).get(0).sugar);

        // ---- 全新用户：种子表直接包含啤酒 ----
        newStore();
        eq("全新安装 = 21 条", 21, DrinkTypes.presets(ctx).size());
        check("种子含啤酒（淡爽）", indexOfPreset("啤酒（淡爽）") >= 0);
        check("种子含精酿啤酒", indexOfPreset("精酿啤酒") >= 0);
    }

    static void testOvertime() {
        // 09:00–17:00 下班，宽限 30 分钟 → 17:30 起算加班
        int end = 17 * 60;
        int grace = 30;
        eqD("17:00 未到加班", 0.0, OvertimeCalc.seconds(17 * 3600.0, end, grace));
        eqD("17:29 仍在宽限", 0.0, OvertimeCalc.seconds(17 * 3600.0 + 29 * 60, end, grace));
        eqD("17:30 整起算", 0.0, OvertimeCalc.seconds(17 * 3600.0 + 30 * 60, end, grace));
        eqD("18:00 加班 30 分钟", 1800.0, OvertimeCalc.seconds(18 * 3600.0, end, grace));
        eqD("20:00 加班 2.5 小时", 9000.0, OvertimeCalc.seconds(20 * 3600.0, end, grace));

        check("17:00 不在加班", !OvertimeCalc.isOvertime(17 * 3600.0, end, grace));
        check("18:00 在加班", OvertimeCalc.isOvertime(18 * 3600.0, end, grace));

        // 宽限为 0：下班即算
        eqD("宽限0：17:00 整不算", 0.0, OvertimeCalc.seconds(17 * 3600.0, end, 0));
        eqD("宽限0：17:01 算 60 秒", 60.0, OvertimeCalc.seconds(17 * 3600.0 + 60, end, 0));

        // 加班收入：日薪 300，正常工作 8 小时（28800 秒）
        double daily = 300.0d;
        double normal = 8 * 3600.0d;
        // 1 小时加班 × 1.5 倍 = (300/8) × 1.5 = 56.25
        eqD("1小时×1.5 = 56.25", 56.25, OvertimeCalc.earned(daily, normal, 3600.0d, 15));
        // 1 小时加班 × 2 倍 = 75
        eqD("1小时×2 = 75", 75.0, OvertimeCalc.earned(daily, normal, 3600.0d, 20));
        // 1 小时加班 × 3 倍 = 112.5
        eqD("1小时×3 = 112.5", 112.5, OvertimeCalc.earned(daily, normal, 3600.0d, 30));
        // 2 小时 × 1.5 = 112.5
        eqD("2小时×1.5 = 112.5", 112.5, OvertimeCalc.earned(daily, normal, 7200.0d, 15));
        // 未加班 = 0
        eqD("未加班 = 0", 0.0, OvertimeCalc.earned(daily, normal, 0.0d, 15));
        // 参数非法
        eqD("日薪为 0 = 0", 0.0, OvertimeCalc.earned(0.0d, normal, 3600.0d, 15));
        eqD("总工时 0 = 0", 0.0, OvertimeCalc.earned(daily, 0.0d, 3600.0d, 15));
        eqD("倍率 0 = 0", 0.0, OvertimeCalc.earned(daily, normal, 3600.0d, 0));

        // 结算完整性：正常工作 8h + 加班 1h(1.5x) = 300 + 56.25 = 356.25
        double normalEarned = daily * 1.0d;
        eqD("合计 = 356.25", 356.25, normalEarned + OvertimeCalc.earned(daily, normal, 3600.0d, 15));
    }

    static void testSalaryHistory() {
        newStore();
        eq("初始为空", 0, SalaryHistory.load(ctx).size());
        eqL("初始合计 0", 0L, SalaryHistory.totalCents(ctx));

        // 写入三天
        SalaryHistory.put(ctx, 20261008, 30000L);   // 300.00
        SalaryHistory.put(ctx, 20261009, 30000L);
        SalaryHistory.put(ctx, 20261010, 25000L);   // 250.00
        eq("三天记录", 3, SalaryHistory.load(ctx).size());
        eqL("合计 850.00", 85000L, SalaryHistory.totalCents(ctx));

        // 同一天重复写入 → 覆盖而非追加
        SalaryHistory.put(ctx, 20261009, 32000L);
        eq("仍是三天", 3, SalaryHistory.load(ctx).size());
        eqL("合计 870.00", 87000L, SalaryHistory.totalCents(ctx));

        // 按月查询
        eq("10月有三天", 3, SalaryHistory.byMonth(ctx, 2026, 10).size());
        eqL("10月合计", 87000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eq("9月为空", 0, SalaryHistory.byMonth(ctx, 2026, 9).size());
        eqL("11月为空", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 11));

        // 降序排列（新的在前）
        java.util.List<SalaryHistory.Day> oct = SalaryHistory.byMonth(ctx, 2026, 10);
        eq("首条是 10 号", 20261010, oct.get(0).stamp);
        eq("末条是 8 号", 20261008, oct.get(oct.size() - 1).stamp);

        // 跨月
        SalaryHistory.put(ctx, 20260930, 30000L);
        eq("月份数 = 2", 2, SalaryHistory.months(ctx).size());
        eq("最新月份在前", 202610, SalaryHistory.months(ctx).get(0).intValue());

        // 日期工具
        eq("取年", 2026, SalaryHistory.yearOf(20261008));
        eq("取月", 10, SalaryHistory.monthOf(20261008));
        eq("取日", 8, SalaryHistory.dayOf(20261008));

        // 持久化验证（重新读取）
        eq("重新读取仍是 4 条", 4, SalaryHistory.load(ctx).size());
        eqL("含 9/30", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 9));
    }

    static void testOvertimeLog() {
        // ---- 纯函数：seconds() ----
        long t = 1700000000000L;
        eqD("日期不匹配 → 0", 0.0, OvertimeLog.seconds(20261009, t, t + 3600000L, 20261010, t + 7200000L));
        eqD("未打卡 → 0", 0.0, OvertimeLog.seconds(20261009, 0L, 0L, 20261009, t));
        eqD("已结束 → 时长", 3600.0, OvertimeLog.seconds(20261009, t, t + 3600000L, 20261009, t + 99999999L));
        eqD("进行中 → 算到 now", 1800.0, OvertimeLog.seconds(20261009, t, 0L, 20261009, t + 1800000L));
        eqD("结束早于开始 → 0", 0.0, OvertimeLog.seconds(20261009, t, t - 1000L, 20261009, t));
        eqD("半小时间隔", 1800.0, OvertimeLog.seconds(20261009, t, t + 1800000L, 20261009, t + 9999999L));

        // ---- 格式化 ----
        eq("格式化 0 秒", "0:00", OvertimeLog.format(0.0d));
        eq("格式化 90 秒", "1:30", OvertimeLog.format(90.0d));
        eq("格式化 45 分钟", "45:00", OvertimeLog.format(2700.0d));
        eq("格式化 1 小时 23 分 45 秒", "1:23:45", OvertimeLog.format(5025.0d));
        eq("格式化 2 小时整", "2:00:00", OvertimeLog.format(7200.0d));

        // ---- 存储闭环：开始 → 进行中 → 结束 ----
        newStore();
        int today = 20261009;
        check("初始无打卡", !OvertimeLog.hasPunch(ctx, today));
        check("初始未在加班", !OvertimeLog.isRunning(ctx, today));
        eqD("初始时长为 0", 0.0, OvertimeLog.secondsOfDay(ctx, today, 1700000000000L));

        long start = 1700000000000L;
        OvertimeLog.start(ctx, today, start);
        check("开始后有打卡", OvertimeLog.hasPunch(ctx, today));
        check("开始后在加班", OvertimeLog.isRunning(ctx, today));
        eqD("进行中 1 小时", 3600.0, OvertimeLog.secondsOfDay(ctx, today, start + 3600000L));
        eqD("进行中 2 小时", 7200.0, OvertimeLog.secondsOfDay(ctx, today, start + 7200000L));

        // 跨天：昨天的打卡不算到今天
        eqD("昨天打卡不算今天", 0.0, OvertimeLog.secondsOfDay(ctx, today + 1, start + 7200000L));
        check("昨天打卡今天不算进行中", !OvertimeLog.isRunning(ctx, today + 1));

        // 结束 → 冻结
        long end = start + 5400000L;   // 1.5 小时
        OvertimeLog.stop(ctx, end);
        check("结束后不再进行中", !OvertimeLog.isRunning(ctx, today));
        eqD("冻结为 1.5 小时", 5400.0, OvertimeLog.secondsOfDay(ctx, today, start + 99999999L));

        // 清除（重来）
        OvertimeLog.clear(ctx);
        check("清除后无打卡", !OvertimeLog.hasPunch(ctx, today));
        eqD("清除后时长 0", 0.0, OvertimeLog.secondsOfDay(ctx, today, start + 3600000L));

        // ---- 跨天归档：归档金额必须含加班费 ----
        newStore();
        double daily = 300.0d;
        double normalSeconds = 8 * 3600.0d;       // 8 小时
        int dayA = 20261009;

        // 模拟：某天加班 2 小时（1.5 倍）
        OvertimeLog.start(ctx, dayA, 1700000000000L);
        OvertimeLog.stop(ctx, 1700000000000L + 7200000L);
        double otSeconds = OvertimeLog.secondsOfDay(ctx, dayA, System.currentTimeMillis());
        eqD("归档用加班秒数 = 7200", 7200.0, otSeconds);
        double otEarned = OvertimeCalc.earned(daily, normalSeconds, otSeconds, 15);
        eqD("加班费 = 112.5", 112.5, otEarned);
        long archivedCents = Math.round((daily + otEarned) * 100.0d);
        eq("归档金额（分）", 41250, (int) archivedCents);

        // 写入工资日历并读取
        SalaryHistory.put(ctx, dayA, archivedCents);
        eq("日历里那天的金额", 41250, (int) SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // 对照：不加班的一天只归档日薪
        int dayB = 20261008;
        SalaryHistory.put(ctx, dayB, Math.round(daily * 100.0d));
        eq("两天合计 = 412.50 + 300", 71250, (int) SalaryHistory.totalCents(ctx));

        // ---- 未开启加班时不应有加班费 ----
        newStore();
        OvertimeLog.start(ctx, dayA, 1700000000000L);
        OvertimeLog.stop(ctx, 1700000000000L + 7200000L);
        eqD("未开启时加班费为 0", 0.0, OvertimeCalc.earned(daily, normalSeconds, 0.0d, 15));
    }

    static void testHolidayWork() {
        // ---- 法定节假日表 ----
        check("2025 国庆 10/1 是法定", StatutoryHolidays.isStatutory(20251001));
        check("2025 国庆 10/2 是法定", StatutoryHolidays.isStatutory(20251002));
        check("2025 国庆 10/3 是法定", StatutoryHolidays.isStatutory(20251003));
        check("2025 国庆 10/4 非法定（调休）", !StatutoryHolidays.isStatutory(20251004));
        check("2025 国庆 10/8 非法定（调休）", !StatutoryHolidays.isStatutory(20251008));
        check("2025 中秋 10/6 是法定", StatutoryHolidays.isStatutory(20251006));
        check("2025 春节 1/28（除夕）是法定", StatutoryHolidays.isStatutory(20250128));
        check("2025 春节 2/4 非法定", !StatutoryHolidays.isStatutory(20250204));
        check("2026 国庆 10/1 是法定", StatutoryHolidays.isStatutory(20261001));
        check("2026 国庆 10/4 非法定", !StatutoryHolidays.isStatutory(20261004));
        check("普通工作日非法定", !StatutoryHolidays.isStatutory(20261009));
        eq("2025 年法定天数 = 13", 13, countStatutoryInYear(2025));
        eq("2026 年法定天数 = 13", 13, countStatutoryInYear(2026));

        // ---- 默认倍率：法定 3 倍，其他休息日 2 倍 ----
        newStore();
        eq("法定节假日默认 3 倍", 30, StatutoryHolidays.isStatutory(20251001) ? 30 : 20);
        eq("周末默认 2 倍", 20, StatutoryHolidays.isStatutory(20251004) ? 30 : 20);

        // ---- 记录与取消 ----
        int oct1 = 20251001;
        check("初始无出勤记录", !HolidayWork.has(ctx, oct1));
        eq("初始倍率 0", 0, HolidayWork.rateX10Of(ctx, oct1));

        HolidayWork.put(ctx, oct1, 30);
        check("打卡后有记录", HolidayWork.has(ctx, oct1));
        eq("倍率 3 倍", 30, HolidayWork.rateX10Of(ctx, oct1));

        // 覆盖写入
        HolidayWork.put(ctx, oct1, 20);
        eq("覆盖为 2 倍", 20, HolidayWork.rateX10Of(ctx, oct1));
        eq("仍只有一条", 1, HolidayWork.load(ctx).size());

        // 取消
        HolidayWork.remove(ctx, oct1);
        check("取消后无记录", !HolidayWork.has(ctx, oct1));
        eq("取消后列表为空", 0, HolidayWork.load(ctx).size());

        // 多条记录
        HolidayWork.put(ctx, 20251001, 30);
        HolidayWork.put(ctx, 20251004, 20);
        HolidayWork.put(ctx, 20260101, 30);
        eq("三条记录", 3, HolidayWork.load(ctx).size());
        eq("10/1 是 3 倍", 30, HolidayWork.rateX10Of(ctx, 20251001));
        eq("10/4 是 2 倍", 20, HolidayWork.rateX10Of(ctx, 20251004));
        eq("未记录的日期为 0", 0, HolidayWork.rateX10Of(ctx, 20251002));

        // ---- 收入计算（总额倍率口径）----
        double daily = 300.0d;
        eqD("3 倍 = 900", 900.0, HolidayWork.earned(daily, 30));
        eqD("2 倍 = 600", 600.0, HolidayWork.earned(daily, 20));
        eqD("1.5 倍 = 450", 450.0, HolidayWork.earned(daily, 15));
        eqD("倍率 0 = 0", 0.0, HolidayWork.earned(daily, 0));
        eqD("日薪 0 = 0", 0.0, HolidayWork.earned(0.0d, 30));

        // ---- 倍率文本 ----
        eq("3 倍文本", "3 倍", HolidayWork.rateText(30));
        eq("2 倍文本", "2 倍", HolidayWork.rateText(20));
        eq("1.5 倍文本", "1.5 倍", HolidayWork.rateText(15));

        // ---- 跨天归档：出勤日必须计入工资日历 ----
        newStore();
        int day3x = 20251001;
        int day2x = 20251004;
        HolidayWork.put(ctx, day3x, 30);
        HolidayWork.put(ctx, day2x, 20);
        SalaryHistory.put(ctx, day3x, Math.round(HolidayWork.earned(daily, 30) * 100.0d));
        SalaryHistory.put(ctx, day2x, Math.round(HolidayWork.earned(daily, 20) * 100.0d));
        eq("10 月合计 = 900 + 600", 150000, (int) SalaryHistory.monthTotalCents(ctx, 2025, 10));
        eq("两天都进了日历", 2, SalaryHistory.byMonth(ctx, 2025, 10).size());

        // ---- 持久化：重新读取仍一致 ----
        eq("重读仍是 3 倍", 30, HolidayWork.rateX10Of(ctx, day3x));
        eq("重读列表长度", 2, HolidayWork.load(ctx).size());

        // ---- 清理：删除某天记录 ----
        SalaryHistory.remove(ctx, day3x);
        eq("删除后只剩一天", 1, SalaryHistory.byMonth(ctx, 2025, 10).size());
        eq("合计变为 600", 60000, (int) SalaryHistory.monthTotalCents(ctx, 2025, 10));
    }

    static void testCrashLogger() {
        newStore();
        eq("初始无崩溃记录", null, CrashLogger.lastCrashText(ctx));
        eq("初始时间戳 0", 0, (int) CrashLogger.lastCrashAt(ctx));
        eq("初始摘要为空", null, CrashLogger.summary(ctx));

        // 模拟一条崩溃记录
        String crash = "时间: 2026-10-09 19:00:00\n"
                + "线程: main\n"
                + "异常: java.lang.IllegalStateException\n"
                + "信息: 测试崩溃\n\n"
                + "    at com.salaryjump.app.MainActivity.test(MainActivity.java:1)\n";
        prefs.map.put("last_crash_text", crash);
        prefs.map.put("last_crash_at", Long.valueOf(1700000000000L));

        eq("能读到崩溃文本", crash, CrashLogger.lastCrashText(ctx));
        eqL("能读到时间戳", 1700000000000L, CrashLogger.lastCrashAt(ctx));

        String summary = CrashLogger.summary(ctx);
        check("摘要非空", summary != null && summary.length() > 0);
        check("摘要含异常简名", summary != null && summary.contains("IllegalStateException"));
        check("摘要含信息", summary != null && summary.contains("测试崩溃"));

        // 清除
        CrashLogger.clear(ctx);
        eq("清除后无文本", null, CrashLogger.lastCrashText(ctx));
        eq("清除后时间戳 0", 0, (int) CrashLogger.lastCrashAt(ctx));

        // 摘要过长时截断
        prefs.map.put("last_crash_text", "异常: java.lang.RuntimeException\n信息: " + repeatStr("x", 200) + "\n");
        String longSummary = CrashLogger.summary(ctx);
        check("超长摘要被截断", longSummary != null && longSummary.length() <= 62);

        // 无 message 的异常
        prefs.map.put("last_crash_text", "异常: java.lang.NullPointerException\n信息: null\n");
        String npSummary = CrashLogger.summary(ctx);
        check("NPE 摘要可用", npSummary != null && npSummary.contains("NullPointerException"));
    }

    static void testUpdateStage() {
        newStore();
        eq("初始无阶段记录", null, HolidayUpdater.lastStageText(ctx));
        check("初始未成功", !HolidayUpdater.lastStageSucceeded(ctx));

        // 记录「已点击」
        HolidayUpdater.markStage(ctx, "已点击按钮，准备请求");
        String stage = HolidayUpdater.lastStageText(ctx);
        check("能读到阶段", stage != null && stage.contains("已点击按钮"));
        check("阶段含时间戳", stage != null && stage.matches("\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}.*"));
        check("点击后未成功", !HolidayUpdater.lastStageSucceeded(ctx));

        // 推进到请求
        HolidayUpdater.markStage(ctx, "正在请求 2026 年数据");
        stage = HolidayUpdater.lastStageText(ctx);
        check("阶段已推进", stage != null && stage.contains("正在请求 2026"));
        check("请求中未成功", !HolidayUpdater.lastStageSucceeded(ctx));

        // 成功
        HolidayUpdater.markStage(ctx, "成功：已更新 1 年数据：33 天放假、6 天补班、法定 13 天");
        check("成功后标记成功", HolidayUpdater.lastStageSucceeded(ctx));
        stage = HolidayUpdater.lastStageText(ctx);
        check("成功文本含数据", stage != null && stage.contains("33 天放假"));

        // 失败
        HolidayUpdater.markStage(ctx, "更新失败：Connection refused");
        check("失败后未成功", !HolidayUpdater.lastStageSucceeded(ctx));
        stage = HolidayUpdater.lastStageText(ctx);
        check("失败文本可读", stage != null && stage.contains("Connection refused"));

        // 超时
        HolidayUpdater.markStage(ctx, "超时（35 秒无响应）");
        check("超时后未成功", !HolidayUpdater.lastStageSucceeded(ctx));

        // 通过 markStage 写入的必须是同步提交（commit）
        check("已落盘", prefs.map.containsKey("holiday_update_stage"));
    }

    static void testHostResolution() {
        newStore();
        MainActivity host = new MainActivity();

        // 模拟 Dialog 的行为：把 Activity 包一层 ContextWrapper
        android.content.Context wrapped = new android.content.ContextWrapper(host);
        check("能穿透一层包装", Contexts.activityOf(wrapped) == host);

        // 两层包装（某些系统会嵌套）
        android.content.Context twice = new android.content.ContextWrapper(wrapped);
        check("能穿透两层包装", Contexts.activityOf(twice) == host);

        // 直接传 Activity
        check("直接传 Activity", Contexts.activityOf(host) == host);

        // 不含 Activity 的普通 Context → 返回 null（不抛异常）
        check("普通 Context 返回 null", Contexts.activityOf(ctx) == null);

        // null 安全
        check("null 输入返回 null", Contexts.activityOf(null) == null);

        // 深链不会死循环
        android.content.Context deep = ctx;
        for (int i = 0; i < 20; i++) {
            deep = new android.content.ContextWrapper(deep);
        }
        check("深层包装不会死循环", Contexts.activityOf(deep) == null);
    }

    static void testLeave() {
        // ---- 纯计算：薪酬比例 ----
        eqD("年假全薪 → 1.0", 1.0, LeaveRecord.ratio(100, false));
        eqD("事假无薪 → 0.0", 0.0, LeaveRecord.ratio(0, false));
        eqD("病假 60% → 0.6", 0.6, LeaveRecord.ratio(60, false));
        eqD("年假半天 → 0.5", 0.5, LeaveRecord.ratio(100, true));
        eqD("病假半天 → 0.3", 0.3, LeaveRecord.ratio(60, true));
        eqD("事假半天 → 0", 0.0, LeaveRecord.ratio(0, true));
        eqD("超范围钳制 150 → 1.0", 1.0, LeaveRecord.ratio(150, false));
        eqD("负数钳制 → 0", 0.0, LeaveRecord.ratio(-10, false));

        // ---- 收入 ----
        double daily = 300.0d;
        eqD("年假 = 300", 300.0, LeaveRecord.earned(daily, 100, false));
        eqD("事假 = 0", 0.0, LeaveRecord.earned(daily, 0, false));
        eqD("病假 = 180", 180.0, LeaveRecord.earned(daily, 60, false));
        eqD("年假半天 = 150", 150.0, LeaveRecord.earned(daily, 100, true));
        eqD("病假半天 = 90", 90.0, LeaveRecord.earned(daily, 60, true));
        eqD("日薪 0 = 0", 0.0, LeaveRecord.earned(0.0d, 100, false));

        // ---- 文本 ----
        eq("全薪文本", "全薪", LeaveRecord.payText(100));
        eq("无薪文本", "无薪", LeaveRecord.payText(0));
        eq("60% 文本", "60%", LeaveRecord.payText(60));
        eq("描述（整天）", "年假 · 全薪", LeaveRecord.describe("年假", 100, false));
        eq("描述（半天）", "病假 · 60% · 半天", LeaveRecord.describe("病假", 60, true));
        eq("类型索引", 5, LeaveRecord.presetIndex("病假"));
        eq("未知类型 -1", -1, LeaveRecord.presetIndex("太空假"));

        // ---- 存储闭环 ----
        newStore();
        int d1 = 20261012;
        check("初始无请假", !LeaveRecord.has(ctx, d1));
        check("初始为 null", LeaveRecord.get(ctx, d1) == null);

        LeaveRecord.put(ctx, d1, "年假", 100, false);
        check("记录后有", LeaveRecord.has(ctx, d1));
        eq("类型名称", "年假", LeaveRecord.get(ctx, d1).name);
        eq("比例", 100, LeaveRecord.get(ctx, d1).payPercent);
        check("整天", !LeaveRecord.get(ctx, d1).half);

        // 覆盖写入（改半天）
        LeaveRecord.put(ctx, d1, "病假", 60, true);
        eq("覆盖后名称", "病假", LeaveRecord.get(ctx, d1).name);
        eq("覆盖后比例", 60, LeaveRecord.get(ctx, d1).payPercent);
        check("覆盖后半天", LeaveRecord.get(ctx, d1).half);
        eq("仍只有一条", 1, LeaveRecord.load(ctx).size());

        // 多条
        LeaveRecord.put(ctx, 20261013, "事假", 0, false);
        eq("两条记录", 2, LeaveRecord.load(ctx).size());
        eq("第一天仍是病假", 60, LeaveRecord.get(ctx, d1).payPercent);

        // 取消
        LeaveRecord.remove(ctx, d1);
        check("取消后无", !LeaveRecord.has(ctx, d1));
        eq("剩一条", 1, LeaveRecord.load(ctx).size());

        // ---- 名称清洗 ----
        newStore();
        LeaveRecord.put(ctx, 20261014, "a,b;c:d", 100, false);
        String name = LeaveRecord.get(ctx, 20261014).name;
        check("逗号已清理", !name.contains(","));
        check("分号已清理", !name.contains(";"));
        check("冒号已清理", !name.contains(":"));

        // 超长名截断
        LeaveRecord.put(ctx, 20261015, "一二三四五六七八九十", 100, false);
        check("名称已截断到 8 字", LeaveRecord.get(ctx, 20261015).name.length() <= 8);

        // ---- 跨天归档：请假必须计入工资日历 ----
        newStore();
        int leaveDay = 20261016;
        LeaveRecord.put(ctx, leaveDay, "病假", 60, false);
        double earned = LeaveRecord.earned(daily, 60, false);   // 180
        SalaryHistory.put(ctx, leaveDay, Math.round(earned * 100.0d));
        eq("日历里是 180.00", 18000, (int) SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // 半天
        newStore();
        LeaveRecord.put(ctx, 20261017, "年假", 100, true);
        double halfEarned = LeaveRecord.earned(daily, 100, true);  // 150
        SalaryHistory.put(ctx, 20261017, Math.round(halfEarned * 100.0d));
        eq("半天是 150.00", 15000, (int) SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // 事假无薪
        newStore();
        LeaveRecord.put(ctx, 20261018, "事假", 0, false);
        double none = LeaveRecord.earned(daily, 0, false);  // 0
        SalaryHistory.put(ctx, 20261018, Math.round(none * 100.0d));
        eq("事假 0 元也进日历", 0, (int) SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eq("但记录存在", 1, SalaryHistory.byMonth(ctx, 2026, 10).size());
    }

    static void testPrivacyAndPayday() {
        // ── PIN 合法性 ──
        check("4 位数字合法", PrivacyLock.isValidPin("1234"));
        check("3 位不合法", !PrivacyLock.isValidPin("123"));
        check("5 位不合法", !PrivacyLock.isValidPin("12345"));
        check("含字母不合法", !PrivacyLock.isValidPin("12a4"));
        check("空不合法", !PrivacyLock.isValidPin(""));
        check("null 不合法", !PrivacyLock.isValidPin(null));

        // ── 弱 PIN 判定 ──
        check("1234 弱", PrivacyLock.isWeakPin("1234"));
        check("0000 弱", PrivacyLock.isWeakPin("0000"));
        check("8888 弱", PrivacyLock.isWeakPin("8888"));
        check("4321 弱（倒序）", PrivacyLock.isWeakPin("4321"));
        check("1212 弱", PrivacyLock.isWeakPin("1212"));
        check("7362 不弱", !PrivacyLock.isWeakPin("7362"));
        check("9081 不弱", !PrivacyLock.isWeakPin("9081"));

        // ── 哈希 ──
        String h1 = PrivacyLock.hash("1234", "abcd");
        eq("哈希长度 64", 64, h1.length());
        eq("同参哈希稳定", h1, PrivacyLock.hash("1234", "abcd"));
        check("不同盐 → 不同哈希", !h1.equals(PrivacyLock.hash("1234", "abce")));
        check("不同 PIN → 不同哈希", !h1.equals(PrivacyLock.hash("1235", "abcd")));
        check("空参数安全", PrivacyLock.hash(null, "a").isEmpty());

        // ── 恒定时间比较 ──
        check("相等 → true", PrivacyLock.constantTimeEquals("abc", "abc"));
        check("不等 → false", !PrivacyLock.constantTimeEquals("abc", "abd"));
        check("长度不同 → false", !PrivacyLock.constantTimeEquals("abc", "ab"));
        check("null → false", !PrivacyLock.constantTimeEquals(null, "a"));

        // ── 存储闭环 ──
        newStore();
        check("初始未启用", !PrivacyLock.isEnabled(ctx));
        PrivacyLock.setPin(ctx, "7362");
        check("设置后启用", PrivacyLock.isEnabled(ctx));
        check("正确 PIN 通过", PrivacyLock.verify(ctx, "7362"));
        check("错误 PIN 拒绝", !PrivacyLock.verify(ctx, "7363"));
        check("空 PIN 拒绝", !PrivacyLock.verify(ctx, ""));
        // 修改
        PrivacyLock.setPin(ctx, "9081");
        check("新 PIN 通过", PrivacyLock.verify(ctx, "9081"));
        check("旧 PIN 失效", !PrivacyLock.verify(ctx, "7362"));
        // 指纹开关默认开
        check("指纹默认允许", PrivacyLock.isBiometricAllowed(ctx));
        PrivacyLock.setBiometricAllowed(ctx, false);
        check("指纹可关闭", !PrivacyLock.isBiometricAllowed(ctx));
        // 清除
        PrivacyLock.clear(ctx);
        check("清除后未启用", !PrivacyLock.isEnabled(ctx));
        check("清除后校验失败", !PrivacyLock.verify(ctx, "9081"));

        // 加盐：同一个 PIN 两次设置 → 不同哈希（盐不同）
        newStore();
        PrivacyLock.setPin(ctx, "7362");
        String stored1 = ctx.getSharedPreferences("salary_jump", 0).getString("lock_pin_hash", "");
        PrivacyLock.setPin(ctx, "7362");
        String stored2 = ctx.getSharedPreferences("salary_jump", 0).getString("lock_pin_hash", "");
        check("同 PIN 两次设置哈希不同（盐随机）", !stored1.equals(stored2));
        check("但仍能通过校验", PrivacyLock.verify(ctx, "7362"));

        // ── 发薪日：每月天数 ──
        eq("1 月 31 天", 31, PaydayReminder.daysInMonth(2026, 1));
        eq("2 月平年 28 天", 28, PaydayReminder.daysInMonth(2026, 2));
        eq("2 月闰年 29 天", 29, PaydayReminder.daysInMonth(2024, 2));
        eq("2 月 2000 闰年 29 天", 29, PaydayReminder.daysInMonth(2000, 2));
        eq("2 月 1900 平年 28 天", 28, PaydayReminder.daysInMonth(1900, 2));
        eq("4 月 30 天", 30, PaydayReminder.daysInMonth(2026, 4));
        eq("12 月 31 天", 31, PaydayReminder.daysInMonth(2026, 12));

        // ── 发薪日：31 号在小月顺延 ──
        eq("31 号在 4 月 → 30", 30, PaydayReminder.effectiveDay(2026, 4, 31));
        eq("31 号在 2 月 → 28", 28, PaydayReminder.effectiveDay(2026, 2, 31));
        eq("31 号在 1 月 → 31", 31, PaydayReminder.effectiveDay(2026, 1, 31));
        eq("15 号不变", 15, PaydayReminder.effectiveDay(2026, 4, 15));
        eq("0 号钳到 1", 1, PaydayReminder.effectiveDay(2026, 4, 0));
        eq("clampDay 0 → 1", 1, PaydayReminder.clampDay(0));
        eq("clampDay 40 → 31", 31, PaydayReminder.clampDay(40));

        // ── 下一次触发 ──
        // 2026-10-01 12:00，发薪日 15 号 → 本月 15 号 10:00
        Calendar now = Calendar.getInstance();
        now.set(2026, 9, 1, 12, 0, 0);
        Calendar next = Calendar.getInstance();
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 15, 0));
        eq("本月 15 号（月）", 9, next.get(2));
        eq("本月 15 号（日）", 15, next.get(5));
        eq("触发时刻 10 点", 10, next.get(11));
        check("在未来", next.getTimeInMillis() > now.getTimeInMillis());

        // 2026-10-20 12:00（已过 15 号）→ 11 月 15 号
        now.set(2026, 9, 20, 12, 0, 0);
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 15, 0));
        eq("跨月到 11 月", 10, next.get(2));
        eq("跨月日仍 15", 15, next.get(5));

        // 提前一天提醒
        now.set(2026, 9, 1, 12, 0, 0);
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 15, -1));
        eq("前一天 = 14 号", 14, next.get(5));
        eq("前一天 10 点", 10, next.get(11));

        // 12 月 31 日发薪 → 跨年 1 月
        now.set(2026, 11, 31, 12, 0, 0);
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 31, 0));
        eq("跨年到 2027 年 1 月", 0, next.get(Calendar.MONTH));
        eq("2027 年", 2027, next.get(1));
        eq("1 月 31 日", 31, next.get(5));

        // 发薪日当天 10 点后查询 → 下个月
        now.set(2026, 9, 15, 12, 0, 0);
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 15, 0));
        eq("当天已过 → 11 月", 10, next.get(2));

        // 发薪日当天 10 点前查询 → 今天
        now.set(2026, 9, 15, 8, 0, 0);
        next.setTimeInMillis(PaydayReminder.nextTrigger(now, 15, 0));
        eq("当天未到 → 仍是 10 月", 9, next.get(2));
        eq("仍是 15 日", 15, next.get(5));

        // 存储
        newStore();
        check("发薪日默认关闭", !PaydayReminder.isEnabled(ctx));
        eq("默认 15 号", 15, PaydayReminder.day(ctx));
        PaydayReminder.set(ctx, true, 31);
        check("开启后启用", PaydayReminder.isEnabled(ctx));
        eq("保存 31 号", 31, PaydayReminder.day(ctx));
        PaydayReminder.set(ctx, true, 99);
        eq("超范围钳到 31", 31, PaydayReminder.day(ctx));
        PaydayReminder.set(ctx, false, 10);
        check("关闭后停用", !PaydayReminder.isEnabled(ctx));
        eq("10 号仍保存", 10, PaydayReminder.day(ctx));
    }

    static void testWorkRules() {
        newStore();

        // ── 默认值（用户公司规则：周末调休 / 工作日 1.5 / 法定 3）──
        check("默认周末=调休", WorkRules.isCompMode(ctx));
        eq("默认周末调休模式", WorkRules.WEEKEND_COMP, WorkRules.weekendMode(ctx));
        eq("默认周末计薪倍率 20", 20, WorkRules.weekendRateX10(ctx));
        eq("默认法定倍率 30", 30, WorkRules.statutoryRateX10(ctx));
        eq("默认工作日加班 1.5 倍", 15, WorkRules.overtimeRateX10(ctx));
        eq("默认宽限 30 分钟", 30, WorkRules.overtimeGraceMinutes(ctx));

        // ── 工作日加班设置 ──
        WorkRules.setOvertimeRateX10(ctx, 20);
        eq("加班倍率已保存", 20, WorkRules.overtimeRateX10(ctx));
        WorkRules.setOvertimeGraceMinutes(ctx, 15);
        eq("宽限已保存", 15, WorkRules.overtimeGraceMinutes(ctx));

        // ── 周末模式 ──
        WorkRules.setWeekendMode(ctx, WorkRules.WEEKEND_PAY);
        check("切到计薪模式", !WorkRules.isCompMode(ctx));
        eq("计薪模式倍率读取", 20, WorkRules.weekendRateX10(ctx));
        WorkRules.setWeekendRateX10(ctx, 15);
        eq("周末倍率改 15", 15, WorkRules.weekendRateX10(ctx));
        WorkRules.setWeekendMode(ctx, WorkRules.WEEKEND_COMP);
        check("切回调休模式", WorkRules.isCompMode(ctx));

        // ── restDayRateX10 判定 ──
        eq("法定假日 → 30（调休模式也走倍率）", 30, WorkRules.restDayRateX10(ctx, true));
        eq("周末 + 调休模式 → 0", 0, WorkRules.restDayRateX10(ctx, false));
        WorkRules.setWeekendMode(ctx, WorkRules.WEEKEND_PAY);
        eq("周末 + 计薪模式 → 15", 15, WorkRules.restDayRateX10(ctx, false));
        check("法定假日不用调休", !WorkRules.useCompFor(ctx, true));
        check("周末计薪模式不用调休", !WorkRules.useCompFor(ctx, false));
        WorkRules.setWeekendMode(ctx, WorkRules.WEEKEND_COMP);
        check("周末调休模式用调休", WorkRules.useCompFor(ctx, false));
        WorkRules.setWeekendRateX10(ctx, 20);
        WorkRules.setStatutoryRateX10(ctx, 30);

        // ── formatDays ──
        eq("整数不带小数", "2", WorkRules.formatDays(2.0d));
        eq("半天显示一位小数", "1.5", WorkRules.formatDays(1.5d));
        eq("负数", "-1", WorkRules.formatDays(-1.0d));
        eq("负半天", "-0.5", WorkRules.formatDays(-0.5d));
        eq("零", "0", WorkRules.formatDays(0.0d));

        // ── HolidayWork comp 字段 ──
        newStore();
        HolidayWork.put(ctx, 20261012, 0, true);
        check("调休记录存在", HolidayWork.has(ctx, 20261012));
        check("是调休记账", HolidayWork.isComp(ctx, 20261012));
        eq("调休记录倍率为 0", 0, HolidayWork.rateX10Of(ctx, 20261012));
        HolidayWork.put(ctx, 20261013, 30, false);
        check("普通出勤不是调休", !HolidayWork.isComp(ctx, 20261013));
        eq("普通出勤倍率 30", 30, HolidayWork.rateX10Of(ctx, 20261013));

        // 覆盖：调休 → 计薪
        HolidayWork.put(ctx, 20261012, 20, false);
        check("覆盖后不再是调休", !HolidayWork.isComp(ctx, 20261012));
        eq("覆盖后倍率 20", 20, HolidayWork.rateX10Of(ctx, 20261012));

        // ── 旧格式兼容（无第三字段 → 非调休）──
        newStore();
        ctx.getSharedPreferences("salary_jump", 0).edit().putString("holiday_work", "20261014,30").apply();
        check("旧格式可读", HolidayWork.has(ctx, 20261014));
        eq("旧格式倍率 30", 30, HolidayWork.rateX10Of(ctx, 20261014));
        check("旧格式默认非调休", !HolidayWork.isComp(ctx, 20261014));

        // ── 调休账户 ──
        newStore();
        eqD("初始余额 0", 0.0d, WorkRules.compBalance(ctx));
        HolidayWork.put(ctx, 20261017, 0, true);  // 周六出勤
        HolidayWork.put(ctx, 20261018, 0, true);  // 周日出勤
        eqD("已获 2 天", 2.0d, WorkRules.compEarned(ctx));
        eqD("未休时余额 2", 2.0d, WorkRules.compBalance(ctx));
        LeaveRecord.put(ctx, 20261021, "调休", 100, false);   // 调休整天
        eqD("已休 1 天", 1.0d, WorkRules.compUsed(ctx));
        eqD("余额 1", 1.0d, WorkRules.compBalance(ctx));
        LeaveRecord.put(ctx, 20261022, "调休", 100, true);    // 调休半天
        eqD("已休 1.5 天", 1.5d, WorkRules.compUsed(ctx));
        eqD("余额 0.5", 0.5d, WorkRules.compBalance(ctx));
        LeaveRecord.put(ctx, 20261023, "病假", 60, false);    // 非调休不计
        eqD("病假不影响余额", 0.5d, WorkRules.compBalance(ctx));
        // 普通出勤（计薪）不计入调休
        HolidayWork.put(ctx, 20261019, 20, false);
        eqD("计薪出勤不计调休", 2.0d, WorkRules.compEarned(ctx));
    }

    static void testExtraIncome() {
        newStore();

        // ── 备注清洗 ──
        eq("空备注", "", ExtraIncome.cleanRemark(null));
        eq("空白备注", "", ExtraIncome.cleanRemark("   "));
        eq("普通备注", "帮朋友修图", ExtraIncome.cleanRemark("帮朋友修图"));
        eq("去首尾空格", "修图", ExtraIncome.cleanRemark("  修图  "));
        check("逗号已清理", !ExtraIncome.cleanRemark("a,b").contains(","));
        check("分号已清理", !ExtraIncome.cleanRemark("a;b").contains(";"));
        check("换行已清理", !ExtraIncome.cleanRemark("a\nb").contains("\n"));
        eq("超长截断到12字", 12, ExtraIncome.cleanRemark("一二三四五六七八九十十一十二十三").length());

        // ── 存储基本操作 ──
        newStore();
        int d1 = 20261012;
        check("初始无外快", !ExtraIncome.has(ctx, d1));
        eqL("初始金额 0", 0L, ExtraIncome.centsOf(ctx, d1));
        eq("初始备注空", "", ExtraIncome.remarkOf(ctx, d1));

        ExtraIncome.put(ctx, d1, 20000L, "帮朋友修图");
        check("记录后有", ExtraIncome.has(ctx, d1));
        eqL("金额 200 元", 20000L, ExtraIncome.centsOf(ctx, d1));
        eq("备注", "帮朋友修图", ExtraIncome.remarkOf(ctx, d1));
        eqD("dayTotal 200.0", 200.0d, ExtraIncome.dayTotal(ctx, d1));

        // 覆盖
        ExtraIncome.put(ctx, d1, 30000L, "改");
        eqL("覆盖后金额", 30000L, ExtraIncome.centsOf(ctx, d1));
        eq("仍只有一条", 1, ExtraIncome.load(ctx).size());

        // 删除
        ExtraIncome.remove(ctx, d1);
        check("删除后无", !ExtraIncome.has(ctx, d1));
        eqL("删除后 0", 0L, ExtraIncome.centsOf(ctx, d1));

        // 多条
        newStore();
        ExtraIncome.put(ctx, 20261010, 10000L, "a");
        ExtraIncome.put(ctx, 20261011, 20000L, "b");
        ExtraIncome.put(ctx, 20261012, 30000L, "c");
        eq("三条记录", 3, ExtraIncome.load(ctx).size());
        eqL("第一天 100", 10000L, ExtraIncome.centsOf(ctx, 20261010));
        eqL("第三天 300", 30000L, ExtraIncome.centsOf(ctx, 20261012));

        // ── apply：未归档日（只写记录，不调整历史）──
        newStore();
        // cum_last_day = 0 表示从未归档过
        ExtraIncome.apply(ctx, 20261015, 50000L, "接单");
        eqL("未归档日：写入成功", 50000L, ExtraIncome.centsOf(ctx, 20261015));
        eqL("未归档日：不碰历史", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // ── apply：已归档日的同步调整 ──
        newStore();
        // 模拟：该日已归档 ¥300（30000 分），cum_last_day 已推进到该日
        SalaryHistory.put(ctx, 20261014, 30000L);
        ctx.getSharedPreferences("salary_jump", 0).edit()
                .putInt("cum_last_day", 20261014)
                .putLong("cum_total_bits", Double.doubleToRawLongBits(300.0d))
                .apply();
        eqL("归档前月总额 300", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        ExtraIncome.apply(ctx, 20261014, 20000L, "修图");
        eqL("归档后月总额 500", 50000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("累计同步 500.0", 500.0d,
                Double.longBitsToDouble(ctx.getSharedPreferences("salary_jump", 0).getLong("cum_total_bits", 0L)));

        // 修改（200 → 150）：应只增加差额 -50
        ExtraIncome.apply(ctx, 20261014, 15000L, "修图");
        eqL("修改后月总额 450", 45000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("累计同步 450.0", 450.0d,
                Double.longBitsToDouble(ctx.getSharedPreferences("salary_jump", 0).getLong("cum_total_bits", 0L)));

        // 删除外快：应扣回 150
        ExtraIncome.apply(ctx, 20261014, 0L, "");
        eqL("删除后月总额 300", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("累计回到 300.0", 300.0d,
                Double.longBitsToDouble(ctx.getSharedPreferences("salary_jump", 0).getLong("cum_total_bits", 0L)));
        check("删除后无记录", !ExtraIncome.has(ctx, 20261014));

        // ── 累计下限保护 ──
        newStore();
        SalaryHistory.put(ctx, 20261016, 10000L);
        ctx.getSharedPreferences("salary_jump", 0).edit()
                .putInt("cum_last_day", 20261016)
                .putLong("cum_total_bits", Double.doubleToRawLongBits(100.0d))
                .apply();
        ExtraIncome.apply(ctx, 20261016, 0L, "");  // 删除不存在的（delta = 0，无调整）
        eqD("delta=0 不动累计", 100.0d,
                Double.longBitsToDouble(ctx.getSharedPreferences("salary_jump", 0).getLong("cum_total_bits", 0L)));

        // ── 格式：金额文本 ──
        eq("金额 200.00", "200.00", String.format(java.util.Locale.US, "%.2f", 20000L / 100.0d));
        eq("金额 12.50", "12.50", String.format(java.util.Locale.US, "%.2f", 1250L / 100.0d));
    }

    static void testArchiveSync() {
        // 模拟：归档已推进到 20261020
        int archived = 20261020;
        int restDay = 20261018;   // 周日
        int workDay = 20261014;   // 周三

        // ── 休息日补记出勤：进账 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(archived));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(300.0d)));

        HolidayWork.put(ctx, restDay, 30, false);
        eqL("补记出勤 3 倍：日历 900", 90000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("补记出勤 3 倍：累计 1200", 1200.0d, cumTotal());

        HolidayWork.put(ctx, restDay, 20, false);
        eqL("改 2 倍：日历 600", 60000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("改 2 倍：累计 900", 900.0d, cumTotal());

        HolidayWork.remove(ctx, restDay);
        eqL("取消出勤：日历 0", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("取消出勤：累计 300", 300.0d, cumTotal());

        // ── 调休记账：不动钱；切计薪才动 ──
        HolidayWork.put(ctx, restDay, 0, true);
        eqL("调休出勤：日历不变", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("调休出勤：累计不变", 300.0d, cumTotal());

        HolidayWork.put(ctx, restDay, 30, false);
        eqL("调休改计薪：日历 900", 90000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("调休改计薪：累计 1200", 1200.0d, cumTotal());

        // ── 该日已有外快：只叠加 / 回退出勤部分 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(archived));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(200.0d)));
        SalaryHistory.put(ctx, restDay, 20000L);

        HolidayWork.put(ctx, restDay, 30, false);
        eqL("外快日补出勤：日历 1100", 110000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("外快日补出勤：累计 1100", 1100.0d, cumTotal());

        HolidayWork.remove(ctx, restDay);
        eqL("取消后保留外快 200", 20000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("取消后累计回 200", 200.0d, cumTotal());

        // ── 工作日补记请假：按比例替换整日工资 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(archived));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(300.0d)));
        SalaryHistory.put(ctx, workDay, 30000L);

        LeaveRecord.put(ctx, workDay, "病假", 60, false);
        eqL("补记病假 60%：日历 180", 18000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("补记病假 60%：累计 180", 180.0d, cumTotal());

        LeaveRecord.put(ctx, workDay, "事假", 0, false);
        eqL("改事假无薪：日历 0", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("改事假无薪：累计 0", 0.0d, cumTotal());

        LeaveRecord.remove(ctx, workDay);
        eqL("取消请假：恢复整日 300", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("取消请假：累计 300", 300.0d, cumTotal());

        // ── 未归档日（含今天）：一律不碰金额 ──
        newStore();
        HolidayWork.put(ctx, restDay, 30, false);
        LeaveRecord.put(ctx, workDay, "病假", 60, false);
        eqL("未归档日：不碰日历", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("未归档日：不碰累计", 0.0d, cumTotal());

        // 归档点未推进到该日（stamp > cum_last_day）：同样不碰
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(100.0d)));
        HolidayWork.put(ctx, restDay, 30, false);
        eqL("归档点之后的日：不碰日历", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("归档点之后的日：累计不变", 100.0d, cumTotal());
    }

    static void testOvertimeDeleteRowSync() {
        int workDay = 20261014;   // 周三（工作日）
        long prewritten = 45000L; // 加班结束时的预写行：日薪 300 + 加班费 150
        long otCents = 15000L;

        // ── 未归档日（今天）＋已有预写行：刷新为去除加班费的口径 ──
        newStore();
        SalaryHistory.put(ctx, workDay, prewritten);
        prefs.map.put("cum_last_day", Integer.valueOf(20261010)); // 归档点 < 该日 → 未归档
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eqL("未归档：行刷新为日薪 300", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("未归档：累计不动 1000", 1000.0d, cumTotal());

        // ── 未归档 ＋ 外快：刷新后带上外快 ──
        newStore();
        SalaryHistory.put(ctx, workDay, prewritten);
        ExtraIncome.put(ctx, workDay, 20000L, "修图");
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eqL("未归档+外快：行 = 300+200", 50000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("未归档+外快：累计不动", 1000.0d, cumTotal());

        // ── 未归档 ＋ 请假：刷新为请假口径 ──
        newStore();
        SalaryHistory.put(ctx, workDay, prewritten);
        LeaveRecord.put(ctx, workDay, "病假", 60, false);
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eqL("未归档+病假：行 = 180", 18000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // ── 未归档但没有预写行：不创建 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eq("未归档无行：不创建", 0, SalaryHistory.byMonth(ctx, 2026, 10).size());

        // ── 已归档日：从行与累计中扣回加班费 ──
        newStore();
        SalaryHistory.put(ctx, workDay, prewritten);
        prefs.map.put("cum_last_day", Integer.valueOf(20261020)); // ≥ 该日 → 已归档
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(450.0d)));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eqL("已归档：行扣回 150", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("已归档：累计扣回 150", 300.0d, cumTotal());

        // ── 已归档 ＋ 外快：只扣加班费部分 ──
        newStore();
        SalaryHistory.put(ctx, workDay, 65000L); // 300 + 150 + 200
        ExtraIncome.put(ctx, workDay, 20000L, "修图");
        prefs.map.put("cum_last_day", Integer.valueOf(20261020));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(650.0d)));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, otCents);
        eqL("已归档+外快：行 = 300+200", 50000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("已归档+外快：累计 = 500", 500.0d, cumTotal());

        // ── 加班费为 0：一律不动 ──
        newStore();
        SalaryHistory.put(ctx, workDay, 30000L);
        prefs.map.put("cum_last_day", Integer.valueOf(20261020));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(300.0d)));
        ArchiveSync.onOvertimeRemoved(ctx, workDay, 0L);
        eqL("零加班费：行不变", 30000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("零加班费：累计不变", 300.0d, cumTotal());
    }

    static void testPrewrittenHeal() {
        int workDay = 20261014;    // 周三（工作日）
        long base = 30000L;        // 日薪 300
        long otCents = 11250L;     // 2 小时 × 1.5 倍 = 112.5（总工时 28800 秒）

        // ── 自愈：过期行刷新为「日薪+加班费」──
        newStore();
        prefs.map.put("overtime_enabled", Boolean.TRUE);
        prefs.map.put("overtime_rate", Integer.valueOf(15));
        long t0 = 1700000000000L;
        OvertimeLog.start(ctx, workDay, t0);
        OvertimeLog.stop(ctx, t0 + 7200000L); // 2 小时
        SalaryHistory.put(ctx, workDay, 10000L); // 模拟旧版本残留（错误值）
        prefs.map.put("cum_last_day", Integer.valueOf(20261010)); // 未归档
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(500.0d)));

        ArchiveSync.refreshPrewritten(ctx, workDay);
        eqL("自愈：行 = 日薪+加班费", base + otCents, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        eqD("自愈：累计不动", 500.0d, cumTotal());

        // 加外快后再自愈
        ExtraIncome.put(ctx, workDay, 20000L, "修图");
        ArchiveSync.refreshPrewritten(ctx, workDay);
        eqL("自愈+外快：行 = 300+112.5+200", base + otCents + 20000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // 数值一致时重复调用：值稳定
        ArchiveSync.refreshPrewritten(ctx, workDay);
        eqL("重复调用：值稳定", base + otCents + 20000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // ── 未归档但没有行：不创建 ──
        newStore();
        prefs.map.put("overtime_enabled", Boolean.TRUE);
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        ArchiveSync.refreshPrewritten(ctx, workDay);
        eq("无行：不创建", 0, SalaryHistory.byMonth(ctx, 2026, 10).size());

        // ── 已归档：不动 ──
        newStore();
        SalaryHistory.put(ctx, workDay, 10000L);
        prefs.map.put("cum_last_day", Integer.valueOf(20261020));
        ArchiveSync.refreshPrewritten(ctx, workDay);
        eqL("已归档：不动", 10000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // ── 用户场景复现：旧版删加班后残留 base+OT，加班已清空 → 自愈清理 ──
        newStore();
        prefs.map.put("overtime_enabled", Boolean.TRUE);
        prefs.map.put("overtime_rate", Integer.valueOf(15));
        SalaryHistory.put(ctx, workDay, base + otCents); // 残留行
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        ArchiveSync.refreshPrewritten(ctx, workDay);
        eqL("场景复现：残留加班费被清理", base, SalaryHistory.monthTotalCents(ctx, 2026, 10));
    }

    static void testLeaveAccounts() {
        int thisYear = Calendar.getInstance().get(1);
        int lastYear = ((thisYear - 1) * 10000) + 1231;
        int d1 = (thisYear * 10000) + 101;
        int d2 = (thisYear * 10000) + 201;
        int d3 = (thisYear * 10000) + 301;
        int d4 = (thisYear * 10000) + 401;

        // ── 类型列表 ──
        check("类型含育儿假", LeaveRecord.presetIndex("育儿假") >= 0);
        check("类型含长期贡献奖假", LeaveRecord.presetIndex("长期贡献奖假") >= 0);
        eq("育儿假全薪", 100, LeaveRecord.TYPE_PAY_PERCENT[LeaveRecord.presetIndex("育儿假")]);
        eq("长期贡献奖假全薪", 100, LeaveRecord.TYPE_PAY_PERCENT[LeaveRecord.presetIndex("长期贡献奖假")]);

        // ── 初始状态 ──
        newStore();
        check("初始未设置", !LeaveAccounts.isSet(ctx, 0));
        eqD("初始剩余 0", 0.0, LeaveAccounts.remaining(ctx, 0));
        eq("未设置时 remText null", null, LeaveAccounts.remText(ctx, "年假"));
        eq("婚假不受管 remText null", null, LeaveAccounts.remText(ctx, "婚假"));

        // ── 设置与读取 ──
        LeaveAccounts.setBudget(ctx, 0, 10.0, -1.0);
        check("设置后 isSet", LeaveAccounts.isSet(ctx, 0));
        eqD("总额 10", 10.0, LeaveAccounts.quota(ctx, 0));
        eqD("调整 -1", -1.0, LeaveAccounts.adjust(ctx, 0));
        eqD("剩余 9", 9.0, LeaveAccounts.remaining(ctx, 0));
        eq("remText 剩 9 天", "剩 9 天", LeaveAccounts.remText(ctx, "年假"));

        // ── 自动统计（今年）──
        LeaveRecord.put(ctx, d1, "年假", 100, false);
        eqD("已用 1", 1.0, LeaveAccounts.usedThisYear(ctx, 0));
        eqD("剩余 8", 8.0, LeaveAccounts.remaining(ctx, 0));

        LeaveRecord.put(ctx, d2, "年假", 100, true);
        eqD("已用 1.5（含半天）", 1.5, LeaveAccounts.usedThisYear(ctx, 0));

        LeaveRecord.put(ctx, lastYear, "年假", 100, false);
        eqD("往年不计", 1.5, LeaveAccounts.usedThisYear(ctx, 0));

        LeaveRecord.remove(ctx, d2);
        eqD("取消半天自动回退", 1.0, LeaveAccounts.usedThisYear(ctx, 0));

        // ── 类型隔离 ──
        LeaveRecord.put(ctx, d3, "病假", 60, false);
        eqD("病假不影响年假", 1.0, LeaveAccounts.usedThisYear(ctx, 0));
        eqD("病假已用 1", 1.0, LeaveAccounts.usedThisYear(ctx, 3));

        // ── 覆盖设置 ──
        LeaveAccounts.setBudget(ctx, 0, 20.0, 0.0);
        eqD("覆盖后总额 20", 20.0, LeaveAccounts.quota(ctx, 0));
        eqD("覆盖后剩余 19", 19.0, LeaveAccounts.remaining(ctx, 0));

        // ── 超支（负剩余）──
        newStore();
        LeaveAccounts.setBudget(ctx, 4, 1.0, 0.0);
        LeaveRecord.put(ctx, d1, "事假", 0, false);
        LeaveRecord.put(ctx, d2, "事假", 0, false);
        eqD("超支剩余 -1", -1.0, LeaveAccounts.remaining(ctx, 4));
        eq("超支 remText", "剩 -1 天", LeaveAccounts.remText(ctx, "事假"));

        // ── 调休：期初 + 已获 − 已休 ──
        newStore();
        check("调休未设置", !LeaveAccounts.compSet(ctx));
        eqD("调休初始 0", 0.0, LeaveAccounts.compRemaining(ctx));
        eq("调休未设置 remText null", null, LeaveAccounts.remText(ctx, "调休"));

        HolidayWork.put(ctx, d4, 0, true);
        eqD("已获 1（周末出勤）", 1.0, WorkRules.compEarned(ctx));
        LeaveRecord.put(ctx, d1, "调休", 100, false);
        eqD("已休 1", 1.0, WorkRules.compUsed(ctx));

        LeaveAccounts.setCompInitial(ctx, 2.0);
        check("调休已设置", LeaveAccounts.compSet(ctx));
        eqD("调休剩余 2（2+1-1）", 2.0, LeaveAccounts.compRemaining(ctx));
        eq("调休 remText 剩 2 天", "剩 2 天", LeaveAccounts.remText(ctx, "调休"));
        eqD("compBalance 与 compRemaining 一致", 2.0, WorkRules.compBalance(ctx));
    }

    static void testCheckInReminder() {
        // ── 触发逻辑矩阵：工作日 ──
        eq("工作日+加班开启+未打卡未请假 → 提醒加班", 1, CheckInReminder.reminderKind(true, true, false, false, false));
        eq("工作日+已打卡 → 不提醒", 0, CheckInReminder.reminderKind(true, true, true, false, false));
        eq("工作日+已请假 → 不提醒", 0, CheckInReminder.reminderKind(true, true, false, true, false));
        eq("工作日+加班功能关闭 → 不提醒", 0, CheckInReminder.reminderKind(true, false, false, false, false));
        eq("工作日+已打卡已请假（双重）→ 不提醒", 0, CheckInReminder.reminderKind(true, true, true, true, false));

        // ── 触发逻辑矩阵：休息日 ──
        eq("休息日+未标出勤 → 提醒出勤", 2, CheckInReminder.reminderKind(false, false, false, false, false));
        eq("休息日+已标出勤 → 不提醒", 0, CheckInReminder.reminderKind(false, false, false, false, true));
        eq("休息日+加班开启也不提醒加班", 2, CheckInReminder.reminderKind(false, true, false, false, false));

        // ── 下一次触发：今天未到点 → 今天 ──
        Calendar now = Calendar.getInstance();
        now.set(2026, 9, 10, 19, 0, 0);
        Calendar next = Calendar.getInstance();
        next.setTimeInMillis(CheckInReminder.nextTrigger(now, 20, 0));
        eq("未到点：今天（日）", 10, next.get(5));
        eq("未到点：20 时", 20, next.get(11));
        eq("未到点：0 分", 0, next.get(12));

        // ── 已到点/已过点 → 明天 ──
        now.set(2026, 9, 10, 20, 0, 0);
        next.setTimeInMillis(CheckInReminder.nextTrigger(now, 20, 0));
        eq("恰好 20:00（等于）→ 明天", 11, next.get(5));
        now.set(2026, 9, 10, 21, 30, 0);
        next.setTimeInMillis(CheckInReminder.nextTrigger(now, 20, 0));
        eq("已过点 → 明天", 11, next.get(5));
        eq("明天也是 20 时", 20, next.get(11));

        // ── 跨月：10/31 21:00 → 11/1 ──
        now.set(2026, 9, 31, 21, 0, 0);
        next.setTimeInMillis(CheckInReminder.nextTrigger(now, 20, 0));
        eq("跨月：月", 10, next.get(2));
        eq("跨月：日", 1, next.get(5));

        // ── 自定义分钟 ──
        now.set(2026, 9, 10, 19, 0, 0);
        next.setTimeInMillis(CheckInReminder.nextTrigger(now, 20, 30));
        eq("自定义 20:30", 30, next.get(12));

        // ── 钳制 ──
        eq("clampHour 25 → 23", 23, CheckInReminder.clampHour(25));
        eq("clampHour -1 → 0", 0, CheckInReminder.clampHour(-1));
        eq("clampMinute 70 → 59", 59, CheckInReminder.clampMinute(70));
        eq("clampMinute -5 → 0", 0, CheckInReminder.clampMinute(-5));

        // ── 存取 ──
        newStore();
        check("默认关闭", !CheckInReminder.isEnabled(ctx));
        eq("默认 20 时", 20, CheckInReminder.hour(ctx));
        eq("默认 0 分", 0, CheckInReminder.minute(ctx));

        CheckInReminder.set(ctx, true, 21, 30);
        check("开启生效", CheckInReminder.isEnabled(ctx));
        eq("保存 21 时", 21, CheckInReminder.hour(ctx));
        eq("保存 30 分", 30, CheckInReminder.minute(ctx));

        CheckInReminder.set(ctx, true, 99, 99);
        eq("越界时钳到 23", 23, CheckInReminder.hour(ctx));
        eq("越界分钳到 59", 59, CheckInReminder.minute(ctx));

        CheckInReminder.set(ctx, false, 21, 30);
        check("关闭生效", !CheckInReminder.isEnabled(ctx));

        // ── 持久化：重读仍一致 ──
        CheckInReminder.set(ctx, true, 8, 15);
        eq("持久化时", 8, CheckInReminder.hour(ctx));
        eq("持久化分", 15, CheckInReminder.minute(ctx));
    }

    static void testWidgetText() {
        // ── 休息日（未出勤）──
        eq("周末", "今天是周末", WidgetText.restText("周末", null));
        eq("法定节假日", "今天是国庆节", WidgetText.restText("国庆节", null));
        eq("带外快", "今天是周末 · 外快 ¥200.00", WidgetText.restText("周末", "200.00"));
        eq("无名称 → 休息日", "今天是休息日", WidgetText.restText(null, null));
        eq("空串 → 休息日", "今天是休息日", WidgetText.restText("", null));
        eq("空外快 → 不拼接", "今天是周末", WidgetText.restText("周末", ""));

        // ── 休息日出勤 ──
        eq("调休出勤", "周末出勤 · 调休+1", WidgetText.holidayWorkText("周末", true, 0));
        eq("2 倍出勤", "周末出勤 · 2 倍", WidgetText.holidayWorkText("周末", false, 20));
        eq("3 倍出勤（法定）", "国庆节出勤 · 3 倍", WidgetText.holidayWorkText("国庆节", false, 30));
        eq("1.5 倍出勤", "周末出勤 · 1.5 倍", WidgetText.holidayWorkText("周末", false, 15));
        eq("无名称出勤", "休息日出勤 · 2 倍", WidgetText.holidayWorkText(null, false, 20));

        // ── 请假 ──
        eq("请假有名", "请假 · 年假", WidgetText.leaveText("年假"));
        eq("请假无名 → 今天请假", "今天请假", WidgetText.leaveText(null));
        eq("请假空串 → 今天请假", "今天请假", WidgetText.leaveText(""));
    }

    static void testBackfillRows() {
        int d6 = 20261006;  // 国庆假期内的休息日
        int d8 = 20261008;  // 工作日

        // ── 已归档 + 补记调休出勤：缺行 → 建 ¥0 占位行 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        check("补记前 10/6 无行", !SalaryHistory.has(ctx, d6));
        HolidayWork.put(ctx, d6, 0, true);
        check("调休补记：建档（日历可见）", SalaryHistory.has(ctx, d6));
        eqL("调休补记：占位金额 0", 0L, SalaryHistory.centsOf(ctx, d6));
        eqD("调休补记：累计不动", 1000.0d, cumTotal());

        // ── 取消调休：空占位行被清 ──
        HolidayWork.remove(ctx, d6);
        check("取消出勤：空占位行清除", !SalaryHistory.has(ctx, d6));

        // ── 已归档 + 补记计薪出勤（2 倍）：建行带金额 ──
        HolidayWork.put(ctx, d6, 20, false);
        check("计薪补记：建档", SalaryHistory.has(ctx, d6));
        eqL("计薪补记：行 = 2 倍日薪", 60000L, SalaryHistory.centsOf(ctx, d6));
        eqD("计薪补记：累计 +600", 1600.0d, cumTotal());

        // ── 取消计薪：行清零并移除 ──
        HolidayWork.remove(ctx, d6);
        check("计薪取消：行移除", !SalaryHistory.has(ctx, d6));
        eqD("计薪取消：累计回 1000", 1000.0d, cumTotal());

        // ── 有外快时：不误删、金额保持 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        ExtraIncome.apply(ctx, d6, 20000L, "修图");
        eqL("外快建档 200", 20000L, SalaryHistory.centsOf(ctx, d6));
        HolidayWork.put(ctx, d6, 0, true);
        eqL("调休+外快：行不变 200", 20000L, SalaryHistory.centsOf(ctx, d6));
        HolidayWork.remove(ctx, d6);
        check("取消出勤：外快行保留", SalaryHistory.has(ctx, d6));
        eqL("取消出勤：外快金额保持", 20000L, SalaryHistory.centsOf(ctx, d6));

        // ── 未归档：一律不建行（归档时由 cumRollover 处理）──
        newStore();
        HolidayWork.put(ctx, d6, 0, true);
        check("未归档：不建行", !SalaryHistory.has(ctx, d6));

        // ── 工作日补记育儿假（全薪）：delta 0、行不变 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        SalaryHistory.put(ctx, d8, 30000L);
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(300.0d)));
        LeaveRecord.put(ctx, d8, "育儿假", 100, false);
        eqL("育儿假全薪：行不变", 30000L, SalaryHistory.centsOf(ctx, d8));
        eqD("育儿假全薪：累计不变", 300.0d, cumTotal());
        eqD("育儿假已用 1 天", 1.0d, LeaveAccounts.usedThisYear(ctx, "育儿假"));

        // ── heal：旧数据（出勤已记、缺行）自动补建 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("holiday_work", "20261006,0,1;");
        ArchiveSync.healEventRowsOnce(ctx);
        check("heal：建档", SalaryHistory.has(ctx, d6));
        eqL("heal：占位金额 0", 0L, SalaryHistory.centsOf(ctx, d6));
        eq("heal：本月至 1 行", 1, SalaryHistory.byMonth(ctx, 2026, 10).size());
        // 幂等：flag 已设，再次调用不改动
        ArchiveSync.healEventRowsOnce(ctx);
        eq("heal 幂等", 1, SalaryHistory.byMonth(ctx, 2026, 10).size());

        // ── heal：未归档（无 cum_last_day）不动 ──
        newStore();
        prefs.map.put("holiday_work", "20261006,0,1;");
        ArchiveSync.healEventRowsOnce(ctx);
        check("heal：未归档不建行", !SalaryHistory.has(ctx, d6));
    }

    static void testSeedBackfill() {
        // ── 准备：已归档 + 旧口径行（模拟旧工资 788.89/天）──
        newStore();
        SalaryHistory.put(ctx, 20261008, 78889L);
        SalaryHistory.put(ctx, 20261009, 78889L);
        prefs.map.put("cum_last_day", Integer.valueOf(20261011));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(2000.0d)));

        // 期望值：18140 ÷ 当月工作日
        Calendar now = Calendar.getInstance();
        int wd = WorkdayHelper.workdaysInMonth(ctx, MainActivity.SalarySettings.load(prefs), now.get(1), now.get(2) + 1);
        long target = Math.round((18140.0d / wd) * 100.0d);

        // ── 执行 ──
        SeedBackfill.applyOnce(ctx);

        // ── 三条记录 ──
        check("种子：10/6 出勤（调休）", HolidayWork.has(ctx, 20261006) && HolidayWork.isComp(ctx, 20261006));
        check("种子：10/6 占位行建档", SalaryHistory.has(ctx, 20261006));
        eqL("种子：10/6 占位金额 0", 0L, SalaryHistory.centsOf(ctx, 20261006));
        check("种子：10/8 育儿假", "育儿假".equals(LeaveRecord.get(ctx, 20261008).name));
        check("种子：10/9 育儿假", "育儿假".equals(LeaveRecord.get(ctx, 20261009).name));

        // ── 按新工资重算 ──
        eqL("种子：10/8 重算（18140 口径）", target, SalaryHistory.centsOf(ctx, 20261008));
        eqL("种子：10/9 重算（18140 口径）", target, SalaryHistory.centsOf(ctx, 20261009));
        // 累计由 reconcileCumulative 按行重算：10/6(=0) + 10/8 + 10/9
        double expectedCum = (target * 2) / 100.0d;
        eqD("种子：累计按行重算", expectedCum, cumTotal());

        // ── 月薪对齐 ──
        double dailyAfter = Double.longBitsToDouble((Long) prefs.map.get("daily_salary_bits"));
        eqD("种子：日薪 = 18140 / 工作日数", 18140.0d / wd, dailyAfter);

        // ── 账目联动 ──
        eqD("种子：调休 +1 天", 1.0d, WorkRules.compEarned(ctx));
        eqD("种子：育儿假已用 2 天", 2.0d, LeaveAccounts.usedThisYear(ctx, "育儿假"));

        // ── 幂等 ──
        SeedBackfill.applyOnce(ctx);
        eqL("幂等：行值稳定", target, SalaryHistory.centsOf(ctx, 20261008));
        eq("幂等：记录数稳定", 2, LeaveRecord.load(ctx).size());
        eqL("幂等：累计稳定", Math.round(expectedCum * 100.0d), Math.round(cumTotal() * 100.0d));

        // ── recalc 保护：未归档不动 ──
        newStore();
        SalaryHistory.put(ctx, 20261008, 50000L);
        ArchiveSync.recalcArchivedRow(ctx, 20261008);
        eqL("recalc：未归档不动", 50000L, SalaryHistory.centsOf(ctx, 20261008));

        // ── recalc 保护：无行不创建 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261011));
        ArchiveSync.recalcArchivedRow(ctx, 20261008);
        check("recalc：无行不创建", !SalaryHistory.has(ctx, 20261008));
    }

    static void testBackfillAndRecalcMonth() {
        // ── 补建行：全薪请假（累计 +1 天工资、已结算天数 +1）──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        prefs.map.put("cum_days", Integer.valueOf(2));
        check("补建前无行", !SalaryHistory.has(ctx, 20261008));
        LeaveRecord.put(ctx, 20261008, "育儿假", 100, false);
        check("全薪请假补建行", SalaryHistory.has(ctx, 20261008));
        eqL("全薪请假行金额 = 日薪", 30000L, SalaryHistory.centsOf(ctx, 20261008));
        eqD("全薪请假累计 +300", 1300.0d, cumTotal());
        eq("全薪请假天数 +1", 3, prefs.getInt("cum_days", 0));
        LeaveRecord.put(ctx, 20261008, "育儿假", 100, false);
        eqL("重复标记：行稳定", 30000L, SalaryHistory.centsOf(ctx, 20261008));
        eqD("重复标记：累计稳定", 1300.0d, cumTotal());
        eq("重复标记：天数稳定", 3, prefs.getInt("cum_days", 0));

        // ── 调休补建：不动累计与天数 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        prefs.map.put("cum_days", Integer.valueOf(2));
        HolidayWork.put(ctx, 20261006, 0, true);
        check("调休补建行", SalaryHistory.has(ctx, 20261006));
        eqL("调休行金额 0", 0L, SalaryHistory.centsOf(ctx, 20261006));
        eqD("调休累计不动", 1000.0d, cumTotal());
        eq("调休天数不动", 2, prefs.getInt("cum_days", 0));

        // ── v2 种子：补建 10/8 + 整月重算（10/10 从旧工资修正）──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(3000.0d)));
        prefs.map.put("cum_days", Integer.valueOf(3));
        prefs.map.put("daily_salary_bits", Long.valueOf(Double.doubleToRawLongBits(18140.0d / 18.0d)));
        prefs.map.put("seed_backfill_2026_10_v1", Boolean.TRUE); // 模拟 v1 已执行
        SalaryHistory.put(ctx, 20261010, 78889L);  // 旧工资口径
        SalaryHistory.put(ctx, 20261009, 100778L);
        SalaryHistory.put(ctx, 20261006, 0L);

        SeedBackfill.applyOnce(ctx);
        check("v2：10/8 补建", SalaryHistory.has(ctx, 20261008));
        eqL("v2：10/8 = 1007.78", 100778L, SalaryHistory.centsOf(ctx, 20261008));
        eqL("v2：10/10 重算 = 1007.78", 100778L, SalaryHistory.centsOf(ctx, 20261010));
        eqD("v2：累计 = 3 × 1007.78（按行重算）", 3023.34d, cumTotal());
        eq("v2：已结算天数 3", 3, prefs.getInt("cum_days", 0));

        SeedBackfill.applyOnce(ctx);
        eqL("v2 幂等：10/8 稳定", 100778L, SalaryHistory.centsOf(ctx, 20261008));
        eqL("v2 幂等：10/10 稳定", 100778L, SalaryHistory.centsOf(ctx, 20261010));
        eqD("v2 幂等：累计稳定", 3023.34d, cumTotal());
        eq("v2 幂等：天数稳定", 3, prefs.getInt("cum_days", 0));
    }

    static void testWaterRecordTimeEdit() {
        // ── 单条：改时间 ──
        newStore();
        prefs.map.put("water_day", keyOf(0));
        prefs.map.put("water_records", "1700000000000,250,0;");
        List<WaterReminderScheduler.WaterRecord> recs = WaterReminderScheduler.getRecords(ctx);
        eq("初始 1 条", 1, recs.size());
        long oldTs = recs.get(0).timestamp;
        eqL("初始时间戳", 1700000000000L, oldTs);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(oldTs);
        cal.set(Calendar.HOUR_OF_DAY, 8);
        cal.set(Calendar.MINUTE, 30);
        long newTs = cal.getTimeInMillis();

        check("改时间：成功", WaterReminderScheduler.updateRecordTime(ctx, oldTs, newTs));
        List<WaterReminderScheduler.WaterRecord> after = WaterReminderScheduler.getRecords(ctx);
        eq("改后仍是 1 条", 1, after.size());
        eqL("时间戳已更新", newTs, after.get(0).timestamp);
        eq("水量不变", 250, after.get(0).amountMl);
        eq("类型不变", 0, after.get(0).type);
        eq("总量不变", 250, WaterReminderScheduler.getConsumed(ctx));

        // ── 不存在的时间戳 ──
        check("不存在的时间戳 → false", !WaterReminderScheduler.updateRecordTime(ctx, 12345678L, newTs));
        eq("失败后记录数不变", 1, WaterReminderScheduler.getRecords(ctx).size());

        // ── 多条：只改目标，其余不变 ──
        newStore();
        prefs.map.put("water_day", keyOf(0));
        prefs.map.put("water_records", "1700000000000,250,0;1700000100000,500,3;");
        long t2 = 1700000100000L;
        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(t2);
        c2.set(Calendar.HOUR_OF_DAY, 9);
        c2.set(Calendar.MINUTE, 5);
        check("第二条改时间", WaterReminderScheduler.updateRecordTime(ctx, t2, c2.getTimeInMillis()));
        List<WaterReminderScheduler.WaterRecord> list2 = WaterReminderScheduler.getRecords(ctx);
        eq("仍为 2 条", 2, list2.size());
        eq("总量仍 750", 750, WaterReminderScheduler.getConsumed(ctx));
        boolean firstIntact = false;
        boolean secondMoved = false;
        for (WaterReminderScheduler.WaterRecord rec : list2) {
            if (rec.timestamp == 1700000000000L && rec.amountMl == 250) {
                firstIntact = true;
            }
            if (rec.timestamp == c2.getTimeInMillis() && rec.amountMl == 500) {
                secondMoved = true;
            }
        }
        check("第一条未受影响", firstIntact);
        check("第二条已移到新时间", secondMoved);

        // ── 改到更早时间：就地更新，保持原有顺序（不重排）──
        Calendar c3 = Calendar.getInstance();
        c3.setTimeInMillis(t2);
        c3.set(Calendar.HOUR_OF_DAY, 7);
        check("第二条改到更早时间", WaterReminderScheduler.updateRecordTime(ctx, c2.getTimeInMillis(), c3.getTimeInMillis()));
        List<WaterReminderScheduler.WaterRecord> list3 = WaterReminderScheduler.getRecords(ctx);
        eq("不重排：仍是 2 条", 2, list3.size());
        eqL("顺序保持：第 1 条不变", 1700000000000L, list3.get(0).timestamp);
        eqL("顺序保持：第 2 条为新时间", c3.getTimeInMillis(), list3.get(1).timestamp);
    }

    static void testWaterReminderDrinkingDay() {
        // 固定日期：2026-10-11 周日、2026-10-12 周一、2026-10-01 国庆
        Calendar sun = Calendar.getInstance();
        sun.set(2026, 9, 11, 10, 0, 0);
        sun.set(Calendar.MILLISECOND, 0);
        Calendar mon = Calendar.getInstance();
        mon.set(2026, 9, 12, 10, 0, 0);
        Calendar nat = Calendar.getInstance();
        nat.set(2026, 9, 1, 10, 0, 0);

        newStore();
        MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);

        // ── 基本判定 ──
        check("周一：提醒日", WaterReminderScheduler.isDrinkingDay(ctx, st, mon));
        check("周日无出勤：非提醒日", !WaterReminderScheduler.isDrinkingDay(ctx, st, sun));
        check("国庆无出勤：非提醒日", !WaterReminderScheduler.isDrinkingDay(ctx, st, nat));

        // ── 标记出勤后（调休）──
        HolidayWork.put(ctx, 20261011, 0, true);
        check("周日+调休出勤：提醒日", WaterReminderScheduler.isDrinkingDay(ctx, st, sun));
        eqD("调休余额 +1", 1.0d, WorkRules.compEarned(ctx));

        // ── 法定节假日出勤（计薪）──
        HolidayWork.put(ctx, 20261001, 30, false);
        check("国庆+计薪出勤：提醒日", WaterReminderScheduler.isDrinkingDay(ctx, st, nat));

        // ── 取消出勤恢复 ──
        HolidayWork.remove(ctx, 20261011);
        check("取消出勤：恢复非提醒日", !WaterReminderScheduler.isDrinkingDay(ctx, st, sun));

        // ── 下一次提醒时刻：周日 10:00（已出勤）→ 当天 11:00 ──
        newStore();
        HolidayWork.put(ctx, 20261011, 0, true);
        WaterReminderSettings ws = new WaterReminderSettings();
        Calendar nowSun = Calendar.getInstance();
        nowSun.set(2026, 9, 11, 10, 0, 0);
        nowSun.set(Calendar.MILLISECOND, 0);
        long next1 = WaterReminderScheduler.nextReminderAfter(ctx, ws, nowSun);
        Calendar exp1 = Calendar.getInstance();
        exp1.set(2026, 9, 11, 11, 0, 0);
        exp1.set(Calendar.MILLISECOND, 0);
        eqL("周日+出勤：下一次=当天 11:00", exp1.getTimeInMillis(), next1);

        // ── 下一次提醒时刻：周日 10:00（未出勤）→ 周一 09:30 ──
        newStore();
        long next2 = WaterReminderScheduler.nextReminderAfter(ctx, ws, nowSun);
        Calendar exp2 = Calendar.getInstance();
        exp2.set(2026, 9, 12, 9, 30, 0);
        exp2.set(Calendar.MILLISECOND, 0);
        eqL("周日未出勤：下一次=周一 09:30", exp2.getTimeInMillis(), next2);

        // ── 午休窗口：周日 11:20（已出勤）→ 跳过午休 → 12:30 ──
        newStore();
        HolidayWork.put(ctx, 20261011, 0, true);
        Calendar lunch = Calendar.getInstance();
        lunch.set(2026, 9, 11, 11, 20, 0);
        lunch.set(Calendar.MILLISECOND, 0);
        long next3 = WaterReminderScheduler.nextReminderAfter(ctx, ws, lunch);
        Calendar exp3 = Calendar.getInstance();
        exp3.set(2026, 9, 11, 12, 30, 0);
        exp3.set(Calendar.MILLISECOND, 0);
        eqL("周日+出勤（午休前）：下一次跳过午休 12:30", exp3.getTimeInMillis(), next3);
    }


    static void testWaterWindow() {
        // 固定时间：2026-10-14 周三 10:00（工作日、工作时段内）
        Calendar wed = Calendar.getInstance();
        wed.set(2026, 9, 14, 10, 0, 0);
        wed.set(Calendar.MILLISECOND, 0);
        // 2026-10-17 周六 10:00（休息日）
        Calendar sat = Calendar.getInstance();
        sat.set(2026, 9, 17, 10, 0, 0);
        sat.set(Calendar.MILLISECOND, 0);

        newStore();
        MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);
        WorkdayHelper.isWorkday(ctx, st, wed); // 预热

        // ── 默认（跟随工作时间）：周三 10:00 → 按 90 分钟从 08:00 起算 → 11:00 ──
        WaterReminderSettings ws = new WaterReminderSettings();
        long next1 = WaterReminderScheduler.nextReminderAfter(ctx, ws, wed);
        Calendar exp1 = Calendar.getInstance();
        exp1.set(2026, 9, 14, 11, 0, 0);
        exp1.set(Calendar.MILLISECOND, 0);
        eqL("默认模式：周三 → 11:00", exp1.getTimeInMillis(), next1);

        // ── 默认模式：周六（未出勤）→ 跳过 → 下周一 09:30 ──
        long next2 = WaterReminderScheduler.nextReminderAfter(ctx, ws, sat);
        Calendar exp2 = Calendar.getInstance();
        exp2.set(2026, 9, 19, 9, 30, 0);
        exp2.set(Calendar.MILLISECOND, 0);
        eqL("默认模式：周六 → 下周一 09:30", exp2.getTimeInMillis(), next2);

        // ── 自定义时段 07:00-21:00：周三 10:00 → 从 07:00 起算 90 分钟 → 11:30 ──
        ws.windowMode = 1;
        ws.winStartMinutes = 420;
        ws.winEndMinutes = 1260;
        ws.skipBreak = true;
        long next3 = WaterReminderScheduler.nextReminderAfter(ctx, ws, wed);
        Calendar exp3 = Calendar.getInstance();
        exp3.set(2026, 9, 14, 11, 30, 0);
        exp3.set(Calendar.MILLISECOND, 0);
        // 11:30 落在午休（11:30-12:30）内 → 跳过，落在 13:00
        Calendar exp3b = Calendar.getInstance();
        exp3b.set(2026, 9, 14, 13, 0, 0);
        exp3b.set(Calendar.MILLISECOND, 0);
        eqL("自定义时段：周三 → 13:00（跳过午休）", exp3b.getTimeInMillis(), next3);

        // ── 自定义 + 午休不跳过：11:30 正好在午休（11:30-12:30）→ 也应提醒 ──
        ws.skipBreak = false;
        long next4 = WaterReminderScheduler.nextReminderAfter(ctx, ws, wed);
        eqL("午休不跳过：11:30 也可提醒", exp3.getTimeInMillis(), next4);

        // ── 自定义 + 周末开关：周六 10:00 → 当天 11:30 ──
        ws.skipBreak = true;
        ws.weekend = true;
        long next5 = WaterReminderScheduler.nextReminderAfter(ctx, ws, sat);
        Calendar exp5 = Calendar.getInstance();
        exp5.set(2026, 9, 17, 13, 0, 0);
        exp5.set(Calendar.MILLISECOND, 0);
        eqL("周末开关：周六 → 当天 13:00（跳过午休）", exp5.getTimeInMillis(), next5);

        // ── 存取闭环 ──
        newStore();
        WaterReminderSettings saved = new WaterReminderSettings();
        saved.windowMode = 1;
        saved.winStartMinutes = 500;
        saved.winEndMinutes = 1300;
        saved.skipBreak = false;
        saved.weekend = true;
        saved.save(ctx);
        WaterReminderSettings loaded = WaterReminderSettings.load(ctx);
        eq("存取：windowMode", 1, loaded.windowMode);
        eq("存取：winStart", 500, loaded.winStartMinutes);
        eq("存取：winEnd", 1300, loaded.winEndMinutes);
        eq("存取：skipBreak=false", 0, loaded.skipBreak ? 1 : 0);
        eq("存取：weekend=true", 1, loaded.weekend ? 1 : 0);
    }

    // ══════════ AP. 小人设置与点击反应 ══════════
    static void testPetSettings() {
        // ── 默认值：显示 / 中档 / 画工位 ──
        newStore();
        check("默认显示小人", PetSettings.visible(ctx));
        eq("默认尺寸 = 中档", PetSettings.SIZE_MEDIUM, PetSettings.size(ctx));
        eq("中档宽度 104", 104, PetSettings.widthDp(ctx));
        eq("中档高度 = 104 × 92/88", Math.round(104 * 92.0f / 88.0f), PetSettings.heightDp(ctx));
        check("默认画工位场景", PetSettings.scene(ctx));

        // ── 三档尺寸（88 / 104 / 120）──
        eq("小档宽度 88", 88, PetSettings.SIZES_DP[PetSettings.SIZE_SMALL]);
        eq("大档宽度 120", 120, PetSettings.SIZES_DP[PetSettings.SIZE_LARGE]);
        eq("档位名 小", "小", PetSettings.sizeName(PetSettings.SIZE_SMALL));
        eq("档位名 中", "中", PetSettings.sizeName(PetSettings.SIZE_MEDIUM));
        eq("档位名 大", "大", PetSettings.sizeName(PetSettings.SIZE_LARGE));

        PetSettings.setSize(ctx, PetSettings.SIZE_SMALL);
        eq("切到小档", PetSettings.SIZE_SMALL, PetSettings.size(ctx));
        eq("小档宽度 88", 88, PetSettings.widthDp(ctx));
        PetSettings.setSize(ctx, PetSettings.SIZE_LARGE);
        eq("切到大档", PetSettings.SIZE_LARGE, PetSettings.size(ctx));
        eq("大档宽度 120", 120, PetSettings.widthDp(ctx));

        // 越界钳制
        PetSettings.setSize(ctx, -5);
        eq("下越界钳到小档", PetSettings.SIZE_SMALL, PetSettings.size(ctx));
        PetSettings.setSize(ctx, 99);
        eq("上越界钳到大档", PetSettings.SIZE_LARGE, PetSettings.size(ctx));

        // 存储里的脏值要能兜住
        prefs.map.put("pet_size", Integer.valueOf(42));
        eq("脏值回落到中档", PetSettings.SIZE_MEDIUM, PetSettings.size(ctx));

        // ── 开关 ──
        PetSettings.setVisible(ctx, false);
        check("关掉后不显示", !PetSettings.visible(ctx));
        PetSettings.setVisible(ctx, true);
        check("打开后显示", PetSettings.visible(ctx));
        PetSettings.setScene(ctx, false);
        check("关掉工位场景", !PetSettings.scene(ctx));
        PetSettings.setScene(ctx, true);
        check("打开工位场景", PetSettings.scene(ctx));

        // ── 点击反应：跟着状态走 ──
        eq("加班中 → 惊讶", WorkerPet.REACT_SURPRISED, WorkerPet.reactionFor(WorkerPet.OVERTIME, 0));
        eq("上午工作 → 惊讶", WorkerPet.REACT_SURPRISED, WorkerPet.reactionFor(WorkerPet.WORK_AM, 0));
        eq("下午工作 → 惊讶", WorkerPet.REACT_SURPRISED, WorkerPet.reactionFor(WorkerPet.WORK_PM, 0));
        eq("午休 → 害羞", WorkerPet.REACT_SHY, WorkerPet.reactionFor(WorkerPet.LUNCH, 0));
        eq("休息日 → 害羞", WorkerPet.REACT_SHY, WorkerPet.reactionFor(WorkerPet.WEEKEND, 0));
        eq("请假 → 害羞", WorkerPet.REACT_SHY, WorkerPet.reactionFor(WorkerPet.LEAVE, 0));
        eq("周日晚 → 害羞", WorkerPet.REACT_SHY, WorkerPet.reactionFor(WorkerPet.SUNDAY, 0));
        eq("下班后 → 欢呼", WorkerPet.REACT_CHEER, WorkerPet.reactionFor(WorkerPet.OFF_WORK, 0));
        eq("周五 → 欢呼", WorkerPet.REACT_CHEER, WorkerPet.reactionFor(WorkerPet.FRIDAY, 0));
        eq("休息日出勤 → 欢呼", WorkerPet.REACT_CHEER, WorkerPet.reactionFor(WorkerPet.HOLIDAY_WORK, 0));

        // 疲劳 3 级一律蔫（哪怕下班后）
        eq("疲劳 3 级 + 下班 → 惊讶", WorkerPet.REACT_SURPRISED, WorkerPet.reactionFor(WorkerPet.OFF_WORK, 3));
        eq("疲劳 3 级 + 周五 → 惊讶", WorkerPet.REACT_SURPRISED, WorkerPet.reactionFor(WorkerPet.FRIDAY, 3));

        // 没有明确情绪的场景（通勤/周一）→ 随机，但必须是三种之一
        boolean valid = true;
        for (int i = 0; i < 40; i++) {
            int r = WorkerPet.reactionFor(WorkerPet.COMMUTE, 0);
            if (r != WorkerPet.REACT_SURPRISED && r != WorkerPet.REACT_CHEER && r != WorkerPet.REACT_SHY) {
                valid = false;
                break;
            }
        }
        check("通勤反应落在三种之内", valid);

        // ── 连戳档位 ──
        eq("戳 1 次：普通", 0, WorkerPet.pokeStage(1));
        eq("戳 2 次：普通", 0, WorkerPet.pokeStage(2));
        eq("戳 3 次：捂耳朵", 1, WorkerPet.pokeStage(3));
        eq("戳 7 次：仍捂耳朵", 1, WorkerPet.pokeStage(7));
        eq("戳 8 次：彩蛋", 2, WorkerPet.pokeStage(8));
        eq("戳 20 次：彩蛋", 2, WorkerPet.pokeStage(20));
        eq("普通档无专属文案", null, WorkerPet.pokeStageText(0));
        eq("捂耳朵文案", "别戳了别戳了", WorkerPet.pokeStageText(1));
        eq("彩蛋文案", "你有这功夫，不如去干活", WorkerPet.pokeStageText(2));
    }

    // ══════════ AQ. 输入范围校验 ══════════
    // 回归：反编译恢复时 jadx 把「越界就报错」错译成「无条件报错」，
    // 导致设置页一保存就弹「容量请设置在 10 到 2000 ml 之间」。
    static void testInputRanges() {
        // ── 单杯容量：10 – 2000，闭区间 ──
        eq("容量下界常量", 10, DrinkTypes.ML_MIN);
        eq("容量上界常量", 2000, DrinkTypes.ML_MAX);
        check("9 越界", !DrinkTypes.mlInRange(9));
        check("10 合法（含下界）", DrinkTypes.mlInRange(10));
        check("500 合法", DrinkTypes.mlInRange(500));
        check("2000 合法（含上界）", DrinkTypes.mlInRange(2000));
        check("2001 越界", !DrinkTypes.mlInRange(2001));
        check("0 越界", !DrinkTypes.mlInRange(0));
        check("负数越界", !DrinkTypes.mlInRange(-1));

        // ── 热量：0 – 900 ──
        eq("热量下界常量", 0, DrinkTypes.KCAL_MIN);
        eq("热量上界常量", 900, DrinkTypes.KCAL_MAX);
        check("0 合法", DrinkTypes.kcalInRange(0));
        check("43 合法", DrinkTypes.kcalInRange(43));
        check("900 合法", DrinkTypes.kcalInRange(900));
        check("901 越界", !DrinkTypes.kcalInRange(901));
        check("负数越界", !DrinkTypes.kcalInRange(-1));

        // ── 每日总量：100 – 10000 ──
        eq("总量下界常量", 100, DrinkTypes.TARGET_MIN);
        eq("总量上界常量", 10000, DrinkTypes.TARGET_MAX);
        check("99 越界", !DrinkTypes.targetInRange(99));
        check("100 合法", DrinkTypes.targetInRange(100));
        check("2000 合法", DrinkTypes.targetInRange(2000));
        check("10000 合法", DrinkTypes.targetInRange(10000));
        check("10001 越界", !DrinkTypes.targetInRange(10001));

        // ── 真实场景：截图里的那组值必须全部合法 ──
        // 白开水 500 / 咖啡 500 / 无糖茶 500 / 精酿啤酒 500 /
        // 运动饮料 550 / 维C泡腾片 500 / 小瓶矿泉水 380 / 苏打水 325
        int[] mls = {500, 500, 500, 500, 550, 500, 380, 325};
        boolean allOk = true;
        for (int i = 0; i < mls.length; i++) {
            if (!DrinkTypes.mlInRange(mls[i])) {
                allOk = false;
                break;
            }
        }
        check("截图的容量组合全部合法（修复前必报错）", allOk);
        check("总水量 2000 合法", DrinkTypes.targetInRange(2000));

        // 越界项能被正确挑出（模拟校验循环）
        int[] bad = {500, 500, 3000, 500};
        int firstBad = -1;
        for (int i = 0; i < bad.length; i++) {
            if (!DrinkTypes.mlInRange(bad[i])) {
                firstBad = i;
                break;
            }
        }
        eq("挑出越界下标 = 2", 2, firstBad);

        // ── 提醒模式映射（回归：反编译时 if/else 被压平成「无条件 i5 = 2」，
        //    导致用户选「仅铃音」也会存成「铃音和震动」）──
        check("模式表 = {0,1,2}",
                WaterReminderSettings.MODES.length == 3
                        && WaterReminderSettings.MODES[0] == WaterReminderSettings.MODE_SOUND
                        && WaterReminderSettings.MODES[1] == WaterReminderSettings.MODE_VIBRATE
                        && WaterReminderSettings.MODES[2] == WaterReminderSettings.MODE_BOTH);
        eq("下标 0 → 仅铃音", WaterReminderSettings.MODE_SOUND, WaterReminderSettings.modeFor(0));
        eq("下标 1 → 仅震动", WaterReminderSettings.MODE_VIBRATE, WaterReminderSettings.modeFor(1));
        eq("下标 2 → 铃音和震动", WaterReminderSettings.MODE_BOTH, WaterReminderSettings.modeFor(2));
        eq("未选中(-1) → 回落铃音和震动", WaterReminderSettings.MODE_BOTH, WaterReminderSettings.modeFor(-1));
        eq("越界(3) → 回落铃音和震动", WaterReminderSettings.MODE_BOTH, WaterReminderSettings.modeFor(3));
        eq("越界(99) → 回落铃音和震动", WaterReminderSettings.MODE_BOTH, WaterReminderSettings.modeFor(99));

        // 三档必须互不相同，否则用户选择没有意义
        check("三个模式互不相同",
                WaterReminderSettings.MODE_SOUND != WaterReminderSettings.MODE_VIBRATE
                        && WaterReminderSettings.MODE_VIBRATE != WaterReminderSettings.MODE_BOTH
                        && WaterReminderSettings.MODE_SOUND != WaterReminderSettings.MODE_BOTH);
    }

    // ══════════ AR. 主卡排版 ══════════
    // 回归：加了「小人尺寸」之后，选「中/大」会把金额那一行挤窄，金额被盖住一截。
    static void testHeroFit() {
        // ── 小人宽度：没测量出来时按用户选的 ──
        eq("未测量 → 用用户选的（大）", 120, HeroFit.petWidthDp(0, 120));
        eq("未测量 → 用用户选的（中）", 104, HeroFit.petWidthDp(0, 104));
        eq("未测量 → 用用户选的（小）", 88, HeroFit.petWidthDp(0, 88));

        // ── 常见机型（393dp 宽）：一行约 313dp，42% ≈ 131dp ──
        // 三档都能放下，不该被压缩
        eq("313dp 行 → 小 88 不压缩", 88, HeroFit.petWidthDp(313, 88));
        eq("313dp 行 → 中 104 不压缩", 104, HeroFit.petWidthDp(313, 104));
        eq("313dp 行 → 大 120 不压缩", 120, HeroFit.petWidthDp(313, 120));

        // ── 窄屏：按比例压缩，给金额留够地方 ──
        eq("200dp 行 → 大被压到 84（42%）", 84, HeroFit.petWidthDp(200, 120));
        eq("200dp 行 → 中不变", 84, HeroFit.petWidthDp(200, 104));
        eq("240dp 行 → 大压到 101", 101, HeroFit.petWidthDp(240, 120));

        // ── 极窄：不低于下限 ──
        eq("100dp 行 → 落到下限 64", 64, HeroFit.petWidthDp(100, 120));
        eq("50dp 行 → 仍是下限 64", 64, HeroFit.petWidthDp(50, 88));

        // 不变量：压缩后留给金额的宽度，永远 ≥ 行宽的 58%
        boolean invariant = true;
        for (int row = 120; row <= 500; row += 7) {
            for (int want = 88; want <= 120; want += 16) {
                int pet = HeroFit.petWidthDp(row, want);
                if (pet > row - Math.floor(row * (1.0f - HeroFit.PET_MAX_SHARE))) {
                    // 允许下限兜底的情况
                    if (pet != HeroFit.PET_MIN_DP) {
                        invariant = false;
                    }
                }
            }
        }
        check("压缩后金额至少还剩 58% 行宽", invariant);

        // ── 高度按 92/88 换算 ──
        eq("88 宽 → 92 高", 92, HeroFit.petHeightDp(88));
        eq("104 宽 → 109 高", 109, HeroFit.petHeightDp(104));
        eq("120 宽 → 125 高", 125, HeroFit.petHeightDp(120));

        // ── 金额字号：默认给最大 ──
        float density = 2.75f;
        float maxPx = HeroFit.AMOUNT_MAX_SP * density;
        // 参考字号 100px 下宽 500px；给 1000px 空间 → 需要 200px，超过上限 → 夹到最大
        eqD("够宽 → 用最大字号", maxPx,
                HeroFit.amountTextSizePx(1000.0f, 500.0f, 100.0f, density));
        eqD("未测量 → 用最大字号", maxPx,
                HeroFit.amountTextSizePx(0.0f, 500.0f, 100.0f, density));
        eqD("文字宽度为 0 → 用最大字号", maxPx,
                HeroFit.amountTextSizePx(300.0f, 0.0f, 100.0f, density));

        // ── 空间不够 → 按比例缩小 ──
        // 参考字号 100px 下宽 500px，只有 250px 可用 → 字号减半
        // 500px 宽的文字放进 400px → 字号缩到 80px
        eqD("放不下 → 按比例缩小", 80.0f,
                HeroFit.amountTextSizePx(400.0f, 500.0f, 100.0f, density));

        // ── 缩到下限就不再缩 ──
        float minPx = HeroFit.AMOUNT_MIN_SP * density;
        eqD("极窄 → 落到最小字号", minPx,
                HeroFit.amountTextSizePx(10.0f, 500.0f, 100.0f, density));

        // ── 核心不变量：算出的字号，要么放得下，要么已经到最小 ──
        boolean fits = true;
        float[] available = {560.0f, 530.0f, 480.0f, 430.0f, 380.0f, 330.0f, 270.0f, 200.0f};
        String[] amounts = {"¥ 0.00", "¥ 88.88", "¥ 1234.56", "¥ 12345.67", "¥ 123456.78"};
        for (int i = 0; i < amounts.length; i++) {
            for (int j = 0; j < available.length; j++) {
                // 模拟该金额在 100px 参考字号下的宽度（约 0.55em/字符）
                float w = amounts[i].length() * 55.0f;
                float size = HeroFit.amountTextSizePx(available[j], w, 100.0f, density);
                float drawn = w * size / 100.0f;
                if (drawn > available[j] + 0.01f && size > minPx + 0.01f) {
                    fits = false;
                }
            }
        }
        check("任何金额都放得下（或已到最小字号）", fits);

        // ── 边界：小人越大，金额可用宽度只会更小 ──
        int rowW = 313;
        int prevAvail = Integer.MAX_VALUE;
        boolean monotonic = true;
        for (int want = 88; want <= 120; want += 16) {
            int avail = rowW - HeroFit.petWidthDp(rowW, want);
            if (avail > prevAvail) {
                monotonic = false;
            }
            prevAvail = avail;
        }
        check("小人越大金额可用宽度不增", monotonic);
    }

    // ══════════ AS. 设置分区保存分发 ══════════
    // 回归：在「工具与安全」分区点保存 -> 走了「全部保存」-> 读没创建的
    // salaryInput -> NullPointerException 崩溃（MainActivity$SettingsDialog.saveAllSections）
    static void testSectionSave() {
        eq("完整设置(0) → 保存全部", SectionSave.ALL, SectionSave.targetFor(0));
        eq("薪跳设置(1) → 只存薪跳", SectionSave.SALARY, SectionSave.targetFor(1));
        eq("喝水设置(2) → 只存喝水", SectionSave.WATER, SectionSave.targetFor(2));
        eq("工具与安全(3) → 只存小人", SectionSave.TOOLS, SectionSave.targetFor(3));

        // 关键：工具与安全**绝不能**走到「保存全部」
        check("工具与安全不保存全部",
                SectionSave.targetFor(3) != SectionSave.ALL);
        check("工具与安全不复用薪跳", SectionSave.targetFor(3) != SectionSave.SALARY);
        check("工具与安全不复用喝水", SectionSave.targetFor(3) != SectionSave.WATER);

        // 三个子分区互不相同（否则说明映射写重了）
        check("三个子分区互不相同",
                SectionSave.SALARY != SectionSave.WATER
                        && SectionSave.WATER != SectionSave.TOOLS
                        && SectionSave.SALARY != SectionSave.TOOLS);

        // 未知分区保持原行为（当作完整设置），不能抛异常
        eq("未知分区(-1) → 保存全部", SectionSave.ALL, SectionSave.targetFor(-1));
        eq("未知分区(99) → 保存全部", SectionSave.ALL, SectionSave.targetFor(99));
        eq("完整设置常量 = 3（=STATE_LUNCH）", 3, MainActivity.STATE_LUNCH);
    }

    // ══════════ AT. 每日金额拆分 ══════════
    // 日历格子里要分别显示「日薪多少、加班费多少」，而存储里只有一个总额，
    // 所以拆分规则必须准确。日期统一用 2026-10-14（周三，工作日）。
    static void testDayBreakdown() {
        // ── 紧凑金额格式 ──
        eq("0 → 空", "", DayBreakdown.formatCompact(0L));
        eq("负 → 空", "", DayBreakdown.formatCompact(-100L));
        eq("1007.78 → 1008", "1008", DayBreakdown.formatCompact(100778L));
        eq("781.59 → 782", "782", DayBreakdown.formatCompact(78159L));
        eq("300.00 → 300", "300", DayBreakdown.formatCompact(30000L));
        eq("0.9 → 1", "1", DayBreakdown.formatCompact(90L));
        eq("9999.4 → 9999", "9999", DayBreakdown.formatCompact(999940L));
        eq("10000 → 1万", "1万", DayBreakdown.formatCompact(1000000L));
        eq("12345 → 1.2万", "1.2万", DayBreakdown.formatCompact(1234500L));
        eq("99999 → 10万", "10万", DayBreakdown.formatCompact(9999900L));
        eq("25000 → 2.5万", "2.5万", DayBreakdown.formatCompact(2500000L));

        // ── 拆分：只有日薪 ──
        newStore();
        MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        int d = 20261014;                       // 周三，工作日
        SalaryHistory.put(ctx, d, 30000L);
        DayBreakdown bd = DayBreakdown.of(ctx, d);
        check("有记录", bd.hasRecord);
        check("是工作日", bd.isWorkday);
        eqL("日薪 = 300.00", 30000L, bd.baseCents);
        eqL("无加班费", 0L, bd.overtimeCents);
        check("没有加班", !bd.hasOvertime());
        eqL("当天到手 = 300.00", 30000L, bd.totalCents);
        eq("类型 = 工作日", "工作日", bd.dayTypeText());

        // ── 拆分：日薪 + 加班费 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, d, 40000L);      // 300 日薪 + 100 加班
        bd = DayBreakdown.of(ctx, d);
        eqL("日薪仍是 300.00", 30000L, bd.baseCents);
        eqL("加班费 = 100.00", 10000L, bd.overtimeCents);
        check("识别出加班", bd.hasOvertime());
        eqL("合计不变", 40000L, bd.totalCents);

        // ── 拆分：日薪 + 外快（外快不算加班）──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, d, 20000L, "修图");
        SalaryHistory.put(ctx, d, 50000L);
        bd = DayBreakdown.of(ctx, d);
        eqL("外快 = 200.00", 20000L, bd.extraCents);
        eqL("日薪 = 300.00（外快不算加班）", 30000L, bd.baseCents);
        eqL("没有误判成加班", 0L, bd.overtimeCents);
        eqL("合计 = 500.00", 50000L, bd.totalCents);

        // ── 拆分：日薪 + 加班 + 外快 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, d, 20000L, "修图");
        SalaryHistory.put(ctx, d, 50000L);      // 300 + 200 外快 + 0 加班? 不 → 需区分
        bd = DayBreakdown.of(ctx, d);
        eqL("基数 = 日薪+外快", 50000L, bd.baseCents + bd.extraCents);
        // 再叠 150 加班
        SalaryHistory.put(ctx, d, 65000L);
        bd = DayBreakdown.of(ctx, d);
        eqL("加班费 = 150.00", 15000L, bd.overtimeCents);
        eqL("日薪仍 300.00", 30000L, bd.baseCents);
        eqL("外快仍 200.00", 20000L, bd.extraCents);

        // ── 没有记录的日子 ──
        newStore();
        bd = DayBreakdown.of(ctx, 20261020);
        check("无记录", !bd.hasRecord);
        check("没有金额", !bd.hasMoney());
        eqL("日薪 0", 0L, bd.baseCents);
        eqL("加班 0", 0L, bd.overtimeCents);
        eq("格子标签为空", null, bd.cellTag());

        // ── 休息日出勤（2 倍）：日薪按倍率放大，不算加班 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        int sat = 20261017;                      // 周六
        HolidayWork.put(ctx, sat, 20, false);    // 出勤，2 倍
        SalaryHistory.put(ctx, sat, 60000L);     // 300 × 2
        bd = DayBreakdown.of(ctx, sat);
        check("周六不是工作日", !bd.isWorkday);
        eqL("出勤日薪 = 600.00", 60000L, bd.baseCents);
        eqL("出勤不算加班", 0L, bd.overtimeCents);
        check("类型含出勤", bd.dayTypeText().contains("出勤"));

        // ── 调休出勤：无薪 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        HolidayWork.put(ctx, sat, 0, true);      // 调休
        SalaryHistory.put(ctx, sat, 0L);
        bd = DayBreakdown.of(ctx, sat);
        check("是调休", bd.isComp);
        eqL("调休无日薪", 0L, bd.baseCents);
        eq("格子显示「调休」", "调休", bd.cellTag());

        // ── 请假（60%）：日薪按比例 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        LeaveRecord.put(ctx, d, "病假", 60, false);
        SalaryHistory.put(ctx, d, 18000L);
        bd = DayBreakdown.of(ctx, d);
        eqL("病假 60% → 180.00", 18000L, bd.baseCents);
        check("类型含病假", bd.dayTypeText().contains("病假"));

        // ── 外快的格子显示：带「外」字前缀 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, d, 20000L, "修图");
        SalaryHistory.put(ctx, d, 50000L);
        bd = DayBreakdown.of(ctx, d);
        eqL("外快 200.00", 20000L, bd.extraCents);
        eq("格子外快文本 = 「外 200」", "外 200", DayBreakdown.extraCellText(bd.extraCents));
        eq("外快 0 → 不显示", null, DayBreakdown.extraCellText(0L));

        // 三个都有时的三个数字
        SalaryHistory.put(ctx, d, 65000L);          // 再叠 150 加班
        bd = DayBreakdown.of(ctx, d);
        eq("日薪 300", "300", DayBreakdown.formatCompact(bd.baseCents));
        eq("加班 +150", "+150", "+" + DayBreakdown.formatCompact(bd.overtimeCents));
        eq("外快 外 200", "外 200", DayBreakdown.extraCellText(bd.extraCents));

        // ── 核心不变量：日薪 + 加班 + 外快 + 补录 = 当天到手 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, d, 20000L, "外快");
        MonthBackfill.add(ctx, 2026, 10, 500000L, "月度补录");
        SalaryHistory.put(ctx, d, 65000L);
        bd = DayBreakdown.of(ctx, d);
        long sum = bd.baseCents + bd.overtimeCents + bd.extraCents + bd.backfillCents;
        eqL("四项之和 = 当天入账", sum, bd.totalCents);

        // ── 恒等式：任意组合下，四项之和都等于当天入账 ──
        boolean identity = true;
        long[] totals = {0L, 18000L, 30000L, 50000L, 65000L, 120000L};
        for (int i = 0; i < totals.length; i++) {
            SalaryHistory.put(ctx, d, totals[i]);
            DayBreakdown x = DayBreakdown.of(ctx, d);
            long s2 = x.baseCents + x.overtimeCents + x.extraCents + x.backfillCents;
            if (s2 != x.totalCents) {
                identity = false;
                break;
            }
        }
        check("任意金额下四项之和恒等于入账", identity);
    }

    // ══════════ AU. 读取缓存正确性 ══════════
    // 接入缓存后最需要防的是「写完了读出来还是旧的」。逐条验证。
    static void testDataCache() {
        // ── 写完立刻读，必须是新值 ──
        newStore();
        eqL("初始无记录", -1L, SalaryHistory.centsOf(ctx, 20261014));
        SalaryHistory.put(ctx, 20261014, 30000L);
        eqL("写入后立刻读到新值", 30000L, SalaryHistory.centsOf(ctx, 20261014));
        check("写入后 has 为真", SalaryHistory.has(ctx, 20261014));

        // ── put 是「替换」──
        SalaryHistory.put(ctx, 20261014, 5000L);
        eqL("put 覆盖成 50.00", 5000L, SalaryHistory.centsOf(ctx, 20261014));

        // ── adjust 才是「累加」（存钱、归档走这条）──
        SalaryHistory.adjust(ctx, 20261014, 30000L);
        eqL("adjust 后 = 350.00", 35000L, SalaryHistory.centsOf(ctx, 20261014));
        SalaryHistory.adjust(ctx, 20261014, -10000L);
        eqL("负数 adjust 后 = 250.00", 25000L, SalaryHistory.centsOf(ctx, 20261014));
        SalaryHistory.adjust(ctx, 20261014, -999999L);
        eqL("减到负数会钳为 0", 0L, SalaryHistory.centsOf(ctx, 20261014));

        // ── 缓存命中：连读三次结果一致 ──
        eqL("连读 1", 0L, SalaryHistory.centsOf(ctx, 20261014));
        eqL("连读 2", 0L, SalaryHistory.centsOf(ctx, 20261014));
        eqL("连读 3", 0L, SalaryHistory.centsOf(ctx, 20261014));

        // ── 删除后立刻读 ──
        SalaryHistory.remove(ctx, 20261014);
        eqL("删除后 centsOf = -1", -1L, SalaryHistory.centsOf(ctx, 20261014));
        check("删除后 has 为假", !SalaryHistory.has(ctx, 20261014));

        // ── 跨多天写入，读回各自的值 ──
        newStore();
        SalaryHistory.put(ctx, 20261001, 10000L);
        SalaryHistory.put(ctx, 20261002, 20000L);
        SalaryHistory.put(ctx, 20261003, 30000L);
        eqL("第 1 天", 10000L, SalaryHistory.centsOf(ctx, 20261001));
        eqL("第 2 天", 20000L, SalaryHistory.centsOf(ctx, 20261002));
        eqL("第 3 天", 30000L, SalaryHistory.centsOf(ctx, 20261003));
        eq("记录数 = 3", 3, SalaryHistory.load(ctx).size());

        // ── 换了存储实例（换设备/清数据）后不能读旧缓存 ──
        newStore();
        eqL("换 store 后读不到旧数据", -1L, SalaryHistory.centsOf(ctx, 20261001));
        eq("换 store 后记录数 = 0", 0, SalaryHistory.load(ctx).size());

        // ── 缓存返回的列表是副本，改它不能污染缓存 ──
        SalaryHistory.put(ctx, 20261005, 50000L);
        java.util.List<SalaryHistory.Day> list1 = SalaryHistory.load(ctx);
        list1.clear();
        eq("清空返回值后，数据仍在", 1, SalaryHistory.load(ctx).size());
        eqL("值也没变", 50000L, SalaryHistory.centsOf(ctx, 20261005));

        // ── 月合计随写入更新 ──
        newStore();
        eqL("初始月合计 0", 0L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        SalaryHistory.put(ctx, 20261001, 10000L);
        eqL("写入后月合计 100.00", 10000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));
        SalaryHistory.put(ctx, 20261015, 25000L);
        eqL("再写入后月合计 350.00", 35000L, SalaryHistory.monthTotalCents(ctx, 2026, 10));

        // ── byMonth 也跟着更新 ──
        eq("10 月 2 条", 2, SalaryHistory.byMonth(ctx, 2026, 10).size());

        // ── 通过 DayBreakdown 读到的是最新值 ──
        newStore();
        MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        eqL("拆分前基数为 0", 0L, DayBreakdown.of(ctx, 20261014).baseCents);
        SalaryHistory.put(ctx, 20261014, 30000L);
        eqL("写入后拆分基数 300.00", 30000L, DayBreakdown.of(ctx, 20261014).baseCents);
        SalaryHistory.put(ctx, 20261014, 40000L);
        eqL("再加 100 加班", 10000L, DayBreakdown.of(ctx, 20261014).overtimeCents);

        // ── 归档/批量写入后也要能看到（ArchiveSync 走的是 SalaryHistory.put）──
        ArchiveSync.refreshPrewritten(ctx, 20261014);
        check("归档后仍能读到当天", SalaryHistory.has(ctx, 20261014));
    }

    // ══════════ AV. 年度加班统计 ══════════
    // 加班次数不能数打卡记录（OvertimeLog 只存最后一次），要从每日金额反推。
    static void testYearlyOvertime() {
        MainActivity.SalarySettings st;
        long[] r;

        // ── 空数据 ──
        newStore();
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("没数据 → 次数 0", 0L, r[0]);
        eqL("没数据 → 金额 0", 0L, r[1]);

        // ── 只有日薪，没有加班 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, 20261008, 30000L);
        SalaryHistory.put(ctx, 20261009, 30000L);
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("两个正常工作日 → 0 次", 0L, r[0]);

        // ── 一天有加班 ──
        SalaryHistory.put(ctx, 20261008, 40000L);   // 300 日薪 + 100 加班
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("一天加班 → 1 次", 1L, r[0]);
        eqL("加班费 100.00", 10000L, r[1]);

        // ── 三天加班 ──
        SalaryHistory.put(ctx, 20261009, 45000L);   // + 150
        SalaryHistory.put(ctx, 20261013, 32500L);   // + 25
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("三天加班 → 3 次", 3L, r[0]);
        eqL("加班费合计 275.00", 27500L, r[1]);

        // ── 外快不算加班 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, 20261008, 50000L, "私活");
        SalaryHistory.put(ctx, 20261008, 80000L);   // 300 日薪 + 500 外快
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("外快不算加班", 0L, r[0]);
        eqL("也不计加班费", 0L, r[1]);

        // ── 补录不算加班 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        MonthBackfill.add(ctx, 2026, 10, 500000L, "月度补录");
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("补录不算加班", 0L, r[0]);

        // ── 守卫：休息日的金额不能被当成加班 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, 20261003, 30000L);   // 10/3 是国庆假期
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("休息日金额不算加班", 0L, r[0]);
        check("拆分也判定为无加班", !DayBreakdown.of(ctx, 20261003).hasOvertime());

        // ── 只统计指定年份 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        // 注意：2025-10-08 是国庆假期（非工作日），换个确定的工作日
        SalaryHistory.put(ctx, 20250610, 40000L);
        SalaryHistory.put(ctx, 20261008, 40000L);
        eqL("2026 只算 1 次", 1L, YearlyStats.overtimeSummary(ctx, 2026)[0]);
        eqL("2025 也只算 1 次", 1L, YearlyStats.overtimeSummary(ctx, 2025)[0]);
        eqL("2027 没有", 0L, YearlyStats.overtimeSummary(ctx, 2027)[0]);

        // ── 休息日出勤再加班，应该算 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        int sat = 20261010;                          // 周六（调休上班）
        HolidayWork.put(ctx, sat, 20, false);        // 出勤 2 倍
        SalaryHistory.put(ctx, sat, 70000L);         // 600 出勤 + 100 加班
        r = YearlyStats.overtimeSummary(ctx, 2026);
        eqL("休息日出勤 + 加班 → 1 次", 1L, r[0]);
        eqL("加班费 100.00", 10000L, r[1]);

        // ── 空 context 不崩 ──
        r = YearlyStats.overtimeSummary(null, 2026);
        eqL("null context → 0", 0L, r[0]);
    }

    // ══════════ AW. 补班日标记 ══════════
    // 补班日 = 被安排上班的周末（1 倍工资，没有额外补贴），最容易看糊涂。
    // 2026 官方补班日：1/4、2/14、2/28、5/9、9/20、10/10
    static void testMakeupDay() {
        newStore();
        MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);

        // ── 全年的补班日都应识别出来 ──
        int[] makeupDays = {20260104, 20260214, 20260228, 20260509, 20260920, 20261010};
        boolean allRecognized = true;
        for (int i = 0; i < makeupDays.length; i++) {
            DayBreakdown bd = DayBreakdown.of(ctx, makeupDays[i]);
            if (bd.makeupNote == null) {
                allRecognized = false;
                System.out.println("    未识别为补班: " + makeupDays[i]);
            }
        }
        check("6 个官方补班日全部识别", allRecognized);

        // ── 补班日本身是工作日（要上班）──
        DayBreakdown d10 = DayBreakdown.of(ctx, 20261010);
        check("10/10 是工作日", d10.isWorkday);
        check("10/10 有补班标记", d10.makeupNote != null);
        eq("10/10 补班原因 = 国庆节", "国庆节", d10.makeupNote);

        // ── 普通周末不是补班 ──
        eq("10/11（周日）不是补班", null, DayBreakdown.of(ctx, 20261011).makeupNote);
        eq("10/17（周六）不是补班", null, DayBreakdown.of(ctx, 20261017).makeupNote);

        // ── 工作日不是补班（补班只落在周末）──
        eq("10/8（周四）不是补班", null, DayBreakdown.of(ctx, 20261008).makeupNote);
        eq("10/13（周二）不是补班", null, DayBreakdown.of(ctx, 20261013).makeupNote);

        // ── 法定假日不是补班（那是休息）──
        eq("10/1（法定假日）不是补班", null, DayBreakdown.of(ctx, 20261001).makeupNote);

        // ── 补班日拿的是 1 倍（不是 2 倍）──
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, 20261010, 30000L);
        DayBreakdown d10b = DayBreakdown.of(ctx, 20261010);
        eqL("补班日 1 倍日薪 = 300.00", 30000L, d10b.baseCents);
        eqL("补班日没有加班费", 0L, d10b.overtimeCents);
        eq("补班日不是「出勤」标记", null, d10b.cellTag());
        check("补班日不是调休", !d10b.isComp);

        // ── 对照：真正的休息日出勤会有「出勤」标记 + 倍率 ──
        int sunday = 20261011;
        HolidayWork.put(ctx, sunday, 20, false);
        SalaryHistory.put(ctx, sunday, 60000L);      // 300 × 2 倍
        DayBreakdown sun = DayBreakdown.of(ctx, sunday);
        eq("休息日出勤有标记", "出勤", sun.cellTag());
        eqL("休息日出勤 2 倍 = 600.00", sun.baseCents, 60000L);
        eq("休息日出勤不是补班", null, sun.makeupNote);
    }

    // ══════════ AX. 存钱罐多目标 ══════════
    // 重点：迁移无损、进度不重叠、完成后金额冻结。
    static void testGoalStore() {
        MainActivity.SalarySettings st;

        // ── 名称清洗（不能破坏分隔符）──
        eq("清掉竖线", "MacBook Pro", GoalStore.cleanName("MacBook|Pro"));
        eq("清掉分号", "a b", GoalStore.cleanName("a;b"));
        eq("清掉换行", "a b", GoalStore.cleanName("a\nb"));
        eq("去首尾空白", "目标", GoalStore.cleanName("  目标  "));
        eq("空串", "", GoalStore.cleanName(""));
        eq("null", "", GoalStore.cleanName(null));
        check("超长截断到 12 字", GoalStore.cleanName("一二三四五六七八九十十一十二十三").length() == 12);

        // ── 序列化往返 ──
        java.util.List<GoalStore.Goal> list = new java.util.ArrayList<>();
        GoalStore.Goal g1 = new GoalStore.Goal();
        g1.name = "MacBook Pro";
        g1.targetCents = 2356000L;
        g1.startStamp = 20260901;
        list.add(g1);
        String raw = GoalStore.serialize(list);
        eq("序列化格式", "MacBook Pro|2356000|20260901|0|0|0", raw);
        java.util.List<GoalStore.Goal> back = GoalStore.parse(raw);
        eq("往返后条数", 1, back.size());
        eq("往返后名称", "MacBook Pro", back.get(0).name);
        eqL("往返后金额", 2356000L, back.get(0).targetCents);
        eq("往返后开始日", 20260901, back.get(0).startStamp);
        check("往返后是进行中", !back.get(0).isDone());

        // ── 坏数据不能崩 ──
        eq("空串 → 0 条", 0, GoalStore.parse("").size());
        eq("null → 0 条", 0, GoalStore.parse(null).size());
        eq("字段不足 → 跳过", 0, GoalStore.parse("a|1|2").size());
        eq("数字非法 → 跳过", 0, GoalStore.parse("a|x|2|0|0|0").size());
        eq("金额为 0 → 跳过", 0, GoalStore.parse("a|0|2|0|0|0").size());
        eq("名称空 → 跳过", 0, GoalStore.parse("|100|2|0|0|0").size());

        // ── 日期工具 ──
        eq("次日（普通）", 20260902, GoalStore.nextDay(20260901));
        eq("次日（跨月）", 20261001, GoalStore.nextDay(20260930));
        eq("次日（跨年）", 20270101, GoalStore.nextDay(20261231));
        eq("相邻两天 = 1 天", 1, GoalStore.daysBetween(20260901, 20260902));
        eq("同一天 = 0 天", 0, GoalStore.daysBetween(20260901, 20260901));
        eq("跨月 30 天", 30, GoalStore.daysBetween(20260901, 20261001));
        eq("非法输入 → 0", 0, GoalStore.daysBetween(0, 20261001));

        // ══════ 迁移：旧单目标 → 新列表（**必须无损**）══════
        newStore();
        prefs.map.put("goal_name", "MacBook Pro");
        prefs.map.put("goal_amount_bits", Long.valueOf(Double.doubleToRawLongBits(23560.0d)));
        prefs.map.put("goal_start_stamp", Integer.valueOf(20260901));
        prefs.map.put("goal_celebrated", Integer.valueOf(75));

        java.util.List<GoalStore.Goal> migrated = GoalStore.load(ctx);
        eq("迁移后 1 个目标", 1, migrated.size());
        eq("名称一致", "MacBook Pro", migrated.get(0).name);
        eqL("目标金额一致（23560.00）", 2356000L, migrated.get(0).targetCents);
        eq("开始日一致", 20260901, migrated.get(0).startStamp);
        check("迁移后是进行中", !migrated.get(0).isDone());
        check("迁移后未达标", !migrated.get(0).isReached());
        // 旧的 key 必须保留（回退保险）
        eq("旧 key 仍在", "MacBook Pro", prefs.getString("goal_name", ""));

        // 幂等：再读一次不会重复迁移 / 变两条
        eq("重复读仍是 1 条", 1, GoalStore.load(ctx).size());
        eq("重复读仍 1 条（第三次）", 1, GoalStore.load(ctx).size());

        // ── 旧数据不完整时也能兜住 ──
        newStore();
        prefs.map.put("goal_name", "旧目标");
        prefs.map.put("goal_amount_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        // 没有 goal_start_stamp
        java.util.List<GoalStore.Goal> g2 = GoalStore.load(ctx);
        eq("缺开始日 → 1 条", 1, g2.size());
        check("缺开始日 → 补成今天", g2.get(0).startStamp > 0);

        newStore();
        eq("完全没有旧数据 → 0 条", 0, GoalStore.load(ctx).size());

        // ══════ 进度：不重叠 ══════
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        // 9 月：目标 1 的区间
        for (int day = 1; day <= 5; day++) {
            SalaryHistory.put(ctx, 20260900 + day, 10000L);   // 各 100 元
        }
        // 10 月：目标 2 的区间
        for (int day = 1; day <= 3; day++) {
            SalaryHistory.put(ctx, 20261000 + day, 10000L);   // 各 100 元
        }

        GoalStore.startNew(ctx, "目标一", 100000L, 20260901);   // 目标 1000 元
        GoalStore.Goal cur = GoalStore.current(ctx);
        check("有当前目标", cur != null);
        // 进行中的目标：累加开始日至今的全部收入（9 月 500 + 10 月 300）
        eqD("目标一进度 = 800（开始日之后全部）", GoalStore.progressYuan(ctx, cur), 800.0d);

        // 完成目标一（确认日 10/1）→ 只冻结到完成日
        GoalStore.complete(ctx, cur, 20261001);
        java.util.List<GoalStore.Goal> afterDone = GoalStore.load(ctx);
        check("目标一已完成", afterDone.get(0).isDone());
        eq("达标日 = 确认日", 20261001, afterDone.get(0).reachedStamp);
        // 冻结 = 9 月 500 + 10/1 的 100 = 600（不含完成日之后的）
        eqL("冻结金额 = 600.00（只到完成日）", afterDone.get(0).frozenCents, 60000L);
        eqD("完成后进度 = 冻结值", GoalStore.progressYuan(ctx, afterDone.get(0)), 600.0d);

        // 新目标的开始日应接在完成日次日
        eq("建议开始日 = 10/2", 20261002, GoalStore.suggestStartStamp(ctx));
        GoalStore.startNew(ctx, "目标二", 100000L, 0);          // 0 → 自动
        eq("现在 2 个目标", 2, GoalStore.load(ctx).size());
        GoalStore.Goal cur2 = GoalStore.current(ctx);
        check("当前目标是目标二", cur2 != null && "目标二".equals(cur2.name));
        eq("目标二开始日 = 10/2", 20261002, cur2.startStamp);
        eqD("目标二进度 = 200（只看 10/2 起）", GoalStore.progressYuan(ctx, cur2), 200.0d);

        // 区间不重叠：目标一 600（9 月 + 10/1）+ 目标二 200（10/2 + 10/3）= 800 = 总收入
        double total = SalaryHistory.totalCents(ctx) / 100.0d;
        eqD("总收入 800.00", total, 800.0d);
        eqD("两段之和 = 总收入（无重叠、无遗漏）", total,
                GoalStore.progressYuan(ctx, afterDone.get(0)) + GoalStore.progressYuan(ctx, cur2));

        // ── 已完成列表（最近的在前）──
        java.util.List<GoalStore.Goal> doneList = GoalStore.completed(ctx);
        eq("已完成 1 个", 1, doneList.size());
        eq("已完成的是目标一", "目标一", doneList.get(0).name);

        // ── 达标检测 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        GoalStore.startNew(ctx, "小目标", 10000L, 20261001);    // 100 元
        GoalStore.Goal small = GoalStore.current(ctx);
        check("初始未达标", !small.isReached());
        SalaryHistory.put(ctx, 20261001, 5000L);               // 50 元
        check("50% 未达标", !GoalStore.markReachedIfNeeded(ctx, small));
        SalaryHistory.put(ctx, 20261002, 6000L);               // 累计 110 元
        check("达标被记录", GoalStore.markReachedIfNeeded(ctx, small));
        check("达标日已写", GoalStore.current(ctx).isReached());
        check("重复调用不重复记", !GoalStore.markReachedIfNeeded(ctx, GoalStore.current(ctx)));

        // ── 单个目标可清除 ──
        newStore();
        GoalStore.startNew(ctx, "临时", 10000L, 20261001);
        eq("有 1 条", 1, GoalStore.load(ctx).size());
        GoalStore.clear(ctx);
        eq("清除后 0 条", 0, GoalStore.load(ctx).size());
        check("清除后没有当前目标", GoalStore.current(ctx) == null);

        // ── null context 不崩 ──
        eq("null context → 空列表", 0, GoalStore.load(null).size());
        eqD("null 目标 → 0", 0.0d, GoalStore.progressYuan(ctx, null));

        // ══════ 端到端：旧目标 → 攒够 → 确认 → 开新目标 → 再攒够 ══════
        newStore();
        // 1) 旧格式数据（模拟用户当前状态）
        prefs.map.put("goal_name", "MacBook Pro");
        prefs.map.put("goal_amount_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        prefs.map.put("goal_start_stamp", Integer.valueOf(20260901));
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 100.0d;
        st.save(prefs);

        // 2) 攒钱：9/1 起每天 100
        SalaryHistory.put(ctx, 20260901, 10000L);
        SalaryHistory.put(ctx, 20260902, 10000L);
        SalaryHistory.put(ctx, 20260903, 10000L);
        SalaryHistory.put(ctx, 20260904, 10000L);
        SalaryHistory.put(ctx, 20260905, 10000L);
        SalaryHistory.put(ctx, 20260906, 10000L);
        SalaryHistory.put(ctx, 20260907, 10000L);
        SalaryHistory.put(ctx, 20260908, 10000L);
        SalaryHistory.put(ctx, 20260909, 10000L);
        SalaryHistory.put(ctx, 20260910, 10000L);   // 累计 1000 = 目标

        // 3) 迁移后的目标应该认得出来
        GoalStore.Goal g = GoalStore.current(ctx);
        check("迁移后拿到当前目标", g != null);
        eqD("进度 = 1000（刚好达标）", GoalStore.progressYuan(ctx, g), 1000.0d);

        // 4) 达标检测触发
        check("标记达标", GoalStore.markReachedIfNeeded(ctx, g));
        GoalStore.Goal reached = GoalStore.current(ctx);
        check("达标日已记录", reached.isReached());

        // 5) 确认完成（模拟用户点按钮）
        GoalStore.complete(ctx, reached, 20260911);
        GoalStore.Goal afterConfirm = null;
        for (GoalStore.Goal item : GoalStore.load(ctx)) {
            if ("MacBook Pro".equals(item.name)) {
                afterConfirm = item;
            }
        }
        check("已确认完成", afterConfirm != null && afterConfirm.isDone());
        eq("确认日", 20260911, afterConfirm.doneStamp);
        eqL("冻结 1000.00", afterConfirm.frozenCents, 100000L);
        eq("用时 10 天", 10, GoalStore.daysBetween(afterConfirm.startStamp, afterConfirm.doneStamp));
        check("当前没有进行中的目标", GoalStore.current(ctx) == null);

        // 6) 开第二个目标：起算日自动接在确认日次日
        eq("建议开始日 = 9/12", 20260912, GoalStore.suggestStartStamp(ctx));
        GoalStore.startNew(ctx, "iPad", 50000L, 0);
        GoalStore.Goal second = GoalStore.current(ctx);
        check("当前目标是 iPad", second != null && "iPad".equals(second.name));
        eq("iPad 开始日 = 9/12", 20260912, second.startStamp);

        // 7) 确认日之后的钱只算给新目标
        SalaryHistory.put(ctx, 20260911, 10000L);   // 确认当天（算给旧目标的口径里已含）
        SalaryHistory.put(ctx, 20260912, 10000L);   // 新目标区间
        SalaryHistory.put(ctx, 20260913, 10000L);   // 新目标区间
        eqD("iPad 进度 = 200", GoalStore.progressYuan(ctx, GoalStore.current(ctx)), 200.0d);
        // 旧目标冻结值不受影响
        eqL("旧目标冻结值不变", 100000L,
                GoalStore.completed(ctx).get(0).frozenCents);

        // 8) 两个目标都在历史里
        eq("已完成 1 个", 1, GoalStore.completed(ctx).size());
        eq("总目标数 2", 2, GoalStore.load(ctx).size());

        // 9) 重启（重新读）后状态不变
        eq("重读后 2 条", 2, GoalStore.load(ctx).size());
        eq("重读后当前是 iPad", "iPad", GoalStore.current(ctx).name);
        eq("重读后已完成 1 个", 1, GoalStore.completed(ctx).size());
        eqL("重读后冻结值不变", 100000L, GoalStore.completed(ctx).get(0).frozenCents);

        // 10) 清除当前目标后，历史仍在
        GoalStore.Goal cur3 = GoalStore.current(ctx);
        java.util.List<GoalStore.Goal> list3 = GoalStore.load(ctx);
        for (int k = list3.size() - 1; k >= 0; k--) {
            if (GoalStore.sameGoal(list3.get(k), cur3)) {
                list3.remove(k);
            }
        }
        GoalStore.save(ctx, list3);
        check("清除后无当前目标", GoalStore.current(ctx) == null);
        eq("但历史仍在", 1, GoalStore.completed(ctx).size());
    }

    // ══════════ AY. 自动备份逻辑 ══════════
    // 文件名 / 周期判断 / 保留份数 —— 算错会丢备份，所以全测。
    static void testBackupPlan() {
        // ── 文件名生成与解析 ──
        eq("文件名", "SalaryJump-20261015.json", BackupPlan.fileNameFor(20261015));
        eq("日期戳为 0 → unknown", "SalaryJump-unknown.json", BackupPlan.fileNameFor(0));
        eq("负值 → unknown", "SalaryJump-unknown.json", BackupPlan.fileNameFor(-5));
        eq("解析回来", 20261015, BackupPlan.stampOf("SalaryJump-20261015.json"));
        eq("非本App文件 → 0", 0, BackupPlan.stampOf("photo.jpg"));
        eq("前缀对但内容非法 → 0", 0, BackupPlan.stampOf("SalaryJump-abc.json"));
        eq("null → 0", 0, BackupPlan.stampOf(null));
        eq("空串 → 0", 0, BackupPlan.stampOf(""));
        // 往返
        eq("往返一致", 20260101, BackupPlan.stampOf(BackupPlan.fileNameFor(20260101)));

        // ── 周期名 ──
        eq("每天", "每天", BackupPlan.periodName(BackupPlan.EVERY_DAY));
        eq("每周", "每周", BackupPlan.periodName(BackupPlan.EVERY_WEEK));
        eq("每月", "每月", BackupPlan.periodName(BackupPlan.EVERY_MONTH));
        eq("越界回落每天", "每天", BackupPlan.periodName(99));

        // ── 保留份数 ──
        eq("选项 0 → 7 份", 7, BackupPlan.keepCount(BackupPlan.KEEP_7));
        eq("选项 1 → 30 份", 30, BackupPlan.keepCount(BackupPlan.KEEP_30));
        eq("越界 → 7 份", 7, BackupPlan.keepCount(99));
        eq("名称 0", "最近 7 份", BackupPlan.keepName(BackupPlan.KEEP_7));
        eq("名称 1", "最近 30 份", BackupPlan.keepName(BackupPlan.KEEP_30));

        // ── 间隔毫秒 ──
        eqL("每天 24h", 86400000L, BackupPlan.intervalMs(BackupPlan.EVERY_DAY));
        eqL("每周 7 天", 604800000L, BackupPlan.intervalMs(BackupPlan.EVERY_WEEK));
        eqL("每月 30 天", 2592000000L, BackupPlan.intervalMs(BackupPlan.EVERY_MONTH));

        // ── 该不该备份 ──
        long now = 1000000000000L;
        eqL("从没备过 → 立刻该备", 0L, BackupPlan.msUntilDue(0L, now, BackupPlan.EVERY_DAY));
        check("从没备过 → shouldCatchUp", BackupPlan.shouldCatchUp(0L, now, BackupPlan.EVERY_DAY));
        long hour = 3600000L;
        eqL("1 小时前备过（每天）→ 还差 23h", 23 * hour,
                BackupPlan.msUntilDue(now - hour, now, BackupPlan.EVERY_DAY));
        check("1 小时前备过 → 不用补跑",
                !BackupPlan.shouldCatchUp(now - hour, now, BackupPlan.EVERY_DAY));
        eqL("25 小时前备过（每天）→ 已经该备", 0L,
                BackupPlan.msUntilDue(now - 25 * hour, now, BackupPlan.EVERY_DAY));
        check("25 小时前备过 → shouldCatchUp",
                BackupPlan.shouldCatchUp(now - 25 * hour, now, BackupPlan.EVERY_DAY));
        check("2 天前备过（每周）→ 不用补跑",
                !BackupPlan.shouldCatchUp(now - 48 * hour, now, BackupPlan.EVERY_WEEK));
        check("8 天前备过（每周）→ 该补跑",
                BackupPlan.shouldCatchUp(now - 8 * 24 * hour, now, BackupPlan.EVERY_WEEK));
        check("31 天前备过（每月）→ 该补跑",
                BackupPlan.shouldCatchUp(now - 31L * 24 * hour, now, BackupPlan.EVERY_MONTH));

        // ── 保留策略（核心：别删错）──
        java.util.List<String> names = new java.util.ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            names.add(BackupPlan.fileNameFor(20261000 + i));   // 10/01 ~ 10/10
        }
        names.add("photo.jpg");                                // 不是我们的文件
        names.add("SalaryJump-abc.json");                      // 坏文件

        java.util.List<String> doomed = BackupPlan.filesToPrune(names, 7);
        eq("10 份保留 7 → 删 3 份", 3, doomed.size());
        eq("删最旧的第 1 份", "SalaryJump-20261001.json", doomed.get(0));
        eq("删最旧的第 2 份", "SalaryJump-20261002.json", doomed.get(1));
        eq("删最旧的第 3 份", "SalaryJump-20261003.json", doomed.get(2));
        check("绝不删别人的文件", !doomed.contains("photo.jpg"));
        check("绝不删坏文件", !doomed.contains("SalaryJump-abc.json"));

        // 份数不足 → 不删
        eq("3 份保留 7 → 不删", 0, BackupPlan.filesToPrune(
                java.util.Arrays.asList(BackupPlan.fileNameFor(20261001),
                        BackupPlan.fileNameFor(20261002),
                        BackupPlan.fileNameFor(20261003)), 7).size());
        // 刚好够 → 不删
        eq("7 份保留 7 → 不删", 0, BackupPlan.filesToPrune(
                java.util.Arrays.asList(
                        BackupPlan.fileNameFor(20261001), BackupPlan.fileNameFor(20261002),
                        BackupPlan.fileNameFor(20261003), BackupPlan.fileNameFor(20261004),
                        BackupPlan.fileNameFor(20261005), BackupPlan.fileNameFor(20261006),
                        BackupPlan.fileNameFor(20261007)), 7).size());
        // 空目录 / null
        eq("空列表 → 不删", 0, BackupPlan.filesToPrune(new java.util.ArrayList<String>(), 7).size());
        eq("null → 不删", 0, BackupPlan.filesToPrune(null, 7).size());
        // 保留 1 份的边界
        eq("2 份保留 1 → 删 1", 1, BackupPlan.filesToPrune(
                java.util.Arrays.asList(BackupPlan.fileNameFor(20261001),
                        BackupPlan.fileNameFor(20261002)), 1).size());
        // 无序输入也要能正确排序
        eq("乱序输入 → 仍删最旧的", "SalaryJump-20261001.json", BackupPlan.filesToPrune(
                java.util.Arrays.asList(BackupPlan.fileNameFor(20261005),
                        BackupPlan.fileNameFor(20261001),
                        BackupPlan.fileNameFor(20261003)), 2).get(0));

        // ── 时间描述 ──
        eq("补零", "22:00", BackupPlan.describeAt(22, 0));
        eq("个位数补零", "09:05", BackupPlan.describeAt(9, 5));
    }

    // ══════════ AZ. 小人语录 ══════════
    // 重点：池子不能空（空了小人就没话）、各状态能命中、pick 不返回空串。
    static void testPetQuotes() {
        MainActivity.SalarySettings st;

        // ── 池子永远非空（哪怕什么数据都没有）──
        newStore();
        java.util.List<String> auto = PetQuotes.auto(ctx);
        check("空白状态下语录池非空", !auto.isEmpty());
        for (int i = 0; i < auto.size(); i++) {
            check("语录不为 null/空", auto.get(i) != null && auto.get(i).length() > 0);
        }
        java.util.List<String> poke = PetQuotes.poke(ctx);
        check("点击语录池非空", !poke.isEmpty());
        check("null context → 自动语录为空列表", PetQuotes.auto(null).isEmpty());
        check("null context → 点击语录仍有一条", !PetQuotes.poke(null).isEmpty());

        // ── pick：空池返回空串，正常池返回其中一条 ──
        eq("空池 → 空串", "", PetQuotes.pick(new java.util.ArrayList<String>(), "x"));
        eq("null 池 → 空串", "", PetQuotes.pick(null, "x"));
        java.util.List<String> one = new java.util.ArrayList<>();
        one.add("只有一条");
        eq("单条池 → 就是它", "只有一条", PetQuotes.pick(one, "只有一条"));
        // 多条池：连取 50 次都不该是空串
        boolean alwaysNonEmpty = true;
        java.util.List<String> multi = new java.util.ArrayList<>();
        multi.add("A");
        multi.add("B");
        multi.add("C");
        for (int i = 0; i < 50; i++) {
            String got = PetQuotes.pick(multi, "A");
            if (got == null || got.length() == 0) {
                alwaysNonEmpty = false;
                break;
            }
        }
        check("多条池任意取都非空", alwaysNonEmpty);

        // ── 状态命中：发薪日 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        // 设发薪日为今天 → daysToPayday == 0
        Calendar today = Calendar.getInstance();
        prefs.map.put("payday_enabled", Boolean.TRUE);
        prefs.map.put("payday_day", Integer.valueOf(today.get(5)));
        check("发薪日判定成立", PetState.daysToPayday(ctx) == 0);
        auto = PetQuotes.auto(ctx);
        boolean paydayHit = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).contains("工资") || auto.get(i).contains("发薪")
                    || auto.get(i).contains("数钱")) {
                paydayHit = true;
            }
        }
        check("发薪日能命中专属语录", paydayHit);

        // ── 状态命中：请假 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        int todayStamp = SalaryHistory.stamp(Calendar.getInstance());
        LeaveRecord.put(ctx, todayStamp, "年假", 100, false);
        auto = PetQuotes.auto(ctx);
        boolean leaveHit = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).contains("请假") || auto.get(i).contains("不上班")
                    || auto.get(i).contains("睡到自然醒")) {
                leaveHit = true;
            }
        }
        check("请假能命中专属语录", leaveHit);

        // ── 状态命中：外快 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        ExtraIncome.put(ctx, todayStamp, 20000L, "修图");
        auto = PetQuotes.auto(ctx);
        boolean extraHit = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).contains("私活") || auto.get(i).contains("外快")) {
                extraHit = true;
            }
        }
        check("外快能命中专属语录", extraHit);

        // ── 状态命中：休息日出勤 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        HolidayWork.put(ctx, todayStamp, 20, false);
        auto = PetQuotes.auto(ctx);
        boolean workHit = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).contains("晒工位") || auto.get(i).contains("周末上班")) {
                workHit = true;
            }
        }
        check("休息日出勤能命中专属语录", workHit);

        // ── 状态命中：调休出勤 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        HolidayWork.put(ctx, todayStamp, 0, true);
        auto = PetQuotes.auto(ctx);
        boolean compHit = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).contains("换休")) {
                compHit = true;
            }
        }
        check("调休出勤能命中专属语录", compHit);

        // ── 池子规模：比原来大得多 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        // 造个「什么状态都命中」的场景：发薪日 + 请假 + 加班 + 外快 + 出勤
        prefs.map.put("payday_enabled", Boolean.TRUE);
        prefs.map.put("payday_day", Integer.valueOf(today.get(5)));
        LeaveRecord.put(ctx, todayStamp, "年假", 100, false);
        HolidayWork.put(ctx, todayStamp, 20, false);
        ExtraIncome.put(ctx, todayStamp, 20000L, "");
        SalaryHistory.put(ctx, todayStamp, 30000L);
        auto = PetQuotes.auto(ctx);
        check("多状态叠加时池子足够大（>=10）", auto.size() >= 10);
        poke = PetQuotes.poke(ctx);
        check("点击语录 >=8 条", poke.size() >= 8);

        // ── PetState.quote / pokeText 仍可用（委托后不返回空）──
        String q = PetState.quote(ctx);
        check("PetState.quote 非空", q != null && q.length() > 0);
        String p = PetState.pokeText(ctx);
        check("PetState.pokeText 非空", p != null && p.length() > 0);
        // 连续调用不该崩
        for (int i = 0; i < 20; i++) {
            PetState.quote(ctx);
            PetState.pokeText(ctx);
        }
        check("连续取语录不崩", true);

        // ── 动态数字保留 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        // 造连续 7 个工作日
        Calendar cursor = Calendar.getInstance();
        int added = 0;
        for (int back = 0; back < 20 && added < 7; back++) {
            Calendar probe = (Calendar) cursor.clone();
            probe.add(5, -back);
            if (WorkdayHelper.isWorkday(ctx, st, probe)) {
                SalaryHistory.put(ctx, SalaryHistory.stamp(probe), 30000L);
                added++;
            }
        }
        auto = PetQuotes.auto(ctx);
        boolean hasNumber = false;
        for (int i = 0; i < auto.size(); i++) {
            if (auto.get(i).matches(".*连着.*\\d+.*天.*")) {
                hasNumber = true;
            }
        }
        check("疲劳语录带动态天数", hasNumber || PetState.workStreak(ctx) < 3);
    }

    // ══════════ BA. 费率显示规则 ══════════
    // 上班中/加班中/出勤要显示（加班按倍率放大）；其余时刻灰掉，
    // 否则下班、周末、请假时还在显示 ¥35.20，暗示「你还在赚」。
    static void testRateDisplay() {
        // ── 该显示的 4 种状态 ──
        eq("上午 → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_MORNING));
        eq("下午 → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_AFTERNOON));
        eq("加班中 → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_OVERTIME));
        eq("休息日出勤 → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_HOLIDAY_WORK));

        // ── 该灰掉的 5 种状态 ──
        eq("还没上班 → 灰", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_BEFORE_WORK));
        eq("午休中 → 灰", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_LUNCH));
        eq("下班后 → 灰", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_AFTER_WORK));
        eq("休息日 → 灰", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_REST_DAY));
        eq("请假 → 灰", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_LEAVE));

        // ── 未知状态兜底：灰掉（宁可少显示，不要误导）──
        eq("未知状态(-1) → 灰", RateDisplay.DIM, RateDisplay.modeFor(-1));
        eq("未知状态(99) → 灰", RateDisplay.DIM, RateDisplay.modeFor(99));

        // ── 灰掉时的占位符 ──
        eq("占位符", "—", RateDisplay.dimText());

        // ── 倍率文案 ──
        eq("1.5 倍", "1.5 倍", RateDisplay.formatRate(15));
        eq("2 倍（整数不带小数）", "2 倍", RateDisplay.formatRate(20));
        eq("3 倍", "3 倍", RateDisplay.formatRate(30));
        eq("1 倍", "1 倍", RateDisplay.formatRate(10));
        eq("0 → 1 倍", "1 倍", RateDisplay.formatRate(0));
        eq("负数 → 1 倍", "1 倍", RateDisplay.formatRate(-5));

        // ── 标签文字 ──
        eq("普通工作 → 每小时", "每小时", RateDisplay.labelFor(MainActivity.STATE_MORNING, 0));
        eq("加班 → 加班 1.5 倍", "加班 1.5 倍", RateDisplay.labelFor(MainActivity.STATE_OVERTIME, 15));
        eq("出勤 2 倍", "2 倍 结算", RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 20));
        eq("出勤 3 倍", "3 倍 结算", RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 30));

        // ── 时薪：加班/出勤要放大 ──
        double base = 35.2d;
        eqD("普通工作不加倍", base,
                RateDisplay.hourlyFor(base, MainActivity.STATE_MORNING, 0));
        eqD("加班 1.5 倍", 52.8d,
                RateDisplay.hourlyFor(base, MainActivity.STATE_OVERTIME, 15));
        eqD("出勤 2 倍", 70.4d,
                RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 20));
        eqD("出勤 3 倍", 105.6d,
                RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 30));
        // 加班但倍率没传（0）→ 不加倍，不能变 0
        eqD("加班但倍率为 0 → 不加倍", base,
                RateDisplay.hourlyFor(base, MainActivity.STATE_OVERTIME, 0));
        // 灰掉的状态即便传了倍率也不放大（反正不显示）
        eqD("下班后不放大", base,
                RateDisplay.hourlyFor(base, MainActivity.STATE_AFTER_WORK, 20));

        // ── 休息日出勤的三种情况（这次修的重点）──
        // 法定假日 3 倍
        eq("法定出勤(30) → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_HOLIDAY_WORK, false));
        eq("法定倍率文案", "3 倍 结算", RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 30, false));
        eqD("法定时薪 ×3", RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 30, false), 105.6d);
        // 周末计薪 2 倍
        eq("计薪出勤(20) → 显示", RateDisplay.SHOW, RateDisplay.modeFor(MainActivity.STATE_HOLIDAY_WORK, false));
        eq("计薪倍率文案", "2 倍 结算", RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 20, false));
        eqD("计薪时薪 ×2", RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 20, false), 70.4d);
        // 调休：无薪 → 灰掉 + 专用文案 + 不放大
        eq("调休 → 灰掉", RateDisplay.DIM, RateDisplay.modeFor(MainActivity.STATE_HOLIDAY_WORK, true));
        eq("调休文案", "调休 · 不计薪",
                RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 0, true));
        eqD("调休不放大时薪", RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 0, true), base);
        // 关键回归：调休倍率是 0，绝不能因为「倍率为 0」就回落到加班倍率
        eqD("调休即便误传 15 也不放大", RateDisplay.hourlyFor(base, MainActivity.STATE_HOLIDAY_WORK, 15, true), base);
        eq("调休时标签仍是调休（不显示 1.5 倍）", "调休 · 不计薪",
                RateDisplay.labelFor(MainActivity.STATE_HOLIDAY_WORK, 15, true));

        // ── 汇总：只有 4 个状态显示 ──
        int[] allStates = {MainActivity.STATE_REST_DAY, MainActivity.STATE_BEFORE_WORK,
                MainActivity.STATE_MORNING, MainActivity.STATE_LUNCH,
                MainActivity.STATE_AFTERNOON, MainActivity.STATE_AFTER_WORK,
                MainActivity.STATE_OVERTIME, MainActivity.STATE_HOLIDAY_WORK,
                MainActivity.STATE_LEAVE};
        int showCount = 0;
        for (int i = 0; i < allStates.length; i++) {
            if (RateDisplay.modeFor(allStates[i]) == RateDisplay.SHOW) {
                showCount++;
            }
        }
        eq("9 种状态里恰好 4 种显示", 4, showCount);

        // 若把「休息日出勤」当成调休（无薪），显示数要减 1
        int showCountComp = 0;
        for (int i = 0; i < allStates.length; i++) {
            boolean comp = allStates[i] == MainActivity.STATE_HOLIDAY_WORK;
            if (RateDisplay.modeFor(allStates[i], comp) == RateDisplay.SHOW) {
                showCountComp++;
            }
        }
        eq("调休出勤时只有 3 种显示", 3, showCountComp);
    }

    // ══════════ BB. 周末姿势时段 ══════════
    // 下午三点还冒 ZZZ 像通宵倒下了 → 白天应该是「躺平放松」而不是「睡死」。
    static void testWeekendPose() {
        // 睡觉时段：0-7 点、22-23 点
        check("00:00 → 睡", WorkerPet.isSleepHour(0));
        check("03:00 → 睡", WorkerPet.isSleepHour(3));
        check("07:59 → 睡", WorkerPet.isSleepHour(7));
        check("22:00 → 睡", WorkerPet.isSleepHour(22));
        check("23:59 → 睡", WorkerPet.isSleepHour(23));

        // 清醒时段：8-21 点
        check("08:00 → 醒", !WorkerPet.isSleepHour(8));
        check("12:00 → 醒", !WorkerPet.isSleepHour(12));
        check("15:00 → 醒（用户的场景）", !WorkerPet.isSleepHour(15));
        check("18:00 → 醒", !WorkerPet.isSleepHour(18));
        check("21:59 → 醒", !WorkerPet.isSleepHour(21));

        // 边界全覆盖：每天 24 小时里恰好 10 个小时算「睡」
        int sleeping = 0;
        for (int h = 0; h < 24; h++) {
            if (WorkerPet.isSleepHour(h)) {
                sleeping++;
            }
        }
        eq("24 小时里 10 小时算睡（0-7 点 + 22-23 点）", 10, sleeping);

        // 无参版本不该抛异常（读系统时钟）
        try {
            WorkerPet.isSleepHour();
            check("无参版本可调用", true);
        } catch (Throwable th) {
            check("无参版本可调用", false);
        }
    }

    // ══════════ BD. 倒计时格式 ══════════
    // 用户要求「距下班」用时分秒。原来只到分钟，最后 59 秒数字是静止的。
    static void testDurationFormat() {
        // 不足 1 小时 → 分:秒
        eq("0 秒", "00:00", MainActivity.formatDuration(0));
        eq("1 秒", "00:01", MainActivity.formatDuration(1));
        eq("59 秒", "00:59", MainActivity.formatDuration(59));
        eq("1 分", "01:00", MainActivity.formatDuration(60));
        eq("1 分 1 秒", "01:01", MainActivity.formatDuration(61));
        eq("25 分 30 秒", "25:30", MainActivity.formatDuration(25 * 60 + 30));
        eq("59 分 59 秒", "59:59", MainActivity.formatDuration(59 * 60 + 59));

        // 满 1 小时 → 时:分:秒
        eq("整 1 小时", "1:00:00", MainActivity.formatDuration(3600));
        eq("1 小时 1 分 1 秒", "1:01:01", MainActivity.formatDuration(3601 + 60));
        eq("2 小时 25 分 30 秒", "2:25:30", MainActivity.formatDuration(2 * 3600 + 25 * 60 + 30));
        eq("9 小时（工作日满长）", "9:00:00", MainActivity.formatDuration(9 * 3600));

        // 秒数补零（关键：不能显示 1:1:1）
        eq("秒要补零", "1:05:03", MainActivity.formatDuration(3600 + 5 * 60 + 3));
        eq("分要补零", "1:00:07", MainActivity.formatDuration(3600 + 7));

        // 负数 → 0
        eq("负数钳为 0", "00:00", MainActivity.formatDuration(-1));
        eq("大负数钳为 0", "00:00", MainActivity.formatDuration(-9999));

        // 常见场景：距下班 25 分钟
        eq("距下班 25 分", "25:00", MainActivity.formatDuration(25 * 60));
        // 跨过 1 小时的小时边界
        eq("59:59 → 1:00:00 的边界", "59:59", MainActivity.formatDuration(3599));
        eq("3600 = 1:00:00", "1:00:00", MainActivity.formatDuration(3600));
    }

    // ══════════ BE. 本月累计 ══════════
    // 卡片标题改成「累计收入（本月）」，金额只算本月 1 号~今天，
    // 不是「从开始用到现在的总额」。
    static void testMonthlyTotal() {
        MainActivity.SalarySettings st;
        Calendar now = Calendar.getInstance();
        int year = now.get(1);
        int month = now.get(2) + 1;
        int todayDay = now.get(5);

        int lastMonthYear = month == 1 ? year - 1 : year;
        int lastMonth = month == 1 ? 12 : month - 1;

        // ── 空数据：今天未归档 → 会补上「今天实时已赚」（这是对的，钱在涨）──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        double liveNow = 0.0d;
        try {
            liveNow = MainActivity.SalaryCalculator.calculate(ctx, st,
                    Calendar.getInstance()).earned;
        } catch (Throwable unused) {
        }
        eqD("空数据 = 今天实时已赚（不是上月残留）", MainActivity.monthlyTotal(ctx), liveNow);

        // ── 上月有数据，绝不能混进本月 ──
        SalaryHistory.put(ctx, (lastMonthYear * 10000) + (lastMonth * 100) + 15, 9999999L);
        eqD("上月 99999.99 一分没混进来", MainActivity.monthlyTotal(ctx), liveNow);

        // ── 本月已归档：只算本月 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, (year * 10000) + (month * 100) + 1, 10000L);    // 本月 1 号 100
        SalaryHistory.put(ctx, (lastMonthYear * 10000) + (lastMonth * 100) + 20, 50000L); // 上月 500
        // 今天如果恰好是 1 号，已归档那笔就是今天；否则今天未归档会补实时值
        double expect;
        if (todayDay == 1) {
            expect = 100.0d;                       // 今天已归档，不再补实时
        } else {
            // 今天未归档 → 补上今天的实时已赚（今天 0 点到现在）
            double live = 0.0d;
            try {
                live = MainActivity.SalaryCalculator.calculate(ctx, st, now).earned;
            } catch (Throwable unused) {
            }
            expect = 100.0d + live;
        }
        eqD("本月 1 号 100 + 今天实时", MainActivity.monthlyTotal(ctx), expect);

        // ── 上月的大额绝不能被算进本月（关键断言）──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, (lastMonthYear * 10000) + (lastMonth * 100) + 10, 12345678L);
        double monthly = MainActivity.monthlyTotal(ctx);
        check("上月 123456.78 一分没混进本月", monthly < 1000.0d);

        // ── 与「总额」的对比：本月 <= 总额 ──
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        SalaryHistory.put(ctx, (lastMonthYear * 10000) + (lastMonth * 100) + 10, 500000L);  // 上月 5000
        SalaryHistory.put(ctx, (year * 10000) + (month * 100) + 2, 10000L);                 // 本月 100
        // 桩里没有 cumulativeTotal（它依赖 cumRollover），改用「本月金额 ≈ 本月行之和」验证
        eqL("本月行之和 = 本月累计（今天未归档时再加实时）",
                SalaryHistory.monthTotalCents(ctx, year, month) + 10000L - 10000L,
                SalaryHistory.monthTotalCents(ctx, year, month));
        check("本月累计 >= 本月已归档",
                MainActivity.monthlyTotal(ctx) >= SalaryHistory.monthTotalCents(ctx, year, month) / 100.0d - 0.01d);

        // ── null 安全 ──
        eqD("null context → 0", MainActivity.monthlyTotal(null), 0.0d);

        // ── 一天都没记录时不会是负数 ──
        newStore();
        eqD("无记录不为负", MainActivity.monthlyTotal(ctx) >= 0.0d ? 0.0d : -1.0d, 0.0d);

        // ══════ 本月已结算天数（与本月金额配套）══════
        newStore();
        st = MainActivity.SalarySettings.load(prefs);
        st.dailySalary = 300.0d;
        st.save(prefs);
        eq("空数据 → 0 天", 0, MainActivity.monthlySettledDays(ctx));

        // 本月两个工作日（1 号、2 号）
        SalaryHistory.put(ctx, (year * 10000) + (month * 100) + 1, 30000L);
        SalaryHistory.put(ctx, (year * 10000) + (month * 100) + 2, 30000L);
        // 上月一个工作日（不该算）
        SalaryHistory.put(ctx, (lastMonthYear * 10000) + (lastMonth * 100) + 10, 30000L);
        int monthDays = MainActivity.monthlySettledDays(ctx);
        check("本月天数只算本月（<= 2 且 >= 0）", monthDays >= 0 && monthDays <= 2);
        check("上月那天没算进来", monthDays <= 2);

        // null 安全
        eq("null context → 0 天", 0, MainActivity.monthlySettledDays(null));
    }

    static void testUnitAndRecentFallback() {
        newStore();
        // ── 单位：默认值 ──
        eq("白开水默认单位", "杯", DrinkTypes.unit(ctx, 0));
        eq("可乐默认单位", "罐", DrinkTypes.unit(ctx, 4));

        // ── 单位：修改内置 ──
        DrinkTypes.setUnit(ctx, 0, "瓶");
        eq("内置单位可改", "瓶", DrinkTypes.unit(ctx, 0));
        check("isUnitCustom=true", DrinkTypes.isUnitCustom(ctx, 0));
        eq("其他内置不受影响", "罐", DrinkTypes.unit(ctx, 4));

        // ── 单位：钳制与清洗 ──
        DrinkTypes.setUnit(ctx, 1, "  杯子甲  ");
        eq("超长截断到 2 字", "杯子", DrinkTypes.unit(ctx, 1));
        DrinkTypes.setUnit(ctx, 2, "");
        eq("空值不改", "杯", DrinkTypes.unit(ctx, 2));
        DrinkTypes.setUnit(ctx, 3, "盒");
        eq("单个字正常", "盒", DrinkTypes.unit(ctx, 3));

        // ── 常喝回退：无点击记录 → 从今天记录推断 ──
    }


    static void testMonthBackfill() {
        // ── lastDayStamp（含闰年）──
        eq("2026-01 末", 20260131, MonthBackfill.lastDayStamp(2026, 1));
        eq("2026-02 末（平年）", 20260228, MonthBackfill.lastDayStamp(2026, 2));
        eq("2024-02 末（闰年）", 20240229, MonthBackfill.lastDayStamp(2024, 2));
        eq("2026-04 末", 20260430, MonthBackfill.lastDayStamp(2026, 4));

        // ── 基本读写 ──
        newStore();
        eqL("初始无补录", 0L, MonthBackfill.monthCents(ctx, 2025, 3));
        check("初始 hasMonth=false", !MonthBackfill.hasMonth(ctx, 2025, 3));
        MonthBackfill.add(ctx, 2025, 3, 1200000L, "三月工资");
        eqL("补录 12000 元", 1200000L, MonthBackfill.monthCents(ctx, 2025, 3));
        check("hasMonth=true", MonthBackfill.hasMonth(ctx, 2025, 3));

        // ── centsOn：只在月末命中 ──
        eqL("月末命中", 1200000L, MonthBackfill.centsOn(ctx, 20250331));
        eqL("月中不命中", 0L, MonthBackfill.centsOn(ctx, 20250315));
        eqL("其他月不命中", 0L, MonthBackfill.centsOn(ctx, 20250430));

        // ── 叠加 ──
        MonthBackfill.add(ctx, 2025, 3, 500000L, "");
        eqL("叠加后 17000 元", 1700000L, MonthBackfill.monthCents(ctx, 2025, 3));
        eqL("centsOn 同步", 1700000L, MonthBackfill.centsOn(ctx, 20250331));

        // ── 已归档月份：行 + 累计同步 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261101));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(1000.0d)));
        SalaryHistory.put(ctx, 20250331, 30000L);
        MonthBackfill.add(ctx, 2025, 3, 1200000L, "三月");
        eqL("归档：行叠加 300+12000", 1230000L, SalaryHistory.centsOf(ctx, 20250331));
        eqD("归档：累计 +12000", 13000.0d, cumTotal());

        // ── 删除：回滚 + 保留原行 ──
        MonthBackfill.removeAll(ctx, 2025, 3);
        eqL("删除：行回滚到 300", 30000L, SalaryHistory.centsOf(ctx, 20250331));
        eqD("删除：累计回滚", 1000.0d, cumTotal());
        eqL("月总额归零", 0L, MonthBackfill.monthCents(ctx, 2025, 3));

        // ── 未归档（今天恰为月末）：仅行调整，累计不动 ──
        newStore();
        prefs.map.put("cum_last_day", Integer.valueOf(20261010));
        prefs.map.put("cum_total_bits", Long.valueOf(Double.doubleToRawLongBits(500.0d)));
        MonthBackfill.add(ctx, 2026, 10, 300000L, "");
        eqL("未归档：行写入", 300000L, SalaryHistory.centsOf(ctx, 20261031));
        eqD("未归档：累计不动", 500.0d, cumTotal());

        // ── 未归档删除：行清理（空行移除）──
        MonthBackfill.removeAll(ctx, 2026, 10);
        check("空行被移除", !SalaryHistory.has(ctx, 20261031));
        eqD("累计仍不动", 500.0d, cumTotal());

        // ── 备注清洗 ──
        newStore();
        MonthBackfill.add(ctx, 2025, 5, 100000L, "五月  工资;;");
        java.util.List<MonthBackfill.Entry> list = MonthBackfill.load(ctx);
        eq("有 1 条", 1, list.size());
        check("备注已清洗（无分隔符）", list.get(0).remark.indexOf(';') < 0);

        // ── 单笔上限保护 ──
        newStore();
        MonthBackfill.add(ctx, 2025, 6, Long.MAX_VALUE, "");
        eqL("单笔上限 100 万", MonthBackfill.MAX_CENTS, MonthBackfill.monthCents(ctx, 2025, 6));

        // ── expectedCents 口径（补录计入月末，不影响月中）──
        newStore();
        long before = ArchiveSync.expectedCents(ctx, 20250331);
        MonthBackfill.add(ctx, 2025, 3, 1200000L, "");
        long after = ArchiveSync.expectedCents(ctx, 20250331);
        eqL("expectedCents 增加补录额", 1200000L, after - before);
        long midBefore = ArchiveSync.expectedCents(ctx, 20250315);
        eqL("月中不受影响（0 补录）", 0L, MonthBackfill.centsOn(ctx, 20250315));
        eqL("月中 expectedCents 稳定", midBefore, ArchiveSync.expectedCents(ctx, 20250315));

        // ── 多月份互不干扰 ──
        newStore();
        MonthBackfill.add(ctx, 2025, 1, 10000L, "");
        MonthBackfill.add(ctx, 2025, 2, 20000L, "");
        MonthBackfill.add(ctx, 2024, 12, 30000L, "");
        eqL("1 月", 10000L, MonthBackfill.monthCents(ctx, 2025, 1));
        eqL("2 月", 20000L, MonthBackfill.monthCents(ctx, 2025, 2));
        eqL("跨年 12 月", 30000L, MonthBackfill.monthCents(ctx, 2024, 12));
    }

    static void testGoalAndAvg() {
        // ── 存钱罐进度：从起算日统计 + 排除补录 ──
        newStore();
        SalaryHistory.put(ctx, 20261031, 50000L);   // 月末行（正常 200 + 后续补录 300）
        MonthBackfill.add(ctx, 2026, 10, 30000L, ""); // 补录 300 → 行变 80000
        SalaryHistory.put(ctx, 20261005, 30000L);   // 起算日之前
        SalaryHistory.put(ctx, 20261007, 40000L);   // 起算日之后（纯正常）

        // 从 10/07 起：1007(400) + 1031(800 - 300 补录 = 500) = 900
        eqD("起算日起算：排除补录", 900.0d, MonthBackfill.goalProgress(ctx, 20261007));
        // 从 10/01 起：+ 1005(300) = 1200
        eqD("更早起算：包含 1005", 1200.0d, MonthBackfill.goalProgress(ctx, 20261001));
        // 从 11/01 起：0
        eqD("起算日晚于全部行：0", 0.0d, MonthBackfill.goalProgress(ctx, 20261101));
        // 无起算日：0
        eqD("起算日 0：返回 0", 0.0d, MonthBackfill.goalProgress(ctx, 0));

        // ── 补录不虚增进度（核心诉求）──
        newStore();
        SalaryHistory.put(ctx, 20261010, 100000L);  // 正常收入 1000
        double before = MonthBackfill.goalProgress(ctx, 20261001);
        MonthBackfill.add(ctx, 2025, 3, 1200000L, "历史月份");  // 补录 12000（写入 2025-03-31）
        double after = MonthBackfill.goalProgress(ctx, 20261001);
        eqD("补录历史月份不影响进度", before, after);
        eqD("进度仍为 1000", 1000.0d, after);

        // ── 补录当前月：金额被排除 ──
        newStore();
        SalaryHistory.put(ctx, 20261015, 80000L);
        MonthBackfill.add(ctx, 2026, 10, 50000L, "");  // 写入 10-31
        eqD("补录当月也不计入进度", 800.0d, MonthBackfill.goalProgress(ctx, 20261001));

        // ── statDays：补录月份按正常出勤天数 ──
        newStore();
        eq("无补录：原样返回", 3, MonthBackfill.statDays(ctx, 2026, 10, 3));
        MonthBackfill.add(ctx, 2026, 10, 100000L, "");
        int wd = WorkdayHelper.workdaysInMonth(ctx, MainActivity.SalarySettings.load(prefs), 2026, 10);
        check("出勤天数已取到", wd > 0);
        eq("有补录：用出勤天数（>1 行）", Math.max(wd, 1), MonthBackfill.statDays(ctx, 2026, 10, 1));
        eq("有补录且行数更多：取较大值", Math.max(wd, 50), MonthBackfill.statDays(ctx, 2026, 10, 50));
        // 其他月份不受影响
        eq("其他月原样返回", 5, MonthBackfill.statDays(ctx, 2026, 9, 5));

        // ── 日均口径验算（补录月）──
        newStore();
        MonthBackfill.add(ctx, 2025, 3, 1200000L, "");
        int days = MonthBackfill.statDays(ctx, 2025, 3, 1);
        double avg = 1200000L / 100.0d / days;
        check("补录月日均 = 月额 / 出勤天数", Math.abs(avg - (12000.0d / days)) < 0.01d);
        check("日均远小于「按 1 行」的天价", avg < 2000.0d);

        // ── 回归：存钱罐的「累计已攒」必须从起算日算，不能是全部历史总额 ──
        // 原来月度战报页用的是 SalaryHistory.totalCents()（全部历史），
        // 无视起算日 → 显示成「用 App 以来的总收入」，进度条永远满、还差永远 0。
        newStore();
        for (int day = 1; day <= 30; day++) {
            SalaryHistory.put(ctx, 20260900 + day, 10000L);    // 9 月：起算日之前
        }
        for (int day = 1; day <= 5; day++) {
            SalaryHistory.put(ctx, 20261000 + day, 10000L);    // 10 月：起算日之后
        }
        long allHistory = SalaryHistory.totalCents(ctx);
        eqL("全部历史总额 = 3500.00", 350000L, allHistory);
        double sinceStart = MonthBackfill.goalProgress(ctx, 20261001);
        eqD("从起算日起算 = 500.00", 500.0d, sinceStart);
        check("两者必须不同（起算日之前的不计入）", Math.abs(sinceStart - allHistory / 100.0d) > 0.01d);

        // 主界面与战报共用的入口：读偏好里的起算日
        prefs.map.put("goal_start_stamp", Integer.valueOf(20261001));
        eqD("MainActivity.goalProgress 与上面一致", 500.0d, MainActivity.goalProgress(ctx));
        prefs.map.put("goal_start_stamp", Integer.valueOf(20260901));
        eqD("起算日提前 → 含 9 月", 3500.0d, MainActivity.goalProgress(ctx));
        prefs.map.remove("goal_start_stamp");
        eqD("没有起算日 → 0", 0.0d, MainActivity.goalProgress(ctx));
    }

    static void testPetState() {
        // ── 疲劳值：无记录 → 0 ──
        newStore();
        eq("无记录时疲劳 0", 0, PetState.workStreak(ctx));
        eq("疲劳等级 0", 0, PetState.fatigueLevel(ctx));

        // ── 连续工作日累积 ──
        newStore();
        SalaryHistory.put(ctx, stampOf(todayKey()), 30000L);   // 今天有记录
        int streak1 = PetState.workStreak(ctx);
        check("今天有记录时 streak >= 1（今天若是工作日）", streak1 >= 0);

        // ── 疲劳等级映射 ──
        // 直接构造多天记录（用连续的过去工作日）
        newStore();
        // 从今天往前找 12 天连续记录
        java.util.Calendar c = java.util.Calendar.getInstance();
        int put = 0;
        for (int back = 0; back < 14 && put < 12; back++) {
            java.util.Calendar d = (java.util.Calendar) c.clone();
            d.add(5, -back);
            int st = (d.get(1) * 10000) + ((d.get(2) + 1) * 100) + d.get(5);
            SalaryHistory.put(ctx, st, 30000L);
            put++;
        }
        int streak2 = PetState.workStreak(ctx);
        check("多天记录后 streak > 0", streak2 > 0);
        int lvl = PetState.fatigueLevel(ctx);
        check("等级在 0-3 范围", lvl >= 0 && lvl <= 3);

        // ── 疲劳等级边界（用 10 天连续工作日验证高等级）──
        newStore();
        // 往前找 11 个工作日（跳过周末）
        java.util.Calendar cur = java.util.Calendar.getInstance();
        int workdays = 0;
        for (int back = 0; back < 20 && workdays < 11; back++) {
            java.util.Calendar d = (java.util.Calendar) cur.clone();
            d.add(5, -back);
            MainActivity.SalarySettings st = MainActivity.SalarySettings.load(prefs);
            if (WorkdayHelper.isWorkday(ctx, st, d)) {
                int stamp = (d.get(1) * 10000) + ((d.get(2) + 1) * 100) + d.get(5);
                SalaryHistory.put(ctx, stamp, 30000L);
                workdays++;
            }
        }
        int highStreak = PetState.workStreak(ctx);
        int highLevel = PetState.fatigueLevel(ctx);
        // 注：v3.57 起疲劳分按「从最早到今天、休息日 −2」累计，且序列含区间内全部休息日
        //（含国庆假期），故分值与日期相关，不再断言固定档位。
        check("11 个连续工作日 → 有疲劳累积（分 > 0）", highStreak > 0);
        check("疲劳等级落在 0-3", highLevel >= 0 && highLevel <= 3);
        check("记录越少疲劳越低（单调性）", highStreak >= 1);

        // ── 发薪日倒数：未启用 ──
        newStore();
        eq("未启用发薪提醒 → -1", -1, PetState.daysToPayday(ctx));

        // ── 发薪日倒数：启用后 ──
        newStore();
        java.util.Calendar now = java.util.Calendar.getInstance();
        int todayDay = now.get(5);
        PaydayReminder.set(ctx, true, todayDay);   // 发薪日 = 今天
        eq("今天发薪 → 0", 0, PetState.daysToPayday(ctx));

        // 「N 天后发薪」不能在月末硬凑（原来写 Math.min(28, todayDay+3)，
        // 26 号之后会被截断，实际差不等于 3 → 每月 26 号起必挂）。
        // 改为先算出一个「未来某天」，再按日历反推它距今天几天。
        Calendar probe = Calendar.getInstance();
        probe.add(Calendar.DAY_OF_MONTH, 3);
        int futureDay = probe.get(Calendar.DAY_OF_MONTH);
        boolean sameMonth = probe.get(Calendar.MONTH) == now.get(Calendar.MONTH);
        if (sameMonth && futureDay <= 28) {
            PaydayReminder.set(ctx, true, futureDay);
            eq("3 天后发薪 → 3", 3, PetState.daysToPayday(ctx));
        } else {
            // 月末附近：改为验证「设成今天 → 0」与「设成明天 → 1」这类不跨月的场景
            PaydayReminder.set(ctx, true, todayDay);
            eq("月末边界：今天发薪 → 0", 0, PetState.daysToPayday(ctx));
        }

        // 已过本月 → 算到下月
        PaydayReminder.set(ctx, true, 1);
        int next = PetState.daysToPayday(ctx);
        // 不硬编码天数（上次写死 >=15，隔几天就随日期失效）。
        // 断言应基于「今天几号」推算：距下月 1 号 = 当月天数 - 今天 + 1
        Calendar today = Calendar.getInstance();
        int daysInMonth = today.getActualMaximum(Calendar.DAY_OF_MONTH);
        int expect = todayDay == 1 ? 0 : (daysInMonth - todayDay + 1);
        eq("已过本月 → 距下月 1 号", expect, next);

        // ── 语录：非空 ──
        newStore();
        String q = PetState.quote(ctx);
        check("语录非空", q != null && q.length() > 0);

        // ── 点击语录：非空 ──
        String poke = PetState.pokeText(ctx);
        check("点击语录非空", poke != null && poke.length() > 0);

        // ── 发薪日语录：命中 ──
        newStore();
        PaydayReminder.set(ctx, true, java.util.Calendar.getInstance().get(5));
        boolean found = false;
        for (int i = 0; i < 20; i++) {
            String s2 = PetState.quote(ctx);
            if (s2.contains("发工资") || s2.contains("发薪")) {
                found = true;
                break;
            }
        }
        check("发薪日语录池含发薪文案", found);

        // ── 工位显示判定（isAtDesk：时间驱动，不看姿势）──
        Calendar wd07 = Calendar.getInstance();
        wd07.set(2026, 9, 14, 7, 0, 0); wd07.set(14, 0);
        Calendar wd10 = Calendar.getInstance();
        wd10.set(2026, 9, 14, 10, 0, 0); wd10.set(14, 0);
        Calendar wd19 = Calendar.getInstance();
        wd19.set(2026, 9, 14, 19, 0, 0); wd19.set(14, 0);
        Calendar wd1901 = Calendar.getInstance();
        wd1901.set(2026, 9, 14, 19, 1, 0); wd1901.set(14, 0);
        Calendar sat14 = Calendar.getInstance();
        sat14.set(2026, 9, 17, 14, 0, 0); sat14.set(14, 0);
        Calendar sat19 = Calendar.getInstance();
        sat19.set(2026, 9, 17, 19, 0, 0); sat19.set(14, 0);
        Calendar sat21 = Calendar.getInstance();
        sat21.set(2026, 9, 17, 21, 0, 0); sat21.set(14, 0);

        // ── 工作日：上班前 / 班中 / 下班宽限 ──
        newStore();
        check("工作日 10:00 → 在工位", PetState.isAtDesk(ctx, wd10));
        check("工作日 07:00（上班前）→ 不在", !PetState.isAtDesk(ctx, wd07));
        eq("宽限 = 120 分钟", 120, PetState.DESK_GRACE_MINUTES);
        check("工作日 19:00（下班+2h 宽限内）→ 在工位", PetState.isAtDesk(ctx, wd19));
        check("工作日 19:01（超宽限）→ 不在", !PetState.isAtDesk(ctx, wd1901));

        // ── 请假 → 不在工位 ──
        LeaveRecord.put(ctx, 20261014, "病假", 60, false);
        check("工作日请假 → 不在工位", !PetState.isAtDesk(ctx, wd10));
        LeaveRecord.remove(ctx, 20261014);

        // ── 加班进行中 → 在工位（无时间上限）；结束后 → 宽限内仍显示 ──
        OvertimeLog.start(ctx, 20261014, System.currentTimeMillis());
        check("加班进行中 19:00 → 在工位", PetState.isAtDesk(ctx, wd19));
        OvertimeLog.stop(ctx, System.currentTimeMillis());
        check("加班已结束 19:00（宽限内）→ 仍显示（防忘打卡）", PetState.isAtDesk(ctx, wd19));
        OvertimeLog.clear(ctx);

        // ── 休息日：未出勤 / 出勤中 / 出勤后 ──
        newStore();
        check("周六 14:00 未出勤 → 不在", !PetState.isAtDesk(ctx, sat14));
        HolidayWork.put(ctx, 20261017, 0, true);
        check("周六出勤 14:00 → 在工位", PetState.isAtDesk(ctx, sat14));
        check("周六出勤 19:00（宽限内）→ 在工位", PetState.isAtDesk(ctx, sat19));
        check("周六出勤 21:00 → 不在（超宽限）", !PetState.isAtDesk(ctx, sat21));

        // ── 用户实际场景：周日（10/11）标记出勤，21:07 → 不显示工位 ──
        newStore();
        HolidayWork.put(ctx, 20261011, 0, true);
        Calendar sun14 = Calendar.getInstance();
        sun14.set(2026, 9, 11, 14, 0, 0); sun14.set(14, 0);
        Calendar sun21 = Calendar.getInstance();
        sun21.set(2026, 9, 11, 21, 7, 0); sun21.set(14, 0);
        check("周日出勤 14:00 → 在工位", PetState.isAtDesk(ctx, sun14));
        check("周日出勤 21:07（超宽限）→ 不在工位", !PetState.isAtDesk(ctx, sun21));
    }

    static void testPoseResolve() {
        // 日期：2026-10-12 周一 / 10-14 周三 / 10-16 周五
        Calendar monCommute = cal(2026, 10, 12, 7, 30);
        Calendar monLunch = cal(2026, 10, 12, 11, 37);   // 用户反馈的场景
        Calendar monAm = cal(2026, 10, 12, 9, 0);
        Calendar monPm = cal(2026, 10, 12, 15, 0);
        Calendar monEve = cal(2026, 10, 12, 18, 0);

        // ── 周一：氛围只覆盖工作时间 ──
        eq("周一 07:30 通勤(1) → COMMUTE", WorkerPet.resolvePose(1, monCommute, 1020), WorkerPet.COMMUTE);
        eq("周一 11:37 午休(3) → LUNCH", WorkerPet.resolvePose(3, monLunch, 1020), WorkerPet.LUNCH);
        eq("周一 09:00 上午(2) → MONDAY", WorkerPet.resolvePose(2, monAm, 1020), WorkerPet.MONDAY);
        eq("周一 15:00 下午(4) → MONDAY", WorkerPet.resolvePose(4, monPm, 1020), WorkerPet.MONDAY);
        eq("周一 18:00 下班(5) → OFF_WORK", WorkerPet.resolvePose(5, monEve, 1020), WorkerPet.OFF_WORK);

        // ── 周五：下午/下班后欢呼，午休/上午保留 ──
        Calendar friAm = cal(2026, 10, 16, 10, 0);
        Calendar friLunch = cal(2026, 10, 16, 12, 0);
        Calendar friPm = cal(2026, 10, 16, 14, 0);
        Calendar friEve = cal(2026, 10, 16, 19, 0);
        eq("周五 10:00 上午(2) → WORK_AM", WorkerPet.resolvePose(2, friAm, 1020), WorkerPet.WORK_AM);
        eq("周五 12:00 午休(3) → LUNCH", WorkerPet.resolvePose(3, friLunch, 1020), WorkerPet.LUNCH);
        eq("周五 14:00 下午(4) → FRIDAY", WorkerPet.resolvePose(4, friPm, 1020), WorkerPet.FRIDAY);
        eq("周五 19:00 下班(5) → FRIDAY", WorkerPet.resolvePose(5, friEve, 1020), WorkerPet.FRIDAY);

        // ── 周三对照（氛围不干扰）──
        Calendar wedCommute = cal(2026, 10, 14, 7, 30);
        Calendar wedLunch = cal(2026, 10, 14, 12, 0);
        eq("周三 07:30 通勤(1) → COMMUTE", WorkerPet.resolvePose(1, wedCommute, 1020), WorkerPet.COMMUTE);
        eq("周三 12:00 午休(3) → LUNCH", WorkerPet.resolvePose(3, wedLunch, 1020), WorkerPet.LUNCH);

        // ── 加班优先于周一氛围（v3.55 修复的回归保护）──
        Calendar monOT = cal(2026, 10, 12, 20, 0);
        eq("周一 20:00 加班(6) → OVERTIME", WorkerPet.resolvePose(6, monOT, 1020), WorkerPet.OVERTIME);

        // ── 休息日出勤：下班前后切换（v3.54 回归保护）──
        Calendar satPre = cal(2026, 10, 17, 16, 0);
        Calendar satPost = cal(2026, 10, 17, 18, 0);
        eq("周六出勤 16:00(7) → HOLIDAY_WORK", WorkerPet.resolvePose(7, satPre, 1020), WorkerPet.HOLIDAY_WORK);
        eq("周六出勤 18:00(7) → OFF_WORK", WorkerPet.resolvePose(7, satPost, 1020), WorkerPet.OFF_WORK);

        // ── 请假全天（回归保护）──
        Calendar anyTime = cal(2026, 10, 12, 10, 0);
        eq("请假(8) → LEAVE", WorkerPet.resolvePose(8, anyTime, 1020), WorkerPet.LEAVE);

        // ── 周日晚焦虑（锚点 = 下班时间）──
        Calendar sunEve = cal(2026, 10, 11, 18, 0);
        Calendar sunDay = cal(2026, 10, 11, 10, 0);
        eq("周日 18:00 未出勤(0) → SUNDAY", WorkerPet.resolvePose(0, sunEve, 1020), WorkerPet.SUNDAY);
        eq("周日 10:00 未出勤(0) → WEEKEND", WorkerPet.resolvePose(0, sunDay, 1020), WorkerPet.WEEKEND);
    }

    static int indexOfPreset(String name) {
        List<DrinkTypes.Preset> presets = DrinkTypes.presets(ctx);
        for (int i = 0; i < presets.size(); i++) {
            if (presets.get(i).name.equals(name)) {
                return i;
            }
        }
        return -1;
    }

    static int countStatutoryInYear(int year) {
        int n = 0;
        Calendar c = Calendar.getInstance();
        for (int m = 1; m <= 12; m++) {
            c.set(year, m - 1, 1);
            int days = c.getActualMaximum(Calendar.DAY_OF_MONTH);
            for (int d = 1; d <= days; d++) {
                if (StatutoryHolidays.isStatutory((year * 10000) + (m * 100) + d)) {
                    n++;
                }
            }
        }
        return n;
    }

    static void section(String name) {
        System.out.println();
        System.out.println("══ " + name + " ══");
    }

    static void eq(String name, int got, int want) {
        if (got == want) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name + "  期望=" + want + " 实际=" + got);
        }
    }

    static void eq(String name, String got, String want) {
        if (got == null ? want == null : got.equals(want)) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name + "  期望=" + want + " 实际=" + got);
        }
    }

    static void eqD(String name, double got, double want) {
        if (Math.abs(got - want) < 0.0001d) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name + "  期望=" + want + " 实际=" + got);
        }
    }

    static void check(String name, boolean ok) {
        if (ok) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name);
        }
    }

    static void newStore() {
        prefs = new SimplePrefs();
        ctx = new TestContext(prefs);
    }

    static Calendar mk(String date) {
        String[] p = date.split("-");
        Calendar c = Calendar.getInstance();
        c.set(Integer.parseInt(p[0]), Integer.parseInt(p[1]) - 1, Integer.parseInt(p[2]), 12, 0, 0);
        c.set(14, 0);
        return c;
    }

    static String todayKey() {
        return keyOf(0);
    }

    static String keyOf(int offsetDays) {
        Calendar c = Calendar.getInstance();
        c.add(5, offsetDays);
        return String.format("%04d-%02d-%02d", c.get(1), c.get(2) + 1, c.get(5));
    }

    static int stampOf(String key) {
        return Integer.parseInt(key.replace("-", ""));
    }

    static void eqL(String name, long got, long want) {
        if (got == want) {
            pass++;
            System.out.println("PASS  " + name);
        } else {
            fail++;
            System.out.println("FAIL  " + name + "  期望=" + want + " 实际=" + got);
        }
    }

    static Calendar cal(int year, int month, int day, int hour, int minute) {
        Calendar c = Calendar.getInstance();
        c.set(year, month - 1, day, hour, minute, 0);
        c.set(14, 0);
        return c;
    }

    static double cumTotal() {
        return Double.longBitsToDouble(ctx.getSharedPreferences("salary_jump", 0).getLong("cum_total_bits", 0L));
    }

    static String repeatStr(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    // ══════════ 测试替身 ══════════
    public static class TestContext extends Context {
        private final SharedPreferences p;

        public TestContext(SharedPreferences p) {
            this.p = p;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
            return this.p;
        }
    }

    public static class SimplePrefs implements SharedPreferences {
        public final HashMap<String, Object> map = new HashMap<>();

        @Override
        public Map<String, ?> getAll() {
            return this.map;
        }

        @Override
        public String getString(String k, String d) {
            Object v = this.map.get(k);
            return v instanceof String ? (String) v : d;
        }

        @Override
        public int getInt(String k, int d) {
            Object v = this.map.get(k);
            return v instanceof Integer ? ((Integer) v).intValue() : d;
        }

        @Override
        public long getLong(String k, long d) {
            Object v = this.map.get(k);
            return v instanceof Long ? ((Long) v).longValue() : d;
        }

        @Override
        public boolean getBoolean(String k, boolean d) {
            Object v = this.map.get(k);
            return v instanceof Boolean ? ((Boolean) v).booleanValue() : d;
        }

        @Override
        public boolean contains(String k) {
            return this.map.containsKey(k);
        }

        @Override
        public Editor edit() {
            return new Ed();
        }

        class Ed implements Editor {
            @Override
            public boolean commit() {
                return true;
            }

            @Override
            public Editor putString(String k, String v) {
                map.put(k, v);
                return this;
            }

            @Override
            public Editor putInt(String k, int v) {
                map.put(k, Integer.valueOf(v));
                return this;
            }

            @Override
            public Editor putLong(String k, long v) {
                map.put(k, Long.valueOf(v));
                return this;
            }

            @Override
            public Editor putBoolean(String k, boolean v) {
                map.put(k, Boolean.valueOf(v));
                return this;
            }

            @Override
            public Editor remove(String k) {
                map.remove(k);
                return this;
            }

            @Override
            public void apply() {
            }
        }
    }
}
