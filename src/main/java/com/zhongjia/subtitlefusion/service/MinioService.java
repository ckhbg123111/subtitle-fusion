package com.zhongjia.subtitlefusion.service;

import com.zhongjia.subtitlefusion.config.MinioConfig;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.GetObjectResponse;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * MinIO文件上传服务
 */
@Service
public class MinioService {
    
    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    
    public MinioService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
        // 初始化时确保bucket存在
        initializeBucket();
    }
    
    /**
     * 初始化bucket，如果不存在则创建
     */
    private void initializeBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build()
            );
            
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build()
                );
                System.out.println("Created bucket: " + minioConfig.getBucketName());
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize bucket: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 上传文件到MinIO
     * @param filePath 本地文件路径
     * @param fileName 上传后的文件名
     * @return 对象在桶中的路径（例如：videos/xxx.mp4）
     */
    public String uploadFile(Path filePath, String fileName) {
        try {
            // 生成带时间戳的文件名以避免冲突
            String objectName = generateObjectName(fileName);
            
            // 上传文件
            try (InputStream inputStream = new FileInputStream(filePath.toFile())) {
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .object(objectName)
                                .stream(inputStream, filePath.toFile().length(), -1)
                                .contentType(getContentType(fileName))
                                .build()
                );
            }
            
            // 返回对象路径（不返回外部可访问URL）
            return objectName;
            
        } catch (Exception e) {
            System.err.println("Failed to upload file to MinIO: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    /**
     * 通过对象路径获取对象流（用于服务端代理下载）
     */
    public GetObjectResponse getObject(String objectPath) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取对象失败: " + objectPath + ", " + e.getMessage(), e);
        }
    }

	/**
	 * 通过对象路径按范围获取对象流（支持断点续传/视频按需加载）
	 * @param objectPath 对象路径
	 * @param offset 起始偏移（字节）
	 * @param length 读取长度（字节）
	 */
	public GetObjectResponse getObjectRange(String objectPath, long offset, long length) {
		try {
			return minioClient.getObject(
					GetObjectArgs.builder()
							.bucket(minioConfig.getBucketName())
							.object(objectPath)
							.offset(offset)
							.length(length)
							.build()
			);
		} catch (Exception e) {
			throw new RuntimeException("获取对象分片失败: " + objectPath + ", " + e.getMessage(), e);
		}
	}

    /**
     * 获取对象元数据
     */
    public StatObjectResponse statObject(String objectPath) {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectPath)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("获取对象元数据失败: " + objectPath + ", " + e.getMessage(), e);
        }
    }
    
    /**
     * 生成带时间戳的对象名称
     */
    private String generateObjectName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
            originalFileName = originalFileName.substring(0, dotIndex);
        }
        return String.format("videos/%s_%s%s", originalFileName, timestamp, extension);
    }
    
    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = fileName.toLowerCase();
        if (extension.endsWith(".mp4")) {
            return "video/mp4";
        } else if (extension.endsWith(".avi")) {
            return "video/x-msvideo";
        } else if (extension.endsWith(".mov")) {
            return "video/quicktime";
        } else if (extension.endsWith(".mkv")) {
            return "video/x-matroska";
        } else {
            return "application/octet-stream";
        }
    }
    
    /**
     * 构建文件访问URL - 使用外部访问地址
     */
    private String buildFileUrl(String objectName) {
        // 优先使用外部地址，如果没有配置则使用内部地址
        String baseUrl = minioConfig.getExtEndpoint() != null && !minioConfig.getExtEndpoint().isEmpty() 
            ? minioConfig.getExtEndpoint() 
            : minioConfig.getEndpoint();
        return String.format("%s/%s/%s", baseUrl, minioConfig.getBucketName(), objectName);
    }
}
