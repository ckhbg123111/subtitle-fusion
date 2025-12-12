package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.Result;
import com.zhongjia.subtitlefusion.model.capcut.CapCutResponse;
import com.zhongjia.subtitlefusion.model.capcut.DraftRefOutput;
import com.zhongjia.subtitlefusion.model.clip.PictureClip;
import com.zhongjia.subtitlefusion.service.PictureService;
import com.zhongjia.subtitlefusion.service.api.CapCutApiClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试接口：演示「N 张图片拼接生成草稿（时间轴为图片序列）」能力。
 *
 * - POST /api/test/draft-from-images ：传入图片 URL 列表，后端调用 CapCut API 创建草稿并按顺序铺在时间线上，返回 draftId 与 draftUrl。
 * - GET  /api/test/draft-url        ：仅返回一个固定示例 URL，方便快速联通测试。
 */
@RestController
@RequestMapping("/api/test")
@Slf4j
@RequiredArgsConstructor
public class TestController {

    private final CapCutApiClient capCutApiClient;
    private final PictureService pictureService;

    /**
     * 简单联通测试：返回一个示例草稿 URL（固定值）
     *
     * GET /api/test/draft-url
     */
    @GetMapping(value = "/draft-url", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> getDraftUrl() {
        Map<String, Object> resp = new HashMap<>();
        String demoDraftUrl = "https://www.install-ai-guider.top/draft/downloader?draft_id=demo_draft_id";
        resp.put("draftUrl", demoDraftUrl);
        return resp;
    }

    /**
     * 核心测试接口：N 张图片生成一个草稿（时间轴为图片轮播），返回 draftId 与 draftUrl
     *
     * - 图片顺序：按 imageUrls 的顺序依次排布；
     * - 时长分配：如果未指定 totalDurationSeconds，则默认整体 20s，均分到每张图片；
     * - 画布尺寸：未指定 width/height 时，默认 1080x1920。
     *
     * 示例请求：
     * POST /api/test/draft-from-images
     * Content-Type: application/json
     * {
     *   "imageUrls": ["https://xxx/1.png", "https://xxx/2.png", "https://xxx/3.png"],
     *   "totalDurationSeconds": 20,
     *   "width": 1080,
     *   "height": 1920
     * }
     */
    @PostMapping(value = "/draft-from-images", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Result<Map<String, Object>> createDraftFromImages(@RequestBody ImagesToDraftRequest request) {
        if (request == null || request.getImageUrls() == null || request.getImageUrls().isEmpty()) {
            return Result.error("imageUrls 不能为空");
        }

        List<String> imageUrls = request.getImageUrls();
        int n = imageUrls.size();

        double totalDuration = request.getTotalDurationSeconds() != null && request.getTotalDurationSeconds() > 0
                ? request.getTotalDurationSeconds()
                : 20.0;
        double perDuration = totalDuration / n;

        int width = (request.getWidth() != null && request.getWidth() > 0) ? request.getWidth() : 1080;
        int height = (request.getHeight() != null && request.getHeight() > 0) ? request.getHeight() : 1920;

        // 1. 创建草稿
        CapCutResponse<DraftRefOutput> draftResp = capCutApiClient.createDraft(width, height);
        if (draftResp == null || !draftResp.isSuccess() || draftResp.getOutput() == null
                || draftResp.getOutput().getDraftId() == null || draftResp.getOutput().getDraftId().isEmpty()) {
            String err = (draftResp != null && draftResp.getError() != null) ? draftResp.getError() : "创建草稿失败";
            log.warn("[TestController] createDraft 失败, error={}", err);
            return Result.error(err);
        }

        String draftId = draftResp.getOutput().getDraftId();
        String draftUrl = draftResp.getOutput().getDraftUrl();
        log.info("[TestController] 草稿创建成功 draftId={}, draftUrl={}", draftId, draftUrl);

        // 2. 组装图片片段（顺序排布在时间轴上）
        List<PictureClip> clips = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String url = imageUrls.get(i);
            if (url == null || url.isEmpty()) {
                continue;
            }
            double start = perDuration * i;
            double end = (i == n - 1) ? totalDuration : perDuration * (i + 1);

            PictureClip clip = new PictureClip();
            clip.setPictureUrl(url);
            clip.setStartTime(String.valueOf(start));
            clip.setEndTime(String.valueOf(end));
            clips.add(clip);
        }

        if (clips.isEmpty()) {
            return Result.error("有效的图片 URL 为空，无法生成草稿");
        }

        // 3. 为图片添加简单的入/出场动画（如果获取失败则忽略）
        String imageIntro = null;
        String imageOutro = null;
        try {
            imageIntro = capCutApiClient.getRandomImageIntro(null);
            imageOutro = capCutApiClient.getRandomImageOutro(null);
        } catch (Exception e) {
            log.warn("[TestController] 获取图片入/出场动画失败（忽略）: {}", e.getMessage());
        }

        // 4. 调用 PictureService，将图片按时间轴写入草稿
        pictureService.processPictures(draftId, clips, imageIntro, imageOutro, width, height);

        Map<String, Object> data = new HashMap<>();
        data.put("draftId", draftId);
        data.put("draftUrl", draftUrl);
        data.put("totalDurationSeconds", totalDuration);
        data.put("imageCount", clips.size());
        return Result.success(data);
    }

    /**
     * N 张图片生成草稿的请求体
     */
    @Data
    public static class ImagesToDraftRequest {
        /**
         * 图片 URL 列表，按顺序排布在时间轴上
         */
        private List<String> imageUrls;

        /**
         * 整体时长（秒），为空或 <=0 时默认 20s，并均分给每张图片
         */
        private Double totalDurationSeconds;

        /**
         * 画布宽度，默认 1080
         */
        private Integer width;

        /**
         * 画布高度，默认 1920
         */
        private Integer height;
    }
}

