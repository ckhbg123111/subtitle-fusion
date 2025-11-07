## CapCutScriptDrivenController 接口文档

- 基础路径：`/api/capcut-script-driven`
- 控制器：`com.zhongjia.subtitlefusion.controller.CapCutScriptDrivenController`

### 接口调用链路与使用时机（从调用方视角）

- 本文推荐两种调用链路：
  - 本地处理链路：适用于 `cloudRendering=false` 或未指定云渲染。
  - 云渲染链路：适用于 `cloudRendering=true`，获取云侧更权威、细粒度的进度与结果。

- 调用链路 A：本地处理
  1. 调用 POST `/capcut-gen` 提交任务，使用自定义且全局唯一的 `taskId`；立即得到初始 `TaskResponse`（通常 `state=PENDING`）。
  2. 轮询 GET `/task/{taskId}` 获取任务状态与进度。
     - 轮询建议：每 2-3 秒；失败指数退避；最长等待可按业务设置（如 30 分钟）。
     - 终止条件：`state=COMPLETED` 或 `state=FAILED`。
  3. 当 `state=COMPLETED` 时，读取 `outputUrl`（以及可选 `resourcePackageZipUrl`）进行后续下载/分发。

- 调用链路 B：云渲染
  1. 调用 POST `/capcut-gen`，请求中设置 `cloudRendering=true`。
  2. 轮询 GET `/task/{taskId}`，当返回体中出现 `cloudTaskId` 后进入下一步（初始 POST 响应通常不包含该字段）。
  3. 优先轮询 GET `/cloud-task/{cloudTaskId}` 获取云侧权威状态与 `resultUrl`。
     - 轮询建议：每 3-5 秒；失败指数退避；与本地状态可并行观测但以云侧为准。
     - 终止条件：`status=SUCCESS` 或 `status=FAILED`。
  4. 成功时可直接使用云侧 `resultUrl`，或回查 GET `/task/{taskId}` 获取汇总态与 `outputUrl`（若平台将结果同步回本地）。

- 三个接口的使用时机
  - POST `/capcut-gen`
    - 何时用：创建新任务时使用；请确保 `taskId` 唯一（幂等），避免重复提交。
    - 注意：若网络不稳定导致不确定是否创建成功，先用 GET `/task/{taskId}` 查状态，避免二次提交。
  - GET `/task/{taskId}`
    - 何时用：本地处理链路的主查询接口；云渲染链路的兜底/汇总查询（拿 `outputUrl`、`resourcePackageZipUrl` 等）。
    - UI 场景：列表/详情页轮询展示进度条、阶段状态、错误原因。
  - GET `/cloud-task/{cloudTaskId}`
    - 何时用：启用云渲染后优先查询云侧真实进度与 `resultUrl`；`cloudTaskId` 需先通过 `/task/{taskId}` 轮询获取，当 `cloudTaskId` 为空时不应调用。
    - 场景：需要更及时与权威的进度，或直接消费云端产物链接时。

- 轮询与容错建议
  - 轮询间隔：2-5 秒；失败后指数退避（如 2s → 4s → 8s，封顶 10-15s）。
  - 超时策略：按照业务可设总时长；达到上限后提示用户并允许继续后台处理。
  - 幂等：若提示“任务ID已存在”，直接转为查询 `/task/{taskId}`。
  - 失败兜底：云侧查询失败可退回查本地 `/task/{taskId}` 以确认是否已落库。

---

### 1) 提交任务 - 生成字幕/草稿
- 方法：POST
- 路径：`/api/capcut-script-driven/capcut-gen`
- 请求体：`application/json`
- 响应体：`application/json`（`TaskResponse`）

请求体模型（`SubtitleFusionV2Request`）：

- 字段摘要：
  - `taskId`（必填）：业务自定义任务 ID，需全局唯一。
  - `videoUrl`（必填）：源视频 URL。
  - `cloudRendering`（可选）：是否走云渲染；为 `true` 时，`cloudTaskId` 将在后续通过 GET `/task/{taskId}` 查询获得。
  - `subtitleInfo`（可选）：字幕与插图编排，包含：
    - `commonSubtitleInfoList[]`：`text`、`startTime`、`endTime`、`subtitleEffectInfo{ textEffectId, textTemplateId, templateTexts, effectAudioUrl, keyWords }`
    - `pictureInfoList[]`：`pictureUrl`、`startTime`、`endTime`、`effectAudioUrl`（部分旧字段已废弃）

校验规则与幂等：
- `taskId`：必填且非空；若已存在，则返回提示“任务ID已存在，请使用不同的taskId”。
- `videoUrl`：必填且非空。
- 任务创建成功后异步处理（立即返回任务初始信息），可通过查询接口轮询进度。

请求示例：

```json
{
  "taskId": "task-001",
  "videoUrl": "https://example.com/video.mp4",
  "cloudRendering": true,
  "subtitleInfo": {
    "commonSubtitleInfoList": [
      {
        "text": "你好世界",
        "startTime": "00:00:01.000",
        "endTime": "00:00:03.000",
        "subtitleEffectInfo": {
          "effectAudioUrl": "https://example.com/audio.mp3",
          "keyWords": ["你好", "世界"],
          "textEffectId": "effect_001",
          "textTemplateId": "tpl_123",
          "templateTexts": ["A", "B"]
        }
      }
    ],
    "pictureInfoList": [
      {
        "pictureUrl": "https://example.com/pic.png",
        "startTime": "00:00:02.000",
        "endTime": "00:00:05.000",
        "effectAudioUrl": "https://example.com/in.mp3"
      }
    ]
  }
}
```

成功响应示例（`TaskResponse`）：

- 字段摘要：`taskId`、`state`（PENDING/DOWNLOADING/PROCESSING/UPLOADING/COMPLETED/FAILED）、`message`、`progress`、`outputUrl`、`resourcePackageZipUrl`、`errorMessage`、`createTime`、`updateTime`、`cloudTaskId`。

```json
{
  "taskId": "task-001",
  "state": "PENDING",
  "message": "任务已创建，等待处理",
  "outputUrl": null,
  "resourcePackageZipUrl": null,
  "errorMessage": null,
  "createTime": "2025-11-07 12:00:00",
  "updateTime": "2025-11-07 12:00:00",
  "progress": 0,
  "cloudTaskId": null
}
```

失败响应示例：
- `taskId` 为空：`{"taskId": null, "state": "PENDING", "message": "taskId 不能为空"}`
- `videoUrl` 为空：`{"taskId": "task-001", "state": "PENDING", "message": "videoUrl 不能为空"}`
- `taskId` 已存在：`{"taskId": "task-001", "state": "PENDING", "message": "任务ID已存在，请使用不同的taskId"}`

cURL 示例：

```bash
curl -X POST "http://localhost:8080/api/capcut-script-driven/capcut-gen" \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "task-001",
    "videoUrl": "https://example.com/video.mp4",
    "cloudRendering": true
  }'
```

---

### 2) 查询任务状态（本地任务）
- 方法：GET
- 路径：`/api/capcut-script-driven/task/{taskId}`
- 路径参数：
  - `taskId`：必填，提交任务时的业务 ID
- 响应体：`application/json`（`TaskResponse`）

成功响应示例：

```json
{
  "taskId": "task-001",
  "state": "PROCESSING",
  "message": "字幕渲染中",
  "outputUrl": null,
  "resourcePackageZipUrl": null,
  "errorMessage": null,
  "createTime": "2025-11-07 12:00:00",
  "updateTime": "2025-11-07 12:05:00",
  "progress": 42,
  "cloudTaskId": "cloud-abc"
}
```

未找到任务：

```json
{
  "taskId": "task-404",
  "state": "PENDING",
  "message": "任务不存在",
  "outputUrl": null,
  "resourcePackageZipUrl": null,
  "errorMessage": null,
  "createTime": "2025-11-07 12:00:00",
  "updateTime": "2025-11-07 12:00:00",
  "progress": 0
}
```

cURL 示例：

```bash
curl -X GET "http://localhost:8080/api/capcut-script-driven/task/task-001"
```

---

### 3) 查询云渲染任务进度（云侧 taskId）
- 方法：GET
- 路径：`/api/capcut-script-driven/cloud-task/{cloudTaskId}`
- 路径参数：
  - `cloudTaskId`：必填，云渲染平台返回的任务 ID
- 响应体：`application/json`（`CapCutCloudTaskStatus`）

响应模型要点：`taskId`、`success`、`progress`、`message`、`error`、`resultUrl`、`status`（PENDING/RUNNING/SUCCESS/FAILED）。

成功响应示例：

```json
{
  "taskId": "cloud-abc",
  "success": true,
  "progress": 80,
  "message": "RUNNING",
  "error": null,
  "resultUrl": null,
  "status": "RUNNING"
}
```

参数缺失示例：

```json
{
  "taskId": "",
  "success": false,
  "progress": null,
  "message": "cloudTaskId 不能为空",
  "error": null,
  "resultUrl": null,
  "status": null
}
```

cURL 示例：

```bash
curl -X GET "http://localhost:8080/api/capcut-script-driven/cloud-task/cloud-abc"
```

---

### 状态流转说明（`TaskState`）

- PENDING：等待处理
- DOWNLOADING：下载视频中
- PROCESSING：字幕渲染中
- UPLOADING：上传到 MinIO 中
- COMPLETED：处理完成
- FAILED：处理失败

- `message` 字段通常与上述状态的中文描述一致；失败时 `errorMessage` 会包含具体原因。
- 当 `cloudRendering=true` 时，`cloudTaskId` 通常通过后续的 GET `/task/{taskId}` 查询获得，再配合云侧查询接口获取更细粒度的进度与结果。

---

### 约定与注意事项
- **幂等性**：`taskId` 需全局唯一；重复提交会直接返回提示，不会重复处理。
- **时间格式**：字幕与插图的时间建议使用 `HH:mm:ss.SSS`。
- **废弃字段**：`SubtitleEffectInfo.effectType`、`PictureInfo.effectType` 已标记为废弃，建议使用模板/效果 ID 体系（`textEffectId`、`textTemplateId` 等）。
- **资源结果**：成功完成后，`outputUrl`（与可选的 `resourcePackageZipUrl`）在 `TaskResponse` 中返回。
 - **资源结果**：成功完成后，`outputUrl` 会返回草稿工程压缩包的下载地址（非成片视频播放地址），可选的 `resourcePackageZipUrl` 为素材资源包；如需播放链接，请参考云侧 `resultUrl`（通过 `/cloud-task/{cloudTaskId}` 获取）或自有转码分发产物。
- **错误消息**：常见错误包括“taskId 不能为空”、“videoUrl 不能为空”、“任务ID已存在，请使用不同的taskId”、“任务不存在”、“cloudTaskId 不能为空”。

