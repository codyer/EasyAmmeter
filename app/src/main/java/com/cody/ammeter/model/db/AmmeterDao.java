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


import com.cody.ammeter.model.db.table.Ammeter;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

/**
 * 电表数据访问接口
 */
@Dao
public interface AmmeterDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Ammeter ammeter);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Ammeter ammeter);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Ammeter> ammeter);

    @Query("SELECT * FROM ammeters WHERE id =:id")
    LiveData<Ammeter> liveAmmeter(long id);

    @Query("SELECT * FROM ammeters WHERE id == 1")
    Ammeter getMainAmmeter();

    @Query("SELECT * FROM ammeters WHERE id =:id")
    Ammeter getAmmeter(long id);

    @Query("SELECT * FROM ammeters WHERE id > 1")
    List<Ammeter> getTenantAmmeters();

    @Query("SELECT * FROM ammeters WHERE isLeave =:leave order by id ASC")
    LiveData<List<Ammeter>> liveAmmeters(boolean leave);

    @Query("SELECT * FROM ammeters WHERE isLeave =:leave and id > 1")
    List<Ammeter> getTenantAmmeters(boolean leave);

    @Query("select * from ammeters WHERE id > 1 order by isLeave ,id ASC")
    DataSource.Factory<Integer, Ammeter> getTenantDataSource();

    @Query("SELECT count(*) FROM ammeters  WHERE id > 1")
    LiveData<Long> liveTenantCount();

    @Query("SELECT count(*) FROM ammeters WHERE id > 1")
    long tenantCount();

    @Query("select * from ammeters WHERE id > 1 and isLeave =:leave order by newBalance ASC")
    DataSource.Factory<Integer, Ammeter> getTenantDataSource(boolean leave);

    @Query("SELECT count(*) FROM ammeters  WHERE id > 1 and isLeave =:leave")
    LiveData<Long> liveTenantCount(boolean leave);

    @Query("SELECT count(*) FROM ammeters WHERE id > 1 and isLeave =:leave")
    long tenantCount(boolean leave);

    @Query("DELETE FROM ammeters")
    void delete();
}