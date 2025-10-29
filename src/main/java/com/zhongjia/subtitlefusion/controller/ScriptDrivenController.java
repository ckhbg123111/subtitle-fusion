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
import java.util.Base64;

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
                    String srtUrl = minioService.uploadToPublicBucket(new ByteArrayInputStream(bytes), bytes.length, fileName);
                    seg.setSrtUrl(srtUrl);
                }
            }

            // 物体/文字 -> 图片或 SVG 叠加（生产环境关闭）
            List<VideoChainRequest.PictureInfo> pictureInfos = new ArrayList<>();
            List<VideoChainRequest.SvgInfo> svgInfos = new ArrayList<>();
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
                spec.setDurationSec(0.8); // 默认0.8秒
                gaps.add(spec);
            }
            chainRequest.setGapTransitions(gaps);
        }

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

		// 4) 固定形状气泡，无需左右尾巴处理

		// 5) 计算文本块起始基线 Y，使整体竖直居中
		int textBlockHeight = lines.size() * lineHeight;
		int firstBaselineY = rectY + (bodyH - textBlockHeight) / 2 + ascent; // 第一行基线
		int textCenterX = rectX + bodyW / 2;

		g.dispose();

		// 6) 组装 SVG（使用提供的固定形状 SVG，并按计算尺寸缩放）
		double baseW = 651.0;
		double baseH = 554.0;
		double scaleX = svgW / baseW;
		double scaleY = svgH / baseH;

		// 将外部图片转为 data URI，避免 SVG 渲染器屏蔽网络资源
		String img1Data = fetchAsDataUri("http://114.215.202.44:9000/nis-public/test/img1.png");

		String img2Data = fetchAsDataUri("http://114.215.202.44:9000/nis-public/test/img2.png");
		// 计算图片固定大小与随文本宽度变化的坐标（仅位置随 bodyW 变化，尺寸不缩放）
		int img1W = 185, img1H = 189; // 固定宽高
		int img2W = 152, img2H = 151; // 固定宽高
		int img1MarginRight = 74; // 贴近右侧的间距（参考示例）
		int img1MarginBottom = 50;
		int img2RightOffset = 350; // 距离右侧更远一些，形成分布
		int img2MarginBottom = 80;
		int img1X = Math.max(0, rectX + bodyW - img1W - img1MarginRight);
		int img1Y = Math.max(0, rectY + bodyH - img1H - img1MarginBottom);
		int img2X = Math.max(0, rectX + bodyW - img2W - img2RightOffset);
		int img2Y = Math.max(0, rectY + bodyH - img2H - img2MarginBottom);
		StringBuilder imageNodes = new StringBuilder();
		if (img1Data != null && !img1Data.isEmpty()) {
			imageNodes.append(String.format(java.util.Locale.US,
					"<image id=\"编组备份\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" xlink:href=\"%s\"></image>\n",
					img1X, img1Y, img1W, img1H, img1Data));
		}
		if (img2Data != null && !img2Data.isEmpty()) {
			imageNodes.append(String.format(java.util.Locale.US,
					"<image x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\" xlink:href=\"%s\"></image>\n",
					img2X, img2Y, img2W, img2H, img2Data));
		}

		String defs = "" +
				"<defs>\n" +
				"  <linearGradient x1=\"50%\" y1=\"0%\" x2=\"50%\" y2=\"100%\" id=\"linearGradient-1\">\n" +
				"    <stop stop-color=\"#FFF07F\" offset=\"0%\"></stop>\n" +
				"    <stop stop-color=\"#FFC855\" offset=\"65.040567%\"></stop>\n" +
				"    <stop stop-color=\"#D57F05\" offset=\"100%\"></stop>\n" +
				"  </linearGradient>\n" +
				"  <linearGradient x1=\"50%\" y1=\"0.643300818%\" x2=\"50%\" y2=\"87.1728744%\" id=\"linearGradient-2\">\n" +
				"    <stop stop-color=\"#FEDC47\" offset=\"0%\"></stop>\n" +
				"    <stop stop-color=\"#FDD02F\" offset=\"100%\"></stop>\n" +
				"  </linearGradient>\n" +
				"  <filter x=\"-5.8%\" y=\"0.0%\" width=\"111.6%\" height=\"100.0%\" filterUnits=\"objectBoundingBox\" id=\"filter-3\">\n" +
				"    <feGaussianBlur stdDeviation=\"10 0\" in=\"SourceGraphic\"></feGaussianBlur>\n" +
				"  </filter>\n" +
				"</defs>\n";

		String bubble = String.format(java.util.Locale.US,
				"<g transform=\"scale(%f,%f)\">\n" +
				"  <g id=\"v2.0\" stroke=\"none\" stroke-width=\"1\" fill=\"none\" fill-rule=\"evenodd\">\n" +
				"    <g id=\"编组\" transform=\"translate(-0.1292, 0)\">\n" +
				"      <g transform=\"translate(0.1292, 0)\">\n" +
				"        <g transform=\"translate(49, 0)\" id=\"矩形\">\n" +
				"          <path d=\"M61,0 L479,0 C512.68937,-7.10542736e-15 540,27.3106303 540,61 L540,424.203918 C539.998673,457.893288 512.686967,485.202843 478.997597,485.201516 C478.685211,485.201503 478.37283,485.199091 478.060482,485.19428 C456.916003,484.868712 444.789724,485.80395 441.681642,488 C433.561278,493.737536 441.681642,552.659937 433.561278,552.659937 C425.440913,552.659937 431.52051,566.11049 351.3766,488 C254.5844,488 157.7922,488 61,488 C27.3106303,488 0,460.68937 0,427 L0,61 C-7.10542736e-15,27.3106303 27.3106303,7.10542736e-15 61,0 Z\" fill=\"url(#linearGradient-1)\"></path>\n" +
				"          <path d=\"M73,12 L468,12 C501.68937,12 529,39.3106303 529,73 L529,414.168025 C528.998867,447.119755 502.82835,474.11426 469.892481,475.136564 C448.643648,475.796217 436.504349,477.186376 433.474585,479.307032 C425.700088,484.74872 434.869276,536.162829 427.094779,536.162829 C419.320282,536.162829 428.300953,548.919824 351.570579,474.836988 C258.71372,474.836988 165.85686,474.836988 73,474.836988 C39.3106303,474.836988 12,447.526358 12,413.836988 L12,73 C12,39.3106303 39.3106303,12 73,12 Z\" fill=\"url(#linearGradient-2)\" filter=\"url(#filter-3)\"></path>\n" +
				"        </g>\n" +
				"      </g>\n" +
				"    </g>\n" +
				"  </g>\n" +
				"</g>\n",
				scaleX, scaleY);

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
				"<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" viewBox=\"0 0 %d %d\" width=\"%d\" height=\"%d\">\n%s%s%s%s</svg>\n",
			svgW, svgH, svgW, svgH, defs, bubble, imageNodes.toString(), textNode.toString());

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

	/**
	 * 拉取远程图片并转为 data URI（base64）。网络失败时返回空字符串。 fixme 可以考虑缓存
	 */
	private static String fetchAsDataUri(String url) {
		if (url == null || url.isEmpty()) return "";
		java.io.InputStream in = null;
		try {
			java.net.URL u = new java.net.URL(url);
			java.net.URLConnection conn = u.openConnection();
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			in = conn.getInputStream();
			byte[] bytes = readAllBytes(in);
			String mime = guessMime(url);
			String b64 = java.util.Base64.getEncoder().encodeToString(bytes);
			return "data:" + mime + ";base64," + b64;
		} catch (Exception ignore) {
			return "";
		} finally {
			if (in != null) try { in.close(); } catch (Exception ignore) {}
		}
	}

	private static String guessMime(String url) {
		String u = url.toLowerCase();
		if (u.endsWith(".png")) return "image/png";
		if (u.endsWith(".jpg") || u.endsWith(".jpeg")) return "image/jpeg";
		if (u.endsWith(".gif")) return "image/gif";
		return "application/octet-stream";
	}

	private static byte[] readAllBytes(java.io.InputStream in) throws java.io.IOException {
		byte[] buf = new byte[8192];
		int n;
		java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
		while ((n = in.read(buf)) != -1) {
			out.write(buf, 0, n);
		}
		return out.toByteArray();
	}
}


