# 字幕渲染服务

## 项目简介
提供稳定可靠的字幕渲染服务，将SRT格式字幕文件合成到视频中。

## 技术栈
- **Spring Boot** 2.7.18
- **JavaCV** 1.5.6 (封装FFmpeg)
- **Java** 8

## 核心特性
- ✅ **Java2D字幕渲染** - 稳定可靠，不依赖FFmpeg滤镜
- ✅ **URL文件处理** - 支持从网络下载视频和字幕文件（新增）
- ✅ **中文字幕支持** - 自动编码检测和转换
- ✅ **视频参数保持** - 分辨率、帧率、编码格式、音频参数与原视频一致
- ✅ **权限验证** - 支持Token认证机制
- ✅ **错误处理完善** - 详细的异常信息和日志
- ✅ **模块化架构** - 服务职责分离，易于维护和扩展

## 接口说明

### 1. 本地文件字幕渲染
```
POST /api/subtitles/burn-local-srt
Content-Type: application/json
Authorization: sf_token_2024_admin
```

**请求体**：
```json
{
  "videoPath": "C:/path/to/video.mp4",
  "subtitlePath": "C:/path/to/subtitle.srt"
}
```

### 2. URL文件字幕渲染（新增）
```
POST /api/subtitles/burn-url-srt
Content-Type: application/json
Authorization: sf_token_2024_admin
```

**请求体**：
```json
{
  "videoUrl": "https://example.com/video.mp4",
  "subtitleUrl": "https://example.com/subtitle.srt"
}
```

**响应格式**（两个接口相同）：
```json
{
  "outputPath": "C:/path/to/output/video_sub_srt2d_20240101_120000.mp4",
  "message": "Java2D字幕渲染完成"
}
```

## 支持格式
- **视频格式**: MP4, AVI, MOV 等常见格式
- **字幕格式**: SRT (支持UTF-8, GBK, GB2312等编码)

## 快速开始

### 1. 构建并运行
```bash
mvn clean package
java -jar target/subtitle-fusion-0.0.1-SNAPSHOT.jar
```

### 2. 配置说明
在 `application.properties` 中配置：
```properties
# 输出目录
app.output-dir=output/
# 权限认证token
app.auth.tokens=sf_token_2024_admin,sf_token_2024_user1
```

### 3. 测试
使用 `test_subtitle_with_java2d.http` 文件进行接口测试，包含：
- 本地文件处理测试
- URL文件处理测试
- 权限验证测试

## 架构设计

### 服务拆分
项目采用模块化设计，将原来的单一大服务拆分为多个专门的服务：

- **SubtitleFusionService** - 主服务，协调各子服务
- **FileDownloadService** - 文件下载服务，处理URL文件下载
- **SubtitleParserService** - 字幕解析服务，处理SRT文件解析和编码转换
- **SubtitleRendererService** - 字幕渲染服务，负责将字幕绘制到视频帧
- **VideoProcessingService** - 视频处理服务，处理视频编码和合成

### URL处理流程
1. 接收视频和字幕URL
2. 并行下载到临时目录
3. 自动编码检测和转换
4. 视频处理和字幕合成
5. 自动清理临时文件

## 输出说明
- 输出文件命名格式：`原文件名_sub_srt2d_时间戳.扩展名`
- 默认输出目录：`output/`
- 视频质量：保持与原视频相同的码率和质量设置
- 临时文件：自动下载和清理，无需手动管理

## 更新日志
- **v2.0** - 新增URL文件处理功能，代码架构重构
- **v1.0** - 基础本地文件字幕渲染功能