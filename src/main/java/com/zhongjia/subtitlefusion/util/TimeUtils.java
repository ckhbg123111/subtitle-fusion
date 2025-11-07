package com.zhongjia.subtitlefusion.util;

public final class TimeUtils {

    private TimeUtils() {}

    public static double parseToSeconds(String s) {
        if (s == null) return 0.0;
        s = s.trim();
        if (s.isEmpty()) return 0.0;
        try {
            if (s.contains(":")) {
                String[] parts = s.split(":");
                if (parts.length == 3) {
                    int hh = parseIntSafe(parts[0]);
                    int mm = parseIntSafe(parts[1]);
                    double ss = parseSecondWithFraction(parts[2]);
                    return hh * 3600.0 + mm * 60.0 + ss;
                } else if (parts.length == 2) {
                    int mm = parseIntSafe(parts[0]);
                    double ss = parseSecondWithFraction(parts[1]);
                    return mm * 60.0 + ss;
                }
            }
            return Double.parseDouble(s.replace(',', '.'));
        } catch (Exception ignore) {
            return 0.0;
        }
    }

    public static int parseIntSafe(String v) {
        if (v == null || v.isEmpty()) return 0;
        v = v.trim();
        int dot = v.indexOf('.');
        int comma = v.indexOf(',');
        int cut = -1;
        if (dot >= 0 && comma >= 0) cut = Math.min(dot, comma);
        else if (dot >= 0) cut = dot;
        else if (comma >= 0) cut = comma;
        if (cut >= 0) v = v.substring(0, cut);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static double parseSecondWithFraction(String secondPart) {
        if (secondPart == null || secondPart.isEmpty()) return 0.0;
        secondPart = secondPart.trim();
        String normalized = secondPart.replace(',', '.');
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return parseIntSafe(secondPart);
        }
    }
}


