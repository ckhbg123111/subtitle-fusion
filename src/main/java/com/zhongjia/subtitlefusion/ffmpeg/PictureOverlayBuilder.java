package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectSupport;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategyResolver;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;

/**
 * 构建图片叠加滤镜片段的 Builder。
 */
@Component
public class PictureOverlayBuilder {

    @Autowired
    private OverlayEffectStrategyResolver strategyResolver;

    public String apply(List<String> chains,
                        VideoChainRequest.SegmentInfo seg,
                        List<Path> pictures,
                        int picBaseIndex,
                        String last,
                        OverlayTagSupplier tagSupplier) {
        if (pictures == null || pictures.isEmpty()) return last;
        if (seg.getPictureInfos() == null || seg.getPictureInfos().isEmpty()) return last;

        OverlayEffectSupport support = tagSupplier::tag;
        for (int i = 0; i < pictures.size(); i++) {
            if (i >= seg.getPictureInfos().size()) break;
            int inIndex = picBaseIndex + i;
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(pi.getStartTime());
            String endSec = FilterExprUtils.toSeconds(pi.getEndTime());

            String baseX = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            String baseY = "(H-h)/2";

            OverlayEffectStrategy strategy = strategyResolver.resolve(pi);
            String out = strategy.apply(chains, last, inIndex, startSec, endSec, baseX, baseY, support, pi);
            last = out;
        }
        return last;
    }

    public interface OverlayTagSupplier {
        String tag();
    }
}


