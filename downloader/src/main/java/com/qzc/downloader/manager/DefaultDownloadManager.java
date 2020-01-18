package com.qzc.downloader.manager;

import android.util.Log;

import com.qzc.downloader.IDownLoader;
import com.qzc.downloader.bean.DownloadInfo;
import com.qzc.downloader.listener.OnDownloadListener;
import com.qzc.downloader.listener.OnMultiThreadDownloadListener;
import com.qzc.downloader.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * created by qzc at 2019/12/22 12:05
 * desc:
 */
public class DefaultDownloadManager extends BaseDownloadManager implements IDownLoader {

    private static final String TAG = "Downloader";
    private ConcurrentLinkedQueue<DownloadInfo> mDownloadInfoList;

    public DefaultDownloadManager(
            String[] urls,
            String[] savePaths,
            String[] fileNames,
            boolean isRandom,
            boolean isMultiThread,
            OnDownloadListener listener,
            OnMultiThreadDownloadListener listener2) {
        this.urls = urls;
        this.savePaths = savePaths;
        this.fileNames = fileNames;
        this.isRandom = isRandom;
        this.isMultiThread = isMultiThread;
        this.listener = listener;
        this.multiThreadDownloadListener = listener2;
    }

    @Override
    public void start() {
        if (urls == null || urls.length == 0) {
            if (listener != null) listener.onFailed("Url is empty");
            return;
        }
        if (savePaths == null || savePaths.length == 0) {
            if (listener != null) listener.onFailed("SavePath is empty");
            return;
        }
        if (fileNames == null || fileNames.length == 0) {
            if (listener != null) listener.onFailed("FileName is empty");
            return;
        }
        mDownloadInfoList = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < urls.length; i++) {
            DownloadInfo info = new DownloadInfo();
            info.setUrl(urls[i]);
            info.setSavePath(savePaths[i]);
            info.setFileName(fileNames[i]);
            info.setRandom(isRandom);
            mDownloadInfoList.offer(info);
        }
        if (isMultiThread) {
            taskSize = mDownloadInfoList.size();
            startDownloadMultiThread();
        } else {
            final DownloadInfo info = mDownloadInfoList.poll();
            startDownloadEnqueue(info);
        }
    }

    @Override
    public void cancel() {
        isShutDown = true;
    }

    /**
     * 主线程任务
     *
     * @param runnable
     */
    private void onMainThread(Runnable runnable) {
        AppExecutors.getInstance().mainThread().execute(runnable);
    }

    /**
     * 顺序下载
     */
    private void startDownloadEnqueue(final DownloadInfo info) {
        if (isDownload) {
            return;
        }
        isDownload = true;
        Log.d(TAG, "onStart->url: " + info.getUrl()
                + "\nfileName: " + info.getFileName()
                + "\nsavePath: " + info.getSavePath());
        if (listener != null) listener.onStart();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL urlNet = new URL(info.getUrl());
                    final HttpURLConnection con = (HttpURLConnection) urlNet.openConnection();
                    con.setRequestMethod("GET");
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Accept-Encoding", "identity");
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = con.getInputStream();
                        final int length = con.getContentLength();
                        int len;
                        //当前已下载完成的进度
                        int progress = 0;
                        int progressP = 0;
                        byte[] buffer = new byte[1024 * 2];
                        final File file = FileUtil.createFile(info.getSavePath(), info.getFileName());
                        FileOutputStream fos = new FileOutputStream(file);
                        while ((len = is.read(buffer)) != -1 && !isShutDown) {
                            //将获取到的流写入文件中
                            fos.write(buffer, 0, len);
                            progress += len;
                            progressP = (int) (progress * 1.0f / length * 100);
                            final int finalProgress = progress;
                            final int finalProgressP = progressP;
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "onDownloading->progress: " + finalProgressP);
                                    if (listener != null)
                                        listener.onDownloading(finalProgress, length, finalProgressP);
                                }
                            });
                        }
                        //完成io操作,释放资源
                        fos.flush();
                        fos.close();
                        is.close();
                        con.disconnect();
                        if (isShutDown) {
                            //取消了下载 同时再恢复状态
                            isShutDown = false;
                            file.deleteOnExit();
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "onCancel");
                                    if (listener != null) listener.onCancel();
                                }
                            });
                        } else {
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "onCompleted->path: " + file.getAbsolutePath());
                                    if (listener != null) listener.onCompleted(file);
                                    isDownload = false;
                                    DownloadInfo info1 = mDownloadInfoList.poll();
                                    if (info1 != null) {
                                        startDownloadEnqueue(info1);
                                    } else {
                                        if (listener != null) listener.onCompletedAll();
                                    }
                                }
                            });
                        }
                    } else {
                        con.disconnect();
                        final String msg = con.getResponseMessage();
                        onMainThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e(TAG, "onFailed->message: " + msg);
                                if (listener != null) listener.onFailed(msg);
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    onMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "onFailed->message: " + e.getMessage());
                            if (listener != null) listener.onFailed(e.getMessage());
                        }
                    });
                }
            }
        };
        AppExecutors.getInstance().diskIO().execute(runnable);
    }

    /**
     * 多线程下载
     */
    private void startDownloadMultiThread() {
        while (!mDownloadInfoList.isEmpty()) {
            Runnable runnable = multiThreadTask(mDownloadInfoList.poll());
            AppExecutors.getInstance().networkIO().execute(runnable);
        }
    }

    /**
     * 多线程下载任务
     *
     * @param info
     * @return
     */
    private Runnable multiThreadTask(final DownloadInfo info) {
        if (multiThreadDownloadListener != null) {
            multiThreadDownloadListener.onStart(info.getUrl());
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    URL urlNet = new URL(info.getUrl());
                    final HttpURLConnection con = (HttpURLConnection) urlNet.openConnection();
                    con.setRequestMethod("GET");
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Accept-Encoding", "identity");
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream is = con.getInputStream();
                        final int length = con.getContentLength();
                        int len;
                        //当前已下载完成的进度
                        int progress = 0;
                        int progressP = 0;
                        byte[] buffer = new byte[1024 * 2];
                        final File file = FileUtil.createFile(info.getSavePath(), info.getFileName());
                        FileOutputStream fos = new FileOutputStream(file);
                        while ((len = is.read(buffer)) != -1 && !isShutDown) {
                            //将获取到的流写入文件中
                            fos.write(buffer, 0, len);
                            progress += len;
                            progressP = (int) (progress * 1.0f / length * 100);
                            final int finalProgress = progress;
                            final int finalProgressP = progressP;
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (multiThreadDownloadListener != null) {
                                        multiThreadDownloadListener.onDownloading(info.getUrl(),
                                                finalProgress, length, finalProgressP);
                                    }
                                }
                            });
                        }
                        //完成io操作,释放资源
                        fos.flush();
                        fos.close();
                        is.close();
                        con.disconnect();
                        if (isShutDown) {
                            //取消了下载 同时再恢复状态
                            isShutDown = false;
                            file.deleteOnExit();
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (multiThreadDownloadListener != null) {
                                        multiThreadDownloadListener.onCancel();
                                    }
                                }
                            });
                        } else {
                            onMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (multiThreadDownloadListener != null) {
                                        multiThreadDownloadListener.onCompleted(info.getUrl(), file);
                                    }
                                    task++;
                                    if (task == taskSize) {
                                        if (multiThreadDownloadListener != null) {
                                            multiThreadDownloadListener.onCompletedAll();
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        con.disconnect();
                        final String msg = con.getResponseMessage();
                        onMainThread(new Runnable() {
                            @Override
                            public void run() {
                                if (multiThreadDownloadListener != null) {
                                    multiThreadDownloadListener.onFailed(info.getUrl(), msg);
                                }
                            }
                        });
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    onMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (multiThreadDownloadListener != null) {
                                multiThreadDownloadListener.onFailed(info.getUrl(), e.getMessage());
                            }
                        }
                    });
                }
            }
        };
        return runnable;
    }
}
