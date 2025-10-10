package com.zhongjia.subtitlefusion.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "subtitlefusion")
public class AppProperties {

    private String outputDir = "output";
    private String tempDir = "temp";
    private Auth auth = new Auth();
    private Render render = new Render();

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    public Auth getAuth() {
        return auth;
    }

    public void setAuth(Auth auth) {
        this.auth = auth;
    }

    public Render getRender() {
        return render;
    }

    public void setRender(Render render) {
        this.render = render;
    }

    /**
     * 权限校验配置类
     */
    public static class Auth {
        private boolean enabled = true;
        private List<String> tokens;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }
    }

    /**
     * 渲染样式配置
     */
    public static class Render {
        /** 字体族（默认 Microsoft YaHei） */
        private String fontFamily = "Microsoft YaHei";
        /** 字体样式：plain|bold|italic|bolditalic */
        private String fontStyle = "plain";
        /** 指定字号像素（优先级最高） */
        private Integer fontSizePx;
        /** 在默认字号基础上的比例（0-若干） */
        private Float fontScale;
        /** 最小字号像素下限 */
        private Integer minFontSizePx = 14;
        /** 阴影/描边不透明度 (0-255)，默认 180 */
        private Integer shadowAlpha = 180;
        /** 阴影/描边半径（像素偏移范围），默认 2；0 表示不绘制阴影 */
        private Integer shadowRadiusPx = 2;
        /** 文本颜色（十六进制，如 #FFFFFF 或 rgb 十进制如 255,255,255）*/
        private String fontColor = "#FFFFFF";
        /** 阴影/描边颜色（默认黑色）*/
        private String shadowColor = "#000000";

        public String getFontFamily() {
            return fontFamily;
        }

        public void setFontFamily(String fontFamily) {
            this.fontFamily = fontFamily;
        }

        public String getFontStyle() {
            return fontStyle;
        }

        public void setFontStyle(String fontStyle) {
            this.fontStyle = fontStyle;
        }

        public Integer getFontSizePx() {
            return fontSizePx;
        }

        public void setFontSizePx(Integer fontSizePx) {
            this.fontSizePx = fontSizePx;
        }

        public Float getFontScale() {
            return fontScale;
        }

        public void setFontScale(Float fontScale) {
            this.fontScale = fontScale;
        }

        public Integer getMinFontSizePx() {
            return minFontSizePx;
        }

        public void setMinFontSizePx(Integer minFontSizePx) {
            this.minFontSizePx = minFontSizePx;
        }

        public Integer getShadowAlpha() {
            return shadowAlpha;
        }

        public void setShadowAlpha(Integer shadowAlpha) {
            this.shadowAlpha = shadowAlpha;
        }

        public Integer getShadowRadiusPx() {
            return shadowRadiusPx;
        }

        public void setShadowRadiusPx(Integer shadowRadiusPx) {
            this.shadowRadiusPx = shadowRadiusPx;
        }

        public String getFontColor() {
            return fontColor;
        }

        public void setFontColor(String fontColor) {
            this.fontColor = fontColor;
        }

        public String getShadowColor() {
            return shadowColor;
        }

        public void setShadowColor(String shadowColor) {
            this.shadowColor = shadowColor;
        }
    }
}


