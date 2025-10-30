package com.zhongjia.subtitlefusion.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 全局API日志过滤器
 * 记录 /api/** 的请求参数、响应内容以及耗时
 */
@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiLoggingFilter.class);

    private static final int MAX_LOG_BODY_BYTES = 2048;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    // 需要跳过响应体日志的Content-Type前缀或完整匹配集合（大文件/流）
    private static final Set<String> SKIP_CONTENT_TYPES = new HashSet<>(Arrays.asList(
            "application/octet-stream",
            "application/zip",
            "application/x-7z-compressed",
            "application/x-msdownload",
            "application/pdf",
            "text/event-stream"
    ));
    private static final Set<String> SKIP_CONTENT_TYPE_PREFIX = new HashSet<>(Arrays.asList(
            "video/",
            "audio/",
            "image/"
    ));

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String uri = request.getRequestURI();
        // 仅记录 /api/**
        return !PATH_MATCHER.match("/api/**", uri);
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        // 避免异步多次记录
        return true;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

        long startNs = System.nanoTime();
        try {
            filterChain.doFilter(cachingRequest, cachingResponse);
        } finally {
            long tookMs = (System.nanoTime() - startNs) / 1_000_000;
            logRequestAndResponse(cachingRequest, cachingResponse, tookMs);
            // 必须拷贝回真实响应，否则客户端收不到响应体
            cachingResponse.copyBodyToResponse();
        }
    }

    private void logRequestAndResponse(ContentCachingRequestWrapper request,
                                       ContentCachingResponseWrapper response,
                                       long tookMs) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String clientIp = getClientIp(request);

        boolean isMultipart = isMultipart(request);

        String reqBody = "";
        if (!isMultipart) {
            reqBody = toBodyString(request.getContentAsByteArray(), getCharset(request.getCharacterEncoding()));
        } else {
            reqBody = "<multipart/form-data omitted>";
        }

        int status = response.getStatus();
        String contentType = response.getContentType();
        boolean skipRespBody = shouldSkipResponseBody(contentType);
        String respBody;
        if (skipRespBody) {
            respBody = "<binary/stream omitted>";
        } else {
            respBody = toBodyString(response.getContentAsByteArray(), getCharset(response.getCharacterEncoding()));
        }

        if (StringUtils.hasText(query)) {
            uri = uri + "?" + query;
        }

        // 统一一行输出，便于检索
        log.info("api access | {} {} | status={} | ip={} | took={}ms | reqBody={} | respBody={}",
                method, uri, status, clientIp, tookMs, reqBody, respBody);
    }

    private static String toBodyString(byte[] bodyBytes, Charset charset) {
        if (bodyBytes == null || bodyBytes.length == 0) {
            return "";
        }
        int len = Math.min(bodyBytes.length, MAX_LOG_BODY_BYTES);
        try {
            return new String(bodyBytes, 0, len, charset).replaceAll("\\s+", " ").trim();
        } catch (Exception e) {
            return "<unreadable body>";
        }
    }

    private static Charset getCharset(String encoding) {
        if (!StringUtils.hasText(encoding)) {
            return DEFAULT_CHARSET;
        }
        try {
            return Charset.forName(encoding);
        } catch (Exception e) {
            return DEFAULT_CHARSET;
        }
    }

    private static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.toLowerCase().startsWith("multipart/");
    }

    private static boolean shouldSkipResponseBody(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return false;
        }
        String ct = contentType.toLowerCase();
        if (SKIP_CONTENT_TYPES.contains(ct)) {
            return true;
        }
        for (String prefix : SKIP_CONTENT_TYPE_PREFIX) {
            if (ct.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    private static String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            int idx = xff.indexOf(',');
            return idx > 0 ? xff.substring(0, idx).trim() : xff.trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIp)) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}


