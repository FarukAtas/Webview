package com.oynfrm.webview;

import static android.content.Context.DOWNLOAD_SERVICE;
import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.WebView;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

public class WebDownloader {
    public String sFileName,sUA,sURL,sMM;
    private Activity context;
    public static int downloadCode = 917346;
    private static StateListener stateListener;

    public WebDownloader(Activity context){
        this.context = context;
    }

    public void setTo(WebView webView){
        webView.setDownloadListener(new DownloadListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDownloadAvailable(String url, String userAgent, String contentDisposition,
                                            String mimeType, long size, String filename) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED){
                    startDownload(url,userAgent,filename,mimeType);
                }else {
                    context.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},downloadCode);
                    sFileName = filename;
                    sUA = userAgent;
                    sURL = url;
                    sMM = mimeType;
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startDownload(String url, String us, String filename, String mm){
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("User-Agent",us)
                .addRequestHeader("cookie",cookies)
                .setTitle(filename)
                .setDescription(url)
                .setAllowedOverRoaming(true)
                .setAllowedOverMetered(true)
                .setMimeType(mm)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,filename)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        try {
            request.allowScanningByMediaScanner();
            downloadManager.enqueue(request);
            stateListener.onDownloadStart(url,mm,filename);
            sFileName = "";
            sURL = "";
            sUA = "";
            sMM = "";
        }catch (Exception e){
            stateListener.onDownloadFailed(e.toString());
        }
    }

    public interface StateListener{
        void onDownloadStart(String url, String mimeType, String filename);
        void onDownloadFailed(String errorMessage);
    }

    public void setDownloadListener(StateListener stateListener){
        WebDownloader.stateListener = stateListener;
    }

}
