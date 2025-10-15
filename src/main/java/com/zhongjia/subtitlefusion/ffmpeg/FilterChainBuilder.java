package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.ffmpeg.effect.FloatWaveEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.LeftInRightOutEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectStrategy;
import com.zhongjia.subtitlefusion.ffmpeg.effect.OverlayEffectSupport;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 负责构建 FFmpeg filter_complex 字符串。
 */
@Component
public class FilterChainBuilder implements OverlayEffectSupport {

    private final AppProperties props;
    private final TextOverlayBuilder textOverlayBuilder;
    private final SvgOverlayBuilder svgOverlayBuilder;

    public FilterChainBuilder(AppProperties props) {
        this.props = props;
        this.textOverlayBuilder = new TextOverlayBuilder(props);
        this.svgOverlayBuilder = new SvgOverlayBuilder();
    }

    public String buildFilterChain(VideoChainRequest.SegmentInfo seg, List<Path> pictures, List<Path> svgs, Path srt, boolean hasAudio) {
        boolean hasPics = pictures != null && !pictures.isEmpty();
        boolean hasSvgs = svgs != null && !svgs.isEmpty();
        boolean hasKeywords = seg.getKeywordsInfos() != null && !seg.getKeywordsInfos().isEmpty();
        boolean hasSrt = srt != null;
        if (!hasPics && !hasSvgs && !hasKeywords && !hasSrt) {
            return ""; // 无任何滤镜
        }

        List<String> chains = new ArrayList<>();
        String last = "[0:v]";
        int picBaseIndex = hasAudio ? 2 : 1; // 0:v (+1:a) 之后的图片输入索引

        last = applyPictureOverlays(chains, seg, pictures, picBaseIndex, last);
        last = svgOverlayBuilder.applySvgOverlays(chains, seg, svgs, picBaseIndex + (pictures != null ? pictures.size() : 0), last, this::tag);
        last = textOverlayBuilder.applyKeywords(chains, seg, last);
        applySubtitleOrFormat(chains, last, srt);
        return String.join(";", chains);
    }

    private String applyPictureOverlays(List<String> chains,
                                        VideoChainRequest.SegmentInfo seg,
                                        List<Path> pictures,
                                        int picBaseIndex,
                                        String last) {
        if (pictures == null || pictures.isEmpty()) return last;
        for (int i = 0; i < pictures.size(); i++) {
            int inIndex = picBaseIndex + i;
            if (seg.getPictureInfos() == null || i >= seg.getPictureInfos().size()) continue;
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            String startSec = FilterExprUtils.toSeconds(pi.getStartTime());
            String endSec = FilterExprUtils.toSeconds(pi.getEndTime());

            String baseX = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            String baseY = "(H-h)/2";

            OverlayEffectStrategy strategy = resolveStrategy(pi);
            String out = strategy.apply(chains, last, inIndex, startSec, endSec, baseX, baseY, this, pi);
            last = out;
        }
        return last;
    }

    private void applySubtitleOrFormat(List<String> chains,
                                       String last,
                                       Path srt) {
        if (srt != null) {
            String style = "force_style='FontName=" + FilterExprUtils.safe(props.getRender().getFontFamily(), "Microsoft YaHei") + ",FontSize=18,Outline=1,Shadow=1'";
            String srtPathEscaped = FilterExprUtils.escapeFilterPath(srt.toAbsolutePath().toString());
            chains.add(last + "subtitles='" + srtPathEscaped + "':" + style + "[vout]");
        } else {
            chains.add(last + "format=yuv420p[vout]");
        }
    }

    @Override
    public String tag() {
        return "[v" + UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "]";
    }

    private OverlayEffectStrategy resolveStrategy(VideoChainRequest.PictureInfo pi) {
        VideoChainRequest.OverlayEffectType type = pi.getEffectType();
        if (type == null) type = VideoChainRequest.OverlayEffectType.FLOAT_WAVE;
        switch (type) {
            case LEFT_IN_RIGHT_OUT:
                return new LeftInRightOutEffectStrategy();
            case FLOAT_WAVE:
            default:
                return new FloatWaveEffectStrategy();
        }
    }

    // 保留统一入口，便于策略实现访问；目前内部直接委托给工具类
    public static String toSeconds(String t) { return FilterExprUtils.toSeconds(t); }
    public static String escapeExpr(String expr) { return FilterExprUtils.escapeExpr(expr); }
    public static String escapeText(String s) { return FilterExprUtils.escapeText(s); }
    public static String escapeFilterPath(String path) { return FilterExprUtils.escapeFilterPath(path); }
    public static String safe(String v, String def) { return FilterExprUtils.safe(v, def); }
    public static String calcDuration(String startSec, String endSec) { return FilterExprUtils.calcDuration(startSec, endSec); }
}


