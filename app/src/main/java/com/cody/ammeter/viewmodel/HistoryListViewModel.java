package com.cody.ammeter.viewmodel;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.cody.ammeter.model.db.AmmeterDatabase;
import com.cody.ammeter.model.db.SettlementDao;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Settlement;
import com.cody.component.handler.RequestStatusUtil;
import com.cody.component.handler.data.FriendlyViewData;
import com.cody.component.handler.data.ItemViewDataHolder;
import com.cody.component.handler.define.RequestStatus;
import com.cody.component.handler.viewmodel.AbsPageListViewModel;
import com.cody.component.handler.viewmodel.BaseViewModel;

import java.util.Date;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.paging.DataSource;
import androidx.paging.PagedList;

/**
 * 历史结算记录
 */
public class HistoryListViewModel extends AbsPageListViewModel<FriendlyViewData, Integer> {
    private SettlementDao mSettlementDao;
    private final Date mTime;

    public HistoryListViewModel(Date time) {
        super(new FriendlyViewData());
        mTime = time;
        mSettlementDao = AmmeterDatabase.getInstance().getSettlementDao();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected DataSource.Factory<Integer, ItemViewDataHolder> createDataSourceFactory() {
        mSettlementDao = AmmeterDatabase.getInstance().getSettlementDao();
        DataSource.Factory<Integer, Settlement> factory;
        if (mTime != null) {
            factory = mSettlementDao.getDataSource(mTime);
        } else {
            factory = mSettlementDao.getDataSource(Ammeter.UN_TENANT_ID);
        }
        return factory.map(input -> {
            ItemAmmeter ammeter = new ItemAmmeter();
            ammeter.setItemId((int) input.getId());
            ammeter.setItemType(input.getAmmeterId() == Ammeter.UN_TENANT_ID ? ItemAmmeter.MAIN_TYPE : ItemAmmeter.DEFAULT_TYPE);
            ammeter.setAmmeterId(input.getAmmeterId());
            ammeter.setName(input.getName());
            ammeter.setOldAmmeter(input.getOldAmmeter());
            ammeter.setOldBalance(input.getOldBalance());
            ammeter.setNewAmmeter(input.getNewAmmeter());
            ammeter.setNewBalance(input.getNewBalance());
            ammeter.setSharing(input.getSharing());
            ammeter.setPrice((input.getOldBalance() - input.getNewBalance()) / (input.getNewAmmeter() - input.getOldAmmeter()));
            ammeter.setTime(input.getTime());
            return ammeter;
        });
    }

    @Override
    protected void startOperation(final RequestStatus requestStatus) {
        super.startOperation(requestStatus);
        submitStatus(requestStatus.end());
    }
}
