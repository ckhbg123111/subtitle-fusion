package com.zhongjia.subtitlefusion.templlll.videochainv2;

import com.zhongjia.subtitlefusion.model.SubtitleTemplate;
import com.zhongjia.subtitlefusion.model.options.BasicTextOptions;
import com.zhongjia.subtitlefusion.model.options.CapCutTextAnimationEffectConfig;
import com.zhongjia.subtitlefusion.model.options.FlowerTextOptions;
import com.zhongjia.subtitlefusion.model.options.KeywordHighlightOptions;
import com.zhongjia.subtitlefusion.model.options.TextTemplateOptions;

import java.util.Collections;
import java.util.List;

/**
 * VideoChain V2 字幕/花字/关键字/基础文字模板的硬编码配置（临时方案）。
 *
 * <p>根据 {@code devDoc/videochainv2-interface.md} 中的说明，将花字、文字模板、关键字与基础文字
 * 统一封装为 {@link SubtitleTemplate}，供 VideoChain V2 场景复用。</p>
 */
public final class VideoChainV2SubtitleTemplates {

    private static final SubtitleTemplate DEFAULT_TEMPLATE;

    static {
        SubtitleTemplate template = new SubtitleTemplate();

        // 花字模板列表（文档“花字”段落）
        template.setFlowerTextOptions(buildFlowerTextOptions());
        // 文字模板（文档“文字模板”段落）
        template.setTextTemplateOptions(buildTextTemplateOptions());
        // 关键字高亮模板（文档“关键字”段落）
        template.setKeywordHighlightOptions(buildKeywordOptions());
        // 基础文字模板（文档“基础文字”段落）
        template.setBasicTextOptions(buildBasicTextOptions());

        DEFAULT_TEMPLATE = template;
    }

    private VideoChainV2SubtitleTemplates() {
    }

    /**
     * V2 统一使用的字幕模板：
     * <ul>
     *     <li>底部字幕：依赖 BASIC 文本配置；</li>
     *     <li>标题字：依赖花字/文字模板/关键字配置；</li>
     * </ul>
     */
    public static SubtitleTemplate defaultTemplate() {
        return DEFAULT_TEMPLATE;
    }

    private static List<FlowerTextOptions> buildFlowerTextOptions() {
        FlowerTextOptions o1 = new FlowerTextOptions();
        o1.setFont("半梦体");
        o1.setEffectId("Wk1vRFZWQFJGb1NUTFVKaUdRUA==");
        o1.setTransformX(0.0);
        o1.setTransformY(-0.6);
        o1.setTextIntro(buildIntro("轻微放大"));
        o1.setTextOutro(buildOutro("弹出"));

        FlowerTextOptions o2 = new FlowerTextOptions();
        o2.setFont("半梦体");
        o2.setEffectId("WklqRFJQSlZGalxTS1pBZ0VUVQ==");
        o2.setTransformX(0.0);
        o2.setTransformY(-0.6);
        o2.setTextIntro(buildIntro("左上弹入"));
        o2.setTextOutro(buildOutro("弹出"));

        FlowerTextOptions o3 = new FlowerTextOptions();
        o3.setFont("半梦体");
        o3.setEffectId("W0BoR11bQVBDa1xTTlROZkRdUA==");
        o3.setTransformX(0.0);
        o3.setTransformY(-0.6);
        o3.setTextIntro(buildIntro("右上弹入"));

        FlowerTextOptions o4 = new FlowerTextOptions();
        o4.setFont("半梦体");
        o4.setEffectId("WkptQ1BSQlxMaVpdS1hIbkpXUQ==");
        o4.setTransformX(0.0);
        o4.setTransformY(-0.6);

        FlowerTextOptions o5 = new FlowerTextOptions();
        o5.setFont("半梦体");
        o5.setEffectId("W0FmRVRQRVxGa1NQSlpNaUpQUQ==");
        o5.setTransformX(0.0);
        o5.setTransformY(-0.6);
        o5.setTextIntro(buildIntro("波浪弹入"));
        o5.setTextOutro(buildOutro("渐隐"));

        FlowerTextOptions o6 = new FlowerTextOptions();
        o6.setFont("半梦体");
        o6.setEffectId("WklnR1NSQ1FMaFhUSlRBZ0tSUg==");
        o6.setTransformX(0.0);
        o6.setTransformY(-0.6);
        o6.setTextIntro(buildIntro("波浪弹入"));
        o6.setTextOutro(buildOutro("渐隐"));

        FlowerTextOptions o7 = new FlowerTextOptions();
        o7.setFont("半梦体");
        o7.setEffectId("WklpS1RURFNNbVpWS1hKbkdcUQ==");
        o7.setTransformX(0.0);
        o7.setTransformY(-0.6);
        o7.setTextIntro(buildIntro("羽化向右擦开"));
        o7.setTextOutro(buildOutro("渐隐"));

        FlowerTextOptions o8 = new FlowerTextOptions();
        o8.setFont("半梦体");
        o8.setEffectId("W0BmQFNaQVJBbFlRTVlLbkBdUA==");
        o8.setTransformX(0.0);
        o8.setTransformY(-0.6);
        o8.setTextIntro(buildIntro("右上弹入"));
        o8.setTextOutro(buildOutro("弹出"));

        return java.util.Arrays.asList(o1, o2, o3, o4, o5, o6, o7, o8);
    }

    private static List<TextTemplateOptions> buildTextTemplateOptions() {
        TextTemplateOptions opt = new TextTemplateOptions();
        opt.setTemplateId("7163524521452948744");
        // 默认位置与底部字幕保持类似风格（略靠下）
        opt.setTransformX(0.0);
        opt.setTransformY(-0.6);
        return Collections.singletonList(opt);
    }

    private static List<KeywordHighlightOptions> buildKeywordOptions() {
        KeywordHighlightOptions opt = new KeywordHighlightOptions();
        opt.setFont("卡酷体");
        opt.setTransformX(0.0);
        opt.setTransformY(-0.6);
        opt.setBorderColor("#000000");
        opt.setBorderWidthRate(1);
        opt.setKeywordsFont("卡酷体");
        opt.setKeywordsColor("#52c41a");
        return Collections.singletonList(opt);
    }

    private static List<BasicTextOptions> buildBasicTextOptions() {
        BasicTextOptions opt = new BasicTextOptions();
        opt.setFont("卡酷体");
        opt.setFontColor("#ffffff");
        opt.setBorderColor("#000000");
        opt.setBorderWidthRate(2);
        opt.setTransformX(0.0);
        opt.setTransformY(-0.6);
        opt.setTextIntro(buildIntro("向上弹入"));
        return Collections.singletonList(opt);
    }

    private static CapCutTextAnimationEffectConfig buildIntro(String animation) {
        CapCutTextAnimationEffectConfig cfg = new CapCutTextAnimationEffectConfig();
        cfg.setAnimation(animation);
        // 时长若未指定，留空由下游兜底
        return cfg;
    }

    private static CapCutTextAnimationEffectConfig buildOutro(String animation) {
        CapCutTextAnimationEffectConfig cfg = new CapCutTextAnimationEffectConfig();
        cfg.setAnimation(animation);
        return cfg;
    }
}


