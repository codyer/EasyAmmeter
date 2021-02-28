package com.cody.ammeter.viewmodel;

import com.cody.component.handler.data.ItemViewDataHolder;

import java.util.Objects;

public class ItemTenant extends ItemViewDataHolder {
    public static final int TYPE_LEAVE = DEFAULT_TYPE + 1;
    private long mId;
    private boolean mIsArrears;// 未设定或者欠费
    private String mName;
    private String mValue;
    private long mTime;

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

    public boolean isArrears() {
        return mIsArrears;
    }

    public void setArrears(final boolean arrears) {
        mIsArrears = arrears;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(final String value) {
        mValue = value;
    }

    public long getTime() {
        return mTime;
    }

    public void setTime(final long time) {
        mTime = time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemTenant)) return false;
        final ItemTenant that = (ItemTenant) o;
        return mId == that.mId &&
                mIsArrears == that.mIsArrears &&
                mTime == that.mTime &&
                Objects.equals(mName, that.mName) &&
                Objects.equals(mValue, that.mValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mId, mIsArrears, mName, mValue, mTime);
    }
}
