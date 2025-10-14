## 服务端代理下载改造说明

### 背景

- 之前任务完成后返回的是 MinIO 外链 URL（例如 `http(s)://<minio>/<bucket>/videos/xxx.mp4`）。
- 当桶为私有或外链不可达时，浏览器匿名访问会返回 AccessDenied。
- 出于安全考虑，不再直接暴露外链 URL；改为由服务端代理下载，接口仅返回对象在桶中的存储路径。

### 改动摘要（不改变现有字段名）

- **`TaskResponse.outputUrl` 含义变更**：
  - 由“下载 URL”调整为“对象路径（Object Path）”，例如：`videos/processed_xxx.mp4`。
  - 字段名保持为 `outputUrl`，以避免前后端协议大量变更。

- **`MinioService`**：
  - `uploadFile(Path filePath, String fileName)` 现在返回对象路径（如 `videos/...mp4`），不再返回外部可访问 URL。
  - 新增：`getObject(String objectPath)`、`statObject(String objectPath)`，用于服务端读取对象流和元数据。

- **`SubtitleFusionController`**：
  - 新增下载接口：`GET /api/subtitles/download?path=<objectPath>`，服务端从 MinIO 拉取并流式返回文件。
  - 构造函数新增注入 `MinioService`（不影响现有接口的请求体或返回体结构）。

### 受影响文件

- `src/main/java/com/zhongjia/subtitlefusion/service/MinioService.java`
- `src/main/java/com/zhongjia/subtitlefusion/controller/SubtitleFusionController.java`

### 接口用法

- **查询任务状态**：`GET /api/subtitles/task/{taskId}`
  - 当 `state=COMPLETED` 时，`outputUrl` 字段返回对象路径（示例：`videos/abc_20250101_103000.mp4`）。

- **下载结果文件（服务端代理）**：`GET /api/subtitles/download?path=<objectPath>`
  - 其中 `<objectPath>` 即前述 `outputUrl` 的原值。
  - 示例：
    ```bash
    curl -v -G \
      --data-urlencode "path=videos/abc_20250101_103000.mp4" \
      "http://<your-server>/api/subtitles/download"
    ```
  - 响应头包含 `Content-Disposition: attachment`，默认触发下载；`Content-Type` 依据对象元数据返回。

### 前端/客户端迁移指南

- 旧逻辑（直接使用 `outputUrl` 作为可访问下载链接）需要调整为：
  - 从任务状态接口读取 `outputUrl`（现在是对象路径）。
  - 拼接服务端下载地址：`/api/subtitles/download?path=<outputUrl>` 发起下载/播放。

### 错误与返回

- `400 Bad Request`：`path` 为空、以 `/` 开头或包含 `..` 等非法路径。
- `500 Internal Server Error`：对象不存在或 MinIO 访问异常（当前版本统一返回 500；如需区分 404，可在控制器中进一步细化异常映射）。

### 安全性

- 不再暴露 MinIO 外链，避免匿名访问与外部泄露风险。
- 控制器对 `path` 做了基本校验，避免路径穿越（以 `/` 开头或包含 `..` 会被拒绝）。
- 如需鉴权，请在现有 `AuthInterceptor` 或网关层为 `/api/subtitles/download` 增加身份校验与限流策略。

### 配置与部署

- 继续使用现有 `minio` 配置（`endpoint`、`accessKey`、`secretKey`、`bucketName` 等）。
- 无需改动 MinIO 桶策略（保持私有即可）。

### 可选后续增强

- **Range/断点续传**：为大文件或在线视频播放增加 `Range` 支持，返回 `206 Partial Content`。
- **inline 播放**：根据场景将 `Content-Disposition` 调整为 `inline`，配合前端 `<video>` 标签播放。
- **错误码细化**：针对对象不存在返回 `404 Not Found`，其余异常保留 `500`。

### 附：关键代码位置

- `MinioService#uploadFile`：返回对象路径（`videos/...`）。
- `MinioService#getObject/statObject`：读取对象流与元数据。
- `SubtitleFusionController#/api/subtitles/download`：根据 `path` 从 MinIO 拉取并流式返回。


