package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
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
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/script-driven")
public class ScriptDrivenController {

    @Autowired
    private DistributedTaskManagementService taskService;
    @Autowired
    private VideoChainFFmpegService ffmpegService;

    /**
     * 用于放在左边的svg
     */
    private static final String svgLeftString = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 150" width="300" height="150">
              <!-- 气泡渐变背景 -->
              <defs>
                <linearGradient id="bubbleGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4158D0" />
                  <stop offset="50%" stop-color="#C850C0" />
                  <stop offset="100%" stop-color="#FFCC70" />
                </linearGradient>
                <!-- 发光效果 -->
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="5" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
                <!-- 装饰圆形 -->
                <pattern id="dots" x="0" y="0" width="10" height="10" patternUnits="userSpaceOnUse">
                  <circle cx="5" cy="5" r="1" fill="white" fill-opacity="0.3" />
                </pattern>
              </defs>
            
              <!-- 气泡主体 -->
              <path d="M40,10 Q25,10 25,25 L25,90 Q25,105 40,105 L220,105 Q235,105 235,90 L235,60 L260,45 L235,30 L235,25 Q235,10 220,10 Z"\s
                    fill="url(#bubbleGradient)"\s
                    filter="url(#glow)"
                    stroke="white"
                    stroke-width="1" />
            
              <!-- 装饰图案 -->
              <rect x="40" y="25" width="160" height="60" fill="url(#dots)" opacity="0.5" />
            
              <!-- 文字 -->
              <text x="130" y="65" font-family="Arial, sans-serif" font-size="32" font-weight="bold"\s
                    text-anchor="middle" fill="white" stroke="black" stroke-width="0.5">
                ${replacement_context}
              </text>
            
              <!-- 点缀光效 -->
              <circle cx="80" cy="40" r="3" fill="white" opacity="0.7" />
              <circle cx="200" cy="80" r="2" fill="white" opacity="0.5" />
              <circle cx="150" cy="30" r="2" fill="white" opacity="0.6" />
            </svg>
            """;

    /**
     * 用于放在右边的SVG
     */
    private static final String svgRightString = """
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 300 150" width="300" height="150">
              <!-- 气泡渐变背景 -->
              <defs>
                <linearGradient id="bubbleGradient" x1="0%" y1="0%" x2="100%" y2="100%">
                  <stop offset="0%" stop-color="#4158D0" />
                  <stop offset="50%" stop-color="#C850C0" />
                  <stop offset="100%" stop-color="#FFCC70" />
                </linearGradient>
                <!-- 发光效果 -->
                <filter id="glow" x="-20%" y="-20%" width="140%" height="140%">
                  <feGaussianBlur stdDeviation="5" result="blur" />
                  <feComposite in="SourceGraphic" in2="blur" operator="over" />
                </filter>
                <!-- 装饰圆形 -->
                <pattern id="dots" x="0" y="0" width="10" height="10" patternUnits="userSpaceOnUse">
                  <circle cx="5" cy="5" r="1" fill="white" fill-opacity="0.3" />
                </pattern>
              </defs>
            
              <!-- 气泡主体 - 尾巴在左侧 -->
              <path d="M260,10 Q275,10 275,25 L275,90 Q275,105 260,105 L80,105 Q65,105 65,90 L65,60 L40,45 L65,30 L65,25 Q65,10 80,10 Z"\s
                    fill="url(#bubbleGradient)"\s
                    filter="url(#glow)"
                    stroke="white"
                    stroke-width="1" />
            
              <!-- 装饰图案 -->
              <rect x="80" y="25" width="160" height="60" fill="url(#dots)" opacity="0.5" />
            
              <!-- 文字 -->
              <text x="170" y="65" font-family="Arial, sans-serif" font-size="32" font-weight="bold"\s
                    text-anchor="middle" fill="white" stroke="black" stroke-width="0.5">
                ${replacement_context}
              </text>
            
              <!-- 点缀光效 -->
              <circle cx="120" cy="40" r="3" fill="white" opacity="0.7" />
              <circle cx="240" cy="80" r="2" fill="white" opacity="0.5" />
              <circle cx="190" cy="30" r="2" fill="white" opacity="0.6" />
            </svg>
            
            
            """;

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

        List<VideoChainRequest.SegmentInfo> segmentInfos = new ArrayList<>();
        for (ScriptDrivenSegmentRequest segReq : requests) {
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

            // 物体/文字 -> 图片或 SVG 叠加
            List<VideoChainRequest.PictureInfo> pictureInfos = new ArrayList<>();
            List<VideoChainRequest.SvgInfo> svgInfos = new ArrayList<>();
            if (segReq.getObjectInfo() != null) {
                for (ScriptDrivenSegmentRequest.ObjectItem obj : segReq.getObjectInfo()) {
                    String start = (obj.getTime() != null && obj.getTime().size() > 0) ? obj.getTime().get(0) : null;
                    String end = (obj.getTime() != null && obj.getTime().size() > 1) ? obj.getTime().get(1) : null;

                    // 角色位置 -> 叠加位置取反
                    VideoChainRequest.Position overlayPos = mapOppositePosition(obj.getRolePosition());

                    if ("image".equalsIgnoreCase(obj.getType()) && obj.getImageUrl() != null && !obj.getImageUrl().isEmpty()) {
                        VideoChainRequest.PictureInfo pi = new VideoChainRequest.PictureInfo();
                        pi.setPictureUrl(obj.getImageUrl());
                        pi.setStartTime(start);
                        pi.setEndTime(end);
                        pi.setPosition(overlayPos);
                        pictureInfos.add(pi);
                    } else if ("text".equalsIgnoreCase(obj.getType()) && obj.getText() != null && !obj.getText().isEmpty()) {
                        // 基于角色位置选择左右气泡 SVG 模板，并替换文案
                        String svgTemplate = (overlayPos == VideoChainRequest.Position.RIGHT) ? svgRightString : svgLeftString;
                        String svg = svgTemplate.replace("${replacement_context}", xmlEscape(obj.getText()));
                        String svgBase64 = Base64.getEncoder().encodeToString(svg.getBytes(StandardCharsets.UTF_8));

                        VideoChainRequest.SvgInfo si = new VideoChainRequest.SvgInfo();
                        si.setSvgBase64(svgBase64);
                        si.setStartTime(start);
                        si.setEndTime(end);
                        si.setPosition(overlayPos);
                        svgInfos.add(si);
                    }
                }
            }
            if (!pictureInfos.isEmpty()) seg.setPictureInfos(pictureInfos);
            if (!svgInfos.isEmpty()) seg.setSvgInfos(svgInfos);

            segmentInfos.add(seg);
        }
        chainRequest.setSegmentList(segmentInfos);

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

    private static String xmlEscape(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '&': sb.append("&amp;"); break;
                case '<': sb.append("&lt;"); break;
                case '>': sb.append("&gt;"); break;
                case '"': sb.append("&quot;"); break;
                case '\'': sb.append("&apos;"); break;
                default: sb.append(c);
            }
        }
        return sb.toString();
    }
}


