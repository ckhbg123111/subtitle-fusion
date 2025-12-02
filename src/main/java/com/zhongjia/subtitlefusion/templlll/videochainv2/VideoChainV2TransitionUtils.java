package com.zhongjia.subtitlefusion.templlll.videochainv2;

import com.zhongjia.subtitlefusion.model.VideoChainV2Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * VideoChain V2 段间转场策略工具（临时方案，集中在 temp 包中）。
 *
 * <p>根据文档 {@code devDoc/videochainv2-interface.md} 中的转场名称列表，
 * 为相邻段随机挑选一个转场名称，并生成 {@link VideoChainV2Request.CapCutGapTransitionSpec}。</p>
 */
public final class VideoChainV2TransitionUtils {

    private static final List<String> TRANSITIONS = Arrays.asList(
            "_3D空间",
            "上移",
            "下移",
            "中心旋转",
            "云朵",
            "倒影",
            "冰雪结晶",
            "冲鸭",
            "分割"
    );

    private VideoChainV2TransitionUtils() {
    }

    /**
     * 随机挑选一个转场名称。
     */
    public static String pickRandomTransition() {
        int size = TRANSITIONS.size();
        if (size == 0) {
            return null;
        }
        int idx = ThreadLocalRandom.current().nextInt(size);
        return TRANSITIONS.get(idx);
    }

    /**
     * 为给定段数量构建随机转场配置列表。
     *
     * @param segmentCount 段数量
     * @return 长度为 {@code max(0, segmentCount - 1)} 的转场配置列表
     */
    public static List<VideoChainV2Request.CapCutGapTransitionSpec> buildRandomTransitions(int segmentCount) {
        int gaps = Math.max(0, segmentCount - 1);
        List<VideoChainV2Request.CapCutGapTransitionSpec> list = new ArrayList<>(gaps);
        for (int i = 0; i < gaps; i++) {
            String name = pickRandomTransition();
            if (name == null) {
                continue;
            }
            VideoChainV2Request.CapCutGapTransitionSpec spec = new VideoChainV2Request.CapCutGapTransitionSpec();
            spec.setTransition(name);
            // 默认 0.5 秒，VideoChainV2DraftWorkflowService 内部会再做安全边界校验
            spec.setDurationSec(0.5);
            list.add(spec);
        }
        return list;
    }
}


