package com.zhongjia.subtitlefusion.util;

import java.util.List;

public final class RandomUtils {
    public static <T> T chooseRandom(List<T> list) {
        if (list == null || list.isEmpty()) return null;
        int idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(list.size());
        return list.get(idx);
    }
}
