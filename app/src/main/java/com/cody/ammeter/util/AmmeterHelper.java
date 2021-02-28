package com.cody.ammeter.util;

import android.os.Handler;
import android.os.Looper;

import com.cody.ammeter.model.Repository;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.lifecycle.LiveData;

/**
 * 数据计算帮助类
 * 负责线程切换
 */
public class AmmeterHelper {
    private final static ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private final static Handler sHandler = new Handler(Looper.getMainLooper());
    private static float sUnSettlementMoney = 0f;//当前未结算金额

    public static float getUnSettlementMoney() {
        return sUnSettlementMoney;
    }

    public static void setUnSettlementMoney(final float unSettlementMoney) {
        sUnSettlementMoney = unSettlementMoney;
    }

    public interface CallBack<T> {
        void onResult(T t);
    }

    public static LiveData<Ammeter> getMainAmmeter() {
        sExecutor.submit((Runnable) Repository::getMainAmmeter);
        return Repository.liveAmmeter(Ammeter.UN_TENANT_ID);
    }

    public static LiveData<Ammeter> getTenant(long tenantId) {
        return Repository.liveAmmeter(tenantId);
    }

    /**
     * 结算最新数据
     */
    public static void settlement(CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Ammeter mainAmmeter = Repository.getMainAmmeter();//总电表
            List<Ammeter> subAmmeters = Repository.getSubAmmeters();//所有在租租户分表
            Date time = new Date();
            float mainRealAmmeter = mainAmmeter.getNewAmmeter() - mainAmmeter.getOldAmmeter();// 主表电量
            float mainRealMoneyUsed = mainAmmeter.getOldBalance() - mainAmmeter.getNewBalance();// 主表消耗的金额
            float price = mainRealAmmeter <= 0 ? 0f : (mainRealMoneyUsed / mainRealAmmeter);//电价
            float sharingAmmeter = mainRealAmmeter;//公摊电量
            for (Ammeter sub : subAmmeters) {
                sharingAmmeter -= (sub.getNewAmmeter() - sub.getOldAmmeter());
            }
            List<Settlement> settlements = new ArrayList<>();
            settlements.add(createSettlement(mainAmmeter, sharingAmmeter, time));
            for (Ammeter sub : subAmmeters) {
                settlements.add(createSettlement(sub, sharingAmmeter, time));

                sub.setOldBalance(sub.getNewBalance());
                sub.setNewBalance(sub.getNewBalance() - price * (sharingAmmeter + sub.getNewAmmeter() - sub.getOldAmmeter()));
                sub.setOldAmmeter(sub.getNewAmmeter());
            }
            mainAmmeter.setOldBalance(mainAmmeter.getNewAmmeter());
            mainAmmeter.setOldBalance(mainAmmeter.getNewBalance());
            Repository.insertSettlement(settlements);

            subAmmeters.add(mainAmmeter);// 电表数据添加到一起更新
            Repository.updateAmmeters(subAmmeters);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public static void addTenant(float newAmmeter, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            long count = Repository.getSubAmmetersCount();
            Ammeter ammeter = new Ammeter();
            ammeter.setName(Ammeter.TENANT_NAME + count);
            ammeter.setCheckInTime(new Date());
            ammeter.setAmmeterSetTime(new Date());
            ammeter.setOldAmmeter(newAmmeter);
            ammeter.setNewAmmeter(newAmmeter);
            Repository.insertAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    /**
     * 修改租户信息
     */
    public static void updateTenantName(Ammeter ammeter, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    private static Settlement createSettlement(Ammeter ammeter, float sharingAmmeter, Date time) {
        Settlement settlement = new Settlement();
        settlement.setAmmeterId(ammeter.getId());
        settlement.setOldAmmeter(ammeter.getOldAmmeter());
        settlement.setNewAmmeter(ammeter.getNewAmmeter());
        settlement.setOldBalance(ammeter.getOldBalance());
        settlement.setNewBalance(ammeter.getNewBalance());
        settlement.setSharing(sharingAmmeter);
        settlement.setTime(time);
        return settlement;
    }

    public static void addPayment(Ammeter ammeter, float value, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            if (ammeter.getId() == Ammeter.UN_TENANT_ID) {
                //房东：每次缴费都加上缴费金额
                ammeter.setOldBalance(ammeter.getOldBalance() + value);
            } else {// 租客每次缴费都加上缴费金额
                ammeter.setNewBalance(ammeter.getNewBalance() + value);
            }
            Repository.updateAmmeter(ammeter);
            Payment payment = new Payment();
            payment.setAmmeterId(ammeter.getId());
            payment.setTime(new Date());
            payment.setValue(value);
            Repository.insertPayment(payment);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public static void updateAmmeter(Ammeter ammeter, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public static void checkOut(final Ammeter ammeter, CallBack<Boolean> callBack) {
        ammeter.setLeave(true);
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }
}
