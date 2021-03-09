package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class WakeTimeGoalDaoTests
{
    // REFACTOR [21-03-8 10:12PM] -- this setup stuff is boilerplate, duplicated in
    //  SleepSessionDaoTests
    //  make a common base test class for this stuff if that's possible - look into test
    //  inheritance.

//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private WakeTimeGoalDao wakeTimeGoalDao;

//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, SleepAppDatabase.class).build();
        wakeTimeGoalDao = database.getWakeTimeGoalDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    @Test
    public void getCurrentWakeTimeGoal_reflects_updateWakeTimeGoal()
    {
        WakeTimeGoalEntity expected = new WakeTimeGoalEntity();
        expected.editTime = TestUtils.ArbitraryData.getDate();
        expected.wakeTimeGoal = 12345;
        
        wakeTimeGoalDao.updateWakeTimeGoal(expected);
        
        LiveData<WakeTimeGoalEntity> currentWakeTimeGoal = wakeTimeGoalDao.getCurrentWakeTimeGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<WakeTimeGoalEntity> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentWakeTimeGoal);
        
        // REFACTOR [21-03-8 10:19PM] -- maybe use a custom equals() here? although it's awkward
        //  because you don't want to compare the id here.
        assertThat(currentWakeTimeGoal.getValue().editTime, is(equalTo(expected.editTime)));
        assertThat(currentWakeTimeGoal.getValue().wakeTimeGoal, is(equalTo(expected.wakeTimeGoal)));
    }
}
