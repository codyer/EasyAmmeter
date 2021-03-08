package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.MainActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.component.app.activity.BaseActionbarActivity;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

/**
 * 房东详情
 */
@SuppressLint("NonConstantResourceId")
public class MainActivity extends BaseActionbarActivity<MainActivityBinding> {
    private Ammeter mMainAmmeter;

    @Override
    public boolean isSupportSwipeBack() {
        return false;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.main_activity;
    }

    @Override
    protected boolean isShowBack() {
        return false;
    }

    @Override
    public boolean isSupportImmersive() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
        showLoading();
        AmmeterHelper.getMainAmmeter().observeForever(ammeter -> {
            if (ammeter == null) return;
            mMainAmmeter = ammeter;
            AmmeterHelper.setUnSettlementMoney(ammeter.getOldBalance());
            getBinding().setUsed(String.format(getString(R.string.price_used_hint), ammeter.getOldBalance()));
            hideLoading();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //设置字体为默认大小，不随系统字体大小改而改变
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//非默认值
            getResources();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
        return res;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.history:
                HistoryListActivity.start(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            case R.id.history:
                HistoryListActivity.start(this);
                break;
            case R.id.initData:
                InitListActivity.start(this);
                break;
            case R.id.rechargePayment:
                InputActivity.start(this, InputActivity.INPUT_TYPE_PAYMENT, Ammeter.UN_TENANT_NAME, mMainAmmeter.getNewBalance());
                break;
            case R.id.paymentRecord:
                PaymentListActivity.start(this, Ammeter.UN_TENANT_ID);
                break;
            case R.id.checkIn:
                InputActivity.start(this, InputActivity.INPUT_TYPE_NEW_TENANT, Ammeter.UN_TENANT_NAME, 0f);
                break;
            case R.id.myTenant:
                TenantListActivity.start(this);
                break;
            case R.id.settlement:
                InputActivity.start(MainActivity.this, InputActivity.INPUT_TYPE_BALANCE, Ammeter.UN_TENANT_NAME, mMainAmmeter.getNewBalance());
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
                mMainAmmeter.setNewBalance(value);
                mMainAmmeter.setCheckInTime(new Date());
                showLoading();
                AmmeterHelper.updateAmmeter(mMainAmmeter, result -> {
                    hideLoading();
                    SettlementListActivity.start(MainActivity.this);
                });
                break;
            case InputActivity.INPUT_TYPE_NEW_TENANT:
                showLoading();
                AmmeterHelper.addTenant(value, result -> {
                    hideLoading();
                    showToast(String.format(getString(R.string.success_new_ammeter), value));
                });
                break;
            case InputActivity.INPUT_TYPE_PAYMENT:
                showLoading();
                AmmeterHelper.addPayment(mMainAmmeter, value, result -> {
                    hideLoading();
                    showToast(mMainAmmeter.getName() + String.format(getString(R.string.success_set_payment), value));
                });
                break;
        }
    }

    @Override
    public void scrollToTop() {

    }
}