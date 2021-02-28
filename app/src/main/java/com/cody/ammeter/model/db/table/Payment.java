package com.cody.ammeter.model.db.table;

import java.util.Date;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 缴费数据
 * 所有的缴费都进行记录
 * ammeterId = 0 总表缴费记录
 * ammeterId > 0 租户缴费记录
 */
@Entity(tableName = "payments")
public class Payment {
    @PrimaryKey(autoGenerate = true)
    private long id; // 数据 id
    private long ammeterId;// 电表id
    private float value;// 缴费金额值
    private Date time; // 缴费时间

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

    public float getValue() {
        return value;
    }

    public void setValue(final float value) {
        this.value = value;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(final Date time) {
        this.time = time;
    }
}
