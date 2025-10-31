package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import org.springframework.stereotype.Component;

@Component
public class TopInFadeOutTextBoxEffectStrategy implements TextBoxEffectStrategy {
    @Override
    public TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH) {
        String inDur = "0.40";
        String outDur = "0.40";
        String x = baseX0;
        String y = "if(lt(t," + startSec + "+" + inDur + "),(-h)+((t-" + startSec + ")/" + inDur + ")*(" + baseY0 + "+h),if(lt(t," + endSec + "-" + outDur + ")," + baseY0 + ",(" + baseY0 + ")+((t-(" + endSec + "-" + outDur + "))/" + outDur + ")*(H-" + baseY0 + ")))";
        return new TextBoxEffect(x, y);
    }
}


