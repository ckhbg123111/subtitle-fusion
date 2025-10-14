# 视频烧录需求

## 需求描述
传入一个视频链请求VideoChainRequest
请求中包括多个视频段 segment
段中包含：视频、音频、字幕文件、插图信息、关键字（特效字幕）信息
其中:
    - 关键字与插图是在合适的时间轴添加滤镜展现在视频中的
    - 一个段中包含若干无声小视频，小视频总时长约等于音频时常
    - 每个段的小视频要先合成一个段视频，再去和字幕、音频、滤镜等信息一起烧录

要求，实现每个段的视频合成，最后将每个段的视频拼接为一个完整视频

---

## 名词解释与输入模型
- VideoChainRequest：一次完整的视频合成任务，请求体包含 `taskId` 与 `segmentList`。
- SegmentInfo：段内素材集合，包含：
  - videoInfos：若干无声小视频，顺序拼接后得到该段的视频底片（无声）。
  - audioUrl：该段的配音/背景音频，时长与段视频近似对齐。
  - srtFile：该段字幕（SRT），用于烧录到段视频中。
  - pictureInfos：插图叠加信息（含时间窗与位置）。
  - keywordsInfos：关键字高亮/特效信息（含时间窗与位置）。
- Position：插图/关键字文字在画面中的位置（Left/right，后续扩展 Top/Center/Bottom）。

与模型对应的Java定义参考 `com.zhongjia.subtitlefusion.model.VideoChainRequest`：
- `segmentList`：按顺序处理；每个 `SegmentInfo` 内部的 `videoInfos` 也按给定顺序拼接。
- `srtFile`：采用SRT格式（UTF-8优先）。

---

## 整体流程与目标产物
目标：对每个段完成「小视频拼接 → 叠加插图/关键字滤镜 → 字幕烧录 → 音视频合成」得到段成片；最后将所有段成片无缝拼接，生成单一成品视频，并上传对象存储（MinIO）。

高层步骤：
1. 校验与拉取素材（视频/音频/插图/字幕）
2. 段内小视频无声拼接（同参同编解码）
3. 段内滤镜与字幕合成（仅使用FFmpeg滤镜）
4. 段内音视频对齐与混流（以音频为基准，视频变速/冻结/裁剪）
5. 各段产物拼接（参数一致前提下 concat demuxer 无重编码）
6. 上传产物，清理中间文件，返回下载URL

---

## 技术方案设计（FFmpeg-only）

### A. 段内合成
- 小视频拼接（无声底片）：
  - 各小视频已保证分辨率/帧率/像素格式/编码器一致，可直接使用 concat demuxer 无重编码：
    - 生成 `list.txt`：
      - `file 'part1.mp4'`
      - `file 'part2.mp4'`
    - 命令：
      - `ffmpeg -f concat -safe 0 -i list.txt -c copy segment_{i}_nosound.mp4`

- 插图叠加（overlay）：
  - 对每个插图构造 `-i picture.png` 作为额外输入；按时间窗 `enable='between(t,ts1,ts2)'` 控制出现；按位置计算 `x/y`：
  - 示例（左侧 5% 边距，垂直居中）：
    - `overlay=x=W*0.05:y=(H-h)/2:enable='between(t,2,8)'`
  - 更多插图特效见下文“插图特效（FFmpeg）”。

- 关键字特效（drawtext）：
  - 使用 `drawtext` 渲染文字，支持字体/字号/描边/阴影/颜色，带时间窗 enable 控制：
  - 示例：
    - `drawtext=fontfile=/path/yahei.ttf:text='核心亮点':fontcolor=white:fontsize=h*0.04:shadowx=2:shadowy=2:shadowcolor=black@0.7:x=(W-tw)/2:y=H*0.85-th:enable='between(t,3,4.5)'`

- 字幕烧录（SRT → subtitles）：
  - 直接用 `subtitles` 滤镜：`-vf subtitles=srt_file.srt:force_style='FontName=Microsoft YaHei,FontSize=18,Outline=1,Shadow=1'`
  - 若需高级样式，建议将 SRT 转成 ASS 并使用 `ass` 滤镜，统一样式模板。

- 典型段内滤镜链路：
  - 将多个 overlay/drawtext/subtitles 通过复杂滤镜图 `-filter_complex` 串联，示例骨架：
  - `-i segment_{i}_nosound.mp4 -i pic1.png -i pic2.png -vf "[0:v][1:v]overlay=...:enable=...,[vtmp];[vtmp][2:v]overlay=...:enable=...,[v1];[v1]drawtext=...:enable=...,[v2];[v2]subtitles=srt_file.srt" -map "[v2]" -an segment_{i}_vf.mp4`

- 音视频对齐（以音频为基准调整视频）：
  - 变速视频：`-vf setpts=PTS/ratio`（ratio>1 变慢、ratio<1 变快），仅视频变速，音频保持不变；
  - 冻结补齐：在视频尾部使用 `tpad=stop_mode=clone:stop_duration=SECS`；
  - 裁剪视频：`-t DURATION` 或 `-vf trim`；
  - 最终混流：`-map [v] -map 1:a -c:v libx264 -c:a aac`（如需避免重编码视频，可先行确定是否必须变速/冻结/裁剪）。

- 段内输出：`segment_{index}.mp4`（带滤镜与字幕的有声成片）。

### B. 段间拼接（参数一致）
- 本次请求内各段视频与音轨参数一致：直接使用 concat demuxer：
  - 生成 `concat.txt`：
    - `file 'segment_1.mp4'`
    - `file 'segment_2.mp4'`
  - 命令：`ffmpeg -f concat -safe 0 -i concat.txt -c copy final_{taskId}.mp4`

### C. 关键实现要点
- 时间窗：统一为秒（小数）或 `hh:mm:ss.mmm` 字符串，并转换为 `enable='between(t,ts1,ts2)'`。
- 位置：Left/right 通过 `x` 为 `W*0.05` 与 `W-tw-W*0.05` 等表达；可扩展 top/center/bottom。
- 字体：服务器需部署中文字体文件并以绝对路径提供给 `drawtext/subtitles`。
- 性能：合并滤镜链，避免多次编码；优先 concat 复制；必要时统一一次编码。

---

## 与服务的衔接（FFmpeg-only）
- 下载：沿用 `FileDownloadService`；
- 渲染：由统一的命令构建器/执行器封装 FFmpeg 命令（替代 Java2D/JavaCV 逐帧方案）；
- 进度：解析 FFmpeg 日志（`-progress pipe:1` 或 stderr 输出）推送 `DistributedTaskManagementService`；
- 上传：沿用 `MinioService`；
- 任务流：`下载 → 段内concat → filter_complex 渲染(overlay/drawtext/subtitles) + A/V对齐 → 混流 → 段间concat → 上传`。

---

## 接口约定与示例
- 创建任务：`POST /api/video-chain/tasks`
  - 入参：`VideoChainRequest`（JSON 或 multipart/form-data，若包含 srt 文件可选走表单）
  - 返回：`{ taskId }`
- 查询任务状态：`GET /api/video-chain/tasks/{taskId}`
  - 返回：任务状态与结果；成功时包含最终视频URL

创建任务请求示例（JSON 简化示例）：
```json
{
  "taskId": "task-20241014-0001",
  "segmentList": [
    {
      "videoInfos": [
        { "videoUrl": "https://host/v/s1_a.mp4" },
        { "videoUrl": "https://host/v/s1_b.mp4" }
      ],
      "audioUrl": "https://host/a/s1.m4a",
      "pictureInfos": [
        { "pictureUrl": "https://host/p/logo.png", "startTime": "00:00:02.000", "endTime": "00:00:08.000", "position": "Left" }
      ],
      "keywordsInfos": [
        { "keyword": "核心亮点", "startTime": "00:00:03.000", "endTime": "00:00:04.500", "position": "right" }
      ]
    }
  ]
}
```

创建任务响应示例：
```json
{ "taskId": "task-20241014-0001" }
```

查询任务状态响应示例（进行中）：
```json
{
  "taskId": "task-20241014-0001",
  "state": "PROCESSING",
  "progress": 72,
  "message": "渲染字幕与插图特效中"
}
```

查询任务状态响应示例（成功）：
```json
{
  "taskId": "task-20241014-0001",
  "state": "COMPLETED",
  "progress": 100,
  "resultUrl": "https://minio.example.com/bucket/final_task-20241014-0001.mp4"
}
```

查询任务状态响应示例（失败）：
```json
{
  "taskId": "task-20241014-0001",
  "state": "FAILED",
  "progress": 60,
  "error": "拼接失败：concat 列表不满足一致性"
}
```

- 说明：
  - `state` 取值参考 `TaskState`：`QUEUED`/`DOWNLOADING`/`PROCESSING`/`UPLOADING`/`COMPLETED`/`FAILED`。
  - `progress` 为0–100的整数，关键阶段应更新。
  - `resultUrl` 仅在 `COMPLETED` 时返回。
  - `error` 仅在 `FAILED` 时返回。

---

## 部署与依赖
- 必须安装 FFmpeg 可执行文件（具备 `overlay`、`drawtext`、`subtitles`、`ass`、`concat` 等滤镜/功能）。
- 部署中文字体文件（如 `Microsoft YaHei` 或 `Noto Sans CJK`）并确保 FFmpeg 可访问。
- 服务器磁盘：充足的本地临时目录；I/O 顺序写优先。

---

## 验收标准
- 插图/关键字/字幕均按时间窗正确渲染，位置与样式符合预期。
- 段内/段间拼接无黑帧、无跳帧；最终视频参数与输入一致或按配置统一。
- 性能满足规模期望，在多段长视频下仍能稳定产出。

### 插图特效（FFmpeg）
- 目标：在插图叠加基础上，支持淡入淡出、溶解、滑入/擦除等出现/消失动画。
- 基本思路：为插图输入建立独立的滤镜分支，对该分支先应用透明度或几何变化，再送入 overlay 与主画面合成。

- 淡入/淡出（alpha 渐变）：
  - 使用 `format=rgba,geq` 或 `fade` 滤镜处理 alpha；推荐 `fade`：
  - 插图输入索引假设为 `[pic1]`，其时间窗为 [ts1, ts2]，淡入 0.5s，淡出 0.5s：
    - `[{pic1}]format=rgba,fade=t=in:st=ts1:d=0.5:alpha=1,fade=t=out:st=ts2-0.5:d=0.5:alpha=1[pic1_faded]`
  - 随后 `overlay`：`[base][pic1_faded]overlay=x=...:y=...:enable='between(t,ts1,ts2)'[v1]`

- 溶解（cross dissolve 与主画面融合）：
  - 利用 `blend` 基于时间控制混合系数，与 overlay 结合：
  - 方法1：将插图先与一张透明画布 `nullsrc,format=rgba` 做时间混合得到溶解效果，再 overlay：
    - `nullsrc=size=WxH:duration=...:rate=... , format=rgba [blank];`
    - `[blank][pic1]blend=all_mode=normal:all_opacity='between(t,ts1,ts2)?( (t-ts1)/(0.5) ):0' [pic1_dissolve]`
  - 方法2：使用 `fade` alpha 也可达到近似溶解。

- 滑入/擦除（位移或遮罩）：
  - 位移滑入：对插图画面用 `scale,format,setsar` 后，通过 `overlay` 的 `x/y` 表达式随时间变化：
    - 例如自左向右滑入 0.6s：`x='between(t,ts1,ts1+0.6)? (W*-1 + (t-ts1)/0.6*(W*0.05)) : W*0.05'`
  - 擦除/百叶窗：使用遮罩 `alphamerge` 或 `lut` 生成动态 alpha；简化实现可用 `wipe` 类似效果（需自制遮罩序列或用 `geq` 动态生成）：
    - `geq=a='if(between(T,ts1,ts1+0.6), 255*(T-ts1)/0.6, 255)' : r=R : g=G : b=B`
    - 将动态 alpha 与插图 `alphamerge` 后再 overlay。

- 组合与多插图：将每个插图处理分支命名，如 `[pic1_faded]`、`[pic2_slide]`，逐次 overlay 到主链路 `[base]` 上，产出 `[v]`。

- 性能建议：
  - 尽量在单条 `-filter_complex` 中完成所有特效与字幕，避免多次编码；
  - 控制图片分辨率，按目标显示尺寸预缩放；
  - 统一时间表达，避免复杂表达式重复计算。


