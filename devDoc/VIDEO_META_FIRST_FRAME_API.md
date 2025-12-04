## `/video-meta-first-frame` 接口说明

### 接口概述

`/video-meta-first-frame` 用于根据远程视频 URL，探测视频的分辨率并抽取第一帧图片，将首帧图上传到 MinIO 公开桶，返回分辨率及首帧图访问地址。

### 接口信息

- **接口路径**：`/api/subtitles/video-meta-first-frame`
- **请求方法**：`POST`
- **请求类型**：`application/x-www-form-urlencoded`
- **响应类型**：`application/json`

### 请求参数

| 参数名      | 类型   | 必填 | 说明                                                   |
|-----------|--------|------|--------------------------------------------------------|
| `videoUrl` | String | 是   | 视频文件的 HTTP/HTTPS URL，服务端会通过该地址下载视频 |

> 要求：`videoUrl` 不能为空且必须以 `http://` 或 `https://` 开头。

### 成功响应

```json
{
  "width": 1920,
  "height": 1080,
  "firstFramePath": "/public-bucket/videos/demo_first_frame_20251204_153959.jpg",
  "firstFrameUrl": "https://minio.example.com/public-bucket/videos/demo_first_frame_20251204_153959.jpg"
}
```

- **width / height**：视频分辨率（像素），若探测失败会回退为 `1920x1080`。
- **firstFramePath**：首帧图片在 MinIO 中的对象路径（含桶名），便于服务端内部使用。
- **firstFrameUrl**：首帧图片的完整访问 URL，可直接在前端展示。

### 错误响应示例

- 参数错误（URL 非法）：

```json
{
  "error": "invalid_url",
  "message": "videoUrl 不能为空且必须是 http/https URL"
}
```

- 抽帧失败或其他内部错误（示例）：

```json
{
  "error": "extract_failed",
  "message": "无法从视频中抽取第一帧"
}
```


