package com.zhongjia.subtitlefusion.service.videochainv2;

import com.zhongjia.subtitlefusion.model.CommonSubtitleInfo;
import com.zhongjia.subtitlefusion.model.SubtitleInfo;
import com.zhongjia.subtitlefusion.util.TimeUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 字幕时间轴处理工具：分段→全局的时间偏移与合并
 */
public final class SubtitleTimelineUtils {

    private SubtitleTimelineUtils() {}

    /**
     * 将单段字幕整体偏移（秒），返回新对象
     */
    public static SubtitleInfo offset(SubtitleInfo origin, double offsetSec) {
        if (origin == null) return null;
        SubtitleInfo out = new SubtitleInfo();
        out.setSubtitleTemplate(origin.getSubtitleTemplate());
        if (origin.getCommonSubtitleInfoList() == null) {
            out.setCommonSubtitleInfoList(new ArrayList<>());
            return out;
        }
        List<CommonSubtitleInfo> list = new ArrayList<>();
        for (CommonSubtitleInfo it : origin.getCommonSubtitleInfoList()) {
            if (it == null) continue;
            CommonSubtitleInfo ni = new CommonSubtitleInfo();
            ni.setText(it.getText());
            ni.setSubtitleEffectInfo(it.getSubtitleEffectInfo());
            double s = TimeUtils.parseToSeconds(it.getStartTime()) + offsetSec;
            double e = TimeUtils.parseToSeconds(it.getEndTime()) + offsetSec;
            if (e <= s) e = s + 0.5;
            ni.setStartTime(Double.toString(Math.max(0.0, s)));
            ni.setEndTime(Double.toString(Math.max(0.0, e)));
            list.add(ni);
        }
        out.setCommonSubtitleInfoList(list);
        return out;
    }

    /**
     * 合并多个字幕集合为一个（模板以第一个非空的为准）
     */
    public static SubtitleInfo merge(List<SubtitleInfo> infos) {
        SubtitleInfo out = new SubtitleInfo();
        List<CommonSubtitleInfo> all = new ArrayList<>();
        out.setCommonSubtitleInfoList(all);
        if (infos == null) return out;
        for (SubtitleInfo si : infos) {
            if (si == null || si.getCommonSubtitleInfoList() == null) continue;
            if (out.getSubtitleTemplate() == null && si.getSubtitleTemplate() != null) {
                out.setSubtitleTemplate(si.getSubtitleTemplate());
            }
            all.addAll(si.getCommonSubtitleInfoList());
        }
        return out;
    }
}


