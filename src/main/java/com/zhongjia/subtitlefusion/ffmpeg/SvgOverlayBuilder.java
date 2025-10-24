package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectSupport;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.FloatWaveEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.LeftInRightOutEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.LeftInBlindsOutEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.TopInFadeOutSvgEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.FadeInFadeOutEffectStrategy;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

/**
 * 构建 SVG 叠加相关链片段（使用策略模式注入动效）。
 */
@Component
public class SvgOverlayBuilder {

    @Autowired
    private FloatWaveEffectStrategy floatWaveEffectStrategy;
    @Autowired
    private LeftInRightOutEffectStrategy leftInRightOutEffectStrategy;
    @Autowired
    private LeftInBlindsOutEffectStrategy leftInBlindsOutEffectStrategy;
    @Autowired
    private TopInFadeOutSvgEffectStrategy topInFadeOutSvgEffectStrategy;
	@Autowired
	private FadeInFadeOutEffectStrategy fadeInFadeOutEffectStrategy;

    public String applySvgOverlays(List<String> chains,
                                   VideoChainRequest.SegmentInfo seg,
                                   List<Path> svgs,
                                   int svgBaseIndex,
                                   String last,
                                   OverlayTagSupplier tagSupplier) {
        if (svgs == null || svgs.isEmpty() || seg.getSvgInfos() == null || seg.getSvgInfos().isEmpty()) return last;
        
        // 预计算：按侧（LEFT/RIGHT）进行时间重叠的车道分配
        LanePlan lanePlan = planSvgLanes(seg);

        OverlayEffectSupport support = tagSupplier::tag;
        for (int i = 0; i < svgs.size(); i++) {
            if (i >= seg.getSvgInfos().size()) break;
            int inIndex = svgBaseIndex + i;
            VideoChainRequest.SvgInfo si = seg.getSvgInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(si.getStartTime());
            String endSec = FilterExprUtils.toSeconds(si.getEndTime());

            String baseX = (si.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            int lane = (si.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLanes[i] : lanePlan.rightLanes[i];
            int laneCnt = (si.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLaneCount : lanePlan.rightLaneCount;
            String baseY = laneBaseY(lane, laneCnt);

            OverlayEffectStrategy strategy = resolveStrategy(si);
            String out = strategy.apply(chains, last, inIndex, startSec, endSec, baseX, baseY, support, si);
            last = out;
        }
        return last;
    }

    private String laneBaseY(int lane, int laneCnt) {
        double centerShift = lane - (laneCnt - 1) / 2.0;
        return "(H-h)/2 + " + String.format(Locale.US, "%.3f", centerShift) + "*(h+20)";
    }

    private LanePlan planSvgLanes(VideoChainRequest.SegmentInfo seg) {
        int n = seg.getSvgInfos().size();
        int[] leftLanes = new int[n];
        int[] rightLanes = new int[n];

        List<Double> leftEnds = new ArrayList<>();
        List<Double> rightEnds = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            VideoChainRequest.SvgInfo si = seg.getSvgInfos().get(i);
            double s = parse(FilterExprUtils.toSeconds(si.getStartTime()));
            double e = parse(FilterExprUtils.toSeconds(si.getEndTime()));

            if (si.getPosition() == VideoChainRequest.Position.LEFT) {
                int lane = firstNonOverlappingLane(leftEnds, s);
                if (lane == leftEnds.size()) leftEnds.add(e); else leftEnds.set(lane, e);
                leftLanes[i] = lane;
            } else {
                int lane = firstNonOverlappingLane(rightEnds, s);
                if (lane == rightEnds.size()) rightEnds.add(e); else rightEnds.set(lane, e);
                rightLanes[i] = lane;
            }
        }

        LanePlan plan = new LanePlan();
        plan.leftLanes = leftLanes;
        plan.rightLanes = rightLanes;
        plan.leftLaneCount = leftEnds.size();
        plan.rightLaneCount = rightEnds.size();
        return plan;
    }

    private int firstNonOverlappingLane(List<Double> laneEndTimes, double start) {
        for (int i = 0; i < laneEndTimes.size(); i++) {
            if (start >= laneEndTimes.get(i)) return i;
        }
        return laneEndTimes.size();
    }

    private double parse(String s) {
        try { return Double.parseDouble(s); } catch (Exception ignore) { return 0d; }
    }

    private OverlayEffectStrategy resolveStrategy(VideoChainRequest.SvgInfo si) {
        OverlayEffectType type = si.getEffectType();
        if (type == null) type = OverlayEffectType.FADE_IN_FADE_OUT;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return leftInRightOutEffectStrategy;
            case LEFT_IN_BLINDS_OUT:
                return leftInBlindsOutEffectStrategy;
            case FLOAT_WAVE:
                return floatWaveEffectStrategy;
            case FADE_IN_FADE_OUT:
                return fadeInFadeOutEffectStrategy;
            case TOP_IN_FADE_OUT:
            default:
                return topInFadeOutSvgEffectStrategy;
        }
    }

    /**
     * 供外部（如 FilterChainBuilder）传入，以保持统一的中间标签生成风格。
     */
    public interface OverlayTagSupplier {
        String tag();
    }

    private static class LanePlan {
        int[] leftLanes;
        int[] rightLanes;
        int leftLaneCount;
        int rightLaneCount;
    }
}


