package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ItemAmmeterResultBinding;
import com.cody.ammeter.databinding.SettlementLitActivityBinding;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.util.AmmeterHelper;
import com.cody.component.app.activity.BaseActionbarActivity;
import com.cody.component.util.RecyclerViewUtil;

import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 结算列表
 */
public class SettlementListActivity extends BaseActionbarActivity<SettlementLitActivityBinding> {
    private Ammeter mClickedAmmeter;
    private float mSharing;
    private float mPrice;

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
    protected int getLayoutID() {
        return R.layout.settlement_lit_activity;
    }

    @Override
    protected void onBaseReady(Bundle savedInstanceState) {
        super.onBaseReady(savedInstanceState);
        setSupportActionBar(getBinding().toolbar);
        getBinding().recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        AmmeterHelper.getAllAmmeter().observe(this, ammeters -> {
            if (calculate(ammeters)) {
                getBinding().recyclerView.setAdapter(new AmmeterAdapter(ammeters, SettlementListActivity.this));
            }
        });
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        float value = InputActivity.getActivityResult(resultCode, data);
        if (value < 0) return;
        if (mClickedAmmeter != null) {
            if (requestCode == InputActivity.INPUT_TYPE_AMMETER) {
                showLoading();
                AmmeterHelper.updateAmmeter(mClickedAmmeter.getId(), value, result -> {
                    hideLoading();
                    showToast(mClickedAmmeter.getName() + String.format(getString(R.string.success_set_ammeter), value));
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
            showLoading();
            AmmeterHelper.settlement(result -> {
                hideLoading();
                showToast(R.string.settlement_success);
            });
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
        if (total == null) return false;

        float publicUse = total.getNewAmmeter() - total.getOldAmmeter();
        mPrice = total.getOldBalance() - total.getNewBalance() / publicUse;
        int count = 0;
        Ammeter item;
        long time = new Date().getTime();
        for (int i = 0; i < ammeters.size(); i++) {
            item = ammeters.get(i);
            if (item.getId() != Ammeter.UN_TENANT_ID) {
                if (item.getNewAmmeter() < 0f || item.getNewAmmeter() < item.getOldAmmeter()) {
                    showToast(item.getName() + getString(R.string.please_input_wrong_hint));
                    return false;
                }if ((time - item.getAmmeterSetTime().getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
                    // 数据超过一天无效
                    showToast(String.format(getString(R.string.time_long), item.getName()));
                    return false;
                }
                publicUse -= (item.getNewAmmeter() - item.getOldAmmeter());
                count++;
            }
        }
        if (count <= 0) {
            return false;
        }
        mSharing = publicUse / count;
        return true;
    }

    class AmmeterAdapter extends RecyclerView.Adapter<ItemAmmeterViewHolder> {
        private final List<Ammeter> mAmmeters;
        private final LifecycleOwner mLifecycleOwner;

        public AmmeterAdapter(final List<Ammeter> ammeters, final LifecycleOwner lifecycleOwner) {
            mAmmeters = ammeters;
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public ItemAmmeterViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            return new ItemAmmeterViewHolder(ItemAmmeterResultBinding.inflate(LayoutInflater.from(parent.getContext())));
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

    class ItemAmmeterViewHolder extends RecyclerView.ViewHolder {
        private final ItemAmmeterResultBinding mItemBinding;

        public ItemAmmeterViewHolder(ItemAmmeterResultBinding binding) {
            super(binding.getRoot());
            mItemBinding = binding;
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setAmmeter(ammeter);
            mItemBinding.setPrice(mPrice);
            mItemBinding.setSharing(mSharing);
            itemView.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(SettlementListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName());
            });
        }
    }
}