/*
 * ************************************************************
 * 文件：HttpCatDao.java  模块：http-cat  项目：component
 * 当前修改时间：2019年04月23日 18:23:19
 * 上次修改时间：2019年04月13日 08:43:55
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：http-cat
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.ammeter.model.db;


import com.cody.ammeter.model.db.table.Payment;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

/**
 * 缴费数据访问接口
 */
@Dao
public interface PaymentDao {

    @Insert
    long insert(Payment payment);

    @Update
    void update(Payment payment);

    @Query("SELECT * FROM payments WHERE ammeterId = :ammeterId")
    LiveData<Payment> livePayment(long ammeterId);

    @Query("select * from payments WHERE ammeterId =:ammeterId order by id DESC")
    DataSource.Factory<Integer, Payment> getDataSource(long ammeterId);

    @Query("SELECT count(*) FROM payments WHERE ammeterId =:ammeterId ")
    LiveData<Long> liveCount(long ammeterId);

    @Query("DELETE FROM payments")
    void delete();
}