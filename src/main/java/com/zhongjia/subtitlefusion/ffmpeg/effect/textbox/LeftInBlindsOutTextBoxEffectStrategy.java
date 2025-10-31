package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import org.springframework.stereotype.Component;

@Component
public class LeftInBlindsOutTextBoxEffectStrategy implements TextBoxEffectStrategy {
    @Override
    public TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH) {
        String inDur = "0.40";
        String stayX = baseX0;
        String stayY = baseY0;
        String x = "if(lt(t," + startSec + "+" + inDur + "),(-w)+((t-" + startSec + ")/" + inDur + ")*(" + baseX0 + "+w)," + stayX + ")";
        String y = stayY;
        return new TextBoxEffect(x, y);
    }
}


