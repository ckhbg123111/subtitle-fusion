package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.config.AppProperties;
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
    private final PictureOverlayBuilder pictureOverlayBuilder;

    public FilterChainBuilder(AppProperties props, PictureOverlayBuilder pictureOverlayBuilder) {
        this.props = props;
        this.textOverlayBuilder = new TextOverlayBuilder(props);
        this.svgOverlayBuilder = new SvgOverlayBuilder();
        this.pictureOverlayBuilder = pictureOverlayBuilder;
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

        last = pictureOverlayBuilder.apply(chains, seg, pictures, picBaseIndex, last, this::tag);
        last = svgOverlayBuilder.applySvgOverlays(chains, seg, svgs, picBaseIndex + (pictures != null ? pictures.size() : 0), last, this::tag);
        last = textOverlayBuilder.applyKeywords(chains, seg, last);
        applySubtitleOrFormat(chains, last, srt);
        return String.join(";", chains);
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


    // 保留统一入口，便于策略实现访问；目前内部直接委托给工具类
    public static String toSeconds(String t) { return FilterExprUtils.toSeconds(t); }
    public static String escapeExpr(String expr) { return FilterExprUtils.escapeExpr(expr); }
    public static String escapeText(String s) { return FilterExprUtils.escapeText(s); }
    public static String escapeFilterPath(String path) { return FilterExprUtils.escapeFilterPath(path); }
    public static String safe(String v, String def) { return FilterExprUtils.safe(v, def); }
    public static String calcDuration(String startSec, String endSec) { return FilterExprUtils.calcDuration(startSec, endSec); }
}


