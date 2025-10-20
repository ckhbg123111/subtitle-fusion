package com.zhongjia.subtitlefusion.ffmpeg.transition;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单工厂：根据名称选择策略，默认 zoom。
 */
public final class TransitionStrategyFactory {

    private static final Map<String, TransitionStrategy> REGISTRY = new HashMap<>();
    static {
        register(new XfadeZoomTransitionStrategy());
        // 后续可注册更多：register(new XfadeFadeTransitionStrategy());
    }

    private TransitionStrategyFactory() {}

    public static void register(TransitionStrategy s) {
        REGISTRY.put(s.name().toLowerCase(), s);
    }

    public static TransitionStrategy get(String name) {
        if (name == null) name = "zoom";
        TransitionStrategy s = REGISTRY.get(name.toLowerCase());
        if (s == null) s = REGISTRY.get("zoom");
        return s;
    }
}


