package com.zhongjia.subtitlefusion.controller;

import com.zhongjia.subtitlefusion.model.ScriptDrivenSegmentRequest;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.model.TaskInfo;
import com.zhongjia.subtitlefusion.model.TaskResponse;
import com.zhongjia.subtitlefusion.model.enums.OverlayEffectType;
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

    

    private static final String side = "http://114.215.202.44:9000/nis-public/test/p0.png";
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
                        // 映射图片边框（允许为空）
                        pi.setImageBorderUrl(side);
                        pi.setStartTime(start);
                        pi.setEndTime(end);
                        pi.setPosition(overlayPos);
                        pi.setEffectType(OverlayEffectType.BLINDS_IN_CLOCK_OUT);
                        pictureInfos.add(pi);
                    } else if ("text".equalsIgnoreCase(obj.getType()) && obj.getText() != null && !obj.getText().isEmpty()) {
                        // 动态生成随文字宽度自适应的 SVG 气泡
                        String svg = buildBubbleSvg(obj.getText(), overlayPos == VideoChainRequest.Position.RIGHT);
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


    /**
     * 动态生成随文字宽度自适应的 SVG 气泡。
     * @param text 文本内容
     * @param rightSide 是否作为右侧气泡（尾巴在左侧）。否则为左侧气泡（尾巴在右侧）。
     */
	private static String buildBubbleSvg(String text, boolean rightSide) {
		String safeText = (text == null) ? "" : text;

		// 1) 使用 Java2D 计算字体度量
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1, 1, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = img.createGraphics();
		java.awt.Font font = new java.awt.Font("Arial", java.awt.Font.BOLD, 34);
		java.awt.FontMetrics fm = g.getFontMetrics(font);
		int ascent = fm.getAscent();
		int lineHeight = fm.getHeight();

		// 2) 先尝试加宽：整句能在最大宽度内单行显示则不换行，否则再换行
		int paddingLR = 80; // 左右总留白
		int minBodyW = 120;
		int maxBodyW = 640;

		int fullTextW = fm.stringWidth(safeText);
		int bodyW = Math.max(minBodyW, Math.min(fullTextW + paddingLR, maxBodyW));

		java.util.List<String> lines;
		if (fullTextW + paddingLR <= maxBodyW) {
			// 能放下：单行
			lines = java.util.Arrays.asList(safeText);
		} else {
			// 放不下：在最大可用宽度下换行，再以最长行回算最终宽度
			int wrapMaxWidth = Math.max(40, maxBodyW - paddingLR);
			lines = wrapTextByFontMetrics(safeText, fm, wrapMaxWidth);
			int longest = 0;
			for (String ln : lines) longest = Math.max(longest, fm.stringWidth(ln));
			bodyW = Math.max(minBodyW, Math.min(longest + paddingLR, maxBodyW));
		}

		int vPaddingTB = 20; // 上下留白
		int bodyH = Math.max(80, lines.size() * lineHeight + vPaddingTB);

		int svgPadL = 40;
		int svgPadR = 40;
		int svgW = svgPadL + bodyW + svgPadR;
		int svgH = Math.max(150, bodyH + 60); // 画布高度给一些富余

		int rectX = svgPadL;
		int rectY = 30;
		int rectRX = 16, rectRY = 16; // 圆角

		// 4) 尾巴位置基于中线，细节在 path 里构建

		// 5) 计算文本块起始基线 Y，使整体竖直居中
		int textBlockHeight = lines.size() * lineHeight;
		int firstBaselineY = rectY + (bodyH - textBlockHeight) / 2 + ascent; // 第一行基线
		int textCenterX = rectX + bodyW / 2;

		g.dispose();

		// 6) 组装 SVG
		String defs = "" +
				"<defs>\n" +
				"  <linearGradient id=\"bubbleGradient\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">\n" +
				"    <stop offset=\"0%\" stop-color=\"#4158D0\" />\n" +
				"    <stop offset=\"50%\" stop-color=\"#C850C0\" />\n" +
				"    <stop offset=\"100%\" stop-color=\"#FFCC70\" />\n" +
				"  </linearGradient>\n" +
				"  <filter id=\"glow\" x=\"-20%\" y=\"-20%\" width=\"140%\" height=\"140%\">\n" +
				"    <feGaussianBlur stdDeviation=\"5\" result=\"blur\" />\n" +
				"    <feComposite in=\"SourceGraphic\" in2=\"blur\" operator=\"over\" />\n" +
				"  </filter>\n" +
				"</defs>\n";

		// 使用单一 path 生成圆角气泡并在侧边插入尾巴，避免描边/渐变接缝
		int midY = rectY + bodyH / 2;
		int tailLen = 24;
		int tailHalf = 14;
		String pathD;
		if (rightSide) {
			// 尾巴在左侧
			pathD = String.format(
					"M %d %d " +
					"H %d " +
					"Q %d %d %d %d " +
					"V %d " +
					"Q %d %d %d %d " +
					"H %d " +
					"Q %d %d %d %d " +
					"V %d " +
					"L %d %d " +
					"L %d %d " +
					"V %d " +
					"Q %d %d %d %d " +
					"Z",
				rectX + rectRX, rectY,
				rectX + bodyW - rectRX,
				rectX + bodyW, rectY, rectX + bodyW, rectY + rectRY,
				rectY + bodyH - rectRY,
				rectX + bodyW, rectY + bodyH, rectX + bodyW - rectRX, rectY + bodyH,
				rectX + rectRX,
				rectX, rectY + bodyH, rectX, rectY + bodyH - rectRY,
				midY + tailHalf,
				rectX - tailLen, midY,
				rectX, midY - tailHalf,
				rectY + rectRY,
				rectX, rectY, rectX + rectRX, rectY
			);
		} else {
			// 尾巴在右侧
			pathD = String.format(
					"M %d %d " +
					"H %d " +
					"Q %d %d %d %d " +
					"V %d " +
					"L %d %d " +
					"L %d %d " +
					"V %d " +
					"Q %d %d %d %d " +
					"H %d " +
					"Q %d %d %d %d " +
					"V %d " +
					"Q %d %d %d %d " +
					"Z",
				rectX + rectRX, rectY,
				rectX + bodyW - rectRX,
				rectX + bodyW, rectY, rectX + bodyW, rectY + rectRY,
				midY - tailHalf,
				rectX + bodyW + tailLen, midY,
				rectX + bodyW, midY + tailHalf,
				rectY + bodyH - rectRY,
				rectX + bodyW, rectY + bodyH, rectX + bodyW - rectRX, rectY + bodyH,
				rectX + rectRX,
				rectX, rectY + bodyH, rectX, rectY + bodyH - rectRY,
				rectY + rectRY,
				rectX, rectY, rectX + rectRX, rectY
			);
		}
		String bubble = String.format(
				"<path d=\"%s\" fill=\"url(#bubbleGradient)\" filter=\"url(#glow)\" stroke=\"white\" stroke-width=\"1\"/>\n",
				pathD);

		StringBuilder textNode = new StringBuilder();
		textNode.append(String.format(
				"<text x=\"%d\" y=\"%d\" font-family=\"Arial, sans-serif\" font-size=\"34\" font-weight=\"bold\" text-anchor=\"middle\" fill=\"white\" stroke=\"black\" stroke-width=\"0.5\">",
				textCenterX, firstBaselineY));
		for (int i = 0; i < lines.size(); i++) {
			String line = xmlEscape(lines.get(i));
			if (i == 0) {
				textNode.append(line);
			} else {
				textNode.append(String.format("<tspan x=\"%d\" dy=\"%d\">%s</tspan>", textCenterX, lineHeight, line));
			}
		}
		textNode.append("</text>\n");

		String svg = String.format(
				"<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\">\n%s%s%s</svg>\n",
				svgW, svgH, svgW, svgH, defs, bubble, textNode.toString());

		return svg;
	}

	/**
	 * 将文本按最大像素宽度进行自动换行（CJK逐字；含空格则按词）。
	 */
	private static java.util.List<String> wrapTextByFontMetrics(String text, java.awt.FontMetrics fm, int maxWidth) {
		java.util.List<String> result = new java.util.ArrayList<>();
		if (text == null || text.isEmpty()) {
			result.add("");
			return result;
		}
		String s = text.replace("\r", "");
		boolean hasSpace = s.indexOf(' ') >= 0;
		boolean likelyCjk = !hasSpace && containsCjk(s);
		if (likelyCjk) {
			StringBuilder line = new StringBuilder();
			for (int i = 0; i < s.length(); ) {
				int cp = s.codePointAt(i);
				String ch = new String(Character.toChars(cp));
				if (line.length() == 0) {
					line.append(ch);
				} else {
					String candidate = line + ch;
					if (fm.stringWidth(candidate) <= maxWidth) {
						line.append(ch);
					} else {
						result.add(line.toString());
						line.setLength(0);
						line.append(ch);
					}
				}
				i += Character.charCount(cp);
			}
			if (line.length() > 0) result.add(line.toString());
		} else {
			String[] words = s.split(" ");
			StringBuilder line = new StringBuilder();
			for (int i = 0; i < words.length; i++) {
				String word = words[i];
				String candidate = (line.length() == 0) ? word : line + " " + word;
				if (fm.stringWidth(candidate) <= maxWidth) {
					line.setLength(0);
					line.append(candidate);
				} else {
					if (line.length() > 0) result.add(line.toString());
					// 如果单词本身超过一行最大宽度，则硬切
					if (fm.stringWidth(word) > maxWidth) {
						result.add(word);
						line.setLength(0);
					} else {
						line.setLength(0);
						line.append(word);
					}
				}
			}
			if (line.length() > 0) result.add(line.toString());
		}
		return result;
	}

	private static boolean containsCjk(String s) {
		for (int i = 0; i < s.length(); ) {
			int cp = s.codePointAt(i);
			Character.UnicodeBlock block = Character.UnicodeBlock.of(cp);
			if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
					block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
					block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
					block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
					block == Character.UnicodeBlock.HANGUL_SYLLABLES ||
					block == Character.UnicodeBlock.HIRAGANA ||
					block == Character.UnicodeBlock.KATAKANA) {
				return true;
			}
			i += Character.charCount(cp);
		}
		return false;
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


