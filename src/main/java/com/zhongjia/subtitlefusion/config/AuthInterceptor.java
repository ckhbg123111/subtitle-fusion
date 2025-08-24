package com.zhongjia.subtitlefusion.config;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * API权限校验拦截器
 * 校验请求头中的Authorization token
 */
public class AuthInterceptor implements HandlerInterceptor {

    private final AppProperties appProperties;

    public AuthInterceptor(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public boolean preHandle(@org.springframework.lang.NonNull HttpServletRequest request, 
                           @org.springframework.lang.NonNull HttpServletResponse response, 
                           @org.springframework.lang.NonNull Object handler) throws Exception {
        // 如果权限校验被禁用，直接通过
        if (!appProperties.getAuth().isEnabled()) {
            return true;
        }

        // 获取Authorization头
        String authHeader = request.getHeader("Authorization");
        
        if (!StringUtils.hasText(authHeader)) {
            sendErrorResponse(response, "缺少Authorization头");
            return false;
        }

        // 支持两种格式：
        // 1. Authorization: Bearer <token>
        // 2. Authorization: <token>
        String token = authHeader;
        if (authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        // 校验token是否在允许列表中
        List<String> validTokens = appProperties.getAuth().getTokens();
        if (validTokens == null || !validTokens.contains(token)) {
            sendErrorResponse(response, "无效的token");
            return false;
        }

        return true;
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        String jsonResponse = String.format("{\"code\":401,\"message\":\"%s\",\"data\":null}", message);
        response.getWriter().write(jsonResponse);
    }
}
