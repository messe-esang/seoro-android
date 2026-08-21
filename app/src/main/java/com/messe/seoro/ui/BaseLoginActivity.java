package com.messe.seoro.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.messe.seoro.kit.Kit;
import com.messe.seoro.kit.PrefKit;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;

public abstract class BaseLoginActivity extends BaseActivity {
    private static String TAG = "BaseLoginActivity";

    // Naver Login
    public static final String OAUTH_CLIENT_ID = "2mauv7kq8nFXVlWHplyZ";
    public static final String OAUTH_CLIENT_SECRET = "y8JAnz9xHq";
    public static final String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";
    protected static NidOAuthLogin mNidOAuthLogin = null;
    protected static NaverIdLoginSDK mNaverIdLoginSDK = null;
    protected Context mContext;

    protected boolean mConnect = false;     // 환경 설정 페이지의 로그인 연동 여부 (true: SNS로그인 연동, false: SNS로그인)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.e("BaseLoginActivity", "onCreate");
        mContext = this;

        // naver login
        mNaverIdLoginSDK = NaverIdLoginSDK.INSTANCE;
        mNaverIdLoginSDK.initialize(this
                , OAUTH_CLIENT_ID
                , OAUTH_CLIENT_SECRET
                , OAUTH_CLIENT_NAME);

        mNidOAuthLogin = new NidOAuthLogin();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected abstract void onLoginSuccess(String loginType, String userID, String userName, String accessToken, String userEmail);

    protected abstract void onLoginFailed(String loginType);

    private void KakaoAppLogin() {
        UserApiClient.getInstance().loginWithKakaoTalk(this, (oAuthToken, error) -> {
            if (error != null) {
                onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            } else if (oAuthToken != null) {
                KakaoLoginUserInfo(oAuthToken);
            }
            return null;
        });
    }

    private void KakaoAccountLogin() {
        UserApiClient.getInstance().loginWithKakaoAccount(this, (oAuthToken, error) -> {
            if (error != null) {
                onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
            } else if (oAuthToken != null) {
                KakaoLoginUserInfo(oAuthToken);
            }
            return null;
        });
    }

    private void KakaoLoginUserInfo(OAuthToken oAuthToken) {
        if (oAuthToken != null) {
            Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
            String kakaoAccessToken = oAuthToken.getAccessToken();
            String refreshToken = oAuthToken.getRefreshToken();
            Log.e(TAG, "kakaoLogin::kakaoaccessToken = " + kakaoAccessToken);
            Log.e(TAG, "kakaoLogin::refreshToken = " + refreshToken);

            UserApiClient.getInstance().me((user, meError) -> {
                if (meError != null) {
                    Kit.showAlertDialog(BaseLoginActivity.this, "카카오톡 로그인", meError.getMessage(), "확인");
                    onLoginFailed(PrefKit.MEMBER_TYPE_KAKAO);
                } else {
                    System.out.println("로그인 완료");
                    long userIDValue = user.getId();
                    Log.e(TAG, "kakaoLogin::userIDValue = " + userIDValue);
                    Log.e(TAG, "회원번호: " + user.getId());
                    Log.e(TAG, "이메일: " + user.getKakaoAccount().getEmail());
                    Account user1 = user.getKakaoAccount();
                    Log.e(TAG, "사용자 계정: " + user1);
                    onLoginSuccess(PrefKit.MEMBER_TYPE_KAKAO, String.valueOf(user.getId()), "", kakaoAccessToken, user.getKakaoAccount().getEmail());
                }
                return null;
            });
        }
    }

    protected void kakaoLogin() {

        if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(this)) {
            KakaoAppLogin();
        } else {
            KakaoAccountLogin();
        }
    }

    protected static void kakaoLogout() {
        Log.e(TAG, "kakaoLogout");
        UserApiClient.getInstance().logout(new Function1<Throwable, Unit>() {
            @Override
            public Unit invoke(Throwable throwable) {
                Log.e("BaseLoginActivity", "kakaoLogout invoke return null");
                return null;
            }
        });
    }

    protected void naverLogin() {

        mNaverIdLoginSDK.authenticate(mContext, new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
                String naverAccessToken = mNaverIdLoginSDK.getAccessToken();
                String refreshToken = mNaverIdLoginSDK.getRefreshToken();
                long expiresAt = mNaverIdLoginSDK.getExpiresAt();
                String tokenType = mNaverIdLoginSDK.getTokenType();
                String State = mNaverIdLoginSDK.getState().toString();

                Log.e("BaseLoginActivity", "naverLogin::naverAccessToken = " + naverAccessToken);
                Log.e("BaseLoginActivity", "naverLogin::refreshToken = " + refreshToken);
                Log.e("BaseLoginActivity", "naverLogin::expiresAt = " + expiresAt);
                Log.e("BaseLoginActivity", "naverLogin::tokenType = " + tokenType);
                Log.e("BaseLoginActivity", "naverLogin::State = " + State);

                mNidOAuthLogin.callProfileApi(new NidProfileCallback<NidProfileResponse>() {
                    @Override
                    public void onSuccess(NidProfileResponse nidProfileResponse) {
                        Log.e("BaseLoginActivity", "callProfileApi component1()= " + nidProfileResponse.component1());
                        Log.e("BaseLoginActivity", "callProfileApi component2()= " + nidProfileResponse.component2());
                        Log.e("BaseLoginActivity", "callProfileApi component3()= " + nidProfileResponse.component3());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile()= " + nidProfileResponse.getProfile());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getEmail()= " + nidProfileResponse.getProfile().getEmail());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getAge()= " + nidProfileResponse.getProfile().getAge());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getId()= " + nidProfileResponse.getProfile().getId());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getNickname()= " + nidProfileResponse.getProfile().getNickname());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getMobile()= " + nidProfileResponse.getProfile().getMobile());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getName()= " + nidProfileResponse.getProfile().getName());
                        Log.e("BaseLoginActivity", "callProfileApi getProfile().getGender()= " + nidProfileResponse.getProfile().getGender());
                        Log.e("BaseLoginActivity", "callProfileApi getMessage()= " + nidProfileResponse.getMessage());
                        Log.e("BaseLoginActivity", "callProfileApi getResultCode()= " + nidProfileResponse.getResultCode());
                        onLoginSuccess(PrefKit.MEMBER_TYPE_NAVER, nidProfileResponse.getProfile().getId(), nidProfileResponse.getProfile().getName(), naverAccessToken, nidProfileResponse.getProfile().getEmail());
                    }

                    @Override
                    public void onFailure(int i, @NonNull String s) {
                        onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                        naverLogout(mContext);
                    }

                    @Override
                    public void onError(int i, @NonNull String s) {
                        onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                        naverLogout(mContext);
                    }
                });
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.e("BaseLoginActivity", "naverLogin::onFailure = " + i);
                Log.e("BaseLoginActivity", "naverLogin::onFailure = " + s);
                onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                naverLogout(mContext);
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.e("BaseLoginActivity", "naverLogin::onError = " + i);
                Log.e("BaseLoginActivity", "naverLogin::onError = " + s);
                onLoginFailed(PrefKit.MEMBER_TYPE_NAVER);
                naverLogout(mContext);
            }
        });
    }

    protected static void naverLogout(Context context) {
        Log.e(TAG, "naverLogout");
        mNaverIdLoginSDK.logout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult::");
        Log.e(TAG, "onActivityResult::requestCode = " + requestCode);
        Log.e(TAG, "onActivityResult::resultCode = " + resultCode);

        super.onActivityResult(requestCode, resultCode, data);
    }

    protected String getLoginName(String loginType) {
        String loginName = "";
        if (loginType.equals(PrefKit.MEMBER_TYPE_FACEBOOK)) {
            loginName = "페이스북";
        } else if (loginType.equals(PrefKit.MEMBER_TYPE_KAKAO)) {
            loginName = "카카오";
        } else if (loginType.equals(PrefKit.MEMBER_TYPE_NAVER)) {
            loginName = "네이버";
        }

        return loginName;
    }
}