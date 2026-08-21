package com.messe.seoro.ui;

import static com.messe.seoro.MyApplication.IR_CD_Value;
import static com.messe.seoro.MyApplication.URL_Value;
import static com.messe.seoro.MyApplication.getAppContext;
import static com.messe.seoro.kit.Kit.isNetworkConnected;
import static com.messe.seoro.kit.TelKit.DOMAIN_LIST;
import static com.messe.seoro.kit.TelKit.DOWN_DOMAIN_LIST;
import static com.messe.seoro.kit.TelKit.PATH_HOME;
import static com.messe.seoro.kit.TelKit.URL_BASE_PRD;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import com.messe.seoro.R;
import com.messe.seoro.data.Extra;
import com.messe.seoro.kit.Kit;
import com.messe.seoro.kit.PrefKit;
import com.messe.seoro.kit.TelKit;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class SplashActivity extends BaseActivity implements TelKit.OnResultListener {
    private String TAG = getClass().getSimpleName();
    private static int SPLASH_TIME_OUT = 1000;
    private AlertDialog dialog = null;
    private AlertDialog.Builder dialogBuilder;
    private AlertDialog alertDialog;

    private ImageView imageA;
    private ImageView imageB;
    private ImageView imageA_logo;
    private ImageView imageB_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int themeColor = ContextCompat.getColor(this, R.color.white);
        setStatusColor(themeColor, true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        imageA = findViewById(R.id.imageA);
        imageB = findViewById(R.id.imageB);
        imageA_logo = findViewById(R.id.imageA_logo);
        imageB_logo = findViewById(R.id.imageB_logo);


        // 처음에는 A가 보이고 B는 안 보임
        imageA.setAlpha(1f);
        imageB.setAlpha(0f);
        imageA_logo.setAlpha(1f);
        imageB_logo.setAlpha(0f);

        // 1초 후 A → B 크로스페이드 시작
        new Handler().postDelayed(() -> {
            // A는 사라지고
            //imageA.animate().alpha(0f).setDuration(500).start();
            imageA.animate().alpha(0f).setDuration(500).start();
            imageA_logo.animate().alpha(0f).setDuration(500).start();

            // B는 동시에 나타남
            //imageB.animate().alpha(1f).setDuration(500).start();
            imageB.animate().alpha(1f).setDuration(500).start();
            imageB_logo.animate().alpha(1f).setDuration(500).start();

        }, 500);
//        ImageView logo = findViewById(R.id.logo_img);
//
//        ClipDrawable clip = new ClipDrawable(
//                logo.getDrawable(),
//                Gravity.LEFT,
//                ClipDrawable.HORIZONTAL
//        );
//
//        logo.setImageDrawable(clip);
//        clip.setLevel(0);
//
//        ObjectAnimator animator =
//                ObjectAnimator.ofInt(clip, "level", 0, 10000);
//        animator.setDuration(600);
//        animator.start();


        Kit.getOrCreateUUID(this);

        if (isNetworkConnected(SplashActivity.this)) {
            new TelKit(SplashActivity.this, SplashActivity.this).request(TelKit.PATH_DOMAIN_LIST, "");
            if (PrefKit.getFirstPermission(SplashActivity.this)) {
                showPermissionDialog(R.layout.permission_dialog_layout);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PostNotifications();
                } else {
                    AppVersionChecker();
                }
            }
        } else {
            if (!SplashActivity.this.isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                builder.show();
            }
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
                System.exit(0);
            }
        });

        //# Appsflyer Start
        AppsFlyerLib.getInstance().sendPushNotificationData(this);
        //# Appsflyer End

    }

    private void goMain() {
        if (isNetworkConnected(SplashActivity.this)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    String link = getIntent().getStringExtra(Extra.KEY_LINK);
                    Log.e(TAG, "SplashActivity::next::link = " + link);
                    if (Kit.isNotNullNotEmpty(link)) {
                        Log.e(TAG, "SplashActivity::111111111");
                        // 푸시알림 터치해서 들어온 경우...
                        intent.putExtra(Extra.KEY_LINK, link);
                    } else {
                        Log.e(TAG, "SplashActivity::2222222222");
                        // 카카오링크 통해서 들어온 경우...
                        Uri uri = getIntent().getData();
                        if (uri != null) {
                            link = uri.getQueryParameter("link");
                            String ir_cd = uri.getQueryParameter("ir_cd");
                            String url = uri.getQueryParameter("url");
                            if (Kit.isNotNullNotEmpty(link)) {   // 카톡 공유하기 글을 이용하여 진입시...
                                intent.putExtra(Extra.KEY_LINK, link);
                            } else if (uri.getScheme().equals("seoroappsflyer")) {
                                if (Kit.isNotNullNotEmpty(ir_cd)) {
                                    IR_CD_Value = ir_cd;
                                }
                                if (Kit.isNotNullNotEmpty(URL_Value)) {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                                } else {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                                }
                            } else if (Kit.isNotNullNotEmpty(ir_cd)) {   // 브라우저에서 앱으로보기 버튼 눌러 진입시...무조건 홈화면으로 이동
                                Log.e("SplashActivity", "isNotNullNotEmpty ir_cd : " + ir_cd);
                                IR_CD_Value = ir_cd;
                                if (Kit.isNotNullNotEmpty(url)) {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + url + "/?ir_cd=" + IR_CD_Value);
                                } else {
                                    intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                                }
                            }
                        } else if (Kit.isNotNullNotEmpty(IR_CD_Value)) {  //uri가 Null 이고 Firebase DynamicLink 로 들어오는 경우 Start
                            if (Kit.isNotNullNotEmpty(URL_Value)) {
                                intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                            } else {
                                intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + PATH_HOME + "?ir_cd=" + IR_CD_Value);
                            }
                        } else if (Kit.isNotNullNotEmpty(URL_Value)) {
                            intent.putExtra(Extra.KEY_LINK, URL_BASE_PRD + URL_Value + "/?ir_cd=" + IR_CD_Value);
                        }
                    }
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        } else {
            if (!SplashActivity.this.isFinishing()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
                builder.setMessage("네트워크 연결상태를 확인해 주세요.");
                builder.setCancelable(false);
                builder.setPositiveButton("종료",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        });
                builder.show();
            }
        }
    }

    public void AppVersionChecker() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // 버전 확인
                AppUpdateManager mAppUpdateManager = AppUpdateManagerFactory.create(getApplicationContext());
                // 업데이트 사용 가능 상태인지 체크
                Task<AppUpdateInfo> appUpdateInfoTask = mAppUpdateManager.getAppUpdateInfo();
                // 사용가능 체크 리스너를 달아준다
                appUpdateInfoTask.addOnSuccessListener(AppUpdateInfo -> {

                    if (AppUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                            && // 유연한 업데이트 사용 시 (AppUpdateType.FLEXIBLE) 사용
                            AppUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                        Log.e("SplashActivity", "업데이트 필요 : " + AppUpdateInfo.availableVersionCode());
                        showAppUpdateDialog();
                    } else {
                        Log.e("SplashActivity", "업데이트 불필요 : " + AppUpdateInfo.availableVersionCode());
                        goMain();
                    }
                });
                appUpdateInfoTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        goMain();
                    }
                });
            }
        }).start();
    }

    private void showAppUpdateDialog() {
        if (!SplashActivity.this.isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
            builder.setTitle("업데이트");
            builder.setCancelable(false);
            builder.setMessage("최신 버전의 앱이 등록되었습니다\n업데이트 하시겠습니까?");

            String positiveText = "업데이트";
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
                            marketLaunch.setData(Uri.parse("market://details?id=" + getPackageName()));
                            startActivity(marketLaunch);
                            dialog.dismiss();
                            finishAffinity();
                        }
                    });

            String negativeText = "종료";
            builder.setNegativeButton(negativeText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finishAffinity();
                        }
                    });
            dialog = builder.create();
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }

        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        super.onDestroy();
    }

    // queryParameter 추출 보조 메서드
    private String getQueryParam(Uri uri, String key) {
        String value = uri.getQueryParameter(key);
        return Kit.isNotNullNotEmpty(value) ? value : "";
    }

    private void showPermissionDialog(int layout) {
        dialogBuilder = new AlertDialog.Builder(SplashActivity.this);
        View layoutView = getLayoutInflater().inflate(layout, null);
        Button btnCancel = layoutView.findViewById(R.id.ok_btn);

        dialogBuilder.setView(layoutView);
        dialogBuilder.setCancelable(false);
        alertDialog = dialogBuilder.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.show();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PrefKit.setFirstPermission(SplashActivity.this, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PostNotifications();
                } else {
                    AppVersionChecker();
                }
                alertDialog.dismiss();
            }
        });
    }

    @Override
    public void onResult(TelKit.Result result) {
        if (result.mRequestUrl.equals(TelKit.PATH_DOMAIN_LIST)) {
            if (!result.mResponse.isEmpty()) {
                try {
                    JSONArray jsonArray = new JSONArray(result.mResponse);
                    int length = jsonArray.length();

                    if (length > 0) {
                        DOWN_DOMAIN_LIST = new String[length];
                        for (int i = 0; i < length; i++) {
                            DOWN_DOMAIN_LIST[i] = jsonArray.getString(i);
                        }
                    } else {
                        DOWN_DOMAIN_LIST = DOMAIN_LIST;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    DOWN_DOMAIN_LIST = DOMAIN_LIST;
                }
            } else {
                DOWN_DOMAIN_LIST = DOMAIN_LIST;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private void PostNotifications() {
        TedPermission.create()
                .setDeniedTitle("알림 권한")
                .setDeniedMessage("설정에서 앱 알림 권한을 허용해 주세요.")
                .setRationaleConfirmText("확인")
                .setDeniedCloseButtonText("취소")
                .setGotoSettingButtonText("설정")
                .setPermissionListener(new PermissionListener() {

                    @Override
                    public void onPermissionGranted() {
                        AppVersionChecker();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        Toast.makeText(getAppContext(), "앱 알림 권한을 허용하지 않으시더라도 앱을 이용하실 수 있으나, 알림을 받으실 수 없습니다.", Toast.LENGTH_SHORT).show();
                        AppVersionChecker();
                    }
                }).setPermissions(Manifest.permission.POST_NOTIFICATIONS)
                .check();
    }
}
