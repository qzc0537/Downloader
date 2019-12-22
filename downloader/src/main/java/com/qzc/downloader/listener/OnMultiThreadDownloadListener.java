package com.qzc.downloader.listener;

import java.io.File;

/**
 * created by qzc at 2019/12/22 11:53
 * desc:
 */
public interface OnMultiThreadDownloadListener {
    void onStart(String url);

    void onDownloading(String url, int progress, int length, int progressP);

    void onCompleted(String url, File file);

    void onCompletedAll();

    void onCancel();

    void onFailed(String url, String message);
}
