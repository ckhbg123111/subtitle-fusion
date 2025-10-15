package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.nio.file.Path;
import java.util.List;

/**
 * 构建 SVG 叠加相关链片段（静态显示，时间窗控制）。
 */
public class SvgOverlayBuilder {

    public String applySvgOverlays(List<String> chains,
                                   VideoChainRequest.SegmentInfo seg,
                                   List<Path> svgs,
                                   int svgBaseIndex,
                                   String last,
                                   OverlayTagSupplier tagSupplier) {
        if (svgs == null || svgs.isEmpty() || seg.getSvgInfos() == null || seg.getSvgInfos().isEmpty()) return last;
        for (int i = 0; i < svgs.size(); i++) {
            if (i >= seg.getSvgInfos().size()) break;
            int inIndex = svgBaseIndex + i;
            VideoChainRequest.SvgInfo si = seg.getSvgInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(si.getStartTime());
            String endSec = FilterExprUtils.toSeconds(si.getEndTime());

            String baseX = (si.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            String baseY = "(H-h)/2";

            String pLoop = tagSupplier.tag();
            chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);

            String ptrim = tagSupplier.tag();
            chains.add(pLoop + "trim=duration=" + FilterExprUtils.calcDuration(startSec, endSec) + ptrim);

            String pshift = tagSupplier.tag();
            chains.add(ptrim + "setpts=PTS+" + startSec + "/TB" + pshift);

            String out = tagSupplier.tag();
            chains.add(last + pshift + "overlay=x=" + baseX + ":y=" + baseY + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
            last = out;
        }
        return last;
    }

    /**
     * 供外部（如 FilterChainBuilder）传入，以保持统一的中间标签生成风格。
     */
    public interface OverlayTagSupplier {
        String tag();
    }
}


