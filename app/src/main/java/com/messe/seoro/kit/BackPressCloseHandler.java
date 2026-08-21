package com.messe.seoro.kit;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.messe.seoro.R;


public class BackPressCloseHandler {
    private long backKeyPressedTime = 0;
    private Activity activity;
    private View mainLayout;

    public BackPressCloseHandler(Activity context, View layout) {
        this.activity = context;
        this.mainLayout = layout;
    }

    public void onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            showGuide();
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            activity.setResult(Activity.RESULT_CANCELED);
            activity.finishAffinity();
        }
    }

    public void showGuide() {
        Snackbar snackbar = Snackbar.make(mainLayout, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Snackbar.LENGTH_SHORT);
        snackbar.getView().setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
        snackbar.setBackgroundTint(ContextCompat.getColor(activity, R.color.colorPrimary));
        snackbar.setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);
        View snackbarView = snackbar.getView();
        TextView snackbarText = (TextView) snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        snackbarText.setTextColor(Color.WHITE);
        snackbar.show();
    }
}
