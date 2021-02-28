package com.cody.ammeter.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.cody.ammeter.model.Repository;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    /**
     * 所有在用的电表数据
     */
    public static LiveData<List<Ammeter>> getAllAmmeter() {
        return Repository.liveAmmeters();
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
            mainAmmeter.setOldAmmeter(mainAmmeter.getNewAmmeter());
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
    public static void updateTenantName(Ammeter ammeter, String name, CallBack<Boolean> callBack) {
        ammeter.setName(name);
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

    public static void updateAmmeter(long ammeterId, float value, CallBack<Boolean> callBack) {
        sExecutor.submit(() -> {
            Repository.updateAmmeter(ammeterId, value);
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


    public static boolean copy(Context context) {
        if (context == null) return false;
        StringBuffer info = getShareInfo();
        ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(ClipData.newPlainText("ammeter_info", info));
                Log.d("DataHelper", info.toString());
                return true;
            } catch (Exception e) {
                //
                return false;
            }
        }
        return false;
    }

  /*  private void share() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, DataHelper.getShareInfo().toString());
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent, null));
    }
*/
    public static StringBuffer getShareInfo() {
        StringBuffer info = new StringBuffer();
        info.append("本次电费详情:(时间：").append(getDateString()).append(")\n");
        /*AmmeterViewData item;
        for (int i = 0; i < mAmmeters.size(); i++) {
            item = mAmmeters.get(i);
            if (item.isSubMeter()) {
                info.append("房间【").append(item.getName())
                        .append("】应缴电费：").append(item.getTotalPrice().getValue()).append("元\n（公摊电费：")
                        .append(item.getPublicPrice().getValue()).append("元，分表电费：")
                        .append(item.getPrivatePrice().getValue()).append("元，用电：")
                        .append(item.getKilowattHour()).append("度，当前读数：")
                        .append(item.getThisMonth().getValue()).append("度)；\n ------------ \n");
            } else {
                info.append("总缴电费：").append(item.getTotalPrice().getValue()).append("元\n（总用电：")
                        .append(item.getKilowattHour()).append("度").append("，公摊度数：")
                        .append(item.getPublicPrice().getValue()).append("度，上月总表读数：")
                        .append(item.getLastMonth().getValue()).append("度，本月总表读数：")
                        .append(item.getThisMonth().getValue()).append("度)；\n ------------ \n");
            }
        }*/
        return info;
    }

    public static String getDateString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/M/d HH:mm:ss", Locale.CHINA);
        return df.format(new Date(System.currentTimeMillis()));
    }
}
