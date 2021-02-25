package com.cody.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cody.helper.databinding.ItemChooseBinding;
import com.cody.helper.databinding.ListActivityBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChooseActivity extends AppCompatActivity {
    private List<Ammeter> mAmmeters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ListActivityBinding binding = ListActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAmmeters = DataHelper.getAllAmmeters(this);
        binding.ok.setText(R.string.save);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(new AmmeterAdapter(mAmmeters, this));
        binding.ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                save();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish(); // back button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void save() {
        if (DataHelper.saveChoose(this, mAmmeters)) {
            setResult(RESULT_OK);
            finish();
        }
    }

    static class AmmeterAdapter extends RecyclerView.Adapter<ItemAmmeterViewHolder> {
        private List<Ammeter> mAmmeters;
        private final LifecycleOwner mLifecycleOwner;

        public AmmeterAdapter(final List<Ammeter> ammeters, final LifecycleOwner lifecycleOwner) {
            mAmmeters = ammeters;
            mLifecycleOwner = lifecycleOwner;
        }

        @NonNull
        @Override
        public ItemAmmeterViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
            return new ItemAmmeterViewHolder(ItemChooseBinding.inflate(LayoutInflater.from(parent.getContext())));
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
        private ItemChooseBinding mItemBinding;

        public ItemAmmeterViewHolder(ItemChooseBinding binding) {
            super(binding.getRoot());
            mItemBinding = binding;
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setAmmeter(ammeter);
        }
    }
}