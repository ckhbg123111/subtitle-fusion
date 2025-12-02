package com.zhongjia.subtitlefusion.service.videochainv2;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.VideoChainV2Request;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.service.SubtitleService;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoChainV2DraftWorkflowService {

    private final CapCutApiClient apiClient;
    private final SubtitleService subtitleService;

    public DraftRefOutput generateDraft(VideoChainV2Request request, List<String> segmentVideoUrls) throws Exception {
        if (request == null || CollectionUtils.isEmpty(request.getSegmentList())) {
            throw new IllegalArgumentException("segmentList 不能为空");
        }
        int n = request.getSegmentList().size();
        if (segmentVideoUrls == null || segmentVideoUrls.size() != n) {
            throw new IllegalArgumentException("segmentVideoUrls 数量与段数量不一致");
        }

        // 1) 计算每段时长（秒）
        double[] D = new double[n];
        for (int i = 0; i < n; i++) {
            VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);
            double d = 0.0;
            if (seg.getDuration() != null && seg.getDuration() > 0) {
                d = seg.getDuration() / 1000.0;
            } else if (StringUtils.hasText(seg.getAudioUrl())) {
                Double remote = apiClient.getDuration(seg.getAudioUrl());
                d = remote != null ? remote : 0.0;
            }
            if (d <= 0.0) {
                d = 1.0; // 兜底
            }
            D[i] = d;
        }

        // 2) 转场时长（秒）
        double[] T = new double[Math.max(0, n - 1)];
        if (request.getGapTransitions() != null && request.getGapTransitions().size() == n - 1) {
            for (int i = 0; i < n - 1; i++) {
                Double t = request.getGapTransitions().get(i).getDurationSec();
                double safe = t == null ? 0.5 : t;
                // 修正上下限
                safe = Math.max(0.1, Math.min(safe, Math.max(0.5, D[i] - 0.1)));
                T[i] = safe;
            }
        } else {
            for (int i = 0; i < n - 1; i++) T[i] = 0.0;
        }

        // 3) 计算段起点 S[i]
        // 方案A：加了转场也不影响视频总时长 —— 段起点按完整时长线性累加，不再减去转场时长
        // total = sum(D[i])，transition/transition_duration 仅作为视觉特效参数
        double[] S = new double[n];
        S[0] = 0.0;
        for (int i = 0; i < n - 1; i++) {
            S[i + 1] = S[i] + D[i];
        }

        // 4) 创建草稿（默认 1080x1920，可按需增强）
        CapCutResponse<DraftRefOutput> createRes = apiClient.createDraft(1080, 1920);
        if (createRes == null || !createRes.isSuccess() || createRes.getOutput() == null) {
            throw new IllegalStateException("create_draft 失败: " + (createRes != null ? createRes.getError() : "null"));
        }
        String draftId = createRes.getOutput().getDraftId();

        // 5) 逐段添加视频与音频
        for (int i = 0; i < n; i++) {
            String videoUrl = segmentVideoUrls.get(i);
            Map<String, Object> pv = new HashMap<>();
            pv.put("draft_id", draftId);
            pv.put("video_url", apiClient.encodeUrl(videoUrl));
            pv.put("target_start", S[i]);
            pv.put("track_name", "video_main");
            pv.put("width", 1080);
            pv.put("height", 1920);
            pv.put("volume", 0.0); // 段视频静音
            if (i < n - 1 && T[i] > 0.0 && request.getGapTransitions() != null) {
                String transition = request.getGapTransitions().get(i).getTransition();
                if (StringUtils.hasText(transition)) {
                    pv.put("transition", transition);
                    pv.put("transition_duration", T[i]);
                }
            }
            apiClient.addVideo(pv);

            VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);
            if (StringUtils.hasText(seg.getAudioUrl())) {
                Map<String, Object> pa = new HashMap<>();
                pa.put("draft_id", draftId);
                pa.put("audio_url", apiClient.encodeUrl(seg.getAudioUrl()));
                pa.put("target_start", S[i]);
                pa.put("width", 1080);
                pa.put("height", 1920);
                pa.put("duration", D[i]);
                apiClient.addAudio(pa);
            }
        }

        // 6) 插图（按段偏移），并为同一时间重叠的图片分配不同轨道（track_name）
        if (!CollectionUtils.isEmpty(request.getSegmentList())) {
            for (int i = 0; i < n; i++) {
                VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);
                if (CollectionUtils.isEmpty(seg.getPictureInfos())) continue;

                // 6.1 先在“段内局部时间轴”上为图片规划车道，避免同一时间区间在同一轨道上重叠
                List<VideoChainV2Request.PictureInfo> pics = seg.getPictureInfos();
                List<VideoChainV2Request.PictureInfo> ordered = new ArrayList<>(pics);
                ordered.sort(Comparator.comparingDouble(p -> com.zhongjia.subtitlefusion.util.TimeUtils.parseToSeconds(p.getStartTime())));

                Map<VideoChainV2Request.PictureInfo, Integer> laneMap = new HashMap<>();
                List<Double> laneEndTimes = new ArrayList<>();
                for (VideoChainV2Request.PictureInfo pic : ordered) {
                    double localStart = com.zhongjia.subtitlefusion.util.TimeUtils.parseToSeconds(pic.getStartTime());
                    double localEnd = com.zhongjia.subtitlefusion.util.TimeUtils.parseToSeconds(pic.getEndTime());
                    if (localEnd <= localStart) {
                        localEnd = localStart + 0.5;
                    }
                    int lane = 0;
                    while (lane < laneEndTimes.size() && localStart < laneEndTimes.get(lane)) {
                        lane++;
                    }
                    if (lane == laneEndTimes.size()) {
                        laneEndTimes.add(localEnd);
                    } else {
                        laneEndTimes.set(lane, localEnd);
                    }
                    laneMap.put(pic, lane);
                }

                // 6.2 按全局时间（加上 S[i] 偏移）下发到 CapCut，不同 lane 使用不同 track_name
                for (VideoChainV2Request.PictureInfo pic : pics) {
                    Map<String, Object> pi = new HashMap<>();
                    pi.put("draft_id", draftId);
                    pi.put("image_url", apiClient.encodeUrl(pic.getPictureUrl()));
                    double start = com.zhongjia.subtitlefusion.util.TimeUtils.parseToSeconds(pic.getStartTime()) + S[i];
                    double end = com.zhongjia.subtitlefusion.util.TimeUtils.parseToSeconds(pic.getEndTime()) + S[i];
                    if (end <= start) end = start + 1.0;
                    pi.put("start", start);
                    pi.put("end", end);
                    pi.put("width", 1080);
                    pi.put("height", 1920);

                    Integer lane = laneMap.get(pic);
                    if (lane == null) {
                        lane = 0;
                    }
                    pi.put("track_name", "image_fx_lane_" + lane);

                    // 简单布局：左右两侧
                    if (pic.getPosition() == VideoChainV2Request.Position.LEFT) {
                        pi.put("transform_x", -0.6);
                    } else {
                        pi.put("transform_x", 0.6);
                    }
                    pi.put("transform_y", 0.0);
                    if (StringUtils.hasText(pic.getIntro())) {
                        pi.put("intro_animation", pic.getIntro());
                    }
                    if (StringUtils.hasText(pic.getOutro())) {
                        pi.put("outro_animation", pic.getOutro());
                    }
                    if (StringUtils.hasText(pic.getCombo())) {
                        pi.put("combo_animation", pic.getCombo());
                    }
                    apiClient.addImage(pi);
                }
            }
        }

        // 7) 底部字幕与标题字幕（合并偏移）
        List<SubtitleInfo> bottomList = new ArrayList<>();
        List<SubtitleInfo> titleList = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            VideoChainV2Request.SegmentInfo seg = request.getSegmentList().get(i);
            if (seg.getSubtitleInfo() != null) {
                bottomList.add(SubtitleTimelineUtils.offset(seg.getSubtitleInfo(), S[i]));
            }
            if (seg.getTextInfo() != null) {
                titleList.add(SubtitleTimelineUtils.offset(seg.getTextInfo(), S[i]));
            }
        }
        SubtitleInfo bottomMerged = SubtitleTimelineUtils.merge(bottomList);
        SubtitleInfo titleMerged = SubtitleTimelineUtils.merge(titleList);

        subtitleService.processSubtitles(draftId, bottomMerged, 1080, 1920);
        // 标题走通用字幕逻辑，但使用专用轨道
        subtitleService.processSubtitlesOnTrack(draftId, titleMerged, 1080, 1920, "title_fx");

        return createRes.getOutput();
    }
}


