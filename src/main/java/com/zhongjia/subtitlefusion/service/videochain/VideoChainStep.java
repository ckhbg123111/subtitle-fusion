package com.zhongjia.subtitlefusion.service.videochain;

public interface VideoChainStep {
    String name();
    void execute(VideoChainContext ctx) throws Exception;
}


