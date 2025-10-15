package com.zhongjia.subtitlefusion.service;

import org.apache.batik.bridge.ExternalResourceSecurity;
import org.apache.batik.bridge.ScriptSecurity;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * SVG 渲染服务：将 SVG 内容转码为 PNG。
 * 使用 Apache Batik，并在 UserAgent 层禁用外部资源与脚本以增强安全性。
 */
@Service
public class SvgRenderService {

	/**
	 * 将 SVG 字符串渲染为 PNG 字节数组。
	 *
	 * @param svgContent SVG 文本内容（UTF-8）
	 * @param widthPx  目标宽度（可空），单位：px
	 * @param heightPx 目标高度（可空），单位：px
	 * @return PNG 二进制数据
	 */
	public byte[] renderSvgStringToPng(String svgContent, Integer widthPx, Integer heightPx) {
		Objects.requireNonNull(svgContent, "svgContent must not be null");
		byte[] bytes = svgContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
		return renderSvgBytesToPng(bytes, widthPx, heightPx);
	}

	/**
	 * 将 SVG 字节数组渲染为 PNG 字节数组。
	 *
	 * @param svgBytes SVG 二进制数据
	 * @param widthPx  目标宽度（可空），单位：px
	 * @param heightPx 目标高度（可空），单位：px
	 * @return PNG 二进制数据
	 */
	public byte[] renderSvgBytesToPng(byte[] svgBytes, Integer widthPx, Integer heightPx) {
		Objects.requireNonNull(svgBytes, "svgBytes must not be null");
		try (InputStream in = new ByteArrayInputStream(svgBytes);
			 ByteArrayOutputStream out = new ByteArrayOutputStream(8 * 1024)) {
			transcodeToPng(in, out, widthPx, heightPx);
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Failed to render SVG to PNG", e);
		}
	}

	/**
	 * 将 SVG 文件渲染为 PNG 文件（自动创建父目录）。
	 *
	 * @param svgFile  输入 SVG 文件路径
	 * @param pngFile  输出 PNG 文件路径
	 * @param widthPx  目标宽度（可空），单位：px
	 * @param heightPx 目标高度（可空），单位：px
	 */
	public void renderSvgFileToPngFile(Path svgFile, Path pngFile, Integer widthPx, Integer heightPx) {
		Objects.requireNonNull(svgFile, "svgFile must not be null");
		Objects.requireNonNull(pngFile, "pngFile must not be null");
		try {
			Files.createDirectories(pngFile.toAbsolutePath().getParent());
			try (InputStream in = Files.newInputStream(svgFile);
				 OutputStream out = Files.newOutputStream(pngFile)) {
				transcodeToPng(in, out, widthPx, heightPx);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to render SVG file to PNG file", e);
		}
	}

	private void transcodeToPng(InputStream svgInputStream, OutputStream pngOutputStream, Integer widthPx, Integer heightPx) {
		SecurePngTranscoder transcoder = new SecurePngTranscoder();
		if (widthPx != null && widthPx > 0) {
			transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, widthPx.floatValue());
		}
		if (heightPx != null && heightPx > 0) {
			transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, heightPx.floatValue());
		}

		TranscoderInput input = new TranscoderInput(svgInputStream);
		TranscoderOutput output = new TranscoderOutput(pngOutputStream);
		try {
			transcoder.transcode(input, output);
		} catch (TranscoderException e) {
			throw new RuntimeException("Batik transcode failed", e);
		}
	}

	/**
	 * 自定义 PNGTranscoder，禁用外部资源与脚本，降低 XXE/外链风险。
	 */
	static class SecurePngTranscoder extends PNGTranscoder {
		@Override
		protected org.apache.batik.bridge.UserAgent createUserAgent() {
			return new UserAgentAdapter() {
				@Override
				public ExternalResourceSecurity getExternalResourceSecurity(org.apache.batik.util.ParsedURL resourceURL, org.apache.batik.util.ParsedURL docURL) {
					return new ExternalResourceSecurity() {
						@Override
						public void checkLoadExternalResource() {
							throw new SecurityException("Loading external resources is disabled");
						}
					};
				}

				@Override
				public ScriptSecurity getScriptSecurity(String scriptType, org.apache.batik.util.ParsedURL scriptURL, org.apache.batik.util.ParsedURL docURL) {
					return new ScriptSecurity() {
						@Override
						public void checkLoadScript() {
							throw new SecurityException("Running scripts in SVG is disabled");
						}
					};
				}
			};
		}
	}
}


