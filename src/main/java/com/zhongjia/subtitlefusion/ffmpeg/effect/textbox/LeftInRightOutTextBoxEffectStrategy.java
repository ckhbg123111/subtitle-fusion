package com.zhongjia.subtitlefusion.ffmpeg.effect.textbox;

import org.springframework.stereotype.Component;

@Component
public class LeftInRightOutTextBoxEffectStrategy implements TextBoxEffectStrategy {
    @Override
    public TextBoxEffect build(String startSec, String endSec, String baseX0, String baseY0, int boxW, int boxH) {
        String inDur = "0.40";
        String outDur = "0.40";
        String stayX = baseX0 + "+(W*0.0045)*sin(2*PI*(t*0.35))";
        String stayY = baseY0 + "+(H*0.0045)*sin(2*PI*(t*0.40))";
        String x = "if(lt(t," + startSec + "+" + inDur + "),(-w)+((t-" + startSec + ")/" + inDur + ")*(" + baseX0 + "+w),if(lt(t," + endSec + "-" + outDur + ")," + stayX + ",(" + baseX0 + ")+((t-(" + endSec + "-" + outDur + "))/" + outDur + ")*(W-" + baseX0 + ")))";
        String y = stayY;
        return new TextBoxEffect(x, y);
    }
}


