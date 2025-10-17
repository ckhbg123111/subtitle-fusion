package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.config.AppProperties;
import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.model.VideoChainRequest;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.FileDownloadService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Order(10)
public class PrepareWorkspaceAndBgmStep implements VideoChainStep {

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private FileDownloadService downloader;
    @Autowired
    private AppProperties props;

    @Override
    public String name() {
        return "PrepareWorkspaceAndBgm";
    }

    @Override
    public void execute(VideoChainContext ctx) throws Exception {
        String taskId = ctx.getTaskId();
        tasks.updateTaskProgress(taskId, TaskState.DOWNLOADING, 5, "开始下载素材");

        Path workDir = MediaIoUtils.ensureWorkDir(props, taskId);
        ctx.setWorkDir(workDir);

        VideoChainRequest req = ctx.getRequest();
        Path bgm = null;
        Double bgmVolume = null;
        Double bgmFadeInSec = null;
        Double bgmFadeOutSec = null;

        if (req.getBgmInfo() != null) {
            String bgmUrl = req.getBgmInfo().getBackgroundMusicUrl();
            bgmVolume = req.getBgmInfo().getBgmVolume();
            bgmFadeInSec = req.getBgmInfo().getBgmFadeInSec();
            bgmFadeOutSec = req.getBgmInfo().getBgmFadeOutSec();
            if (bgmUrl != null && !bgmUrl.isEmpty()) {
                bgm = downloader.downloadFile(bgmUrl, ".m4a");
                if (bgm != null) ctx.getTempFiles().add(bgm);
            }
        }

        ctx.setBgm(bgm);
        ctx.setBgmVolume(bgmVolume);
        ctx.setBgmFadeInSec(bgmFadeInSec);
        ctx.setBgmFadeOutSec(bgmFadeOutSec);
        ctx.setAnySegHasAudio(false);
    }
}


