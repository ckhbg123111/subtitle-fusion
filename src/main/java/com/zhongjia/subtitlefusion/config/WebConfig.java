package com.zhongjia.subtitlefusion.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类 - 配置跨域和权限校验拦截器
 * 
 * 执行顺序：
 * 1. CORS处理（框架内置，自动在拦截器之前执行）
 * 2. 权限校验拦截器（order=1）
 */
@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addCorsMappings(@org.springframework.lang.NonNull CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的HTTP方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带凭证（如Cookie、Authorization头）
                .maxAge(3600);  // 预检请求缓存时间（1小时）
    }

    @Override
    public void addInterceptors(@org.springframework.lang.NonNull InterceptorRegistry registry) {
        // 对所有API路径添加权限校验拦截器，但排除公开的下载接口
        // 设置较高的order值，确保在CORS处理之后执行
        registry.addInterceptor(new AuthInterceptor(appProperties))
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/subtitles/download")
                .order(1);  // 设置拦截器执行顺序
    }
}
