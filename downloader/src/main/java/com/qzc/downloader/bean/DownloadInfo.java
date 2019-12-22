package com.qzc.downloader.bean;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * created by qzc at 2019/12/22 14:31
 * desc:
 */
public class DownloadInfo {
    private int id = new AtomicInteger(100).getAndIncrement();
    private String url;
    private String savePath;
    private String fileName;
    private boolean isRandom;

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public void setRandom(boolean random) {
        isRandom = random;
    }
}
