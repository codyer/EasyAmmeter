package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.TenantActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.component.app.activity.BaseActionbarActivity;

import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

/**
 * 租客详情
 */
public class TenantActivity extends BaseActionbarActivity<TenantActivityBinding> {
    private Ammeter mTenantAmmeter;
    private final static String TENANT_ID = "TENANT_ID";
    private final static String NAME = "name";

    public static void start(Activity activity, long tenantId, String name) {
        Intent intent = new Intent(activity, TenantActivity.class);
        intent.putExtra(TENANT_ID, tenantId);
        intent.putExtra(NAME, name);
        activity.startActivity(intent);
    }

    @Override
    protected int getLayoutID() {
        return R.layout.tenant_activity;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onBaseReady(final Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        String name = Ammeter.UN_TENANT_NAME;
        long tenantId = Ammeter.UN_TENANT_ID;
        if (getIntent() != null) {
            name = getIntent().getStringExtra(NAME);
            tenantId = getIntent().getLongExtra(TENANT_ID, Ammeter.UN_TENANT_ID);
        }
        setTitle(name);
        showLoading();
        AmmeterHelper.getTenant(tenantId).observeForever(ammeter -> {
            if (ammeter == null) return;
            mTenantAmmeter = ammeter;
            setTitle(ammeter.getName() + (ammeter.isLeave() ? "(已退租)" : ""));
            if (ammeter.getNewBalance() > 0f) {
                getBinding().setBalance(String.format(getString(R.string.format_yuan), ammeter.getNewBalance()));
            } else {
                getBinding().setBalance(String.format("已欠费：%.2f元", Math.abs(ammeter.getNewBalance())));
            }
            getBinding().setAmmeter(String.format(getString(R.string.format_du), ammeter.getNewAmmeter()));
            hideLoading();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.edit:
                ModifyActivity.start(this, mTenantAmmeter.getName());
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
            case R.id.newAmmeter:
                InputActivity.start(this, InputActivity.INPUT_TYPE_AMMETER, mTenantAmmeter.getName());
                break;
            case R.id.newBalance:
            case R.id.rechargePayment:
                InputActivity.start(this, InputActivity.INPUT_TYPE_PAYMENT, mTenantAmmeter.getName());
                break;
            case R.id.paymentRecord:
                PaymentListActivity.start(this, mTenantAmmeter.getId());
                break;
            case R.id.checkOut:
                if (mTenantAmmeter.isLeave()) {
                    showToast(getString(R.string.check_out_success));
                    return;
                }
                if (AmmeterHelper.getUnSettlementMoney() > 0f) {
                    new AlertDialog.Builder(this).setMessage(
                            String.format(getString(R.string.check_out_info_total), AmmeterHelper.getUnSettlementMoney()))
                            .setPositiveButton(R.string.ui_str_confirm, (dialog, which) -> {
                                showLoading();
                                AmmeterHelper.checkOut(mTenantAmmeter, result -> {
                                    hideLoading();
                                    showToast(R.string.check_out_success);
                                });
                            }).setNegativeButton(R.string.ui_str_cancel, null)
                            .create().show();
                } else if (mTenantAmmeter.getNewBalance() < 0f) {
                    new AlertDialog.Builder(this).setMessage(R.string.check_out_info)
                            .setPositiveButton(R.string.ui_str_confirm, (dialog, which) -> {
                                showLoading();
                                AmmeterHelper.checkOut(mTenantAmmeter, result -> {
                                    hideLoading();
                                    showToast(R.string.check_out_success);
                                });
                            }).setNegativeButton(R.string.ui_str_cancel, null)
                            .create().show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        float value = InputActivity.getActivityResult(resultCode, data);
        String name = ModifyActivity.getActivityResult(requestCode, resultCode, data);
        if (value >= 0f) {
            switch (requestCode) {
                case InputActivity.INPUT_TYPE_AMMETER:
                    mTenantAmmeter.setNewAmmeter(value);
                    mTenantAmmeter.setAmmeterSetTime(new Date());
                    showLoading();
                    AmmeterHelper.updateAmmeter(mTenantAmmeter, result -> {
                        hideLoading();
                        showToast(mTenantAmmeter.getName() + String.format(getString(R.string.success_set_ammeter), value));
                    });
                    break;
                case InputActivity.INPUT_TYPE_PAYMENT:
                    showLoading();
                    AmmeterHelper.addPayment(mTenantAmmeter, value, result -> {
                        hideLoading();
                        showToast(mTenantAmmeter.getName() + String.format(getString(R.string.success_set_payment), value));
                    });
                    break;
            }
        }
        if (name != null) {
            showLoading();
            AmmeterHelper.updateTenantName(mTenantAmmeter, name, result -> {
                hideLoading();
                showToast(R.string.modify_name_success);
            });
        }
    }

    @Override
    public void scrollToTop() {

    }
}