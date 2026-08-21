package com.messe.seoro.kit;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefKit {

    public static final String PREF_UUID = "uuid";

    public static final String MEMBER_TYPE_FACEBOOK = "facebook";
    public static final String MEMBER_TYPE_KAKAO = "kakao";
    public static final String MEMBER_TYPE_NAVER = "naver";

    public static final String PREF_MEMBER_IDX = "member_idx";
    public static final String PREF_EXEC_COUNT_FOR_PUSH_AGREE = "EXEC_count_for_push_agree";
    public static final String PREF_AGREE_PUSH_NOTI = "agree_push_noti";
    public static final String PREF_FIRST_PERMISSION = "first_permission";
    public static final String PREF_USER_ADID = "user_adid";
    public static final String PREF_USER_PUSH_TOKEN = "user_push_token";

    /// ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static public String getDefaultPreferenceString(Context context, String key, String defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getString(key, defaultValue);
    }

    static public void setDefaultPreferenceString(Context context, String key, String value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putString(key, value);
        editor.commit();
    }

    static public boolean getDefaultPreferenceBoolean(Context context, String key, boolean defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getBoolean(key, defaultValue);
    }

    static public void setDefaultPreferenceBoolean(Context context, String key, boolean value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    static public int getDefaultPreferenceInt(Context context, String key, int defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getInt(key, defaultValue);
    }

    static public void setDefaultPreferenceInt(Context context, String key, int value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    static public long getDefaultPreferenceLong(Context context, String key, long defaultValue) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);

        return defaultPreference.getLong(key, defaultValue);
    }

    static public void setDefaultPreferenceLong(Context context, String key, long value) {
        final SharedPreferences defaultPreference = PreferenceManager.getDefaultSharedPreferences(context);
        Editor editor = defaultPreference.edit();

        editor.putLong(key, value);
        editor.commit();
    }

    static public String getMemberIdx(Context context) {
        return getDefaultPreferenceString(context, PREF_MEMBER_IDX, "");
    }

    static public void setMemberIdx(Context context, String mem_idx) {
        setDefaultPreferenceString(context, PREF_MEMBER_IDX, mem_idx);
    }

    static public boolean getFirstPermission(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_FIRST_PERMISSION, true);
    }

    static public void setFirstPermission(Context context, boolean firstPermission) {
        setDefaultPreferenceBoolean(context, PREF_FIRST_PERMISSION, firstPermission);
    }

    // 첫 실행 여부 (MainActivity 진입 후)
    static public long getExecCountForPushAgree(Context context) {
        return getDefaultPreferenceLong(context, PREF_EXEC_COUNT_FOR_PUSH_AGREE, 0);
    }

    static public void increaseExecCountForPushAgree(Context context) {
        long count = getExecCountForPushAgree(context);
        setDefaultPreferenceLong(context, PREF_EXEC_COUNT_FOR_PUSH_AGREE, ++count);
    }

    // 푸시 알림 동의 여부
    static public boolean getAgreePushNoti(Context context) {
        return getDefaultPreferenceBoolean(context, PREF_AGREE_PUSH_NOTI, false);
    }

    static public void setAgreePushNoti(Context context, boolean agree) {
        setDefaultPreferenceBoolean(context, PREF_AGREE_PUSH_NOTI, agree);
    }

    static public String getPushToken(Context context) {
        return getDefaultPreferenceString(context, PREF_USER_PUSH_TOKEN, "");
    }

    static public void setPushToken(Context context, String token) {
        setDefaultPreferenceString(context, PREF_USER_PUSH_TOKEN, token);
    }

    static public String getUserAdId(Context context) {
        return getDefaultPreferenceString(context, PREF_USER_ADID, "");
    }

    static public void setUserAdId(Context context, String adid) {
        setDefaultPreferenceString(context, PREF_USER_ADID, adid);
    }

    static public String getUUID(Context context) {
        return getDefaultPreferenceString(context, PREF_UUID, "");
    }

    static public void setUUID(Context context, String uuid) {
        setDefaultPreferenceString(context, PREF_UUID, uuid);
    }
}
