package com.cody.ammeter.model.db.table;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 电表表
 */
@Entity(tableName = "ammeters")
public class Ammeter {
    public static final String UN_TENANT_NAME = "总表";
    public static final String TENANT_NAME = "租户";
    public static final long UN_TENANT_ID = 1L; //非租客ID，主表使用这个ID
    @PrimaryKey(autoGenerate = true)
    private long id; // 电表 id
    private String name; // 电表名称（或者也可以看成租客名称）
    private Date checkInTime; // 入住时间 ，或者表示房东余额设定时间
    private Date ammeterSetTime; // 电量设定时间
    private boolean isLeave = false; // 是否已退租
    private float oldAmmeter = 0f;// 上一次电表数据，每次结算确认更新成 newAmmeter
    private float newAmmeter = 0f;// 新的电表数据
    private float oldBalance = 0f;// 上一次余额，每次结算确认更新成 mNewBalance：房东：每次缴费都加上缴费金额
    private float newBalance = 0f;// 当前余额 租客，每次缴费都加上缴费金额，每次结算确认都减去新的应缴总额

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Date getAmmeterSetTime() {
        return ammeterSetTime;
    }

    public void setAmmeterSetTime(final Date ammeterSetTime) {
        this.ammeterSetTime = ammeterSetTime;
    }

    public Date getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(final Date checkInTime) {
        this.checkInTime = checkInTime;
    }

    public boolean isLeave() {
        return isLeave;
    }

    public void setLeave(final boolean leave) {
        this.isLeave = leave;
    }

    public float getOldAmmeter() {
        return oldAmmeter;
    }

    public void setOldAmmeter(final float oldAmmeter) {
        this.oldAmmeter = oldAmmeter;
    }

    public float getNewAmmeter() {
        return newAmmeter;
    }

    public void setNewAmmeter(final float newAmmeter) {
        this.newAmmeter = newAmmeter;
    }

    public float getOldBalance() {
        return oldBalance;
    }

    public void setOldBalance(final float oldBalance) {
        this.oldBalance = oldBalance;
    }

    public float getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(final float newBalance) {
        this.newBalance = newBalance;
    }
}
