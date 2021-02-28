package com.cody.ammeter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.lifecycle.MutableLiveData;

public class DataHelper {
    public static final String[] NAME = new String[]{"总表",
            "春", "夏", "秋", "冬",
            "梅", "兰", "竹", "菊", "棠",
            "仁", "义", "礼", "智", "信",
            "白羊", "金牛", "双子", "巨蟹", "狮子", "处女", "天秤", "天蝎", "射手", "摩羯", "水瓶", "双鱼"
    };
    public static final String DEFAULT_VALUE = "";
    private static final String SHARED_PREFERENCES = "helper";
    private static final String AMMETER_IS_USED = "ammeter_is_used_";
    private static final String AMMETER_NAME = "ammeter_name_";
    private static final String AMMETER_THIS_MONTH = "ammeter_this_month_";
    private static final String AMMETER_LAST_MONTH = "ammeter_last_month_";
    private static final String AMMETER_PUBLIC_PRICE = "ammeter_public_price_";
    private static final String AMMETER_PRIVATE_PRICE = "ammeter_private_price_";
    private static final String AMMETER_TOTAL_PRICE = "ammeter_total_price_";
    private static List<AmmeterViewData> mAmmeters;
    private static final MutableLiveData<Boolean> sLiveData = new MutableLiveData<>(false);

    public static MutableLiveData<Boolean> getLiveData() {
        return sLiveData;
    }

    public static List<AmmeterViewData> getAllAmmeters(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        List<AmmeterViewData> ammeters = new ArrayList<>();
        AmmeterViewData item;
        for (int i = 1; i < NAME.length; i++) {
            item = new AmmeterViewData();
            item.setSubMeter(true);
            item.getIsUsed().setValue(preferences.getBoolean(AMMETER_IS_USED + i, false));
            item.setName(preferences.getString(AMMETER_NAME + i, NAME[i]));
            item.getLastMonth().setValue(preferences.getString(AMMETER_THIS_MONTH + i, "0"));
            item.getThisMonth().setValue(DEFAULT_VALUE);
            item.getPublicPrice().setValue(DEFAULT_VALUE);
            item.getPrivatePrice().setValue(DEFAULT_VALUE);
            item.getTotalPrice().setValue(DEFAULT_VALUE);
            ammeters.add(item);
        }
        return ammeters;
    }

    public static List<AmmeterViewData> getAmmeters(Context context) {
        if (mAmmeters == null) {
            mAmmeters = initData(context);
        }
        return mAmmeters;
    }

    public static boolean save(Context context, List<AmmeterViewData> ammeters) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        AmmeterViewData item;
        for (int i = 0; i < ammeters.size(); i++) {
            item = ammeters.get(i);
            if (item.getIsUsed().getValue() != null && item.getIsUsed().getValue()) {
                editor.putBoolean(AMMETER_IS_USED + i, true);
                editor.putString(AMMETER_NAME + i, item.getName());
                editor.putString(AMMETER_THIS_MONTH + i, item.getThisMonth().getValue());
                editor.putString(AMMETER_LAST_MONTH + i, item.getLastMonth().getValue());
                editor.putString(AMMETER_PUBLIC_PRICE + i, item.getPublicPrice().getValue());
                editor.putString(AMMETER_PRIVATE_PRICE + i, item.getPrivatePrice().getValue());
                editor.putString(AMMETER_TOTAL_PRICE + i, item.getTotalPrice().getValue());
            }
        }
        editor.apply();
        mAmmeters = ammeters;
        return true;
    }

    public static boolean saveChoose(Context context, List<AmmeterViewData> ammeters) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        @SuppressLint("CommitPrefEdits")
        SharedPreferences.Editor editor = preferences.edit();
        AmmeterViewData item;
        for (int i = 1; i < NAME.length; i++) {
            item = ammeters.get(i - 1);
            editor.putBoolean(AMMETER_IS_USED + i, item.getIsUsed().getValue() != null && item.getIsUsed().getValue());
        }
        editor.apply();
        item = mAmmeters.get(0);
        mAmmeters = initData(context);
        mAmmeters.set(0, item);
        return true;
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

    public static StringBuffer getShareInfo() {
        StringBuffer info = new StringBuffer();
        info.append("本次电费详情:(时间：").append(getDateString()).append(")\n");
        AmmeterViewData item;
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
        }
        return info;
    }

    public static String getDateString() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy/M/d HH:mm:ss", Locale.CHINA);
        return df.format(new Date(System.currentTimeMillis()));
    }

    public static void showToast(Context context, String toast) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Context context, int toast) {
        Toast.makeText(context, toast, Toast.LENGTH_LONG).show();
    }

    private static List<AmmeterViewData> initData(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES, Context.MODE_PRIVATE);
        List<AmmeterViewData> ammeters = new ArrayList<>();
        AmmeterViewData item;
        for (int i = 0; i < NAME.length; i++) {
            if (i == 0 || preferences.getBoolean(AMMETER_IS_USED + i, false)) {
                item = new AmmeterViewData();
                item.setSubMeter(i != 0);
                item.getIsUsed().setValue(true);
                item.setName(preferences.getString(AMMETER_NAME + i, NAME[i]));
                item.getLastMonth().setValue(preferences.getString(AMMETER_THIS_MONTH + i, "0"));
                item.getThisMonth().setValue(DEFAULT_VALUE);
                item.getPublicPrice().setValue(DEFAULT_VALUE);
                item.getPrivatePrice().setValue(DEFAULT_VALUE);
                item.getTotalPrice().setValue(DEFAULT_VALUE);
                ammeters.add(item);
            }
        }
        return ammeters;
    }
}
