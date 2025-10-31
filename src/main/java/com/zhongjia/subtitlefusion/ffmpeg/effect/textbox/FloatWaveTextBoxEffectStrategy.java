package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import org.springframework.stereotype.Component;

@Component
public class FloatWaveTextBoxEffectStrategy implements TextBoxEffectStrategy {
    @Override
    public TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH) {
        String x = baseX0 + "+(W*0.0075)*sin(2*PI*(t*0.35))";
        String y = baseY0 + "+(H*0.0075)*sin(2*PI*(t*0.40))";
        return new TextBoxEffect(x, y);
    }
}


