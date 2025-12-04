---
name: 视频链V2（CapCut集成）实施方案
overview: ""
todos:
  - id: 62d642b8-3f38-498a-8e2c-4679a8065cee
    content: 编写一条端到端测试用例与示例请求
    status: pending
---

# 视频链V2（CapCut集成）实施方案

## 目标

- 基于 `VideoChainV2Request` 实现视频链自动合成：段内拼接、按音频定段时长、段间转场、插图与字幕（底部/标题），最终生成 CapCut 草稿；
- 提供“客户端触发云渲染”的接口；
- 严谨的字幕时间计算：按全局时间轴对齐，考虑转场时长的重叠扣除。

## 关键设计

- **段内拼接（本地FFmpeg）**：按文档要求先将每段内的若干无声小视频使用 FFmpeg `concat` 无损拼接，得到 `segment_i_nosound.mp4`。
- **段时长来源**：优先使用 `segment.audioUrl` 的音频时长；若 `duration` 提供则以请求为准；时长为 0/缺失时尝试远程探测（`MediaProbeUtils` 或 MCP `/get_duration` 可扩展）。
- **全局时间轴与转场叠加**：
- 设第 i 段时长为 `D[i]`，第 i 与 i+1 段转场时长为 `T[i]`（秒）。
- 段起始时间：`S[1]=0`；`S[i+1]=S[i]+D[i]-T[i]`。
- 时间线总时长：`sum(D) - sum(T)`。
- 所有元素（视频、音频、字幕、图片）均使用“绝对时间”= 段内相对时间 + `S[i]`。
- **CapCut 草稿生成**：
- `create_draft`（宽高按首段或探测分辨率，默认 1080x1920）。
- 逐段 `add_video`：使用拼接后视频 URL，`target_start=S[i]`；若存在段间转场，在第 i 段上设置 `transition` 与 `transition_duration=T[i]`。
- 段音频 `add_audio`：`audio_url=segment.audioUrl`，`target_start=S[i]`，与段时长一致；必要时设置 `volume`。
- 背景乐（可选）`add_audio`：`target_start=0`，循环；支持 `fade_in/out` 与 `sidechaincompress`（鸭嘴）逻辑视为后续增强。
- 图片 `add_image`：按请求时间换算后投放，通道按“无重叠”贪心分配；默认放右侧 20% 宽度，支持 intro/outro，图片尺寸等比例缩放到视频总面积的八分之一。
- 底部字幕：将 `segment.subtitleInfo` 合并偏移为全局 `SubtitleInfo`，复用 `SubtitleService` 构建 `add_text` 请求。
- 标题字幕：将 `segment.textInfo` 同样合并并由“标题文本服务”生成 `add_text`（固定顶部行；走 `SubtitleService` 的车道覆写）。
- **云渲染触发**：新增接口供客户端调用 `generate_video` 并返回 `cloudTaskId`；查询参考现有 `/api/capcut-script-driven/cloud-task/{cloudTaskId}` 在本控制器增加镜像接口。

## 接口变更

- 更新/实现 `[烧录服务/src/main/java/com/zhongjia/subtitlefusion/controller/VideoChainV2Controller.java]`：
- `POST /api/video-chain-v2/tasks`：提交异步任务（已有骨架，接入新异步服务）。
- `POST /api/video-chain-v2/cloud-render`：入参 `{ draftId, resolution?, framerate? }`，返回 `{ cloudTaskId }`。
- `GET  /api/video-chain-v2/cloud-task/{cloudTaskId}`：转发查询云渲染进度（便于客户端就近使用）。

## 新增/改动文件

- 新增 `[烧录服务/src/main/java/com/zhongjia/subtitlefusion/service/videochainv2/VideoChainV2AsyncService.java]`
- 负责任务进度与容错，串联：下载→段内拼接→上传→时间轴计算→CapCut草稿生成（视频/音频/图片/字幕）→完成（或提交云渲染不轮询）。
- 新增 `[烧录服务/src/main/java/com/zhongjia/subtitlefusion/service/videochainv2/VideoChainV2DraftWorkflowService.java]`
- 封装 CapCut API 调用与“全局时间轴偏移”逻辑；暴露 `generateDraft(VideoChainV2Request)`，返回 `{draftId, draftUrl, cloudTaskId?}`。
- 新增 `[烧录服务/src/main/java/com/zhongjia/subtitlefusion/service/videochainv2/SubtitleTimelineUtils.java]`
- 提供将分段字幕（底部/标题）按 `S[i] `偏移并合并为全局字幕列表的方法；校验并修剪重叠边界（避免 `end <= start`）。
- 新增 `[烧录服务/src/main/java/com/zhongjia/subtitlefusion/service/videochainv2/TitleTextService.java]`
- 生成“标题字幕”`add_text` 载荷（固定顶部行、可配置字体/颜色/入出场动画）。
- 复用
- `CapCutApiClient`（已具备 `create_draft/add_video/add_image/add_audio/generate_video`）。
- `SubtitleService`（用于底部字幕，车道规整与多策略文本效果）。
- `DistributedTaskManagementService` 任务状态。
- `FFmpegExecutor` + `FileDownloadService` + `MinioService`：拼接并上传拼接结果供 CapCut 访问。

## 校验与回退

- 缺失 `gapTransitions`：直接顺序拼接，无转场。
- `T[i] `过大（≥`D[i]`）：按 `min(T[i], max(D[i]-0.1, 0.5))` 下限/上限修正并记录告警。
- 远程素材不可达：任务失败并写明 URL。
- `capcut.license.key` 缺失：允许草稿模式；云渲染接口返回错误提示。

## 交付与验收

- 用例：
- 2 段，各含 3 个无声小视频；各段有 `audioUrl`；设置一个 0.5s 转场；底部+标题字幕与 2 张图片。
- 期望：草稿可预览；时间轴总时长 = `D1 + D2 - 0.5`；字幕与图片按预期对齐；云渲染接口返回 `cloudTaskId`。

## 备注

- 先按“段内本地拼接+CapCut段间转场”的最小闭环实现；如需改为“全程在CapCut内部拼装小视频”，可在下一步迭代将段内拼接改为多次 `add_video` 并在同一轨道顺序排列。