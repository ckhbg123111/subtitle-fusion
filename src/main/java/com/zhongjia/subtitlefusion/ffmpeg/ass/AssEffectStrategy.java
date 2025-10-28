package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

/**
 * ASS 字幕动效策略接口。
 */
public interface AssEffectStrategy {
    /**
     * 基于每行字幕信息生成 ASS 覆盖标签（override tags），例如 {\fad(200,100)} 等。
     * 仅返回标签片段，不含左右花括号；调用方负责组合成 {tags}text。
     */
    String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY);

    /**
     * 针对关键字高亮类效果，允许对原文本进行替换（插入标签），默认返回原文。
     */
    default String rewriteTextWithKeywords(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        return lineInfo.getText();
    }
}


