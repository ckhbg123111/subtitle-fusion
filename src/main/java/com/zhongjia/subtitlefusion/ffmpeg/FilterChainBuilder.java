package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 负责构建 FFmpeg filter_complex 字符串。
 */
@Component
public class FilterChainBuilder {

    private final AppProperties props;

    public FilterChainBuilder(AppProperties props) {
        this.props = props;
    }

    public String buildFilterChain(VideoChainRequest.SegmentInfo seg, List<Path> pictures, Path srt, boolean hasAudio) {
        boolean hasPics = pictures != null && !pictures.isEmpty();
        boolean hasKeywords = seg.getKeywordsInfos() != null && !seg.getKeywordsInfos().isEmpty();
        boolean hasSrt = srt != null;
        if (!hasPics && !hasKeywords && !hasSrt) {
            return ""; // 无任何滤镜
        }

        List<String> chains = new ArrayList<>();
        String last = "[0:v]";
        int picBaseIndex = hasAudio ? 2 : 1; // 0:v (+1:a) 之后的图片输入索引

        for (int i = 0; i < (pictures != null ? pictures.size() : 0); i++) {
            int inIndex = picBaseIndex + i;
            VideoChainRequest.PictureInfo pi = seg.getPictureInfos().get(i);
            String startSec = toSeconds(pi.getStartTime());
            String endSec = toSeconds(pi.getEndTime());

            String baseX = (pi.getPosition() == VideoChainRequest.Position.LEFT) ? "W*0.05" : "W-w-W*0.05";
            String baseY = "(H-h)/2";

            String pLoop = tag();
            chains.add("[" + inIndex + ":v]format=rgba,loop=loop=-1:size=1:start=0,setpts=N/FRAME_RATE/TB" + pLoop);

            String c0 = tag();
            String c1 = tag();
            chains.add("color=c=black@0.0:s=64x64:r=60" + c0);
            chains.add("color=c=black@0.0:s=64x64:r=60" + c1);

            String cmx = tag();
            String p1 = tag();
            chains.add(c0 + pLoop + "scale2ref" + cmx + p1);

            String cmy = tag();
            String p2 = tag();
            chains.add(c1 + p1 + "scale2ref" + cmy + p2);

            String mx = tag();
            String my = tag();
            chains.add(cmx + "geq=lum='128+20*sin(2*PI*(Y/32)+T*6)'" + mx);
            chains.add(cmy + "geq=lum='128+10*sin(2*PI*(X/32)+T*6)'" + my);

            String pwave = tag();
            chains.add(p2 + mx + my + "displace=edge=smear" + pwave);

            String xMove = baseX + "+(W*0.02)*sin(2*PI*(t*0.6))";
            String yMove = baseY + "+(H*0.02)*sin(2*PI*(t*0.7))";

            String out = tag();
            chains.add(last + pwave + "overlay=x=" + xMove + ":y=" + yMove + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
            last = out;
        }

        if (seg.getKeywordsInfos() != null) {
            for (VideoChainRequest.KeywordsInfo ki : seg.getKeywordsInfos()) {
                String font = props.getRender().getFontFile() != null && !props.getRender().getFontFile().isEmpty()
                        ? ":fontfile='" + escapeFilterPath(props.getRender().getFontFile()) + "'"
                        : ":fontfile='/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc'";
                String color = "white";
                String xExpr = (ki.getPosition() == VideoChainRequest.Position.LEFT) ? "(w-tw)/6" : "w-tw-(w*0.05)";
                String baseY = "h*0.85-th";
                String startSec = toSeconds(ki.getStartTime());
                String endSec = toSeconds(ki.getEndTime());
                String inDur = "0.30";
                String outDur = "0.30";
                String dist = "min(h*0.08,120)";
                String yExpr = "if(lt(t," + startSec + ")," + baseY + "-" + dist + ",if(lt(t," + startSec + "+" + inDur + "),(" + baseY + "-" + dist + ")+((t-" + startSec + ")/" + inDur + ")*" + dist + ",if(lt(t," + endSec + "-" + outDur + ")," + baseY + ",(" + baseY + ")+((t-(" + endSec + "-" + outDur + "))/" + outDur + ")*" + dist + ")))";
                String pos = "x=" + xExpr + ":y=" + yExpr;
                String out = tag();
                chains.add(last + "drawtext=text='" + escapeText(ki.getKeyword()) + "'" + font + ":fontcolor=" + color + ":fontsize=h*0.04:shadowx=2:shadowy=2:shadowcolor=black@0.7:" + pos + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
                last = out;
            }
        }

        if (srt != null) {
            String style = "force_style='FontName=" + safe(props.getRender().getFontFamily(), "Microsoft YaHei") + ",FontSize=18,Outline=1,Shadow=1'";
            String srtPathEscaped = escapeFilterPath(srt.toAbsolutePath().toString());
            chains.add(last + "subtitles='" + srtPathEscaped + "':" + style + "[vout]");
        } else {
            chains.add(last + "format=yuv420p[vout]");
        }
        return String.join(";", chains);
    }

    private String tag() {
        return "[v" + UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "]";
    }

    private String escapeText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace(":", "\\:");
    }

    private String escapeFilterPath(String path) {
        if (path == null) return "";
        String normalized = path.replace("\\", "/");
        normalized = normalized.replace(":", "\\:");
        normalized = normalized.replace("'", "\\'");
        return normalized;
    }

    private String toSeconds(String t) {
        if (t == null || t.isEmpty()) return "0";
        try {
            if (t.contains(":")) {
                String[] ps = t.split(":");
                double h = Double.parseDouble(ps[0]);
                double m = Double.parseDouble(ps[1]);
                double s = Double.parseDouble(ps[2]);
                return String.format(Locale.US, "%.3f", h * 3600 + m * 60 + s);
            }
            return String.format(Locale.US, "%.3f", Double.parseDouble(t));
        } catch (Exception e) {
            return "0";
        }
    }

    private String safe(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }
}


