package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ToolbarLitActivityBinding;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.ammeter.util.TimeUtil;
import com.cody.ammeter.viewmodel.HistoryListViewModel;
import com.cody.ammeter.viewmodel.ItemAmmeter;
import com.cody.component.app.activity.AbsPageListActivity;
import com.cody.component.app.widget.friendly.FriendlyLayout;
import com.cody.component.bind.adapter.list.BindingViewHolder;
import com.cody.component.bind.adapter.list.MultiBindingPageListAdapter;
import com.cody.component.handler.data.FriendlyViewData;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 历史结算记录列表
 */
public class HistoryListActivity extends AbsPageListActivity<ToolbarLitActivityBinding, HistoryListViewModel> {

    private final static String SETTLEMENT_TIME = "SETTLEMENT_TIME";
    private Date mTime = null;

    /**
     * 按时间分次数列表
     */
    public static void start(Activity activity, Date time) {
        Intent intent = new Intent(activity, HistoryListActivity.class);
        intent.putExtra(SETTLEMENT_TIME, time);
        activity.startActivity(intent);
    }

    /**
     * 主列表
     * 所有结算总记录
     */
    public static void start(Activity activity) {
        Intent intent = new Intent(activity, HistoryListActivity.class);
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
    public HistoryListViewModel buildViewModel() {
        if (getIntent() != null) {
            mTime = (Date) getIntent().getSerializableExtra(SETTLEMENT_TIME);
        }
        if (mTime != null) {
            setTitle(TimeUtil.getLongTimeString(mTime));
        } else {
            setTitle(R.string.settlement_history);
        }
        return new HistoryListViewModel(mTime);
    }

    @NonNull
    @Override
    public MultiBindingPageListAdapter buildListAdapter() {
        return new MultiBindingPageListAdapter(this, this) {
            @Override
            public int getItemLayoutId(int viewType) {
                if (viewType == ItemAmmeter.DEFAULT_TYPE) {
                    return R.layout.item_history_sub;
                } else if (viewType == ItemAmmeter.MAIN_TYPE) {
                    return R.layout.item_histtory_main;
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
    public Class<HistoryListViewModel> getVMClass() {
        return HistoryListViewModel.class;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mTime != null) {
            getMenuInflater().inflate(R.menu.share, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case R.id.share:
                share();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, AmmeterHelper.getShareInfo(this,
                getViewModel().getPagedList().getValue().snapshot()).toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    @Override
    public void onItemClick(final BindingViewHolder holder, final View view, final int position, final int id) {
        if (mTime == null && getListAdapter().getItem(position) instanceof ItemAmmeter) {
            ItemAmmeter itemAmmeter = (ItemAmmeter) getListAdapter().getItem(position);
            if (itemAmmeter != null) {
                HistoryListActivity.start(this, itemAmmeter.getTime());
            }
        } else {
            if (AmmeterHelper.copy(this, getViewModel().getPagedList().getValue().snapshot())) {
                showToast(TimeUtil.getTimeString(mTime) + "的结算数据已经复制到剪切板！");
            }
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
            scrollToTop();
        }
    }
}