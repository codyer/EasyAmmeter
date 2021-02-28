package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.SettlementLitActivityBinding;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.ammeter.viewmodel.ItemTenant;
import com.cody.ammeter.viewmodel.SettlementListViewModel;
import com.cody.component.app.activity.AbsPageListActivity;
import com.cody.component.app.widget.friendly.FriendlyLayout;
import com.cody.component.bind.adapter.list.BindingViewHolder;
import com.cody.component.bind.adapter.list.MultiBindingPageListAdapter;
import com.cody.component.handler.data.FriendlyViewData;
import com.cody.component.util.RecyclerViewUtil;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 结算列表
 */
public class SettlementListActivity extends AbsPageListActivity<SettlementLitActivityBinding, SettlementListViewModel> {

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettlementListActivity.class));
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
    public FriendlyLayout getFriendlyLayout() {
        return getBinding().friendlyView;
    }

    @NonNull
    @Override
    public MultiBindingPageListAdapter buildListAdapter() {
        return new MultiBindingPageListAdapter(this, this) {
            @Override
            public int getItemLayoutId(int viewType) {
                if (viewType == ItemTenant.DEFAULT_TYPE) {
                    return R.layout.item_tenant;
                }
                return super.getItemLayoutId(viewType);
            }
        };
    }

    @NonNull
    @Override
    public RecyclerView getRecyclerView() {
        return getBinding().recyclerView;
    }

    @Override
    protected FriendlyViewData getViewData() {
        return getViewModel().getFriendlyViewData();
    }

    @NonNull
    @Override
    public Class<SettlementListViewModel> getVMClass() {
        return SettlementListViewModel.class;
    }

    @Override
    public void onItemClick(final BindingViewHolder holder, final View view, final int position, final int id) {
        if (holder.getItemViewType() == ItemTenant.DEFAULT_TYPE) {
            ItemTenant item = (ItemTenant) getListAdapter().getItem(position);
            if (item != null) {
                TenantActivity.start(this, item.getId(), item.getName());
            }
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.settlement_lit_activity;
    }

    @Override
    protected void onBaseReady(Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        setSupportActionBar(getBinding().toolbar);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.top) {
            RecyclerViewUtil.smoothScrollToTop(getBinding().recyclerView);
        } else if (v.getId() == R.id.settlement) {
            showLoading();
            AmmeterHelper.settlement(result -> {
                hideLoading();
                showToast(R.string.settlement_success);
            });
        }
    }
}