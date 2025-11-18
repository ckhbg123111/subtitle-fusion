# SubtitleFusion API 接口文档

## `/burn-url-srt` 接口说明

### 接口概述

`/burn-url-srt` 是一个视频字幕融合接口，支持通过视频URL和上传的字幕文件进行字幕烧录。该接口使用Java2D技术进行字幕渲染，提供稳定可靠的字幕合成功能。

### 接口信息

- **接口路径**: `/api/subtitles/burn-url-srt`
- **请求方法**: `POST`
- **请求类型**: `multipart/form-data`
- **响应类型**: `application/json`

### 核心功能

1. **视频下载**: 从提供的URL下载视频文件
2. **字幕上传**: 接收用户上传的SRT格式字幕文件
3. **字幕解析**: 自动解析SRT字幕内容和时间轴
4. **字幕渲染**: 使用Java2D技术将字幕烧录到视频中
5. **云存储上传**: 处理完成后自动上传到MinIO对象存储
6. **资源清理**: 自动清理临时文件，节省服务器存储空间

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `videoUrl` | String | 是 | 视频文件的URL地址，支持HTTP/HTTPS协议 |
| `subtitleFile` | MultipartFile | 是 | 上传的字幕文件，仅支持SRT格式 |

#### 参数详细说明

**videoUrl**:
- 必须是有效的HTTP或HTTPS URL
- 支持常见的视频格式（MP4、AVI、MOV等）
- 确保URL可公开访问，无需特殊认证

**subtitleFile**:
- 文件格式：仅支持`.srt`格式
- 编码支持：自动检测和处理各种字符编码
- 文件大小：建议小于10MB
- 时间轴格式：标准SRT时间格式（HH:MM:SS,mmm --> HH:MM:SS,mmm）

### 响应格式

#### 成功响应

```json
{
  "outputPath": "https://minio.example.com/videos/processed_video_20231201_123456.mp4",
  "message": "Java2D字幕渲染完成，视频已上传到MinIO"
}
```

#### 错误响应

```json
{
  "outputPath": null,
  "message": "错误信息描述"
}
```

### 响应字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| `outputPath` | String | 处理后视频的MinIO下载链接，失败时为null |
| `message` | String | 处理结果消息或错误信息 |

### 错误码说明

| 错误信息 | 说明 | 解决方案 |
|----------|------|----------|
| "videoUrl 不能为空" | 未提供视频URL | 请提供有效的视频URL |
| "字幕文件不能为空" | 未上传字幕文件 | 请上传SRT格式的字幕文件 |
| "无效的URL格式" | URL格式不正确 | 确保URL以http://或https://开头 |
| "Java2D方案仅支持 .srt 字幕格式" | 字幕文件格式不支持 | 请上传.srt格式的字幕文件 |

### 请求示例

#### cURL 示例

```bash
curl -X POST http://localhost:8081/api/subtitles/burn-url-srt \
  -F "videoUrl=https://example.com/sample-video.mp4" \
  -F "subtitleFile=@/path/to/subtitle.srt"
```

#### HTTP 请求示例

```http
POST /api/subtitles/burn-url-srt HTTP/1.1
Host: localhost:8081
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="videoUrl"

https://example.com/sample-video.mp4
------WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="subtitleFile"; filename="subtitle.srt"
Content-Type: text/plain

1
00:00:01,000 --> 00:00:03,000
这是第一行字幕

2
00:00:04,000 --> 00:00:06,000
这是第二行字幕

------WebKitFormBoundary7MA4YWxkTrZu0gW--
```

### 处理流程

1. **参数验证**: 检查videoUrl和subtitleFile是否为空，验证URL格式和文件扩展名
2. **文件保存**: 将上传的字幕文件保存到临时目录
3. **视频下载**: 从提供的URL下载视频文件到临时目录
4. **字幕解析**: 解析SRT字幕文件，提取时间轴和文本内容
5. **视频处理**: 使用Java2D技术逐帧渲染字幕到视频上
6. **文件上传**: 将处理后的视频上传到MinIO对象存储
7. **资源清理**: 删除所有临时文件
8. **返回结果**: 返回MinIO中的视频访问链接

### 技术特点

#### 优势
- **稳定可靠**: 使用Java2D技术，不依赖FFmpeg滤镜
- **编码兼容**: 自动处理各种字符编码的SRT文件
- **中文优化**: 针对中文字幕进行特殊优化
- **资源管理**: 自动清理临时文件，避免磁盘空间浪费
- **云存储**: 处理结果自动上传到对象存储，便于分享和下载

#### 限制
- **格式支持**: 目前仅支持SRT格式字幕
- **处理速度**: 由于逐帧处理，速度相对较慢
- **资源占用**: 处理大视频文件时占用较多内存和CPU资源

### 最佳实践

1. **视频URL**: 使用稳定可靠的视频源，避免临时链接
2. **字幕文件**: 确保SRT文件格式正确，时间轴准确
3. **文件大小**: 建议视频文件不超过500MB，以保证处理速度
4. **网络环境**: 确保服务器能够正常访问视频URL
5. **错误处理**: 客户端应当处理各种错误情况，提供用户友好的提示

### 相关配置

接口的运行需要以下组件配置：

- **MinIO服务**: 用于存储处理后的视频文件
- **FFmpeg**: 用于视频编解码处理
- **临时目录**: 用于存储下载和处理过程中的临时文件

### 注意事项

1. 处理时间取决于视频大小和字幕数量，大文件可能需要几分钟时间
2. 服务器需要足够的磁盘空间存储临时文件
3. 确保MinIO服务正常运行，否则无法返回最终的视频链接
4. 建议在生产环境中设置合适的超时时间和资源限制

---

*最后更新时间: 2024年12月*

## `/capcut-script-driven/cloud-task/{cloudTaskId}` 接口说明

### 接口概述

基于 CapCut 云渲染任务的进度查询接口。你需要在提交云渲染任务后，使用返回的云侧 `taskId` 查询任务状态。

### 接口信息

- **接口路径**: `/api/capcut-script-driven/cloud-task/{cloudTaskId}`
- **请求方法**: `GET`
- **响应类型**: `application/json`

### 路径参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `cloudTaskId` | String | 是 | CapCut 云侧任务 ID（提交云渲染时返回） |

### 返回数据模型

- 返回包装：`Result<CapCutCloudTaskStatus>`
- `CapCutCloudTaskStatus` 字段说明：
  - `taskId` String: 云侧任务ID
  - `success` boolean: 任务是否成功；仅当任务完成成功时为 `true`，处理中/失败均为 `false`
  - `progress` Integer: 进度 0-100
  - `message` String: 状态文本，如“排队、导出、上传、成功、错误…”
  - `error` String: 业务错误信息
  - `result` String: 成功时的成片地址（下载/播放URL）
  - `status` String: 枚举（PENDING/PROCESSING/UPLOADING/SUCCESS/DOWNLOADING/EXPORTING/FAILURE）

### 响应示例

成功（任务已完成）：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": "abc123",
    "success": true,
    "progress": 100,
    "message": "SUCCESS",
    "error": null,
    "result": "https://example.com/output/video.mp4",
    "status": "SUCCESS"
  }
}
```

失败或处理中（云侧 success=false 时，当前接口按错误返回；data 为空）：

```json
{
  "code": 400,
  "message": "查询失败或任务未完成",
  "data": null
}
```

> 说明：当前实现中，当云侧返回 `success=false`（包括排队/处理中/失败场景），接口会返回业务错误（code=400），不透出中间进度数据。若需要在处理中也获取 `progress/message/status` 等，可在后续版本将云侧返回映射为 `data` 并以业务 code=200 返回。

### 兼容性说明

- 云侧可能返回 `output` 为空字符串 `""`。服务端已将该值归一化为 `null`，不会导致反序列化异常。

### 调用示例

```bash
curl -X GET "http://localhost:8081/api/capcut-script-driven/cloud-task/abc123"
```