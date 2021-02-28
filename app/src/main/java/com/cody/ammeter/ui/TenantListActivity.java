package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ToolbarLitActivityBinding;
import com.cody.ammeter.viewmodel.ItemTenant;
import com.cody.ammeter.viewmodel.TenantListViewModel;
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
 * 租户列表
 */
public class TenantListActivity extends AbsPageListActivity<ToolbarLitActivityBinding, TenantListViewModel> {

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, TenantListActivity.class));
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
                switch (viewType) {
                    case ItemTenant.DEFAULT_TYPE:
                        return R.layout.item_tenant;
                    case ItemTenant.TYPE_LEAVE:
                        return R.layout.item_leave_tenant;
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
    public Class<TenantListViewModel> getVMClass() {
        return TenantListViewModel.class;
    }

    @Override
    public void onItemClick(final BindingViewHolder holder, final View view, final int position, final int id) {
        switch (holder.getItemViewType()) {
            case ItemTenant.DEFAULT_TYPE:
            case ItemTenant.TYPE_LEAVE:
                ItemTenant item = (ItemTenant) getListAdapter().getItem(position);
                if (item != null) {
                    TenantActivity.start(this, item.getId(), item.getName());
                }
                break;
        }
    }

    @Override
    protected int getLayoutID() {
        return R.layout.toolbar_lit_activity;
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
        }
    }
}