package com.zhongjia.subtitlefusion.service.subtitle;

import com.zhongjia.subtitlefusion.model.CommonSubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.util.TimeUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 负责将时间轴可能重叠的字幕分配到不同车道（lane），
 * 使每个车道内时间段不重叠；并提供按车道计算 transform_y 的方法。
 */
@Component
public class SubtitleLanePlanner {

    /**
     * 基础行位（靠底部），负值表示向上偏移。与现有模板默认值保持一致。
     */
    private static final double BASE_Y = -0.55;

    /**
     * 相邻车道的垂直间距（向上为负方向，这里用相减叠加）。
     */
    private static final double STEP_Y = 0.12;

    /**
     * Y 位置的夹紧上限与下限，避免超出画面可视区域。
     */
    private static final double MIN_Y = -0.95;
    private static final double MAX_Y = -0.25;

    /**
     * 标题字幕的基准行位（画面中上部），正值表示向上偏移。
     * 这里选择 0.25 作为第 0 行中心，大致对应“画面中上部”区域。
     */
    private static final double TITLE_BASE_Y = 0.25;

    /**
     * 标题字幕的基准 X 位置，偏向画面左侧。
     * 坐标原点为画布中心，范围通常为 [-1, 1]，这里选择 -0.4 使标题略偏左但不过于贴边。
     */
    private static final double TITLE_BASE_X = -0.4;

    /**
     * 标题相邻车道的垂直间距。
     * 由于标题一般行数较少，步长可以略大一些，保证行间距充足。
     */
    private static final double TITLE_STEP_Y = 0.14;

    /**
     * 标题 Y 位置的夹紧上限与下限，保证不与底部区域（负值区间）发生重叠。
     * 这里只做“中上部”范围约束，避免过于贴顶。
     */
    private static final double TITLE_MIN_Y = 0.05;
    private static final double TITLE_MAX_Y = 0.85;

    /**
     * 根据字幕时间段分配车道索引。要求入参已按开始时间升序。
     *
     * @param items 字幕条目（已按开始时间排序）
     * @return 与 items 同长度的车道索引数组
     */
    public int[] planLanes(List<CommonSubtitleInfo> items) {
        if (items == null || items.isEmpty()) {
            return new int[0];
        }
        List<Double> laneEnds = new ArrayList<>();
        int[] lanes = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            CommonSubtitleInfo si = items.get(i);
            double s = TimeUtils.parseToSeconds(si != null ? si.getStartTime() : null);
            double e = TimeUtils.parseToSeconds(si != null ? si.getEndTime() : null);
            if (Double.isNaN(s)) s = 0d;
            if (Double.isNaN(e) || e <= s) e = s + 1.0;

            int lane = findFirstNonOverlappingLane(laneEnds, s);
            if (lane == laneEnds.size()) {
                laneEnds.add(e);
            } else {
                laneEnds.set(lane, e);
            }
            lanes[i] = lane;
        }
        return lanes;
    }

    /**
     * 计算给定车道的 transform_y。
     * 若后续需要可改为依赖画布尺寸自适应步进。
     *
     * @param laneIndex 车道索引（从 0 起）
     * @return 合理范围内的 transform_y
     */
    public double transformYForLane(int laneIndex) {
        double y = BASE_Y - laneIndex * STEP_Y;
        if (y < MIN_Y) return MIN_Y;
        if (y > MAX_Y) return MAX_Y;
        return y;
    }

    /**
     * 计算“标题车道”的 transform_y。
     * 布局目标：将标题集中放置在画面中上部区域（约 0.2~0.3 起步），
     * 与底部字幕（负值区域）完全错开，避免重叠或挤在一起。
     *
     * @param laneIndex 车道索引（从 0 起）
     * @return 合理范围内的 transform_y（中上部）
     */
    public double transformYForTitleLane(int laneIndex) {
        double y = TITLE_BASE_Y + laneIndex * TITLE_STEP_Y;
        if (y < TITLE_MIN_Y) return TITLE_MIN_Y;
        if (y > TITLE_MAX_Y) return TITLE_MAX_Y;
        return y;
    }

    /**
     * 标题默认的 X 位置（偏左），供标题轨道在未指定 transform_x 时复用。
     *
     * @return 标题基准 X
     */
    public double getTitleBaseX() {
        return TITLE_BASE_X;
    }

    private int findFirstNonOverlappingLane(List<Double> laneEndTimes, double start) {
        for (int i = 0; i < laneEndTimes.size(); i++) {
            if (start >= laneEndTimes.get(i)) {
                return i;
            }
        }
        return laneEndTimes.size();
    }
}


