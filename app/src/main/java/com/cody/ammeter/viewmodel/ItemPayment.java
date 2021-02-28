package com.cody.ammeter.viewmodel;

import com.cody.component.handler.data.ItemViewDataHolder;

import java.util.Objects;

public class ItemPayment extends ItemViewDataHolder {
    private long mId;
    private String mTime;
    private String mValue;

    public long getId() {
        return mId;
    }

    public void setId(final long id) {
        mId = id;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(final String time) {
        mTime = time;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(final String value) {
        mValue = value;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemPayment)) return false;
        final ItemPayment that = (ItemPayment) o;
        return Objects.equals(mTime, that.mTime) &&
                Objects.equals(mValue, that.mValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mTime, mValue);
    }
}
