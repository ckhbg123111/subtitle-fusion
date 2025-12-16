package com.zhongjia.subtitlefusion.model;

import lombok.Data;

import java.util.List;

@Data
public class SubtitleInfo {
    private List<CommonSubtitleInfo> commonSubtitleInfoList;
    private SubtitleTemplate subtitleTemplate;
}
