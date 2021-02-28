package com.cody.ammeter.model;

import android.content.Context;

import com.cody.ammeter.model.db.AmmeterDatabase;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

/**
 * 数据仓库
 */
public class Repository {
    private final static int DEFAULT_PAGE_SIZE = 20;
    private final static int DEFAULT_PREFETCH_DISTANCE = 5;

    public static void init(final Context context) {
        AmmeterDatabase.init(context);
    }
    /* settlement start */

    /**
     * 获取租户历史结算数据
     * 总表 ： ammeterId = 0
     *
     * @param ammeterId 电表id
     * @return 分页加载数据
     */
    public static LiveData<PagedList<Settlement>> liveSettlements(long ammeterId) {
        return new LivePagedListBuilder<>(AmmeterDatabase
                .getInstance()
                .getSettlementDao()
                .getDataSource(ammeterId),
                initPageListConfig())
                .build();
    }

    /**
     * 获取总表历史结算数据
     *
     * @return 分页加载数据
     */
    public static LiveData<PagedList<Settlement>> liveSettlements() {
        return liveSettlements(Ammeter.UN_TENANT_ID);
    }

    /**
     * 新增一次结算数据
     *
     * @param settlements 结算数据
     */
    public static void insertSettlement(List<Settlement> settlements) {
        AmmeterDatabase.getInstance().getSettlementDao().insert(settlements);
    }

    /**
     * 新增一次结算数据 只有主表记录
     *
     * @param settlement 结算数据
     */
    public static void insertSettlement(Settlement settlement) {
        AmmeterDatabase.getInstance().getSettlementDao().insert(settlement);
    }
    /* settlement end */
    /* ammeter start */

    /**
     * 获取租户列表
     *
     * @return 分页加载数据
     */
    public static LiveData<PagedList<Ammeter>> liveTenants() {
        return new LivePagedListBuilder<>(AmmeterDatabase
                .getInstance()
                .getAmmeterDao()
                .getTenantDataSource(false),
                initPageListConfig())
                .build();
    }

    /**
     * 获取指定租客的数据
     *
     * @return 动态监听数据变化 TODO 待验证
     */
    public static LiveData<Ammeter> liveAmmeter(long ammeterId) {
        return AmmeterDatabase.getInstance().getAmmeterDao().liveAmmeter(ammeterId);
    }

    /**
     * 获取当前所有在租租户电表
     *
     * @return 当前租户
     */
    public static List<Ammeter> getSubAmmeters() {
        return AmmeterDatabase.getInstance().getAmmeterDao().getTenantAmmeters(false);
    }

    /**
     * 获取当前所有在租租户电表
     *
     * @return 当前租户
     */
    public static long getSubAmmetersCount() {
        return AmmeterDatabase.getInstance().getAmmeterDao().tenantCount();
    }

    /**
     * 获取主表
     *
     * @return 主表
     */
    public static Ammeter getMainAmmeter() {
        Ammeter ammeter = AmmeterDatabase.getInstance().getAmmeterDao().getMainAmmeter();
        if (ammeter == null) {
            ammeter = new Ammeter();// 总表
            ammeter.setId(Ammeter.UN_TENANT_ID);
            ammeter.setName(Ammeter.UN_TENANT_NAME);
            ammeter.setCheckInTime(new Date());
            ammeter.setAmmeterSetTime(new Date());
            AmmeterDatabase.getInstance().getAmmeterDao().insert(ammeter);
        }
        return ammeter;
    }

    /**
     * 新增一个租户
     *
     * @param ammeter 租户数据
     */
    public static void insertAmmeter(Ammeter ammeter) {
        AmmeterDatabase.getInstance().getAmmeterDao().insert(ammeter);
    }

    /**
     * 更新电表数据，结算时调用
     *
     * @param ammeters 电表数据
     */
    public static void updateAmmeters(List<Ammeter> ammeters) {
        AmmeterDatabase.getInstance().getAmmeterDao().update(ammeters);
    }

    public static void updateAmmeter(final Ammeter ammeter) {
        AmmeterDatabase.getInstance().getAmmeterDao().update(ammeter);
    }
    /* ammeter end */
    /* payment start */

    /**
     * 获取租户的缴费记录
     *
     * @param ammeterId 电表id
     * @return 分页加载数据
     */
    public static LiveData<PagedList<Payment>> livePayments(long ammeterId) {
        return new LivePagedListBuilder<>(AmmeterDatabase
                .getInstance()
                .getPaymentDao()
                .getDataSource(ammeterId),
                initPageListConfig())
                .build();
    }

    /* payment end */

    /**
     * 分页数据配置
     */
    private static PagedList.Config initPageListConfig() {
        return new PagedList.Config.Builder()
                .setPrefetchDistance(DEFAULT_PREFETCH_DISTANCE)
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(DEFAULT_PAGE_SIZE)
                .setPageSize(DEFAULT_PAGE_SIZE)
                .build();
    }

    public static void insertPayment(final Payment payment) {
        AmmeterDatabase.getInstance().getPaymentDao().insert(payment);
    }

    public static void updateAmmeter(final long ammeterId, final float value) {
        Ammeter ammeter = AmmeterDatabase.getInstance().getAmmeterDao().getAmmeter(ammeterId);
        ammeter.setNewAmmeter(value);
        ammeter.setAmmeterSetTime(new Date());
        AmmeterDatabase.getInstance().getAmmeterDao().update(ammeter);
    }

    public static LiveData<List<Ammeter>> liveAmmeters() {
        return AmmeterDatabase.getInstance().getAmmeterDao().liveAmmeters(false);
    }
}
