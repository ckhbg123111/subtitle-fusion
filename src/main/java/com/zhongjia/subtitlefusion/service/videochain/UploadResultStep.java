package com.zhongjia.subtitlefusion.service.videochain;

import com.zhongjia.subtitlefusion.model.TaskState;
import com.zhongjia.subtitlefusion.service.DistributedTaskManagementService;
import com.zhongjia.subtitlefusion.service.MinioService;
import com.zhongjia.subtitlefusion.util.MediaIoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

        // 1) 先打包素材资源（包含成品视频、原始片段、字幕、图片）
        Path zipPath = createResourcesZip(ctx);

        // 2) 上传成品视频
        String videoUrl;
        try (InputStream in = new FileInputStream(finalOut.toFile())) {
            videoUrl = minio.uploadToPublicBucket(in, finalOut.toFile().length(), finalOut.getFileName().toString());
        }

        // 3) 上传资源压缩包
        String zipUrl;
        try (InputStream in = new FileInputStream(zipPath.toFile())) {
            zipUrl = minio.uploadToPublicBucket(in, zipPath.toFile().length(), zipPath.getFileName().toString());
        }

        // 4) 更新任务完成并记录两个URL
        tasks.markTaskCompleted(taskId, videoUrl, zipUrl);

        // 5) 清理本地文件
        MediaIoUtils.safeDelete(finalOut);
        MediaIoUtils.safeDelete(zipPath);
    }

    private Path createResourcesZip(VideoChainContext ctx) throws Exception {
        String taskId = ctx.getTaskId();
        Path workDir = ctx.getWorkDir();
        Path finalOut = ctx.getFinalOut();

        Path zip = workDir.resolve("resources_" + taskId + ".zip");

        Set<Path> originals = new HashSet<>();
        Set<Path> subtitles = new HashSet<>();
        Set<Path> images = new HashSet<>();

        for (Path p : ctx.getTempFiles()) {
            if (p == null || !Files.exists(p)) continue;
            String name = p.getFileName().toString().toLowerCase();
            boolean underWorkDir = workDir != null && p.normalize().startsWith(workDir.normalize());
            if (isVideo(name)) {
                // 仅收集原始下载的小视频片段：排除工作目录内生成物
                if (!underWorkDir) originals.add(p);
            } else if (isSubtitle(name)) {
                subtitles.add(p);
            } else if (isImage(name)) {
                images.add(p);
            }
        }

        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip.toFile()))) {
            // 成品视频
            addFileToZip(zos, finalOut, "final/" + finalOut.getFileName().toString());
            // 原始片段
            for (Path p : originals) addFileToZip(zos, p, "segments/" + p.getFileName().toString());
            // 字幕
            for (Path p : subtitles) addFileToZip(zos, p, "subtitles/" + p.getFileName().toString());
            // 图片
            for (Path p : images) addFileToZip(zos, p, "images/" + p.getFileName().toString());
            
        }

        return zip;
    }

    private void addFileToZip(ZipOutputStream zos, Path file, String entryName) throws Exception {
        if (file == null || !Files.exists(file)) return;
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        Files.copy(file, zos);
        zos.closeEntry();
    }

    private boolean isVideo(String name) {
        return name.endsWith(".mp4") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".avi")
                || name.endsWith(".flv") || name.endsWith(".webm") || name.endsWith(".m4v") || name.endsWith(".ts");
    }

    private boolean isSubtitle(String name) {
        return name.endsWith(".srt") || name.endsWith(".ass") || name.endsWith(".vtt");
    }

    private boolean isImage(String name) {
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".webp");
    }

    
}


