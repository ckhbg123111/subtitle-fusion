package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.enums.AssSubtitleEffectTypeEnum;
import org.springframework.stereotype.Component;

@Component
public class AssEffectStrategyResolver {

    private final AssEffectStrategy defaultStrategy = new DefaultAssEffectStrategy();
    private final AssEffectStrategy typewriterStrategy = new TypewriterCursorStrategy();
    private final AssEffectStrategy leftInBounceStrategy = new LeftInBounceStrategy();
    private final AssEffectStrategy keywordHighlightStrategy = new KeywordHighlightStrategy();

    public AssEffectStrategy resolve(SubtitleFusionV2Request.CommonSubtitleInfo line) {
        SubtitleFusionV2Request.SubtitleEffectInfo eff = line.getSubtitleEffectInfo();
        if (eff == null || eff.getEffectType() == null) {
            return defaultStrategy;
        }
        AssSubtitleEffectTypeEnum t = eff.getEffectType();
        switch (t) {
            case TYPEWRITER_CURSOR: return typewriterStrategy;
            case LEFT_IN_BOUNCE: return leftInBounceStrategy;
            case KEYWORD_HIGHLIGHT: return keywordHighlightStrategy;
            case DEFAULT:
            default: return defaultStrategy;
        }
    }
}


