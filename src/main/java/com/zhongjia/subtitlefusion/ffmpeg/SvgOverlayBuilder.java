package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectSupport;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.FloatWaveEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.LeftInRightOutEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.TopInFadeOutSvgEffectStrategy;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.nio.file.Path;
import java.util.List;

/**
 * 构建 SVG 叠加相关链片段（使用策略模式注入动效）。
 */
public class SvgOverlayBuilder {

    public String applySvgOverlays(List<String> chains,
                                   VideoChainRequest.SegmentInfo seg,
                                   List<Path> svgs,
                                   int svgBaseIndex,
                                   String last,
                                   OverlayTagSupplier tagSupplier) {
        if (svgs == null || svgs.isEmpty() || seg.getSvgInfos() == null || seg.getSvgInfos().isEmpty()) return last;
        OverlayEffectSupport support = tagSupplier::tag;
        for (int i = 0; i < svgs.size(); i++) {
            if (i >= seg.getSvgInfos().size()) break;
            int inIndex = svgBaseIndex + i;
            VideoChainRequest.SvgInfo si = seg.getSvgInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(si.getStartTime());
            String endSec = FilterExprUtils.toSeconds(si.getEndTime());

            String baseX = (si.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            String baseY = "(H-h)/2";

            OverlayEffectStrategy strategy = resolveStrategy(si);
            String out = strategy.apply(chains, last, inIndex, startSec, endSec, baseX, baseY, support, si);
            last = out;
        }
        return last;
    }

    private OverlayEffectStrategy resolveStrategy(VideoChainRequest.SvgInfo si) {
        VideoChainRequest.OverlayEffectType type = si.getEffectType();
        if (type == null) type = VideoChainRequest.OverlayEffectType.TOP_IN_FADE_OUT;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return new LeftInRightOutEffectStrategy();
            case FLOAT_WAVE:
                return new FloatWaveEffectStrategy();
            case TOP_IN_FADE_OUT:
            default:
                return new TopInFadeOutSvgEffectStrategy();
        }
    }

    /**
     * 供外部（如 FilterChainBuilder）传入，以保持统一的中间标签生成风格。
     */
    public interface OverlayTagSupplier {
        String tag();
    }
}


