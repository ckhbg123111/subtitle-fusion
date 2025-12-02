# video chain v2 接口对接

## 需求
/api/script-driven/tasks-v2
接口对接VideoChainV2Controller 中的接口

## 说明
不再使用boolean effectsEnabled = appProperties.getFeatures().isSubtitleObjectEffectsEnabled(); 总是开启

字幕也不再走srt方案，而是采用新的VideoChainV2Controller 接口中的方案

字幕模板硬编码：
`花字`：[{"font": "半梦体", "effectId": "Wk1vRFZWQFJGb1NUTFVKaUdRUA==", "textIntro": {"animation": "轻微放大"}, "textOutro": {"animation": "弹出"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "WklqRFJQSlZGalxTS1pBZ0VUVQ==", "textIntro": {"animation": "左上弹入"}, "textOutro": {"animation": "弹出"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "W0BoR11bQVBDa1xTTlROZkRdUA==", "textIntro": {"animation": "右上弹入"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "WkptQ1BSQlxMaVpdS1hIbkpXUQ==", "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "W0FmRVRQRVxGa1NQSlpNaUpQUQ==", "textIntro": {"animation": "波浪弹入"}, "textOutro": {"animation": "渐隐"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "WklnR1NSQ1FMaFhUSlRBZ0tSUg==", "textIntro": {"animation": "波浪弹入"}, "textOutro": {"animation": "渐隐"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "WklpS1RURFNNbVpWS1hKbkdcUQ==", "textIntro": {"animation": "羽化向右擦开"}, "textOutro": {"animation": "渐隐"}, "transformX": 0.0, "transformY": -0.6}, {"font": "半梦体", "effectId": "W0BmQFNaQVJBbFlRTVlLbkBdUA==", "textIntro": {"animation": "右上弹入"}, "textOutro": {"animation": "弹出"}, "transformX": 0.0, "transformY": -0.6}]
`文字模板`：[{"templateId": "7163524521452948744"}]
`关键字`：[{"font": "卡酷体", "transformX": 0.0, "transformY": -0.6, "borderColor": "#000000", "keywordsFont": "卡酷体", "keywordsColor": "#52c41a", "borderWidthRate": 1}]
`基础文字`：[{"font": "卡酷体", "fontColor": "#ffffff", "textIntro": {"animation": "向上弹入"}, "transformX": 0.0, "transformY": -0.6, "borderColor": "#000000", "borderWidthRate": 2}]

private List<SubtitleInfo> subtitleInfo; 走底部字幕轨道，效果就用`基础文字`

接口的ObjectItem 中有两种类型，图片，和标题字，标题字将使用花字和文字模板二选一。

转场可以从这几个里面挑选

[
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
}