package com.messe.seoro.ui;

import static com.messe.seoro.MyApplication.IR_CD_Value;
import static com.messe.seoro.kit.TelKit.PATH_HOME;
import static com.messe.seoro.kit.TelKit.URL_BASE_PRD;
import static com.messe.seoro.ui.MainWebViewFragment.mWebViewEx;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;

import com.google.firebase.messaging.FirebaseMessaging;
import com.messe.seoro.R;
import com.messe.seoro.data.Extra;
import com.messe.seoro.kit.BackPressCloseHandler;
import com.messe.seoro.kit.Kit;
import com.messe.seoro.kit.PrefKit;
import com.messe.seoro.kit.TelKit;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MainActivity extends BaseLoginActivity implements TelKit.OnResultListener, View.OnClickListener {
    private static String TAG = MainActivity.class.getSimpleName();

    public static BackPressCloseHandler backPressCloseHandler;

    private MainWebViewFragment mMainWebViewFragment = null;

    public static final int REQUEST_CODE_PAYMENT = 3000;
    public static final int REQUEST_CODE_NAVER_LOGIN = 3001;
    public static final int REQUEST_CODE_INPUT_FILE = 3002;
    public static final int REQUEST_CODE_AGREE_PUSH_NOTIFICATION = 3003;
    public static final int REQUEST_CODE_CONTENT_NUMBER = 3004;

    public static RelativeLayout mLayoutRoot = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layoutRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mLayoutRoot = findViewById(R.id.layoutRoot);

        backPressCloseHandler = new BackPressCloseHandler(this, mLayoutRoot);

        FirebaseApp.initializeApp(this);

        // 푸시알림 동의 화면 띄울지 결정할 실행 횟수
        PrefKit.increaseExecCountForPushAgree(this);

        mMainWebViewFragment = new MainWebViewFragment();
        Bundle bundle = new Bundle();
        Intent intent = getIntent(); /*데이터 수신*/
        if (intent.getExtras() != null) {
            String url = intent.getExtras().getString("url");
            bundle.putString("URL", url);
        } else {
            bundle.putString("URL", URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
        }
        mMainWebViewFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.layoutFragmentContainer, mMainWebViewFragment).commit();

        //# Appsflyer Start
        AppsFlyerLib.getInstance().sendPushNotificationData(this);
        //# Appsflyer End

        //getHashKey();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // 앱이 켜져있고 Pending Intent 를 통해 들어왔을 때 여기로 들어옴...
        checkAndOpenLink(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult::requestCode = " + requestCode);
        Log.e(TAG, "onActivityResult::resultCode = " + resultCode);
        Log.e(TAG, "onActivityResult::data = " + data);

        switch (requestCode) {
            case REQUEST_CODE_CONTENT_NUMBER:
                if (resultCode == RESULT_OK) {
                    Cursor cursor = null;
                    if (data != null) {
                        cursor = getContentResolver().query(data.getData(),
                                new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                        ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                    }

                    if (cursor != null) {
                        cursor.moveToFirst();
                        String script = String.format("javascript:selectContact('%s', '%s');", cursor.getString(1), cursor.getString(0));
                        mMainWebViewFragment.getInstance().loadUrl(script);
                        cursor.close();
                    }
                }
                break;
            case REQUEST_CODE_PAYMENT:
                if (resultCode == RESULT_OK) {
                    String url = data.getStringExtra(Extra.KEY_URL);
                    Log.e(TAG, "REQUEST_CODE_PAYMENT : " + url);
                    if (Kit.isNotNullNotEmpty(url)) {
                        loadUrl(url);
                    }
                    boolean isReload = data.getBooleanExtra(Extra.KEY_PARENT_RELOAD, false);
                    if (isReload) {
                        WebViewEx webViewEx = mMainWebViewFragment.getInstance().mWebViewEx;
                        if (webViewEx != null) {
                            webViewEx.reload();
                        }
                    }
                }
                break;
            case REQUEST_CODE_NAVER_LOGIN:
                if (resultCode == RESULT_OK) {
                    String loginType = data.getStringExtra(Extra.KEY_LOGIN_TYPE);
                    String userID = data.getStringExtra(Extra.KEY_USER_ID);
                    String userName = data.getStringExtra(Extra.KEY_USER_NAME);
                    String accessToken = data.getStringExtra(Extra.KEY_ACCESS_TOKEN);
                    String userEmail = data.getStringExtra(Extra.KEY_USER_EMAIL);
                    onLoginSuccess(loginType, userID, userName, accessToken, userEmail);
                } else {
                    String loginType = "naver";
                    if (data != null) {
                        loginType = data.getStringExtra(Extra.KEY_LOGIN_TYPE);
                    }
                    onLoginFailed(loginType);
                }
                break;
            case REQUEST_CODE_INPUT_FILE:
                // WebViewEx webViewEx = getCurrentWebViewEx();
                WebViewEx webViewEx = mMainWebViewFragment.getInstance().mWebViewEx;
                if (webViewEx.onActivityResultForInputFile(requestCode, resultCode, data) == false) {
                    super.onActivityResult(requestCode, resultCode, data);
                    return;
                }
                break;
            case REQUEST_CODE_AGREE_PUSH_NOTIFICATION:
                if (resultCode == RESULT_OK) {
                    boolean allow = data.getBooleanExtra(Extra.KEY_ALLOW_PUSH_NOTIFICATION, false);
//                    Kit.log(Kit.LogType.TEST, "onActivityResult::REQUEST_CODE_AGREE_PUSH_NOTIFICATION::allow = " + allow);
                    if (allow) {
                        mMainWebViewFragment.getInstance().loadUrl("javascript:push_setting('Y');");
                    }
                }
                break;
            default:
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onLoginSuccess(String loginType, String userID, String userName, String accessToken, String userEmail) {
        Log.e(TAG, "onLoginSuccess");
        Log.e(TAG, "onLoginSuccess::loginType = " + loginType);
        Log.e(TAG, "onLoginSuccess::userID = " + userID);
        Log.e(TAG, "onLoginSuccess::userName = " + userName);
        Log.e(TAG, "onLoginSuccess::accessToken = " + accessToken);
        Log.e(TAG, "onLoginSuccess::mConnect = " + mConnect);
        Log.e(TAG, "onLoginSuccess::userEmail = " + userEmail);

        //WebViewEx webViewEx = getCurrentWebViewEx();
        WebViewEx webViewEx = mMainWebViewFragment.getInstance().mWebViewEx;
        if (webViewEx == null) {
            return;
        } else {
            String fcmToken = PrefKit.getPushToken(mContext);
            if (loginType == null) {
                loginType = "";
            }
            if (userID == null) {
                userID = "";
            }
            if (userName == null) {
                userName = "";
            }
            if (accessToken == null) {
                accessToken = "";
            }
            if (fcmToken == null) {
                fcmToken = "";
            }

            String url;
            if (mConnect) {
                url = String.format("javascript:sns_connected('%s', '%s', '%s');", loginType, userID, accessToken);

                if (loginType.equals(PrefKit.MEMBER_TYPE_KAKAO)) {
                    kakaoLogout();
                } else if (loginType.equals(PrefKit.MEMBER_TYPE_NAVER)) {
                    naverLogout(this);
                }
            } else {
                final String sns_mode = loginType;
                final String sns_id = userID;
                String mem_name = userName;
                if (mem_name == null)
                    mem_name = "";
                String access_token = accessToken;
                String device_id = "";

                final String device_flag = "1";
                try {
                    if (Kit.isNotNullNotEmpty(mem_name)) {
                        mem_name = URLEncoder.encode(mem_name, "utf-8");
                    }
                    if (Kit.isNotNullNotEmpty(access_token)) {
                        access_token = URLEncoder.encode(access_token, "utf-8");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

//        String postData = String.format("login_type=%s&sns_mode=%s&sns_id=%s&mem_name=%s&access_token=%s&device_id=%s&device_flag=%s&token_id=%s&",
//                login_type, sns_mode, sns_id, mem_name, access_token, device_id, device_flag, token_id);
//        Log.e(TAG, "onLoginSuccess::TelKit.URL_REQUEST_CHECK_LOGIN = " + TelKit.URL_REQUEST_CHECK_LOGIN);
//        Log.e(TAG, "onLoginSuccess::postData = " + postData);
//        mWebViewEx.postUrl(TelKit.URL_REQUEST_CHECK_LOGIN, postData.getBytes());
                String adv_id = "";
                adv_id = PrefKit.getUserAdId(this);
                url = String.format("javascript:sns_login('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                        sns_mode, sns_id, access_token, device_id, device_flag, fcmToken, adv_id, userEmail);
            }
//        Kit.log(Kit.LogType.TEST, "onLoginSuccess::url = " + url);
            webViewEx.loadUrl(url);
        }
    }

    @Override
    protected void onLoginFailed(String loginType) {
        Log.e(TAG, "onLoginFailed::loginType = " + loginType);
        if (loginType == null) {
            loginType = "";
        }

        String msg = String.format("%s 계정 연결에 실패했습니다. 다시 시도 해주세요.", getLoginName(loginType));
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public void loadUrl(final String url) {
        Log.e(TAG, "MainActivity::loadUrl::url = " + url);

        if (mWebViewEx != null) {
            mWebViewEx.post(new Runnable() {
                @Override
                public void run() {
                    if (Kit.isNotNullNotEmpty(url)) {
                        mMainWebViewFragment.loadUrl(url);
                    } else {
                        mMainWebViewFragment.loadUrl(URL_BASE_PRD + PATH_HOME);
                    }
                }
            });
        }
    }

    public void checkAndOpenLink(Intent intent) {
        if (intent.getExtras() != null) {
            String link = intent.getStringExtra(Extra.KEY_LINK);
            String url = intent.getStringExtra(Extra.KEY_URL);
//        Log.e(TAG, "MainActivity::checkAndOpenLink::link = " + link);
            if (Kit.isNotNullNotEmpty(link)) {
                Uri uri = Uri.parse(link);
                String scheme = uri.getScheme();
                String host = "";
                String query = "";
                if (uri.getHost() != null) {
                    host = uri.getHost().toLowerCase();
                }
                if (uri.getQuery() != null) {
                    query = uri.getQuery();
                }
//            Kit.log(Kit.LogType.TEST, "MainActivity::checkAndOpenLink::scheme = " + scheme);        // https
//            Kit.log(Kit.LogType.TEST, "MainActivity::checkAndOpenLink::host = " + host);            // dev.cobemall.com
//            Kit.log(Kit.LogType.TEST, "MainActivity::checkAndOpenLink::query = " + query);          // null
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                    if (WebViewEx.isAllowedHost(host)) {
                        loadUrl(link);
                    } else {
                        Kit.execBrowserEx(mContext, link);
                    }
                } else if (Kit.isNotNullNotEmpty(query) && query.contains("ir_cd")) {
                    Log.e("MainActivity", "link : " + link);
                    loadUrl(link);
                } else {
                    loadUrl(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                }
            } else if (Kit.isNotNullNotEmpty(url)) {
                loadUrl(url);
            } else {
                loadUrl(URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
            }
        }
    }

    private void getHashKey() {
        try {                                                        // 패키지이름을 입력해줍니다.
            PackageInfo info = getPackageManager().getPackageInfo("com.messe.seoro", PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e(TAG, "key_hash=" + Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onResult(TelKit.Result result) {

    }
}