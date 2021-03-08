package com.cody.ammeter.viewmodel;

import android.annotation.SuppressLint;

import com.cody.ammeter.model.Repository;
import com.cody.ammeter.model.db.AmmeterDao;
import com.cody.ammeter.model.db.AmmeterDatabase;
import com.cody.component.handler.RequestStatusUtil;
import com.cody.component.handler.data.FriendlyViewData;
import com.cody.component.handler.data.ItemViewDataHolder;
import com.cody.component.handler.define.RequestStatus;
import com.cody.component.handler.viewmodel.AbsPageListViewModel;
import com.cody.component.handler.viewmodel.BaseViewModel;

import androidx.lifecycle.LifecycleOwner;
import androidx.paging.DataSource;

/**
 * 所有租户，包含已经退租的
 */
public class TenantListViewModel extends AbsPageListViewModel<FriendlyViewData, Integer> {

    public TenantListViewModel() {
        super(new FriendlyViewData());
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected DataSource.Factory<Integer, ItemViewDataHolder> createDataSourceFactory() {
        // 包含退租的租户
        return Repository.getTenantDataSource().map(input -> {
            ItemTenant tenant = new ItemTenant();
            tenant.setId(input.getId());
            tenant.setItemId((int) input.getId());
            tenant.setItemType(input.isLeave() ? ItemTenant.TYPE_LEAVE : ItemTenant.DEFAULT_TYPE);
            tenant.setName(input.getName() + (input.isLeave() ? "(已退租)" : ""));
            tenant.setArrears(input.getNewBalance() < 0f);
            tenant.setTime(input.getAmmeterSetTime().getTime());
            tenant.setNewBalance(input.getNewBalance());
            if (tenant.isArrears()) {
                tenant.setValue(String.format("已欠费：%.2f 元", Math.abs(input.getNewBalance())));
            } else {
                tenant.setValue(String.format("余额：%.2f 元", input.getNewBalance()));
            }
            return tenant;
        });
    }

    @Override
    protected boolean isInitOnce() {
        return true;
    }
}
