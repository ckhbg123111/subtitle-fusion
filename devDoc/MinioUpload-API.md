## `/api/minio/upload-by-url` 接口文档

### 接口概述

通过远程文件 URL（HTTP/HTTPS）将文件下载并上传至 MinIO 公有桶，成功后返回可直接访问的文件直链。适用于将第三方可访问资源“转存”到本服务的对象存储中。

### 接口信息

- 接口路径: `/api/minio/upload-by-url`
- 请求方法: `POST`
- Content-Type: `application/x-www-form-urlencoded`
- 响应类型: `application/json`
- 鉴权: 无（默认无需鉴权；若部署侧加了网关/鉴权，请以实际为准）

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileUrl | String | 是 | 远程文件URL；必须以 `http://` 或 `https://` 开头，且服务器可访问 |

说明与约束：
- 仅支持 `http/https` 协议，否则直接返回错误信息。
- 服务会先尝试 `HEAD`，若被拒绝（常见403/405）会自动回退 `GET`。
- 文件名从 URL 路径末尾自动解析（会进行URL解码）；若无法解析，则使用默认文件名 `file.bin`。
- 即使无法提前获知文件长度，也会在服务端以“未知长度”模式上传到 MinIO。

### 响应

成功：

```json
{
  "url": "https://minio.example.com/public/your-file-name.ext"
}
```

失败（说明性消息，HTTP状态码仍可能为200）：

```json
{
  "message": "错误信息描述"
}
```

常见错误消息：
- `无效的URL（仅支持 http/https）`：`fileUrl` 为空或协议不为 http/https。
- `URL不可访问，GET状态码=xxx[, HEAD状态码=yyy]`：远端资源不可达或被拒绝。

调用方建议：以是否存在 `url` 字段作为成功判断；若无 `url` 则读取 `message` 获取失败原因。

### 调用示例

curl：

```bash
curl -X POST "http://localhost:8081/api/minio/upload-by-url" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "fileUrl=https://example.com/path/to/video.mp4"
```

HTTP：

```http
POST /api/minio/upload-by-url HTTP/1.1
Host: localhost:8081
Content-Type: application/x-www-form-urlencoded

fileUrl=https%3A%2F%2Fexample.com%2Fpath%2Fto%2Fvideo.mp4
```

### 返回字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| url | String | 成功时返回，MinIO 公有桶中文件的直链 |
| message | String | 失败时返回的错误描述 |

### 最佳实践

- 使用可公开直链，避免需要鉴权或有强防盗链限制的URL。
- 大文件上行/转存时，在客户端设置合理的超时与重试策略。
- 若需访问控制，请在部署侧调整 MinIO 桶策略或在网关增加鉴权。


## `/api/minio/upload-by-url-path` 接口文档

### 接口概述

通过远程文件 URL（HTTP/HTTPS）将文件下载后上传至默认（非公开）桶，成功后返回对象在桶内的路径（path），不返回外部可访问直链。

### 接口信息

- 接口路径: `/api/minio/upload-by-url-path`
- 请求方法: `POST`
- Content-Type: `application/x-www-form-urlencoded`
- 响应类型: `application/json`
- 鉴权: 无（默认无需鉴权；若部署侧加了网关/鉴权，请以实际为准）

### 请求参数

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| fileUrl | String | 是 | 远程文件URL；必须以 `http://` 或 `https://` 开头，且服务器可访问 |

说明与约束：
- 仅支持 `http/https` 协议，否则直接返回错误信息。
- 服务会先尝试 `HEAD`，若被拒绝（常见403/405）会自动回退 `GET`。
- 文件名从 URL 路径末尾自动解析（会进行URL解码）；若无法解析，则使用默认文件名 `file.bin`。
- 服务将先下载至临时文件，再调用默认桶上传方法，返回对象路径（如 `videos/xxx_yyyyMMdd_HHmmss.ext`）。

### 响应

成功：

```json
{
  "path": "videos/your-file-name_20250101_120000.ext"
}
```

失败（说明性消息，HTTP状态码仍可能为200）：

```json
{
  "message": "错误信息描述"
}
```

常见错误消息：
- `无效的URL（仅支持 http/https）`：`fileUrl` 为空或协议不为 http/https。
- `URL不可访问，GET状态码=xxx[, HEAD状态码=yyy]`：远端资源不可达或被拒绝。

调用方建议：以是否存在 `path` 字段作为成功判断；若无 `path` 则读取 `message` 获取失败原因。

### 调用示例

curl：

```bash
curl -X POST "http://localhost:8081/api/minio/upload-by-url-path" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "fileUrl=https://example.com/path/to/video.mp4"
```

HTTP：

```http
POST /api/minio/upload-by-url-path HTTP/1.1
Host: localhost:8081
Content-Type: application/x-www-form-urlencoded

fileUrl=https%3A%2F%2Fexample.com%2Fpath%2Fto%2Fvideo.mp4
```

### 返回字段说明

| 字段名 | 类型 | 说明 |
|--------|------|------|
| path | String | 成功时返回，对象在默认桶中的路径 |
| message | String | 失败时返回的错误描述 |

