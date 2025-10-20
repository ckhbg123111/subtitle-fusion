package com.zhongjia.subtitlefusion.ffmpeg.transition;

import java.nio.file.Path;
import java.util.List;

/**
 * 段间转场策略：负责根据输入段生成 filter_complex 与映射输出。
 * 实现需确保音视频参数一致化（像素格式/采样率/声道），并处理 offset 计算。
 */
public interface TransitionStrategy {

    /**
     * 使用策略将多个段拼接为一个成品。
     * @param inputs 段文件路径（按顺序）
     * @param transitionDurationSec 转场时长（秒）
     * @param output 输出文件
     * @return 包含完整命令的字符串数组（ffmpeg 命令），供执行器调用
     */
    String[] buildCommand(List<Path> inputs, double transitionDurationSec, Path output) throws Exception;

    /**
     * 策略名称（如 zoom / fade / wipeleft ...）
     */
    String name();
}


