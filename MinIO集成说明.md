# MinIO集成说明

## 概述

本项目已成功集成MinIO对象存储服务，用于存储合成后的视频文件。`/burn-url-srt`接口现在会将合成的视频上传到MinIO并返回访问URL，而不是存储在本地。

## 新增功能

### 1. MinIO依赖
- 在`pom.xml`中添加了MinIO客户端依赖（版本8.4.6）
- 兼容Java 8环境

### 2. MinIO配置
在`application.properties`中添加了以下配置：

```properties
# MinIO配置
minio.endpoint=http://xxx-file:9010
minio.ext-endpoint=https://net-file.netxxxdata.com
minio.bucket-name=nis
minio.access-key=admin
minio.secret-key=xxx@123456
```

### 3. 核心组件

#### MinioConfig (配置类)
- 自动配置MinIO客户端Bean
- 支持属性注入和配置绑定

#### MinioService (服务类)
- 文件上传功能
- 自动创建bucket（如果不存在）
- 生成带时间戳的文件名避免冲突
- 支持多种视频格式的Content-Type设置
- 自动构建文件访问URL

### 4. 接口变更

#### `/burn-url-srt` 接口
- **变更前**: 返回本地文件路径
- **变更后**: 返回MinIO文件访问URL
- 自动清理本地临时文件和输出文件
- 响应消息更新为"Java2D字幕渲染完成，视频已上传到MinIO"

#### `/burn-local-srt` 接口
- 保持原有行为，返回本地文件路径
- 适用于本地文件处理场景

## 使用方法

### 1. 启动MinIO服务
```bash
# 使用Docker启动MinIO
docker run -p 9000:9000 -p 9001:9001 \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  minio/minio server /data --console-address ":9001"
```

### 2. 调用API
使用相同的请求格式调用`/burn-url-srt`接口：

```json
{
  "videoUrl": "https://example.com/video.mp4",
  "subtitleUrl": "https://example.com/subtitle.srt"
}
```

### 3. 响应格式
```json
{
  "outputPath": "https://net-file.netxxxdata.com/nis/videos/video_20250117_143022.mp4",
  "message": "Java2D字幕渲染完成，视频已上传到MinIO"
}
```

## 配置说明

### 必需配置
- `minio.endpoint`: MinIO内部服务地址（用于连接）
- `minio.access-key`: 访问密钥
- `minio.secret-key`: 密钥
- `minio.bucket-name`: 存储桶名称

### 可选配置
- `minio.ext-endpoint`: 外部访问地址（用于生成公网可访问的URL，如果未配置则使用endpoint）

## 文件组织

上传的视频文件会按以下规则组织：
- 路径格式: `videos/{原文件名}_{时间戳}.{扩展名}`
- 时间戳格式: `yyyyMMdd_HHmmss`
- 例如: `videos/sample_video_20250117_143022.mp4`

## 外部访问支持

系统支持内外网分离的MinIO部署模式：
- **内部连接**: 使用`minio.endpoint`连接到MinIO服务进行文件上传
- **外部访问**: 使用`minio.ext-endpoint`生成公网可访问的文件URL
- **自动适配**: 如果未配置外部地址，则使用内部地址生成URL

## 错误处理

- MinIO连接失败会抛出运行时异常
- 自动重试bucket创建
- 上传失败时保留详细错误信息
- 确保临时文件在异常情况下也能被清理

## 注意事项

1. **MinIO服务**: 确保MinIO服务正常运行且可访问
2. **网络配置**: 检查防火墙和网络策略
3. **存储空间**: 监控MinIO存储使用情况
4. **访问权限**: 确保bucket具有适当的访问策略
5. **清理策略**: 考虑实施定期清理策略管理存储空间

## 测试

可以使用提供的`test_minio_integration.http`文件测试MinIO集成功能。
