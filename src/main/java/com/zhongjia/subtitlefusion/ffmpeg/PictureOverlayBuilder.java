package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectSupport;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategyResolver;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

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
        
        // 预计算：按侧（LEFT/RIGHT）进行时间重叠的车道分配，避免同侧同时间堆叠
        LanePlan lanePlan = planPictureLanes(seg);

        OverlayEffectSupport support = tagSupplier::tag;
        // 按业务图片数量遍历；边框作为额外输入参与索引偏移
        for (int i = 0; i < seg.getPictureInfos().size(); i++) {
            int borderBefore = countBordersBefore(seg, i);
            int inIndexPic = picBaseIndex + i + borderBefore;
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(pi.getStartTime());
            String endSec = FilterExprUtils.toSeconds(pi.getEndTime());

            String baseX = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            // 基于车道计算 Y，使同侧重叠元素分不同行
            int lane = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLanes[i] : lanePlan.rightLanes[i];
            int laneCnt = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLaneCount : lanePlan.rightLaneCount;
            String baseY = laneBaseY(lane, laneCnt);

            OverlayEffectStrategy strategy = strategyResolver.resolve(pi);
            String out = strategy.apply(chains, last, inIndexPic, startSec, endSec, baseX, baseY, support, pi);
            last = out;

            // 如果存在边框：索引紧随主图 +1，动效与坐标一致，覆盖在主图之上
            if (pi.getImageBorderUrl() != null && !pi.getImageBorderUrl().isEmpty()) {
                int inIndexBorder = inIndexPic + 1;
                String outBorder = strategy.apply(chains, last, inIndexBorder, startSec, endSec, baseX, baseY, support, pi);
                last = outBorder;
            }
        }
        return last;
    }

    private int countBordersBefore(VideoChainRequest.SegmentInfo seg, int idx) {
        int c = 0;
        for (int k = 0; k < idx; k++) {
            VideoChainRequest.PictureInfo prev = seg.getPictureInfos().get(k);
            if (prev.getImageBorderUrl() != null && !prev.getImageBorderUrl().isEmpty()) c++;
        }
        return c;
    }

    private String laneBaseY(int lane, int laneCnt) {
        // 以画面垂直居中为基准，按车道向上/下对称分布；相邻车道间距 (h+20)
        double centerShift = lane - (laneCnt - 1) / 2.0;
        return "(H-h)/2 + " + String.format(Locale.US, "%.3f", centerShift) + "*(h+20)";
    }

    private LanePlan planPictureLanes(VideoChainRequest.SegmentInfo seg) {
        int n = seg.getPictureInfos().size();
        int[] leftLanes = new int[n];
        int[] rightLanes = new int[n];

        List<Double> leftEnds = new ArrayList<>();
        List<Double> rightEnds = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            double s = parse(FilterExprUtils.toSeconds(pi.getStartTime()));
            double e = parse(FilterExprUtils.toSeconds(pi.getEndTime()));

            if (pi.getPosition() == VideoChainRequest.Position.LEFT) {
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

    private static class LanePlan {
        int[] leftLanes;
        int[] rightLanes;
        int leftLaneCount;
        int rightLaneCount;
    }

    public interface OverlayTagSupplier {
        String tag();
    }
}


