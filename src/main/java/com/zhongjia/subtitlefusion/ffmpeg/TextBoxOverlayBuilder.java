package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.util.TextLayoutUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 图片+文字文本框叠加：先叠加固定尺寸的 box 图片，再用 drawtext 在其中心区域内绘制文本。
 */
@Component
public class TextBoxOverlayBuilder {

    @Autowired
    private AppProperties props;

    public interface OverlayTagSupplier { String tag(); }

    public String applyTextBoxes(List<String> chains,
                                 VideoChainRequest.SegmentInfo seg,
                                 List<Path> boxImages,
                                 int baseInputIndex,
                                 String last,
                                 OverlayTagSupplier tagSupplier) throws Exception {
        if (seg.getTextBoxInfos() == null || seg.getTextBoxInfos().isEmpty()) return last;
        if (boxImages == null || boxImages.isEmpty()) return last;

        // 预计算：左右车道分配，避免同侧同窗重叠；按固定 boxHeight 进行垂直间距规划
        LanePlan lanePlan = planLanes(seg);

        for (int i = 0; i < seg.getTextBoxInfos().size() && i < boxImages.size(); i++) {
            VideoChainRequest.TextBoxInfo tb = seg.getTextBoxInfos().get(i);
            VideoChainRequest.BoxInfo bi = tb.getBoxInfo();
            if (bi == null) continue;
            int boxW = safeInt(bi.getBoxWidth(), 600);
            int boxH = safeInt(bi.getBoxHeight(), 300);
            int textW = safeInt(bi.getTextWidth(), (int) Math.round(boxW * 0.8));
            int textH = safeInt(bi.getTextHeight(), (int) Math.round(boxH * 0.6));

            String start = FilterExprUtils.toSeconds(tb.getStartTime());
            String end   = FilterExprUtils.toSeconds(tb.getEndTime());

            // 水平中心：左右各占 1/4 与 3/4 的中心
            String centerX = (tb.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.25" : "W*0.75";
            // 垂直中心：基于车道规划（以固定 boxH 作为间距单元）
            int lane = (tb.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLanes[i] : lanePlan.rightLanes[i];
            int laneCnt = (tb.getPosition() == VideoChainRequest.Position.LEFT) ? lanePlan.leftLaneCount : lanePlan.rightLaneCount;
            String centerY = laneCenterY(lane, laneCnt, boxH);

            // 1) 对 box 图做缩放
            int inputIndex = baseInputIndex + i;
            String in = "[" + inputIndex + ":v]";
            String scaled = tagSupplier.tag();
            chains.add(in + "scale=" + boxW + ":" + boxH + scaled);

            // 2) overlay box（居中对齐）
            String outOverlay = tagSupplier.tag();
            String x0 = "(" + centerX + ")-" + boxW + "/2";
            String y0 = "(" + centerY + ")-" + boxH + "/2";
            chains.add(last + scaled + "overlay=x='" + FilterExprUtils.escapeExpr(x0) + "':y='" + FilterExprUtils.escapeExpr(y0) +
                    "':enable='between(t," + start + "," + end + ")'" + outOverlay);
            last = outOverlay;

            // 3) drawtext 文本（同一中心内）
            // 文本样式与字体
            VideoChainRequest.TextStyle style = tb.getTextStyle();
            String fontFile = (style != null && style.getFontFile() != null && !style.getFontFile().isEmpty())
                    ? style.getFontFile() : props.getRender().getFontFile();
            String fontColor = (style != null && style.getFontColor() != null && !style.getFontColor().isEmpty())
                    ? normalizeColor(style.getFontColor()) : normalizeColor(props.getRender().getFontColor());
            int minSize = (style != null && style.getFontSizeMin() != null) ? style.getFontSizeMin() : 14;
            int maxSize = (style != null && style.getFontSizeMax() != null) ? style.getFontSizeMax() : 48;
            int lineSpacing = (style != null && style.getLineSpacing() != null) ? style.getLineSpacing() : 6;

            Path fontPath = (fontFile != null && !fontFile.isEmpty()) ? Paths.get(fontFile) : null;
            if (fontPath == null) {
                // 提供一个常见中文字体的默认路径
                fontFile = "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc";
                fontPath = Paths.get(fontFile);
            }

            TextLayoutUtils.Result layout = TextLayoutUtils.layout(
                    tb.getText(), textW, textH, fontPath, minSize, maxSize, lineSpacing
            );

            String fontExpr = ":fontfile='" + FilterExprUtils.escapeFilterPath(fontPath.toString()) + "'";
            String textEsc = FilterExprUtils.escapeText(layout.textWithNewlines);
            String xText = "(" + centerX + ")-" + "tw/2";
            String yText = "(" + centerY + ")-" + "th/2";
            String outText = tagSupplier.tag();
            chains.add(last + "drawtext=text='" + textEsc + "'" + fontExpr
                    + ":fontcolor=" + fontColor
                    + ":fontsize=" + layout.fontSize
                    + ":line_spacing=" + layout.lineSpacing
                    + ":x='" + FilterExprUtils.escapeExpr(xText) + "'"
                    + ":y='" + FilterExprUtils.escapeExpr(yText) + "'"
                    + ":enable='between(t," + start + "," + end + ")'" + outText);
            last = outText;
        }
        return last;
    }

    private String laneCenterY(int lane, int laneCnt, int boxH) {
        double centerShift = lane - (laneCnt - 1) / 2.0;
        return "H/2 + " + String.format(Locale.US, "%.3f", centerShift) + "*(" + boxH + "+20)";
    }

    private int safeInt(Integer v, int def) { return v == null ? def : v; }

    private String normalizeColor(String c) {
        if (c == null || c.isEmpty()) return "white";
        String s = c.trim();
        if (s.startsWith("#")) {
            // drawtext 支持 #RRGGBB
            return s;
        }
        return s;
    }

    private LanePlan planLanes(VideoChainRequest.SegmentInfo seg) {
        int n = seg.getTextBoxInfos().size();
        int[] left = new int[n];
        int[] right = new int[n];
        List<Double> leftEnds = new ArrayList<>();
        List<Double> rightEnds = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            VideoChainRequest.TextBoxInfo tb = seg.getTextBoxInfos().get(i);
            double s = parse(FilterExprUtils.toSeconds(tb.getStartTime()));
            double e = parse(FilterExprUtils.toSeconds(tb.getEndTime()));
            if (tb.getPosition() == VideoChainRequest.Position.LEFT) {
                int lane = firstNonOverlappingLane(leftEnds, s);
                if (lane == leftEnds.size()) leftEnds.add(e); else leftEnds.set(lane, e);
                left[i] = lane;
            } else {
                int lane = firstNonOverlappingLane(rightEnds, s);
                if (lane == rightEnds.size()) rightEnds.add(e); else rightEnds.set(lane, e);
                right[i] = lane;
            }
        }

        LanePlan p = new LanePlan();
        p.leftLanes = left;
        p.rightLanes = right;
        p.leftLaneCount = leftEnds.size();
        p.rightLaneCount = rightEnds.size();
        return p;
    }

    private int firstNonOverlappingLane(List<Double> laneEndTimes, double start) {
        for (int i = 0; i < laneEndTimes.size(); i++) {
            if (start >= laneEndTimes.get(i)) return i;
        }
        return laneEndTimes.size();
    }

    private double parse(String s) { try { return Double.parseDouble(s); } catch (Exception ignore) { return 0d; } }

    private static final class LanePlan {
        int[] leftLanes;
        int[] rightLanes;
        int leftLaneCount;
        int rightLaneCount;
    }
}


