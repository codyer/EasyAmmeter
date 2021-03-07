package com.cody.ammeter.model;

import android.content.Context;

import com.cody.ammeter.model.db.AmmeterDatabase;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;

/**
 * 数据仓库
 */
public class Repository {

    public static void init(final Context context) {
        AmmeterDatabase.init(context);
    }
    /* settlement start */

    /**
     * 新增一次结算数据
     *
     * @param settlements 结算数据
     */
    public static void insertSettlement(List<Settlement> settlements) {
        AmmeterDatabase.getInstance().getSettlementDao().insert(settlements);
    }

    public static DataSource.Factory<Integer, Settlement> getSettlementDataSource(final Date time) {
        return AmmeterDatabase.getInstance().getSettlementDao().getDataSource(time);
    }

    public static DataSource.Factory<Integer, Settlement> getSettlementDataSource(final long tenantId) {
        return AmmeterDatabase.getInstance().getSettlementDao().getDataSource(tenantId);
    }

    /* settlement end */
    /* ammeter start */

    /**
     * 在租用户数
     */
    public static LiveData<Long> liveTenantCount() {
        return AmmeterDatabase.getInstance().getAmmeterDao().liveTenantCount(false);
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
    public static long getSubAmmetersCount() {
        return AmmeterDatabase.getInstance().getAmmeterDao().tenantCount();
    }

    /**
     * 初始化主表
     */
    public static void intMainAmmeter() {
        Ammeter ammeter = AmmeterDatabase.getInstance().getAmmeterDao().getMainAmmeter();
        if (ammeter == null) {
            ammeter = new Ammeter();// 总表
            ammeter.setId(Ammeter.UN_TENANT_ID);
            ammeter.setName(Ammeter.UN_TENANT_NAME);
            ammeter.setCheckInTime(new Date(0L));
            ammeter.setAmmeterSetTime(new Date(0L));
            AmmeterDatabase.getInstance().getAmmeterDao().insert(ammeter);
        }
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

    public static void updateAmmeter(final long ammeterId, final double value) {
        Ammeter ammeter = AmmeterDatabase.getInstance().getAmmeterDao().getAmmeter(ammeterId);
        ammeter.setNewAmmeter(value);
        ammeter.setAmmeterSetTime(new Date());
        AmmeterDatabase.getInstance().getAmmeterDao().update(ammeter);
    }

    public static LiveData<List<Ammeter>> liveAmmeters() {
        return AmmeterDatabase.getInstance().getAmmeterDao().liveAmmeters(false);
    }

    public static Ammeter getSubAmmeter(final long ammeterId) {
        return AmmeterDatabase.getInstance().getAmmeterDao().getAmmeter(ammeterId);
    }

    public static DataSource.Factory<Integer, Ammeter> getTenantDataSource() {
        return AmmeterDatabase.getInstance().getAmmeterDao().getTenantDataSource();
    }
    /* ammeter end */
    /* payment start */

    public static void insertPayment(final Payment payment) {
        AmmeterDatabase.getInstance().getPaymentDao().insert(payment);
    }

    public static DataSource.Factory<Integer, Payment> getPaymentDataSource(final long ammeterId) {
        return AmmeterDatabase.getInstance().getPaymentDao().getDataSource(ammeterId);
    }

    /* payment end */
}
