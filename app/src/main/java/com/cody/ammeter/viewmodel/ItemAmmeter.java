package com.cody.ammeter.viewmodel;

import com.cody.component.handler.data.ItemViewDataHolder;

import java.util.Date;
import java.util.Objects;

public class ItemAmmeter extends ItemViewDataHolder {
    public static final int MAIN_TYPE = 1;
    public static final int NOT_SET_TYPE = 2;
    private long mAmmeterId;// 电表id
    private String mName;// 电表name
    private double mOldAmmeter;// 上一次电表数据
    private double mNewAmmeter;// 新的电表数据
    private double mOldBalance;// 上一次余额，每次结算生成新的结算记录时使用 mNewBalance
    private double mNewBalance;// 新的余额，每次缴费都加上缴费金额
    private double mSharing;// 公摊电量 sharing = ammeter - ammeterAmmeter
    private double mPrice;// 本次结算每度电单价  price = money/ammeter
    private Date mTime; // 结算时间，结算确认时要更新成当前时间，否则为上次结算时间

    public boolean validData() {
        if (mTime == null) return false;
        if ((new Date().getTime() - mTime.getTime()) * 1.0 / (1000 * 60 * 60) > 24) {
            return false;
        }
        return mNewAmmeter > mOldAmmeter;
    }

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

    public double getOldAmmeter() {
        return mOldAmmeter;
    }

    public void setOldAmmeter(final double oldAmmeter) {
        mOldAmmeter = oldAmmeter;
    }

    public double getNewAmmeter() {
        return mNewAmmeter;
    }

    public void setNewAmmeter(final double newAmmeter) {
        mNewAmmeter = newAmmeter;
    }

    public double getOldBalance() {
        return mOldBalance;
    }

    public void setOldBalance(final double oldBalance) {
        mOldBalance = oldBalance;
    }

    public double getNewBalance() {
        return mNewBalance;
    }

    public void setNewBalance(final double newBalance) {
        mNewBalance = newBalance;
    }

    public double getSharing() {
        return mSharing;
    }

    public void setSharing(final double sharing) {
        mSharing = sharing;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(final double price) {
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
                Double.compare(that.mOldAmmeter, mOldAmmeter) == 0 &&
                Double.compare(that.mNewAmmeter, mNewAmmeter) == 0 &&
                Double.compare(that.mOldBalance, mOldBalance) == 0 &&
                Double.compare(that.mNewBalance, mNewBalance) == 0 &&
                Double.compare(that.mSharing, mSharing) == 0 &&
                Double.compare(that.mPrice, mPrice) == 0 &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mTime, that.mTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mAmmeterId, mName, mOldAmmeter, mNewAmmeter, mOldBalance, mNewBalance, mSharing, mPrice, mTime);
    }
}
