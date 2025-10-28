package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;

import java.util.List;

/**
 * 关键字高亮：对匹配词包裹更高字号/加粗/黄色。
 */
public class KeywordHighlightStrategy implements AssEffectStrategy {
    @Override
    public String buildOverrideTags(SubtitleFusionV2Request.CommonSubtitleInfo lineInfo) {
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
            // 简单直接替换（大小写敏感），为避免嵌套替换，逐一处理
            String tagPrefix = "{\\b1\\1c&H00FFFF&\\fscx=110\\fscy=110}"; // 加粗、青黄色、放大
            String tagSuffix = "{\\b0\\1c&HFFFFFF&\\fscx=100\\fscy=100}"; // 还原标题样
            text = text.replace(kw, tagPrefix + kw + tagSuffix);
        }
        return text;
    }
}


