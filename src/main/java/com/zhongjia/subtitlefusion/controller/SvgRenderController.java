package com.zhongjia.subtitlefusion.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zhongjia.subtitlefusion.service.SvgRenderService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping("/api/svg")
public class SvgRenderController {

	private final SvgRenderService svgRenderService;

	public SvgRenderController(SvgRenderService svgRenderService) {
		this.svgRenderService = svgRenderService;
	}

	/**
	 * 直接接收 text/plain 的 SVG 文本，返回 image/png。
	 */
	@PostMapping(value = "/render", consumes = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<byte[]> renderFromText(@RequestBody String svgText) {
		if (!StringUtils.hasText(svgText)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
					.body("svg text is empty".getBytes(StandardCharsets.UTF_8));
		}
		byte[] png = svgRenderService.renderSvgStringToPng(svgText, null, null);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
				.body(png);
	}

	/**
	 * 接收 JSON，支持可选 width/height，支持 svgBase64 或 svgText。
	 * 返回 image/png。
	 */
	@PostMapping(value = "/render-json", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> renderFromJson(@RequestBody RenderRequest request) {
		if (request == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
					.body("request is null".getBytes(StandardCharsets.UTF_8));
		}
		String svg = null;
		if (StringUtils.hasText(request.svgText)) {
			svg = request.svgText;
		} else if (StringUtils.hasText(request.svgBase64)) {
			try {
				byte[] decoded = Base64.getDecoder().decode(request.svgBase64);
				svg = new String(decoded, StandardCharsets.UTF_8);
			} catch (IllegalArgumentException e) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
						.body("invalid svgBase64".getBytes(StandardCharsets.UTF_8));
			}
		}
		if (!StringUtils.hasText(svg)) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE)
					.body("svg is empty".getBytes(StandardCharsets.UTF_8));
		}

		Integer w = request.widthPx;
		Integer h = request.heightPx;
		byte[] png = svgRenderService.renderSvgStringToPng(svg, w, h);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE)
				.body(png);
	}

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class RenderRequest {
		@JsonProperty("svgText")
		public String svgText;
		@JsonProperty("svgBase64")
		public String svgBase64;
		@JsonProperty("widthPx")
		public Integer widthPx;
		@JsonProperty("heightPx")
		public Integer heightPx;
	}
}


