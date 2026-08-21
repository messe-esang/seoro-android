package com.messe.seoro.kit;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.kakao.sdk.common.util.KakaoCustomTabsClient;
import com.kakao.sdk.share.ShareClient;
import com.kakao.sdk.share.WebSharerClient;
import com.kakao.sdk.template.model.Content;
import com.kakao.sdk.template.model.FeedTemplate;
import com.kakao.sdk.template.model.ItemContent;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.Social;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;

public class ShareKit {

    public static void shareKakao(final Context context, String title, String shareImageUrl,
                                  String shareLink1, String btnTitle) {
        try {
            //최신 카카오 sdk버전용
            FeedTemplate feedTemplate = new FeedTemplate(
                    new Content(title,
                            shareImageUrl,
                            new Link(shareLink1),
                            null
                    ),
                    new ItemContent(null,
                            null,
                            null,
                            null,
                            null
                    ),
                    new Social(null, null, null),
                    Arrays.asList(new com.kakao.sdk.template.model.Button(btnTitle, new Link(shareLink1)))
            );

            if (ShareClient.getInstance().isKakaoTalkSharingAvailable(context)) { //카톡 설치 됨
                ShareClient.getInstance().shareDefault(context, feedTemplate, null, (shareResult, error) -> {
                    if (error != null) {
                        Log.e("ShareKit", "sendKakaoLink: 카카오링크 보내기 실패", error);
                    } else if (shareResult != null) {
                        Log.e("ShareKit", "sendKakaoLink: 카카오링크 보내기 성공 " + shareResult.getIntent());
                        context.startActivity(shareResult.getIntent());

                        // 카카오링크 보내기에 성공했지만 아래 경고 메시지가 존재할 경우 일부 컨텐츠가 정상 동작하지 않을 수 있습니다.
                        Log.e("ShareKit", "Warning Msg: " + shareResult.getWarningMsg());
                        Log.e("ShareKit", "Argument Msg: " + shareResult.getArgumentMsg());
                    }
                    return null;
                });
            } else {  //카톡 설치 안됨
                Uri shareUrl = WebSharerClient.getInstance().makeDefaultUrl(feedTemplate);//.defaultTemplateUri(feedTemplate);

                try {
                    KakaoCustomTabsClient.INSTANCE.open(context, shareUrl);
                } catch (ActivityNotFoundException e) {
                    Log.e("ShareKit", "sendKakaoLink: can not find WebBrowser" + e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "카카오링크를 실행 할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public static void shareLINE(Context context, String shareContent) {
        String msg = shareContent;
        try {
            msg = URLEncoder.encode(msg, "utf-8");

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format("line://msg/text/%s", msg)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ActivityNotFoundException e) {
            // LINE 설치
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(String.format("market://details?id=%s", "jp.naver.line.android")));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void shareSMS(Context context, String shareContent) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra("sms_body", shareContent); // 보낼 문자
            intent.setType("vnd.android-dir/mms-sms");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyClipboard(Context context, String content) {
        try {
            ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("label", content);
            clipboardManager.setPrimaryClip(clipData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
