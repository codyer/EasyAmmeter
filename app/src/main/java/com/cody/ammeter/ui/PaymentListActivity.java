package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ToolbarLitActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.viewmodel.ItemPayment;
import com.cody.ammeter.viewmodel.PaymentListViewModel;
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
 * 缴费记录列表
 */
public class PaymentListActivity extends AbsPageListActivity<ToolbarLitActivityBinding, PaymentListViewModel> {

    private final static String AMMETER_ID = "AMMETER_ID";

    public static void start(Activity activity, long ammeterId) {
        Intent intent = new Intent(activity, PaymentListActivity.class);
        intent.putExtra(AMMETER_ID, ammeterId);
        activity.startActivity(intent);
    }

    @Override
    public boolean isSupportImmersive() {
        return false;
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

    @Override
    public PaymentListViewModel buildViewModel() {
        long ammeterId = Ammeter.UN_TENANT_ID;
        if (getIntent() != null) {
            ammeterId = getIntent().getLongExtra(AMMETER_ID, Ammeter.UN_TENANT_ID);
        }
        return new PaymentListViewModel(ammeterId);
    }

    @NonNull
    @Override
    public MultiBindingPageListAdapter buildListAdapter() {
        return new MultiBindingPageListAdapter(this, this) {
            @Override
            public int getItemLayoutId(int viewType) {
                if (viewType == ItemPayment.DEFAULT_TYPE) {
                    return R.layout.item_payment;
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
    public Class<PaymentListViewModel> getVMClass() {
        return PaymentListViewModel.class;
    }

    @Override
    public void onItemClick(final BindingViewHolder holder, final View view, final int position, final int id) {
    }

    @Override
    protected int getLayoutID() {
        return R.layout.toolbar_lit_activity;
    }

    @Override
    protected void onBaseReady(Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        setSupportActionBar(getBinding().toolbar);
        setTitle(R.string.payment_record);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.top) {
            RecyclerViewUtil.smoothScrollToTop(getBinding().recyclerView);
        }
    }
}