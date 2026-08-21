package com.messe.seoro;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.appsflyer.AppsFlyerConversionListener;
import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerProperties;
import com.appsflyer.attribution.AppsFlyerRequestListener;
import com.appsflyer.deeplink.DeepLink;
import com.appsflyer.deeplink.DeepLinkResult;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.messe.seoro.kit.Kit;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Pattern;

public class MyApplication extends Application {
    private static Context context;
    public static boolean AlertDialog_Check = false;

    public static FirebaseAnalytics mFirebaseAnalytics;
    public static String IR_CD_Value = "";
    public static String URL_Value = "";

    public Uri m_uriResult;
    public boolean b_type = false;

    public static final String m_strLogTag = "PaySample";

    public static boolean SIDE_MENU_OPEN_CHECK = false;

    //# Appsflyer Start
    private static final String AF_DEV_KEY = "T6pTVKur8h6JuTmhMHAeM7";
    public static final String LOG_TAG = "MyApplication";
    //# Appsflyer End

    @Override
    public void onCreate() {
        super.onCreate();

        MyApplication.context = getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Android 9 (Pie) 이상에서 Multi-Process WebView 지원
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                WebView.setDataDirectorySuffix(Application.getProcessName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //# Appsflyer Start
        AppsFlyerLib appsflyerlib = AppsFlyerLib.getInstance();
        appsflyerlib.setMinTimeBetweenSessions(0);
        appsflyerlib.setDebugLog(false);
        appsflyerlib.waitForCustomerUserId(false);

        AppsFlyerLib.getInstance().subscribeForDeepLink(deepLinkResult -> {

            DeepLinkResult.Error dlError = deepLinkResult.getError();
            if (dlError != null) {
                Log.e(LOG_TAG, "Error getting Deep Link data: " + dlError.toString());
                return;
            }

            DeepLink deepLink = deepLinkResult.getDeepLink();
            if (deepLink == null) {
                Log.e(LOG_TAG, "DeepLink data is null");
                return;
            }

            Log.e(LOG_TAG, "DeepLink data: " + deepLink.toString());

            try {
                URL_Value = getDecodedParam(deepLink, "url", URL_Value);
                URL_Value = getDecodedParam(deepLink, "deep_link_value", URL_Value);
                IR_CD_Value = getDecodedParam(deepLink, "ir_cd", IR_CD_Value);
                IR_CD_Value = getDecodedParam(deepLink, "deep_link_sub1", IR_CD_Value);
                Log.e(LOG_TAG, "AppsFlyerLib DeepLink URL_Value: " + URL_Value);
                Log.e(LOG_TAG, "AppsFlyerLib DeepLink IR_CD_Value: " + IR_CD_Value);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOG_TAG, "URL decoding error", e);
            }
        });

        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {
            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {
                for (String attrName : conversionData.keySet())
                    Log.d(LOG_TAG, "Conversion attribute: " + attrName + " = " + conversionData.get(attrName));
                //TODO - remove this
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d(LOG_TAG, "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> attributionData) {
                Log.d(LOG_TAG, "onAppOpenAttribution: This is fake call.");
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d(LOG_TAG, "error onAttributionFailure : " + errorMessage);
            }
        };

        appsflyerlib.init(AF_DEV_KEY, conversionListener, this);
        appsflyerlib.start(this, AF_DEV_KEY, myListener());

        Log.d("MyApplication", "AppsFlyerProperties.APP_USER_ID: " + AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID));
        if (Kit.isNotNullNotEmpty(AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID))) {
            appsflyerlib.setCustomerIdAndLogSession(AppsFlyerProperties.getInstance().getString(AppsFlyerProperties.APP_USER_ID), this);
        }
        //# Appsflyer End
    }

    public static Context getAppContext() {
        return MyApplication.context;
    }

    public static String getMarketVersionFast(String packageName) {
        String MarketVersion = null;

        try {
            Elements elements = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName)
                    .timeout(30000)
                    .referrer("http://www.google.com")
                    .get()
                    .select("span.htlgb");

            if (elements.size() != 0) {
                for (int i = 0; i < elements.size(); i++) {
                    String htlgb = elements.get(i).ownText();
                    //Log.d("MyApplication", "htlgb : " + htlgb);
                    if (!TextUtils.isEmpty(htlgb)) {
                        if (Pattern.matches("^[0-9]{1}.[0-9]{1}.[0-9]{1}$", htlgb)) {
                            MarketVersion = htlgb;
                            Log.d("MyApplication", "MarketVersion : " + MarketVersion);
                            return MarketVersion;
                        }
                    }
                }
            } else {
                Connection.Response response = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName)
                        .method(Connection.Method.GET)
                        .execute();
                Document document = response.parse();
                Elements elements_sub = document.select("script");

                for (int i = 0; i < elements_sub.size(); i++) {
                    String html = elements_sub.get(i).html();
                    String[] html_arry = html.split(",");

                    if (html_arry != null) {
                        int nCnt = html_arry.length;

                        for (int k = 0; k < nCnt; ++k) {
                            if (Pattern.matches("\"[0-9]{1}.[0-9]{1}.[0-9]{1}\"", html_arry[k])) {
                                MarketVersion = html_arry[k].replace("\"", "");
                                return MarketVersion;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    //# Appsflyer Start
    private AppsFlyerRequestListener myListener() {
        return new AppsFlyerRequestListener() {
            @Override
            public void onSuccess() {
                Log.d("MyApplication", "Event sent successfully");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("MyApplication", "Event failed to be sent:\n" +
                        "Error code: " + i + "\n"
                        + "Error description: " + s);
            }
        };
    }

    private String getDecodedParam(DeepLink deepLink, String key, String defaultValue) throws UnsupportedEncodingException {
        String value = deepLink.getStringValue(key);
        if (Kit.isNotNullNotEmpty(value)) {
            String decoded = URLDecoder.decode(value, "UTF-8");
            Log.d(LOG_TAG, "DeepLink param [" + key + "] = " + decoded);
            return decoded;
        }
        return defaultValue;
    }
    //# Appsflyer End
}