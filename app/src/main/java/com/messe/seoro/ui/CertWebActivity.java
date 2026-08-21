package com.messe.seoro.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.messe.seoro.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class CertWebActivity extends BaseActivity {
    private String TAG = getClass().getSimpleName();
    private TextView toolbar_title = null;
    private WebViewEx mWebViewEx = null;
    private String postData = "";
    private String URL = "";
    private String Data = "";
    public static Activity CertWebActivity;
    private ProgressBar progress_bar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cert_web);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar_title = (TextView) findViewById(R.id.toolbar_title);
            toolbar_title.setText("휴대폰 본인 인증");
            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            //actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        }
        CertWebActivity = CertWebActivity.this;
        mWebViewEx = findViewById(R.id.webViewEx);
        progress_bar = findViewById(R.id.progress_bar);
        mWebViewEx.setProgressBar(progress_bar);

        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            Data = intent.getExtras().getString("Data");

            try {
                JSONObject obj = new JSONObject(Data);
                if (obj != null) {
                    String getResult = obj.optString("open_hp_cert").replace("null", "");
                    URL = obj.optString("url").replace("null", "");
                    String req_info = obj.optString("req_info").replace("null", "");
                    String rtn_url = obj.optString("rtn_url").replace("null", "");
                    String cpid = obj.optString("cpid").replace("null", "");

                    if (getResult.equals("ok")) {
                        Log.e(TAG, "result : " + getResult);
                        postData = "req_info=" + URLEncoder.encode(req_info, "UTF-8")
                                + "&rtn_url=" + URLEncoder.encode(rtn_url, "UTF-8")
                                + "&cpid=" + URLEncoder.encode(cpid, "UTF-8");

                        mWebViewEx.postUrl(URL, postData.getBytes());
                    }
                }
            } catch (UnsupportedEncodingException | JSONException e) {
                e.printStackTrace();
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