package com.messe.seoro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.messe.seoro.R;
import com.messe.seoro.data.Extra;


public class NaverLoginActivity extends BaseLoginActivity {
    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_naver_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        naverLogin();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult");
        Log.e(TAG, "onActivityResult::requestCode = " + requestCode);
        Log.e(TAG, "onActivityResult::resultCode = " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onLoginSuccess(String loginType, String userID, String userName, String accessToken, String userEmail) {
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess");
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess::loginType = " + loginType);
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess::userID = " + userID);
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess::userName = " + userName);
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess::accessToken = " + accessToken);
        Log.e(TAG, "NaverLoginActivity::onLoginSuccess::userEmail = " + userEmail);

        Intent intent = new Intent();
        intent.putExtra(Extra.KEY_LOGIN_TYPE, loginType);
        intent.putExtra(Extra.KEY_USER_ID, userID);
        intent.putExtra(Extra.KEY_USER_NAME, userName);
        intent.putExtra(Extra.KEY_ACCESS_TOKEN, accessToken);
        intent.putExtra(Extra.KEY_USER_EMAIL, userEmail);

        setResult(RESULT_OK, intent);
        finish();

        overridePendingTransition(0, android.R.anim.fade_out);
    }

    @Override
    protected void onLoginFailed(String loginType) {
        Log.e(TAG, "NaverLoginActivity::onLoginFailed");
        Log.e(TAG, "NaverLoginActivity::onLoginFailed::loginType = " + loginType);

        Intent intent = new Intent();
        intent.putExtra(Extra.KEY_LOGIN_TYPE, loginType);

        setResult(RESULT_CANCELED, intent);
        finish();

        overridePendingTransition(0, android.R.anim.fade_out);
    }
}