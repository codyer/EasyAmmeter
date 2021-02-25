package com.cody.helper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.cody.helper.databinding.ItemAmmeterResultBinding;
import com.cody.helper.databinding.ListActivityBinding;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ResultActivity extends AppCompatActivity {

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
        final List<Ammeter> ammeters = DataHelper.getAmmeters(this);
        binding.ok.setText(R.string.save_and_clear);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setAdapter(new AmmeterAdapter(ammeters, this));
        binding.ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (DataHelper.save(ResultActivity.this, ammeters)) {
                    if (DataHelper.copy(ResultActivity.this)) {
                        DataHelper.showToast(ResultActivity.this, "保存成功，且添加到剪切板");
                    } else {
                        DataHelper.showToast(ResultActivity.this, "保存成功");
                    }
                    restartApplication();
                }
            }
        });
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
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, DataHelper.getShareInfo().toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }

    private void restartApplication() {
        final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        //杀掉以前进程
        android.os.Process.killProcess(android.os.Process.myPid());
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

    static class ItemAmmeterViewHolder extends RecyclerView.ViewHolder {
        private ItemAmmeterResultBinding mItemBinding;

        public ItemAmmeterViewHolder(ItemAmmeterResultBinding binding) {
            super(binding.getRoot());
            mItemBinding = binding;
        }

        void bindTo(final LifecycleOwner lifecycleOwner, final Ammeter ammeter) {
            mItemBinding.setLifecycleOwner(lifecycleOwner);
            mItemBinding.setAmmeter(ammeter);
        }
    }
}