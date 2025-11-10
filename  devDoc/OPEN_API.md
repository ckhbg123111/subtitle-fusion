# JianYingAPI

## Docs
- [一分钟，了解 剪映API！](https://docs.capcutapi.top/7013988m0.md):
- [如何下载草稿](https://docs.capcutapi.top/7163338m0.md):
- [在线预览草稿](https://docs.capcutapi.top/7184090m0.md):
- [获取API Key](https://docs.capcutapi.top/7193258m0.md):
- [使用工作流功能](https://docs.capcutapi.top/7526013m0.md):
- 文本 [使用花字](https://docs.capcutapi.top/7321758m0.md):
- 文本 [使用文字模版](https://docs.capcutapi.top/7322081m0.md):
- 文本 [如何上传文字模板](https://docs.capcutapi.top/7350425m0.md):
- 工作流 [工作流脚本技术文档](https://docs.capcutapi.top/7545863m0.md):
- 工作流 [工作流 AI Prompt 提示词](https://docs.capcutapi.top/7560190m0.md):
- 模版/预设 [如何使用模版/预设功能](https://docs.capcutapi.top/7625978m0.md):
- 模版/预设 [如何上传模版](https://docs.capcutapi.top/7636263m0.md):

## API Docs
- 文本 [get_text_intro_types](https://docs.capcutapi.top/321242300e0.md): 获取可用的文本入场动画
- 文本 [get_text_outro_types](https://docs.capcutapi.top/321242330e0.md): 获取可用的文本出场动画
- 文本 [get_text_loop_anim_types](https://docs.capcutapi.top/321242352e0.md): 获取可用的文本循环动画
- 文本 [get_font_types](https://docs.capcutapi.top/321241595e0.md): 获取可用字体
- 文本 [add_text](https://docs.capcutapi.top/321240978e0.md): 添加文字
- 文本 [add_subtitle](https://docs.capcutapi.top/321242724e0.md): 向指定草稿添加字幕，比add_text方法多了直接添加srt格式字幕的能力，但是不支持动画
- 文本 [search_artist](https://docs.capcutapi.top/344228917e0.md): 搜索花字。从返回结果中获取：data.data.effect_item_list.common_attr.effect_id，将它填入add_text的effect_effect_id即可设置花字
- 文本 [add_text_template](https://docs.capcutapi.top/346034618e0.md):
- 图片 [get_intro_animation_types](https://docs.capcutapi.top/321181567e0.md): 获取可用的入场动画
- 图片 [get_outro_animation_types](https://docs.capcutapi.top/321183440e0.md): 获取可用的出场动画
- 图片 [get_combo_animation_types](https://docs.capcutapi.top/321183634e0.md): 获取可用的组合动画
- 图片 [add_image](https://docs.capcutapi.top/320460206e0.md): 向草稿中添加图片
- 视频 [add_video](https://docs.capcutapi.top/321243745e0.md): 添加视频
- 转场 [get_transition_types](https://docs.capcutapi.top/321183893e0.md): 获取可用的转场动画
- 蒙版 [get_mask_types](https://docs.capcutapi.top/321189552e0.md): 获取支持的蒙版
- 音频 [get_audio_effect_types](https://docs.capcutapi.top/321197911e0.md): 获取可用音频特效
- 音频 [add_audio](https://docs.capcutapi.top/321196190e0.md): 添加音频
- 关键帧 [add_video_keyframe](https://docs.capcutapi.top/321244301e0.md): 向指定轨道添加关键帧，支持批量操作
- 特效 [get_video_character_effect_types](https://docs.capcutapi.top/321245379e0.md): 获取可用的人物特效列表
- 特效 [get_video_scene_effect_types](https://docs.capcutapi.top/321245348e0.md): 获取可用的场景特效列表
- 特效 [add_effect](https://docs.capcutapi.top/321244826e0.md): 添加特效
- 贴纸 [search_sticker](https://docs.capcutapi.top/321246502e0.md): 从贴纸库里搜索可用的贴纸
- 贴纸 [add_sticker](https://docs.capcutapi.top/321246572e0.md): 添加贴纸
- 云渲染 [generate_video](https://docs.capcutapi.top/321247224e0.md): 提交云渲染任务
- 云渲染 [task_status](https://docs.capcutapi.top/321247406e0.md): 查询云渲染任务状态
- 工作流 [execute_workflow](https://docs.capcutapi.top/363414609e0.md): 执行剪辑操作工作流
- 工作流 [publish_workflow](https://docs.capcutapi.top/364820146e0.md): 发布工作流，返回工作流id，方便在其他地方调用这个工作流
- 工作流 [list_workflows](https://docs.capcutapi.top/364820814e0.md): 获取当前用户的所有工作流
- 工作流 [get_workflow](https://docs.capcutapi.top/364821775e0.md): 获取工作流信息
- 模版/预设 [add_preset](https://docs.capcutapi.top/372375099e0.md): 添加预设片段
- [create_draft](https://docs.capcutapi.top/321174266e0.md): 创建一个草稿
- [save_draft](https://docs.capcutapi.top/321190145e0.md): 生成草稿的下载链接
- [get_duration](https://docs.capcutapi.top/328289318e0.md): 