package com.zhongjia.subtitlefusion.util;

import java.util.concurrent.ThreadLocalRandom;

public final class ColorUtils {

    private ColorUtils() {}

    public static String randomBrightColor() {
        String[] palette = new String[] {
                "#FFD400",
                "#FF5C5C",
                "#00D1FF",
                "#8A2BE2",
                "#00E676",
                "#FF7F50",
                "#FFA500"
        };
        int idx = ThreadLocalRandom.current().nextInt(palette.length);
        return palette[idx];
    }
}


