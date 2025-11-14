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
                                String imageOutro,
                                Integer canvasWidth,
                                Integer canvasHeight) {
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

            // 画面右侧五分之一处：
            // 1) 目标区域为 [0.8W, 1.0W]，居中放置 -> 中心点在 0.9W
            // 2) CapCut 此处 transform_x_px 为相对画布中心的偏移像素，经验为负值向右偏移
            // 3) scale 基于内部 1000 像素参考，故使用 目标像素宽度 / 1000 计算
            int w = (canvasWidth != null && canvasWidth > 0) ? canvasWidth : 1080;
            // int h = (canvasHeight != null && canvasHeight > 0) ? canvasHeight : 1920; // 当前未使用，保留以便扩展

            double rightFifthCenterX = w * 0.9;                 // 右侧 1/5 区域中心
            double canvasCenterX = w / 2.0;
            double offsetX = rightFifthCenterX - canvasCenterX; // 相对画布中心的偏移（向右为正）
            double transformX = -offsetX;                       // 与现有坐标系保持一致：右移使用负值

            double targetWidthPx = w * 0.20;                    // 占画面宽度的 20%
            double scale = targetWidthPx / 1000.0;              // 按 CapCut 约定的 1000 参考尺寸换算

            addImage.put("transform_y_px", 0);
            addImage.put("transform_x_px", transformX);
            addImage.put("scale_x", scale);
            addImage.put("scale_y", scale);
            addImage.put("intro_animation", imageIntro);
            addImage.put("intro_animation_duration", 0.5);
            addImage.put("outro_animation", imageOutro);
            addImage.put("outro_animation_duration", 0.5);
            apiClient.addImage(addImage);
        }
    }
}


