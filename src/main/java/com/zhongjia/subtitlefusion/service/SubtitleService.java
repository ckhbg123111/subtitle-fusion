package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleTemplate;

import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.service.subtitle.SubtitleLanePlanner;
import com.zhongjia.subtitlefusion.service.subtitle.TextRenderStrategy;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubtitleService {

    private final CapCutApiClient apiClient;
    private final List<TextRenderStrategy<?>> strategies;
    private final SubtitleLanePlanner lanePlanner;

    private boolean checkValid(SubtitleInfo subtitleInfo) {
        return subtitleInfo.getSubtitleTemplate() != null;
    }

    public void processSubtitles(String draftId,
                                 SubtitleInfo subtitleInfo,
                                 int canvasWidth,
                                 int canvasHeight) throws Exception {
        if (subtitleInfo == null || CollectionUtils.isEmpty(subtitleInfo.getCommonSubtitleInfoList())) {
            log.info("缺失字幕，跳过字幕处理");
            return;
        }
        if (!checkValid(subtitleInfo)) {
            throw new Exception("参数不合法,缺失字幕模板");
        }
        SubtitleTemplate subtitleTemplate = subtitleInfo.getSubtitleTemplate();

        List<SubtitleInfo.CommonSubtitleInfo> items = subtitleInfo.getCommonSubtitleInfoList();
        log.info("[SubtitleService] subtitles: {}", items.size());

        // 按开始时间排序，保证渲染顺序稳定
        items.sort(Comparator.comparingDouble(si -> TimeUtils.parseToSeconds(si != null ? si.getStartTime() : null)));

        // 基于排序后的列表，规划车道，避免重叠
        int[] lanes = lanePlanner.planLanes(items);

        for (int idx = 0; idx < items.size(); idx++) {
            SubtitleInfo.CommonSubtitleInfo si = items.get(idx);
            if (si == null || si.getText() == null || si.getText().isEmpty()) continue;
            double start = TimeUtils.parseToSeconds(si.getStartTime());
            double end = TimeUtils.parseToSeconds(si.getEndTime());
            if (end <= start) end = start + 1.0;

            TextRenderStrategy<?> strategy = selectStrategy(si.getSubtitleEffectInfo());
            List<Map<String, Object>> payloads = strategy.buildWithAutoOptions(
                    draftId, si.getText(), start, end, canvasWidth, canvasHeight, subtitleTemplate, si.getSubtitleEffectInfo()
            );
            if (CollectionUtils.isEmpty(payloads)) {
                log.warn("[SubtitleService] no payloads generated for strategy {} (maybe options empty)", strategy.supports());
                continue;
            }
            int lane = (lanes != null && idx < lanes.length) ? lanes[idx] : 0;
            applyLaneLayout(payloads, lane, null);
            dispatchPayloads(payloads);
        }
    }

    /**
     * 与 processSubtitles 相同，但允许指定基础轨道名（例如 title_fx），便于与普通字幕分轨渲染。
     */
    public void processSubtitlesOnTrack(String draftId,
                                        SubtitleInfo subtitleInfo,
                                        int canvasWidth,
                                        int canvasHeight,
                                        String baseTrackName) throws Exception {
        if (subtitleInfo == null || CollectionUtils.isEmpty(subtitleInfo.getCommonSubtitleInfoList())) {
            log.info("缺失字幕，跳过字幕处理");
            return;
        }
        if (!checkValid(subtitleInfo)) {
            throw new Exception("参数不合法,缺失字幕模板");
        }
        SubtitleTemplate subtitleTemplate = subtitleInfo.getSubtitleTemplate();

        List<SubtitleInfo.CommonSubtitleInfo> items = subtitleInfo.getCommonSubtitleInfoList();
        log.info("[SubtitleService] subtitles(on track={}): {}", baseTrackName, items.size());

        items.sort(Comparator.comparingDouble(si -> TimeUtils.parseToSeconds(si != null ? si.getStartTime() : null)));
        int[] lanes = lanePlanner.planLanes(items);

        for (int idx = 0; idx < items.size(); idx++) {
            SubtitleInfo.CommonSubtitleInfo si = items.get(idx);
            if (si == null || si.getText() == null || si.getText().isEmpty()) continue;
            double start = TimeUtils.parseToSeconds(si.getStartTime());
            double end = TimeUtils.parseToSeconds(si.getEndTime());
            if (end <= start) end = start + 1.0;

            TextRenderStrategy<?> strategy = selectStrategy(si.getSubtitleEffectInfo());
            List<Map<String, Object>> payloads = strategy.buildWithAutoOptions(
                    draftId, si.getText(), start, end, canvasWidth, canvasHeight, subtitleTemplate, si.getSubtitleEffectInfo()
            );
            if (CollectionUtils.isEmpty(payloads)) {
                log.warn("[SubtitleService] no payloads generated for strategy {} (maybe options empty)", strategy.supports());
                continue;
            }
            int lane = (lanes != null && idx < lanes.length) ? lanes[idx] : 0;
            applyLaneLayout(payloads, lane, (baseTrackName == null || baseTrackName.isEmpty()) ? "text_fx" : baseTrackName);
            dispatchPayloads(payloads);
        }
    }

    private TextRenderStrategy<?> selectStrategy(SubtitleInfo.SubtitleEffectInfo subtitleEffectInfo) {
        if (subtitleEffectInfo != null) {
            for (TextRenderStrategy<?> s : strategies) {
                if (s.supports().equals(subtitleEffectInfo.getTextStrategy())) return s;
            }
        }
        return strategies.get(strategies.size() - 1); // 兜底
    }

    /**
     * 基于车道对 payload 进行布局覆写：
     * - 将 track_name 动态化为 "{原名}_lane_{lane}"
     * - 若调用方/策略未指定 transform_y，则按车道设置一个合适的行位（靠底部向上分行）
     */
    private void applyLaneLayout(List<Map<String, Object>> payloads, int lane, String baseTrackOverride) {
        if (CollectionUtils.isEmpty(payloads)) return;
        for (Map<String, Object> p : payloads) {
            Object tn = p.get("track_name");
            String baseTrack;
            if (baseTrackOverride != null && !baseTrackOverride.isEmpty()) {
                baseTrack = baseTrackOverride;
            } else {
                baseTrack = (tn != null && !String.valueOf(tn).isEmpty()) ? String.valueOf(tn) : "text_fx";
            }
            p.put("track_name", baseTrack + "_lane_" + lane);
            // 若是标题轨道，且未显式指定 transform_x，则给一个偏左的默认 X
            if (baseTrack.startsWith("title_fx") && !p.containsKey("transform_x")) {
                p.put("transform_x", lanePlanner.getTitleBaseX());
            }
            // 总是按车道覆盖 transform_y，确保多策略在同屏时按行分布
            // 对于标题轨道（如 title_fx），使用单独的“中上部”布局方案，避免与底部字幕重叠
            double y;
            if (baseTrack.startsWith("title_fx")) {
                y = lanePlanner.transformYForTitleLane(lane);
            } else {
                y = lanePlanner.transformYForLane(lane);
            }
            p.put("transform_y", y);
        }
    }

    private void dispatchPayloads(List<Map<String, Object>> payloads) {
        for (Map<String, Object> p : payloads) {
            if (p.containsKey("template_id")) {
                apiClient.addTextTemplate(p);
            } else {
                apiClient.addText(p);
            }
        }
    }
}


