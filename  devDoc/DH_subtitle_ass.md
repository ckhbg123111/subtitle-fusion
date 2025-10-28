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
