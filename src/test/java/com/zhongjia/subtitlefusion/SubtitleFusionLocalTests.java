package com.zhongjia.subtitlefusion;

import com.zhongjia.subtitlefusion.service.SubtitleFusionService;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootTest
public class SubtitleFusionLocalTests {

    @Autowired
    private SubtitleFusionService subtitleFusionService;

    /**
     * 测试Java2D方案（需要提供环境变量或系统属性）
     * 使用方式：
     * -DTEST_VIDEO_PATH=C:\path\to\video.mp4 -DTEST_SUB_PATH=C:\path\to\subtitle.srt
     */
    @Test
    void testJava2DSubtitleRendering() throws Exception {
        String videoPathStr = System.getProperty("TEST_VIDEO_PATH", System.getenv("TEST_VIDEO_PATH"));
        String subPathStr = System.getProperty("TEST_SUB_PATH", System.getenv("TEST_SUB_PATH"));

        Assumptions.assumeTrue(videoPathStr != null && !videoPathStr.isBlank(), "跳过测试：未提供 TEST_VIDEO_PATH");
        Assumptions.assumeTrue(subPathStr != null && !subPathStr.isBlank(), "跳过测试：未提供 TEST_SUB_PATH");

        Path videoPath = Paths.get(videoPathStr);
        Path subtitlePath = Paths.get(subPathStr);

        Assumptions.assumeTrue(Files.exists(videoPath), "视频文件不存在：" + videoPath);
        Assumptions.assumeTrue(Files.exists(subtitlePath), "字幕文件不存在：" + subtitlePath);

        System.out.println("开始测试Java2D字幕渲染...");
        String outputPath = subtitleFusionService.burnSrtViaJava2D(videoPath, subtitlePath);
        System.out.println("Java2D渲染完成，输出文件：" + outputPath);
        
        Assumptions.assumeTrue(Files.exists(Paths.get(outputPath)), "输出文件不存在：" + outputPath);
    }


}
