package com.messe.seoro.ui;

import static com.messe.seoro.MyApplication.SIDE_MENU_OPEN_CHECK;
import static com.messe.seoro.kit.TelKit.PATH_HOME;
import static com.messe.seoro.kit.TelKit.URL_BASE_PRD;
import static com.messe.seoro.ui.MainActivity.backPressCloseHandler;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import com.messe.seoro.R;
import com.messe.seoro.kit.Kit;
import com.messe.seoro.kit.PrefKit;
import com.messe.seoro.kit.TelKit;

import java.util.Objects;

public class MainWebViewFragment extends Fragment {
    private String TAG = getClass().getSimpleName();
    public static WebViewEx mWebViewEx = null;
    public CookieManager cookieManager;
    private ProgressBar progress_bar = null;
    private String sPage_URL = "";

    private long downloadId = -1;
    private final Handler downloadHandler = new Handler(Looper.getMainLooper());

    public static MainWebViewFragment getInstance() {
        MainWebViewFragment fragment = new MainWebViewFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 뒤로가기 콜백 등록
        requireActivity().getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Log.e(TAG, "addCallback getUrl : " + mWebViewEx.getUrl());

                        if (SIDE_MENU_OPEN_CHECK) {
                            mWebViewEx.loadUrl("javascript:fnSideAOSClose();");  //사이드메뉴 닫기
                            return;
                        }

                        if (mWebViewEx.canGoBack()) {
                            if (Objects.equals(mWebViewEx.getUrl(), URL_BASE_PRD + PATH_HOME)) {
                                backPressCloseHandler.onBackPressed();
                            } else {
                                mWebViewEx.goBack();
                            }
                        } else {
                            backPressCloseHandler.onBackPressed();
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (container == null)
            return null;

        View v = inflater.inflate(R.layout.fragment_main_webview, null);

        if (getArguments() != null) {
            sPage_URL = getArguments().getString("URL");
        }

        cookieManager = CookieManager.getInstance();
        mWebViewEx = v.findViewById(R.id.WebViewEx_main);
        progress_bar = v.findViewById(R.id.progress_bar);

        mWebViewEx.setProgressBar(progress_bar);
        Log.e(TAG, "sPage_URL : " + sPage_URL);

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            mWebViewEx.setContainer(((MainActivity) activity).mLayoutRoot);
        }
        mWebViewEx.setFragment(this);
        //mWebViewEx.setWebContentsDebuggingEnabled(true); //웹 디버깅

        loadUrl(sPage_URL);

        initWebView();
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Activity activity = getActivity();

        if (activity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.checkAndOpenLink(mainActivity.getIntent());
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<String> task) {
                String token_id = "";
                if (task.isSuccessful()) {
                    // Get new FCM registration token
                    token_id = task.getResult();
                    PrefKit.setPushToken(activity, token_id);
                    Log.e(TAG, "onSuccess::token_id = " + token_id);
                    String mem_idx = PrefKit.getMemberIdx(activity);
                    Log.e(TAG, "mem_idx : " + mem_idx);
                    if (Kit.isNotNullNotEmpty(mem_idx)) {
                        Log.e(TAG, "Refreshed token2: " + token_id);
                        TelKit.tokenRegistrationToServer(activity, mem_idx, token_id);
                    }

                } else {
                    Log.w("MainWebViewFragment", "Fetching FCM registration token failed", task.getException());
                }
            }
        });

        //# Appsflyer Start
        AppsFlyerLib.getInstance().sendPushNotificationData(activity);
        //# Appsflyer End

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        // TODO Auto-generated method stub
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        // TODO Auto-generated method stub
        super.onDetach();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }


    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        if (mWebViewEx != null) {
            mWebViewEx.onPause();
            mWebViewEx.pauseTimers();
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (mWebViewEx != null) {
            mWebViewEx.onResume();
            mWebViewEx.resumeTimers();
            if (mWebViewEx.getUrl() == null || mWebViewEx.getUrl().equals("")) {
                loadUrl(URL_BASE_PRD + PATH_HOME);
            }
        }
    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    public void loadUrl(String url) {
        if (mWebViewEx == null) {
            return;
        }
        mWebViewEx.post(new Runnable() {
            @Override
            public void run() {
                mWebViewEx.loadUrl(url);
            }
        });
    }

    private void initWebView() {

        mWebViewEx.getSettings().setJavaScriptEnabled(true);

        mWebViewEx.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {

            downloadFile(url, userAgent, mimeType);

        });
    }

    private String getFileNameFromUrl(String url) {

        Uri uri = Uri.parse(url);
        String name = uri.getQueryParameter("name");

        if (name != null && !name.isEmpty()) {
            return name;
        }

        return URLUtil.guessFileName(url, null, null);
    }

    private void downloadFile(String url, String userAgent, String mimeType) {

        String fileName = getFileNameFromUrl(url);

        DownloadManager.Request request =
                new DownloadManager.Request(Uri.parse(url));

        request.setTitle(fileName);
        request.setDescription("파일 다운로드 중...");
        request.setMimeType(mimeType);

        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                fileName
        );

        // WebView 쿠키 유지
        String cookies = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("Cookie", cookies);
        request.addRequestHeader("User-Agent", userAgent);

        DownloadManager dm =
                (DownloadManager) requireContext()
                        .getSystemService(Context.DOWNLOAD_SERVICE);

        downloadId = dm.enqueue(request);

        // 다운로드 상태 체크 시작
        startDownloadCheck(dm, fileName);
    }

    private void startDownloadCheck(DownloadManager dm, String fileName) {

        downloadHandler.postDelayed(() -> checkDownloadStatus(dm, fileName), 2000);

    }

    private void checkDownloadStatus(DownloadManager dm, String fileName) {

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        Cursor cursor = dm.query(query);

        if (cursor != null && cursor.moveToFirst()) {

            int status = cursor.getInt(
                    cursor.getColumnIndexOrThrow(
                            DownloadManager.COLUMN_STATUS));

            if (status == DownloadManager.STATUS_SUCCESSFUL) {

                Toast.makeText(getContext(),
                        "파일 다운로드 완료",
                        Toast.LENGTH_SHORT).show();

            } else if (status == DownloadManager.STATUS_FAILED) {

                Toast.makeText(getContext(),
                        "다운로드 실패",
                        Toast.LENGTH_SHORT).show();

            } else {

                // 아직 다운로드 중이면 다시 체크
                downloadHandler.postDelayed(() ->
                        checkDownloadStatus(dm, fileName), 2000);
            }
        }

        if (cursor != null) cursor.close();
    }
}