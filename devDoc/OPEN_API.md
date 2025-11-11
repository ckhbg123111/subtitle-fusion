---
title: 默认模块
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
code_clipboard: true
highlight_theme: darkula
headingLevel: 2
generator: "@tarslib/widdershins v4.0.30"

---

# 默认模块

Base URLs:

* <a href="https://open.capcutapi.top/cut_jianying">正式环境: https://open.capcutapi.top/cut_jianying</a>

# Authentication

* API Key (myfirstauth)
    - Parameter Name: **Authorization**, in: header. 

# Default

## POST create_draft

POST /create_draft

创建一个草稿

> Body 请求参数

```json
"{\n    \"width\": 1080,  // 视频宽度（选填，默认 1080 ）\n    \"height\": 1920  // 视频高度（选填，默认 1920 ）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» width|body|integer| 否 |视频宽度（选填，默认 1080 ）|
|» height|body|integer| 否 |视频高度（选填，默认 1920 ）|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752305579_38e2e11b",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752305579_38e2e11b"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||草稿id，可以进一步编辑|
|»» draft_url|string|true|none||草稿预览页面|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST save_draft

POST /save_draft

生成草稿的下载链接

> Body 请求参数

```json
{
  "draft_id": "dfd_cat_1753709045_3a033ea7",
  "draft_folder": "/Users/sunguannan/Movies/JianyingPro/User Data/Projects/com.lveditor.draft",
  "is_capcut": 0
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» draft_id|body|string| 是 |草稿id|
|» draft_folder|body|string| 是 |剪映草稿路径|
|» is_capcut|body|integer| 是 |是否是CapCut，0表示剪映，1表示CapCut|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_url": "https://oss-oversea-bucket.oss-cn-hongkong.aliyuncs.com/dfd_cat_1752309810_0b941b76.zip?OSSAccessKeyId=LTAI5t6GK97EdxsFqDT25U2j&Expires=1752396279&Signature=AH2nwv0q7IhHlT1kYRtOTiLJkyk%3D",
    "success": true
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_url|string|true|none||下载链接|
|»» success|boolean|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST get_duration

POST /get_duration

> Body 请求参数

```json
{
  "url": "https://lf3-lv-music-tos.faceu.com/obj/tos-cn-ve-2774/oYACBQRCMlWBIrZipvQZhI5LAlUFYii0RwEPh"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» url|body|string| 是 |链接，例如https://xxx.mp3|

> 返回示例

> 200 Response

```json
{
  "error": "string",
  "output": {
    "duration": 0,
    "video_url": "string"
  },
  "purchase_link": "string",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» duration|number|true|none||none|
|»» video_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 文本

## GET get_text_intro_types

GET /get_text_intro_types

获取可用的文本入场动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "冲屏位移"
    },
    {
      "name": "卡拉OK"
    },
    {
      "name": "变色输入"
    },
    {
      "name": "右上弹入"
    },
    {
      "name": "右下擦开"
    },
    {
      "name": "向上擦除"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向上翻转"
    },
    {
      "name": "向上重叠"
    },
    {
      "name": "向上露出"
    },
    {
      "name": "向下擦除"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向下露出"
    },
    {
      "name": "向下飞入"
    },
    {
      "name": "向右擦除"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向右缓入"
    },
    {
      "name": "向右集合"
    },
    {
      "name": "向右露出"
    },
    {
      "name": "向左擦除"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向左露出"
    },
    {
      "name": "圆形扫描"
    },
    {
      "name": "复古打字机"
    },
    {
      "name": "居中打字"
    },
    {
      "name": "左上弹入"
    },
    {
      "name": "左移弹动"
    },
    {
      "name": "开幕"
    },
    {
      "name": "弹入"
    },
    {
      "name": "弹弓"
    },
    {
      "name": "弹性伸缩"
    },
    {
      "name": "弹簧"
    },
    {
      "name": "彩色映射"
    },
    {
      "name": "打字机_I"
    },
    {
      "name": "打字机_II"
    },
    {
      "name": "打字机_III"
    },
    {
      "name": "打字机IV"
    },
    {
      "name": "扭曲模糊"
    },
    {
      "name": "拖尾"
    },
    {
      "name": "收拢"
    },
    {
      "name": "放大"
    },
    {
      "name": "故障打字机"
    },
    {
      "name": "旋入"
    },
    {
      "name": "日出"
    },
    {
      "name": "晕开"
    },
    {
      "name": "模糊"
    },
    {
      "name": "水墨晕开"
    },
    {
      "name": "水平翻转"
    },
    {
      "name": "波浪弹入"
    },
    {
      "name": "渐显"
    },
    {
      "name": "溶解"
    },
    {
      "name": "滑动上升"
    },
    {
      "name": "生长"
    },
    {
      "name": "甩出"
    },
    {
      "name": "站起"
    },
    {
      "name": "缩小"
    },
    {
      "name": "缩小_II"
    },
    {
      "name": "羽化向右擦开"
    },
    {
      "name": "羽化向左擦开"
    },
    {
      "name": "翻动"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "逐字旋转"
    },
    {
      "name": "逐字显影"
    },
    {
      "name": "逐字翻转"
    },
    {
      "name": "闪动"
    },
    {
      "name": "随机弹跳"
    },
    {
      "name": "随机飞入"
    },
    {
      "name": "乱码故障"
    },
    {
      "name": "二段缩放"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "倒数"
    },
    {
      "name": "兔子弹跳"
    },
    {
      "name": "冰雪飘动"
    },
    {
      "name": "发光闪入"
    },
    {
      "name": "叠影并入"
    },
    {
      "name": "向上弹入"
    },
    {
      "name": "向下溶解"
    },
    {
      "name": "向右模糊_II"
    },
    {
      "name": "向左模糊"
    },
    {
      "name": "吸入"
    },
    {
      "name": "呐喊声波"
    },
    {
      "name": "喷绘"
    },
    {
      "name": "圆柱体滚动"
    },
    {
      "name": "圣诞帽弹跳"
    },
    {
      "name": "圣诞树弹跳II"
    },
    {
      "name": "弹入跳动"
    },
    {
      "name": "弹性伸缩_II"
    },
    {
      "name": "心动瞬间"
    },
    {
      "name": "慢速放大"
    },
    {
      "name": "打字光标"
    },
    {
      "name": "抖动甩入"
    },
    {
      "name": "折叠"
    },
    {
      "name": "描边填充"
    },
    {
      "name": "放大震动"
    },
    {
      "name": "故障闪动"
    },
    {
      "name": "新年打字机"
    },
    {
      "name": "旋转缩放"
    },
    {
      "name": "旋转飞入"
    },
    {
      "name": "星光闪闪"
    },
    {
      "name": "星光闪闪_II"
    },
    {
      "name": "星星弹跳"
    },
    {
      "name": "模糊发光"
    },
    {
      "name": "模糊滚动"
    },
    {
      "name": "模糊缩小"
    },
    {
      "name": "汇聚"
    },
    {
      "name": "波浪弹跳"
    },
    {
      "name": "流光扩散"
    },
    {
      "name": "滚入"
    },
    {
      "name": "激光雕刻"
    },
    {
      "name": "爱心弹跳"
    },
    {
      "name": "玩雪"
    },
    {
      "name": "环绕滑入"
    },
    {
      "name": "生长_II"
    },
    {
      "name": "电光"
    },
    {
      "name": "电光_II"
    },
    {
      "name": "碰碰车"
    },
    {
      "name": "空翻"
    },
    {
      "name": "缤纷冲屏"
    },
    {
      "name": "缩放_III"
    },
    {
      "name": "翻页II"
    },
    {
      "name": "背景滑入"
    },
    {
      "name": "色散拖影"
    },
    {
      "name": "螺旋上升"
    },
    {
      "name": "跃进"
    },
    {
      "name": "跳跳捣蛋鬼"
    },
    {
      "name": "跳跳糖"
    },
    {
      "name": "辉光"
    },
    {
      "name": "辉光扫描"
    },
    {
      "name": "逐字弹跳"
    },
    {
      "name": "逐字旋入"
    },
    {
      "name": "金粉飘落"
    },
    {
      "name": "镂空跳入"
    },
    {
      "name": "闪烁集合"
    },
    {
      "name": "随机上升"
    },
    {
      "name": "随机弹跳_II"
    },
    {
      "name": "随机打字机"
    },
    {
      "name": "随机落下"
    },
    {
      "name": "随机集合"
    },
    {
      "name": "雪光模糊"
    },
    {
      "name": "音符弹跳"
    },
    {
      "name": "顶出"
    },
    {
      "name": "预览打字"
    },
    {
      "name": "飞入"
    },
    {
      "name": "鼠标点击"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_text_outro_types

GET /get_text_outro_types

获取可用的文本出场动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "冲屏位移"
    },
    {
      "name": "卡拉OK"
    },
    {
      "name": "变色输入"
    },
    {
      "name": "右上弹入"
    },
    {
      "name": "右下擦开"
    },
    {
      "name": "向上擦除"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向上翻转"
    },
    {
      "name": "向上重叠"
    },
    {
      "name": "向上露出"
    },
    {
      "name": "向下擦除"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向下露出"
    },
    {
      "name": "向下飞入"
    },
    {
      "name": "向右擦除"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向右缓入"
    },
    {
      "name": "向右集合"
    },
    {
      "name": "向右露出"
    },
    {
      "name": "向左擦除"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向左露出"
    },
    {
      "name": "圆形扫描"
    },
    {
      "name": "复古打字机"
    },
    {
      "name": "居中打字"
    },
    {
      "name": "左上弹入"
    },
    {
      "name": "左移弹动"
    },
    {
      "name": "开幕"
    },
    {
      "name": "弹入"
    },
    {
      "name": "弹弓"
    },
    {
      "name": "弹性伸缩"
    },
    {
      "name": "弹簧"
    },
    {
      "name": "彩色映射"
    },
    {
      "name": "打字机_I"
    },
    {
      "name": "打字机_II"
    },
    {
      "name": "打字机_III"
    },
    {
      "name": "打字机IV"
    },
    {
      "name": "扭曲模糊"
    },
    {
      "name": "拖尾"
    },
    {
      "name": "收拢"
    },
    {
      "name": "放大"
    },
    {
      "name": "故障打字机"
    },
    {
      "name": "旋入"
    },
    {
      "name": "日出"
    },
    {
      "name": "晕开"
    },
    {
      "name": "模糊"
    },
    {
      "name": "水墨晕开"
    },
    {
      "name": "水平翻转"
    },
    {
      "name": "波浪弹入"
    },
    {
      "name": "渐显"
    },
    {
      "name": "溶解"
    },
    {
      "name": "滑动上升"
    },
    {
      "name": "生长"
    },
    {
      "name": "甩出"
    },
    {
      "name": "站起"
    },
    {
      "name": "缩小"
    },
    {
      "name": "缩小_II"
    },
    {
      "name": "羽化向右擦开"
    },
    {
      "name": "羽化向左擦开"
    },
    {
      "name": "翻动"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "逐字旋转"
    },
    {
      "name": "逐字显影"
    },
    {
      "name": "逐字翻转"
    },
    {
      "name": "闪动"
    },
    {
      "name": "随机弹跳"
    },
    {
      "name": "随机飞入"
    },
    {
      "name": "乱码故障"
    },
    {
      "name": "二段缩放"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "倒数"
    },
    {
      "name": "兔子弹跳"
    },
    {
      "name": "冰雪飘动"
    },
    {
      "name": "发光闪入"
    },
    {
      "name": "叠影并入"
    },
    {
      "name": "向上弹入"
    },
    {
      "name": "向下溶解"
    },
    {
      "name": "向右模糊_II"
    },
    {
      "name": "向左模糊"
    },
    {
      "name": "吸入"
    },
    {
      "name": "呐喊声波"
    },
    {
      "name": "喷绘"
    },
    {
      "name": "圆柱体滚动"
    },
    {
      "name": "圣诞帽弹跳"
    },
    {
      "name": "圣诞树弹跳II"
    },
    {
      "name": "弹入跳动"
    },
    {
      "name": "弹性伸缩_II"
    },
    {
      "name": "心动瞬间"
    },
    {
      "name": "慢速放大"
    },
    {
      "name": "打字光标"
    },
    {
      "name": "抖动甩入"
    },
    {
      "name": "折叠"
    },
    {
      "name": "描边填充"
    },
    {
      "name": "放大震动"
    },
    {
      "name": "故障闪动"
    },
    {
      "name": "新年打字机"
    },
    {
      "name": "旋转缩放"
    },
    {
      "name": "旋转飞入"
    },
    {
      "name": "星光闪闪"
    },
    {
      "name": "星光闪闪_II"
    },
    {
      "name": "星星弹跳"
    },
    {
      "name": "模糊发光"
    },
    {
      "name": "模糊滚动"
    },
    {
      "name": "模糊缩小"
    },
    {
      "name": "汇聚"
    },
    {
      "name": "波浪弹跳"
    },
    {
      "name": "流光扩散"
    },
    {
      "name": "滚入"
    },
    {
      "name": "激光雕刻"
    },
    {
      "name": "爱心弹跳"
    },
    {
      "name": "玩雪"
    },
    {
      "name": "环绕滑入"
    },
    {
      "name": "生长_II"
    },
    {
      "name": "电光"
    },
    {
      "name": "电光_II"
    },
    {
      "name": "碰碰车"
    },
    {
      "name": "空翻"
    },
    {
      "name": "缤纷冲屏"
    },
    {
      "name": "缩放_III"
    },
    {
      "name": "翻页II"
    },
    {
      "name": "背景滑入"
    },
    {
      "name": "色散拖影"
    },
    {
      "name": "螺旋上升"
    },
    {
      "name": "跃进"
    },
    {
      "name": "跳跳捣蛋鬼"
    },
    {
      "name": "跳跳糖"
    },
    {
      "name": "辉光"
    },
    {
      "name": "辉光扫描"
    },
    {
      "name": "逐字弹跳"
    },
    {
      "name": "逐字旋入"
    },
    {
      "name": "金粉飘落"
    },
    {
      "name": "镂空跳入"
    },
    {
      "name": "闪烁集合"
    },
    {
      "name": "随机上升"
    },
    {
      "name": "随机弹跳_II"
    },
    {
      "name": "随机打字机"
    },
    {
      "name": "随机落下"
    },
    {
      "name": "随机集合"
    },
    {
      "name": "雪光模糊"
    },
    {
      "name": "音符弹跳"
    },
    {
      "name": "顶出"
    },
    {
      "name": "预览打字"
    },
    {
      "name": "飞入"
    },
    {
      "name": "鼠标点击"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_text_loop_anim_types

GET /get_text_loop_anim_types

获取可用的文本循环动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "冲屏位移"
    },
    {
      "name": "卡拉OK"
    },
    {
      "name": "变色输入"
    },
    {
      "name": "右上弹入"
    },
    {
      "name": "右下擦开"
    },
    {
      "name": "向上擦除"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向上翻转"
    },
    {
      "name": "向上重叠"
    },
    {
      "name": "向上露出"
    },
    {
      "name": "向下擦除"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向下露出"
    },
    {
      "name": "向下飞入"
    },
    {
      "name": "向右擦除"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向右缓入"
    },
    {
      "name": "向右集合"
    },
    {
      "name": "向右露出"
    },
    {
      "name": "向左擦除"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向左露出"
    },
    {
      "name": "圆形扫描"
    },
    {
      "name": "复古打字机"
    },
    {
      "name": "居中打字"
    },
    {
      "name": "左上弹入"
    },
    {
      "name": "左移弹动"
    },
    {
      "name": "开幕"
    },
    {
      "name": "弹入"
    },
    {
      "name": "弹弓"
    },
    {
      "name": "弹性伸缩"
    },
    {
      "name": "弹簧"
    },
    {
      "name": "彩色映射"
    },
    {
      "name": "打字机_I"
    },
    {
      "name": "打字机_II"
    },
    {
      "name": "打字机_III"
    },
    {
      "name": "打字机IV"
    },
    {
      "name": "扭曲模糊"
    },
    {
      "name": "拖尾"
    },
    {
      "name": "收拢"
    },
    {
      "name": "放大"
    },
    {
      "name": "故障打字机"
    },
    {
      "name": "旋入"
    },
    {
      "name": "日出"
    },
    {
      "name": "晕开"
    },
    {
      "name": "模糊"
    },
    {
      "name": "水墨晕开"
    },
    {
      "name": "水平翻转"
    },
    {
      "name": "波浪弹入"
    },
    {
      "name": "渐显"
    },
    {
      "name": "溶解"
    },
    {
      "name": "滑动上升"
    },
    {
      "name": "生长"
    },
    {
      "name": "甩出"
    },
    {
      "name": "站起"
    },
    {
      "name": "缩小"
    },
    {
      "name": "缩小_II"
    },
    {
      "name": "羽化向右擦开"
    },
    {
      "name": "羽化向左擦开"
    },
    {
      "name": "翻动"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "逐字旋转"
    },
    {
      "name": "逐字显影"
    },
    {
      "name": "逐字翻转"
    },
    {
      "name": "闪动"
    },
    {
      "name": "随机弹跳"
    },
    {
      "name": "随机飞入"
    },
    {
      "name": "乱码故障"
    },
    {
      "name": "二段缩放"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "倒数"
    },
    {
      "name": "兔子弹跳"
    },
    {
      "name": "冰雪飘动"
    },
    {
      "name": "发光闪入"
    },
    {
      "name": "叠影并入"
    },
    {
      "name": "向上弹入"
    },
    {
      "name": "向下溶解"
    },
    {
      "name": "向右模糊_II"
    },
    {
      "name": "向左模糊"
    },
    {
      "name": "吸入"
    },
    {
      "name": "呐喊声波"
    },
    {
      "name": "喷绘"
    },
    {
      "name": "圆柱体滚动"
    },
    {
      "name": "圣诞帽弹跳"
    },
    {
      "name": "圣诞树弹跳II"
    },
    {
      "name": "弹入跳动"
    },
    {
      "name": "弹性伸缩_II"
    },
    {
      "name": "心动瞬间"
    },
    {
      "name": "慢速放大"
    },
    {
      "name": "打字光标"
    },
    {
      "name": "抖动甩入"
    },
    {
      "name": "折叠"
    },
    {
      "name": "描边填充"
    },
    {
      "name": "放大震动"
    },
    {
      "name": "故障闪动"
    },
    {
      "name": "新年打字机"
    },
    {
      "name": "旋转缩放"
    },
    {
      "name": "旋转飞入"
    },
    {
      "name": "星光闪闪"
    },
    {
      "name": "星光闪闪_II"
    },
    {
      "name": "星星弹跳"
    },
    {
      "name": "模糊发光"
    },
    {
      "name": "模糊滚动"
    },
    {
      "name": "模糊缩小"
    },
    {
      "name": "汇聚"
    },
    {
      "name": "波浪弹跳"
    },
    {
      "name": "流光扩散"
    },
    {
      "name": "滚入"
    },
    {
      "name": "激光雕刻"
    },
    {
      "name": "爱心弹跳"
    },
    {
      "name": "玩雪"
    },
    {
      "name": "环绕滑入"
    },
    {
      "name": "生长_II"
    },
    {
      "name": "电光"
    },
    {
      "name": "电光_II"
    },
    {
      "name": "碰碰车"
    },
    {
      "name": "空翻"
    },
    {
      "name": "缤纷冲屏"
    },
    {
      "name": "缩放_III"
    },
    {
      "name": "翻页II"
    },
    {
      "name": "背景滑入"
    },
    {
      "name": "色散拖影"
    },
    {
      "name": "螺旋上升"
    },
    {
      "name": "跃进"
    },
    {
      "name": "跳跳捣蛋鬼"
    },
    {
      "name": "跳跳糖"
    },
    {
      "name": "辉光"
    },
    {
      "name": "辉光扫描"
    },
    {
      "name": "逐字弹跳"
    },
    {
      "name": "逐字旋入"
    },
    {
      "name": "金粉飘落"
    },
    {
      "name": "镂空跳入"
    },
    {
      "name": "闪烁集合"
    },
    {
      "name": "随机上升"
    },
    {
      "name": "随机弹跳_II"
    },
    {
      "name": "随机打字机"
    },
    {
      "name": "随机落下"
    },
    {
      "name": "随机集合"
    },
    {
      "name": "雪光模糊"
    },
    {
      "name": "音符弹跳"
    },
    {
      "name": "顶出"
    },
    {
      "name": "预览打字"
    },
    {
      "name": "飞入"
    },
    {
      "name": "鼠标点击"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_font_types

GET /get_font_types

获取可用字体

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "CC_Captial"
    },
    {
      "name": "CC_Moderno"
    },
    {
      "name": "JYruantang"
    },
    {
      "name": "JYshiduo"
    },
    {
      "name": "JYzhuqingting"
    },
    {
      "name": "Merry_Christmas"
    },
    {
      "name": "MyFont凌渡哥哥简"
    },
    {
      "name": "ZY_Balloonbillow"
    },
    {
      "name": "ZY_Blossom"
    },
    {
      "name": "ZY_Brief"
    },
    {
      "name": "ZY_Courage"
    },
    {
      "name": "ZY_Daisy"
    },
    {
      "name": "ZY_Elixir"
    },
    {
      "name": "ZY_Fabulous"
    },
    {
      "name": "ZY_Fantasy"
    },
    {
      "name": "ZY_Flourishing_Italic"
    },
    {
      "name": "ZY_Fortitude"
    },
    {
      "name": "ZY_Kindly_Breeze"
    },
    {
      "name": "ZY_Loyalty"
    },
    {
      "name": "ZY_Modern"
    },
    {
      "name": "ZY_Multiplicity"
    },
    {
      "name": "ZY_Panacea"
    },
    {
      "name": "ZY_Relax"
    },
    {
      "name": "ZY_Slender"
    },
    {
      "name": "ZY_Spunk"
    },
    {
      "name": "ZY_Squiggle"
    },
    {
      "name": "ZY_Starry"
    },
    {
      "name": "ZY_Timing"
    },
    {
      "name": "ZY_Trend"
    },
    {
      "name": "ZYLAA_Demure"
    },
    {
      "name": "Amigate"
    },
    {
      "name": "Anson"
    },
    {
      "name": "BlackMango_Black"
    },
    {
      "name": "BlackMango_Regular"
    },
    {
      "name": "Bungee_Regular"
    },
    {
      "name": "Cabin_Rg"
    },
    {
      "name": "Caveat_Regular"
    },
    {
      "name": "Climate"
    },
    {
      "name": "Coiny_Regular"
    },
    {
      "name": "DMSans_BoldItalic"
    },
    {
      "name": "Exo"
    },
    {
      "name": "Gallery"
    },
    {
      "name": "Giveny"
    },
    {
      "name": "Grandstander_Regular"
    },
    {
      "name": "Gratefulness"
    },
    {
      "name": "HarmonyOS_Sans_SC_Bold"
    },
    {
      "name": "HarmonyOS_Sans_SC_Medium"
    },
    {
      "name": "HarmonyOS_Sans_SC_Regular"
    },
    {
      "name": "HarmonyOS_Sans_TC_Bold"
    },
    {
      "name": "HarmonyOS_Sans_TC_Light"
    },
    {
      "name": "HarmonyOS_Sans_TC_Medium"
    },
    {
      "name": "HarmonyOS_Sans_TC_Regular"
    },
    {
      "name": "HeptaSlab_ExtraBold"
    },
    {
      "name": "HeptaSlab_Light"
    },
    {
      "name": "Huben"
    },
    {
      "name": "Ingram"
    },
    {
      "name": "Integrity"
    },
    {
      "name": "Inter_Black"
    },
    {
      "name": "Kanit_Black"
    },
    {
      "name": "Kanit_Regular"
    },
    {
      "name": "Koulen_Regular"
    },
    {
      "name": "LXGWWenKai_Bold"
    },
    {
      "name": "LXGWWenKai_Light"
    },
    {
      "name": "LXGWWenKai_Regular"
    },
    {
      "name": "Love"
    },
    {
      "name": "Luxury"
    },
    {
      "name": "MiSans_Heavy"
    },
    {
      "name": "MiSans_Regular"
    },
    {
      "name": "Modern"
    },
    {
      "name": "Nunito"
    },
    {
      "name": "OldStandardTT_Regular"
    },
    {
      "name": "Pacifico_Regular"
    },
    {
      "name": "PlayfairDisplay_Bold"
    },
    {
      "name": "Plunct"
    },
    {
      "name": "Polly"
    },
    {
      "name": "Poppins_Bold"
    },
    {
      "name": "Poppins_Regular"
    },
    {
      "name": "RedHatDisplay_BoldItalic"
    },
    {
      "name": "RedHatDisplay_Light"
    },
    {
      "name": "ResourceHanRoundedCN_Md"
    },
    {
      "name": "ResourceHanRoundedCN_Nl"
    },
    {
      "name": "Roboto_BlkCn"
    },
    {
      "name": "SansitaSwashed_Regular"
    },
    {
      "name": "SecularOne_Regular"
    },
    {
      "name": "Signature"
    },
    {
      "name": "Soap"
    },
    {
      "name": "Sora_Bold"
    },
    {
      "name": "Sora_Regular"
    },
    {
      "name": "SourceHanSansCN_Bold"
    },
    {
      "name": "SourceHanSansCN_Light"
    },
    {
      "name": "SourceHanSansCN_Medium"
    },
    {
      "name": "SourceHanSansCN_Normal"
    },
    {
      "name": "SourceHanSansCN_Regular"
    },
    {
      "name": "SourceHanSansTW_Bold"
    },
    {
      "name": "SourceHanSansTW_Light"
    },
    {
      "name": "SourceHanSansTW_Medium"
    },
    {
      "name": "SourceHanSansTW_Normal"
    },
    {
      "name": "SourceHanSansTW_Regular"
    },
    {
      "name": "SourceHanSerifCN_Light"
    },
    {
      "name": "SourceHanSerifCN_Medium"
    },
    {
      "name": "SourceHanSerifCN_Regular"
    },
    {
      "name": "SourceHanSerifCN_SemiBold"
    },
    {
      "name": "SourceHanSerifTW_Bold"
    },
    {
      "name": "SourceHanSerifTW_Light"
    },
    {
      "name": "SourceHanSerifTW_Medium"
    },
    {
      "name": "SourceHanSerifTW_Regular"
    },
    {
      "name": "SourceHanSerifTW_SemiBold"
    },
    {
      "name": "Staatliches_Regular"
    },
    {
      "name": "Sunset"
    },
    {
      "name": "Thrive"
    },
    {
      "name": "Thunder"
    },
    {
      "name": "Tronica"
    },
    {
      "name": "Vintage"
    },
    {
      "name": "ZY_Dexterous"
    },
    {
      "name": "ZY_Earnest"
    },
    {
      "name": "ZY_Vigorous"
    },
    {
      "name": "ZY_Vigorous_Medium"
    },
    {
      "name": "ZYLantastic"
    },
    {
      "name": "ZYLullaby"
    },
    {
      "name": "ZYSilhouette"
    },
    {
      "name": "ZYWitty"
    },
    {
      "name": "Zapfino"
    },
    {
      "name": "中秀体"
    },
    {
      "name": "今宋体"
    },
    {
      "name": "仓耳周珂正大榜书"
    },
    {
      "name": "优设标题黑"
    },
    {
      "name": "俊雅体"
    },
    {
      "name": "元气泡泡体"
    },
    {
      "name": "元瑶体"
    },
    {
      "name": "先锋体"
    },
    {
      "name": "兰亭圆"
    },
    {
      "name": "凌东齐伋体_combo"
    },
    {
      "name": "凌东齐伋体_fallback"
    },
    {
      "name": "匹喏曹"
    },
    {
      "name": "半梦体"
    },
    {
      "name": "卡酷体"
    },
    {
      "name": "古典体"
    },
    {
      "name": "古印宋简"
    },
    {
      "name": "古雅体"
    },
    {
      "name": "古风小楷"
    },
    {
      "name": "台北黑体_Light"
    },
    {
      "name": "台北黑体_Regular"
    },
    {
      "name": "后现代体"
    },
    {
      "name": "喜悦体"
    },
    {
      "name": "嘉木体"
    },
    {
      "name": "圆体"
    },
    {
      "name": "基础像素"
    },
    {
      "name": "墩墩体"
    },
    {
      "name": "大字报"
    },
    {
      "name": "大梁体"
    },
    {
      "name": "妙黑体"
    },
    {
      "name": "字制区喜脉体"
    },
    {
      "name": "孤月体"
    },
    {
      "name": "宋体"
    },
    {
      "name": "小薇体"
    },
    {
      "name": "尔雅新大黑"
    },
    {
      "name": "峰骨体"
    },
    {
      "name": "幼萱体"
    },
    {
      "name": "得意黑"
    },
    {
      "name": "快乐体"
    },
    {
      "name": "快速体"
    },
    {
      "name": "思源中宋"
    },
    {
      "name": "思源粗宋"
    },
    {
      "name": "悠悠然"
    },
    {
      "name": "悦妍体"
    },
    {
      "name": "惊鸿体"
    },
    {
      "name": "抖音美好体"
    },
    {
      "name": "招牌体"
    },
    {
      "name": "挥墨体"
    },
    {
      "name": "文研体"
    },
    {
      "name": "文艺繁体"
    },
    {
      "name": "文轩体"
    },
    {
      "name": "文雅体"
    },
    {
      "name": "新青年体"
    },
    {
      "name": "方糖体"
    },
    {
      "name": "无界黑"
    },
    {
      "name": "日式标题"
    },
    {
      "name": "星光体"
    },
    {
      "name": "有猫在"
    },
    {
      "name": "李李体"
    },
    {
      "name": "极简拼音"
    },
    {
      "name": "梅雨煎茶"
    },
    {
      "name": "梦桃体"
    },
    {
      "name": "楚辰体"
    },
    {
      "name": "欣然体"
    },
    {
      "name": "毡笔体"
    },
    {
      "name": "汇文明朝体"
    },
    {
      "name": "汉仪英雄体"
    },
    {
      "name": "江户招牌"
    },
    {
      "name": "江湖体"
    },
    {
      "name": "油漆体"
    },
    {
      "name": "海岛森林_全字符"
    },
    {
      "name": "清刻本悦"
    },
    {
      "name": "温柔体"
    },
    {
      "name": "港风繁体"
    },
    {
      "name": "游园体"
    },
    {
      "name": "漫语体"
    },
    {
      "name": "点宋体"
    },
    {
      "name": "烈金体"
    },
    {
      "name": "烟波宋"
    },
    {
      "name": "特黑体"
    },
    {
      "name": "琉璃宋"
    },
    {
      "name": "瑞意宋"
    },
    {
      "name": "瑶蝶体"
    },
    {
      "name": "甜甜圈"
    },
    {
      "name": "目光体"
    },
    {
      "name": "真言体"
    },
    {
      "name": "研宋体"
    },
    {
      "name": "禅影体"
    },
    {
      "name": "童趣体"
    },
    {
      "name": "简中圆"
    },
    {
      "name": "糯米团"
    },
    {
      "name": "纯真体"
    },
    {
      "name": "细体"
    },
    {
      "name": "经典雅黑"
    },
    {
      "name": "综艺字"
    },
    {
      "name": "美佳体"
    },
    {
      "name": "聚珍体"
    },
    {
      "name": "芋圆体"
    },
    {
      "name": "若烟体"
    },
    {
      "name": "荔枝体"
    },
    {
      "name": "萌趣体"
    },
    {
      "name": "蒹葭体"
    },
    {
      "name": "薯条少年"
    },
    {
      "name": "蝉影隶书"
    },
    {
      "name": "装甲明朝"
    },
    {
      "name": "谷秋体"
    },
    {
      "name": "超重要体"
    },
    {
      "name": "轻吟体"
    },
    {
      "name": "追光体"
    },
    {
      "name": "逸致拼音"
    },
    {
      "name": "金陵体"
    },
    {
      "name": "锦瑟体"
    },
    {
      "name": "雁兰体"
    },
    {
      "name": "雅酷黑简"
    },
    {
      "name": "霸燃手书"
    },
    {
      "name": "青松体"
    },
    {
      "name": "风雅宋"
    },
    {
      "name": "飒爽手写"
    },
    {
      "name": "飞扬行书"
    },
    {
      "name": "飞驰体"
    },
    {
      "name": "高字标志黑"
    },
    {
      "name": "高字湘黑体"
    },
    {
      "name": "黄令东齐伋复刻体"
    },
    {
      "name": "黄金时代"
    },
    {
      "name": "黑糖体"
    },
    {
      "name": "默陌手写"
    },
    {
      "name": "아기"
    },
    {
      "name": "セリフ太字"
    },
    {
      "name": "一笔壹画加油体"
    },
    {
      "name": "一笔壹画潮黑体"
    },
    {
      "name": "三极力量体简_粗"
    },
    {
      "name": "三极妙漫体"
    },
    {
      "name": "三极宋黑体超粗"
    },
    {
      "name": "三极拙墨体"
    },
    {
      "name": "三极极宋超粗"
    },
    {
      "name": "三极榜楷简体"
    },
    {
      "name": "三极欢乐体"
    },
    {
      "name": "三极正雅黑粗"
    },
    {
      "name": "三极气泡体"
    },
    {
      "name": "三极泼墨体"
    },
    {
      "name": "三极浓密仙粗"
    },
    {
      "name": "三极湘乡体"
    },
    {
      "name": "三极萌喵简体"
    },
    {
      "name": "三极行楷简体_粗"
    },
    {
      "name": "三极黑宋体中粗"
    },
    {
      "name": "云书法三行魏碑体"
    },
    {
      "name": "云书法手书建刚静心楷简"
    },
    {
      "name": "云书法生如夏花简"
    },
    {
      "name": "云书法罗西硬笔楷书体"
    },
    {
      "name": "亦然体"
    },
    {
      "name": "仓耳丝柔体"
    },
    {
      "name": "仓耳体"
    },
    {
      "name": "仓耳力士"
    },
    {
      "name": "凌丝体"
    },
    {
      "name": "利飞体"
    },
    {
      "name": "剪映新年体"
    },
    {
      "name": "励字大黑简繁"
    },
    {
      "name": "励字姚体简繁"
    },
    {
      "name": "励字志向黑简_特粗"
    },
    {
      "name": "励字憨憨简"
    },
    {
      "name": "励字敲可爱简_中粗"
    },
    {
      "name": "励字行楷简繁"
    },
    {
      "name": "励字趣石简"
    },
    {
      "name": "励字造梦简_特粗"
    },
    {
      "name": "励字隶书简繁"
    },
    {
      "name": "华书体"
    },
    {
      "name": "听露体"
    },
    {
      "name": "字由爱驾公路体"
    },
    {
      "name": "字语古兰体"
    },
    {
      "name": "字语咏宋体"
    },
    {
      "name": "字语咏楷体"
    },
    {
      "name": "字语嘟嘟体"
    },
    {
      "name": "字语文韵体"
    },
    {
      "name": "字语软糖体"
    },
    {
      "name": "宜宋"
    },
    {
      "name": "小可爱体"
    },
    {
      "name": "少年南波万"
    },
    {
      "name": "山雁体"
    },
    {
      "name": "幽梦体"
    },
    {
      "name": "归雁体"
    },
    {
      "name": "景曜体"
    },
    {
      "name": "月亮供电不足"
    },
    {
      "name": "未光体"
    },
    {
      "name": "毛体行楷"
    },
    {
      "name": "汉字之美棒棒糖粗简"
    },
    {
      "name": "汉字之美郝刚牡丹体简"
    },
    {
      "name": "点字佳楷"
    },
    {
      "name": "点字奇巧"
    },
    {
      "name": "点字小隶书"
    },
    {
      "name": "点字玄真宋"
    },
    {
      "name": "点字艺圆"
    },
    {
      "name": "点字青花楷"
    },
    {
      "name": "点字青花隶"
    },
    {
      "name": "烟客体"
    },
    {
      "name": "爱你是无解命题"
    },
    {
      "name": "爱民小楷"
    },
    {
      "name": "玄鸟体"
    },
    {
      "name": "知新体"
    },
    {
      "name": "竹言体"
    },
    {
      "name": "花锦体"
    },
    {
      "name": "莫雪体"
    },
    {
      "name": "造字侠今朝醉简"
    },
    {
      "name": "造字侠寻味江湖简"
    },
    {
      "name": "造字侠陈坤风行简繁"
    },
    {
      "name": "阳华体"
    },
    {
      "name": "阳煦体"
    },
    {
      "name": "雅月体"
    },
    {
      "name": "青鸟华光书宋2"
    },
    {
      "name": "青鸟华光仿宋2"
    },
    {
      "name": "青鸟华光细黑"
    },
    {
      "name": "青鸟华光美黑"
    },
    {
      "name": "青鸟华光黑变"
    },
    {
      "name": "高字标志圆"
    },
    {
      "name": "鱼太闲躺平体"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_text

POST /add_text

添加文字

> Body 请求参数

```json
"{\n    \"text\": \"你好!Hello\",  // 文本内容（必填，核心显示内容）\n    \"start\": 0,  // 文本在时间线的起始时间（秒，必填）\n    \"end\": 5,  // 文本在时间线的结束时间（秒，必填）\n    \"draft_id\": \"your_draft_id\",  // 草稿ID（选填，用于关联操作的草稿）\n    \"transform_y\": 0,  // Y轴变换参数（选填，默认0）\n    \"transform_x\": 0,  // X轴变换参数（选填，默认0）\n    \"font\": \"文轩体\",  // 字体（选填，默认\"系统\"）\n    \"font_color\": \"#FF0000\",  // 字体颜色（选填，默认红色#FF0000）\n    \"font_size\": 8.0,  // 字体大小（选填，默认8.0）\n    \"track_name\": \"text_main\",  // 轨道名称（选填，默认\"text_main\"）\n    \"vertical\": false,  // 是否垂直显示（选填，默认false）\n    \"font_alpha\": 1.0,  // 字体透明度（选填，默认1.0，范围0.0-1.0）\n    \"fixed_width\": -1,  // 固定宽度（选填，默认-1，-1表示不固定）\n    \"fixed_height\": -1,  // 固定高度（选填，默认-1，-1表示不固定）\n    // 描边参数\n    \"border_alpha\": 1.0,  // 描边透明度（选填，默认1.0）\n    \"border_color\": \"#000000\",  // 描边颜色（选填，默认黑色#000000）\n    \"border_width\": 0.0,  // 描边宽度（选填，默认0.0）\n    // 背景参数\n    \"background_color\": \"#000000\",  // 背景颜色（选填，默认黑色#000000）\n    \"background_style\": 0,  // 背景样式（选填，默认0，需与业务支持的样式枚举匹配）\n    \"background_alpha\": 0.0,  // 背景透明度（选填，默认0.0）\n    // 入场动画\n    \"intro_animation\": \"向下飞入\",  // 入场动画类型（选填，如\"向下飞入\"等）\n    \"intro_duration\": 0.5,  // 入场动画持续时间（秒，选填，默认0.5）\n    // 出场动画\n    \"outro_animation\": \"向下滑动\",  // 出场动画类型（选填，如\"向下滑动\"淡出等）\n    \"outro_duration\": 0.5,  // 出场动画持续时间（秒，选填，默认0.5）\n    \"width\": 1080,  // 画布宽度（选填，默认1080）\n    \"height\": 1920,  // 画布高度（选填，默认1920）\n    \"text_styles\": [\n        {\n            \"start\": 0, // 开始字符位置，包含\n            \"end\": 2,  // 结束字符位置，不包含\n            \"style\": {\n                \"size\": 50.0,\n                \"bold\": true,\n                \"italic\": true,\n                \"underline\": true,\n                \"color\": \"#00FF00\"\n            },\n            \"border\": {\n                \"alpha\": 1,\n                \"color\": \"#FFFFFF\",\n                \"width\": 40\n            },\n            \"font\": \"挥墨体\"\n        }\n    ]\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» text|body|string| 是 |文本内容（必填，核心显示内容）|
|» start|body|number| 是 |文本在时间线的起始时间（秒，必填）|
|» end|body|number| 是 |文本在时间线的结束时间（秒，必填）|
|» draft_id|body|string| 否 |草稿ID（选填，用于关联操作的草稿）|
|» transform_y|body|number| 否 |垂直移动参数，相对值，1表示屏幕最右边，-1表示屏幕最左边，0表示剧中（选填，默认0）|
|» transform_y_px|body|integer| 否 |垂直移动参数，像素值|
|» transform_x|body|number| 否 |水平移动参数，相对值，1表示屏幕最上边，-1表示屏幕最下边，0表示居中（选填，默认0）|
|» transform_x_px|body|integer| 否 |水平移动参数，像素值|
|» scale_x|body|number| 否 |水平方向缩放，相对值，默认为1|
|» scale_y|body|number| 否 |垂直方向缩放，相对值，默认为1|
|» font|body|string| 否 |字体（选填，默认系统）|
|» align|body|integer| 否 |对齐方式，0左对齐，1居中，2右对齐|
|» rotation|body|number| 否 |旋转角度|
|» font_color|body|string| 否 |字体颜色（选填，默认红色#FF0000）|
|» font_size|body|integer| 否 |字体大小（选填，默认8.0）|
|» track_name|body|string| 否 |轨道名|
|» relative_index|body|integer| 否 |轨道相对位置，越大越靠上|
|» vertical|body|boolean| 否 |是否垂直显示（选填，默认false）|
|» font_alpha|body|number| 否 |字体透明度（选填，默认1.0，范围0.0-1.0）|
|» fixed_width|body|number| 否 |固定宽度（选填，默认-1，-1表示不固定）|
|» fixed_height|body|number| 否 |固定高度（选填，默认-1，-1表示不固定）|
|» border_alpha|body|number| 否 |描边透明度（选填，默认1.0）|
|» border_color|body|string| 否 |描边颜色（选填，默认黑色#000000）|
|» border_width|body|integer| 否 |描边宽度（选填，默认0.0）|
|» background_color|body|string| 否 |背景颜色（选填，默认黑色#000000）|
|» background_style|body|integer| 否 |背景样式（选填，默认0，需与业务支持的样式枚举匹配）|
|» background_alpha|body|number| 否 |背景透明度（选填，默认0.0）|
|» background_round_radius|body|number| 否 |背景圆角（选填，默认0，取值范围0-1）|
|» background_height|body|number| 否 |背景高度占比（选填，默认0.14，范围0.0-1.0）|
|» background_width|body|number| 否 |背景宽度占比（选填，默认0.14，范围0.0-1.0）|
|» background_horizontal_offset|body|number| 否 |背景水平偏移（选填，默认0.5，范围0.0-1.0，0.5为水平居中）|
|» background_vertical_offset|body|number| 否 |背景垂直偏移（选填，默认0.5，范围0.0-1.0，0.5为垂直居中）|
|» shadow_enabled|body|boolean| 否 |是否启用阴影（选填，默认false，true为启用阴影效果）|
|» shadow_alpha|body|number| 否 |阴影透明度（选填，默认0.9，范围0.0-1.0）|
|» shadow_angle|body|number| 否 |阴影角度（选填，默认-45.0，范围-180.0-180.0，单位为度）|
|» shadow_color|body|string| 否 |阴影颜色（选填，默认#000000，支持十六进制颜色码）|
|» shadow_distance|body|integer| 否 |阴影距离（选填，默认5.0，范围0-100，控制阴影与文本的间距）|
|» shadow_smoothing|body|number| 否 |阴影平滑度（选填，默认0.15，范围0.0-1.0，值越大阴影越模糊）|
|» intro_animation|body|string| 否 |入场动画|
|» intro_duration|body|number| 否 |入场动画持续时间（秒，选填，默认0.5）|
|» outro_animation|body|string| 否 |出场动画|
|» outro_duration|body|number| 否 |出场动画持续时间（秒，选填，默认0.5）|
|» loop_animation|body|string| 否 |循环动画|
|» loop_duration|body|number| 否 |x|
|» width|body|integer| 否 |画布宽度（选填，默认1080）|
|» height|body|integer| 否 |画布高度（选填，默认1920）|
|» text_styles|body|[object]| 否 |none|
|»» start|body|integer| 否 |开始字符位置，包含|
|»» end|body|integer| 否 |结束字符位置，不包含|
|»» style|body|object| 否 |none|
|»»» size|body|integer| 是 |none|
|»»» bold|body|boolean| 是 |none|
|»»» italic|body|boolean| 是 |none|
|»»» underline|body|boolean| 是 |none|
|»»» color|body|string| 是 |none|
|»» border|body|object| 否 |none|
|»»» alpha|body|integer| 是 |none|
|»»» color|body|string| 是 |none|
|»»» width|body|integer| 是 |none|
|»» font|body|string| 否 |none|
|» bubble_effect_id|body|string| 否 |气泡效果ID（选填）|
|» bubble_resource_id|body|string| 否 |气泡资源ID（选填）|
|» effect_effect_id|body|string| 否 |花字效果ID（选填）|
|» letter_spacing|body|integer| 否 |字符间距（选填）|
|» line_spacing|body|integer| 否 |行间距（选填）|

#### 枚举值

|属性|值|
|---|---|
|» align|0|
|» align|1|
|» align|2|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752371354_d9fa0aaf",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752371354_d9fa0aaf"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_subtitle

POST /add_subtitle

向指定草稿添加字幕，比add_text方法多了直接添加srt格式字幕的能力，但是不支持动画

> Body 请求参数

```json
"{\n    \"srt\": \"1\\\\n00:00:00,000 --> 00:00:04,433\\\\n你333好，我是孙观楠开发的剪映草稿助手。\",  // 字幕内容或SRT文件URL（必填，支持直接传字幕文本或文件路径/URL）\n    \"draft_id\": \"your_draft_id_123\",  // 草稿ID（选填，用于指定要添加字幕的草稿）\n    \"time_offset\": 0.0,  // 字幕时间偏移量（秒，选填，默认0.0，可整体调整字幕显示时间）\n    // 字体样式参数\n    \"font_size\": 5.0,  // 字体大小（选填，默认5.0）\n    \"font\": \"挥墨体\",\n    \"bold\": false,  // 是否加粗（选填，默认false）\n    \"italic\": false,  // 是否斜体（选填，默认false）\n    \"underline\": false,  // 是否下划线（选填，默认false）\n    \"font_color\": \"#FFFFFF\",  // 字体颜色（选填，默认白色#FFFFFF，支持十六进制色值）\n    \"vertical\": false,  // 是否垂直显示（选填，默认false）\n    \"alpha\": 1,  // 字体透明度（选填，默认1，范围0-1，1为完全不透明）\n    // 边框参数\n    \"border_alpha\": 1.0,  // 边框透明度（选填，默认1.0）\n    \"border_color\": \"#000000\",  // 边框颜色（选填，默认黑色#0a00000）\n    \"border_width\": 0.0,  // 边框宽度（选填，默认0.0）\n    // 背景参数\n    \"background_color\": \"#000000\",  // 背景颜色（选填，默认黑色#000000）\n    \"background_style\": 0,  // 背景样式（选填，默认0，需与业务支持的样式枚举匹配，如0为无背景、1为矩形背景等）\n    \"background_alpha\": 0.0,  // 背景透明度（选填，默认0.0）\n    // 图像调节参数\n    \"transform_x\": 0.0,  // X轴位置偏移（选填，默认0.0）\n    \"transform_y\": -0.8,  // Y轴位置偏移（选填，默认-0.8）\n    \"scale_x\": 1.0,  // X轴缩放比例（选填，默认1.0）\n    \"scale_y\": 1.0,  // Y轴缩放比例（选填，默认1.0）\n    \"rotation\": 0.0,  // 旋转角度（选填，默认0.0，单位为度）\n    \"track_name\": \"subtitle\",  // 轨道名称（选填，默认\"subtitle\"）\n    \"width\": 1080,  // 画布宽度（选填，默认1080）\n    \"height\": 1920  // 画布高度（选填，默认1920）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» srt|body|string| 是 |字幕内容或SRT文件URL（必填，支持直接传字幕文本或文件路径/URL）|
|» draft_id|body|string| 否 |草稿ID（选填，用于指定要添加字幕的草稿）|
|» time_offset|body|number| 否 |字幕时间偏移量（秒，选填，默认0.0，可整体调整字幕显示时间）|
|» font_size|body|integer| 否 |字体大小（选填，默认5.0）|
|» font|body|string| 否 |字体|
|» bold|body|boolean| 否 |是否加粗（选填，默认false）|
|» italic|body|boolean| 否 |是否斜体（选填，默认false）|
|» underline|body|boolean| 否 |是否下划线（选填，默认false）|
|» font_color|body|string| 否 |字体颜色（选填，默认白色#FFFFFF，支持十六进制色值）|
|» vertical|body|boolean| 否 |是否垂直显示（选填，默认false）|
|» alpha|body|number| 否 |字体透明度（选填，默认1，范围0-1，1为完全不透明）|
|» border_alpha|body|number| 否 |边框透明度（选填，默认1.0）|
|» border_color|body|string| 否 |边框颜色（选填，默认黑色#000000）|
|» border_width|body|integer| 否 |边框宽度（选填，默认0.0）|
|» background_color|body|string| 否 |背景颜色（选填，默认黑色#000000）|
|» background_style|body|integer| 否 |背景样式（选填，默认0，需与业务支持的样式枚举匹配，如0为无背景、1为矩形背景等）|
|» background_alpha|body|number| 否 |背景透明度（选填，默认0.0）|
|» transform_x|body|number| 否 |水平偏移，相对值（选填，默认0.0）|
|» transform_x_px|body|integer| 否 |水平便宜，像素值|
|» transform_y|body|number| 否 |垂直偏移，相对值（选填，默认-0.8）|
|» transform_y_px|body|integer| 否 |垂直偏移，像素值|
|» scale_x|body|number| 否 |X轴缩放比例（选填，默认1.0）|
|» scale_y|body|number| 否 |Y轴缩放比例（选填，默认1.0）|
|» rotation|body|number| 否 |旋转角度（选填，默认0.0，单位为度）|
|» track_name|body|string| 否 |none|
|» width|body|integer| 否 |画布宽度（选填，默认1080）|
|» height|body|integer| 否 |画布高度（选填，默认1920）|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752373713_c3229c85",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752373713_c3229c85"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST search_artist

POST /artist/search_artist

搜索花字。从返回结果中获取：data.data.effect_item_list.common_attr.effect_id，将它填入add_text的effect_effect_id即可设置花字

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|keyword|query|string| 是 |none|
|offset|query|integer| 是 |none|
|Content-Type|header|string| 是 |none|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "data": {
    "data": {
      "effect_item_list": [
        {
          "description": "红色发光花字",
          "effect_id": "WklmRFJVRlRCaVJVTl9PZ0BTVQ==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/9be4cf7687484b488dc265139a390f29~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=9fOO8DT2IHlT9ysZV39sgHNFJOs%3D"
        },
        {
          "description": "红色花字",
          "effect_id": "W0BmQFNaQVJBbFlRTVlLbkBdUA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/34751f7d2b6e4f0a855024561dde5f3c~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=HkfAYxljb77MuVsAjjgyE31zStk%3D"
        },
        {
          "description": "纯红色白边花字1",
          "effect_id": "W0BoR1JbR1RMaFxdTlVPZkdRVA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f5d7421cdafe48059c9828a3e0bf0ffa~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=Dg1Y8%2BehlJhHHJbfs1tK3rwdyyA%3D"
        },
        {
          "description": "黄色红色发光花字",
          "effect_id": "WkhvQFJaRlJDa1pQQVtAbkVUVg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/da0be87c7f124e11bb97237219b72a1b~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=N3aoM%2FTSN3YkUvEW6NJRQLSigOE%3D"
        },
        {
          "description": "",
          "effect_id": "W0BtRFRVQlRAa19XSFVMb0BTWw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/5bf1e6ce93f843fa997f9a3ddf30b209~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=LaXCMkXfwZjEsDjCzdqisUFDDm4%3D"
        },
        {
          "description": "红色立体描边字",
          "effect_id": "WkltQlVUSlxCbFhRT1pObkFVWw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/5f56905bbb5d4cedb3092a758285f55d~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=WRIZbW7BTSo2Wq86mO4H6u%2FOWtI%3D"
        },
        {
          "description": "白色红描边花字",
          "effect_id": "W0BoRFZTRFBMaVpVSV5KaEpQVQ==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/9ead19d5b2af41ee94ecf88da05c1f32~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=fxPJHXB48q%2BC2hL3WS1KQArqSUQ%3D"
        },
        {
          "description": "潮酷红色渐变裂痕立体花字",
          "effect_id": "WklpS1RURFNNbVpWS1hKbkdcUQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/14674593144a462f8d3c9117d332a33b~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=71Vh0IKGtn3cHr2m%2BbE8nNA12iY%3D"
        },
        {
          "description": "渐变红色纹理花字",
          "effect_id": "W0BoR1BUQFNMal5RTVxNZ0FWUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d02272d5b234458a8c582960078ed58c~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=5N6TP8Rys7V2ScI8t4DoCV7m3Dw%3D"
        },
        {
          "description": "红色立体花字",
          "effect_id": "W0BoQFBVRVVGaV9WTVhNbUtdVg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/55ffff4f565c4cfc933d82325968ca93~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=fQfLdSC8RU22y0zph7RuNO9SQ38%3D"
        },
        {
          "description": "",
          "effect_id": "W0FmRVRQSldBa1NUQFhLaEpQUw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ba96576adf1f4550a8cdc143d4584966~tplv-3jr8j4ixpe-resize:0:0.png?x-expires=1788332384&x-signature=%2B%2FOiEcZnVcI1B4ssa7hahDHdY0o%3D"
        },
        {
          "description": "197",
          "effect_id": "W0BuQldSQFJNbFNSQV9BakZdUQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/b9ae05b386b54dcd80c365c2f872c5ba~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=qQs5L9ILRWGIueu9wS5f0kDOuwg%3D"
        },
        {
          "description": "",
          "effect_id": "W0FmRVRQSlRGbVxSTVlAZ0ZTVA==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d5605fd611b9471f81ba36c1b20f8e5a~tplv-3jr8j4ixpe-resize:0:0.png?x-expires=1788332384&x-signature=CpMkqE68qEJORXF0QAzLphutIFw%3D"
        },
        {
          "description": "可爱红蓝色立体花字",
          "effect_id": "WklmSlJVRVdBZ1xXSVRIa0VcVg==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d61be13ecbf34027ab33711c539c0ce2~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=2T8XITlRtjsqBe2CkeWmjtI29Jw%3D"
        },
        {
          "description": "红色立体花字",
          "effect_id": "W0BoS1dWRVZGbFpcTVxJZkFXUw==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/91c4e592f983425592c3fa1247f4a4d8~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=pMseAVmea1EdxphaTJOfCxZFORY%3D"
        },
        {
          "description": "电影字幕-红色",
          "effect_id": "WkluRlxRRlZDaVJQT1tJbEVSWw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c5dfb845babd42ebb553a2f28b4eacb0~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=uxB89XYOAR0TSU%2BPe3DMcrbc3z4%3D"
        },
        {
          "description": "综艺-黑暗斑驳红色",
          "effect_id": "WkpqQlZSRFBEbFxSSF5PZ0RUUA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2634ee84152f4f29adbc48a7e195fc51~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=%2BYUU7MZQdvGFclFTY3AMJjlzoLM%3D"
        },
        {
          "description": "红色印章花字",
          "effect_id": "WklrRF1RQ1VMZltTSF9LaERdVw==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f6f16a950cfa4f22bf60052a79303c25~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=fTKwoItaQtKBhYRMhNAV6oeN3AQ%3D"
        },
        {
          "description": "kr021",
          "effect_id": "W0BuQldSQFNHZ1JTSFpAZ0dVVA==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/69bb332efe994d929ac26dd94e96b6d4~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=GjWXULsgRxf2wql2GsEi2c%2FUTYc%3D"
        },
        {
          "description": "简易透明红色花字",
          "effect_id": "WklrQ1FQSl1BZ11QTF9JaUVdVQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/36f5b3fd6cf44d1189099c3e18149228~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=CGAYuZq0O0iH%2B%2Fr2%2BXMxng1%2FBCE%3D"
        },
        {
          "description": "",
          "effect_id": "WkptQlxUQFVNa1JRQF1IZ0pXUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/895c9cc55c3a4175a0c63eefba1f67a1~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=xGlxog1ykzVlK3IKyU8SnRk2PE4%3D"
        },
        {
          "description": "【国庆专用】红色渐变立体花字",
          "effect_id": "WkluR1BTSl1MaFtVSVVPbkpdWw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7d29e200f61d492a96b5ba6b24ba64d2~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=CPf9C6DrLOkYrK6umBZ7mk2nYnY%3D"
        },
        {
          "description": "花字红字白边",
          "effect_id": "WklsQFdRQV1BaF9VS1xKaEtcUQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/12e5301b22fe4268a5aa70db3b9f5c7d~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=4wpvClyDUaS7kGjrODdOjX2yLN4%3D"
        },
        {
          "description": "喜庆红色渐变白边花字",
          "effect_id": "WklpQ1FSQVFCZltWSFhKbEtWUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/da107855792441e0b21d07629571cfef~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=1h%2FSTvU6GMYU0j1SaElw0B8EkHc%3D"
        },
        {
          "description": "",
          "effect_id": "W0BtRFRVQlRAa19XTFRIbEVUVg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/a96e7090432c4be68d3ce90edc7d12cc~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=zGqMwQ6meB3JQMVVBaEWDF8hAeM%3D"
        },
        {
          "description": "阮氏红色红边黑框花字",
          "effect_id": "W0BoRlNbS1dAb11UTl9LakZVUQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d4143d62b92e4e4a87e426fc906752cc~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=7QtX%2FpGuYr1Z6cYmYvMK0ZKYQGM%3D"
        },
        {
          "description": "潮酷红色渐变裂纹立体花",
          "effect_id": "WklpS1BSQlJBaFxVQVtBb0ddVg==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ae895ae84f224f9d849dba2da4da0d6c~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=9eHZRpiyyEnZmrdLY3t5vG%2FNpFs%3D"
        },
        {
          "description": "醒目红色    白字红底蓝底边花字",
          "effect_id": "WkpsRFNRQV1AaV5WS1hJb0RRVQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/701a0a7387fc4533adb88600d6fa2b43~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=4e103QsDbY9INtFcTaRPH4%2FJCuk%3D"
        },
        {
          "description": "超酷黄红色霓虹发光花字",
          "effect_id": "WklpRFVVR1dGaltUSV5MbkBcUw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/b939eeb784594df59a169a82bde193f9~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=AAay6KzPQrgoFI2OGnSwihObrEA%3D"
        },
        {
          "description": "",
          "effect_id": "W0FmRVRQRV1BZ1JcQVRKb0RVUg==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/3d98408c2acb4b4eabdeab5022ed1061~tplv-3jr8j4ixpe-resize:0:0.png?x-expires=1788332384&x-signature=pRISInpYm3G0xoHEtmTGx%2FpF%2F2w%3D"
        },
        {
          "description": "红色烈焰花字",
          "effect_id": "WkltQlVXS1VFalJRSVpPbUtcUA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/4fcd5034d7c74b239a75b54fd3eb3f8a~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=6oIItxp3ySf3Pul6DdBhaJURKlE%3D"
        },
        {
          "description": "喜庆红色立体花字",
          "effect_id": "W0BoR1VWQlZHZlNTSllNZ0tTUQ==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/42cf89100a194852a32ba03fa88bc76c~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=VX9s2mCNtqdAU4c5YH5y%2Bq4Zx1E%3D"
        },
        {
          "description": "喜庆白字红描边立体花字",
          "effect_id": "WkpuQl1UR1xMblpRTl5OaUpVWw==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/b459ef3c92254ac89a71885f011b76cb~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=1nICyOVFULvuFvZqCa8Jeyxde8M%3D"
        },
        {
          "description": "潮酷红色渐变裂纹立体花字",
          "effect_id": "WklpS1BbRVJCalhUSVlNbURdUA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e896feb2fec84a1dab47d65101d04229~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=tEPkxr3NkeOed6SRTnnc5WHDzRQ%3D"
        },
        {
          "description": "中国红黄色边",
          "effect_id": "WkluRlRVQ1xGaFJcSFxIZkJWVw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c46ffc7369d84952bec300bc6be81ed8~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=Iu2to2HweW6l%2F%2FjWizNimff5fAA%3D"
        },
        {
          "description": "喜庆红白厚重立体花字",
          "effect_id": "W0BoQFxUQFVBalxVSVxPbEtSVQ==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/bad8535b1d1e42f8ae7003d4bf3e2256~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=2xBmFlHSCUEBr2g62phO%2BfvKl5s%3D"
        },
        {
          "description": "综艺红色花字",
          "effect_id": "W0BnR1xaRFFHZl1TTltMaEpcVA==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/4a060840bad44020a85e0f719df5e274~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=1GdvWrkuhMJIPtNZ5qmrL1A1OsU%3D"
        },
        {
          "description": "怀旧红色斑驳立体发光花字",
          "effect_id": "WklqS1xaRlJBaF5cTV5PZkBUUg==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/423f7639f1da4dec975b2eebba02b204~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=nzjMLs%2FMFrZJCIPxNUS78dVfB40%3D"
        },
        {
          "description": "喜庆红黄渐变立体花字",
          "effect_id": "W0BoQFxURVNNaFNVT1hIZkZTVg==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e556623a9d06439eb9ffc0b792b3ab1f~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=xYHtm1mkEa4rCe9J52osnjP2rSA%3D"
        },
        {
          "description": "千禧玉兰体花字",
          "effect_id": "WkluQ1dTQlFBaFpdTV5AZ0VVVg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/05dd069e05124dd18f6d86923e160f41~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=snvZipNW6EBDc5Ced7WX0F5JT4E%3D"
        },
        {
          "description": "简约喜庆红白花字",
          "effect_id": "W0BoQFxUQVZAb1xXTFpLaUdSUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2f6e4cc546c744f8ab74d659ee85cd93~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=TAlHQRMUb4KyaIblnxHK4GYfmUA%3D"
        },
        {
          "description": "喜庆红色渐变黄边花字",
          "effect_id": "WklpQ1FTR11FaVNVTVVBaEtXUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/dcbaa4e811354362a55fe0a565989fba~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=%2Bzl%2FqEmNtUUZsd9Imh1Zi2F%2B5AM%3D"
        },
        {
          "description": "",
          "effect_id": "W0FmRVRQSldBa1NUQFhNa0BWVw==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7d71379cbce4446da186c7afb4416f83~tplv-3jr8j4ixpe-resize:0:0.png?x-expires=1788332384&x-signature=vghNftbz6Sab6g8a5fi7mg6zOss%3D"
        },
        {
          "description": "红色立体简约多边花字",
          "effect_id": "WklvRFNVQlxHbVhcQVpKb0ZUUw==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/0bc7273566574396bf2eb369f374e4b3~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=4fk7LMiEVdaWhasemqgNqWnFtCc%3D"
        },
        {
          "description": "",
          "effect_id": "WkhtRFNVRVJBZ1lRTFRNb0ZUUw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/5bfdb9e9176f4269ae0a0b16fa041049~tplv-3jr8j4ixpe-resize:0:0.png?x-expires=1788332384&x-signature=u%2B1ajS0lbD%2BwKUbWaV3xdqwhp9g%3D"
        },
        {
          "description": "红色血迹墨迹生锈立体花字",
          "effect_id": "WktrQVdSR11EbF1SQVRMbURTVg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6587543510654f8898db9a69d8aac3ad~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=1cj3JsXdgT2NhP812%2BG%2Bad2Eg7I%3D"
        },
        {
          "description": "喜庆红色渐变白边花字",
          "effect_id": "WklpQ1FTSlNNaV5cSl1LaUBRUw==",
          "static_image": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7b30e822a5204925b08d5f05d32475bf~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=PabZB2htFHHlLM42A5FTbcLoC8g%3D"
        },
        {
          "description": "花字-中国红渐变",
          "effect_id": "WkhrS1NXSlJGZ1pST1lPbEJWVQ==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ba330f3d6d374e8bb2499c032d3d4501~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=Jd58aEFInM7TtKGZxbHb%2FLebdUs%3D"
        },
        {
          "description": "白色红边印章花字",
          "effect_id": "WklqQlRTS1VFZllQTFxPa0JcUg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/aa66f0f157c942728abf64573f594541~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=E3hl6s10Ts6AgZ8UBuieVmuHO4g%3D"
        },
        {
          "description": "怀旧红色斑驳花字",
          "effect_id": "WklqS1xWR1xMbV5SQF5LbUJUWg==",
          "static_image": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/091cb42673a24d58abd36880fa17c135~tplv-3jr8j4ixpe-resize:200:200.png?x-expires=1788332384&x-signature=LRtjt2r31d4yXgb3B%2BYS3RQ5w8A%3D"
        }
      ]
    }
  },
  "status_code": 200
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» data|object|true|none||none|
|»» data|object|true|none||none|
|»»» effect_item_list|[object]|true|none||none|
|»»»» description|string|false|none||none|
|»»»» effect_id|string|true|none||none|
|»»»» static_image|string|false|none||none|
|» status_code|integer|true|none||none|

## POST add_text_template

POST /add_text_template

> Body 请求参数

```json
{
  "template_id": "7393022390638251303"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Content-Type|header|string| 是 |none|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» template_id|body|string| 是 |模版id|
|» texts|body|[string]| 否 |文字模版的文字数组|
|» start|body|number| 否 |目标轨道的开始时间，默认0|
|» end|body|number| 否 |目标轨道的结束时间，默认模版持续时间|
|» draft_id|body|string| 否 |草稿id，为空表示创建新草稿|
|» transform_y|body|number| 否 |垂直偏移，相对值|
|» transform_y_px|body|integer| 否 |垂直偏移，像素值|
|» transform_x|body|number| 否 |水平偏移，相对值|
|» transform_x_px|body|integer| 否 |水平偏移，像素值|
|» rotation|body|number| 否 |旋转|
|» scale_x|body|number| 否 |水平缩放|
|» scale_y|body|number| 否 |垂直缩放|
|» track_name|body|string| 否 |轨道名称|
|» width|body|integer| 否 |画布宽度|
|» height|body|integer| 否 |画布高度|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1757036546_95232be6",
    "draft_url": "https://www.capcutapi.top/draft/downloader?draft_id=dfd_cat_1757036546_95232be6&is_capcut=0&api_key_hash=15b082f53a67b381693cc2c62982d3bf662463523721ca35544106af2d2bb57c"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 图片

## GET get_intro_animation_types

GET /get_intro_animation_types

获取可用的入场动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "缩小"
    },
    {
      "name": "渐显"
    },
    {
      "name": "放大"
    },
    {
      "name": "旋转"
    },
    {
      "name": "Kira游动"
    },
    {
      "name": "抖动下降"
    },
    {
      "name": "镜像翻转"
    },
    {
      "name": "旋转开幕"
    },
    {
      "name": "折叠开幕"
    },
    {
      "name": "漩涡旋转"
    },
    {
      "name": "跳转开幕"
    },
    {
      "name": "轻微抖动"
    },
    {
      "name": "轻微抖动_II"
    },
    {
      "name": "轻微抖动_III"
    },
    {
      "name": "上下抖动"
    },
    {
      "name": "左右抖动"
    },
    {
      "name": "斜切"
    },
    {
      "name": "钟摆"
    },
    {
      "name": "雨刷"
    },
    {
      "name": "雨刷_II"
    },
    {
      "name": "向上转入"
    },
    {
      "name": "向上转入_II"
    },
    {
      "name": "向左转入"
    },
    {
      "name": "向右转入"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向下甩入"
    },
    {
      "name": "向右甩入"
    },
    {
      "name": "向左上甩入"
    },
    {
      "name": "向右上甩入"
    },
    {
      "name": "向左下甩入"
    },
    {
      "name": "向右下甩入"
    },
    {
      "name": "动感放大"
    },
    {
      "name": "动感缩小"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "快速翻页"
    },
    {
      "name": "荧光爆闪"
    },
    {
      "name": "十字震动"
    },
    {
      "name": "爱心碰撞"
    },
    {
      "name": "冲撞"
    },
    {
      "name": "闪屏"
    },
    {
      "name": "扫描"
    },
    {
      "name": "震动波纹"
    },
    {
      "name": "分屏翻转"
    },
    {
      "name": "立体翻转"
    },
    {
      "name": "马赛克"
    },
    {
      "name": "_2024"
    },
    {
      "name": "多层环形"
    },
    {
      "name": "弹力分割"
    },
    {
      "name": "弹近"
    },
    {
      "name": "画出爱心"
    },
    {
      "name": "发光矩形"
    },
    {
      "name": "空间扭曲"
    },
    {
      "name": "四屏转换"
    },
    {
      "name": "展开"
    },
    {
      "name": "划水"
    },
    {
      "name": "色散波纹"
    },
    {
      "name": "模糊聚焦"
    },
    {
      "name": "圆形开幕"
    },
    {
      "name": "聚合"
    },
    {
      "name": "砸出波纹"
    },
    {
      "name": "向下甩动"
    },
    {
      "name": "向上滚动"
    },
    {
      "name": "拼图"
    },
    {
      "name": "向上闪入"
    },
    {
      "name": "交错开幕"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "侧滑"
    },
    {
      "name": "横向模糊"
    },
    {
      "name": "闪现"
    },
    {
      "name": "水墨"
    },
    {
      "name": "交叉震动"
    },
    {
      "name": "抖动横移"
    },
    {
      "name": "抖动变焦"
    },
    {
      "name": "斜向拉丝"
    },
    {
      "name": "拉丝滑入"
    },
    {
      "name": "果冻_I"
    },
    {
      "name": "果冻_II"
    },
    {
      "name": "烟雾弹"
    },
    {
      "name": "震波"
    },
    {
      "name": "震波_II"
    },
    {
      "name": "震波_III"
    },
    {
      "name": "旋转圆球"
    },
    {
      "name": "转圈圈"
    },
    {
      "name": "曝光放射"
    },
    {
      "name": "玻璃聚集"
    },
    {
      "name": "分屏横移"
    },
    {
      "name": "流金"
    },
    {
      "name": "心形放大"
    },
    {
      "name": "老电视"
    },
    {
      "name": "脉冲"
    },
    {
      "name": "能量立方"
    },
    {
      "name": "波纹弹动"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_outro_animation_types

GET /get_outro_animation_types

获取可用的出场动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "缩小"
    },
    {
      "name": "渐显"
    },
    {
      "name": "放大"
    },
    {
      "name": "旋转"
    },
    {
      "name": "Kira游动"
    },
    {
      "name": "抖动下降"
    },
    {
      "name": "镜像翻转"
    },
    {
      "name": "旋转开幕"
    },
    {
      "name": "折叠开幕"
    },
    {
      "name": "漩涡旋转"
    },
    {
      "name": "跳转开幕"
    },
    {
      "name": "轻微抖动"
    },
    {
      "name": "轻微抖动_II"
    },
    {
      "name": "轻微抖动_III"
    },
    {
      "name": "上下抖动"
    },
    {
      "name": "左右抖动"
    },
    {
      "name": "斜切"
    },
    {
      "name": "钟摆"
    },
    {
      "name": "雨刷"
    },
    {
      "name": "雨刷_II"
    },
    {
      "name": "向上转入"
    },
    {
      "name": "向上转入_II"
    },
    {
      "name": "向左转入"
    },
    {
      "name": "向右转入"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向下甩入"
    },
    {
      "name": "向右甩入"
    },
    {
      "name": "向左上甩入"
    },
    {
      "name": "向右上甩入"
    },
    {
      "name": "向左下甩入"
    },
    {
      "name": "向右下甩入"
    },
    {
      "name": "动感放大"
    },
    {
      "name": "动感缩小"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "快速翻页"
    },
    {
      "name": "荧光爆闪"
    },
    {
      "name": "十字震动"
    },
    {
      "name": "爱心碰撞"
    },
    {
      "name": "冲撞"
    },
    {
      "name": "闪屏"
    },
    {
      "name": "扫描"
    },
    {
      "name": "震动波纹"
    },
    {
      "name": "分屏翻转"
    },
    {
      "name": "立体翻转"
    },
    {
      "name": "马赛克"
    },
    {
      "name": "_2024"
    },
    {
      "name": "多层环形"
    },
    {
      "name": "弹力分割"
    },
    {
      "name": "弹近"
    },
    {
      "name": "画出爱心"
    },
    {
      "name": "发光矩形"
    },
    {
      "name": "空间扭曲"
    },
    {
      "name": "四屏转换"
    },
    {
      "name": "展开"
    },
    {
      "name": "划水"
    },
    {
      "name": "色散波纹"
    },
    {
      "name": "模糊聚焦"
    },
    {
      "name": "圆形开幕"
    },
    {
      "name": "聚合"
    },
    {
      "name": "砸出波纹"
    },
    {
      "name": "向下甩动"
    },
    {
      "name": "向上滚动"
    },
    {
      "name": "拼图"
    },
    {
      "name": "向上闪入"
    },
    {
      "name": "交错开幕"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "侧滑"
    },
    {
      "name": "横向模糊"
    },
    {
      "name": "闪现"
    },
    {
      "name": "水墨"
    },
    {
      "name": "交叉震动"
    },
    {
      "name": "抖动横移"
    },
    {
      "name": "抖动变焦"
    },
    {
      "name": "斜向拉丝"
    },
    {
      "name": "拉丝滑入"
    },
    {
      "name": "果冻_I"
    },
    {
      "name": "果冻_II"
    },
    {
      "name": "烟雾弹"
    },
    {
      "name": "震波"
    },
    {
      "name": "震波_II"
    },
    {
      "name": "震波_III"
    },
    {
      "name": "旋转圆球"
    },
    {
      "name": "转圈圈"
    },
    {
      "name": "曝光放射"
    },
    {
      "name": "玻璃聚集"
    },
    {
      "name": "分屏横移"
    },
    {
      "name": "流金"
    },
    {
      "name": "心形放大"
    },
    {
      "name": "老电视"
    },
    {
      "name": "脉冲"
    },
    {
      "name": "能量立方"
    },
    {
      "name": "波纹弹动"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_combo_animation_types

GET /get_combo_animation_types

获取可用的组合动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "缩小"
    },
    {
      "name": "渐显"
    },
    {
      "name": "放大"
    },
    {
      "name": "旋转"
    },
    {
      "name": "Kira游动"
    },
    {
      "name": "抖动下降"
    },
    {
      "name": "镜像翻转"
    },
    {
      "name": "旋转开幕"
    },
    {
      "name": "折叠开幕"
    },
    {
      "name": "漩涡旋转"
    },
    {
      "name": "跳转开幕"
    },
    {
      "name": "轻微抖动"
    },
    {
      "name": "轻微抖动_II"
    },
    {
      "name": "轻微抖动_III"
    },
    {
      "name": "上下抖动"
    },
    {
      "name": "左右抖动"
    },
    {
      "name": "斜切"
    },
    {
      "name": "钟摆"
    },
    {
      "name": "雨刷"
    },
    {
      "name": "雨刷_II"
    },
    {
      "name": "向上转入"
    },
    {
      "name": "向上转入_II"
    },
    {
      "name": "向左转入"
    },
    {
      "name": "向右转入"
    },
    {
      "name": "向上滑动"
    },
    {
      "name": "向下滑动"
    },
    {
      "name": "向左滑动"
    },
    {
      "name": "向右滑动"
    },
    {
      "name": "向下甩入"
    },
    {
      "name": "向右甩入"
    },
    {
      "name": "向左上甩入"
    },
    {
      "name": "向右上甩入"
    },
    {
      "name": "向左下甩入"
    },
    {
      "name": "向右下甩入"
    },
    {
      "name": "动感放大"
    },
    {
      "name": "动感缩小"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "快速翻页"
    },
    {
      "name": "荧光爆闪"
    },
    {
      "name": "十字震动"
    },
    {
      "name": "爱心碰撞"
    },
    {
      "name": "冲撞"
    },
    {
      "name": "闪屏"
    },
    {
      "name": "扫描"
    },
    {
      "name": "震动波纹"
    },
    {
      "name": "分屏翻转"
    },
    {
      "name": "立体翻转"
    },
    {
      "name": "马赛克"
    },
    {
      "name": "_2024"
    },
    {
      "name": "多层环形"
    },
    {
      "name": "弹力分割"
    },
    {
      "name": "弹近"
    },
    {
      "name": "画出爱心"
    },
    {
      "name": "发光矩形"
    },
    {
      "name": "空间扭曲"
    },
    {
      "name": "四屏转换"
    },
    {
      "name": "展开"
    },
    {
      "name": "划水"
    },
    {
      "name": "色散波纹"
    },
    {
      "name": "模糊聚焦"
    },
    {
      "name": "圆形开幕"
    },
    {
      "name": "聚合"
    },
    {
      "name": "砸出波纹"
    },
    {
      "name": "向下甩动"
    },
    {
      "name": "向上滚动"
    },
    {
      "name": "拼图"
    },
    {
      "name": "向上闪入"
    },
    {
      "name": "交错开幕"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "侧滑"
    },
    {
      "name": "横向模糊"
    },
    {
      "name": "闪现"
    },
    {
      "name": "水墨"
    },
    {
      "name": "交叉震动"
    },
    {
      "name": "抖动横移"
    },
    {
      "name": "抖动变焦"
    },
    {
      "name": "斜向拉丝"
    },
    {
      "name": "拉丝滑入"
    },
    {
      "name": "果冻_I"
    },
    {
      "name": "果冻_II"
    },
    {
      "name": "烟雾弹"
    },
    {
      "name": "震波"
    },
    {
      "name": "震波_II"
    },
    {
      "name": "震波_III"
    },
    {
      "name": "旋转圆球"
    },
    {
      "name": "转圈圈"
    },
    {
      "name": "曝光放射"
    },
    {
      "name": "玻璃聚集"
    },
    {
      "name": "分屏横移"
    },
    {
      "name": "流金"
    },
    {
      "name": "心形放大"
    },
    {
      "name": "老电视"
    },
    {
      "name": "脉冲"
    },
    {
      "name": "能量立方"
    },
    {
      "name": "波纹弹动"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_image

POST /add_image

向草稿中添加图片

> Body 请求参数

```json
{
  "image_url": "https://cdn.wanx.aliyuncs.com/wanx/1719234057367822001/text_to_image_v2/d6e33c84d7554146a25b1093b012838b_0.png?x-oss-process=image/resize,w_500/watermark,image_aW1nL3dhdGVyMjAyNDExMjkwLnBuZz94LW9zcy1wcm9jZXNzPWltYWdlL3Jlc2l6ZSxtX2ZpeGVkLHdfMTQ1LGhfMjU=,t_80,g_se,x_10,y_10/format,webp",
  "start": 0,
  "end": 5,
  "width": 1920,
  "height": 1080,
  "draft_id": "",
  "transform_x": 0.2,
  "transform_y": 0.2,
  "scale_x": 1,
  "scale_y": 1,
  "track_name": "video_main",
  "relative_index": 99,
  "intro_animation": "放大",
  "intro_animation_duration": 0.5,
  "outro_animation": "闪现",
  "outro_animation_duration": 0.5,
  "transition": "上移",
  "transition_duration": 0.5,
  "mask_type": "矩形",
  "mask_center_x": 0.5,
  "mask_center_y": 0.5,
  "mask_size": 0.7,
  "mask_rotation": 45,
  "mask_feather": 2,
  "mask_invert": true,
  "mask_rect_width": 8,
  "mask_round_corner": 10
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» image_url|body|string| 是 |图片链接|
|» start|body|number| 否 |在目标轨道上的开始时间，单位秒，默认0|
|» end|body|number| 是 |在目标轨道上的结束时间，单位秒|
|» width|body|integer| 否 |目标视频画布的宽度，非当前图片的宽度。如果之前已经设置过，则不能重复设置|
|» height|body|integer| 否 |目标视频画布的高度，非当前图片的宽度。如果之前已经设置过，则不能重复设置|
|» draft_id|body|string| 否 |目标草稿的草稿id，如果不提供，或者云端不存在，则会自动创建一个新的草稿|
|» transform_x|body|number| 否 |水平移动，相对值。0表示位于中心，水平移动像素 = transform_x * 视频宽度|
|» transform_x_px|body|integer| 否 |水平移动，像素值|
|» transform_y|body|number| 否 |垂直移动，相对值。0表示位于中心，垂直移动像素 = transform_y * 视频高度|
|» transform_y_px|body|string| 是 |垂直移动，像素值|
|» scale_x|body|number| 否 |水平方向缩放，默认1|
|» scale_y|body|number| 否 |垂直方向缩放，默认1|
|» track_name|body|string| 否 |添加的轨道名称，默认image_main|
|» relative_index|body|integer| 否 |轨道相对位置，越大越靠前|
|» intro_animation|body|string| 否 |入场动画名，例如“放大”|
|» intro_animation_duration|body|number| 否 |入场动画时间，单位秒|
|» outro_animation|body|string| 否 |出场动画名，例如“旋转”|
|» outro_animation_duration|body|number| 否 |出场动画时间，单位秒|
|» combo_animation|body|string| 否 |组合动画名，例如“水晶”，组合动画不能与入场和出场动画同时出现|
|» combo_animation_duration|body|number| 否 |组合动画时间，单位秒|
|» transition|body|string| 否 |转场动画名，例如"上移"|
|» transition_duration|body|number| 否 |转场动画持续时间，单位秒|
|» mask_type|body|string| 否 |蒙版类型，例如“矩形”|
|» mask_center_x|body|number| 否 |蒙版中心点坐标，0表示中心，0.5表示向右移动0.5个宽度|
|» mask_center_y|body|number| 否 |蒙版中心点坐标，0表示中心，0.5表示向下移动0.5个高度|
|» mask_size|body|number| 否 |蒙版大小，相对值，1表示1个画布宽度|
|» mask_rotation|body|number| 否 |蒙版旋转角度|
|» mask_feather|body|number| 否 |蒙版羽化度|
|» mask_invert|body|boolean| 否 |蒙版翻转|
|» mask_rect_width|body|number| 否 |蒙版矩形宽度|
|» mask_round_corner|body|number| 否 |蒙版圆角|
|» animation|body|string| 否 |入场动画，例如“放大”|
|» animation_duration|body|number| 否 |入场动画持续时间，单位秒|
|» background_blur|body|integer| 否 |背景模糊，1,2,3,4四档可选|
|» alpha|body|number| 否 |透明度，0-1|
|» flip_horizontal|body|boolean| 否 |是否镜像反转，默认false|
|» rotation|body|number| 否 |旋转角度|

> 返回示例

> 200 Response

```json
{
  "error": "string",
  "output": {
    "draft_id": "string",
    "draft_url": "string"
  },
  "purchase_link": "string",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none|草稿id|none|
|»» draft_url|string|true|none|草稿预览链接|none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 视频

## POST add_video

POST /add_video

添加视频

> Body 请求参数

```json
"{\n    \"video_url\": \"https://cdn.wanx.aliyuncs.com/wanx/1719234057367822001/text_to_video/092faf3c94244973ab752ee1280ba76f.mp4?spm=5176.29623064.0.0.41ed26d6cBOhV3&file=092faf3c94244973ab752ee1280ba76f.mp4\",  // 视频资源URL（必填，用于获取视频素材）\n    \"start\": 0,  // 视频素材的起始截取时间（秒，选填，默认0）\n    \"end\": 0,  // 视频素材的结束截取时间（秒，选填，默认0，0通常表示截取至视频末尾）\n    \"width\": 1080,  // 画布宽度（选填，默认1080）\n    \"height\": 1920,  // 画布高度（选填，默认1920）\n    \"draft_id\": \"draft_123456\",  // 草稿ID（选填，用于关联目标草稿）\n    \"transform_y\": 0,  // Y轴位置偏移（选填，默认0）\n    \"scale_x\": 1,  // X轴缩放比例（选填，默认1）\n    \"scale_y\": 1,  // Y轴缩放比例（选填，默认1）\n    \"transform_x\": 0,  // X轴位置偏移（选填，默认0）\n    \"speed\": 1.0,  // 视频播放速度（选填，默认1.0，大于1为加速，小于1为减速）\n    \"target_start\": 0,  // 视频在时间线上的起始位置（秒，选填，默认0）\n    \"track_name\": \"video_main\",  // 轨道名称（选填，默认\"video_main\"）\n    \"relative_index\": 0,  // 相对索引（选填，默认0，用于控制轨道内素材的排列顺序）\n    \"duration\": null,  // 视频素材的总时长（秒，选填，主动设置可以提升当前节点运行速度）\n    \"transition\": \"云朵\",  // 转场类型（选填，如\"云朵\"等，需与支持的类型匹配）\n    \"transition_duration\": 0.5,  // 转场持续时间（秒，选填，默认0.5）\n    \"volume\": 1.0,  // 视频音量（选填，默认1.0，范围通常为0.0-1.0）\n    // 蒙版相关参数\n    \"mask_type\": \"圆形\",  // 蒙版类型（选填，如圆形、矩形等）\n    \"mask_center_x\": 0.5,  // 蒙版中心X坐标（选填，默认0.5，相对屏幕宽度比例）\n    \"mask_center_y\": 0.5,  // 蒙版中心Y坐标（选填，默认0.5，相对屏幕高度比例）\n    \"mask_size\": 1.0,  // 蒙版大小（选填，默认1.0，相对屏幕高度比例）\n    \"mask_rotation\": 0.0,  // 蒙版旋转角度（选填，默认0.0度）\n    \"mask_feather\": 0.0,  // 蒙版羽化程度（选填，默认0.0，值越大边缘越柔和）\n    \"mask_invert\": false,  // 是否反转蒙版（选填，默认false）\n    \"mask_rect_width\": null,  // 矩形蒙版宽度（选填，仅mask_type为矩形时有效）\n    \"mask_round_corner\": null  // 矩形蒙版圆角（选填，仅mask_type为矩形时有效）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» video_url|body|string| 是 |视频资源URL（必填，用于获取视频素材）|
|» start|body|number| 否 |视频素材的起始截取时间（秒，选填，默认0）|
|» end|body|number| 否 |视频素材的结束截取时间（秒，选填，默认0，0通常表示截取至视频末尾）|
|» width|body|integer| 否 |画布宽度（选填，默认1080）|
|» height|body|integer| 否 |画布高度（选填，默认1920）|
|» draft_id|body|string| 否 |草稿ID（选填，用于关联目标草稿）|
|» transform_y|body|number| 否 |垂直偏移，相对值（选填，默认0）|
|» transform_y_px|body|integer| 否 |垂直偏移，像素值|
|» scale_x|body|number| 否 |X轴缩放比例（选填，默认1）|
|» scale_y|body|number| 否 |Y轴缩放比例（选填，默认1）|
|» transform_x|body|number| 否 |水平偏移，相对值（选填，默认0）|
|» transform_x_px|body|integer| 否 |水平偏移，像素值|
|» speed|body|number| 否 |视频播放速度（选填，默认1.0，大于1为加速，小于1为减速）|
|» target_start|body|number| 否 |视频在时间线上的起始位置（秒，选填，默认0）|
|» track_name|body|string| 否 |轨道名|
|» relative_index|body|integer| 否 |相对索引（选填，默认0，用于控制轨道内素材的排列顺序）|
|» duration|body|number| 否 |视频素材的总时长（秒，选填，主动设置可以提升当前节点运行速度）|
|» transition|body|string| 否 |转场动画|
|» transition_duration|body|number| 否 |转场持续时间（秒，选填，默认0.5）|
|» volume|body|number| 否 |音量（选填，单位db，默认0.0，-100表示静音）|
|» mask_type|body|string| 否 |蒙版类型（选填，如圆形、矩形等）|
|» mask_center_x|body|number| 否 |蒙版中心X坐标（选填，默认0.5，相对屏幕宽度比例）|
|» mask_center_y|body|number| 否 |蒙版中心Y坐标（选填，默认0.5，相对屏幕高度比例）|
|» mask_size|body|number| 否 |蒙版大小（选填，默认1.0，相对屏幕高度比例）|
|» mask_rotation|body|number| 否 |蒙版旋转角度（选填，默认0.0度）|
|» mask_feather|body|number| 否 |蒙版羽化程度（选填，默认0.0，值越大边缘越柔和）|
|» mask_invert|body|boolean| 否 |是否反转蒙版（选填，默认false）|
|» mask_rect_width|body|number| 否 |矩形蒙版宽度（选填，仅mask_type为矩形时有效）|
|» mask_round_corner|body|number| 否 |矩形蒙版圆角（选填，仅mask_type为矩形时有效）|
|» background_blur|body|number| 否 |背景模糊，1,2,3,4四档可选|
|» alpha|body|number| 否 |透明度，0-1|
|» flip_horizontal|body|boolean| 否 |镜像反转，默认false|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752374046_d23ea53a",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752374046_d23ea53a"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 转场

## GET get_transition_types

GET /get_transition_types

获取可用的转场动画

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "_3D空间"
    },
    {
      "name": "上移"
    },
    {
      "name": "下移"
    },
    {
      "name": "中心旋转"
    },
    {
      "name": "云朵"
    },
    {
      "name": "倒影"
    },
    {
      "name": "冰雪结晶"
    },
    {
      "name": "冲鸭"
    },
    {
      "name": "分割"
    },
    {
      "name": "分割_II"
    },
    {
      "name": "分割_III"
    },
    {
      "name": "分割_IV"
    },
    {
      "name": "前后对比_II"
    },
    {
      "name": "动漫云朵"
    },
    {
      "name": "动漫漩涡"
    },
    {
      "name": "动漫火焰"
    },
    {
      "name": "动漫闪电"
    },
    {
      "name": "压缩"
    },
    {
      "name": "叠加"
    },
    {
      "name": "叠化"
    },
    {
      "name": "右移"
    },
    {
      "name": "向上"
    },
    {
      "name": "向上擦除"
    },
    {
      "name": "向下"
    },
    {
      "name": "向下擦除"
    },
    {
      "name": "向下流动"
    },
    {
      "name": "向右"
    },
    {
      "name": "向右上"
    },
    {
      "name": "向右下"
    },
    {
      "name": "向右拉伸"
    },
    {
      "name": "向右擦除"
    },
    {
      "name": "向右流动"
    },
    {
      "name": "向左"
    },
    {
      "name": "向左上"
    },
    {
      "name": "向左下"
    },
    {
      "name": "向左拉伸"
    },
    {
      "name": "向左擦除"
    },
    {
      "name": "吸入"
    },
    {
      "name": "回忆下滑"
    },
    {
      "name": "圆形分割_II"
    },
    {
      "name": "圆形扫描"
    },
    {
      "name": "圆形遮罩"
    },
    {
      "name": "圆形遮罩_II"
    },
    {
      "name": "复古放映"
    },
    {
      "name": "岁月的痕迹"
    },
    {
      "name": "左下角_II"
    },
    {
      "name": "左移"
    },
    {
      "name": "开幕"
    },
    {
      "name": "弹幕转场"
    },
    {
      "name": "弹跳"
    },
    {
      "name": "打板转场_I"
    },
    {
      "name": "打板转场_II"
    },
    {
      "name": "抖动"
    },
    {
      "name": "抖动_II"
    },
    {
      "name": "抠像旋转"
    },
    {
      "name": "拉伸"
    },
    {
      "name": "拉伸_II"
    },
    {
      "name": "拉远"
    },
    {
      "name": "拍摄器"
    },
    {
      "name": "推近"
    },
    {
      "name": "撕纸拉屏"
    },
    {
      "name": "放射"
    },
    {
      "name": "故障"
    },
    {
      "name": "斜向分割"
    },
    {
      "name": "星星"
    },
    {
      "name": "星星_II"
    },
    {
      "name": "模糊"
    },
    {
      "name": "横向分割"
    },
    {
      "name": "横向拉幕"
    },
    {
      "name": "横线"
    },
    {
      "name": "气泡转场"
    },
    {
      "name": "水波卷动"
    },
    {
      "name": "水波向右"
    },
    {
      "name": "水波向左"
    },
    {
      "name": "泛光"
    },
    {
      "name": "泛白"
    },
    {
      "name": "波点向右"
    },
    {
      "name": "渐变擦除"
    },
    {
      "name": "滑动"
    },
    {
      "name": "漩涡"
    },
    {
      "name": "爱心"
    },
    {
      "name": "爱心_II"
    },
    {
      "name": "爱心上升"
    },
    {
      "name": "电视故障_I"
    },
    {
      "name": "电视故障_II"
    },
    {
      "name": "画笔擦除"
    },
    {
      "name": "白光快闪"
    },
    {
      "name": "白色墨花"
    },
    {
      "name": "白色烟雾"
    },
    {
      "name": "百叶窗"
    },
    {
      "name": "眨眼"
    },
    {
      "name": "矩形分割"
    },
    {
      "name": "窗格"
    },
    {
      "name": "立方体"
    },
    {
      "name": "竖向分割"
    },
    {
      "name": "竖向拉幕"
    },
    {
      "name": "竖向模糊"
    },
    {
      "name": "竖向模糊_II"
    },
    {
      "name": "竖线"
    },
    {
      "name": "箭头向右"
    },
    {
      "name": "粒子"
    },
    {
      "name": "翻篇"
    },
    {
      "name": "翻页"
    },
    {
      "name": "色差逆时针"
    },
    {
      "name": "色差顺时针"
    },
    {
      "name": "色彩溶解"
    },
    {
      "name": "色彩溶解_II"
    },
    {
      "name": "色彩溶解_III"
    },
    {
      "name": "蓝色线条"
    },
    {
      "name": "逆时针旋转"
    },
    {
      "name": "逆时针旋转_II"
    },
    {
      "name": "镜像翻转"
    },
    {
      "name": "闪白"
    },
    {
      "name": "闪白_II"
    },
    {
      "name": "闪黑"
    },
    {
      "name": "雪花故障"
    },
    {
      "name": "雾化"
    },
    {
      "name": "震动"
    },
    {
      "name": "顺时针旋转"
    },
    {
      "name": "顺时针旋转_II"
    },
    {
      "name": "频闪"
    },
    {
      "name": "风车"
    },
    {
      "name": "马赛克"
    },
    {
      "name": "黑色块"
    },
    {
      "name": "黑色烟雾"
    },
    {
      "name": "万花筒"
    },
    {
      "name": "三屏放大"
    },
    {
      "name": "三屏滑入"
    },
    {
      "name": "三屏闪切"
    },
    {
      "name": "下滑"
    },
    {
      "name": "云朵_II"
    },
    {
      "name": "亮点模糊"
    },
    {
      "name": "便利贴"
    },
    {
      "name": "信号故障"
    },
    {
      "name": "信号故障_II"
    },
    {
      "name": "倾斜拉伸"
    },
    {
      "name": "倾斜模糊"
    },
    {
      "name": "像素冲屏"
    },
    {
      "name": "光束"
    },
    {
      "name": "全息投影"
    },
    {
      "name": "六边形变焦"
    },
    {
      "name": "冲屏扭曲"
    },
    {
      "name": "几何分割"
    },
    {
      "name": "分屏下滑"
    },
    {
      "name": "前后对比"
    },
    {
      "name": "剧烈摇晃"
    },
    {
      "name": "卡片弹出"
    },
    {
      "name": "可爱爆炸"
    },
    {
      "name": "吃掉"
    },
    {
      "name": "后台切换"
    },
    {
      "name": "向上波动"
    },
    {
      "name": "向下抖动"
    },
    {
      "name": "向下拖拽"
    },
    {
      "name": "向左拉屏"
    },
    {
      "name": "向左波动"
    },
    {
      "name": "喜欢"
    },
    {
      "name": "四屏转换"
    },
    {
      "name": "回忆"
    },
    {
      "name": "回忆_II"
    },
    {
      "name": "回忆拉屏"
    },
    {
      "name": "回忆拉屏_II"
    },
    {
      "name": "圆形分割"
    },
    {
      "name": "圣诞树"
    },
    {
      "name": "复古叠影"
    },
    {
      "name": "复古放映_II"
    },
    {
      "name": "复古漏光"
    },
    {
      "name": "复古漏光_II"
    },
    {
      "name": "复古胶片"
    },
    {
      "name": "多层环形"
    },
    {
      "name": "多屏定格"
    },
    {
      "name": "大圆盘"
    },
    {
      "name": "射灯"
    },
    {
      "name": "小喇叭"
    },
    {
      "name": "小恶魔"
    },
    {
      "name": "幻影"
    },
    {
      "name": "幻觉"
    },
    {
      "name": "开心"
    },
    {
      "name": "弹出"
    },
    {
      "name": "弹动发光"
    },
    {
      "name": "彩色像素"
    },
    {
      "name": "微抖动"
    },
    {
      "name": "心形叠化"
    },
    {
      "name": "快速缩放"
    },
    {
      "name": "快门"
    },
    {
      "name": "扫光"
    },
    {
      "name": "扭曲溶解"
    },
    {
      "name": "扭转弹动"
    },
    {
      "name": "抖动放大"
    },
    {
      "name": "抖动缩小"
    },
    {
      "name": "抖动缩小__II"
    },
    {
      "name": "抽象前景"
    },
    {
      "name": "抽象前景_II"
    },
    {
      "name": "拉开"
    },
    {
      "name": "拉框入屏"
    },
    {
      "name": "拍摄器_II"
    },
    {
      "name": "拍摄器_III"
    },
    {
      "name": "推近_II"
    },
    {
      "name": "推远_II"
    },
    {
      "name": "摄像机"
    },
    {
      "name": "摇晃描边"
    },
    {
      "name": "摇晃震动"
    },
    {
      "name": "摇镜"
    },
    {
      "name": "撕纸"
    },
    {
      "name": "撕纸掉落"
    },
    {
      "name": "收缩抖动"
    },
    {
      "name": "放大左移"
    },
    {
      "name": "放大镜"
    },
    {
      "name": "故障模糊"
    },
    {
      "name": "数字矩阵"
    },
    {
      "name": "斜向模糊"
    },
    {
      "name": "斜向闪光"
    },
    {
      "name": "斜线翻页"
    },
    {
      "name": "新篇章"
    },
    {
      "name": "新篇章_II"
    },
    {
      "name": "方形分割"
    },
    {
      "name": "方形模糊"
    },
    {
      "name": "方形模糊_II"
    },
    {
      "name": "旋焦"
    },
    {
      "name": "旋转圆球"
    },
    {
      "name": "旋转圆盘"
    },
    {
      "name": "旋转圆盘_II"
    },
    {
      "name": "旋转快门"
    },
    {
      "name": "旋转拨盘"
    },
    {
      "name": "旋转模糊"
    },
    {
      "name": "旋转穿越"
    },
    {
      "name": "旋转纵深"
    },
    {
      "name": "旋转翻页"
    },
    {
      "name": "旋转震动"
    },
    {
      "name": "无限穿越_I"
    },
    {
      "name": "无限穿越_II"
    },
    {
      "name": "旧胶片"
    },
    {
      "name": "旧胶片_II"
    },
    {
      "name": "时光穿梭"
    },
    {
      "name": "星光"
    },
    {
      "name": "星光叠化"
    },
    {
      "name": "星星_III"
    },
    {
      "name": "星星吸入"
    },
    {
      "name": "星星模糊"
    },
    {
      "name": "春日光斑"
    },
    {
      "name": "暧昧光晕"
    },
    {
      "name": "曝光拉丝"
    },
    {
      "name": "曝光摇镜"
    },
    {
      "name": "未来光谱"
    },
    {
      "name": "未来光谱II"
    },
    {
      "name": "条形模糊"
    },
    {
      "name": "模糊放大"
    },
    {
      "name": "模糊缩小"
    },
    {
      "name": "横条挤压"
    },
    {
      "name": "横移模糊"
    },
    {
      "name": "水墨"
    },
    {
      "name": "水滴"
    },
    {
      "name": "水滴_II"
    },
    {
      "name": "水滴_III"
    },
    {
      "name": "汇聚"
    },
    {
      "name": "泡泡模糊"
    },
    {
      "name": "波光粼粼"
    },
    {
      "name": "波动"
    },
    {
      "name": "波动_II"
    },
    {
      "name": "波动故障"
    },
    {
      "name": "流光"
    },
    {
      "name": "涂鸦放大"
    },
    {
      "name": "溶解推进"
    },
    {
      "name": "滑动弹出"
    },
    {
      "name": "滑动放大"
    },
    {
      "name": "滑块拼贴"
    },
    {
      "name": "漩涡扭曲"
    },
    {
      "name": "炫光"
    },
    {
      "name": "炫光_II"
    },
    {
      "name": "炫光_III"
    },
    {
      "name": "炫光弹动"
    },
    {
      "name": "炫光扫描"
    },
    {
      "name": "炸弹"
    },
    {
      "name": "烟雾弹"
    },
    {
      "name": "热成像"
    },
    {
      "name": "燃烧"
    },
    {
      "name": "燃烧_II"
    },
    {
      "name": "燃烧_III"
    },
    {
      "name": "爆米花"
    },
    {
      "name": "爆闪"
    },
    {
      "name": "爆闪_II"
    },
    {
      "name": "爱心冲击"
    },
    {
      "name": "爱心模糊"
    },
    {
      "name": "爱心气球"
    },
    {
      "name": "环形色散"
    },
    {
      "name": "玻璃破碎"
    },
    {
      "name": "玻璃破碎_II"
    },
    {
      "name": "珠光模糊"
    },
    {
      "name": "生气"
    },
    {
      "name": "电光"
    },
    {
      "name": "电光_II"
    },
    {
      "name": "百叶窗_II"
    },
    {
      "name": "相片切换"
    },
    {
      "name": "相片拼贴"
    },
    {
      "name": "空间弹动"
    },
    {
      "name": "空间弹动_II"
    },
    {
      "name": "空间弹动_III"
    },
    {
      "name": "空间弹动_IV"
    },
    {
      "name": "空间旋转"
    },
    {
      "name": "空间旋转_II"
    },
    {
      "name": "空间旋转_III"
    },
    {
      "name": "空间翻转"
    },
    {
      "name": "空间翻转_II"
    },
    {
      "name": "空间跳跃"
    },
    {
      "name": "穿越"
    },
    {
      "name": "穿越_II"
    },
    {
      "name": "穿越_III"
    },
    {
      "name": "立体翻转"
    },
    {
      "name": "立体翻页"
    },
    {
      "name": "立体翻页_II"
    },
    {
      "name": "竖向拉伸"
    },
    {
      "name": "竖移模糊"
    },
    {
      "name": "粉色反转片"
    },
    {
      "name": "纸团"
    },
    {
      "name": "翻转冲屏"
    },
    {
      "name": "翻页_II"
    },
    {
      "name": "聚光灯"
    },
    {
      "name": "胶片定格"
    },
    {
      "name": "胶片擦除"
    },
    {
      "name": "胶片融化"
    },
    {
      "name": "胶片闪光"
    },
    {
      "name": "色块故障"
    },
    {
      "name": "色差故障"
    },
    {
      "name": "色彩溶解_IV"
    },
    {
      "name": "色彩溶解_V"
    },
    {
      "name": "色散晃镜"
    },
    {
      "name": "色散闪烁"
    },
    {
      "name": "色散闪烁_II"
    },
    {
      "name": "荧光爆闪"
    },
    {
      "name": "菱格翻转"
    },
    {
      "name": "蓝光扫描"
    },
    {
      "name": "蓝色反转片"
    },
    {
      "name": "融化"
    },
    {
      "name": "融化_II"
    },
    {
      "name": "负片下滑"
    },
    {
      "name": "超赞"
    },
    {
      "name": "透镜故障"
    },
    {
      "name": "重叠上滑"
    },
    {
      "name": "金色光斑"
    },
    {
      "name": "钱兔无量"
    },
    {
      "name": "长曝光"
    },
    {
      "name": "闪光灯"
    },
    {
      "name": "闪光灯_II"
    },
    {
      "name": "闪光灯_III"
    },
    {
      "name": "闪动光斑"
    },
    {
      "name": "闪动光斑_II"
    },
    {
      "name": "闪回"
    },
    {
      "name": "闪屏故障"
    },
    {
      "name": "闪黑_II"
    },
    {
      "name": "闹钟"
    },
    {
      "name": "雪雾"
    },
    {
      "name": "震动_II"
    },
    {
      "name": "震动缩小"
    },
    {
      "name": "霓虹闪光"
    },
    {
      "name": "霓虹闪光_II"
    },
    {
      "name": "飘雪"
    },
    {
      "name": "飘雪_II"
    },
    {
      "name": "马赛克_II"
    },
    {
      "name": "鱼眼"
    },
    {
      "name": "鱼眼_II"
    },
    {
      "name": "鱼眼_III"
    },
    {
      "name": "黑白摇镜"
    },
    {
      "name": "黑色反转片"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 蒙版

## GET get_mask_types

GET /get_mask_types

获取支持的蒙版

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "线性"
    },
    {
      "name": "镜面"
    },
    {
      "name": "圆形"
    },
    {
      "name": "矩形"
    },
    {
      "name": "爱心"
    },
    {
      "name": "星形"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 音频

## GET get_audio_effect_types

GET /get_audio_effect_types

获取可用音频特效

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "台湾小哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "圣诞精灵",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "圣诞老人",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "广告男声",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "港普男声",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "老婆婆",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "解说小帅",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "大叔",
      "params": [
        {
          "default_value": 83.39999999999999,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "女生",
      "params": [
        {
          "default_value": 83.39999999999999,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 33.4,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "怪物",
      "params": [
        {
          "default_value": 65,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 78,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "机器人",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "男生",
      "params": [
        {
          "default_value": 37.5,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 25,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "花栗鼠",
      "params": [
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "萝莉",
      "params": [
        {
          "default_value": 75,
          "max_value": 100,
          "min_value": 0,
          "name": "音调"
        },
        {
          "default_value": 60,
          "max_value": 100,
          "min_value": 0,
          "name": "音色"
        }
      ],
      "type": "Tone"
    },
    {
      "name": "TVB女声",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "东厂公公",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "云龙哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "侠客",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "做作夹子音",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "八戒",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "军事解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "动漫小新",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "动漫海绵",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "咆哮哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "商务殷语",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "四郎",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "太白",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "如来佛祖",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "姜饼人",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "容嬷嬷",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "小孩",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "强势妹",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "快板",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "恐怖电影",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "悬疑解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "懒小羊",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "搞笑解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "文艺女声",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "樱桃丸子",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "樱花小哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "武则天",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "沉稳解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "温柔姐姐",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "熊二",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "猴哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "甜美悦悦",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "生活小妙招",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "电竞解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "电视广告",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "紫薇",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "舌尖解说",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "蜡笔小妮",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "语音助手",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "那姐",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "锤子哥",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "顾姐",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "黛玉",
      "params": [],
      "type": "Tone"
    },
    {
      "name": "_8bit",
      "params": [
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_pitch_shift"
        },
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_timbre"
        },
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "低保真",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "合成器",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "回音",
      "params": [
        {
          "default_value": 80,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_quantity"
        },
        {
          "default_value": 76.2,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "扩音器",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "水下",
      "params": [
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "深度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "没电了",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "环绕音",
      "params": [
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_center_position"
        },
        {
          "default_value": 50,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_surrounding_frequency"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "电音",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "颤音",
      "params": [
        {
          "default_value": 71.39999999999999,
          "max_value": 100,
          "min_value": 0,
          "name": "频率"
        },
        {
          "default_value": 90.5,
          "max_value": 100,
          "min_value": 0,
          "name": "幅度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "麦霸",
      "params": [
        {
          "default_value": 5.2,
          "max_value": 100,
          "min_value": 0,
          "name": "空间大小"
        },
        {
          "default_value": 45,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "黑胶",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "噪点"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "_3d环绕音",
      "params": [
        {
          "default_value": 0,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "Autotune",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "下雨",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "乡村大喇叭",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "人声增强",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "低音增强",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "change_voice_param_strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "停车场",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "冰川之下",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "刮风",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "噪音混响",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "地狱",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "复古收音机",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "失真电子",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "对讲机",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "房间",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "捂嘴",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "教堂",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "教室",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "机器人2",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "沙漠",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "派对",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "深海回声",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "电话",
      "params": [
        {
          "default_value": 70,
          "max_value": 100,
          "min_value": 0,
          "name": "强弱"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "留声机",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "百老汇",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "空灵感",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "空谷回声",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "老式电话",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "言灵术",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "豪宅回声",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "强度"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "迷幻电子",
      "params": [
        {
          "default_value": 100,
          "max_value": 100,
          "min_value": 0,
          "name": "strength"
        },
        {
          "default_value": 74.3,
          "max_value": 100,
          "min_value": 0,
          "name": "noise"
        }
      ],
      "type": "Audio_scene"
    },
    {
      "name": "Lofi",
      "params": [],
      "type": "Speech_to_song"
    },
    {
      "name": "民谣",
      "params": [],
      "type": "Speech_to_song"
    },
    {
      "name": "嘻哈",
      "params": [],
      "type": "Speech_to_song"
    },
    {
      "name": "爵士",
      "params": [],
      "type": "Speech_to_song"
    },
    {
      "name": "节奏蓝调",
      "params": [],
      "type": "Speech_to_song"
    },
    {
      "name": "雷鬼",
      "params": [],
      "type": "Speech_to_song"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|»» params|[object]|true|none||none|
|»»» default_value|number|true|none||none|
|»»» max_value|integer|true|none||none|
|»»» min_value|integer|true|none||none|
|»»» name|string|true|none||none|
|»» type|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_audio

POST /add_audio

添加音频

> Body 请求参数

```json
"{\n  \"audio_url\": \"https://lf3-lv-music-tos.faceu.com/obj/tos-cn-ve-2774/oYACBQRCMlWBIrZipvQZhI5LAlUFYii0RwEPh\",  // 音频文件URL（必填）\n  \"start\": 0,  // 音频素材的起始截取时间（秒，默认0）\n  \"end\": 30,  // 音频素材的结束截取时间（秒，可选，默认取完整音频长度）\n  \"draft_id\": \"your_draft_id\",  // 草稿ID（可选，用于指定操作的草稿）\n  \"volume\": 0.8,  // 音量大小（默认1.0）\n  \"target_start\": 5,  // 音频在时间线上的起始位置（秒，默认0）\n  \"speed\": 1.2,  // 音频速度（默认1.0，>1加速，<1减速）\n  \"track_name\": \"audio_background\",  // 轨道名称（默认\"audio_main\"）\n  \"duration\": 20,  // 音频素材的总时长（秒）主动设置可以提升请求速度\n  \"effect_type\": \"回音\",  // 音效类型\n  \"effect_params\": [45],  // 音效参数（可选，根据effect_type设置）\n  \"width\": 1080,  // 视频宽度（默认1080）\n  \"height\": 1920  // 视频高度（默认1920）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» audio_url|body|string| 是 |音频文件URL（必填）|
|» start|body|number| 否 |音频素材的起始截取时间（秒，默认0）|
|» end|body|number| 否 |音频素材的结束截取时间（秒，可选，默认取完整音频长度）|
|» draft_id|body|string| 否 |草稿ID（可选，用于指定操作的草稿）|
|» volume|body|number| 否 |音量（选填，单位db，默认0.0，-100表示静音）|
|» target_start|body|number| 否 |音频在时间线上的起始位置（秒，默认0）|
|» speed|body|number| 否 |音频速度（默认1.0，>1加速，<1减速）|
|» track_name|body|string| 否 |轨道名称|
|» duration|body|number| 否 |音频素材的总时长（秒）主动设置可以提升请求速度|
|» effect_type|body|string| 否 |音效类型|
|» effect_params|body|[integer]| 否 |音效参数（可选，根据effect_type设置）|
|» width|body|integer| 否 |视频宽度（默认1080）|
|» height|body|integer| 否 |视频高度（默认1920）|
|» fade_in_duration|body|number| 否 |淡入时间，单位秒|
|» fade_out_duratioin|body|number| 否 |淡出时间，单位秒|

> 返回示例

> 200 Response

```json
{
  "error": "string",
  "output": {
    "draft_id": "string",
    "draft_url": "string"
  },
  "purchase_link": "string",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 关键帧

## POST add_video_keyframe

POST /add_video_keyframe

向指定轨道添加关键帧，支持批量操作

> Body 请求参数

```json
"{\n    \"draft_id\": \"dfd_cat_1753709045_3a033ea7\",  // 草稿ID（必填，指定要操作的草稿）\n    \"track_name\": \"video_main\",  // 轨道名称（选填，默认\"video_main\"，指定要添加关键帧的轨道）\n    \n    // 单个关键帧参数（向后兼容，用于添加单个关键帧）\n    \"property_type\": \"alpha\",  // 属性类型（选填，默认\"alpha\"不透明度，可选值如\"scale_x\"、\"rotation\"等）\n    \"time\": 0.0,  // 关键帧时间（秒，选填，默认0.0秒）\n    \"value\": \"1.0\",  // 属性值（选填，默认\"1.0\"，需根据property_type调整类型，如数字、字符串等）\n    \n    // 批量关键帧参数（新增，优先使用，用于一次性添加多个关键帧）\n    \"property_types\": [\"alpha\", \"scale_x\"],  // 属性类型列表（选填，数组形式，与times、values对应）\n    \"times\": [0.0, 2.0],  // 时间列表（选填，数组形式，与property_types、values对应）\n    \"values\": [\"1.0\", \"0.8\"]  // 值列表（选填，数组形式，与property_types、times对应）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» draft_id|body|string| 是 |草稿ID（必填，指定要操作的草稿）|
|» track_name|body|string| 否 |目标轨道名称|
|» property_type|body|string| 否 |属性类型|
|» time|body|number| 否 |关键帧时间戳（秒，选填，默认0.0秒）|
|» value|body|string| 否 |属性值|
|» property_types|body|[string]| 否 |属性类型列表，优先使用（数组，与times、values对应）|
|» times|body|[number]| 否 |时间列表（数组，与property_types、values对应）|
|» values|body|[string]| 否 |值列表（数组，与property_types、times对应）|

#### 枚举值

|属性|值|
|---|---|
|» property_type|position_x|
|» property_type|position_y|
|» property_type|position_x_px|
|» property_type|position_y_px|
|» property_type|mask_position_x|
|» property_type|mask_positioin_y|
|» property_type|mask_position_x_px|
|» property_type|mask_positioin_y_px|
|» property_type|mask_size_x|
|» property_type|mask_size_y|
|» property_type|rotation|
|» property_type|scale_x|
|» property_type|scale_y|
|» property_type|uniform_scale|
|» property_type|alpha|
|» property_type|saturation|
|» property_type|contrast|
|» property_type|brightness|
|» property_type|volume|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "added_keyframes_count": 2,
    "draft_id": "dfd_cat_1752374660_d5424447",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752374660_d5424447"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» added_keyframes_count|integer|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 特效

## GET get_video_character_effect_types

GET /get_video_character_effect_types

获取可用的人物特效列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "BOOM"
    },
    {
      "name": "X"
    },
    {
      "name": "crash"
    },
    {
      "name": "中刀"
    },
    {
      "name": "主体冲破屏幕"
    },
    {
      "name": "九尾狐"
    },
    {
      "name": "人影爆闪"
    },
    {
      "name": "光环_I"
    },
    {
      "name": "光环_II"
    },
    {
      "name": "几何拖尾_I"
    },
    {
      "name": "几何拖尾_II"
    },
    {
      "name": "击中"
    },
    {
      "name": "分头行动"
    },
    {
      "name": "分身"
    },
    {
      "name": "动感爱心"
    },
    {
      "name": "卡通脸"
    },
    {
      "name": "变身"
    },
    {
      "name": "可爱女生"
    },
    {
      "name": "可爱猪"
    },
    {
      "name": "吻痕坏笑"
    },
    {
      "name": "哈哈哈"
    },
    {
      "name": "图腾"
    },
    {
      "name": "圣诞小熊"
    },
    {
      "name": "圣诞帽"
    },
    {
      "name": "圣诞树"
    },
    {
      "name": "圣诞胡子"
    },
    {
      "name": "圣诞辣妹"
    },
    {
      "name": "圣诞铃铛"
    },
    {
      "name": "声波"
    },
    {
      "name": "多屏圣诞树"
    },
    {
      "name": "大头"
    },
    {
      "name": "大眼睛"
    },
    {
      "name": "天使环"
    },
    {
      "name": "太阳神"
    },
    {
      "name": "好吃"
    },
    {
      "name": "妖气"
    },
    {
      "name": "委屈丑丑脸"
    },
    {
      "name": "害羞"
    },
    {
      "name": "小恶魔"
    },
    {
      "name": "小鹿角"
    },
    {
      "name": "尴尬住了"
    },
    {
      "name": "局部扭曲"
    },
    {
      "name": "局部马赛克"
    },
    {
      "name": "巴哥犬"
    },
    {
      "name": "帅气男生"
    },
    {
      "name": "幻影_I"
    },
    {
      "name": "幽灵"
    },
    {
      "name": "弥散流光"
    },
    {
      "name": "彩色负片"
    },
    {
      "name": "彩色重影"
    },
    {
      "name": "微笑摇摆头"
    },
    {
      "name": "心动"
    },
    {
      "name": "心动信号"
    },
    {
      "name": "心心眼"
    },
    {
      "name": "恶灵骑士"
    },
    {
      "name": "恶魔印记"
    },
    {
      "name": "恶魔尾巴"
    },
    {
      "name": "恶魔角"
    },
    {
      "name": "惨"
    },
    {
      "name": "意识流"
    },
    {
      "name": "憔悴"
    },
    {
      "name": "懵"
    },
    {
      "name": "我不听"
    },
    {
      "name": "我服了"
    },
    {
      "name": "打击"
    },
    {
      "name": "打脸"
    },
    {
      "name": "扫描_II"
    },
    {
      "name": "拼贴抽帧"
    },
    {
      "name": "拼贴风暴"
    },
    {
      "name": "拽酷红眼"
    },
    {
      "name": "掉小珍珠啦"
    },
    {
      "name": "故障描边_I"
    },
    {
      "name": "敲打"
    },
    {
      "name": "新年星黛露"
    },
    {
      "name": "无信号"
    },
    {
      "name": "星光放射"
    },
    {
      "name": "星星拖尾"
    },
    {
      "name": "未来眼镜"
    },
    {
      "name": "机械几何"
    },
    {
      "name": "机械姬_I"
    },
    {
      "name": "机械姬_II"
    },
    {
      "name": "机灵怪"
    },
    {
      "name": "欧美女性"
    },
    {
      "name": "欧美男性"
    },
    {
      "name": "气泡_I"
    },
    {
      "name": "气泡_II"
    },
    {
      "name": "气波"
    },
    {
      "name": "沉沦"
    },
    {
      "name": "流光描边"
    },
    {
      "name": "流口水"
    },
    {
      "name": "漩涡"
    },
    {
      "name": "潮流入侵"
    },
    {
      "name": "潮酷女孩"
    },
    {
      "name": "潮酷男孩"
    },
    {
      "name": "激光几何"
    },
    {
      "name": "火焰拖尾"
    },
    {
      "name": "火焰环绕"
    },
    {
      "name": "火焰翅膀_I"
    },
    {
      "name": "火焰翅膀_II"
    },
    {
      "name": "灵机一动"
    },
    {
      "name": "灵魂出走"
    },
    {
      "name": "爱心光波"
    },
    {
      "name": "爱心焰火"
    },
    {
      "name": "猩猩脸"
    },
    {
      "name": "猫耳女孩"
    },
    {
      "name": "电光描边"
    },
    {
      "name": "电光放射"
    },
    {
      "name": "电击"
    },
    {
      "name": "电子屏故障"
    },
    {
      "name": "真的会谢"
    },
    {
      "name": "真香"
    },
    {
      "name": "破碎的心"
    },
    {
      "name": "神明少女"
    },
    {
      "name": "科技氛围_I"
    },
    {
      "name": "科技氛围_III"
    },
    {
      "name": "秘密"
    },
    {
      "name": "箭头环绕"
    },
    {
      "name": "粉色便便"
    },
    {
      "name": "背景拖影"
    },
    {
      "name": "背景氛围II"
    },
    {
      "name": "脸红"
    },
    {
      "name": "脸绿了"
    },
    {
      "name": "脸部故障"
    },
    {
      "name": "舞者"
    },
    {
      "name": "舞者_II"
    },
    {
      "name": "萤火"
    },
    {
      "name": "虚拟人生_I"
    },
    {
      "name": "虚拟人生_II"
    },
    {
      "name": "衰"
    },
    {
      "name": "视线遮挡"
    },
    {
      "name": "赛博朋克_I"
    },
    {
      "name": "赛博朋克_II"
    },
    {
      "name": "赛博眼镜"
    },
    {
      "name": "轻金属"
    },
    {
      "name": "运动轨迹"
    },
    {
      "name": "迷茫"
    },
    {
      "name": "闪影"
    },
    {
      "name": "闪烁"
    },
    {
      "name": "闪电炸裂"
    },
    {
      "name": "阳光"
    },
    {
      "name": "阴云密布"
    },
    {
      "name": "阴暗面"
    },
    {
      "name": "难吃"
    },
    {
      "name": "难过"
    },
    {
      "name": "雪花眼泪"
    },
    {
      "name": "霓虹特技"
    },
    {
      "name": "音符拖尾"
    },
    {
      "name": "音符拖尾_II"
    },
    {
      "name": "飓风"
    },
    {
      "name": "飞翔的帽子"
    },
    {
      "name": "鬼火"
    },
    {
      "name": "黑人女孩"
    },
    {
      "name": "黑人男生"
    },
    {
      "name": "_3D兔兔"
    },
    {
      "name": "Love_u"
    },
    {
      "name": "X瞬移"
    },
    {
      "name": "分身_III"
    },
    {
      "name": "分身ll"
    },
    {
      "name": "发光分身"
    },
    {
      "name": "变老美颜"
    },
    {
      "name": "可爱龙龙"
    },
    {
      "name": "嘻哈眼镜"
    },
    {
      "name": "天使"
    },
    {
      "name": "天使翅膀"
    },
    {
      "name": "奇行种"
    },
    {
      "name": "局部模糊"
    },
    {
      "name": "幻彩流光"
    },
    {
      "name": "幻影平移"
    },
    {
      "name": "彩虹流体"
    },
    {
      "name": "彩虹边缘"
    },
    {
      "name": "影分身"
    },
    {
      "name": "恶魔之翼"
    },
    {
      "name": "情绪定格"
    },
    {
      "name": "我太可爱了"
    },
    {
      "name": "我爱了"
    },
    {
      "name": "我麻了"
    },
    {
      "name": "手写描边"
    },
    {
      "name": "捕梦"
    },
    {
      "name": "旋转分身"
    },
    {
      "name": "无限穿越"
    },
    {
      "name": "有事吗"
    },
    {
      "name": "机械环绕_I"
    },
    {
      "name": "机械环绕_II"
    },
    {
      "name": "梦境"
    },
    {
      "name": "气炸了"
    },
    {
      "name": "波点分身"
    },
    {
      "name": "流体故障"
    },
    {
      "name": "漩涡溶解"
    },
    {
      "name": "火焰图腾"
    },
    {
      "name": "点赞"
    },
    {
      "name": "热力光谱_I"
    },
    {
      "name": "热力光谱_II"
    },
    {
      "name": "焰火"
    },
    {
      "name": "熬夜冠军"
    },
    {
      "name": "爱心"
    },
    {
      "name": "爱心发射"
    },
    {
      "name": "爱心泡泡"
    },
    {
      "name": "爱心眼"
    },
    {
      "name": "爱心美瞳"
    },
    {
      "name": "狱火"
    },
    {
      "name": "生气"
    },
    {
      "name": "电光描边_II"
    },
    {
      "name": "电光灼烧"
    },
    {
      "name": "电光眼"
    },
    {
      "name": "电光耳机"
    },
    {
      "name": "眼神光"
    },
    {
      "name": "瞬移"
    },
    {
      "name": "碎片分身"
    },
    {
      "name": "碎闪边缘"
    },
    {
      "name": "科技氛围_II"
    },
    {
      "name": "移形回位"
    },
    {
      "name": "移形幻影_I"
    },
    {
      "name": "移形幻影_II"
    },
    {
      "name": "空气流体"
    },
    {
      "name": "笑哭"
    },
    {
      "name": "粒子弥散"
    },
    {
      "name": "美味召唤"
    },
    {
      "name": "蝴蝶翅膀"
    },
    {
      "name": "轮廓扫描"
    },
    {
      "name": "迷幻分身"
    },
    {
      "name": "金币掉落"
    },
    {
      "name": "镭射眼_I"
    },
    {
      "name": "镭射眼_II"
    },
    {
      "name": "闪电"
    },
    {
      "name": "闪电环绕"
    },
    {
      "name": "闪电眼"
    },
    {
      "name": "霓虹爱心"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## GET get_video_scene_effect_types

GET /get_video_scene_effect_types

获取可用的场景特效列表

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": [
    {
      "name": "_1998"
    },
    {
      "name": "_70s"
    },
    {
      "name": "_90s画质"
    },
    {
      "name": "DV录制框"
    },
    {
      "name": "DV界面"
    },
    {
      "name": "I_Lose_You"
    },
    {
      "name": "I_Love_You"
    },
    {
      "name": "JVC"
    },
    {
      "name": "List边框"
    },
    {
      "name": "MV封面"
    },
    {
      "name": "New_Year"
    },
    {
      "name": "PS边框"
    },
    {
      "name": "RGB描边"
    },
    {
      "name": "VCR"
    },
    {
      "name": "X_Signal"
    },
    {
      "name": "X开幕"
    },
    {
      "name": "betamax"
    },
    {
      "name": "emoji钻石"
    },
    {
      "name": "ins界面"
    },
    {
      "name": "ins风放大镜"
    },
    {
      "name": "kirakira"
    },
    {
      "name": "ktv灯光"
    },
    {
      "name": "ktv灯光_II"
    },
    {
      "name": "windows弹窗关闭"
    },
    {
      "name": "windows弹窗打开"
    },
    {
      "name": "丁达尔光线"
    },
    {
      "name": "万圣emoji"
    },
    {
      "name": "万圣夜"
    },
    {
      "name": "三屏"
    },
    {
      "name": "三格漫画"
    },
    {
      "name": "下雨"
    },
    {
      "name": "不对劲"
    },
    {
      "name": "不规则黑框"
    },
    {
      "name": "两屏"
    },
    {
      "name": "中枪了"
    },
    {
      "name": "乌鸦飞过"
    },
    {
      "name": "九屏"
    },
    {
      "name": "九屏跑马灯"
    },
    {
      "name": "亮片"
    },
    {
      "name": "人鱼滤镜"
    },
    {
      "name": "仙女变身"
    },
    {
      "name": "仙女变身_II"
    },
    {
      "name": "仙女棒"
    },
    {
      "name": "仙尘闪闪"
    },
    {
      "name": "低像素"
    },
    {
      "name": "低像素_II"
    },
    {
      "name": "倒计时_II"
    },
    {
      "name": "像素画"
    },
    {
      "name": "像素纹理"
    },
    {
      "name": "光斑虚化"
    },
    {
      "name": "光斑飘落"
    },
    {
      "name": "光晕"
    },
    {
      "name": "光晕_II"
    },
    {
      "name": "全剧终"
    },
    {
      "name": "六屏"
    },
    {
      "name": "关月亮"
    },
    {
      "name": "冰冷实验室"
    },
    {
      "name": "冰霜"
    },
    {
      "name": "冰霜_II"
    },
    {
      "name": "冲击波"
    },
    {
      "name": "冲刺"
    },
    {
      "name": "冲刺_II"
    },
    {
      "name": "冲刺_III"
    },
    {
      "name": "冲屏闪粉"
    },
    {
      "name": "凄凉"
    },
    {
      "name": "几何图形"
    },
    {
      "name": "刀光剑影"
    },
    {
      "name": "分屏开幕"
    },
    {
      "name": "初雪_I"
    },
    {
      "name": "加载甜蜜"
    },
    {
      "name": "动感模糊"
    },
    {
      "name": "动感荧光"
    },
    {
      "name": "动感蓝带"
    },
    {
      "name": "单色涂鸦"
    },
    {
      "name": "南瓜光斑"
    },
    {
      "name": "南瓜笑脸"
    },
    {
      "name": "卷动"
    },
    {
      "name": "原相机"
    },
    {
      "name": "友友商店"
    },
    {
      "name": "反转片_I"
    },
    {
      "name": "发光"
    },
    {
      "name": "取景框"
    },
    {
      "name": "取景框_II"
    },
    {
      "name": "变形了"
    },
    {
      "name": "变彩色"
    },
    {
      "name": "变清晰"
    },
    {
      "name": "变清晰_II"
    },
    {
      "name": "变焦推镜"
    },
    {
      "name": "变秋天"
    },
    {
      "name": "变黑白"
    },
    {
      "name": "告白氛围"
    },
    {
      "name": "咔嚓"
    },
    {
      "name": "哈哈弹幕"
    },
    {
      "name": "哈苏胶片"
    },
    {
      "name": "唱片"
    },
    {
      "name": "唱片封面"
    },
    {
      "name": "啊啊啊啊"
    },
    {
      "name": "噪点"
    },
    {
      "name": "四屏"
    },
    {
      "name": "回弹摇摆"
    },
    {
      "name": "回忆文件夹"
    },
    {
      "name": "回忆胶片"
    },
    {
      "name": "圆形虚线放大镜"
    },
    {
      "name": "圣诞光斑"
    },
    {
      "name": "圣诞星光"
    },
    {
      "name": "地狱使者"
    },
    {
      "name": "基础黑框"
    },
    {
      "name": "塑料封面"
    },
    {
      "name": "塑料封面_II"
    },
    {
      "name": "塑料封面III"
    },
    {
      "name": "复古DV"
    },
    {
      "name": "复古DV_II"
    },
    {
      "name": "复古DV_III"
    },
    {
      "name": "复古DV_IV"
    },
    {
      "name": "复古发光"
    },
    {
      "name": "复古多格"
    },
    {
      "name": "复古弹窗_I"
    },
    {
      "name": "复古弹窗_II"
    },
    {
      "name": "复古漫画"
    },
    {
      "name": "复古甜心"
    },
    {
      "name": "复古碎钻"
    },
    {
      "name": "复古蓝调"
    },
    {
      "name": "夏日冰块"
    },
    {
      "name": "夏日泡泡_I"
    },
    {
      "name": "夕阳"
    },
    {
      "name": "夕阳_II"
    },
    {
      "name": "夕阳_III"
    },
    {
      "name": "夜蝶"
    },
    {
      "name": "夜视框"
    },
    {
      "name": "大雪"
    },
    {
      "name": "大雪纷飞"
    },
    {
      "name": "天使光"
    },
    {
      "name": "天使降临"
    },
    {
      "name": "失焦"
    },
    {
      "name": "夸夸弹幕"
    },
    {
      "name": "夺冠"
    },
    {
      "name": "孔明灯"
    },
    {
      "name": "孔明灯_II"
    },
    {
      "name": "字幕投影"
    },
    {
      "name": "字幕投影_II"
    },
    {
      "name": "字幕投影_III"
    },
    {
      "name": "字幕投影_IV"
    },
    {
      "name": "定格闪烁"
    },
    {
      "name": "小剧场"
    },
    {
      "name": "小动物"
    },
    {
      "name": "小花花"
    },
    {
      "name": "少女心"
    },
    {
      "name": "少女心事"
    },
    {
      "name": "少女星闪"
    },
    {
      "name": "左右摇晃"
    },
    {
      "name": "布拉格"
    },
    {
      "name": "幻彩文字"
    },
    {
      "name": "幻影"
    },
    {
      "name": "幻影_II"
    },
    {
      "name": "幻术摇摆"
    },
    {
      "name": "幻觉"
    },
    {
      "name": "广角"
    },
    {
      "name": "庆祝彩带"
    },
    {
      "name": "开幕"
    },
    {
      "name": "开幕__II"
    },
    {
      "name": "强锐化"
    },
    {
      "name": "录像带"
    },
    {
      "name": "录像带_II"
    },
    {
      "name": "录像带_III"
    },
    {
      "name": "录制框"
    },
    {
      "name": "录制边框"
    },
    {
      "name": "录制边框_II"
    },
    {
      "name": "录制边框_III"
    },
    {
      "name": "录制边框_IIII"
    },
    {
      "name": "彩信"
    },
    {
      "name": "彩噪画质"
    },
    {
      "name": "彩带"
    },
    {
      "name": "彩色描边"
    },
    {
      "name": "彩色漫画"
    },
    {
      "name": "彩色负片"
    },
    {
      "name": "彩虹光"
    },
    {
      "name": "彩虹光_II"
    },
    {
      "name": "彩虹光晕"
    },
    {
      "name": "彩虹射线"
    },
    {
      "name": "彩虹幻影"
    },
    {
      "name": "彩虹气泡"
    },
    {
      "name": "彩虹爱心"
    },
    {
      "name": "彩钻"
    },
    {
      "name": "心河"
    },
    {
      "name": "心跳"
    },
    {
      "name": "心跳黑框"
    },
    {
      "name": "必杀技"
    },
    {
      "name": "必杀技_II"
    },
    {
      "name": "怀旧边框"
    },
    {
      "name": "怀旧边框_II"
    },
    {
      "name": "怦然心动"
    },
    {
      "name": "恐怖故事"
    },
    {
      "name": "恐怖故事_II"
    },
    {
      "name": "恐怖故事_III"
    },
    {
      "name": "恐怖综艺"
    },
    {
      "name": "恶灵冲屏"
    },
    {
      "name": "愛"
    },
    {
      "name": "我酸了"
    },
    {
      "name": "手帐边框"
    },
    {
      "name": "手电筒"
    },
    {
      "name": "手绘拍摄器"
    },
    {
      "name": "手绘边框_II"
    },
    {
      "name": "扫描光条"
    },
    {
      "name": "抖动"
    },
    {
      "name": "折痕"
    },
    {
      "name": "折痕_II"
    },
    {
      "name": "折痕_III"
    },
    {
      "name": "折痕_IV"
    },
    {
      "name": "折痕_V"
    },
    {
      "name": "报纸_今日热门"
    },
    {
      "name": "摇摆"
    },
    {
      "name": "摇摆_II"
    },
    {
      "name": "撒星星"
    },
    {
      "name": "撒星星_II"
    },
    {
      "name": "撕纸涂鸦边框"
    },
    {
      "name": "播放器"
    },
    {
      "name": "播放器_II"
    },
    {
      "name": "擦拭开幕"
    },
    {
      "name": "放大镜"
    },
    {
      "name": "放映机"
    },
    {
      "name": "放映机卡顿"
    },
    {
      "name": "放映机抖动"
    },
    {
      "name": "放映滚动"
    },
    {
      "name": "故障"
    },
    {
      "name": "故障_II"
    },
    {
      "name": "故障读条"
    },
    {
      "name": "文字闪动"
    },
    {
      "name": "斑斓"
    },
    {
      "name": "斜向模糊"
    },
    {
      "name": "方形取景器"
    },
    {
      "name": "方形开幕"
    },
    {
      "name": "旋转方块"
    },
    {
      "name": "日式DV"
    },
    {
      "name": "日文字幕"
    },
    {
      "name": "日落灯"
    },
    {
      "name": "时光碎片"
    },
    {
      "name": "时间停止"
    },
    {
      "name": "星光"
    },
    {
      "name": "星光_II"
    },
    {
      "name": "星光绽放"
    },
    {
      "name": "星光闪耀"
    },
    {
      "name": "星光闪闪"
    },
    {
      "name": "星夜"
    },
    {
      "name": "星星冲屏"
    },
    {
      "name": "星星坠落"
    },
    {
      "name": "星星投影"
    },
    {
      "name": "星星灯"
    },
    {
      "name": "星星闪烁"
    },
    {
      "name": "星星闪烁_II"
    },
    {
      "name": "星星闪烁_III"
    },
    {
      "name": "星月童话"
    },
    {
      "name": "星河"
    },
    {
      "name": "星河_II"
    },
    {
      "name": "星火"
    },
    {
      "name": "星火_II"
    },
    {
      "name": "星火炸开"
    },
    {
      "name": "星移"
    },
    {
      "name": "星空"
    },
    {
      "name": "星辰"
    },
    {
      "name": "星辰_I"
    },
    {
      "name": "星辰_II"
    },
    {
      "name": "星辰_III"
    },
    {
      "name": "星雨"
    },
    {
      "name": "春日樱花"
    },
    {
      "name": "春日边框"
    },
    {
      "name": "晴天光线"
    },
    {
      "name": "暗夜"
    },
    {
      "name": "暗夜归来"
    },
    {
      "name": "暗夜彩虹"
    },
    {
      "name": "暗夜彩虹_II"
    },
    {
      "name": "暗夜彩虹III"
    },
    {
      "name": "暗夜精灵"
    },
    {
      "name": "暗夜蝙蝠"
    },
    {
      "name": "暗角"
    },
    {
      "name": "暗黑剪影"
    },
    {
      "name": "暗黑噪点"
    },
    {
      "name": "暗黑蝙蝠"
    },
    {
      "name": "曝光"
    },
    {
      "name": "曝光降低"
    },
    {
      "name": "月亮投影"
    },
    {
      "name": "月亮闪闪"
    },
    {
      "name": "月光闪闪"
    },
    {
      "name": "望远镜"
    },
    {
      "name": "未来主义"
    },
    {
      "name": "杂志"
    },
    {
      "name": "树影"
    },
    {
      "name": "树影_II"
    },
    {
      "name": "格纹纸质"
    },
    {
      "name": "格纹纸质_II"
    },
    {
      "name": "梦境"
    },
    {
      "name": "梦境_II"
    },
    {
      "name": "梦境_III"
    },
    {
      "name": "梦境_IV"
    },
    {
      "name": "梦幻雪花"
    },
    {
      "name": "梦蝶"
    },
    {
      "name": "梦魇"
    },
    {
      "name": "梵高背景"
    },
    {
      "name": "模糊"
    },
    {
      "name": "模糊开幕"
    },
    {
      "name": "模糊星光"
    },
    {
      "name": "模糊星光_II"
    },
    {
      "name": "模糊闭幕"
    },
    {
      "name": "横向闭幕"
    },
    {
      "name": "横纹故障"
    },
    {
      "name": "横纹故障_II"
    },
    {
      "name": "樱花朵朵"
    },
    {
      "name": "橘色负片"
    },
    {
      "name": "欧根纱"
    },
    {
      "name": "毛刺"
    },
    {
      "name": "毛玻璃"
    },
    {
      "name": "水墨晕染"
    },
    {
      "name": "水彩晕染"
    },
    {
      "name": "水波纹"
    },
    {
      "name": "水波纹投影"
    },
    {
      "name": "水滴模糊"
    },
    {
      "name": "水滴滚动"
    },
    {
      "name": "油画纹理"
    },
    {
      "name": "泡泡"
    },
    {
      "name": "泡泡变焦"
    },
    {
      "name": "波纹扭曲"
    },
    {
      "name": "波纹色差"
    },
    {
      "name": "流动烟雾"
    },
    {
      "name": "流星雨"
    },
    {
      "name": "浓雾"
    },
    {
      "name": "浪漫氛围"
    },
    {
      "name": "浪漫氛围_II"
    },
    {
      "name": "涂鸦切割边框"
    },
    {
      "name": "淡彩边框"
    },
    {
      "name": "清新绿格子"
    },
    {
      "name": "渐显开幕"
    },
    {
      "name": "渐渐放大"
    },
    {
      "name": "渐隐闭幕"
    },
    {
      "name": "温柔细闪"
    },
    {
      "name": "游戏界面"
    },
    {
      "name": "满屏问号"
    },
    {
      "name": "漏光噪点"
    },
    {
      "name": "火光"
    },
    {
      "name": "火光刷过"
    },
    {
      "name": "火光包围"
    },
    {
      "name": "火光翻滚"
    },
    {
      "name": "火光蔓延"
    },
    {
      "name": "灵魂出窍"
    },
    {
      "name": "炫彩"
    },
    {
      "name": "炫彩_II"
    },
    {
      "name": "烟花"
    },
    {
      "name": "烟花_II"
    },
    {
      "name": "烟花_III"
    },
    {
      "name": "烟雾"
    },
    {
      "name": "烟雾炸开"
    },
    {
      "name": "爆炸"
    },
    {
      "name": "爱心Kira"
    },
    {
      "name": "爱心bling"
    },
    {
      "name": "爱心光斑"
    },
    {
      "name": "爱心光斑_II"
    },
    {
      "name": "爱心光波"
    },
    {
      "name": "爱心啵啵"
    },
    {
      "name": "爱心射线"
    },
    {
      "name": "爱心投影"
    },
    {
      "name": "爱心方块"
    },
    {
      "name": "爱心暗角"
    },
    {
      "name": "爱心气泡"
    },
    {
      "name": "爱心泡泡"
    },
    {
      "name": "爱心爆炸"
    },
    {
      "name": "爱心缤纷"
    },
    {
      "name": "爱心缤纷_II"
    },
    {
      "name": "爱心跳动"
    },
    {
      "name": "爱心跳动_II"
    },
    {
      "name": "爱心闪烁"
    },
    {
      "name": "牛皮纸关闭"
    },
    {
      "name": "牛皮纸打开"
    },
    {
      "name": "牛皮纸边框_I"
    },
    {
      "name": "牛皮纸边框_II"
    },
    {
      "name": "玫瑰花瓣"
    },
    {
      "name": "玻璃破碎"
    },
    {
      "name": "甜心投影"
    },
    {
      "name": "生日快乐"
    },
    {
      "name": "电光包围"
    },
    {
      "name": "电光漩涡"
    },
    {
      "name": "电子屏"
    },
    {
      "name": "电影刮花"
    },
    {
      "name": "电影感"
    },
    {
      "name": "电影感画幅"
    },
    {
      "name": "电脑桌面"
    },
    {
      "name": "电视关机"
    },
    {
      "name": "电视开机"
    },
    {
      "name": "电视彩虹屏"
    },
    {
      "name": "电视纹理"
    },
    {
      "name": "画展边框"
    },
    {
      "name": "白噪点边框"
    },
    {
      "name": "白胶边框"
    },
    {
      "name": "白色描边"
    },
    {
      "name": "白色渐显"
    },
    {
      "name": "白色爱心"
    },
    {
      "name": "白色线框"
    },
    {
      "name": "白色边框"
    },
    {
      "name": "百叶窗"
    },
    {
      "name": "百叶窗_II"
    },
    {
      "name": "监控"
    },
    {
      "name": "盗梦空间"
    },
    {
      "name": "盛世美颜"
    },
    {
      "name": "相机网格"
    },
    {
      "name": "相纸"
    },
    {
      "name": "瞬间模糊"
    },
    {
      "name": "破冰"
    },
    {
      "name": "磨砂纹理"
    },
    {
      "name": "祝福环绕"
    },
    {
      "name": "秋日暖黄"
    },
    {
      "name": "空灵"
    },
    {
      "name": "窗格"
    },
    {
      "name": "窗格光"
    },
    {
      "name": "简约边框"
    },
    {
      "name": "箭头放大镜"
    },
    {
      "name": "粉红老电视"
    },
    {
      "name": "粉红芭比边框"
    },
    {
      "name": "粉色闪粉"
    },
    {
      "name": "粉黄渐变"
    },
    {
      "name": "粒子模糊"
    },
    {
      "name": "精灵闪粉"
    },
    {
      "name": "精细锐化"
    },
    {
      "name": "糖果纸"
    },
    {
      "name": "紫色波纹"
    },
    {
      "name": "紫色负片"
    },
    {
      "name": "紫雾"
    },
    {
      "name": "繁星点点"
    },
    {
      "name": "纵向开幕"
    },
    {
      "name": "纵向模糊"
    },
    {
      "name": "纸膜边框_I"
    },
    {
      "name": "纸膜边框_II"
    },
    {
      "name": "纸质撕边"
    },
    {
      "name": "纸质边框"
    },
    {
      "name": "纸质边框_II"
    },
    {
      "name": "细闪"
    },
    {
      "name": "细闪_II"
    },
    {
      "name": "细闪_III"
    },
    {
      "name": "美式"
    },
    {
      "name": "美式_II"
    },
    {
      "name": "美式_III"
    },
    {
      "name": "美式_IV"
    },
    {
      "name": "美式_V"
    },
    {
      "name": "美漫"
    },
    {
      "name": "羽毛"
    },
    {
      "name": "老照片"
    },
    {
      "name": "老照片_II"
    },
    {
      "name": "老照片_III"
    },
    {
      "name": "老电影"
    },
    {
      "name": "老电影_II"
    },
    {
      "name": "老电视卡顿"
    },
    {
      "name": "聚光灯"
    },
    {
      "name": "聚焦"
    },
    {
      "name": "胡言乱语"
    },
    {
      "name": "胶片"
    },
    {
      "name": "胶片_II"
    },
    {
      "name": "胶片_III"
    },
    {
      "name": "胶片_IV"
    },
    {
      "name": "胶片抖动"
    },
    {
      "name": "胶片显影"
    },
    {
      "name": "胶片框"
    },
    {
      "name": "胶片框_II"
    },
    {
      "name": "胶片框_III"
    },
    {
      "name": "胶片漏光"
    },
    {
      "name": "胶片漏光_II"
    },
    {
      "name": "胶片连拍"
    },
    {
      "name": "自然"
    },
    {
      "name": "自然_II"
    },
    {
      "name": "自然_III"
    },
    {
      "name": "自然_IV"
    },
    {
      "name": "自然_V"
    },
    {
      "name": "色差"
    },
    {
      "name": "色差开幕"
    },
    {
      "name": "色差放大"
    },
    {
      "name": "色差放射"
    },
    {
      "name": "色差故障"
    },
    {
      "name": "色差故障_II"
    },
    {
      "name": "色差星闪"
    },
    {
      "name": "色差默片"
    },
    {
      "name": "节日彩带"
    },
    {
      "name": "花火"
    },
    {
      "name": "花火_II"
    },
    {
      "name": "花瓣飘落"
    },
    {
      "name": "花瓣飞扬"
    },
    {
      "name": "荡漾_II"
    },
    {
      "name": "荡秋千"
    },
    {
      "name": "荧光扫描"
    },
    {
      "name": "荧光爱心"
    },
    {
      "name": "荧光线描"
    },
    {
      "name": "荧光绿"
    },
    {
      "name": "荧光蝙蝠"
    },
    {
      "name": "荧幕噪点"
    },
    {
      "name": "荧幕噪点_II"
    },
    {
      "name": "萤光"
    },
    {
      "name": "萤光飞舞"
    },
    {
      "name": "萤火"
    },
    {
      "name": "落叶"
    },
    {
      "name": "落樱"
    },
    {
      "name": "蒸汽波"
    },
    {
      "name": "蒸汽波投影"
    },
    {
      "name": "蒸汽波路灯"
    },
    {
      "name": "蒸汽腾腾"
    },
    {
      "name": "蓝光扫描"
    },
    {
      "name": "蓝线模糊"
    },
    {
      "name": "蓝色负片"
    },
    {
      "name": "蓝色闪电边框"
    },
    {
      "name": "虚化"
    },
    {
      "name": "蝙蝠Kira"
    },
    {
      "name": "蝴蝶"
    },
    {
      "name": "蝴蝶_II"
    },
    {
      "name": "蝴蝶光斑"
    },
    {
      "name": "蝶舞"
    },
    {
      "name": "表面模糊"
    },
    {
      "name": "裂开了"
    },
    {
      "name": "视频分割"
    },
    {
      "name": "视频界面"
    },
    {
      "name": "诡异分割"
    },
    {
      "name": "负片闪烁"
    },
    {
      "name": "赞赞赞"
    },
    {
      "name": "蹦迪光"
    },
    {
      "name": "蹦迪彩光"
    },
    {
      "name": "车窗"
    },
    {
      "name": "车窗影"
    },
    {
      "name": "轻微抖动"
    },
    {
      "name": "轻微放大"
    },
    {
      "name": "边缘glitch"
    },
    {
      "name": "边缘加色"
    },
    {
      "name": "边缘加色_II"
    },
    {
      "name": "边缘加色_III"
    },
    {
      "name": "边缘发光"
    },
    {
      "name": "边缘荧光"
    },
    {
      "name": "运动一夏"
    },
    {
      "name": "迪斯科"
    },
    {
      "name": "迷幻烟雾"
    },
    {
      "name": "迷离"
    },
    {
      "name": "迷雾"
    },
    {
      "name": "逆光对焦"
    },
    {
      "name": "选中框"
    },
    {
      "name": "邮票边框"
    },
    {
      "name": "金属背景"
    },
    {
      "name": "金片"
    },
    {
      "name": "金片_II"
    },
    {
      "name": "金片炸开"
    },
    {
      "name": "金粉"
    },
    {
      "name": "金粉_II"
    },
    {
      "name": "金粉_III"
    },
    {
      "name": "金粉撒落"
    },
    {
      "name": "金粉旋转"
    },
    {
      "name": "金粉聚拢"
    },
    {
      "name": "金粉闪闪"
    },
    {
      "name": "钻光"
    },
    {
      "name": "钻石碎片"
    },
    {
      "name": "镜像"
    },
    {
      "name": "镜头变焦"
    },
    {
      "name": "长虹玻璃"
    },
    {
      "name": "闪亮登场"
    },
    {
      "name": "闪亮登场_II"
    },
    {
      "name": "闪光灯_I"
    },
    {
      "name": "闪光震动"
    },
    {
      "name": "闪动"
    },
    {
      "name": "闪动光斑"
    },
    {
      "name": "闪屏"
    },
    {
      "name": "闪电"
    },
    {
      "name": "闪白"
    },
    {
      "name": "闪耀星光"
    },
    {
      "name": "闪闪"
    },
    {
      "name": "闪闪发光_II"
    },
    {
      "name": "闪黑"
    },
    {
      "name": "闪黑II"
    },
    {
      "name": "闭幕"
    },
    {
      "name": "闭幕_II"
    },
    {
      "name": "随机色块"
    },
    {
      "name": "随机色块_II"
    },
    {
      "name": "随机裁剪"
    },
    {
      "name": "隐形人"
    },
    {
      "name": "隔行扫描"
    },
    {
      "name": "雨滴晕开"
    },
    {
      "name": "雪窗"
    },
    {
      "name": "雪花"
    },
    {
      "name": "雪花冲屏"
    },
    {
      "name": "雪花开幕"
    },
    {
      "name": "雪花故障"
    },
    {
      "name": "雪花细闪"
    },
    {
      "name": "零点解锁"
    },
    {
      "name": "雾气"
    },
    {
      "name": "雾气_II"
    },
    {
      "name": "雾气光线"
    },
    {
      "name": "震动"
    },
    {
      "name": "霓虹投影"
    },
    {
      "name": "霓虹摇摆"
    },
    {
      "name": "霓虹灯"
    },
    {
      "name": "预警"
    },
    {
      "name": "颤抖"
    },
    {
      "name": "飘落花瓣"
    },
    {
      "name": "飘落闪粉"
    },
    {
      "name": "飘落闪粉_II"
    },
    {
      "name": "飘雪"
    },
    {
      "name": "飘雪_II"
    },
    {
      "name": "飞速计算"
    },
    {
      "name": "马赛克"
    },
    {
      "name": "高光瞬间"
    },
    {
      "name": "魅力光束"
    },
    {
      "name": "魔法"
    },
    {
      "name": "魔法变身"
    },
    {
      "name": "魔法边框"
    },
    {
      "name": "魔法边框_II"
    },
    {
      "name": "鱼眼"
    },
    {
      "name": "黄蓝星芒"
    },
    {
      "name": "黑白VHS"
    },
    {
      "name": "黑白三格"
    },
    {
      "name": "黑白漫画"
    },
    {
      "name": "黑白漫画_II"
    },
    {
      "name": "黑白线描"
    },
    {
      "name": "黑线故障"
    },
    {
      "name": "黑羽毛"
    },
    {
      "name": "黑羽毛_II"
    },
    {
      "name": "黑胶边框"
    },
    {
      "name": "黑色噪点"
    },
    {
      "name": "黑色老电视"
    },
    {
      "name": "Bling飘落"
    },
    {
      "name": "C300"
    },
    {
      "name": "IXUS"
    },
    {
      "name": "Ins描边"
    },
    {
      "name": "S形运镜"
    },
    {
      "name": "W830"
    },
    {
      "name": "一刀两断"
    },
    {
      "name": "丁达尔旋焦"
    },
    {
      "name": "丝印涂鸦"
    },
    {
      "name": "丝滑运镜"
    },
    {
      "name": "两屏分割"
    },
    {
      "name": "中轴旋转"
    },
    {
      "name": "云朵绵绵"
    },
    {
      "name": "五星好评"
    },
    {
      "name": "交叉震闪"
    },
    {
      "name": "低保真"
    },
    {
      "name": "侧移模糊"
    },
    {
      "name": "倒带"
    },
    {
      "name": "倒计时"
    },
    {
      "name": "假日闪闪_II"
    },
    {
      "name": "像素屏闪"
    },
    {
      "name": "像素扫描"
    },
    {
      "name": "像素拉伸_II"
    },
    {
      "name": "像素排序"
    },
    {
      "name": "像素故障"
    },
    {
      "name": "像素爱心"
    },
    {
      "name": "像素震闪"
    },
    {
      "name": "光线扫描"
    },
    {
      "name": "光线拖影"
    },
    {
      "name": "光谱扫描"
    },
    {
      "name": "兔兔碎闪"
    },
    {
      "name": "全息扫描"
    },
    {
      "name": "分屏漏光"
    },
    {
      "name": "动态侦测"
    },
    {
      "name": "动态格"
    },
    {
      "name": "动感光束"
    },
    {
      "name": "动感变焦"
    },
    {
      "name": "动感扫光"
    },
    {
      "name": "动感推镜"
    },
    {
      "name": "动感竖线"
    },
    {
      "name": "动感运镜"
    },
    {
      "name": "十字模糊"
    },
    {
      "name": "十字爆闪"
    },
    {
      "name": "单向移动"
    },
    {
      "name": "单彩渐变"
    },
    {
      "name": "单色填充"
    },
    {
      "name": "卡机"
    },
    {
      "name": "卡通渲染"
    },
    {
      "name": "发光HDR"
    },
    {
      "name": "取景器"
    },
    {
      "name": "变色狙击"
    },
    {
      "name": "变色闪光"
    },
    {
      "name": "变速推镜"
    },
    {
      "name": "变速推镜II"
    },
    {
      "name": "可爱涂鸦"
    },
    {
      "name": "吓到失魂"
    },
    {
      "name": "噪片映射"
    },
    {
      "name": "圆形分屏"
    },
    {
      "name": "圣诞日记"
    },
    {
      "name": "复古彩虹"
    },
    {
      "name": "复古拼贴"
    },
    {
      "name": "复古紫调"
    },
    {
      "name": "复古红调"
    },
    {
      "name": "复古连拍"
    },
    {
      "name": "复古闪闪"
    },
    {
      "name": "复古频闪"
    },
    {
      "name": "失焦CCD"
    },
    {
      "name": "失焦光斑"
    },
    {
      "name": "定格祝福"
    },
    {
      "name": "实况开幕"
    },
    {
      "name": "对焦DV"
    },
    {
      "name": "局部推镜"
    },
    {
      "name": "局部色彩"
    },
    {
      "name": "居中闪切"
    },
    {
      "name": "屏幕律动"
    },
    {
      "name": "幻动光斑"
    },
    {
      "name": "幻彩故障"
    },
    {
      "name": "幻影_I"
    },
    {
      "name": "弹动摇镜"
    },
    {
      "name": "弹动旋入"
    },
    {
      "name": "弹性闪动"
    },
    {
      "name": "彩光摇晃"
    },
    {
      "name": "彩光频闪"
    },
    {
      "name": "彩色像素"
    },
    {
      "name": "彩色流光_I"
    },
    {
      "name": "彩色流光_II"
    },
    {
      "name": "彩色流光_III"
    },
    {
      "name": "彩色火焰"
    },
    {
      "name": "彩色珠滴"
    },
    {
      "name": "彩色电光"
    },
    {
      "name": "彩色碎彩"
    },
    {
      "name": "彩色碎片"
    },
    {
      "name": "彩色碎片_II"
    },
    {
      "name": "彩色闪烁"
    },
    {
      "name": "彩虹光影"
    },
    {
      "name": "彩虹棱镜"
    },
    {
      "name": "彩虹泛光"
    },
    {
      "name": "彩虹闪屏"
    },
    {
      "name": "彩边频闪"
    },
    {
      "name": "微震闪黑"
    },
    {
      "name": "心跳_II"
    },
    {
      "name": "快速变焦"
    },
    {
      "name": "快闪运镜"
    },
    {
      "name": "恐怖涂鸦"
    },
    {
      "name": "慢门拖影"
    },
    {
      "name": "手写边框"
    },
    {
      "name": "扭动变焦"
    },
    {
      "name": "扭曲变焦"
    },
    {
      "name": "扭曲模糊"
    },
    {
      "name": "抖动模糊"
    },
    {
      "name": "抽帧拖影"
    },
    {
      "name": "拉伸旋镜"
    },
    {
      "name": "拉扯震动"
    },
    {
      "name": "拉镜开幕"
    },
    {
      "name": "拍照定格"
    },
    {
      "name": "拖影灯光"
    },
    {
      "name": "拟截图放大镜"
    },
    {
      "name": "推拉跟随"
    },
    {
      "name": "推拉运镜"
    },
    {
      "name": "摇晃叠影"
    },
    {
      "name": "摇晃推镜"
    },
    {
      "name": "摇晃运镜"
    },
    {
      "name": "撕纸特写"
    },
    {
      "name": "播放界面"
    },
    {
      "name": "故障定格"
    },
    {
      "name": "故障开幕"
    },
    {
      "name": "故障震闪"
    },
    {
      "name": "散光弹动"
    },
    {
      "name": "散光闪烁"
    },
    {
      "name": "数字矩阵"
    },
    {
      "name": "斜线震动"
    },
    {
      "name": "新年仙女棒"
    },
    {
      "name": "方形模糊"
    },
    {
      "name": "旋焦"
    },
    {
      "name": "旋焦推镜"
    },
    {
      "name": "旋转变焦"
    },
    {
      "name": "旋转回弹"
    },
    {
      "name": "旋转圆球"
    },
    {
      "name": "旋转抖动"
    },
    {
      "name": "旋转抖动_II"
    },
    {
      "name": "星星变焦"
    },
    {
      "name": "曝光变焦"
    },
    {
      "name": "曝光扩散"
    },
    {
      "name": "曲线模糊"
    },
    {
      "name": "极速旋转"
    },
    {
      "name": "柔和辉光"
    },
    {
      "name": "梦幻辉光"
    },
    {
      "name": "模拟拍照"
    },
    {
      "name": "横向闪光"
    },
    {
      "name": "横条开幕"
    },
    {
      "name": "樱花飘落"
    },
    {
      "name": "欧根纱II"
    },
    {
      "name": "气球花花"
    },
    {
      "name": "氛围边框"
    },
    {
      "name": "水光影"
    },
    {
      "name": "水波倒影"
    },
    {
      "name": "水波模糊"
    },
    {
      "name": "水波泛起"
    },
    {
      "name": "水波流动"
    },
    {
      "name": "水滴扩散"
    },
    {
      "name": "油画模糊"
    },
    {
      "name": "法式暖调"
    },
    {
      "name": "法式涂鸦"
    },
    {
      "name": "泛光扫描"
    },
    {
      "name": "泛光爆闪"
    },
    {
      "name": "泛光闪动"
    },
    {
      "name": "泡泡光斑"
    },
    {
      "name": "泡泡冲屏"
    },
    {
      "name": "波动清晰"
    },
    {
      "name": "波浪"
    },
    {
      "name": "波浪丝印"
    },
    {
      "name": "波纹闪动"
    },
    {
      "name": "流体冲屏"
    },
    {
      "name": "流体荡开"
    },
    {
      "name": "海报描边"
    },
    {
      "name": "海鸥DC"
    },
    {
      "name": "液态分离"
    },
    {
      "name": "漂浮爱心"
    },
    {
      "name": "潮流涂鸦"
    },
    {
      "name": "瀑布开幕"
    },
    {
      "name": "灵魂出窍_II"
    },
    {
      "name": "灿灿金币"
    },
    {
      "name": "灿金彩带"
    },
    {
      "name": "炫光变焦"
    },
    {
      "name": "炫光扫描"
    },
    {
      "name": "烟花2024"
    },
    {
      "name": "热恋"
    },
    {
      "name": "爆闪锐化"
    },
    {
      "name": "爱心扫光"
    },
    {
      "name": "爱心气球"
    },
    {
      "name": "爱心软糖"
    },
    {
      "name": "爱心边框"
    },
    {
      "name": "珠光Kira"
    },
    {
      "name": "珠光碎闪"
    },
    {
      "name": "电光描边"
    },
    {
      "name": "电光波动"
    },
    {
      "name": "电光爆闪"
    },
    {
      "name": "电光爆闪_II"
    },
    {
      "name": "电光爱心"
    },
    {
      "name": "电音故障"
    },
    {
      "name": "画质清晰"
    },
    {
      "name": "白鸽"
    },
    {
      "name": "相片定格"
    },
    {
      "name": "矩阵频闪"
    },
    {
      "name": "碎闪描边"
    },
    {
      "name": "磁带DV"
    },
    {
      "name": "磨砂水晶"
    },
    {
      "name": "神龙纳福"
    },
    {
      "name": "秋日暖阳"
    },
    {
      "name": "移轴模糊"
    },
    {
      "name": "竖向开幕"
    },
    {
      "name": "竖向闪光"
    },
    {
      "name": "竖线屏闪"
    },
    {
      "name": "竖闪模糊"
    },
    {
      "name": "粉雪"
    },
    {
      "name": "粒子放射"
    },
    {
      "name": "精致辉光"
    },
    {
      "name": "紫光夜"
    },
    {
      "name": "繁花棱镜II"
    },
    {
      "name": "红蓝魔"
    },
    {
      "name": "红边模糊"
    },
    {
      "name": "纵向跳动"
    },
    {
      "name": "纸质抽帧"
    },
    {
      "name": "线光变速"
    },
    {
      "name": "线条涂鸦"
    },
    {
      "name": "缤纷"
    },
    {
      "name": "网点丝印"
    },
    {
      "name": "群蝶飞舞"
    },
    {
      "name": "羽毛飘落"
    },
    {
      "name": "翻转变焦"
    },
    {
      "name": "翻转开幕"
    },
    {
      "name": "老式DV"
    },
    {
      "name": "胶片V"
    },
    {
      "name": "胶片冷绿"
    },
    {
      "name": "胶片暖棕"
    },
    {
      "name": "胶片滚动"
    },
    {
      "name": "胶片闪切"
    },
    {
      "name": "脉搏跳动"
    },
    {
      "name": "色差震闪"
    },
    {
      "name": "色散冲击"
    },
    {
      "name": "色散故障"
    },
    {
      "name": "花屏故障"
    },
    {
      "name": "花瓣环绕"
    },
    {
      "name": "菱形光斑"
    },
    {
      "name": "菱形变焦"
    },
    {
      "name": "落叶_II"
    },
    {
      "name": "蓝光爆闪"
    },
    {
      "name": "蓝色丝印"
    },
    {
      "name": "虹光旋入"
    },
    {
      "name": "视频播放"
    },
    {
      "name": "负片分屏"
    },
    {
      "name": "负片涂鸦"
    },
    {
      "name": "负片涂鸦_II"
    },
    {
      "name": "负片涂鸦_III"
    },
    {
      "name": "负片游移"
    },
    {
      "name": "负片频闪"
    },
    {
      "name": "超大光斑"
    },
    {
      "name": "超强锐化"
    },
    {
      "name": "跟随运镜"
    },
    {
      "name": "跟随运镜_II"
    },
    {
      "name": "车窗_II"
    },
    {
      "name": "辉光开幕"
    },
    {
      "name": "边缘扫光"
    },
    {
      "name": "迷幻故障"
    },
    {
      "name": "迷幻荡漾"
    },
    {
      "name": "迷幻震动"
    },
    {
      "name": "重复变焦"
    },
    {
      "name": "重复震闪"
    },
    {
      "name": "金色碎片"
    },
    {
      "name": "金边闪烁"
    },
    {
      "name": "银杏飘落"
    },
    {
      "name": "闪光弹跳"
    },
    {
      "name": "闪光灯_II"
    },
    {
      "name": "闪光灯IV"
    },
    {
      "name": "闪电扭曲"
    },
    {
      "name": "闪白_II"
    },
    {
      "name": "随机闪切"
    },
    {
      "name": "随机马赛克"
    },
    {
      "name": "隔行DV"
    },
    {
      "name": "雨季_I"
    },
    {
      "name": "雪花光斑"
    },
    {
      "name": "雪雾"
    },
    {
      "name": "雾镜_II"
    },
    {
      "name": "震动光束"
    },
    {
      "name": "震动发光"
    },
    {
      "name": "震动屏闪"
    },
    {
      "name": "震动扫光"
    },
    {
      "name": "震动推镜"
    },
    {
      "name": "震闪渐黑"
    },
    {
      "name": "霓虹光线"
    },
    {
      "name": "霓虹闪切"
    },
    {
      "name": "马赛克闪切"
    },
    {
      "name": "高速彩光"
    },
    {
      "name": "鱼眼_II"
    },
    {
      "name": "鱼眼_III"
    },
    {
      "name": "鱼眼_IV"
    },
    {
      "name": "黑白胶片"
    }
  ],
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|[object]|true|none||none|
|»» name|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_effect

POST /add_effect

添加特效

> Body 请求参数

```json
"{\n    \"effect_type\": \"MV封面\",  // 特效类型名称（必填，需从系统支持的特效列表中选择，如:MV封面）\n    \"start\": 0,  // 特效开始时间（秒，选填，默认0）\n    \"end\": 3.0,  // 特效结束时间（秒，选填，默认3.0）\n    \"draft_id\": \"draft_789\",  // 草稿ID（选填，指定要添加特效的目标草稿，若未传或对应草稿不存在，可能自动创建新草稿）\n    \"track_name\": \"effect_01\",  // 特效轨道名称（选填，默认\"effect_01\"，用于区分不同特效轨道）\n    \"params\": [35,45],  // 特效参数列表（选填，未提供的参数将使用默认值，具体参数取决于特效类型）,\n    \"width\": 1080,  // 画布宽度（选填，默认1080）\n    \"height\": 1920  // 画布高度（选填，默认1920）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» effect_type|body|string| 是 |特效类型名称（必填，需从系统支持的特效列表中选择，如:MV封面）|
|» effect_category|body|string| 是 |特效类型（必填，scene或者character，默认scene）|
|» start|body|number| 否 |特效开始时间（秒，选填，默认0）|
|» end|body|number| 否 |特效结束时间（秒，选填，默认3.0）|
|» draft_id|body|string| 否 |草稿ID（选填，指定要添加特效的目标草稿，若未传或对应草稿不存在，可能自动创建新草稿）|
|» track_name|body|string| 否 |轨道名称|
|» relative_index|body|integer| 否 |none|
|» params|body|[integer]| 否 |特效参数列表（选填，未提供的参数将使用默认值，具体参数取决于特效类型）,|
|» width|body|integer| 否 |画布宽度（选填，默认1080）|
|» height|body|integer| 否 |画布高度（选填，默认1920）|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752375207_f11441ac",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752375207_f11441ac"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 贴纸

## POST search_sticker

POST /search_sticker

从贴纸库里搜索可用的贴纸

> Body 请求参数

```json
"{\n    \"keywords\": \"生日快乐\"  // 搜索关键词（必填，用于在贴纸库中查找相关贴纸）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» keywords|body|string| 是 |搜索关键词（必填，用于在贴纸库中查找相关贴纸）|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "data": [
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/da9e45fb4c514829bfedb361e4a7bab3~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=oJdObxvKiZt6fq2EfbdAHTaYkAQ%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 1400843,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/da9e45fb4c514829bfedb361e4a7bab3~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=GWsJevYyntqo5cDmIOdYZRJPfHo%3D&format=.png"
        },
        "sticker_id": "7132097333466025252",
        "title": " Happy Birthady 立体英文字母"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/deb3aeb196374e49bbaa50d9a634773d~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=tqhmWiWZtJxAWhTGWtLhRTMBSU8%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 275,
            "size": 91993,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/deb3aeb196374e49bbaa50d9a634773d~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=DQ8DPdLYm6t6nnW%2BqsL1OC%2BGAE8%3D&format=.png"
        },
        "sticker_id": "7120532388567846148",
        "title": "快乐小羊-派对彩带，横幅装饰，happy手写字"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/38a0a42cd14449b1aed351bf94f23200~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=CfymfFW0PP7Gl1ec8ycrAbmN6w8%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 129442,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/38a0a42cd14449b1aed351bf94f23200~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=Icn8okckcFAnEJFhcmGJzwZcpr8%3D&format=.png"
        },
        "sticker_id": "7069661365685832990",
        "title": "生日快乐，气球，庆祝"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c2a4bdbefe364671b738fefcff054978~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=vVTI8D5%2FHZjt%2FmVOVc8lpONoXJc%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 258,
            "size": 69582,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c2a4bdbefe364671b738fefcff054978~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=AJwcQhChhe1ZYjrWl2ohKXMTzQ4%3D&format=.png"
        },
        "sticker_id": "7011416978976017700",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/145fff8d91c7455c957f957b82a4f77b~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=L0HAEQx80nsyumiRTTIMvzwTIaE%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 27924,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/145fff8d91c7455c957f957b82a4f77b~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=bjP8%2BPxS3fuN3Xcz9gSXy9ARBC0%3D&format=.png"
        },
        "sticker_id": "7241491153592356136",
        "title": "生日拍照边框"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/59e405a1d05ad554edd0f73a339b97db~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=K072bUmebjuAOC%2FcHUKuJHYvYs4%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 453775,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/59e405a1d05ad554edd0f73a339b97db~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=LYFspAJP09Ae9XjNnkwtZ8oeezU%3D&format=.png"
        },
        "sticker_id": "7147606327232056607",
        "title": "手绘烟花"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e35aa750412346d9bd2ebd5d239be6f0~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=jR%2FNVjtyvte%2F8Xr%2FcQAEV43fUT4%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 865229,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e35aa750412346d9bd2ebd5d239be6f0~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=78szdh5T5F7vVBdQ429nU5%2BMOe8%3D&format=.png"
        },
        "sticker_id": "7203350189464177977",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ef594e9731f2425fa43424822db43d48~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=nvwU6xlO%2FRV24Iek452zcAptvvk%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 53148,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ef594e9731f2425fa43424822db43d48~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=s7XRaS2FvDkzXW4mmmExB56UpCQ%3D&format=.png"
        },
        "sticker_id": "7231901846133067008",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/aa8e6bf01707b12c869b34bdade65232~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=g%2BSJ65dRptTdSimODxdksNo8xXo%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 96428,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/aa8e6bf01707b12c869b34bdade65232~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=CBiOHIXnsy%2BuF3njhLy9fxjPXZA%3D&format=.png"
        },
        "sticker_id": "7146548438916730123",
        "title": "小熊涂鸦假期niceday边框"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/05ce35d0246847be8c762c0c29bfb2bf~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=zYIoryIaRLfTbr8qsGUliTDWjUw%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 242,
            "size": 584190,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/05ce35d0246847be8c762c0c29bfb2bf~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=WPR3%2BBZgsLaLURnmXm9cqMAEEpM%3D&format=.png"
        },
        "sticker_id": "7114596400435842343",
        "title": "3Demoji 庆祝 礼花 礼炮 氛围"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6fc442d662ae4fb3ac6da8bf0ca47e6f~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=ON955xrSr9AL%2BEQkWKRnSubq8ms%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 180435,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6fc442d662ae4fb3ac6da8bf0ca47e6f~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=NQ5gsD6sHSYbSsIg%2BRVh6TDJ1C4%3D&format=.png"
        },
        "sticker_id": "7013608910267387144",
        "title": "十一国庆快乐生日舞会派对彩条纸屑彩带庆祝动图"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/60f06cde786247589f854d11b581179a~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=KB3zftmVdfQKpHIbbcvlGDza7XA%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 1080,
            "size": 763633,
            "width_per_frame": 1080
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/60f06cde786247589f854d11b581179a~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=3Uy57h6lo0g3sSP4of76ZoK8Y3M%3D&format=.png"
        },
        "sticker_id": "7024561049512840484",
        "title": "生日快乐礼花撒花"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/5a7bfea8d23e4056b75d4fa903edd723~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=I6Javk%2Becojrpcj5Oca2bYC3h1c%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 2355616,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/5a7bfea8d23e4056b75d4fa903edd723~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=7yxlzrXZ9gYj9H6NhncrLnkoCcw%3D&format=.png"
        },
        "sticker_id": "7132090568426982687",
        "title": "粉色樱桃生日蛋糕"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c48c849f1ecf4e3ab846a81fee58da0e~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=kSrJXpGivU33%2FpRoS8Qfbf3cv1g%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 114961,
            "width_per_frame": 513
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c48c849f1ecf4e3ab846a81fee58da0e~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=I%2BgAYHBPwGtFr1aEF7dmIglcs0g%3D&format=.png"
        },
        "sticker_id": "7120839395803925792",
        "title": "快乐小羊-生日快乐帽子，生日快乐发箍"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f2b27ba271c34595b0b3c75ba9cd221f~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=odxuKSQfOb9R2t%2F5tNTu244meH0%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 72044,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f2b27ba271c34595b0b3c75ba9cd221f~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=kFINeB%2FVLA%2FANS8PqfD0zMPpABg%3D&format=.png"
        },
        "sticker_id": "7024562523403750664",
        "title": "生日快乐动图"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6514f649895d439f9f99d54c26b6583d~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=nKfJ9fyuwnGKjGE5cMCCl27Z810%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 355,
            "size": 163962,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6514f649895d439f9f99d54c26b6583d~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=VxuvMmcPoYNQSpszXpoVGQRPa5E%3D&format=.png"
        },
        "sticker_id": "7150593592438836483",
        "title": "生日派对-生日快乐手写字英文彩带横幅拉条"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d446a893ac474875a7fa7d8d075918d0~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=Lv1KaFVuRMezFVL2vVv07InPo9g%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 33488,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d446a893ac474875a7fa7d8d075918d0~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=am8gs1WEVDAroBdIqaz62WlS7QY%3D&format=.png"
        },
        "sticker_id": "7339747603237604662",
        "title": "女生节女王女神节王冠"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/1f44a90ab8a04bf797b3b9dff7d89602~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=zUVAfvS51EVZLYRwQeEDHD4TFCs%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 593620,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/1f44a90ab8a04bf797b3b9dff7d89602~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=m0LC9trfAdPCwVuZ9rQEVQpqqiM%3D&format=.png"
        },
        "sticker_id": "7202148789119978808",
        "title": "水晶王冠"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/8f969839880040f6ba50f4e7ec726557~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=q%2BgnLFFfcTE4jvb6Cs1hC7Hm5fg%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 161,
            "size": 14015,
            "width_per_frame": 300
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/8f969839880040f6ba50f4e7ec726557~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=Mw4JAGNyRcWyHvkP9R6iW7YKb7E%3D&format=.png"
        },
        "sticker_id": "6983270764015291679",
        "title": "生日快乐装饰元素彩旗"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ec1e02d6b49647c2a0f18b5cf0de22f6~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=mbRfxltQtFFLlv9lXsU7%2BPcvrGE%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 99383,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ec1e02d6b49647c2a0f18b5cf0de22f6~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=IBzuAUA%2FalLbZb0zKMwBEmb%2FdWk%3D&format=.png"
        },
        "sticker_id": "7070071163493387557",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/9a0a4e1452314a81b12acb0c43d62696~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=IvZQUXhAR5I9%2B45oOEMIfI79jBc%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 11852,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/9a0a4e1452314a81b12acb0c43d62696~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=OtS35gcV5hebf1iy%2FXkbc4vLsHg%3D&format=.png"
        },
        "sticker_id": "7035176805329915150",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2ce0e8a1fee740b1b8cb08e5c61fe312~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=E17Ri4K%2FWYLe1TnnwkwnI8ahVlI%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 53037,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2ce0e8a1fee740b1b8cb08e5c61fe312~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=eqNa9l78XO7XmmbzdKnCtMv0DVY%3D&format=.png"
        },
        "sticker_id": "7011908316821589285",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/342cdbc950dc43578c19997e6930f53f~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=eXsJKDOMwKnLPv%2BsOoCjvBYUBdc%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 66293,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/342cdbc950dc43578c19997e6930f53f~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=mF39X30HH6%2Fe8IIU0ORVAtega%2FU%3D&format=.png"
        },
        "sticker_id": "6991495880478133518",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2549368a7b23455a8810c0a3391f19f0~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=zB7K3xec3ayNrFQJB4vK471LJro%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 405,
            "size": 292934,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/2549368a7b23455a8810c0a3391f19f0~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=vlB2llJiXZ4mNRjrvNvFRJ3ODNE%3D&format=.png"
        },
        "sticker_id": "7031790671195344166",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6e3ba1051ed4489ba49e5f42c7659869~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=zHS%2Bk8%2BmuQ8GwfRd7IWv1e%2FG0%2Bc%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 69499,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6e3ba1051ed4489ba49e5f42c7659869~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=E6MgPFUVJc2rfzfNIC%2BJTmi8ffs%3D&format=.png"
        },
        "sticker_id": "7009981019742801183",
        "title": "生日蛋糕烫金生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/116837b728ef4725ad4dbc0f0ccfad8c~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=Y7yLjxq42pIJovEYVwS2uJe6tyA%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 91843,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/116837b728ef4725ad4dbc0f0ccfad8c~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=IPlDTiRmDt0tdnh%2FCTHsDEh7DVc%3D&format=.png"
        },
        "sticker_id": "7074118177235520809",
        "title": "生日快乐，小熊，可爱"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e0490d2dc12f4bb7940552fc681b4f6a~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=9F47tac%2BFTVdIIacG0RZ%2F8uCs9s%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 57116,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e0490d2dc12f4bb7940552fc681b4f6a~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=QXQ6HU67nUfWpZR2ddTUqT%2B0h%2BQ%3D&format=.png"
        },
        "sticker_id": "7084119330530610467",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/36d2ed1ef0bc4cae99bdc13fdbcae977~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=QpDo4dVbO6JYVQSxAW4uHNDTxOY%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 18804,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/36d2ed1ef0bc4cae99bdc13fdbcae977~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=9WrrJvhoirmx1XAjxeQACrppnKQ%3D&format=.png"
        },
        "sticker_id": "7150944804044672294",
        "title": "生日快乐 英文 祝福 GIF"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/165f3436d6cd48d087215087024ae7a8~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=Vq9a4ytP63%2B9rnyDwvxbPibQcQI%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 99920,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/165f3436d6cd48d087215087024ae7a8~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=%2BVu0jc2N2c5cbRqh8maL8MfYU1g%3D&format=.png"
        },
        "sticker_id": "7021498275622669605",
        "title": "手写字，生日快乐，英文 HAPPY birthday"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/c3bc229653e1c2567fc0fe8ffe780f80~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=vrq9qHsWRg%2FItV%2FHLXevI%2FA3hp8%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 847053,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/c3bc229653e1c2567fc0fe8ffe780f80~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=zHcp91I3w31DHcXP3EdAUWpyCx0%3D&format=.png"
        },
        "sticker_id": "7148044489733590279",
        "title": "甜点生日派对-寿星"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/1689e7b34e7d462287ca2dc4ee22bd86~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=3RSdL3yQ7l%2BuIsEYYNIpnPSG%2BUU%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 87749,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/1689e7b34e7d462287ca2dc4ee22bd86~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=Ceh8k2oKm6dWorASFypOtjPu2xg%3D&format=.png"
        },
        "sticker_id": "7108620752903081229",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/63a7abed9b5c4fb5b3935ea746cd62ee~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=tDOOjHSmkhJvQ%2FTzdZK9nlaGXCQ%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 62545,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/63a7abed9b5c4fb5b3935ea746cd62ee~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=OIYw3SzXpOWCrO4nsbxob%2FXAv48%3D&format=.png"
        },
        "sticker_id": "7117128167973768487",
        "title": "奔向新的一岁（生日快乐）"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/4e619f09325ea6e59ef692e9e49f89c9~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=veEeeuZ%2FDdjbADLlK5I8TrsxzNk%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 394242,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-v-436d67/4e619f09325ea6e59ef692e9e49f89c9~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=93UMT2GJQQWlFnj1eLwQOYLNmcQ%3D&format=.png"
        },
        "sticker_id": "7148073628595277069",
        "title": "甜点生日派对-生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f6227b73d6e142e981df46b2a1401e6a~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=zUqPw5zylRLWcxox3z%2BnpAOC1BE%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 9686,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f6227b73d6e142e981df46b2a1401e6a~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=AYJlMVG4tt1o3nZKZFgxbUJ8q1Y%3D&format=.png"
        },
        "sticker_id": "7057932038199905573",
        "title": "生日快乐，氛围炮筒动态"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/b8b97ab9129742e0be5c1bcbfaeec0bb~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=MpP8Kh1Hh%2BwAhFBw3qWn9O5mA%2Fw%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 885678,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/b8b97ab9129742e0be5c1bcbfaeec0bb~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=s%2BStUELLIH8JYd2LDb4l92o5Ctk%3D&format=.png"
        },
        "sticker_id": "7213584852325666107",
        "title": "生日快乐·"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/fd582a419ec341f484a0028cfd5bc9a2~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=3XmMCnsExQwgsdbaxyIZXYgPF0g%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 435,
            "size": 130540,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/fd582a419ec341f484a0028cfd5bc9a2~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=XsteavbHuVrkQmoul3ZpFElgLNo%3D&format=.png"
        },
        "sticker_id": "7175006455928573187",
        "title": "生日快乐派对贴纸-生日节日礼物礼盒"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f5baaaa994f746d8b4f847ddc472ca64~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=M74HOF1PmSeD%2Bg2Co3YUsaadr2Y%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 133185,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/f5baaaa994f746d8b4f847ddc472ca64~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=NxhnK6XMAXZ%2FDN4d60DFNKOeoIU%3D&format=.png"
        },
        "sticker_id": "7074393718769405199",
        "title": "不管几岁快乐万岁 生日祝福 生日快乐 开心"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/a4dcba644c7045beb4838c1e01d58418~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=LVs3Yi3ARtK8O1aaiJVVzkfRc6U%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 157,
            "size": 202673,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/a4dcba644c7045beb4838c1e01d58418~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=r9dXhX4x0%2BhF3T%2B0957o1dfe8F8%3D&format=.png"
        },
        "sticker_id": "7235905440272403773",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6a8af94a65d24240b093c508582a2f6d~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=2aeiBTyCor0o58uC4Dj4PKjKxSw%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 34374,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/6a8af94a65d24240b093c508582a2f6d~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=MVdRmtvgDOo%2B7TPXGi0XOSieUGk%3D&format=.png"
        },
        "sticker_id": "7021071238290345253",
        "title": "生日快乐彩色英文"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/414705ab5f334a068f369471cb0a752a~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=LHNvrabeBnU4m1hR1Sd%2BY1ZVp6w%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 13510,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/414705ab5f334a068f369471cb0a752a~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=a2ax7j5%2B%2FZ6NSxbNqATPRr1JyCw%3D&format=.png"
        },
        "sticker_id": "7059636095272750349",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7797d8f9f60d4d7bb25d3fb1437f0a15~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=nZkLSySo6v7GPCjfgAiJOZL%2FK5Q%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 16286,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7797d8f9f60d4d7bb25d3fb1437f0a15~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=Y%2FcgFXjnmxE8vd8qcBM4YPt8tKo%3D&format=.png"
        },
        "sticker_id": "7150945862343019808",
        "title": "生日快乐 涂鸦 边框 GIF"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/48084ab1e76c443cbf958a2978a5b64c~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=uo6vYQPbOWY7rS05Pwntwend6Rg%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 405,
            "size": 79445,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/48084ab1e76c443cbf958a2978a5b64c~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=He1yM%2FzSUdKk87yQRkQxh3riq%2B8%3D&format=.png"
        },
        "sticker_id": "6983970133001719071",
        "title": "生日快乐可爱气球边框 "
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c17a773ed6274389b16bf76dfa122b92~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=9PixqwzH5xvuz2WscEGdOrQV5SY%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 7478,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/c17a773ed6274389b16bf76dfa122b92~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=mXBOysoEYnR8vRkwF%2BDzdm55dME%3D&format=.png"
        },
        "sticker_id": "7031790884467248423",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d3422e1479814ec9a0ef6b49b1c5e523~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=E4qCl9RUdLu4%2FBqntarOBOOn1xs%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 83754,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/d3422e1479814ec9a0ef6b49b1c5e523~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=rhSrgmslD5H8rs9jwszbM5iXvns%3D&format=.png"
        },
        "sticker_id": "7036240755899288862",
        "title": "节日庆祝礼花碎片金色"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ade9114b64ac41dcab69ab7313c41c0d~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=EYpC8RzkahxSoEDgH%2FGkLSk2R%2Bs%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 52185,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/ade9114b64ac41dcab69ab7313c41c0d~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=8qgPTMmuOJQwsEDXrr4rf3QD2Ig%3D&format=.png"
        },
        "sticker_id": "7133073733152951567",
        "title": "生日快乐彩旗"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/18e360a040524306ad313cd8309ff3cb~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=%2BVVZrQ9bSgH42RUHig7cxBvne3U%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 327,
            "size": 113104,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/18e360a040524306ad313cd8309ff3cb~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=G%2FaRXDC8t5foHKoFDX4abkMgAb8%3D&format=.png"
        },
        "sticker_id": "7011417414827101448",
        "title": "生日"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/994d655a7dc349f3a249aee686276a5b~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=LRpgksAOqI00Ax2y0PoMmk0kuOA%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 208,
            "size": 11040,
            "width_per_frame": 240
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/994d655a7dc349f3a249aee686276a5b~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=QE1x1hyYDiNL1SG9UF3ByP7z2VM%3D&format=.png"
        },
        "sticker_id": "7238043919848590595",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e4be188d43e24cb5add731bc2f13c367~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=R%2FsN9PPiK3z7LcEeUDbRKlaUg94%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 48013,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p9-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/e4be188d43e24cb5add731bc2f13c367~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=Nnti%2BewF3PxrZeiel%2BFNALY1Bss%3D&format=.png"
        },
        "sticker_id": "7206110295562013985",
        "title": "生日快乐"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/caf5133339bf475c92f32b4a0bb07105~tplv-3jr8j4ixpe-resize:1920:1920.png?x-expires=1783912589&x-signature=6HaYzx85qEJUQDp1Pz0I0R%2Fj1b4%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 540,
            "size": 95978,
            "width_per_frame": 540
          },
          "sticker_type": 1,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/caf5133339bf475c92f32b4a0bb07105~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=yXKtaGAfVsD7U1VA97lvY6W1N7I%3D&format=.png"
        },
        "sticker_id": "7163976318260923685",
        "title": "草莓熊，生日素材，生日快乐，礼炮"
      },
      {
        "sticker": {
          "large_image": {
            "image_url": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7936f9b27c6c416a889f0d88db4d2b4f~tplv-3jr8j4ixpe-resize:1920:1920.gif?x-expires=1783912589&x-signature=cm5xNOTGNKihel63miWmVfKssYE%3D"
          },
          "preview_cover": "",
          "sticker_package": {
            "height_per_frame": 280,
            "size": 5703,
            "width_per_frame": 280
          },
          "sticker_type": 2,
          "track_thumbnail": "https://p3-heycan-jy-sign.byteimg.com/tos-cn-i-3jr8j4ixpe/7936f9b27c6c416a889f0d88db4d2b4f~tplv-3jr8j4ixpe-resize:120:120.png?x-expires=1783912589&x-signature=FnVR5N4W%2FkJ0FWivrR98%2FK0JHEc%3D&format=.png"
        },
        "sticker_id": "7025528380791508235",
        "title": "彩色涂鸦-生日蛋糕"
      }
    ],
    "message": "搜索成功"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» data|[object]|true|none||none|
|»»» sticker|object|true|none||none|
|»»»» large_image|object|true|none||none|
|»»»»» image_url|string|true|none||none|
|»»»» preview_cover|string|true|none||none|
|»»»» sticker_package|object|true|none||none|
|»»»»» height_per_frame|integer|true|none||none|
|»»»»» size|integer|true|none||none|
|»»»»» width_per_frame|integer|true|none||none|
|»»»» sticker_type|integer|true|none||none|
|»»»» track_thumbnail|string|true|none||none|
|»»» sticker_id|string|true|none||none|
|»»» title|string|true|none||none|
|»» message|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST add_sticker

POST /add_sticker

添加贴纸

> Body 请求参数

```json
"{\n    \"sticker_id\": \"7132097333466025252\",  // 贴纸资源ID（必填，通过/search_sticker接口获取的贴纸唯一标识）\n    \"start\": 0,  // 贴纸开始显示时间（秒，选填，默认0）\n    \"end\": 5.0,  // 贴纸结束显示时间（秒，选填，默认5.0）\n    \"draft_id\": \"draft_67890\",  // 草稿ID（选填，指定要添加贴纸的目标草稿）\n    \"transform_y\": 0,  // Y轴位置偏移（选填，默认0）\n    \"transform_x\": 0,  // X轴位置偏移（选填，默认0）\n    \"alpha\": 1.0,  // 贴纸透明度（选填，默认1.0，范围0.0-1.0，1.0为完全不透明）\n    \"flip_horizontal\": false,  // 是否水平翻转（选填，默认false）\n    \"flip_vertical\": false,  // 是否垂直翻转（选填，默认false）\n    \"rotation\": 0.0,  // 旋转角度（度，选填，默认0.0）\n    \"scale_x\": 1.0,  // X轴缩放比例（选填，默认1.0）\n    \"scale_y\": 1.0,  // Y轴缩放比例（选填，默认1.0）\n    \"track_name\": \"sticker_main\",  // 轨道名称（选填，默认\"sticker_main\"，用于区分不同贴纸轨道）\n    \"relative_index\": 0,  // 相对索引（选填，默认0，控制轨道内贴纸的层级顺序）\n    \"width\": 1080,  // 画布宽度（选填，默认1080）\n    \"height\": 1920  // 画布高度（选填，默认1920）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» sticker_id|body|string| 是 |贴纸资源ID（必填，通过/search_sticker接口获取的贴纸唯一标识）|
|» start|body|number| 否 |贴纸开始显示时间（秒，选填，默认0）|
|» end|body|number| 否 |贴纸结束显示时间（秒，选填，默认5.0）|
|» draft_id|body|string| 否 |草稿ID（选填，指定要添加贴纸的目标草稿）|
|» transform_y|body|number| 否 |垂直位置偏移，相对值（选填，默认0）|
|» transform_y_px|body|integer| 否 |垂直位置偏移，像素值|
|» transform_x|body|number| 否 |水平位置偏移，相对值（选填，默认0）|
|» transform_x_px|body|integer| 否 |水平位置偏移，像素值|
|» alpha|body|number| 否 |贴纸透明度（选填，默认1.0，范围0.0-1.0，1.0为完全不透明）|
|» flip_horizontal|body|boolean| 否 |是否水平翻转（选填，默认false）|
|» flip_vertical|body|boolean| 否 |是否垂直翻转（选填，默认false）|
|» rotation|body|number| 否 |旋转角度（度，选填，默认0.0）|
|» scale_x|body|number| 否 |X轴缩放比例（选填，默认1.0）|
|» scale_y|body|number| 否 |Y轴缩放比例（选填，默认1.0）|
|» track_name|body|string| 否 |none|
|» relative_index|body|integer| 否 |相对索引（选填，默认0，控制轨道内贴纸的层级顺序）|
|» width|body|integer| 否 |画布宽度（选填，默认1080）|
|» height|body|integer| 否 |画布高度（选填，默认1920）|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1752376744_a34d2d0a",
    "draft_url": "https://www.install-ai-guider.top/draft/downloader?draft_id=dfd_cat_1752376744_a34d2d0a"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 云渲染

## POST generate_video

POST /generate_video

提交云渲染任务

> Body 请求参数

```json
"{\n    \"draft_id\": \"draft id\", // 草稿ID（必填，指定要导出的目标草稿）\n    \"license_key\": \"your license key\", // 授权密钥（必填，从这里获取：https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6gsgqbnsk6g1e）\n    \"resolution\": \"720P\", // 视频分辨率（选填，默认\"720P\"，可选值如\"480P\"、\"720P\"、\"1080P\"、\"2K\"、\"4K\"等）\n    \"framerate\": \"24\" // 视频帧率（选填，默认\"24\"，可选值如\"24\"、\"25\"、\"30\"、\"50\"、\"60\"等）\n}"
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» draft_id|body|string| 是 |草稿ID（必填，指定要导出的目标草稿）|
|» license_key|body|string| 是 |授权密钥（必填，从这里获取：https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6gsgqbnsk6g1e）|
|» resolution|body|string| 否 |分辨率|
|» framerate|body|string| 否 |帧lü|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "error": null,
    "success": true,
    "task_id": "6c653617-8133-4c51-8bd0-8635e9e25879"
  },
  "purchase_link": "https://sguann.gumroad.com/l/vfzutl?wanted=true",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» error|null|true|none||none|
|»» success|boolean|true|none||none|
|»» task_id|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST task_status

POST /task_status

查询云渲染任务状态

> Body 请求参数

```json
{
  "task_id": "DEFA35C07BEE7F8F16162B9FAD7FA9F4"
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 是 |none|
|body|body|object| 否 |none|
|» task_id|body|string| 是 |任务id|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "error": null,
    "message": null,
    "progress": null,
    "result": "http://player.install-ai-guider.top/dfd_58b2fb9f-17ef-4913-abc3-44018fa8ce24.mp4?x-oss-date=20250713T032817Z&x-oss-expires=86400&x-oss-signature-version=OSS4-HMAC-SHA256&x-oss-credential=LTAI5t6GK97EdxsFqDT25U2j%2F20250713%2Fcn-hangzhou%2Foss%2Faliyun_v4_request&x-oss-signature=ab8e7d4318b11fdc6342cb8d125caae286d01d55cd49241363c3c60f8af9b82e",
    "status": "SUCCESS",
    "success": true,
    "task_id": "c18fcb96-42be-4c0e-b4db-6cbf6f0d6188"
  },
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|false|none||网络错误，这里展示的是请求task_status接口的网络错误，不代表业务错误|
|» output|object|true|none||none|
|»» error|string|false|none||业务错误，如果导出失败，这里将会返回具体的错误信息，例如素材丢失，超时等|
|»» message|string|false|none||消息，例如“排队，导出，上传，成功，错误“等等|
|»» progress|integer|false|none||导出进度，例如10，20，80等等|
|»» result|string|false|none||正确结果，只有当导出成功，这里才会展示导出的视频链接|
|»» status|string|true|none||状态枚举|
|»» success|boolean|true|none||是否成功，只有当任务成功才会置为true。任务失败，正在处理中都会置为false|
|»» task_id|string|true|none||当前任务的唯一ID|
|» success|boolean|true|none||网络请求成功，这里仅表示服务握手成功，并不代表业务处理成功|

#### 枚举值

|属性|值|
|---|---|
|status|PENDING|
|status|PROCESSING|
|status|UPLOADING|
|status|SUCCESS|
|status|DOWNLOADING|
|status|EXPORTING|
|status|FAILURE|

# 工作流

## POST execute_workflow

POST /execute_workflow

执行剪辑操作工作流

> Body 请求参数

```json
{
  "inputs": {
    "text": "Hello!",
    "start": 0,
    "end": 15
  },
  "script": [
    {
      "type": "action",
      "id": "uuid_1",
      "index": 0,
      "action_type": "add_text",
      "params": "{\"text\":\"${text}\",\"start\":\"${start}\",\"end\":\"${end}\",\"track_name\":\"text_main\",\"font_size\":\"8.0\",\"font_color\":\"#FF0000\"}"
    },
    {
      "type": "action",
      "id": "uuid_2",
      "index": 1,
      "action_type": "add_text",
      "params": "{\"text\":\"这是第二行文本\",\"start\":\"0\",\"end\":\"5.0\",\"track_name\":\"text_1\",\"transform_y\":\"0.3\"}"
    },
    {
      "type": "action",
      "id": "uuid_3",
      "index": 2,
      "action_type": "add_subtitle",
      "params": "{\"srt_path\":\"1\\n00:00:00,000 --> 00:00:04,433\\n你333好，我是孙关南开发的剪映草稿助手。\\n\\n2\\n00:00:04,433 --> 00:00:11,360\\n我擅长将音频、视频、图片素材拼接在一起剪辑输出剪映草稿。\\n\",\"track_name\":\"subtitle_1\",\"font_size\":\"5.0\"}"
    },
    {
      "type": "action",
      "id": "uuid_4",
      "index": 3,
      "action_type": "add_text_template",
      "params": "{\"template_id\":\"7373303725881822491\",\"start\":\"2.0\",\"track_name\":\"text_template_main\"}"
    },
    {
      "type": "action",
      "id": "uuid_5",
      "index": 4,
      "action_type": "add_image",
      "params": "{\"image_url\":\"https://pic1.imgdb.cn/item/68ba8fc058cb8da5c8801ab0.png\",\"start\":\"5.0\",\"end\":\"10.0\",\"track_name\":\"image_main\"}"
    },
    {
      "type": "action",
      "id": "uuid_6",
      "index": 5,
      "action_type": "add_video",
      "params": "{\"video_url\":\"https://cdn.wanx.aliyuncs.com/wanx/1719234057367822001/text_to_video/092faf3c94244973ab752ee1280ba76f.mp4?spm=5176.29623064.0.0.41ed26d6cBOhV3&file=092faf3c94244973ab752ee1280ba76f.mp4\",\"target_start\":\"10.0\",\"track_name\":\"video_main\"}"
    },
    {
      "type": "action",
      "id": "uuid_7",
      "index": 6,
      "action_type": "add_audio",
      "params": "{\"audio_url\":\"https://lf3-lv-music-tos.faceu.com/obj/tos-cn-ve-2774/oYACBQRCMlWBIrZipvQZhI5LAlUFYii0RwEPh\",\"start\":\"0.0\",\"track_name\":\"audio_main\",\"volume\":\"0.8\"}"
    },
    {
      "type": "action",
      "id": "uuid_8",
      "index": 7,
      "action_type": "add_video_keyframe",
      "params": "{\"track_name\":\"video_main\",\"time\":\"10.5\",\"property_type\":\"position_y\",\"value\":\"1\"}"
    },
    {
      "type": "action",
      "id": "uuid_9",
      "index": 8,
      "action_type": "add_video_keyframe",
      "params": "{\"track_name\":\"video_main\",\"time\":\"11.5\",\"property_type\":\"position_y\",\"value\":\"0.2\"}"
    },
    {
      "type": "action",
      "id": "uuid_10",
      "index": 9,
      "action_type": "add_video_keyframe",
      "params": "{\"track_name\":\"video_main\",\"times\":[10.5,12.5],\"property_types\":[\"position_x\",\"position_x\"],\"values\":[1,-1]}"
    },
    {
      "type": "action",
      "id": "uuid_11",
      "index": 10,
      "action_type": "add_effect",
      "params": "{\"effect_category\":\"scene\",\"effect_type\":\"金粉闪闪\",\"start\":\"0\",\"end\":\"10\",\"track_name\":\"effect_01\",\"params\":[100,50,34]}"
    },
    {
      "type": "action",
      "id": "uuid_12",
      "index": 11,
      "action_type": "add_sticker",
      "params": "{\"sticker_id\":\"7107529669750066445\",\"start\":\"20.0\",\"end\":\"25.0\",\"transform_y\":\"0.3\",\"transform_x\":\"-0.2\",\"alpha\":\"0.8\",\"rotation\":\"45.0\",\"scale_x\":\"1.5\",\"scale_y\":\"1.5\",\"track_name\":\"sticker_main\"}"
    }
  ]
}
```

### 请求参数

|名称|位置|类型|必选|说明|
|---|---|---|---|---|
|Authorization|header|string| 否 |none|
|body|body|object| 是 |none|
|» draft_id|body|string| 否 |草稿id，可以基于已有草稿继续编辑，不填默认创建新草稿|
|» inputs|body|object| 否 |输入参数，替换脚本里的${}值|
|» script|body|[object]| 是 |脚本|
|»» type|body|string| 是 |步骤类型，例如action|
|»» id|body|string| 是 |每个步骤的唯一性标识|
|»» index|body|integer| 是 |顺序，从0开始|
|»» action_type|body|string| 否 |动作类型，对应具体的剪辑接口|
|»» params|body|string| 否 |请求体，对应接口的请求体|

#### 枚举值

|属性|值|
|---|---|
|»» type|action|
|»» type|if|
|»» type|for|
|»» type|set_var|
|»» action_type|add_text|
|»» action_type|add_subtitle|
|»» action_type|add_text_template|
|»» action_type|add_image|
|»» action_type|add_video|
|»» action_type|add_audio|
|»» action_type|add_video_keyframe|
|»» action_type|add_effect|
|»» action_type|add_sticker|
|»» action_type|get_duration|
|»» action_type|add_preset|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1760946180_fc9a8560",
    "draft_url": "https://cn.capcutapi.top/draft/downloader?draft_id=dfd_cat_1760946180_fc9a8560&is_capcut=0&api_key_hash=15b082f53a67b381693cc2c62982d3bf662463523721ca35544106af2d2bb57c"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

## POST publish_workflow

POST /publish_workflow

发布工作流，返回工作流id，方便在其他地方调用这个工作流

> Body 请求参数

```json
{
  "script": [
    {
      "type": "action",
      "id": "uuid_1",
      "action_type": "add_text",
      "params": {
        "text": "Hello World",
        "start": 0,
        "end": 5,
        "track_name": "text_main",
        "font_size": 8,
        "font_color": "#FF0000"
      }
    }
  ]
}
```

### 请求参数

|名称|位置|类型|必选|中文名|说明|
|---|---|---|---|---|---|
|name|query|string| 否 ||工作流名称，不填会生成一个默认名称|
|description|query|string| 否 ||工作流描述|
|Authorization|header|string| 是 ||none|
|Content-Type|header|string| 是 ||none|
|body|body|string| 是 | 工作流文本，json格式|none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "description": "test1",
    "name": "test_workflow_1",
    "workflow_id": "4a9a7e7ac725401dbc3fe20aa513543c"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET list_workflows

GET /list_workflows

获取当前用户的所有工作流

> Body 请求参数

```
string

```

### 请求参数

|名称|位置|类型|必选|中文名|说明|
|---|---|---|---|---|---|
|limit|query|integer| 否 ||一次拉取多少条记录，默认100|
|offset|query|integer| 否 ||起始位置，默认0|
|Authorization|header|string| 是 ||none|
|Content-Type|header|string| 是 ||none|
|body|body|string| 是 ||none|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true,
  "total": 4,
  "workflows": [
    {
      "created_at": "2025-10-22 16:27:05",
      "description": "test1",
      "is_template": 0,
      "name": "test_workflow_1",
      "tags": null,
      "updated_at": "2025-10-22 16:27:05",
      "workflow_id": "4a9a7e7ac725401dbc3fe20aa513543c"
    },
    {
      "created_at": "2025-10-22 16:20:50",
      "description": "test1",
      "is_template": 0,
      "name": "test_workflow_1",
      "tags": null,
      "updated_at": "2025-10-22 16:20:50",
      "workflow_id": "e30bf665190c4211a6a10e0a0f4ecca8"
    },
    {
      "created_at": "2025-10-22 11:32:51",
      "description": "这是一个测试Workflow",
      "is_template": 0,
      "name": "测试Workflow",
      "tags": "测试,文本,标题",
      "updated_at": "2025-10-22 11:32:51",
      "workflow_id": "9294339a99dc4ccbaa3a7ea50fd0ed14"
    },
    {
      "created_at": "2025-10-22 11:00:40",
      "description": "这是一个测试Workflow",
      "is_template": 0,
      "name": "测试Workflow",
      "tags": "测试,文本,标题",
      "updated_at": "2025-10-22 11:00:40",
      "workflow_id": "84761a513ea448ae9987f2059cd2114f"
    }
  ]
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

## GET get_workflow

GET /get_workflow

获取工作流信息

> Body 请求参数

```
string

```

### 请求参数

|名称|位置|类型|必选|中文名|说明|
|---|---|---|---|---|---|
|workflow_id|query|string| 否 ||工作流id|
|Authorization|header|string| 是 ||none|
|Content-Type|header|string| 是 ||none|
|body|body|string| 是 ||none|

> 返回示例

> 200 Response

```json
{}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

# 模版/预设

## POST add_preset

POST /add_preset

添加预设片段

> Body 请求参数

```json
{
  "preset_id": "72d08158-678b-44a7-90b4-2a765a877994",
  "replacements": [
    {
      "audio1": "https://oss-jianying-resource.oss-cn-hangzhou.aliyuncs.com/test/55f87aef1c1a65dcf3acb895ad541729.mp3"
    },
    {
      "audio2": "https://oss-jianying-resource.oss-cn-hangzhou.aliyuncs.com/test/55f87aef1c1a65dcf3acb895ad541729.mp3"
    },
    {
      "image1": "https://oss-jianying-resource.oss-cn-hangzhou.aliyuncs.com/test/workflow_history/image_000.jpg"
    },
    {
      "video1": "https://cdn.wanx.aliyuncs.com/wanx/1719234057367822001/text_to_video/092faf3c94244973ab752ee1280ba76f.mp4?spm=5176.29623064.0.0.41ed26d6cBOhV3&file=092faf3c94244973ab752ee1280ba76f.mp4"
    }
  ],
  "target_start": 2,
  "draft_id": "draft_456",
  "transform_x": 0.5,
  "transform_y": 0.5,
  "rotation": 0,
  "scale_x": 1,
  "scale_y": 1,
  "track_name": "my_preset_track",
  "width": 1080,
  "height": 1920
}
```

### 请求参数

|名称|位置|类型|必选|中文名|说明|
|---|---|---|---|---|---|
|Authorization|header|string| 是 ||none|
|Content-Type|header|string| 是 ||none|
|body|body|object| 是 ||none|
|» preset_id|body|string| 是 ||预设片段id|
|» replacements|body|[object]| 否 ||替换预设素材，支持文字，图片，视频，音频|
|» target_start|body|number| 否 ||目标轨道的开始时间，单位秒|
|» start|body|number| 否 ||原始素材的开始时间，单位秒|
|» end|body|number| 否 ||原始素材的结束时间，单位秒|
|» draft_id|body|string| 否 ||草稿id，不填默认创建新草稿|
|» transform_x|body|number| 否 ||水平偏移，相对值，0在屏幕中间|
|» transform_y|body|number| 否 ||垂直偏移，相对值，0在屏幕中间|
|» transform_x_px|body|integer| 否 ||水平偏移，像素值|
|» transform_y_px|body|integer| 否 ||垂直偏移，像素值|
|» rotation|body|number| 否 ||旋转角度|
|» scale_x|body|number| 否 ||水平缩放|
|» scale_y|body|number| 否 ||垂直缩放|
|» track_name|body|string| 否 ||轨道名，默认preset_track|
|» width|body|integer| 否 ||宽度，默认1080|
|» height|body|integer| 否 ||高度，默认1920|

> 返回示例

> 200 Response

```json
{
  "error": "",
  "output": {
    "draft_id": "dfd_cat_1762657936_33c76364",
    "draft_url": "https://cn.capcutapi.top/draft/downloader?draft_id=dfd_cat_1762657936_33c76364&is_capcut=0&api_key_hash=8d9c14ce4af7ba82880b412d7808f45f4f71fb41188c365fc54dd46d3bfdedf3"
  },
  "purchase_link": "https://www.coze.cn/store/project/7498257920212647946?entity_id=1&bid=6g6miqtbk3009",
  "success": true
}
```

### 返回结果

|状态码|状态码含义|说明|数据模型|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|none|Inline|

### 返回数据结构

状态码 **200**

|名称|类型|必选|约束|中文名|说明|
|---|---|---|---|---|---|
|» error|string|true|none||none|
|» output|object|true|none||none|
|»» draft_id|string|true|none||none|
|»» draft_url|string|true|none||none|
|» purchase_link|string|true|none||none|
|» success|boolean|true|none||none|

# 数据模型

