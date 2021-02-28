package com.cody.ammeter.viewmodel;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;

import com.cody.ammeter.model.db.AmmeterDao;
import com.cody.ammeter.model.db.AmmeterDatabase;
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
 * 所有在住租户结算列表
 */
public class SettlementListViewModel extends AbsPageListViewModel<FriendlyViewData, Integer> {
    private AmmeterDao mAmmeterDao;

    public SettlementListViewModel() {
        super(new FriendlyViewData());
        mAmmeterDao = AmmeterDatabase
                .getInstance()
                .getAmmeterDao();
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected DataSource.Factory<Integer, ItemViewDataHolder> createDataSourceFactory() {
        mAmmeterDao = AmmeterDatabase
                .getInstance()
                .getAmmeterDao();
        return mAmmeterDao.getTenantDataSource(false).map(input -> {
            ItemTenant tenant = new ItemTenant();
            tenant.setId(input.getId());
            tenant.setItemId((int) input.getId());
            tenant.setItemType(ItemTenant.DEFAULT_TYPE);
            tenant.setName(input.getName());
            tenant.setArrears(input.getNewAmmeter() < input.getOldAmmeter());
            tenant.setTime(input.getAmmeterSetTime().getTime());
            if (tenant.isArrears()) {
                tenant.setValue("最新电量未设定\n或者设定错误");
            } else {
                tenant.setValue(String.format("已使用：%.2f度\n%s", input.getNewAmmeter() - input.getOldAmmeter(),
                        TimeUtil.getDateFormatShort(input.getAmmeterSetTime())));
            }
            return tenant;
        });
    }

    @Override
    public <T extends BaseViewModel> T setLifecycleOwner(final LifecycleOwner lifecycleOwner) {
        if (mLifecycleOwner == null && lifecycleOwner != null) {
            mAmmeterDao.liveTenantCount().observe(lifecycleOwner, count -> submitStatus(count > 0 ? getRequestStatus().end() : getRequestStatus().empty()));
        }
        return super.setLifecycleOwner(lifecycleOwner);
    }

    @Override
    protected void startOperation(final RequestStatus requestStatus) {
        super.startOperation(requestStatus);
        new Handler(Looper.getMainLooper()).postDelayed(() -> submitStatus(RequestStatusUtil.getRequestStatus(requestStatus, getPagedList().getValue())), 500);
    }
}
