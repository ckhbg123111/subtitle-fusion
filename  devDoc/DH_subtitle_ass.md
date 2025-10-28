# 数字人ASS字幕方案
在 SubtitleFusionController 中新增一个接口 /burn-as-ass/async, 用于替代原接口(/burn-url-srt/async)的功能
请求参数为 SubtitleFusionV2Request


## 需求
1. 给定一段视频及字幕信息、插图信息，将字幕、插图烧录至视频
2. 技术方案为FFmpeg，字幕方案采用ass
3. 插图动效可复用现有代码逻辑
4. 字幕与插图在入场时定义了音效，需要能够在进场时机听到音效

## ass 动效设计
需要设计一个方法，将请求参数转为ass字幕文件后再进行烧录
每行字幕都需要支持不同的字幕动效，请设计合适的设计模式，以方便扩充ass动效

---

## 异步数字人 ASS 字幕接口文档

### 1) 提交异步任务（ASS）
- 方法: POST
- 路径: `/api/subtitles/burn-as-ass/async`
- Content-Type: `application/json`

#### 请求体模型 `SubtitleFusionV2Request`
```json
{
  "taskId": "string",                 // 必填，任务ID（全局唯一）
  "videoUrl": "https://.../in.mp4",  // 必填，视频URL（http/https）
  "subtitleInfo": {
    "commonSubtitleInfoList": [        // 可选，普通字幕列表（每行可选不同动效/音效）
      {
        "text": "人到中年养生",
        "startTime": "0:00:00.00",   // 或 "0.00" 秒数字符串；均支持 ","/"." 毫秒分隔
        "endTime":   "0:00:02.20",
        "subtitleEffectInfo": {
          "effectType": "LEFT_IN_BOUNCE",   // 见枚举
          "effectAudioUrl": "https://.../whoosh.m4a", // 可选，入场音效；将在 startTime 延时混入
          "keyWords": ["中年", "养生"]   // 可选，关键字高亮用于 KEYWORD_HIGHLIGHT
        }
      }
    ],
    "pictureInfoList": [               // 可选，插图叠加（沿用通用 Overlay 枚举）
      {
        "pictureUrl": "https://.../pic.png",
        "startTime": "0:00:01.00",
        "endTime":   "0:00:03.00",
        "effectType": "LEFT_IN_RIGHT_OUT",  // 见 OverlayEffectType
        "effectAudioUrl": "https://.../pop.m4a" // 可选，插图入场音效
      }
    ]
  }
}
```

#### 字段与动效枚举
- `AssSubtitleEffectTypeEnum`：
  - `TYPEWRITER_CURSOR`：淡入，打字风格（轻量化近似）
  - `LEFT_IN_BOUNCE`：左侧快速滑入 + “咻”速度感 + 刹车回弹（参考示例）
  - `KEYWORD_HIGHLIGHT`：按关键字加粗 + 变色（不放大）
  - `DEFAULT`：默认淡入

- `OverlayEffectType`（插图/通用叠加特效）：
  - `FLOAT_WAVE`、`LEFT_IN_RIGHT_OUT`、`TOP_IN_FADE_OUT`、`LEFT_IN_BLINDS_OUT`、`BLINDS_IN_CLOCK_OUT`、`FADE_IN_FADE_OUT`

#### 行为说明
- 分辨率探测：使用 `ffprobe` 自动探测输入视频分辨率，写入 ASS `PlayResX/PlayResY` 保持像素级一致。
- 字号规则：
  - 默认字号为视频高度的约 4%（并不小于 18），可用配置覆盖：
    - `subtitlefusion.render.fontSizePx`（绝对字号）
    - `subtitlefusion.render.fontScale`（基于默认的比例）
    - `subtitlefusion.render.minFontSizePx`（不低于该下限）
- 垂直位置：
  - 默认 `bottom-center`，底边距约为视频高度的 25%，可用配置覆盖：
    - `subtitlefusion.render.verticalAnchor`（bottom/top/center）
    - `subtitlefusion.render.marginBottomPx` / `marginBottomPercent`
    - `subtitlefusion.render.marginTopPx` / `marginTopPercent`
- 字体：默认 `Microsoft YaHei`，可通过 `subtitlefusion.render.fontFamily` 配置。
- 行级动效：每行根据 `effectType` 应用不同 ASS 标签；`LEFT_IN_BOUNCE` 按分辨率自适应 `\move/\t/\frz` 等序列以实现“滑入+回弹”效果；`KEYWORD_HIGHLIGHT` 对关键词包裹加粗/变色标签。
- 入场音效：若提供 `effectAudioUrl`，将按该元素的 `startTime` 使用 `adelay` 延时并与基础音频 `amix` 混音；若原视频无音轨，则仅以音效输出音频流。
- 插图：下载图片资源，按 `startTime/endTime` 控制 `overlay` 叠加（复用已存在的图片动效策略）。
- 产物：合成完成后上传至 MinIO，返回对象路径；前端可通过已有下载代理 `/api/subtitles/download?path=...` 或直链方式获取。

#### 成功响应（立即返回任务信息）
```json
{
  "taskId": "ass_task_20251028_001",
  "state": "PENDING",
  "message": "任务已创建，等待处理",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": "2025-10-28 14:52:00",
  "updateTime": "2025-10-28 14:52:00",
  "progress": 0
}
```

#### 失败响应（示例）
```json
{
  "taskId": "ass_task_20251028_001",
  "state": null,
  "message": "任务ID已存在，请使用不同的taskId",
  "outputUrl": null,
  "errorMessage": null,
  "createTime": null,
  "updateTime": null,
  "progress": 0
}
```

#### 任务状态查询
- 方法: GET
- 路径: `/api/subtitles/task/{taskId}`
- 返回：与现有异步字幕任务一致，`state`/`progress`/`outputUrl` 等。

#### 使用示例
```bash
curl -X POST "http://localhost:8081/api/subtitles/burn-as-ass/async" \
  -H "Content-Type: application/json" \
  -d '{
    "taskId": "ass_task_20251028_001",
    "videoUrl": "https://example.com/video.mp4",
    "subtitleInfo": {
      "commonSubtitleInfoList": [
        {
          "text": "人到中年养生",
          "startTime": "0:00:00.00",
          "endTime":   "0:00:02.20",
          "subtitleEffectInfo": {
            "effectType": "LEFT_IN_BOUNCE",
            "effectAudioUrl": "https://example.com/whoosh.m4a"
          }
        }
      ],
      "pictureInfoList": [
        {
          "pictureUrl": "https://example.com/pic.png",
          "startTime": "0:00:01.00",
          "endTime":   "0:00:03.00",
          "effectType": "LEFT_IN_RIGHT_OUT"
        }
      ]
    }
  }'
```

### 注意事项
- 服务需要部署支持 `libass` 的 FFmpeg 版本；服务器需存在中文字体（如 Microsoft YaHei / Noto Sans CJK）。
- 外部资源（视频/图片/音频）需为可直连的 http/https URL。
- 大分辨率（如 4K）下字号将随高度自适应；可通过配置精细控制字号与边距。