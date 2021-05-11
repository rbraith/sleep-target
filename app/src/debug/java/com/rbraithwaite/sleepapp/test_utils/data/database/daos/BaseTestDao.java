package com.rbraithwaite.sleepapp.test_utils.data.database.daos;

import android.database.sqlite.SQLiteDatabase;

public abstract class BaseTestDao
{
//*********************************************************
// protected properties
//*********************************************************

    protected SQLiteDatabase mDatabase;

//*********************************************************
// protected constants
//*********************************************************

    protected final String mTableName;
    
//*********************************************************
// constructors
//*********************************************************

    public BaseTestDao(SQLiteDatabase database, String tableName)
    {
        mDatabase = database;
        mTableName = tableName;
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected QueryBuilder createQueryBuilder()
    {
        return new QueryBuilder(mDatabase, mTableName);
    }
}
