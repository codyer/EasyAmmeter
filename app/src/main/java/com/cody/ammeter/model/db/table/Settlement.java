package com.cody.ammeter.model.db.table;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 结算结果
 * <p>
 * ammeterId = 0 总表结算结果
 * ammeterId > 0 租户结算结果
 * <p>
 * 保留所有的历史结算数据
 */
@Entity(tableName = "settlements")
public class Settlement {
    @PrimaryKey(autoGenerate = true)
    private long id; // 结算 id
    private long ammeterId;// 电表id
    private String name;// 电表id
    private double oldAmmeter;// 上一次电表数据
    private double newAmmeter;// 新的电表数据
//    @Ignore
//    private double mAmmeter;// 实际使用电量 ammeter = newAmmeter - oldAmmeter

    private double oldBalance;// 上一次余额，每次结算生成新的结算记录时使用 mNewBalance
    private double newBalance;// 新的余额，每次缴费都加上缴费金额
    //    @Ignore
//    private double mTenantAmmeter;// 所有房客实际使用电量之和，结算确认后才更新此值
//    @Ignore
//    private double mMoney;// 实际使用金额 money = oldBalance - newBalance
    private double sharing;// 公摊电量 sharing = ammeter - ammeterAmmeter
    //    @Ignore
    private double mPrice;// 本次结算每度电单价  price = money/ammeter
    private Date time; // 结算时间，结算确认时要更新成当前时间，否则为上次结算时间

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getAmmeterId() {
        return ammeterId;
    }

    public void setAmmeterId(final long ammeterId) {
        this.ammeterId = ammeterId;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public double getOldAmmeter() {
        return oldAmmeter;
    }

    public void setOldAmmeter(final double oldAmmeter) {
        this.oldAmmeter = oldAmmeter;
    }

    public double getNewAmmeter() {
        return newAmmeter;
    }

    public void setNewAmmeter(final double newAmmeter) {
        this.newAmmeter = newAmmeter;
    }

    public double getOldBalance() {
        return oldBalance;
    }

    public void setOldBalance(final double oldBalance) {
        this.oldBalance = oldBalance;
    }

    public double getNewBalance() {
        return newBalance;
    }

    public void setNewBalance(final double newBalance) {
        this.newBalance = newBalance;
    }

    public double getSharing() {
        return sharing;
    }

    public void setSharing(final double sharing) {
        this.sharing = sharing;
    }

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(final double price) {
        mPrice = price;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }
}
