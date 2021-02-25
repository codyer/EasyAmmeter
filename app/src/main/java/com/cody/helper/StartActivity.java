package com.cody.helper;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.cody.helper.databinding.StartActivityBinding;

import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StartActivityBinding binding = StartActivityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        final Ammeter ammeter = DataHelper.getAmmeters(this).get(0);
        binding.setAmmeter(ammeter);
        binding.ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (ammeter.getLastMonth().getValue() == null || TextUtils.isEmpty(ammeter.getLastMonth().getValue())) {
                    DataHelper.showToast(StartActivity.this, R.string.please_input_last_hint);
                } else if (ammeter.getThisMonth().getValue() == null || TextUtils.isEmpty(ammeter.getThisMonth().getValue())) {
                    DataHelper.showToast(StartActivity.this, R.string.please_input_this_hint);
                } else if (ammeter.getTotalPrice().getValue() == null || TextUtils.isEmpty(ammeter.getTotalPrice().getValue())) {
                    DataHelper.showToast(StartActivity.this, R.string.please_input_price_hint);
                } else {
                    if (ammeter.setKilowattHour()) {
                        Intent intent = new Intent(StartActivity.this, CalculateActivity.class);
                        startActivity(intent);
                    } else {
                        DataHelper.showToast(StartActivity.this, R.string.please_input_wrong_hint);
                    }
                }
            }
        });
    }
}