package com.zhongjia.subtitlefusion.model;

import com.zhongjia.subtitlefusion.model.enums.TextStrategyEnum;
import lombok.Data;

import java.util.List;

@Data
public class CommonSubtitleInfo {
    private String text;
    private String startTime;
    private String endTime;
    private SubtitleEffectInfo subtitleEffectInfo;


    @Data
    public static class SubtitleEffectInfo {
        // 关键句 允许随机自定义, 从花字或模板中随机二选一，但6字以上不选模板
        private Boolean allowRandomEffect;
        // 动效音效
        private String effectAudioUrl;
        // 适用于处理关键字的动效
        private List<String> keyWords;
        // 文字模板内各占位文本（对应 add_text_template 的 texts）
        private List<String> templateTexts;

        private TextStrategyEnum textStrategy;

    }
}
