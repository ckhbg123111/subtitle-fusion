package com.zhongjia.subtitlefusion.model;

public class UploadResult {
    private final String baseUrl;
    private final String path;

    public UploadResult(String baseUrl, String path) {
        this.baseUrl = baseUrl;
        this.path = path;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        if (baseUrl == null) return path;
        if (path == null) return baseUrl;
        boolean baseHasSlash = baseUrl.endsWith("/");
        boolean pathHasSlash = path.startsWith("/");
        if (baseHasSlash && pathHasSlash) {
            return baseUrl + path.substring(1);
        }
        if (!baseHasSlash && !pathHasSlash) {
            return baseUrl + "/" + path;
        }
        return baseUrl + path;
    }
}

