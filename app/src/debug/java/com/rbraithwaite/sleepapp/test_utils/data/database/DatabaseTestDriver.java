package com.rbraithwaite.sleepapp.test_utils.data.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.platform.app.InstrumentationRegistry;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.test_utils.data.database.daos.SleepSessionTestDao;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DatabaseTestDriver
{
//*********************************************************
// private properties
//*********************************************************

    private SQLiteDatabase mDatabase;
    private SleepSessionTestDao mSleepSessionTestDao;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
    {
        private DatabaseTestDriver mOwner;
        
        private Assertions(DatabaseTestDriver owner)
        {
            mOwner = owner;
        }
        
        public void sleepSessionCountIs(int count)
        {
            assertThat(mOwner.mSleepSessionTestDao.getAllSleepSessionIds().size(),
                       is(equalTo(count)));
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public DatabaseTestDriver()
    {
        assertThat = new Assertions(this);
        mDatabase = findDatabase();
        mSleepSessionTestDao = new SleepSessionTestDao(mDatabase);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private SQLiteDatabase findDatabase()
    {
        // HACK [21-05-8 3:08PM] -- I need to find some way to access the RoomDatabase lol.
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        for (String databaseName : context.databaseList()) {
            if (SleepAppDatabase.NAME.equals(databaseName)) {
                return context.openOrCreateDatabase(SleepAppDatabase.NAME, 0, null);
            }
        }
        throw new RuntimeException("Could not find database.");
    }
}
