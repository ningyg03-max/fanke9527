package com.salaryjump.app;

/**
 * 主卡「金额 + 小人」这一行的排版计算。
 *
 * 抽成纯函数的原因：这一行的宽度是两者共享的 —— 小人放大就会挤掉金额，
 * 而这些数字算错了只会在真机上表现为「金额被盖住」，回归测试碰不到。
 * 放到这里之后，{@code tests/} 可以直接覆盖边界。
 */
final class HeroFit {
    /** 小人在这一行里最多占的比例，其余留给金额 */
    static final float PET_MAX_SHARE = 0.42f;
    /** 小人宽度下限（dp），再小就看不清表情了 */
    static final int PET_MIN_DP = 64;
    /** 金额字号范围（sp） */
    static final float AMOUNT_MIN_SP = 24.0f;
    static final float AMOUNT_MAX_SP = 46.0f;
    /** 高度相对宽度的比例（沿用原始 88×92 的观感） */
    static final float PET_HEIGHT_RATIO = 92.0f / 88.0f;

    private HeroFit() {
    }

    /**
     * 按这一行的可用宽度算小人的实际宽度（dp）。
     *
     * 用户选的尺寸是「上限」，不是「保证」：行宽不够时按 {@link #PET_MAX_SHARE} 压缩，
     * 保证金额那边至少还剩 58% 的地方。
     *
     * @param rowWidthDp 这一行的可用宽度（dp）；<= 0 表示还没测量出来，直接返回用户选的尺寸
     * @param requestedDp 用户在设置里选的尺寸（dp）
     */
    static int petWidthDp(int rowWidthDp, int requestedDp) {
        if (rowWidthDp <= 0) {
            return requestedDp;
        }
        int cap = Math.round(rowWidthDp * PET_MAX_SHARE);
        int w = Math.min(requestedDp, cap);
        return Math.max(PET_MIN_DP, w);
    }

    /** 小人高度：按宽度等比换算。 */
    static int petHeightDp(int widthDp) {
        return Math.round(widthDp * PET_HEIGHT_RATIO);
    }

    /**
     * 算出让文字刚好放进 availPx 的字号（px）。
     *
     * @param availPx      可用宽度（px）
     * @param widthAtRefPx 参考字号下这段文字的宽度（px）
     * @param refSizePx    测量时用的参考字号（px）
     * @return 夹在 AMOUNT_MIN_SP–AMOUNT_MAX_SP 之间（按 dp/sp 换算后）的字号
     */
    static float amountTextSizePx(float availPx, float widthAtRefPx, float refSizePx,
                                  float density) {
        float maxPx = AMOUNT_MAX_SP * density;
        float minPx = AMOUNT_MIN_SP * density;
        if (availPx <= 0.0f || widthAtRefPx <= 0.0f || refSizePx <= 0.0f) {
            return maxPx;
        }
        // 文字宽度与字号成正比 → 直接按比例缩放
        float needPx = refSizePx * availPx / widthAtRefPx;
        if (needPx > maxPx) {
            return maxPx;
        }
        return Math.max(minPx, needPx);
    }
}
