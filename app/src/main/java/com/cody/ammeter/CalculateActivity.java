package com.cody.ammeter;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;


import com.cody.ammeter.databinding.ItemAmmeterBinding;
import com.cody.ammeter.databinding.ListActivityBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class CalculateActivity extends AppCompatActivity {
    private static final int CODE = 0x008;
    private ListActivityBinding mBinding;
    private List<AmmeterViewData> mAmmeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mBinding = ListActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mAmmeters = DataHelper.getAmmeters(this);
        mBinding.ok.setText(R.string.calculate);
        mBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mBinding.recyclerView.setAdapter(new AmmeterAdapter(mAmmeters, this));
        mBinding.ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (calculate()) {
                    Intent intent = new Intent(CalculateActivity.this, ResultActivity.class);
                    startActivity(intent);
                } else {
                    DataHelper.showToast(CalculateActivity.this, R.string.please_input_ammeter_hint);
                }
            }
        });
        if (mAmmeters.size() < 2) {
            addAmmeter();
        }
        checkItem();
        DataHelper.getLiveData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean change) {
                checkItem();
            }
        });
    }

    private void checkItem() {
        AmmeterViewData item = mAmmeters.get(0);
        float temp = item.getKilowattHour();
        for (int i = 1; i < mAmmeters.size(); i++) {
            temp -= mAmmeters.get(i).getKilowattHour();
        }
        item.getPublicPrice().setValue(temp + "");
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
                addAmmeter();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE && resultCode == RESULT_OK) {
            mAmmeters = DataHelper.getAmmeters(this);
            mBinding.recyclerView.setAdapter(new AmmeterAdapter(mAmmeters, this));
        }
    }

    private void addAmmeter() {
        Intent intent = new Intent(CalculateActivity.this, ChooseActivity.class);
        startActivityForResult(intent, CODE);
    }

    private boolean calculate() {
        AmmeterViewData total = mAmmeters.get(0);
        float publicUse = total.getKilowattHour();
        if (total.getTotalPrice().getValue() == null || TextUtils.isEmpty(total.getTotalPrice().getValue())) {
            DataHelper.showToast(this, R.string.please_input_payment_hint);
            return false;
        }
        float averagePricePerDegree = Float.parseFloat(total.getTotalPrice().getValue()) / total.getKilowattHour();
        int count = 0;
        AmmeterViewData item;

        for (int i = 1; i < mAmmeters.size(); i++) {
            item = mAmmeters.get(i);
            if (item.isSubMeter()) {
                if (item.getThisMonth().getValue() == null || TextUtils.isEmpty(item.getThisMonth().getValue())) {
                    DataHelper.showToast(this, "请输入【" + item.getName() + "】的电表数据");
                    return false;
                }
                publicUse -= item.getKilowattHour();
                count++;
            }
        }
        if (count <= 0) {
            return false;
        }
        float average = publicUse / count;
        float privatePrice;
        float publicPrice;
        for (int i = 1; i < mAmmeters.size(); i++) {
            item = mAmmeters.get(i);
            if (item.isSubMeter()) {
                privatePrice = averagePricePerDegree * item.getKilowattHour();
                publicPrice = averagePricePerDegree * average;
                item.getPrivatePrice().setValue(String.valueOf(privatePrice));
                item.getPublicPrice().setValue(String.valueOf(publicPrice));
                item.getTotalPrice().setValue(String.valueOf(privatePrice + publicPrice));
            }
        }
        return true;
    }

    static class AmmeterAdapter extends RecyclerView.Adapter<ItemAmmeterViewHolder> {
        private List<AmmeterViewData> mAmmeters;
        private final LifecycleOwner mLifecycleOwner;

        public AmmeterAdapter(final List<AmmeterViewData> ammeters, final LifecycleOwner lifecycleOwner) {
            mAmmeters = ammeters;
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public ItemAmmeterViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            return new ItemAmmeterViewHolder(ItemAmmeterBinding.inflate(LayoutInflater.from(parent.getContext())));
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

    static class ItemAmmeterViewHolder extends RecyclerView.ViewHolder {
        private ItemAmmeterBinding mItemBinding;

        public ItemAmmeterViewHolder(ItemAmmeterBinding binding) {
            super(binding.getRoot());
            mItemBinding = binding;
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final AmmeterViewData ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setAmmeter(ammeter);
            ammeter.getThisMonth().observe(lifecycleOwner, new Observer<String>() {
                @Override
                public void onChanged(final String thisMonth) {
                    ammeter.setKilowattHour();
                    DataHelper.getLiveData().setValue(DataHelper.getLiveData().getValue() == null || !DataHelper.getLiveData().getValue());
                    mItemBinding.kilowattHour.setText(String.format("%s 度", ammeter.getKilowattHour()));
                }
            });
        }
    }
}