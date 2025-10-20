package com.zhongjia.subtitlefusion.ffmpeg.transition;

import java.nio.file.Path;
import java.util.List;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;

/**
 * 段间转场策略：负责根据输入段生成 filter_complex 与映射输出。
 * 实现需确保音视频参数一致化（像素格式/采样率/声道），并处理 offset 计算。
 */
public interface TransitionStrategy {

    // 仅保留逐段配置版本

    /**
     * 使用逐段配置的转场策略将多个段拼接为一个成品。
     * @param inputs 段文件路径（按顺序）
     * @param gaps 与段间一一对应的配置（长度应为 inputs.size()-1），为 null 表示不使用转场
     * @param output 输出文件
     */
    String[] buildCommand(List<Path> inputs, List<VideoChainRequest.GapTransitionSpec> gaps, Path output) throws Exception;

    /**
     * 策略名称（如 xfade / fade / 其他自定义产品 ...）
     */
    String name();
}


