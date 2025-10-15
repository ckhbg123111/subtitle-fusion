package com.zhongjia.subtitlefusion.ffmpeg;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.util.List;

/**
 * 构建关键字文字 drawtext 相关链片段。
 */
public class TextOverlayBuilder {

    private final AppProperties props;

    public TextOverlayBuilder(AppProperties props) {
        this.props = props;
    }

    public String applyKeywords(List<String> chains,
                                VideoChainRequest.SegmentInfo seg,
                                String last) {
        if (seg.getKeywordsInfos() == null) return last;
        for (VideoChainRequest.KeywordsInfo ki : seg.getKeywordsInfos()) {
            String font = props.getRender().getFontFile() != null && !props.getRender().getFontFile().isEmpty()
                    ? ":fontfile='" + FilterExprUtils.escapeFilterPath(props.getRender().getFontFile()) + "'"
                    : ":fontfile='/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc'";
            String color = "white";
            String xExpr = (ki.getPosition() == VideoChainRequest.Position.LEFT) ? "(w-tw)/6" : "w-tw-(w*0.05)";
            String baseY = "h*0.85-th";
            String startSec = FilterExprUtils.toSeconds(ki.getStartTime());
            String endSec = FilterExprUtils.toSeconds(ki.getEndTime());
            String inDur = "0.30";
            String outDur = "0.30";
            String dist = "min(h*0.08,120)";
            String yExpr = "if(lt(t," + startSec + ")," + baseY + "-" + dist + ",if(lt(t," + startSec + "+" + inDur + "),(" + baseY + "-" + dist + ")+((t-" + startSec + ")/" + inDur + ")*" + dist + ",if(lt(t," + endSec + "-" + outDur + ")," + baseY + ",(" + baseY + ")+((t-(" + endSec + "-" + outDur + "))/" + outDur + ")*" + dist + ")))";
            String pos = "x=" + xExpr + ":y='" + FilterExprUtils.escapeExpr(yExpr) + "'";
            String out = "[v" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "]";
            chains.add(last + "drawtext=text='" + FilterExprUtils.escapeText(ki.getKeyword()) + "'" + font + ":fontcolor=" + color + ":fontsize=h*0.04:shadowx=2:shadowy=2:shadowcolor=black@0.7:" + pos + ":enable='between(t," + startSec + "," + endSec + ")'" + out);
            last = out;
        }
        return last;
    }
}


