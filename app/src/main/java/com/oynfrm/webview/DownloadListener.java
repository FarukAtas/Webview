package com.oynfrm.webview;

import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DownloadListener implements android.webkit.DownloadListener {
    private static final Pattern PATTERN1 =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN2 =
            Pattern.compile("attachment;\\s*filename\\*=UTF-8''(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN3 =
            Pattern.compile("attachment;filename=\"(\"?)([^\"]*)\\1\\s*\";filename\\*\\=(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE);


    public DownloadListener() {}

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDownloadStart(String s1, String s2, String s3, String s4, long l) {
        String mimeType = "";
        if (!TextUtils.isEmpty(s4)) {
            mimeType = s4;
        } else {
            mimeType = MimeTypeMap.getFileExtensionFromUrl(s1);
        }

        String filename = guessFileName(s1,s3,s4);
        onDownloadAvailable(s1, s2, s3, s4, l, filename);
    }

    public abstract void onDownloadAvailable(String url, String userAgent, String contentDisposition,
                                             String mimeType, long size, String filename);

    public static final String guessFileName(String url, String disposition, String mimeType) {
        String filename = null;
        // try to get from content disposition
        if (disposition != null) {
            filename=getFilenameFromDispo(disposition);
        }

        // If all the other http-related approaches failed, use the plain uri
        if (filename == null||TextUtils.isEmpty(filename)) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                // If there is a query string strip it, same as desktop browsers
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                    }
                }
            }
        }
        // Finally, if couldn't get filename from URI, get a generic filename
        if (filename == null) {
            filename = "bilinmeyenadoynfrm";
        }
        // Split filename between base and extension
        // Add an extension if filename does not have one
        int dotIndex = filename.indexOf('.'+1);
        if (dotIndex==0){
            String fn = filename;
            String extension = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                extension = MimeType.get(mimeType);
            }
            filename=fn+"."+extension;
        }

        return filename;
    }

    public static String getFilenameFromDispo(String dispo){
        Matcher matcher = PATTERN1.matcher(dispo);
        if (matcher.find()){
            return matcher.group(2);
        }else {
            matcher = PATTERN2.matcher(dispo);
            if (matcher.find()){
                return matcher.group(2);
            }else {
                matcher = PATTERN3.matcher(dispo);
                if (matcher.find()){
                    return matcher.group(2);
                }
            }
        }
        return null;
    }
}
