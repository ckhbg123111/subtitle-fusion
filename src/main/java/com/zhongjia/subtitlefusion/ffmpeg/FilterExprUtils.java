package com.zhongjia.subtitlefusion.ffmpeg;

import java.util.Locale;

/**
 * FFmpeg 滤镜表达式与通用字符串工具。
 */
public final class FilterExprUtils {

    private FilterExprUtils() {}

    public static String toSeconds(String t) {
        if (t == null || t.isEmpty()) return "0";
        try {
            if (t.contains(":")) {
                String[] ps = t.split(":");
                double h = Double.parseDouble(ps[0]);
                double m = Double.parseDouble(ps[1]);
                double s = Double.parseDouble(ps[2]);
                return String.format(Locale.US, "%.3f", h * 3600 + m * 60 + s);
            }
            return String.format(Locale.US, "%.3f", Double.parseDouble(t));
        } catch (Exception e) {
            return "0";
        }
    }

    public static String calcDuration(String startSec, String endSec) {
        try {
            double s = Double.parseDouble(startSec);
            double e = Double.parseDouble(endSec);
            double d = Math.max(0d, e - s);
            return String.format(java.util.Locale.US, "%.3f", d);
        } catch (Exception ignore) {
            return "0";
        }
    }

    public static String escapeExpr(String expr) {
        if (expr == null) return "";
        return expr
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace(",", "\\,");
    }

    public static String escapeText(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace(":", "\\:");
    }

    public static String escapeFilterPath(String path) {
        if (path == null) return "";
        String normalized = path.replace("\\", "/");
        normalized = normalized.replace(":", "\\:");
        normalized = normalized.replace("'", "\\'");
        return normalized;
    }

    public static String safe(String v, String def) {
        return (v == null || v.isEmpty()) ? def : v;
    }
}


