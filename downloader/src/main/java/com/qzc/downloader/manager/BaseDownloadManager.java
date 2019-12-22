package com.qzc.downloader.manager;

import com.qzc.downloader.listener.OnDownloadListener;
import com.qzc.downloader.listener.OnMultiThreadDownloadListener;

/**
 * created by qzc at 2019/12/22 12:23
 * desc:
 */
public abstract class BaseDownloadManager {
    protected String[] urls;
    protected String[] savePaths;
    protected String[] fileNames;
    protected boolean isRandom;
    protected boolean isDownload = false;
    protected boolean isShutDown = false;
    protected boolean isMultiThread = false;
    protected OnDownloadListener listener;
    protected OnMultiThreadDownloadListener multiThreadDownloadListener;
    protected volatile static int task = 0;
    protected volatile static int taskSize = 0;

}
