package com.cody.ammeter.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cody.ammeter.R;
import com.cody.ammeter.databinding.ItemMainAmmeterResultBinding;
import com.cody.ammeter.databinding.ItemSubAmmeterResultBinding;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 结算列表
 */
public class SettlementListActivity extends BaseActionbarActivity<SettlementLitActivityBinding> {
    private Ammeter mClickedAmmeter;
    private List<Ammeter> mAmmeterList;
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
                getBinding().setValid(true);
               if (AmmeterHelper.copy(this, ammeters, mSharing, mPrice)){
                   showToast("最新结算数据已经复制到剪切板！");
               }
            } else {
                getBinding().setValid(false);
            }
            mAmmeterList = ammeters;
            getBinding().recyclerView.setAdapter(new AmmeterAdapter(ammeters, SettlementListActivity.this));
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
            AmmeterHelper.settlement(mAmmeterList, mSharing, mPrice, result -> {
                hideLoading();
                showToast(R.string.settlement_success);
                finish();
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return super.onCreateOptionsMenu(menu);
    }

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
        if (getBinding().getValid()) {
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

        float publicUse = total.getNewAmmeter() - total.getOldAmmeter();
        mPrice = (total.getOldBalance() - total.getNewBalance()) / publicUse;
        int count = 0;
        Ammeter item;
        long time = new Date().getTime();
        for (int i = 0; i < ammeters.size(); i++) {
            item = ammeters.get(i);
            if (item.getId() != Ammeter.UN_TENANT_ID) {
                if (item.getNewAmmeter() < 0f || item.getNewAmmeter() < item.getOldAmmeter()) {
                    showToast(item.getName() + getString(R.string.please_input_wrong_hint));
                    return false;
                }
                if ((time - item.getAmmeterSetTime().getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
                    // 数据超过一天无效
                    showToast(String.format(getString(R.string.time_long), item.getName()));
                    return false;
                }
                publicUse -= (item.getNewAmmeter() - item.getOldAmmeter());
                count++;
            }
        }
        if (count <= 0) {
            new AlertDialog.Builder(this).setMessage(R.string.add_tenant_hint)
                    .setPositiveButton(R.string.ui_str_confirm, (dialog, which) -> {
                        finish();
                    }).setNegativeButton(R.string.ui_str_cancel, null)
                    .create().show();
            return false;
        }
        mSharing = publicUse / count;
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
                return new ItemMainAmmeterViewHolder(ItemMainAmmeterResultBinding.inflate(LayoutInflater.from(parent.getContext())));
            } else {
                return new ItemSubAmmeterViewHolder(ItemSubAmmeterResultBinding.inflate(LayoutInflater.from(parent.getContext())));
            }
        }

        @Override
        public int getItemViewType(final int position) {
            return mAmmeters.get(position).getId() == Ammeter.UN_TENANT_ID ? ItemAmmeter.MAIN_TYPE : ItemAmmeter.DEFAULT_TYPE;
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

    class ItemMainAmmeterViewHolder extends ItemAmmeterViewHolder<ItemMainAmmeterResultBinding> {

        public ItemMainAmmeterViewHolder(final ItemMainAmmeterResultBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setViewData(bindItem(ammeter));
            itemView.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(SettlementListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName(), ammeter.getOldAmmeter());
            });
        }
    }

    class ItemSubAmmeterViewHolder extends ItemAmmeterViewHolder<ItemSubAmmeterResultBinding> {

        public ItemSubAmmeterViewHolder(final ItemSubAmmeterResultBinding binding) {
            super(binding);
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setViewData(bindItem(ammeter));
            itemView.setOnClickListener(v -> {
                mClickedAmmeter = ammeter;
                InputActivity.start(SettlementListActivity.this, InputActivity.INPUT_TYPE_AMMETER, ammeter.getName(), ammeter.getOldAmmeter());
            });
        }
    }

    private ItemAmmeter bindItem(final Ammeter input) {
        ItemAmmeter ammeter = new ItemAmmeter();
        ammeter.setItemId((int) input.getId());
        ammeter.setItemType(input.getId() == Ammeter.UN_TENANT_ID ? ItemAmmeter.MAIN_TYPE : ItemAmmeter.DEFAULT_TYPE);
        ammeter.setAmmeterId(input.getId());
        ammeter.setName(input.getName());
        ammeter.setOldAmmeter(input.getOldAmmeter());
        ammeter.setOldBalance(input.getOldBalance());
        ammeter.setNewAmmeter(input.getNewAmmeter());
        ammeter.setNewBalance(input.getNewBalance());
        ammeter.setSharing(mSharing);
        ammeter.setPrice(mPrice);
        ammeter.setTime(input.getAmmeterSetTime());
        return ammeter;
    }

    static class ItemAmmeterViewHolder<B extends ViewDataBinding> extends RecyclerView.ViewHolder {
        final B mItemBinding;

        public ItemAmmeterViewHolder(B binding) {
            super(binding.getRoot());
            mItemBinding = binding;
            mItemBinding.executePendingBindings();
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
        }
    }
}