package com.zhongjia.subtitlefusion.ffmpeg.transition;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单工厂：根据名称选择已注册的转场策略，默认使用 "xfade"（内部默认 transition 为 zoomin）。
 */
public final class TransitionStrategyFactory {

    private static final Map<String, TransitionStrategy> STRATEGY_REGISTRY = new HashMap<>();
    private static final String DEFAULT_STRATEGY_NAME = "xfade";
    static {
        registerStrategy(new XfadeZoomTransitionStrategy());
        // 后续可注册更多：registerStrategy(new XfadeFadeTransitionStrategy());
    }

    private TransitionStrategyFactory() {}

    public static void registerStrategy(TransitionStrategy s) {
        STRATEGY_REGISTRY.put(s.name().toLowerCase(), s);
    }

    public static TransitionStrategy getStrategy(String name) {
        String key = (name == null) ? DEFAULT_STRATEGY_NAME : name.toLowerCase();
        TransitionStrategy s = STRATEGY_REGISTRY.get(key);
        if (s == null) s = STRATEGY_REGISTRY.get(DEFAULT_STRATEGY_NAME);
        return s;
    }
}


