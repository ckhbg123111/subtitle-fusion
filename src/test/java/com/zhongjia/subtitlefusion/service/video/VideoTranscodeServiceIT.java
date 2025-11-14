package com.zhongjia.subtitlefusion.service.video;

import com.zhongjia.subtitlefusion.util.MediaProbeUtils;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class VideoTranscodeServiceIT {

    @Autowired
    private VideoTranscodeService service;

    @Test
    void hevcInputShouldTranscodeToH264WhenEnvProvided() throws Exception {
        String sample = System.getenv("TEST_HEVC_SAMPLE");
        Assumptions.assumeTrue(sample != null && !sample.isBlank(), "Skip: TEST_HEVC_SAMPLE not set");

        Path input = Path.of(sample);
        Assumptions.assumeTrue(Files.exists(input), "Skip: sample file not found");

        Path out = service.transcodeIfNeeded(input);
        assertTrue(Files.exists(out), "Output should exist");

        String codec = MediaProbeUtils.probeVideoCodecName(out);
        assertTrue("h264".equalsIgnoreCase(codec), "Output codec should be h264 but was " + codec);

        // 清理产物
        try { Files.deleteIfExists(out); } catch (Exception ignore) {}
    }
}


