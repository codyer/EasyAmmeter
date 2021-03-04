package com.cody.ammeter.ui;

import android.content.Intent;

import com.cody.component.app.activity.BaseActivity;

/**
 * 闪屏页
 */
public class SplashActivity extends BaseActivity {
    @Override
    protected void onResume() {
        super.onResume();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}