package com.zhongjia.subtitlefusion.service.webtoon;

import com.zhongjia.subtitlefusion.model.*;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.model.capcut.GenerateVideoOutput;
import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import com.zhongjia.subtitlefusion.model.options.BasicTextOptions;
import com.zhongjia.subtitlefusion.model.options.FlowerTextOptions;
import com.zhongjia.subtitlefusion.model.options.KeywordHighlightOptions;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;
import com.zhongjia.subtitlefusion.service.SubtitleService;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * 漫剧草稿生成工作流：图片铺轨 + 关键帧 + 音频 + 字幕偏移 + 可选云渲染提交。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebtoonDramaDraftWorkflowService {

    private final CapCutApiClient apiClient;
    private final SubtitleService subtitleService;

    private final WebtoonDramaKeyframeBuilder keyframeBuilder = new WebtoonDramaKeyframeBuilder();

    public WebtoonDramaGenResult generate(WebtoonDramaGenerateRequest request) throws Exception {
        validate(request);

        // 画布：竖屏兜底（需求场景更贴近手机漫剧）
        int width = 1080;
        int height = 1920;
        int[] canvas = resolveCanvasSizeFromFirstImage(request, width, height);
        width = canvas[0];
        height = canvas[1];

        CapCutResponse<DraftRefOutput> createRes = apiClient.createDraft(width, height);
        if (createRes == null || !createRes.isSuccess() || createRes.getOutput() == null) {
            throw new IllegalStateException("create_draft 失败: " + (createRes != null ? createRes.getError() : "null"));
        }
        String draftId = createRes.getOutput().getDraftId();
        String draftUrl = createRes.getOutput().getDraftUrl();
        if (!StringUtils.hasText(draftId)) {
            throw new IllegalStateException("create_draft 失败: draftId 为空");
        }

        // 1) 计算每段时长（秒）与段起点
        List<WebtoonDramaSegmentInfo> segments = request.getSegment();
        int n = segments.size();
        double[] segDur = new double[n];
        double[] segStart = new double[n];

        double acc = 0.0;
        for (int i = 0; i < n; i++) {
            WebtoonDramaSegmentInfo seg = segments.get(i);
            double d = resolveSegmentDurationSeconds(seg);
            if (d <= 0.0) d = 1.0;
            segDur[i] = d;
            segStart[i] = acc;
            acc += d;
        }

        // 2) 图片铺到时间轴 + 关键帧
        for (int i = 0; i < n; i++) {
            WebtoonDramaSegmentInfo seg = segments.get(i);
            if (!StringUtils.hasText(seg.getPictureUrl())) {
                continue;
            }
            double start = segStart[i];
            double end = segStart[i] + segDur[i];
            addSegmentImage(draftId, seg.getPictureUrl(), start, end);

            Map<String, Object> kf = keyframeBuilder.buildKeyframeBody(
                    draftId, "image_main", start, end, i, width, height, seg.getKeyframeSpec()
            );
            if (kf != null) {
                apiClient.addVideoKeyframe(kf);
            }
        }

        // 3) 音频上轨（每段内多音频，按 target_start 累加）
        for (int i = 0; i < n; i++) {
            WebtoonDramaSegmentInfo seg = segments.get(i);
            if (CollectionUtils.isEmpty(seg.getAudioInfo())) continue;

            double audioOffset = 0.0;
            for (WebtoonDramaSegmentInfo.AudioInfo ai : seg.getAudioInfo()) {
                if (ai == null || !StringUtils.hasText(ai.getAudioUrl())) continue;
                double ad = resolveAudioDurationSeconds(ai);
                if (ad <= 0.0) ad = 0.2;

                Map<String, Object> pa = new HashMap<>();
                pa.put("draft_id", draftId);
                pa.put("audio_url", ai.getAudioUrl());
                pa.put("target_start", segStart[i] + audioOffset);
                pa.put("width", width);
                pa.put("height", height);
                pa.put("duration", ad);
                apiClient.addAudio(pa);

                audioOffset += ad;
            }
        }

        // 4) 字幕偏移合并（音频局部时间 -> 全局时间）
        SubtitleInfo subtitleInfo = buildSubtitleInfoWithOffset(request, segStart);
        if (subtitleInfo != null && !CollectionUtils.isEmpty(subtitleInfo.getCommonSubtitleInfoList())) {
            fulfillDefaultTemplate(subtitleInfo);
            applyTemporaryEffectFallback(subtitleInfo);
            subtitleService.processSubtitles(draftId, subtitleInfo, width, height);
        }

        // 5) 可选：提交云渲染任务（只提交，不轮询）
        String cloudTaskId = null;
        if (Boolean.TRUE.equals(request.getCloudRendering())) {
            CapCutResponse<GenerateVideoOutput> gen = apiClient.generateVideo(draftId, null, null);
            if (gen != null && gen.isSuccess() && gen.getOutput() != null && gen.getOutput().isSuccess()) {
                cloudTaskId = gen.getOutput().getTaskId();
            }
            if (!StringUtils.hasText(cloudTaskId)) {
                String err = (gen != null && gen.getOutput() != null) ? gen.getOutput().getError() : (gen != null ? gen.getError() : null);
                throw new IllegalStateException(err != null ? ("云渲染任务提交失败: " + err) : "云渲染任务提交失败");
            }
        }

        WebtoonDramaGenResult out = new WebtoonDramaGenResult();
        out.draftId = draftId;
        out.draftUrl = draftUrl;
        out.cloudTaskId = cloudTaskId;
        return out;
    }

    /**
     * 探测第一张图片（严格 segment[0].pictureUrl）的宽高作为画布宽高。
     * 失败/为空则返回默认值。
     */
    private int[] resolveCanvasSizeFromFirstImage(WebtoonDramaGenerateRequest request, int defaultWidth, int defaultHeight) {
        try {
            if (request == null || CollectionUtils.isEmpty(request.getSegment())) {
                return new int[]{defaultWidth, defaultHeight};
            }
            WebtoonDramaSegmentInfo first = request.getSegment().get(0);
            String url = (first != null) ? first.getPictureUrl() : null;
            if (!StringUtils.hasText(url)) {
                return new int[]{defaultWidth, defaultHeight};
            }

            URLConnection conn = new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestProperty("User-Agent", "subtitle-fusion/1.0");

            try (InputStream is = conn.getInputStream()) {
                BufferedImage img = ImageIO.read(is);
                if (img == null || img.getWidth() <= 0 || img.getHeight() <= 0) {
                    log.warn("探测第一张图片宽高失败（ImageIO.read 返回空或非法尺寸），url={}", url);
                    return new int[]{defaultWidth, defaultHeight};
                }
                return new int[]{img.getWidth(), img.getHeight()};
            }
        } catch (Exception e) {
            String url = null;
            try {
                if (request != null && !CollectionUtils.isEmpty(request.getSegment())) {
                    WebtoonDramaSegmentInfo first = request.getSegment().get(0);
                    url = first != null ? first.getPictureUrl() : null;
                }
            } catch (Exception ignore) {
            }
            log.warn("探测第一张图片宽高异常，url={}, err={}", url, e.getMessage());
            return new int[]{defaultWidth, defaultHeight};
        }
    }

    private void validate(WebtoonDramaGenerateRequest request) {
        if (request == null) throw new IllegalArgumentException("请求体不能为空");
        if (CollectionUtils.isEmpty(request.getSegment())) throw new IllegalArgumentException("segment 不能为空");
    }

    private void addSegmentImage(String draftId, String imageUrl, double start, double end) {
        Map<String, Object> body = new HashMap<>();
        body.put("draft_id", draftId);
        body.put("image_url", imageUrl);
        body.put("start", start);
        body.put("end", end);
        body.put("track_name", "image_main");
        // 必填字段
        body.put("transform_y_px", 0);
        // 居中 + 稍微放大，保证基本铺满
        body.put("transform_x", 0.0);
        body.put("transform_y", 0.0);
        body.put("scale_x", 1.1);
        body.put("scale_y", 1.1);
        apiClient.addImage(body);
    }

    private double resolveSegmentDurationSeconds(WebtoonDramaSegmentInfo seg) {
        if (seg == null) return 0.0;
        if (seg.getDuration() != null && seg.getDuration() > 0) {
            return seg.getDuration() / 1000.0;
        }
        if (CollectionUtils.isEmpty(seg.getAudioInfo())) return 0.0;
        double sum = 0.0;
        for (WebtoonDramaSegmentInfo.AudioInfo ai : seg.getAudioInfo()) {
            sum += resolveAudioDurationSeconds(ai);
        }
        return sum;
    }

    private double resolveAudioDurationSeconds(WebtoonDramaSegmentInfo.AudioInfo ai) {
        if (ai == null) return 0.0;
        if (ai.getAudioDuration() != null && ai.getAudioDuration() > 0) {
            return ai.getAudioDuration() / 1000.0;
        }
        if (StringUtils.hasText(ai.getAudioUrl())) {
            Double remote = apiClient.getDuration(ai.getAudioUrl());
            return remote != null ? remote : 0.0;
        }
        return 0.0;
    }

    private SubtitleInfo buildSubtitleInfoWithOffset(WebtoonDramaGenerateRequest request, double[] segStart) {
        if (request == null || CollectionUtils.isEmpty(request.getSegment())) return null;
        List<CommonSubtitleInfo> all = new ArrayList<>();

        List<WebtoonDramaSegmentInfo> segments = request.getSegment();
        for (int i = 0; i < segments.size(); i++) {
            WebtoonDramaSegmentInfo seg = segments.get(i);
            if (seg == null || CollectionUtils.isEmpty(seg.getAudioInfo())) continue;

            double base = (segStart != null && i < segStart.length) ? segStart[i] : 0.0;
            double audioOffset = 0.0;
            for (WebtoonDramaSegmentInfo.AudioInfo ai : seg.getAudioInfo()) {
                if (ai == null) continue;

                List<CommonSubtitleInfo> subs = ai.getCommonSubtitleInfoList();
                if (!CollectionUtils.isEmpty(subs)) {
                    for (CommonSubtitleInfo s : subs) {
                        if (s == null) continue;
                        double st = base + audioOffset + TimeUtils.parseToSeconds(s.getStartTime());
                        double et = base + audioOffset + TimeUtils.parseToSeconds(s.getEndTime());
                        if (et <= st) et = st + 0.2;

                        CommonSubtitleInfo copy = new CommonSubtitleInfo();
                        copy.setText(s.getText());
                        // 直接写秒数字符串，TimeUtils.parseToSeconds 可解析
                        copy.setStartTime(String.valueOf(st));
                        copy.setEndTime(String.valueOf(et));
                        copy.setSubtitleEffectInfo(s.getSubtitleEffectInfo());
                        all.add(copy);
                    }
                }
                audioOffset += resolveAudioDurationSeconds(ai);
            }
        }

        SubtitleInfo subtitleInfo = new SubtitleInfo();
        subtitleInfo.setCommonSubtitleInfoList(all);
        subtitleInfo.setSubtitleTemplate(request.getSubtitleTemplate());
        return subtitleInfo;
    }

    private void fulfillDefaultTemplate(SubtitleInfo subtitleInfo) {
        if (subtitleInfo == null) return;
        if (subtitleInfo.getSubtitleTemplate() == null) {
            subtitleInfo.setSubtitleTemplate(new SubtitleTemplate());
        }
        SubtitleTemplate subtitleTemplate = subtitleInfo.getSubtitleTemplate();

        if (CollectionUtils.isEmpty(subtitleTemplate.getFlowerTextOptions())) {
            List<FlowerTextOptions> flowerTextOptions = new ArrayList<>();
            FlowerTextOptions flowerTextOption = new FlowerTextOptions();
            flowerTextOption.setEffectId("WklvRVxXQlVNbFpTQVtKakJTVA==");
            flowerTextOptions.add(flowerTextOption);
            subtitleTemplate.setFlowerTextOptions(flowerTextOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getTextTemplateOptions())) {
            List<TextTemplateOptions> textTemplateOptions = new ArrayList<>();
            TextTemplateOptions textTemplateOption = new TextTemplateOptions();
            textTemplateOption.setTemplateId("7299286022167285018");
            textTemplateOptions.add(textTemplateOption);
            subtitleTemplate.setTextTemplateOptions(textTemplateOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getKeywordHighlightOptions())) {
            List<KeywordHighlightOptions> keywordHighlightOptions = new ArrayList<>();
            KeywordHighlightOptions keywordHighlightOption = new KeywordHighlightOptions();
            keywordHighlightOption.setKeywordsFont("匹喏曹");
            keywordHighlightOption.setKeywordsColor("#FFFF00");
            keywordHighlightOptions.add(keywordHighlightOption);
            subtitleTemplate.setKeywordHighlightOptions(keywordHighlightOptions);
        }
        if (CollectionUtils.isEmpty(subtitleTemplate.getBasicTextOptions())) {
            List<BasicTextOptions> basicTextOptions = new ArrayList<>();
            BasicTextOptions basicTextOption = new BasicTextOptions();
            basicTextOption.setFont("匹喏曹");
            basicTextOption.setFontColor("#FFFFFF");
            basicTextOptions.add(basicTextOption);
            subtitleTemplate.setBasicTextOptions(basicTextOptions);
        }
    }

    /**
     * 与 CapCutDraftAsyncService 的临时兜底保持一致：未指定 strategy 时给 BASIC。
     */
    private void applyTemporaryEffectFallback(SubtitleInfo subtitleInfo) {
        if (subtitleInfo == null || CollectionUtils.isEmpty(subtitleInfo.getCommonSubtitleInfoList())) {
            return;
        }
        for (CommonSubtitleInfo si : subtitleInfo.getCommonSubtitleInfoList()) {
            if (si == null) continue;
            CommonSubtitleInfo.SubtitleEffectInfo sei = si.getSubtitleEffectInfo();
            if (sei == null) {
                sei = new CommonSubtitleInfo.SubtitleEffectInfo();
                si.setSubtitleEffectInfo(sei);
            }
            if (sei.getTextStrategy() != null) continue;
            sei.setTextStrategy(TextStrategyEnum.BASIC);
        }
    }

    public static class WebtoonDramaGenResult {
        public String draftId;
        public String draftUrl;
        public String cloudTaskId;
    }
}


