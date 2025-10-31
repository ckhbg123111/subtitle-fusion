package com.zhongjia.subtitlefusion.ffmpeg.ass;

import com.zhongjia.subtitlefusion.ffmpeg.FilterExprUtils;
import com.zhongjia.subtitlefusion.ffmpeg.PictureOverlayBuilder;
import com.zhongjia.subtitlefusion.model.SubtitleFusionV2Request;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 针对 V2 请求的 ASS + 插图滤镜链生成器。
 */
@Component
public class AssFilterChainBuilder {

    @Autowired
    private PictureOverlayBuilder pictureOverlayBuilder;

    public String build(SubtitleFusionV2Request.SubtitleInfo subtitleInfo,
                        List<Path> picturePaths,
                        Path assFile,
                        boolean hasAudio) {
        List<String> chains = new ArrayList<>();
        String last = "[0:v]";

        VideoChainRequest.SegmentInfo seg = adaptSegment(subtitleInfo);
        // 对于 /api/subtitles/burn-as-ass/async 命令拼装：只添加了一个视频输入，随后直接添加图片输入，
        // 即图片文件从输入索引 1 开始；是否存在音轨不影响输入“文件”索引。
        int picBaseIndex = 1;
        last = pictureOverlayBuilder.apply(chains, seg, picturePaths, picBaseIndex, last, this::tag);
        // V2 不处理 SVG 叠加

        // 使用 ASS 滤镜
        String assPathEscaped = FilterExprUtils.escapeFilterPath(assFile.toAbsolutePath().toString());
        chains.add(last + "ass='" + assPathEscaped + "'[vout]");
        return String.join(";", chains);
    }

    private VideoChainRequest.SegmentInfo adaptSegment(SubtitleFusionV2Request.SubtitleInfo info) {
        VideoChainRequest.SegmentInfo seg = new VideoChainRequest.SegmentInfo();
        List<VideoChainRequest.PictureInfo> pics = new ArrayList<>();
        if (info != null && info.getPictureInfoList() != null) {
            for (SubtitleFusionV2Request.PictureInfo p : info.getPictureInfoList()) {
                VideoChainRequest.PictureInfo pi = new VideoChainRequest.PictureInfo();
                pi.setPictureUrl(p.getPictureUrl());
                pi.setStartTime(p.getStartTime());
                pi.setEndTime(p.getEndTime());
                // V2 未提供位置，默认左侧；可后续扩展
                pi.setPosition(VideoChainRequest.Position.LEFT);
                pi.setEffectType(p.getEffectType());
                pics.add(pi);
            }
        }
        seg.setPictureInfos(pics);
        return seg;
    }

    private String tag() {
        return "[v" + UUID.randomUUID().toString().replace("-", "").substring(0, 6) + "]";
    }
}


