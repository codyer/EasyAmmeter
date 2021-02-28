package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.InputActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.component.app.activity.BaseActionbarActivity;
import com.cody.component.handler.livedata.StringLiveData;

import androidx.appcompat.widget.Toolbar;


public class InputActivity extends BaseActionbarActivity<InputActivityBinding> {
    private final StringLiveData mValue = new StringLiveData("0");
    public final static int INPUT_TYPE_BALANCE = 0x01;// 余额设定
    public final static int INPUT_TYPE_AMMETER = 0x02;// 电表输入
    public final static int INPUT_TYPE_NEW_TENANT = 0x03;// 新户入住
    public final static int INPUT_TYPE_PAYMENT = 0x04;// 充值缴费
    private final static String VALUE = "value";
    private final static String NAME = "name";
    private final static String TYPE = "type";

    public static void start(Activity activity, int type, String name) {
        Intent intent = new Intent(activity, InputActivity.class);
        intent.putExtra(TYPE, type);
        intent.putExtra(NAME, name);
        activity.startActivityForResult(intent, type);
    }

    public static float getActivityResult(final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            return data.getFloatExtra(VALUE, -1f);
        }
        return -1f;
    }

    @Override
    protected void onBaseReady(final Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        String name = Ammeter.UN_TENANT_NAME;
        int type = INPUT_TYPE_AMMETER;
        if (getIntent() != null) {
            name = getIntent().getStringExtra(NAME);
            type = getIntent().getIntExtra(TYPE, INPUT_TYPE_AMMETER);
        }
        getBinding().setViewData(mValue);
        switch (type) {
            case INPUT_TYPE_BALANCE:
                setTitle(name + getString(R.string.title_balance));
                getBinding().setHint(getString(R.string.please_input_balance_hint));
                getBinding().setUnit(getString(R.string.yuan));
                break;
            case INPUT_TYPE_AMMETER:
                setTitle(name + getString(R.string.title_ammeter));
                getBinding().setHint(getString(R.string.please_input_ammeter_hint));
                getBinding().setUnit(getString(R.string.degree));
                break;
            case INPUT_TYPE_NEW_TENANT:
                setTitle(getString(R.string.ammeter_check_in));
                getBinding().setHint(getString(R.string.new_ammeter_hint));
                getBinding().setUnit(getString(R.string.degree));
                break;
            case INPUT_TYPE_PAYMENT:
                setTitle(name + getString(R.string.title_payment));
                getBinding().setHint(getString(R.string.please_input_payment_hint));
                getBinding().setUnit(getString(R.string.yuan));
                break;
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.input_activity;
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
        switch (v.getId()) {
            case R.id.number_0:
                mValue.setValue(mValue.get() + 0);
                break;
            case R.id.number_1:
                mValue.setValue(mValue.get() + 1);
                break;
            case R.id.number_2:
                mValue.setValue(mValue.get() + 2);
                break;
            case R.id.number_3:
                mValue.setValue(mValue.get() + 3);
                break;
            case R.id.number_4:
                mValue.setValue(mValue.get() + 4);
                break;
            case R.id.number_5:
                mValue.setValue(mValue.get() + 5);
                break;
            case R.id.number_6:
                mValue.setValue(mValue.get() + 6);
                break;
            case R.id.number_7:
                mValue.setValue(mValue.get() + 7);
                break;
            case R.id.number_8:
                mValue.setValue(mValue.get() + 8);
                break;
            case R.id.number_9:
                mValue.setValue(mValue.get() + 9);
                break;
            case R.id.dot:
                if (!mValue.get().contains(".")) {
                    if (mValue.get().length() == 0) {
                        mValue.setValue(mValue.get() + "0");
                    }
                    mValue.setValue(mValue.get() + ".");
                }
                break;
            case R.id.delete:
                if (mValue.get().length() > 0 && !mValue.get().equals("0")) {
                    mValue.setValue(mValue.get().substring(0, mValue.get().length() - 1));
                }
                if (mValue.get().length() == 0) {
                    mValue.setValue(mValue.get() + "0");
                }
                break;
            case R.id.deleteAll:
                mValue.setValue("0");
                break;
            case R.id.ok:
                returnResult();
                break;
        }
        // 0 不作为数字前锥
        while (mValue.get().startsWith("0") && !mValue.get().contains(".") && mValue.get().length() > 1) {
            mValue.setValue(mValue.get().substring(1));
        }
        // 最长保留10位数
        while (mValue.get().length() > 10) {
            mValue.setValue(mValue.get().substring(0, mValue.get().length() - 1));
            showToast(getString(R.string.only_10_count));
        }
        // 小数点最多保留两位
        while (mValue.get().contains(".") && (mValue.get().length() - mValue.get().indexOf(".")) > 3) {
            mValue.setValue(mValue.get().substring(0, mValue.get().length() - 1));
            showToast(getString(R.string.save_2_decimal));
        }
    }

    @Override
    public void scrollToTop() {
        returnResult();
    }

    private void returnResult() {
        Intent intent = new Intent();
        intent.putExtra(VALUE, Float.parseFloat(mValue.get()));
        setResult(RESULT_OK, intent);
        finish();
    }
}