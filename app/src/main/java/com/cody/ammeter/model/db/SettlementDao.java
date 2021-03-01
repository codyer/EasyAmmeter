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


import com.cody.ammeter.model.db.table.Settlement;

import java.util.Date;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * 结算数据访问接口
 */
@Dao
public interface SettlementDao {

    @Insert
    void insert(List<Settlement> settlements);

    @Insert
    void insert(Settlement settlement);

    @Query("SELECT * FROM settlements WHERE id = :id")
    LiveData<Settlement> liveSettlement(long id);

    @Query("SELECT * FROM settlements WHERE id = :id")
    Settlement getSettlement(long id);

    @Query("select * from settlements WHERE time =:time order by id ASC")
    DataSource.Factory<Integer, Settlement> getDataSource(Date time);

    @Query("select * from settlements WHERE ammeterId =:ammeterId order by id ASC")
    DataSource.Factory<Integer, Settlement> getDataSource(long ammeterId);

    @Query("SELECT count(*) FROM settlements WHERE time =:time")
    LiveData<Long> liveCount(Date time);

    @Query("SELECT count(*) FROM settlements WHERE ammeterId =:ammeterId")
    LiveData<Long> liveCount(long ammeterId);

    @Query("DELETE FROM settlements")
    void delete();
}