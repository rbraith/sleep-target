package com.rbraithwaite.sleepapp.test_utils.data.database.daos;

import android.database.sqlite.SQLiteDatabase;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionContract;

import java.util.ArrayList;
import java.util.List;

// HACK [21-05-8 3:29PM] -- why am I doing this?
public class SleepSessionTestDao
        extends BaseTestDao
{
//*********************************************************
// constructors
//*********************************************************

    public SleepSessionTestDao(SQLiteDatabase database)
    {
        super(database, SleepSessionContract.TABLE_NAME);
    }
    
//*********************************************************
// api
//*********************************************************

    public List<Integer> getAllSleepSessionIds()
    {
        List<Integer> ids = new ArrayList<>();
        createQueryBuilder()
                .setColumns(new String[] {SleepSessionContract.Columns.ID})
                .executeQuery(cursor -> {
                    int index = cursor.getColumnIndex(SleepSessionContract.Columns.ID);
                    while (cursor.moveToNext()) {
                        ids.add(cursor.getInt(index));
                    }
                });
        return ids;
    }
}
