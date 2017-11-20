package com.maihehd.sdk.vast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.*;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.maihehd.sdk.vast.util.LogUtil;
import com.maihehd.sdk.vast.util.ResourceUtil;

/**
 * Created by Roger on 16/1/5.
 */
public class VASTWebView extends Activity {

    private final static String TAG = "VASTWebView";

    private RelativeLayout rootLayout;
    private TextView titleTextView;
    private Button closeButton;
    private Button backButton;
    private Button reloadButton;
    private WebView webView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().requestFeature(Window.FEATURE_PROGRESS);
        this.getWindow().setFeatureInt(Window.FEATURE_PROGRESS,
                Window.PROGRESS_VISIBILITY_ON);

        Context context = getBaseContext();
        final float scale = context.getResources().getDisplayMetrics().density;

        //setContentView(R.layout.webview);

//        rootLayout = (RelativeLayout)this.findViewById(R.id.rootLayout);
//        titleTextView = (TextView) this.findViewById(R.id.titleTextView);
//        closeButton = (Button) this.findViewById(R.id.closeButton);
//        backButton = (Button) this.findViewById(R.id.backButton);
//        reloadButton = (Button) this.findViewById(R.id.reloadButton);
//        webView = (WebView)this.findViewById(R.id.webview);

        int contentId = 10101010;
        ResourceUtil resouceTool = new ResourceUtil();
        Resources resources = getResources();

        rootLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams rootLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        setContentView(rootLayout, rootLayoutParams);

        RelativeLayout toolbar = new RelativeLayout(context);
        toolbar.setId(contentId++);
        RelativeLayout.LayoutParams toolbarLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rootLayout.addView(toolbar, toolbarLayoutParams);
        int colors[] = { 0xFFFFFFFF , 0xFFDDDDDD };
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);
        toolbar.setBackgroundDrawable(bg);

        titleTextView = new TextView(context);
        titleTextView.setId(contentId++);
        RelativeLayout.LayoutParams titleLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int)(40 * scale));
        titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        titleLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        toolbar.addView(titleTextView, titleLayoutParams);
        titleTextView.setTextColor(0x000000);
//        titleTextView.setGravity(Gravity.CENTER);

        int iconHeight = (int)(24 * scale);
        int margin = (int)(8 * scale);
        closeButton = new Button(context);
        closeButton.setId(contentId++);
        RelativeLayout.LayoutParams closeLayoutParams = new RelativeLayout.LayoutParams(iconHeight, iconHeight);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        closeLayoutParams.setMargins(margin, margin, 0, 0);
        toolbar.addView(closeButton, closeLayoutParams);
        try {
            Drawable drawable = resouceTool.getDrawable(resources, "icon_close");
            closeButton.setBackgroundDrawable(drawable);
        }
        catch (Exception e){
            LogUtil.d(TAG, e.getMessage());
        }

        backButton = new Button(context);
        backButton.setId(contentId++);
        RelativeLayout.LayoutParams backLayoutParams = new RelativeLayout.LayoutParams(iconHeight, iconHeight);
        backLayoutParams.addRule(RelativeLayout.RIGHT_OF, closeButton.getId());
        backLayoutParams.setMargins(margin, margin, 0, 0);
        toolbar.addView(backButton, backLayoutParams);
        try {
            Drawable drawable = resouceTool.getDrawable(resources, "icon_previous");
            backButton.setBackgroundDrawable(drawable);
        }
        catch (Exception e){
            LogUtil.d(TAG, e.getMessage());
        }

        reloadButton =  new Button(context);
        reloadButton.setId(contentId++);
        RelativeLayout.LayoutParams reloadLayoutParams = new RelativeLayout.LayoutParams(iconHeight, iconHeight);
        reloadLayoutParams.setMargins(0, margin, margin, 0);
        reloadLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        reloadLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        toolbar.addView(reloadButton, reloadLayoutParams);
        try {
            Drawable drawable = resouceTool.getDrawable(resources, "icon_reload");
            reloadButton.setBackgroundDrawable(drawable);
        }
        catch (Exception e){
            LogUtil.d(TAG, e.getMessage());
        }

        webView = new WebView(context);
        webView.setId(contentId++);
        RelativeLayout.LayoutParams webLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        webLayoutParams.addRule(RelativeLayout.BELOW, toolbar.getId());
        webLayoutParams.setMargins(0, 0, 0, 0);
        rootLayout.addView(webView, webLayoutParams);

        titleTextView.setText("广告");
        closeButton.setOnClickListener(new ClickEvent());
        backButton.setOnClickListener(new ClickEvent());
        backButton.getBackground().setAlpha(128);
        reloadButton.setOnClickListener(new ClickEvent());

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");

        Toast loadingmess = Toast.makeText(this,
                "正在加载", Toast.LENGTH_SHORT);
        loadingmess.show();

        final Activity webAcivity = this;
        webView.setWebViewClient(new WebViewClient() {
            // Load opened URL in the application instead of standard browser
            // application
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.startsWith("http")) {
                    view.loadUrl(url);
                    backButton.getBackground().setAlpha(255);
                    return true;
                }
                else{
                    return false;
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            // Set progress bar during loading
            public void onProgressChanged(WebView view, int progress) {
                //LogUtil.d(TAG, "progress = " + progress);
                webAcivity.setTitle("正在加载...");
                webAcivity.setProgress(progress * 100);

                if(progress == 100) {
                    webAcivity.setTitle(webView.getTitle());
                }
            }

            @Override
            public void onConsoleMessage(String message, int lineNumber, String sourceID) {
                return;
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage cm) {
                // LogUtil.d("TAG", cm.message() + " at " + cm.sourceId() + ":" + cm.lineNumber());
                return false;
            }
        });

        webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK ) {
                        LogUtil.d(TAG, "webview back");
                        if (webView.canGoBack()) {
                            webView.goBack();
                        }
                        else{
                            finish();
                        }

                    }
                }
                return false;
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype,
                                        long contentLength) {
                Uri uri = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        webView.requestFocus();
        // Enable some feature like Javascript and pinch zoom
        WebSettings websettings = webView.getSettings();
        websettings.setJavaScriptEnabled(true);     // Warning! You can have XSS vulnerabilities!
        websettings.setBuiltInZoomControls(true);

        webView.loadUrl(url);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        rootLayout.removeView(webView);
        webView.removeAllViews();
        webView.destroy();
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(view.getId() == closeButton.getId()) {
                finish();
            }
            else if(view.getId() == backButton.getId()){
                if (webView.canGoBack()) {
                    webView.goBack();
                }
                else {
                    backButton.getBackground().setAlpha(128);
                }
            }
            else if (view.getId() == reloadButton.getId()){
                webView.reload();
            }
        }
    }
}