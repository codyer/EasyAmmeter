package com.cody.ammeter.ui;

import android.content.Intent;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * 房东详情
 */
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
            AmmeterHelper.setUnSettlementMoney(ammeter.getOldBalance() - ammeter.getNewBalance());
            getBinding().setBalance(String.format(getString(R.string.format_yuan), ammeter.getNewBalance()));
            getBinding().setAmmeter(String.format(getString(R.string.format_du), ammeter.getNewAmmeter() - ammeter.getOldAmmeter()));
            getBinding().setUsed(String.format(getString(R.string.price_used_hint), ammeter.getOldBalance() - ammeter.getNewBalance()));
            getBinding().setNewAmmeter(String.format(getString(R.string.format), ammeter.getNewAmmeter()));
            hideLoading();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.history, menu);
        return super.onCreateOptionsMenu(menu);
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
    public void showToast(final String message) {
        super.showToast(message);
        getBinding().setInfo(message);
    }

    @Override
    public void showToast(final int message) {
        super.showToast(message);
        getBinding().setInfo(getString(message));
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
            case R.id.newBalance:
                InputActivity.start(this, InputActivity.INPUT_TYPE_BALANCE, Ammeter.UN_TENANT_NAME, mMainAmmeter.getNewBalance());
                break;
            case R.id.newAmmeter:
                InputActivity.start(this, InputActivity.INPUT_TYPE_AMMETER, Ammeter.UN_TENANT_NAME, mMainAmmeter.getOldAmmeter());
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
                long time = new Date().getTime();
                if (mMainAmmeter != null) {
                    if (Double.compare(mMainAmmeter.getNewAmmeter(), mMainAmmeter.getOldAmmeter()) <= 0) {
                        showToast(mMainAmmeter.getName() + getString(R.string.please_input_wrong_hint));
                        return;
                    } else if ((time - mMainAmmeter.getCheckInTime().getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
                        // 数据超过一天无效
                        showToast(String.format(getString(R.string.balance_time_long), mMainAmmeter.getName()));
                        return;
                    } else if ((time - mMainAmmeter.getAmmeterSetTime().getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
                        // 数据超过一天无效
                        showToast(String.format(getString(R.string.time_long), mMainAmmeter.getName()));
                        return;
                    } else if (mMainAmmeter.getOldBalance() <= mMainAmmeter.getNewBalance()) {
                        showToast(getString(R.string.no_need_to));
                        return;
                    }
                }
                showLoading();
                LiveData<Long> longLiveData = AmmeterHelper.liveTenantCount();
                longLiveData.observeForever(new Observer<Long>() {
                    @Override
                    public void onChanged(final Long count) {
                        longLiveData.removeObserver(this);
                        if (count > 0) {
                            hideLoading();
                            SettlementListActivity.start(MainActivity.this);
                        } else {
                            mMainAmmeter.setOldAmmeter(mMainAmmeter.getNewAmmeter());
                            mMainAmmeter.setOldBalance(mMainAmmeter.getNewBalance());
                            AmmeterHelper.updateAmmeter(mMainAmmeter, result -> hideLoading());
                            new AlertDialog.Builder(MainActivity.this).setMessage(R.string.add_tenant_hint)
                                    .setPositiveButton(R.string.ui_str_confirm, (dialog, which) -> {
                                        getBinding().checkIn.performClick();
                                    }).setNegativeButton(R.string.ui_str_cancel, null)
                                    .create().show();
                        }
                    }
                });
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
                    showToast(mMainAmmeter.getName() + String.format(getString(R.string.success_set_balance), value));
                });
                break;
            case InputActivity.INPUT_TYPE_AMMETER:
                mMainAmmeter.setNewAmmeter(value);
                mMainAmmeter.setAmmeterSetTime(new Date());
                showLoading();
                AmmeterHelper.updateAmmeter(mMainAmmeter, result -> {
                    hideLoading();
                    showToast(mMainAmmeter.getName() + String.format(getString(R.string.success_set_ammeter), value));
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