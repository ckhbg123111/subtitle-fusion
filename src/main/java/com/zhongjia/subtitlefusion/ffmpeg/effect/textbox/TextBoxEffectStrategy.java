package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

/**
 * 文本框动效策略：生成用于 overlay 底图与 drawtext 文本的统一位移表达式。
 * 返回的表达式为底图左上角的 x/y 表达式；文本应在此基础上居中对齐。
 */
public interface TextBoxEffectStrategy {

    /**
     * 构建动效表达式。
     * @param startSec 入场时间（秒字符串）
     * @param endSec 出场时间（秒字符串）
     * @param baseX0 基准左上角 x 表达式（静态）
     * @param baseY0 基准左上角 y 表达式（静态）
     * @param boxW 底图宽，用于部分动效位移计算
     * @param boxH 底图高，用于部分动效位移计算
     */
    TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH);

    /** 表达式返回对象。 */
    final class TextBoxEffect {
        public final String xExpr; // 底图左上角 x 表达式
        public final String yExpr; // 底图左上角 y 表达式
        public TextBoxEffect(String xExpr, String yExpr) {
            this.xExpr = xExpr;
            this.yExpr = yExpr;
        }
    }
}


