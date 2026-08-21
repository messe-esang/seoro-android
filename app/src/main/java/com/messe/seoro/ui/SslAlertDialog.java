package com.messe.seoro.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.webkit.SslErrorHandler;

public class SslAlertDialog {
    private SslErrorHandler handler = null;
    private AlertDialog dialog = null;

    public SslAlertDialog(SslErrorHandler errorHandler, Context context) {

        if (errorHandler == null || context == null) return;

        this.handler = errorHandler;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("이 사이트의 보안 인증서는 신뢰하는 보안 인증서가 아닙니다. 계속 진행 하시겠습니까?");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.proceed();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                handler.cancel();
            }
        });

        dialog = builder.create();
    }

    public void show() {
        dialog.show();
    }
}