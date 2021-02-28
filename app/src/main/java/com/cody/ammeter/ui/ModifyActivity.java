package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ModifyActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.component.app.activity.BaseActionbarActivity;
import com.cody.component.handler.livedata.StringLiveData;

import androidx.appcompat.widget.Toolbar;


public class ModifyActivity extends BaseActionbarActivity<ModifyActivityBinding> {
    private final StringLiveData mValue = new StringLiveData("");
    public final static int INPUT_TYPE_NAME = 0x01;
    private final static String VALUE = "value";
    private final static String NAME = "name";

    public static void start(Activity activity, String name) {
        Intent intent = new Intent(activity, ModifyActivity.class);
        intent.putExtra(NAME, name);
        activity.startActivityForResult(intent, INPUT_TYPE_NAME);
    }

    public static String getActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && data != null && INPUT_TYPE_NAME == requestCode) {
            return data.getStringExtra(VALUE);
        }
        return null;
    }

    @Override
    protected void onBaseReady(final Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        String name = Ammeter.UN_TENANT_NAME;
        if (getIntent() != null) {
            name = getIntent().getStringExtra(NAME);
        }
        mValue.setValue(name);
        getBinding().setOldName(name);
        getBinding().setViewData(mValue);
        setTitle(getString(R.string.modify_name));
    }

    @Override
    protected int getLayoutID() {
        return R.layout.modify_activity;
    }

    @Override
    protected int getToolbarId() {
        return R.id.toolbar;
    }

    @Override
    protected Toolbar getToolbar() {
        return getBinding().toolbar;
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.submit) {
            if (mValue.get().length() < 1 || TextUtils.isEmpty(mValue.get())) {
                showToast(R.string.name_err);
                return;
            } else if (mValue.get().length() > 4) {
                showToast(R.string.name_max_length);
                return;
            }
            returnResult();
        }
    }

    @Override
    public void scrollToTop() {
        returnResult();
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(VALUE, mValue.get());
        setResult(RESULT_OK, intent);
        finish();
    }
}