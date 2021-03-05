package com.cody.ammeter.viewmodel;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.cody.ammeter.model.db.AmmeterDatabase;
import com.cody.ammeter.model.db.PaymentDao;
import com.cody.ammeter.util.TimeUtil;
import com.cody.component.handler.RequestStatusUtil;
import com.cody.component.handler.data.FriendlyViewData;
import com.cody.component.handler.data.ItemViewDataHolder;
import com.cody.component.handler.define.RequestStatus;
import com.cody.component.handler.viewmodel.AbsPageListViewModel;
import com.cody.component.handler.viewmodel.BaseViewModel;

import androidx.lifecycle.LifecycleOwner;
import androidx.paging.DataSource;

/**
 * 缴费记录
 */
public class PaymentListViewModel extends AbsPageListViewModel<FriendlyViewData, Integer> {
    private PaymentDao mPaymentDao;
    private final long mAmmeterId;

    public PaymentListViewModel(long ammeterId) {
        super(new FriendlyViewData());
        mAmmeterId = ammeterId;
        mPaymentDao = AmmeterDatabase.getInstance().getPaymentDao();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected DataSource.Factory<Integer, ItemViewDataHolder> createDataSourceFactory() {
        mPaymentDao = AmmeterDatabase.getInstance().getPaymentDao();
        return mPaymentDao.getDataSource(mAmmeterId).map(input -> {
            ItemPayment payment = new ItemPayment();
            payment.setId(input.getId());
            payment.setItemId((int) input.getId());
            payment.setItemType(ItemPayment.DEFAULT_TYPE);
            payment.setValue(input.getValue() + "元");
            payment.setTime(TimeUtil.getDateFormatLong(input.getTime()));
            return payment;
        });
    }

    @Override
    protected void startOperation(final RequestStatus requestStatus) {
        super.startOperation(requestStatus);
        submitStatus(requestStatus.end());
    }
}
