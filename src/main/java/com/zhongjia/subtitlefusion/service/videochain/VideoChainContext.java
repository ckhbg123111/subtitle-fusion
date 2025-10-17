package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
@RequiredArgsConstructor
public class VideoChainContext {

    private final String taskId;
    private final VideoChainRequest request;

    private Path workDir;
    private final List<Path> tempFiles = new ArrayList<>();
    private final List<Path> segmentOutputs = new ArrayList<>();

    // BGM related
    private Path bgm;
    private boolean anySegHasAudio;
    private Double bgmVolume;
    private Double bgmFadeInSec;
    private Double bgmFadeOutSec;

    private Path finalOut;
}


