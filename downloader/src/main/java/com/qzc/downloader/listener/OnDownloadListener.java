package com.qzc.downloader.listener;

import java.io.File;

/**
 * created by qzc at 2019/12/22 11:53
 * desc:
 */
public interface OnDownloadListener {
    void onStart();

    void onDownloading(int progress, int length, int progressP);

    void onCompleted(File file);

    void onCompletedAll();

    void onCancel();

    void onFailed(String message);
}
