package com.qzc.downloader;

import com.qzc.downloader.listener.OnDownloadListener;
import com.qzc.downloader.listener.OnMultiThreadDownloadListener;
import com.qzc.downloader.manager.BaseDownloadManager;
import com.qzc.downloader.manager.DefaultDownloadManager;

/**
 * created by qzc at 2019/12/22 11:52
 * desc:
 */
public class Downloader extends BaseDownloadManager implements IDownLoader {

    private DefaultDownloadManager defaultDownloadManager;

    private Downloader() {

    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public void start() {
        defaultDownloadManager = new DefaultDownloadManager(
                urls,
                savePaths,
                fileNames,
                isRandom,
                isMultiThread,
                listener,
                multiThreadDownloadListener
        );
        defaultDownloadManager.start();
    }

    @Override
    public void cancel() {
        defaultDownloadManager.cancel();
    }

    private void setUrls(String[] urls) {
        this.urls = urls;
    }

    private void setSavePaths(String[] savePaths) {
        this.savePaths = savePaths;
    }

    private void setFileNames(String[] fileNames) {
        this.fileNames = fileNames;
    }

    private void setRandom(boolean random) {
        this.isRandom = random;
    }

    private void setMultiThread(boolean multiThread) {
        this.isMultiThread = multiThread;
    }

    private void setListener(OnDownloadListener listener) {
        this.listener = listener;
    }

    private void setMultiThreadListener(OnMultiThreadDownloadListener listener) {
        this.multiThreadDownloadListener = listener;
    }

    public static class Builder extends BaseDownloadManager {

        public Builder url(String... urls) {
            this.urls = urls;
            return this;
        }

        public Builder savePath(String... path) {
            this.savePaths = path;
            return this;
        }

        public Builder fileName(String... fileName) {
            this.fileNames = fileName;
            return this;
        }

        public Builder multiThread(boolean multiThread) {
            this.isMultiThread = multiThread;
            return this;
        }

        public Builder listener(OnDownloadListener listener) {
            this.listener = listener;
            return this;
        }

        public Builder multiThreadListener(OnMultiThreadDownloadListener listener) {
            this.multiThreadDownloadListener = listener;
            return this;
        }

        public Downloader start() {
            Downloader downloader = new Downloader();
            downloader.setUrls(urls);
            downloader.setSavePaths(savePaths);
            downloader.setFileNames(fileNames);
            downloader.setRandom(isRandom);
            downloader.setMultiThread(isMultiThread);
            downloader.setListener(listener);
            downloader.setMultiThreadListener(multiThreadDownloadListener);
            downloader.start();
            return downloader;
        }
    }
}
