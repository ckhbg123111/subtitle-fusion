package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

import java.util.List;

/**
 * 关键字高亮：对匹配词包裹加粗/变色（不放大）。
 */
public class KeywordHighlightStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo, int playX, int playY) {
        // 整体轻微淡入；单词级高亮通过重写文本实现
        return "\\fad(120,120)";
    }

    @Override
    public String rewriteTextWithKeywords(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
        SubtitleFusionV2Request.SubtitleEffectInfo eff = lineInfo.getSubtitleEffectInfo();
        List<String> kws = eff != null ? eff.getKeyWords() : null;
        if (kws == null || kws.isEmpty()) return lineInfo.getText();
        String text = lineInfo.getText();
        for (String kw : kws) {
            if (kw == null || kw.isEmpty()) continue;
            // 加粗+变色，不再放大
            String tagPrefix = "{\\b1\\1c&H00FFFF&}";
            String tagSuffix = "{\\b0\\1c&HFFFFFF&}";
            text = text.replace(kw, tagPrefix + kw + tagSuffix);
        }
        return text;
    }
}


