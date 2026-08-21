package com.messe.seoro.payment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.messe.seoro.MyApplication;
import com.messe.seoro.ui.WebViewEx;

public class ResultRcvActivity extends Activity {
    private String TAG = getClass().getSimpleName();
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "[ResultRcvActivity] called__onCreate");

        super.onCreate(savedInstanceState);

        // TODO Auto-generated method stub
        MyApplication myApp = (MyApplication) getApplication();
        Intent myIntent = getIntent();

        Log.e(TAG,
                "[ResultRcvActivity] launch_uri=[" + myIntent.getData().toString() + "]");

        if (myIntent.getData().getScheme().equals(WebViewEx.SCHEME_SEORO) == true) {
            myApp.b_type = true;
            myApp.m_uriResult = myIntent.getData();
        } else {
            myApp.m_uriResult = null;
        }

        finish();
    }
}