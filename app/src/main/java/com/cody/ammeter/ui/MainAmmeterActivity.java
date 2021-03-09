package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.MainAmmeterActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.component.app.activity.BaseActionbarActivity;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;


public class MainAmmeterActivity extends BaseActionbarActivity<MainAmmeterActivityBinding> {

    private Ammeter mMainAmmeter;

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MainAmmeterActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public boolean isSupportImmersive() {
        return false;
    }

    @Override
    protected void onBaseReady(final Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        setTitle(getString(R.string.please_input_main_data));
        AmmeterHelper.getMainAmmeter().observe(this, ammeter -> {
            if (ammeter == null) return;
            mMainAmmeter = ammeter;
            getBinding().setAmmeter(ammeter);
        });
    }

    @Override
    protected int getLayoutID() {
        return R.layout.main_ammeter_activity;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }

    @Override
    protected Toolbar getToolbar() {
        return getBinding().toolbar;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(final View v) {
        if (mMainAmmeter == null) return;
        switch (v.getId()) {
            case R.id.thisBalance:
                InputActivity.start(this, InputActivity.INPUT_TYPE_BALANCE, mMainAmmeter.getName());
                break;
            case R.id.thisAmmeter:
                InputActivity.start(this, InputActivity.INPUT_TYPE_AMMETER, mMainAmmeter.getName());
                break;
            case R.id.next:
                SettlementListActivity.start(MainAmmeterActivity.this);
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        double value = InputActivity.getActivityResult(resultCode, data);
        if (value < 0) return;
        switch (requestCode) {
            case InputActivity.INPUT_TYPE_BALANCE:
                showLoading();
                mMainAmmeter.setNewBalance(value);
                mMainAmmeter.setCheckInTime(new Date());
                AmmeterHelper.updateAmmeter(mMainAmmeter, result -> hideLoading());
                break;
            case InputActivity.INPUT_TYPE_AMMETER:
                showLoading();
                mMainAmmeter.setNewAmmeter(value);
                mMainAmmeter.setAmmeterSetTime(new Date());
                AmmeterHelper.updateAmmeter(mMainAmmeter, result -> hideLoading());
                break;
        }
    }
}