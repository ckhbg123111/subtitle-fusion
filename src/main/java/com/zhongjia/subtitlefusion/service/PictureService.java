package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.model.clip.PictureClip;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PictureService {

    private final CapCutApiClient apiClient;

    public void processPictures(String draftId,
                                List<PictureClip> pictures,
                                String imageIntro,
                                String imageOutro) {
        if (pictures == null || pictures.isEmpty()) return;
        List<PictureClip> pics = new ArrayList<>(pictures);
        pics.sort(Comparator.comparingDouble(pi -> TimeUtils.parseToSeconds(pi != null ? pi.getStartTime() : null)));
        log.info("[PictureService] pictures: {}", pics.size());

        List<Double> laneEnds = new ArrayList<>();
        final double EPS = 1e-3;

        for (PictureClip pi : pics) {
            if (pi == null || pi.getPictureUrl() == null || pi.getPictureUrl().isEmpty()) continue;
            double start = TimeUtils.parseToSeconds(pi.getStartTime());
            double end = TimeUtils.parseToSeconds(pi.getEndTime());
            if (end <= start) end = start + 2.0;

            int lane = -1;
            for (int i = 0; i < laneEnds.size(); i++) {
                if (start >= laneEnds.get(i) - EPS) { lane = i; break; }
            }
            if (lane < 0) { lane = laneEnds.size(); laneEnds.add(0.0); }
            laneEnds.set(lane, end);

            String trackName = lane == 0 ? "image_main" : ("image_main_" + lane);
            String encodedImageUrl = apiClient.encodeUrl(pi.getPictureUrl());
            java.util.Map<String, Object> addImage = new java.util.HashMap<>();
            addImage.put("draft_id", draftId);
            addImage.put("image_url", encodedImageUrl);
            addImage.put("start", start);
            addImage.put("end", end);
            addImage.put("track_name", trackName);
            addImage.put("transform_y_px", 0);
            addImage.put("transform_x_px", -350);
            // 固定图片显示尺寸为 400x400 像素（基于画布 1080x1920 的相对缩放）
            addImage.put("scale_x", 0.4);
            addImage.put("scale_y", 0.4);
            addImage.put("intro_animation", imageIntro);
            addImage.put("intro_animation_duration", 0.5);
            addImage.put("outro_animation", imageOutro);
            addImage.put("outro_animation_duration", 0.5);
            apiClient.addImage(addImage);
        }
    }
}


