package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cody.ammeter.BuildConfig;
import com.cody.ammeter.R;
import com.cody.ammeter.databinding.MainActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.component.app.activity.BaseActionbarActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + BuildConfig.VERSION_NAME);
        showLoading();
        AmmeterHelper.getMainAmmeter().observeForever(ammeter -> {
            if (ammeter == null) return;
            mMainAmmeter = ammeter;
            getBinding().setBalance(String.format("待结算：%.2f 元", ammeter.getOldBalance()));
            getBinding().setOldAmmeter(String.format("总表上次\n%.2f 度", ammeter.getOldAmmeter()));
            getBinding().setNewAmmeter(String.format("总表本次\n%.2f 度", ammeter.getNewAmmeter()));
            AmmeterHelper.setUnSettlementMoney(ammeter.getOldBalance());
            hideLoading();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.clear, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.clear:
                new AlertDialog.Builder(this).setMessage("是否清空所有历史数据？【不可恢复】")
                        .setPositiveButton(R.string.ui_str_confirm, (dialog, which) -> {
                            showLoading();
                            AmmeterHelper.clearAll(result -> {
                                hideLoading();
                                showToast("所有数据已经清空！");
                            });
                        })
                        .setNegativeButton(R.string.ui_str_cancel, null)
                        .create().show();
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
                InputActivity.start(this, InputActivity.INPUT_TYPE_PAYMENT, Ammeter.UN_TENANT_NAME);
                break;
            case R.id.paymentRecord:
                PaymentListActivity.start(this, Ammeter.UN_TENANT_ID);
                break;
            case R.id.checkIn:
                InputActivity.start(this, InputActivity.INPUT_TYPE_NEW_TENANT, Ammeter.UN_TENANT_NAME);
                break;
            case R.id.myTenant:
                TenantListActivity.start(this);
                break;
            case R.id.settlement:
                MainAmmeterActivity.start(MainActivity.this);
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        double value = InputActivity.getActivityResult(resultCode, data);
        if (value < 0) return;
        switch (requestCode) {
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