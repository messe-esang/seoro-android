package com.messe.seoro.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.messe.seoro.R;
import com.messe.seoro.data.Extra;
import com.messe.seoro.kit.Kit;


public class WebActivity extends BaseActivity {
    private Toolbar mToolbar = null;
    private WebViewEx mWebViewEx = null;
    private ProgressBar progress_bar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mToolbar = findViewById(R.id.toolbar);
        mWebViewEx = findViewById(R.id.webViewEx);
        progress_bar = findViewById(R.id.progress_bar);
        mWebViewEx.setProgressBar(progress_bar);

        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back);
        }

        mWebViewEx.setContainer((RelativeLayout) findViewById(R.id.layoutRoot));
        mWebViewEx.setBackButton((ImageButton) findViewById(R.id.btnBack));
        mWebViewEx.setProgressBar(progress_bar);

        Intent intent = getIntent();
        String url = intent.getStringExtra(Extra.KEY_URL);
        String method = intent.getStringExtra(Extra.KEY_METHOD);

        if (Kit.isNotNullNotEmpty(url) && Kit.isNotNullNotEmpty(method)) {

            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            String path = uri.getPath();
            String query = uri.getQuery();
            if (method.equalsIgnoreCase("post")) {
                String postUrl = String.format("%s://%s%s", scheme, host, path);
                mWebViewEx.postUrl(postUrl, query.getBytes());
            } else {
                mWebViewEx.loadUrl(url);
            }
        }

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (!isFinishing() && !isDestroyed()) {
                            if (mWebViewEx.canGoBack()) {
                                mWebViewEx.goBack();
                            } else {
                                finish();
                            }
                        }
                    }
                }
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWebViewEx != null) {
            mWebViewEx.onPause();
            mWebViewEx.pauseTimers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mWebViewEx != null) {
            mWebViewEx.onResume();
            mWebViewEx.resumeTimers();
        }
    }
}