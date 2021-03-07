package com.cody.ammeter.viewmodel;

import android.annotation.SuppressLint;

import com.cody.ammeter.model.Repository;
import com.cody.ammeter.util.TimeUtil;
import com.cody.component.handler.data.FriendlyViewData;
import com.cody.component.handler.data.ItemViewDataHolder;
import com.cody.component.handler.viewmodel.AbsPageListViewModel;

import androidx.paging.DataSource;

/**
 * 缴费记录
 */
public class PaymentListViewModel extends AbsPageListViewModel<FriendlyViewData, Integer> {
    private final long mAmmeterId;

    public PaymentListViewModel(long ammeterId) {
        super(new FriendlyViewData());
        mAmmeterId = ammeterId;
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected DataSource.Factory<Integer, ItemViewDataHolder> createDataSourceFactory() {
        return Repository.getPaymentDataSource(mAmmeterId).map(input -> {
            ItemPayment payment = new ItemPayment();
            payment.setId(input.getId());
            payment.setItemId((int) input.getId());
            payment.setItemType(ItemPayment.DEFAULT_TYPE);
            payment.setValue(input.getValue() + "元");
            payment.setTime(TimeUtil.getLongTimeString(input.getTime()));
            return payment;
        });
    }

    @Override
    protected boolean isInitOnce() {
        return true;
    }
}
