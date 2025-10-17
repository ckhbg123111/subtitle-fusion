package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.model.VideoChainRequest;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public VideoChainContext(String taskId, VideoChainRequest request) {
        this.taskId = taskId;
        this.request = request;
    }

    public String getTaskId() {
        return taskId;
    }

    public VideoChainRequest getRequest() {
        return request;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public List<Path> getTempFiles() {
        return tempFiles;
    }

    public List<Path> getSegmentOutputs() {
        return segmentOutputs;
    }

    public Path getBgm() {
        return bgm;
    }

    public void setBgm(Path bgm) {
        this.bgm = bgm;
    }

    public boolean isAnySegHasAudio() {
        return anySegHasAudio;
    }

    public void setAnySegHasAudio(boolean anySegHasAudio) {
        this.anySegHasAudio = anySegHasAudio;
    }

    public Double getBgmVolume() {
        return bgmVolume;
    }

    public void setBgmVolume(Double bgmVolume) {
        this.bgmVolume = bgmVolume;
    }

    public Double getBgmFadeInSec() {
        return bgmFadeInSec;
    }

    public void setBgmFadeInSec(Double bgmFadeInSec) {
        this.bgmFadeInSec = bgmFadeInSec;
    }

    public Double getBgmFadeOutSec() {
        return bgmFadeOutSec;
    }

    public void setBgmFadeOutSec(Double bgmFadeOutSec) {
        this.bgmFadeOutSec = bgmFadeOutSec;
    }

    public Path getFinalOut() {
        return finalOut;
    }

    public void setFinalOut(Path finalOut) {
        this.finalOut = finalOut;
    }
}


