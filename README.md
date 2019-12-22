# Downloader
一个简单易用的下载库

[![](https://jitpack.io/v/qzc0537/Downloader.svg)](https://jitpack.io/#qzc0537/Downloader)

使用
--
1.project build.gradle下添加：
maven { url 'https://jitpack.io' }

如下：

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

2.app build.gradle下添加依赖 ：

```
implementation 'com.github.qzc0537:Downloader:1.0.1'
```

3.愉快的使用：
```
    private String url = "http://nuuneoi.com/uploads/source/playstore/cover.jpg";
    private String url2 = "http://nuuneoi.com/uploads/source/playstore/cover.jpg";

    private void download() {
        String path = getExternalCacheDir().getAbsolutePath();
        Downloader.newBuilder()
                .url(url, url2)
                .savePath(path, path)
                .fileName("play.jpg", "play2.jpg")
                .multiThread(true)
                .multiThreadListener(new OnMultiThreadDownloadListener() {
                    @Override
                    public void onStart(String url) {
                        log("onStart->" + url);
                    }

                    @Override
                    public void onDownloading(String url, int progress, int length, int progressP) {
                        log("onDownloading->" + url + "\n" + progressP + "%");
                    }

                    @Override
                    public void onCompleted(String url, File file) {
                        log("onCompleted->" + url + "\n" + file.getAbsolutePath());
                    }

                    @Override
                    public void onCompletedAll() {
                        log("onCompletedAll");
                    }

                    @Override
                    public void onCancel() {
                        log("onCancel");
                    }

                    @Override
                    public void onFailed(String url, String message) {
                        log("onFailed->" + url + "\nmessage: " + message);
                    }
                })
//                .listener(new OnDownloadListener() {
//                    @Override
//                    public void onStart() {
//                        log("onStart");
//                    }
//
//                    @Override
//                    public void onDownloading(int progress, int length, int progressP) {
//                        log("onDownloading->" + progressP + "%");
//                    }
//
//                    @Override
//                    public void onCompleted(File file) {
//                        log("onCompleted->" + file.getAbsolutePath());
//                    }
//
//                    @Override
//                    public void onCompletedAll() {
//                        log("onCompletedAll");
//                    }
//
//                    @Override
//                    public void onCancel() {
//                        log("onCancel");
//                    }
//
//                    @Override
//                    public void onFailed(String message) {
//                        log("onFailed->" + message);
//                    }
//                })
                .start();
    }