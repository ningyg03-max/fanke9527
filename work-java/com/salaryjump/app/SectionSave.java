package com.salaryjump.app;

/**
 * 设置对话框里「哪个分区该保存什么」。
 *
 * 抽成纯函数的原因：设置界面的分区是**按需创建控件**的 ——
 * 「薪跳」分区才会建每天工资输入框，「工具与安全」分区没有。
 * 但保存时如果不看分区就走「全部保存」，就会去读那些根本没创建的控件 → 空指针崩溃。
 *
 * 这个对应关系放在这里之后，回归测试可以直接覆盖。
 */
final class SectionSave {
    /** 完整设置（section 0，所有分区都在）→ 保存全部 */
    static final int ALL = 0;
    /** 薪跳设置（section 1） */
    static final int SALARY = 1;
    /** 喝水设置（section 2） */
    static final int WATER = 2;
    /** 工具与安全（section 3）—— 里面只有「打工小人」是可保存的设置 */
    static final int TOOLS = 3;

    private SectionSave() {
    }

    /** 按分区决定保存范围。未知分区一律按「完整设置」处理（保持原行为）。 */
    static int targetFor(int section) {
        if (section == 1) {
            return SALARY;
        }
        if (section == 2) {
            return WATER;
        }
        if (section == MainActivity.STATE_LUNCH) {
            return TOOLS;
        }
        return ALL;
    }
}
