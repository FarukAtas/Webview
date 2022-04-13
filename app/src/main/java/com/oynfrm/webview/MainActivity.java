package com.oynfrm.webview;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private WebView wb;
    private ProgressBar pB;
    private SwipeRefreshLayout swR;
    private WebDownloader webDownloader;
    private UploadSupport uploadSupport;
    private ValueCallback<Uri[]> mUMA;
    String url = "https://oynfrm.com"; // Tırnak işaretlerinin içerisindeki domain adını değiştirmeniz yeterlidir.

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetCheck();

        uploadSupport = new UploadSupport(MainActivity.this);

        wb = findViewById(R.id.web);
        pB = findViewById(R.id.pB);
        swR = findViewById(R.id.swR);

        WebSettings wbs = wb.getSettings();
        pB.setVisibility(View.VISIBLE);

        pB.setMax(100);
        pB.setProgress(1);

        wbs.setJavaScriptEnabled(true); // Javascript aktif eder.
        wbs.setUserAgentString(new WebView(this).getSettings().getUserAgentString()); // Default ayarları chrome gibi yapar.
        wbs.setDomStorageEnabled(true); //
        wbs.setUseWideViewPort(true); // Resimleri görünüme göre yeniden boyutlandırır.
        wbs.setLoadWithOverviewMode(true); // Ekran boyutuna yakınlaştırır.
        wbs.setSupportZoom(true); // Yakınlaştırmayı(Zoom) aktif eder.
        wbs.setBuiltInZoomControls(true);//Yakınlaşırma kontrolleri aktif
        wbs.setDisplayZoomControls(false);//Yakınlaştırma kontrollerini göster
        wbs.setAppCacheEnabled(true); //Önbellek aktif
        wbs.setAllowFileAccess(true);
        wbs.setAllowContentAccess(true);
        wbs.setJavaScriptCanOpenWindowsAutomatically(true);
        wbs.setLoadsImagesAutomatically(true);
        wbs.setDefaultTextEncodingName("utf-8");
        wbs.setSupportMultipleWindows(false); //Çoklu ekran desteği aktif
        wbs.setJavaScriptCanOpenWindowsAutomatically(true);//ek javascript ayarı - pencere izni
        wb.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        wb.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        wb.setWebViewClient(new tarayici());
        wb.loadUrl(url);

        webDownloader = new WebDownloader(MainActivity.this);
        webDownloader.setTo(wb);
        webDownloader.setDownloadListener(new WebDownloader.StateListener() {
            @Override
            public void onDownloadStart(String url, String mimeType, String filename) {
                Toast.makeText(MainActivity.this, "Dosya İndiriliyor...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDownloadFailed(String errorMessage) {
                Toast.makeText(MainActivity.this, "Dosya İndirme Başarısız...", Toast.LENGTH_LONG).show();
            }
        });

        wb.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {

                pB.setProgress(progress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
               uploadSupport.showFileChooser(filePathCallback);
               mUMA = filePathCallback;
                return true;
            }
        });

        swR.setOnRefreshListener(() -> {
            wb.reload();
            pB.setVisibility(View.VISIBLE);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WebDownloader.downloadCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                webDownloader.startDownload(webDownloader.sURL, webDownloader.sUA, webDownloader.sFileName, webDownloader.sMM);
            }
        }
        if (requestCode==UploadSupport.uploadCode) {
            if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                uploadSupport.showFileChooser(mUMA);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==UploadSupport.FCR) {
            if (uploadSupport.mUMA!=null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    uploadSupport.mUMA.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                }
            }
        }
    }

    @Override // Geri tuşuna basıldığı zaman uygulamadan çıkış yapmadan bir önceki sayfaya yönlenir.
    public void onBackPressed() {
        if (wb != null && wb.canGoBack())
        {
            wb.goBack();
        } else
        {
            uygulamaExit();
        }
    }

    private class tarayici extends WebViewClient {
        tarayici() {

        }

        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            wb.loadUrl(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pB.setVisibility(View.GONE);
            swR.setRefreshing(false);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("whatsapp")) {
                try {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } catch (android.content.ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "Uygulama yüklü değil", Toast.LENGTH_LONG).show();
                }
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    private void internetCheck(){
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting())// iki şart vermemizin sebebi bağlantı olsa
        //bile network bilgisi gelmez ise hataya düşmemek adına işimizi sağlama alıyoruz.
        {

        }
        else
        {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Uyarı!");
            builder.setMessage("Lütfen internet bağlantınızı kontrol ediniz!");
            builder.setCancelable(true);
            builder.setPositiveButton("Çıkış", (dialog, i) -> {
                finish(); //Uygulamayı sonlandırıyoruz
            });
            android.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    private void uygulamaExit() {

       AlertDialog.Builder aD = new AlertDialog.Builder(MainActivity.this);

       aD.setTitle("Uyarı");
       aD.setMessage("Uygulamadan çıkış yapmak istiyor musunuz ?");

       aD.setPositiveButton("Çıkış Yap", (dialogInterface, i) -> finish());

       aD.setNegativeButton("İptal et", (dialogInterface, i) -> {

       });
       aD.create().show();
    }
}