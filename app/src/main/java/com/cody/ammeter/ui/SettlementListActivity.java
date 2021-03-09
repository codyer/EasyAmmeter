package com.cody.ammeter.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ItemNotSetBinding;
import com.cody.ammeter.databinding.ItemSettlementMainBinding;
import com.cody.ammeter.databinding.ItemSettlementSubBinding;
import com.cody.ammeter.databinding.SettlementLitActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.ammeter.viewmodel.ItemAmmeter;
import com.cody.component.app.activity.BaseActionbarActivity;
import com.cody.component.util.RecyclerViewUtil;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 结算列表
 */
public class SettlementListActivity extends BaseActionbarActivity<SettlementLitActivityBinding> {
    private Ammeter mClickedAmmeter;
    private List<Ammeter> mAmmeterList;
    private LiveData<List<Ammeter>> mListLiveData;
    private double mSharing = 0.0f;
    private double mPrice = 0.0f;
    private boolean mFinished = false;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, SettlementListActivity.class));
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
    protected int getLayoutID() {
        return R.layout.settlement_lit_activity;
    }

    @Override
    protected void onBaseReady(Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        setSupportActionBar(getBinding().toolbar);
        setTitle(R.string.settlement);
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mListLiveData = AmmeterHelper.getAllAmmeter();
        mListLiveData.observe(this, ammeters -> {
            if (mFinished) {
                if (AmmeterHelper.copy(this, mAmmeterList, mSharing, mPrice)) {
                    showToast("最新结算数据已经复制到剪切板！");
                }
                return;
            }
            calculate(ammeters);
            mAmmeterList = ammeters;
            getBinding().recyclerView.setAdapter(new AmmeterAdapter(ammeters, SettlementListActivity.this));
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        double value = InputActivity.getActivityResult(resultCode, data);
        if (value < 0) return;
        if (mClickedAmmeter != null) {
            if (requestCode == InputActivity.INPUT_TYPE_AMMETER) {
                showLoading();
                mClickedAmmeter.setNewAmmeter(value);
                mClickedAmmeter.setAmmeterSetTime(new Date());
                AmmeterHelper.updateAmmeter(mClickedAmmeter, result -> {
                    hideLoading();
                    mClickedAmmeter = null;
                });
            }
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.top) {
            RecyclerViewUtil.smoothScrollToTop(getBinding().recyclerView);
        } else if (v.getId() == R.id.settlement) {
            if (!calculate(mAmmeterList)) return;
            showLoading();
            mListLiveData.removeObservers(this);
            mFinished = true;
            AmmeterHelper.settlement(mAmmeterList, mSharing, mPrice, result -> {
                hideLoading();
                showToast(R.string.settlement_success);
                Intent intent = new Intent(SettlementListActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
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
        if (calculate(mAmmeterList)) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, AmmeterHelper.getShareInfo(mAmmeterList, mSharing, mPrice).toString());
            sendIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendIntent, null));
        }
    }

    private boolean calculate(List<Ammeter> ammeters) {
        Ammeter total = null;
        for (int i = 0; i < ammeters.size(); i++) {
            total = ammeters.get(i);
            if (total.getId() == Ammeter.UN_TENANT_ID) {
                break;
            }
        }
        if (total == null) {
            return false;
        }

        double publicUse = total.getNewAmmeter() - total.getOldAmmeter();
        mPrice = (total.getOldBalance() - total.getNewBalance()) / publicUse;
        int count = 0;
        Ammeter item;
        long time = new Date().getTime();
        for (int i = 0; i < ammeters.size(); i++) {
            item = ammeters.get(i);
            if (item.getNewAmmeter() <= 0f || item.getNewAmmeter() <= item.getOldAmmeter()) {
                showToast(item.getName() + getString(R.string.please_input_wrong_hint));
                return false;
            }
            if ((time - item.getAmmeterSetTime().getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
                // 数据超过一天无效
                showToast(String.format(getString(R.string.time_long), item.getName()));
                return false;
            }

            if (item.getId() != Ammeter.UN_TENANT_ID) {
                publicUse -= (item.getNewAmmeter() - item.getOldAmmeter());
                count++;
            }
            if (count > 0) {
                mSharing = publicUse / count;
            }
        }
        return true;
    }

    class AmmeterAdapter extends RecyclerView.Adapter<ItemAmmeterViewHolder<?>> {
        private final List<Ammeter> mAmmeters;
        private final LifecycleOwner mLifecycleOwner;

        public AmmeterAdapter(final List<Ammeter> ammeters, final LifecycleOwner lifecycleOwner) {
            mAmmeters = ammeters;
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public ItemAmmeterViewHolder<?> onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            if (viewType == ItemAmmeter.MAIN_TYPE) {//总表
                return new ItemMainAmmeterViewHolder(ItemSettlementMainBinding.inflate(LayoutInflater.from(parent.getContext())));
            } else if (viewType == ItemAmmeter.NOT_SET_TYPE) {
                return new ItemUnSetAmmeterViewHolder(ItemNotSetBinding.inflate(LayoutInflater.from(parent.getContext())));
            } else {
                return new ItemSubAmmeterViewHolder(ItemSettlementSubBinding.inflate(LayoutInflater.from(parent.getContext())));
            }
        }

        @Override
        public int getItemViewType(final int position) {
            return bindItem(mAmmeters.get(position)).getItemType();
        }

        @Override
        public void onBindViewHolder(@NonNull final ItemAmmeterViewHolder holder, final int position) {
            holder.bindTo(mLifecycleOwner, mAmmeters.get(position));
        }

        @Override
        public int getItemCount() {
            return mAmmeters.size();
        }
    }

    class ItemMainAmmeterViewHolder extends ItemAmmeterViewHolder<ItemSettlementMainBinding> {

        public ItemMainAmmeterViewHolder(final ItemSettlementMainBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            super.bindTo(lifecycleOwner, ammeter);
            mItemBinding.setViewData(bindItem(ammeter));
        }
    }

    class ItemUnSetAmmeterViewHolder extends ItemAmmeterViewHolder<ItemNotSetBinding> {

        public ItemUnSetAmmeterViewHolder(final ItemNotSetBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            super.bindTo(lifecycleOwner, ammeter);
            mItemBinding.setViewData(bindItem(ammeter));
        }
    }

    class ItemSubAmmeterViewHolder extends ItemAmmeterViewHolder<ItemSettlementSubBinding> {

        public ItemSubAmmeterViewHolder(final ItemSettlementSubBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            super.bindTo(lifecycleOwner, ammeter);
            mItemBinding.setViewData(bindItem(ammeter));
        }
    }

    private ItemAmmeter bindItem(final Ammeter input) {
        ItemAmmeter ammeter = new ItemAmmeter();
        ammeter.setItemId((int) input.getId());
        ammeter.setAmmeterId(input.getId());
        ammeter.setName(input.getName());
        ammeter.setOldAmmeter(input.getOldAmmeter());
        ammeter.setOldBalance(input.getOldBalance());
        ammeter.setNewAmmeter(input.getNewAmmeter());
        ammeter.setNewBalance(input.getNewBalance());
        ammeter.setSharing(mSharing);
        ammeter.setPrice(mPrice);
        ammeter.setTime(input.getAmmeterSetTime());
        if (!ammeter.validData()) {
            ammeter.setItemType(ItemAmmeter.NOT_SET_TYPE);
        } else if (ammeter.getAmmeterId() == Ammeter.UN_TENANT_ID) {
            ammeter.setItemType(ItemAmmeter.MAIN_TYPE);
        }
        return ammeter;
    }

    class ItemAmmeterViewHolder<B extends ViewDataBinding> extends RecyclerView.ViewHolder {
        final B mItemBinding;

        public ItemAmmeterViewHolder(B binding) {
            super(binding.getRoot());
            mItemBinding = binding;
            mItemBinding.executePendingBindings();
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            itemView.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(SettlementListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName());
            });
        }
    }
}