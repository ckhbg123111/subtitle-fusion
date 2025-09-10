package com.zhongjia.subtitlefusion.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * MinIO配置类
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@EnableConfigurationProperties
@Data
public class MinioConfig {
    
    private String endpoint;
    private String extEndpoint;
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String publicBucketName;
    
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
