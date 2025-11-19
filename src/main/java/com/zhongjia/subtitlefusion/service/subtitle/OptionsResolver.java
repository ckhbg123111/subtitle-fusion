 package com.zhongjia.subtitlefusion.service.subtitle;
 
 import com.zhongjia.subtitlefusion.model.options.CapCutTextAnimationEffectConfig;
 import com.zhongjia.subtitlefusion.model.options.StrategyOptions;
 import lombok.Getter;
 
 class OptionsResolver {
 
     @Getter
     static class Effective {
        private final String font;
        private final String fontColor;
        private final int fontSize;
        private final int borderWidth;
        private final String borderColor;
        private final Double transformX;
        private final Double transformY;
        private final String introAnimation;
        private final Double introDuration;
        private final String outroAnimation;
        private final Double outroDuration;
        private final boolean shadowEnabled;
        private final double shadowAlpha;

        Effective(String font,
                  String fontColor,
                  int fontSize,
                  int borderWidth,
                  String borderColor,
                  Double transformX,
                  Double transformY,
                  String introAnimation,
                  Double introDuration,
                  String outroAnimation,
                  Double outroDuration,
                  boolean shadowEnabled,
                  double shadowAlpha) {
            this.font = font;
            this.fontColor = fontColor;
            this.fontSize = fontSize;
            this.borderWidth = borderWidth;
            this.borderColor = borderColor;
            this.transformX = transformX;
            this.transformY = transformY;
            this.introAnimation = introAnimation;
            this.introDuration = introDuration;
            this.outroAnimation = outroAnimation;
            this.outroDuration = outroDuration;
            this.shadowEnabled = shadowEnabled;
            this.shadowAlpha = shadowAlpha;
        }
    }

     static class Defaults {
        String font = "匹喏曹";
        String fontColor = "#FFFFFF";
        String borderColor = "#000000";
        Double transformX = null;
        Double transformY = -0.6;
        int baseFontSize;
        int baseBorderWidth;
        String introAnimation = null;
        String outroAnimation = null;
        double defaultAnimDuration = 0.2;
        boolean shadowEnabled = true;
        double shadowAlpha = 0.8;
    }

     static Effective resolve(StrategyOptions opts, Defaults d, int canvasWidth, int canvasHeight) {
        // 计算基础尺寸（已由策略提供）
        int fontSize = d.baseFontSize;
        int borderWidth = d.baseBorderWidth;

         if (opts != null) {
            // 比例修正
             if (opts.getFontSizeRate() != null && opts.getFontSizeRate() > 0) {
                 fontSize = Math.max(1, (int) Math.round(fontSize * (opts.getFontSizeRate() / 100.0)));
            }
             if (opts.getBorderWidthRate() != null && opts.getBorderWidthRate() > 0) {
                 borderWidth = Math.max(0, (int) Math.round(borderWidth * (opts.getBorderWidthRate() / 100.0)));
            }
        }

        // 文本与颜色
         String font = coalesce(opts != null ? opts.getFont() : null, d.font);
         String fontColor = coalesce(opts != null ? opts.getFontColor() : null, d.fontColor);
         String borderColor = coalesce(opts != null ? opts.getBorderColor() : null, d.borderColor);

        // 位置
         Double transformX = coalesce(opts != null ? opts.getTransformX() : null, d.transformX);
         Double transformY = coalesce(opts != null ? opts.getTransformY() : null, d.transformY);

        // 动效
        String introName = d.introAnimation;
        Double introDur = null;
        String outroName = d.outroAnimation;
        Double outroDur = null;
         if (opts != null) {
             CapCutTextAnimationEffectConfig intro = opts.getTextIntro();
            if (intro != null) {
                introName = coalesce(intro.getAnimation(), introName);
                if (intro.getDuration() != null && intro.getDuration() > 0) introDur = intro.getDuration();
            }
             CapCutTextAnimationEffectConfig outro = opts.getTextOutro();
            if (outro != null) {
                outroName = coalesce(outro.getAnimation(), outroName);
                if (outro.getDuration() != null && outro.getDuration() > 0) outroDur = outro.getDuration();
            }
        }
        if (introName != null && introDur == null) introDur = d.defaultAnimDuration;
        if (outroName != null && outroDur == null) outroDur = d.defaultAnimDuration;

        return new Effective(
                font, fontColor, fontSize, borderWidth, borderColor,
                transformX, transformY, introName, introDur, outroName, outroDur,
                d.shadowEnabled, d.shadowAlpha
        );
    }

    private static <T> T coalesce(T a, T b) {
        return a != null ? a : b;
    }
}


