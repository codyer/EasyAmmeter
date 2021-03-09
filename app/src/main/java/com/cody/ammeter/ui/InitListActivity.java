package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ItemInitMainBinding;
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
 * 修改数据列表
 */
public class InitListActivity extends BaseActionbarActivity<SettlementLitActivityBinding> {
    private Ammeter mClickedAmmeter;
    private List<Ammeter> mAmmeterList;
    private LiveData<List<Ammeter>> mListLiveData;

    public static void start(Activity activity) {
        activity.startActivity(new Intent(activity, InitListActivity.class));
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
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mListLiveData = AmmeterHelper.getAllAmmeter();
        mListLiveData.observe(this, ammeters -> {
            mAmmeterList = ammeters;
            getBinding().recyclerView.setAdapter(new AmmeterAdapter(mAmmeterList, InitListActivity.this));
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        double value = InputActivity.getActivityResult(resultCode, data);
        if (value < 0) return;
        if (mClickedAmmeter != null) {
            if (requestCode == InputActivity.INPUT_TYPE_BALANCE) {
                mClickedAmmeter.setNewBalance(value);
                mClickedAmmeter.setOldBalance(value);
                mClickedAmmeter.setCheckInTime(new Date());
            } else if (requestCode == InputActivity.INPUT_TYPE_AMMETER) {
                mClickedAmmeter.setNewAmmeter(value);
                mClickedAmmeter.setOldAmmeter(value);
                mClickedAmmeter.setAmmeterSetTime(new Date());
            }
            getBinding().recyclerView.setAdapter(new AmmeterAdapter(mAmmeterList, InitListActivity.this));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.top) {
            RecyclerViewUtil.smoothScrollToTop(getBinding().recyclerView);
        } else if (v.getId() == R.id.settlement) {
            mListLiveData.removeObservers(this);
            showLoading();
            AmmeterHelper.updateAmmeters(mAmmeterList, result -> {
                hideLoading();
                showToast("数据初始化完成！");
                finish();
            });
        }
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
            return new ItemInitAmmeterViewHolder(ItemInitMainBinding.inflate(LayoutInflater.from(parent.getContext())));
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

    class ItemInitAmmeterViewHolder extends ItemAmmeterViewHolder<ItemInitMainBinding> {

        public ItemInitAmmeterViewHolder(final ItemInitMainBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            super.bindTo(lifecycleOwner, ammeter);
            mItemBinding.setViewData(bindItem(ammeter));
            // 设置当前余额
            mItemBinding.thisBalance.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(InitListActivity.this, InputActivity.INPUT_TYPE_BALANCE, ammeter.getName());
            });
            // 设置当前电表
            mItemBinding.thisAmmeter.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(InitListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName());
            });
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
        ammeter.setTime(input.getAmmeterSetTime());
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
                InputActivity.start(InitListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName());
            });
        }
    }
}