package com.zhongjia.subtitlefusion.templlll.videochainv2;

import com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;
import com.zhongjia.subtitlefusion.model.VideoChainV2Request;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 将脚本驱动的分段请求（ScriptDrivenSegmentRequest）转换为 VideoChainV2Request。
 *
 * <p>临时实现，集中放在 {@code com.zhongjia.subtitlefusion.temp.videochainv2} 包下，方便后续统一下线。</p>
 */
@Service
@Slf4j
public class ScriptDrivenVideoChainV2Builder {

    /**
     * 从脚本驱动段请求构建 VideoChainV2Request。
     *
     * @param taskId   任务 ID
     * @param requests 脚本段列表
     */
    public VideoChainV2Request build(String taskId, List<ScriptDrivenSegmentRequest> requests) {
        if (CollectionUtils.isEmpty(requests)) {
            throw new IllegalArgumentException("requests 不能为空");
        }
        VideoChainV2Request req = new VideoChainV2Request();
        req.setTaskId(taskId);

        List<VideoChainV2Request.SegmentInfo> segments = new ArrayList<>();
        for (ScriptDrivenSegmentRequest segReq : requests) {
            if (segReq == null) continue;
            VideoChainV2Request.SegmentInfo seg = new VideoChainV2Request.SegmentInfo();

            // 音频
            seg.setAudioUrl(segReq.getAudioUrl());

            // 视频列表
            if (!CollectionUtils.isEmpty(segReq.getVideoInfo())) {
                List<VideoChainV2Request.VideoInfo> videoInfos = new ArrayList<>();
                for (ScriptDrivenSegmentRequest.VideoInfo vi : segReq.getVideoInfo()) {
                    if (vi == null || !StringUtils.hasText(vi.getVideoUrl())) continue;
                    VideoChainV2Request.VideoInfo v = new VideoChainV2Request.VideoInfo();
                    v.setVideoUrl(vi.getVideoUrl());
                    videoInfos.add(v);
                }
                if (!videoInfos.isEmpty()) {
                    seg.setVideoInfos(videoInfos);
                }
            }

            // 底部字幕（基础文字）
            SubtitleInfo bottom = buildBottomSubtitle(segReq.getSubtitleInfo());
            if (bottom != null && !CollectionUtils.isEmpty(bottom.getCommonSubtitleInfoList())) {
                seg.setSubtitleInfo(bottom);
            }

            // 标题字幕（花字）
            SubtitleInfo title = buildTitleSubtitle(segReq.getObjectInfo());
            if (title != null && !CollectionUtils.isEmpty(title.getCommonSubtitleInfoList())) {
                seg.setTextInfo(title);
            }

            // 插图
            List<VideoChainV2Request.PictureInfo> pictures = buildPictures(segReq.getObjectInfo());
            if (!CollectionUtils.isEmpty(pictures)) {
                seg.setPictureInfos(pictures);
            }

            segments.add(seg);
        }
        req.setSegmentList(segments);

        // 段间随机转场（若不足两段则为空列表）
        req.setGapTransitions(VideoChainV2TransitionUtils.buildRandomTransitions(segments.size()));
        return req;
    }

    /**
     * 将脚本中的 subtitle_info 转为底部字幕（统一基础文字样式）。
     */
    private SubtitleInfo buildBottomSubtitle(List<ScriptDrivenSegmentRequest.SubtitleInfo> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        SubtitleTemplate template = VideoChainV2SubtitleTemplates.defaultTemplate();
        SubtitleInfo out = new SubtitleInfo();
        out.setSubtitleTemplate(template);
        List<SubtitleInfo.CommonSubtitleInfo> commons = new ArrayList<>();
        for (ScriptDrivenSegmentRequest.SubtitleInfo it : list) {
            if (it == null || !StringUtils.hasText(it.getText())) continue;
            List<String> tm = it.getTime();
            if (tm == null || tm.size() < 2) continue;
            String start = tm.get(0);
            String end = tm.get(1);
            if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) continue;

            SubtitleInfo.CommonSubtitleInfo ci = new SubtitleInfo.CommonSubtitleInfo();
            ci.setText(it.getText());
            ci.setStartTime(start);
            ci.setEndTime(end);

            SubtitleInfo.SubtitleEffectInfo eff = new SubtitleInfo.SubtitleEffectInfo();
            // 底部字幕统一使用 BASIC 文本策略，对应“基础文字”模板
            eff.setTextStrategy(TextStrategyEnum.BASIC);
            ci.setSubtitleEffectInfo(eff);

            commons.add(ci);
        }
        if (commons.isEmpty()) {
            return null;
        }
        out.setCommonSubtitleInfoList(commons);
        return out;
    }

    /**
     * 将 object_info 中的文字转为标题字幕（花字样式）。
     */
    private SubtitleInfo buildTitleSubtitle(List<ScriptDrivenSegmentRequest.ObjectItem> objects) {
        if (CollectionUtils.isEmpty(objects)) {
            return null;
        }
        SubtitleTemplate template = VideoChainV2SubtitleTemplates.defaultTemplate();
        SubtitleInfo out = new SubtitleInfo();
        out.setSubtitleTemplate(template);
        List<SubtitleInfo.CommonSubtitleInfo> commons = new ArrayList<>();

        for (ScriptDrivenSegmentRequest.ObjectItem obj : objects) {
            if (obj == null) continue;
            if (!"text".equalsIgnoreCase(obj.getType())) continue;
            if (!StringUtils.hasText(obj.getText())) continue;

            List<String> tm = obj.getTime();
            if (tm == null || tm.size() < 2) continue;
            String start = tm.get(0);
            String end = tm.get(1);
            if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) continue;

            SubtitleInfo.CommonSubtitleInfo ci = new SubtitleInfo.CommonSubtitleInfo();
            ci.setText(obj.getText());
            ci.setStartTime(start);
            ci.setEndTime(end);

            SubtitleInfo.SubtitleEffectInfo eff = new SubtitleInfo.SubtitleEffectInfo();
            // 标题统一使用花字策略
            eff.setTextStrategy(TextStrategyEnum.FLOWER);
            ci.setSubtitleEffectInfo(eff);

            commons.add(ci);
        }
        if (commons.isEmpty()) {
            return null;
        }
        out.setCommonSubtitleInfoList(commons);
        return out;
    }

    /**
     * 将 object_info 中的图片转为 PictureInfo。
     */
    private List<VideoChainV2Request.PictureInfo> buildPictures(List<ScriptDrivenSegmentRequest.ObjectItem> objects) {
        List<VideoChainV2Request.PictureInfo> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(objects)) {
            return list;
        }
        for (ScriptDrivenSegmentRequest.ObjectItem obj : objects) {
            if (obj == null) continue;
            if (!"image".equalsIgnoreCase(obj.getType())) continue;

            if (!StringUtils.hasText(obj.getImageUrl())) continue;

            List<String> tm = obj.getTime();
            if (tm == null || tm.size() < 2) continue;
            String start = tm.get(0);
            String end = tm.get(1);
            if (!StringUtils.hasText(start) || !StringUtils.hasText(end)) continue;

            VideoChainV2Request.PictureInfo pi = new VideoChainV2Request.PictureInfo();
            pi.setPictureUrl(obj.getImageUrl());
            pi.setStartTime(start);
            pi.setEndTime(end);
            pi.setPosition(mapOppositePosition(obj.getRolePosition()));
            // 暂不设置 intro/outro/combo，保持静态插图
            list.add(pi);
        }
        return list;
    }

    /**
     * 角色位置 -> 叠加位置取反（保持与 FFmpeg 版脚本驱动逻辑一致）。
     */
    private VideoChainV2Request.Position mapOppositePosition(String rolePosition) {
        if (rolePosition == null) {
            return VideoChainV2Request.Position.RIGHT; // 默认右侧
        }
        String rp = rolePosition.trim().toUpperCase();
        if ("LEFT".equals(rp)) {
            return VideoChainV2Request.Position.RIGHT;
        }
        if ("RIGHT".equals(rp)) {
            return VideoChainV2Request.Position.LEFT;
        }
        return VideoChainV2Request.Position.RIGHT;
    }
}


