package com.cody.helper;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;

/**
 * 电表
 */
public class Ammeter {
    private final MutableLiveData<Boolean> mIsUsed = new MutableLiveData<>(false);// 被使用了，有人住
    private boolean mIsSubMeter = true;// 是否为分表
    private String mName;// 电表名，对应房间名 --> 春夏秋冬 总
    private final MutableLiveData<String> mLastMonth = new MutableLiveData<>(DataHelper.DEFAULT_VALUE);// 上一次电表数值
    private final MutableLiveData<String> mThisMonth = new MutableLiveData<>(DataHelper.DEFAULT_VALUE);// 本次电表数值
    private final MutableLiveData<String> mPublicPrice = new MutableLiveData<>(DataHelper.DEFAULT_VALUE);// 分表：公共电费// 总表：公摊电量
    private final MutableLiveData<String> mPrivatePrice = new MutableLiveData<>(DataHelper.DEFAULT_VALUE);// 个人电费
    private final MutableLiveData<String> mTotalPrice = new MutableLiveData<>(DataHelper.DEFAULT_VALUE);// 总电费
    private float mKilowattHour = 0;

    public MutableLiveData<Boolean> getIsUsed() {
        return mIsUsed;
    }

    public boolean isSubMeter() {
        return mIsSubMeter;
    }

    public void setSubMeter(final boolean subMeter) {
        mIsSubMeter = subMeter;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public MutableLiveData<String> getLastMonth() {
        return mLastMonth;
    }

    public MutableLiveData<String> getThisMonth() {
        return mThisMonth;
    }

    public MutableLiveData<String> getPublicPrice() {
        return mPublicPrice;
    }

    public MutableLiveData<String> getPrivatePrice() {
        return mPrivatePrice;
    }

    public MutableLiveData<String> getTotalPrice() {
        return mTotalPrice;
    }

    public float getKilowattHour() {
        return mKilowattHour;
    }

    public boolean setKilowattHour() {
        mKilowattHour = 0;
        if (getThisMonth().getValue() != null && getLastMonth().getValue() != null) {
            if (TextUtils.isEmpty(getThisMonth().getValue()) || TextUtils.isEmpty(getLastMonth().getValue())) {
                return false;
            }
            mKilowattHour = Float.parseFloat(getThisMonth().getValue()) - Float.parseFloat((getLastMonth().getValue()));
            return mKilowattHour >= 0;
        }
        return false;
    }
}
