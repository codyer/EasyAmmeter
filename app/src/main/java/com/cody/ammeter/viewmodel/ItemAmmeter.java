package com.cody.ammeter.viewmodel;

import com.cody.component.handler.data.ItemViewDataHolder;

import java.util.Date;
import java.util.Objects;

public class ItemAmmeter extends ItemViewDataHolder {
    public static final int MAIN_TYPE = 1;
    private long mAmmeterId;// 电表id
    private String mName;// 电表name
    private float mOldAmmeter;// 上一次电表数据
    private float mNewAmmeter;// 新的电表数据
    private float mOldBalance;// 上一次余额，每次结算生成新的结算记录时使用 mNewBalance
    private float mNewBalance;// 新的余额，每次缴费都加上缴费金额
    private float mSharing;// 公摊电量 sharing = ammeter - ammeterAmmeter
    private float mPrice;// 本次结算每度电单价  price = money/ammeter
    private Date mTime; // 结算时间，结算确认时要更新成当前时间，否则为上次结算时间

    public long getAmmeterId() {
        return mAmmeterId;
    }

    public void setAmmeterId(final long ammeterId) {
        mAmmeterId = ammeterId;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public float getOldAmmeter() {
        return mOldAmmeter;
    }

    public void setOldAmmeter(final float oldAmmeter) {
        mOldAmmeter = oldAmmeter;
    }

    public float getNewAmmeter() {
        return mNewAmmeter;
    }

    public void setNewAmmeter(final float newAmmeter) {
        mNewAmmeter = newAmmeter;
    }

    public float getOldBalance() {
        return mOldBalance;
    }

    public void setOldBalance(final float oldBalance) {
        mOldBalance = oldBalance;
    }

    public float getNewBalance() {
        return mNewBalance;
    }

    public void setNewBalance(final float newBalance) {
        mNewBalance = newBalance;
    }

    public float getSharing() {
        return mSharing;
    }

    public void setSharing(final float sharing) {
        mSharing = sharing;
    }

    public float getPrice() {
        return mPrice;
    }

    public void setPrice(final float price) {
        mPrice = price;
    }

    public Date getTime() {
        return mTime;
    }

    public void setTime(final Date time) {
        mTime = time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemAmmeter)) return false;
        final ItemAmmeter that = (ItemAmmeter) o;
        return mAmmeterId == that.mAmmeterId &&
                Float.compare(that.mOldAmmeter, mOldAmmeter) == 0 &&
                Float.compare(that.mNewAmmeter, mNewAmmeter) == 0 &&
                Float.compare(that.mOldBalance, mOldBalance) == 0 &&
                Float.compare(that.mNewBalance, mNewBalance) == 0 &&
                Float.compare(that.mSharing, mSharing) == 0 &&
                Float.compare(that.mPrice, mPrice) == 0 &&
                Objects.equals(mTime, that.mTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mAmmeterId, mOldAmmeter, mNewAmmeter, mOldBalance, mNewBalance, mSharing, mPrice, mTime);
    }
}
