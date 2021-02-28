/*
 * ************************************************************
 * 文件：HttpCatDatabase.java  模块：http-cat  项目：component
 * 当前修改时间：2019年04月23日 18:23:19
 * 上次修改时间：2019年04月13日 08:43:55
 * 作者：Cody.yi   https://github.com/codyer
 *
 * 描述：http-cat
 * Copyright (c) 2019
 * ************************************************************
 */

package com.cody.ammeter.model.db;

import android.content.Context;

import com.cody.ammeter.model.db.table.Payment;
import com.cody.ammeter.model.db.table.Settlement;
import com.cody.ammeter.model.db.table.Ammeter;
import com.cody.component.handler.livedata.BooleanLiveData;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * AmmeterDatabase
 * 电费助手数据库
 */
@Database(entities = {Ammeter.class, Payment.class, Settlement.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AmmeterDatabase extends RoomDatabase {

    private static final String DB_NAME = "ammeter.db";

    private static volatile AmmeterDatabase instance;

    public static AmmeterDatabase getInstance() {
        if (instance == null) {
            throw new NullPointerException("AmmeterDatabase is not init.");
        }
        return instance;
    }

    public static AmmeterDatabase init(Context context) {
        if (instance == null) {
            synchronized (AmmeterDatabase.class) {
                if (instance == null) {
                    instance = create(context);
                }
            }
        }
        return instance;
    }

    private static AmmeterDatabase create(final Context context) {
        return Room.databaseBuilder(context, AmmeterDatabase.class, DB_NAME).build();
    }

    public abstract AmmeterDao getAmmeterDao();

    public abstract PaymentDao getPaymentDao();

    public abstract SettlementDao getSettlementDao();
}