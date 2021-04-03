package com.cody.ammeter.util;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.cody.ammeter.model.Repository;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;
import com.cody.ammeter.viewmodel.ItemAmmeter;
import com.cody.component.handler.data.ItemViewDataHolder;

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
    private static double sUnSettlementMoney = 0f;//当前未结算金额

    public static double getUnSettlementMoney() {
        return sUnSettlementMoney;
    }

    public static void setUnSettlementMoney(final double unSettlementMoney) {
        sUnSettlementMoney = unSettlementMoney;
    }

    /**
     * 所有在用的电表数据
     */
    public static LiveData<List<Ammeter>> getAllAmmeter() {
        return Repository.liveAmmeters();
    }

    public static LiveData<Long> liveTenantCount() {
        return Repository.liveTenantCount();
    }

    public static void updateAmmeters(final List<Ammeter> ammeters, final CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeters(ammeters);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public static void clearAll(final CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.clearAll();
            Repository.intMainAmmeter();
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public interface CallBack<T> {
        void onResult(T t);
    }

    public static LiveData<Ammeter> getMainAmmeter() {
        sExecutor.submit((Runnable) Repository::intMainAmmeter);
        return Repository.liveAmmeter(Ammeter.UN_TENANT_ID);
    }

    public static LiveData<Ammeter> getTenant(long tenantId) {
        return Repository.liveAmmeter(tenantId);
    }

    /**
     * 结算最新数据
     */
    public static void settlement(final List<Ammeter> ammeters, final double sharing, final double price, CallBack<Date> callBack) {
        sExecutor.submit(() -> {
            Date time = new Date();
            List<Settlement> settlements = new ArrayList<>();
            for (Ammeter ammeter : ammeters) {
                settlements.add(createSettlement(ammeter, sharing, price, time));
                ammeter.setOldAmmeter(ammeter.getNewAmmeter());
                ammeter.setOldBalance(ammeter.getNewBalance());
                if (ammeter.getId() != Ammeter.UN_TENANT_ID) {//分表
                    ammeter.setNewBalance(ammeter.getNewBalance() - price * (sharing + ammeter.getNewAmmeter() - ammeter.getOldAmmeter()));
                }
            }
            Repository.insertSettlement(settlements);
            Repository.updateAmmeters(ammeters);
            sHandler.post(() -> callBack.onResult(time));
        });
    }

    /**
     * 添加租户
     *
     * @param newAmmeter 租户当前电表数
     * @param callBack   回调
     */
    public static void addTenant(double newAmmeter, CallBack<Boolean> callBack) {
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
    public static void updateTenantName(Ammeter ammeter, String name, CallBack<Boolean> callBack) {
        ammeter.setName(name);
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    /**
     * 创建结算数据
     */
    private static Settlement createSettlement(Ammeter ammeter, double sharingAmmeter, double price, Date time) {
        Settlement settlement = new Settlement();
        settlement.setAmmeterId(ammeter.getId());
        settlement.setName(ammeter.getName());
        settlement.setOldAmmeter(ammeter.getOldAmmeter());
        settlement.setNewAmmeter(ammeter.getNewAmmeter());
        settlement.setOldBalance(ammeter.getOldBalance());
        settlement.setNewBalance(ammeter.getNewBalance());
        settlement.setSharing(sharingAmmeter);
        settlement.setPrice(price);
        settlement.setTime(time);
        return settlement;
    }

    /**
     * 充值金额
     */
    public static void addPayment(Ammeter ammeter, double value, CallBack<Boolean> callBack) {
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

    /**
     * 充值金额
     */
    public static void addPayment(long ammeterId, double value, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Ammeter ammeter = Repository.getSubAmmeter(ammeterId);
            if (ammeterId == Ammeter.UN_TENANT_ID) {
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

    /**
     * 更新电表信息
     */
    public static void initMainAmmeter(Ammeter ammeter, CallBack<Boolean> callBack) {
        ammeter.setOldAmmeter(ammeter.getNewAmmeter());
        ammeter.setOldBalance(ammeter.getNewBalance());
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    /**
     * 更新电表信息
     */
    public static void updateAmmeter(Ammeter ammeter, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    /**
     * 更新电表数据
     */
    public static void updateAmmeter(long ammeterId, double value, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeterId, value);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    /**
     * 退租
     */
    public static void checkOut(final Ammeter ammeter, CallBack<Boolean> callBack) {
        ammeter.setLeave(true);
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeter);
            sHandler.post(() -> callBack.onResult(true));
        });
    }

    public static boolean copy(Context context, List<ItemViewDataHolder> itemAmmeters) {
        if (context == null || itemAmmeters == null || itemAmmeters.size() == 0) return false;
        StringBuffer info = getShareInfo(context, itemAmmeters);
        return copy(context, info);
    }

    public static boolean copy(Context context, List<Ammeter> ammeters, final double sharing, final double price) {
        if (context == null || ammeters == null) return false;
        StringBuffer info = getShareInfo(ammeters, sharing, price);
        return copy(context, info);
    }

    private static boolean copy(final Context context, final StringBuffer info) {
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(ClipData.newPlainText("ammeter_info", info));
                return true;
            } catch (Exception e) {
                //
                return false;
            }
        }
        return false;
    }

    public static StringBuffer getShareInfo(Context context, List<ItemViewDataHolder> itemAmmeters) {
        if (context == null || itemAmmeters == null || itemAmmeters.size() == 0)
            return new StringBuffer();
        List<Ammeter> ammeters = new ArrayList<>();
        Ammeter item;
        ItemAmmeter itemAmmeter = null;
        for (int i = 0; i < itemAmmeters.size(); i++) {
            item = new Ammeter();
            itemAmmeter = (ItemAmmeter) itemAmmeters.get(i);
            item.setId(itemAmmeter.getAmmeterId());
            item.setName(itemAmmeter.getName());
            item.setOldAmmeter(itemAmmeter.getOldAmmeter());
            item.setOldBalance(itemAmmeter.getOldBalance());
            item.setCheckInTime(itemAmmeter.getTime());
            item.setAmmeterSetTime(itemAmmeter.getTime());
            item.setLeave(false);
            item.setNewAmmeter(itemAmmeter.getNewAmmeter());
            item.setNewBalance(itemAmmeter.getNewBalance());
            ammeters.add(item);
        }
        return getShareInfo(ammeters, itemAmmeter.getSharing(), itemAmmeter.getPrice());
    }

    @SuppressLint("DefaultLocale")
    public static StringBuffer getShareInfo(List<Ammeter> ammeters, final double sharing, final double price) {
        StringBuffer info = new StringBuffer();
        info.append("本次电费详情:(时间：").append(TimeUtil.getTimeString()).append(")\n");
        Ammeter ammeter;
        for (int i = 0; i < ammeters.size(); i++) {
            ammeter = ammeters.get(i);
            if (ammeter.getId() > Ammeter.UN_TENANT_ID) {
                info.append("【").append(ammeter.getName())
                        .append(String.format("】本次应缴电费：%.2f 元", (price * (sharing + ammeter.getNewAmmeter() - ammeter.getOldAmmeter()))))
                        .append(ammeter.getNewBalance() >= 0 ? "，之前余额：" : "，之前欠费：")
                        .append(String.format("%.2f 元", Math.abs(ammeter.getNewBalance())))
                        .append(String.format("，本次剩余应缴电费：%.2f 元", (price * (sharing + ammeter.getNewAmmeter() - ammeter.getOldAmmeter()) - ammeter.getNewBalance())))
                        .append(String.format("\n（公摊电费：%.2f 元，", price * sharing))
                        .append(String.format("分表电费：%.2f 元，", price * (ammeter.getNewAmmeter() - ammeter.getOldAmmeter())))
                        .append(String.format("用电：%.2f 度，",ammeter.getNewAmmeter() - ammeter.getOldAmmeter()))
                        .append(String.format("本次读数：%.2f 度)；\n ------------ \n",ammeter.getNewAmmeter()));
            } else {
                info.append(String.format("总缴电费：%.2f 元\n", ammeter.getOldBalance() - ammeter.getNewBalance()))
                        .append(String.format("（总用电：%.2f 度，",ammeter.getNewAmmeter() - ammeter.getOldAmmeter()))
                        .append(String.format(" 公摊度数：%.2f 度，", sharing))
                        .append(String.format("上月总表读数：%.2f 度，",ammeter.getOldAmmeter()))
                        .append(String.format("本月总表读数：%.2f 度)；\n ------------ \n",ammeter.getNewAmmeter()));
            }
        }
        return info;
    }
}
