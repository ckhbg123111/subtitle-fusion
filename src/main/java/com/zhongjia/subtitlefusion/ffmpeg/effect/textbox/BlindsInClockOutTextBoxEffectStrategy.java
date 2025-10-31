package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import org.springframework.stereotype.Component;

@Component
public class BlindsInClockOutTextBoxEffectStrategy implements TextBoxEffectStrategy {
    @Override
    public TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH) {
        // 简化处理：保持轻微漂浮，避免对文本做复杂 alpha 处理
        String x = baseX0 + "+(W*0.0075)*sin(2*PI*(t*0.35))";
        String y = baseY0 + "+(H*0.0075)*sin(2*PI*(t*0.40))";
        return new TextBoxEffect(x, y);
    }
}


