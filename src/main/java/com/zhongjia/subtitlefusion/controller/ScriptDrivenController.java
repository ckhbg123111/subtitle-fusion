package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.service.VideoChainFFmpegService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import com.zhongjia.subtitlefusion.model.UploadResult;

@RestController
@RequestMapping("/api/script-driven")
public class ScriptDrivenController {

    @Autowired
    private DistributedTaskManagementService taskService;
    @Autowired
    private VideoChainFFmpegService ffmpegService;
    @Autowired
    private MinioService minioService;
    @Autowired
    private AppProperties appProperties;

    

    private static final String side = "http://114.215.202.44:9000/nis-public/test/p0.png";
    // 文本框底图（硬编码默认值）
    private static final String TEXT_BOX_IMAGE_URL = "http://114.215.202.44:9000/nis-public/test/box.png";

    @PostMapping(value = "/tasks-v2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submitV2(@RequestBody List<ScriptDrivenSegmentRequest> requests) throws Exception {
        // 创建任务并启动异步处理
//        TaskInfo taskInfo = taskService.createTask(taskId);
//        ffmpegService.processAsync(chainRequest);
        return new TaskResponse();
    }

    /**
     * 提交脚本驱动分段请求（根为数组），创建任务并返回唯一任务ID
     */
    @PostMapping(value = "/tasks", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public TaskResponse submit(@RequestBody List<ScriptDrivenSegmentRequest> requests) throws Exception {
        if (requests == null || requests.isEmpty()) {
            return new TaskResponse(null, "请求体不能为空，至少需要一条记录");
        }
        String taskId = UUID.randomUUID().toString();

        // 构造 VideoChainRequest
        VideoChainRequest chainRequest = new VideoChainRequest();
        chainRequest.setTaskId(taskId);
        // 不再设置全局转场；由 gapTransitions 决定是否加转场

        List<VideoChainRequest.SegmentInfo> segmentInfos = new ArrayList<>();
        int segIndex = 0;
        for (ScriptDrivenSegmentRequest segReq : requests) {
            segIndex++;
            VideoChainRequest.SegmentInfo seg = new VideoChainRequest.SegmentInfo();

            // 音频
            seg.setAudioUrl(segReq.getAudioUrl());

            // 视频列表
            if (segReq.getVideoInfo() != null && !segReq.getVideoInfo().isEmpty()) {
                List<VideoChainRequest.VideoInfo> videoInfos = new ArrayList<>();
                for (ScriptDrivenSegmentRequest.VideoInfo vi : segReq.getVideoInfo()) {
                    VideoChainRequest.VideoInfo v = new VideoChainRequest.VideoInfo();
                    v.setVideoUrl(vi.getVideoUrl());
                    videoInfos.add(v);
                }
                seg.setVideoInfos(videoInfos);
            }

            boolean effectsEnabled = appProperties.getFeatures().isSubtitleObjectEffectsEnabled();

            // 字幕：将 subtitle_info 生成 SRT 并上传，获取直链设置到 seg.srtUrl（生产环境关闭）
            if (effectsEnabled && segReq.getSubtitleInfo() != null && !segReq.getSubtitleInfo().isEmpty()) {
                String srtContent = buildSrtFromSubtitleInfos(segReq.getSubtitleInfo());
                if (srtContent != null && !srtContent.isEmpty()) {
                    byte[] bytes = srtContent.getBytes(StandardCharsets.UTF_8);
                    String fileName = "subtitle_" + taskId + "_" + segIndex + ".srt";
                    UploadResult up = minioService.uploadToPublicBucket(new ByteArrayInputStream(bytes), bytes.length, fileName);
                    seg.setSrtUrl(up.getUrl());
                }
            }

            // 物体/文字 -> 图片与文本框叠加（生产环境关闭）
            List<VideoChainRequest.PictureInfo> pictureInfos = new ArrayList<>();
            List<VideoChainRequest.TextBoxInfo> textBoxInfos = new ArrayList<>();
            if (effectsEnabled && segReq.getObjectInfo() != null) {
                for (ScriptDrivenSegmentRequest.ObjectItem obj : segReq.getObjectInfo()) {
                    String start = (obj.getTime() != null && obj.getTime().size() > 0) ? obj.getTime().get(0) : null;
                    String end = (obj.getTime() != null && obj.getTime().size() > 1) ? obj.getTime().get(1) : null;

                    // 角色位置 -> 叠加位置取反
                    VideoChainRequest.Position overlayPos = mapOppositePosition(obj.getRolePosition());

                    if ("image".equalsIgnoreCase(obj.getType()) && obj.getImageUrl() != null && !obj.getImageUrl().isEmpty()) {
                        VideoChainRequest.PictureInfo pi = new VideoChainRequest.PictureInfo();
                        pi.setPictureUrl(obj.getImageUrl());
                        // 映射图片边框（允许为空）
                        pi.setImageBorderUrl(side);
                        pi.setStartTime(start);
                        pi.setEndTime(end);
                        pi.setPosition(overlayPos);
                        pi.setEffectType(OverlayEffectType.BLINDS_IN_CLOCK_OUT);
                        pictureInfos.add(pi);
                    } else if ("text".equalsIgnoreCase(obj.getType()) && obj.getText() != null && !obj.getText().isEmpty()) {
                        // 改为：图片+文本文本框（固定底图与尺寸）
                        VideoChainRequest.TextBoxInfo tb = new VideoChainRequest.TextBoxInfo();
                        tb.setText(obj.getText());
                        tb.setStartTime(start);
                        tb.setEndTime(end);
                        tb.setPosition(overlayPos);
                        tb.setEffectType(OverlayEffectType.BLINDS_IN_CLOCK_OUT);
                        VideoChainRequest.BoxInfo bi = new VideoChainRequest.BoxInfo();
                        bi.setBoxPictureUrl(TEXT_BOX_IMAGE_URL);
                        bi.setBoxWidth(369);
                        bi.setBoxHeight(125);
                        bi.setTextWidth(330);
                        bi.setTextHeight(70);
                        tb.setBoxInfo(bi);
                        // 默认给文本框配置一个常用且醒目的字体（优先使用配置中的字体文件，其次使用常见中文粗体）
                        VideoChainRequest.TextStyle style = new VideoChainRequest.TextStyle();
                        String cfgFont = appProperties.getRender() != null ? appProperties.getRender().getFontFile() : null;
                        if (cfgFont != null && !cfgFont.isEmpty()) {
                            style.setFontFile(cfgFont);
                        } else {
                            // 常见中文粗体（容器内优先），让文本更“明显”
                            style.setFontFile("/usr/share/fonts/truetype/noto/NotoSansCJK-Bold.ttc");
                        }
                        // 提升可读性：略增行距，字体改为黑色
                        style.setLineSpacing(6);
                        style.setFontColor("#000000");
                        tb.setTextStyle(style);
                        textBoxInfos.add(tb);
                    }
                }
            }
            if (!pictureInfos.isEmpty()) seg.setPictureInfos(pictureInfos);
            if (!textBoxInfos.isEmpty()) seg.setTextBoxInfos(textBoxInfos);

            segmentInfos.add(seg);
        }
        chainRequest.setSegmentList(segmentInfos);

        // 段间转场：为每个相邻段生成一条随机转场配置
        if (segmentInfos.size() > 1) {
            java.util.List<VideoChainRequest.GapTransitionSpec> gaps = new java.util.ArrayList<>();
            VideoChainRequest.TransitionType[] pool = new VideoChainRequest.TransitionType[]{
                    VideoChainRequest.TransitionType.FADE,
                    VideoChainRequest.TransitionType.DISSOLVE,
                    VideoChainRequest.TransitionType.ZOOMIN,
                    VideoChainRequest.TransitionType.WIPELEFT,
                    VideoChainRequest.TransitionType.WIPERIGHT,
                    VideoChainRequest.TransitionType.WIPEUP,
                    VideoChainRequest.TransitionType.WIPEDOWN,
                    VideoChainRequest.TransitionType.SLIDELEFT,
                    VideoChainRequest.TransitionType.SLIDERIGHT,
                    VideoChainRequest.TransitionType.SLIDEUP,
                    VideoChainRequest.TransitionType.SLIDEDOWN,
                    VideoChainRequest.TransitionType.CIRCLECROP,
                    VideoChainRequest.TransitionType.RECTCROP,
                    VideoChainRequest.TransitionType.FADEBLACK,
                    VideoChainRequest.TransitionType.FADEWHITE,
                    VideoChainRequest.TransitionType.RADIAL,
                    VideoChainRequest.TransitionType.PIXELIZE,
                    VideoChainRequest.TransitionType.SMOOTHLEFT,
                    VideoChainRequest.TransitionType.SMOOTHRIGHT,
                    VideoChainRequest.TransitionType.SMOOTHUP,
                    VideoChainRequest.TransitionType.SMOOTHDOWN,
                    VideoChainRequest.TransitionType.REVEALLEFT,
                    VideoChainRequest.TransitionType.REVEALRIGHT,
                    VideoChainRequest.TransitionType.REVEALUP,
                    VideoChainRequest.TransitionType.REVEALDOWN
            };
            int gapCount = segmentInfos.size() - 1;
            for (int i = 0; i < gapCount; i++) {
                VideoChainRequest.GapTransitionSpec spec = new VideoChainRequest.GapTransitionSpec();
                int r = ThreadLocalRandom.current().nextInt(pool.length);
                spec.setType(pool[r]);
                spec.setDurationSec(0.4); // 默认0.8秒
                gaps.add(spec);
            }
            chainRequest.setGapTransitions(gaps);
        }

        // 创建任务并启动异步处理
        TaskInfo taskInfo = taskService.createTask(taskId);
        ffmpegService.processAsync(chainRequest);
        return new TaskResponse(taskInfo);
    }


    private static VideoChainRequest.Position mapOppositePosition(String rolePosition) {
        if (rolePosition == null) {
            return VideoChainRequest.Position.RIGHT; // 默认右侧
        }
        String rp = rolePosition.trim().toUpperCase();
        if ("LEFT".equals(rp)) {
            return VideoChainRequest.Position.RIGHT;
        }
        if ("RIGHT".equals(rp)) {
            return VideoChainRequest.Position.LEFT;
        }
        return VideoChainRequest.Position.RIGHT;
    }

    /**
     * 根据脚本传入的字幕信息（文本+开始/结束时间）生成标准 SRT 内容。
     */
    private static String buildSrtFromSubtitleInfos(java.util.List<com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest.SubtitleInfo> list) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest.SubtitleInfo it : list) {
            if (it == null) continue;
            java.util.List<String> tm = it.getTime();
            if (tm == null || tm.size() < 2) continue;
            String start = tm.get(0);
            String end = tm.get(1);
            String text = it.getText() == null ? "" : it.getText();
            if (start == null || end == null) continue;
            idx++;
            sb.append(idx).append('\n');
            sb.append(start).append(" --> ").append(end).append('\n');
            sb.append(text).append("\n\n");
        }
        return sb.toString();
    }

}


