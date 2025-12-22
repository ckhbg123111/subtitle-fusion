# 漫剧合成需求

## 接口请求参数定义
WebtoonDramaGenerateRequest

## 关键帧
请抽取定义关键帧的DTO，设置为WebtoonDramaSegmentInfo 中的字段，另外需要预设3个关键帧定义，硬编码在代码中作为传空值时的兜底

## 提供接口
1. 漫剧草稿生成任务创建（包含云渲染发起）
2. 漫剧云渲染任务状态查询（考虑复用现有接口）

## 说明
WebtoonDramaGenerateRequest 中包含若干段
每个段包含一张图片与若干音频，段持续时长由音频总时长决定
每个音频有对应的字幕，及字幕时间轴（字幕时间轴需要计算偏移量后方可使用）

## 请求示例
```json
{
  "segment": [
    {
      "pictureUrl": "https://burnapi.aiqikang.com/nis-public/videos/021766122558707ff0e10ef3dc4fa8284497cad29aae403b297d0_0_20251219_053620.jpeg",
      "audioInfo": [
        {
          "audioUrl": "https://burnapi.aiqikang.com/nis-public/videos/audio_08c511a4-2121-4937-a8fc-8579c322863c_20251219_063026.mp3",
          "audioDuration": 5510,
          "commonSubtitleInfoList": [
            {
              "text": "这很可能是睡眠呼吸暂停综合征",
              "startTime": "00:00:00,000",
              "endTime": "00:00:02,740"
            },
            {
              "text": "需要做一次睡眠监测确诊",
              "startTime": "00:00:02,840",
              "endTime": "00:00:05,160"
            }
          ]
        },
        {
          "audioUrl": "https://burnapi.aiqikang.com/nis-public/videos/audio_908423f2-dd46-496e-8a27-91a3d5e33bc5_20251219_063034.mp3",
          "audioDuration": 3080,
          "commonSubtitleInfoList": [
            {
              "text": "啊",
              "startTime": "00:00:00,000",
              "endTime": "00:00:00,180"
            },
            {
              "text": "还要住院检查",
              "startTime": "00:00:00,180",
              "endTime": "00:00:01,520"
            },
            {
              "text": "会不会很麻烦",
              "startTime": "00:00:01,520",
              "endTime": "00:00:02,880"
            }
          ]
        }
      ]
    },
    {
      "pictureUrl": "https://burnapi.aiqikang.com/nis-public/videos/021766122558727b4352c6bd7f3c350e703a707efabdbce647930_0_20251219_053626.jpeg",
      "audioInfo": [
        {
          "audioUrl": "https://burnapi.aiqikang.com/nis-public/videos/audio_908423f2-dd46-496e-8a27-91a3d5e33bc5_20251219_063034.mp3",
          "audioDuration": 3080,
          "commonSubtitleInfoList": [
            {
              "text": "别担心",
              "startTime": "00:00:00,000",
              "endTime": "00:00:00,180"
            },
            {
              "text": "医生说这是无创的",
              "startTime": "00:00:00,180",
              "endTime": "00:00:01,520"
            },
            {
              "text": "戴着设备睡一觉就行",
              "startTime": "00:00:01,520",
              "endTime": "00:00:02,880"
            }
          ]
        }
      ]
    }
  ],
  "subtitleTemplate": "" 
}
```