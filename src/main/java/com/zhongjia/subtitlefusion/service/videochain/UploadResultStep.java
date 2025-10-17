package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.file.Path;

@Component
@Order(50)
public class UploadResultStep implements VideoChainStep {

    @Autowired
    private DistributedTaskManagementService tasks;
    @Autowired
    private MinioService minio;

    @Override
    public String name() {
        return "UploadResult";
    }

    @Override
    public void execute(VideoChainContext ctx) throws Exception {
        String taskId = ctx.getTaskId();
        tasks.updateTaskProgress(taskId, TaskState.UPLOADING, 90, "上传到对象存储");
        Path finalOut = ctx.getFinalOut();
        String objectUrl = minio.uploadToPublicBucket(new FileInputStream(finalOut.toFile()), finalOut.toFile().length(), finalOut.getFileName().toString());
        tasks.markTaskCompleted(taskId, objectUrl);
        MediaIoUtils.safeDelete(finalOut);
    }
}


