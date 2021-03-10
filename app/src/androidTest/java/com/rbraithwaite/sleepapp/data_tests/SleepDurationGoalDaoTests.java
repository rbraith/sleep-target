package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SleepDurationGoalDaoTests
{
    // REFACTOR [21-03-8 10:12PM] -- this setup stuff is boilerplate, duplicated in
    //  SleepSessionDaoTests
    //  make a common base test class for this stuff if that's possible - look into test
    //  inheritance.

//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private SleepDurationGoalDao sleepDurationGoalDao;

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
        sleepDurationGoalDao = database.getSleepDurationGoalDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    @Test
    public void getCurrentSleepDurationGoal_reflects_updateSleepDurationGoal()
    {
        SleepDurationGoalEntity expected = new SleepDurationGoalEntity();
        expected.editTime = TestUtils.ArbitraryData.getDate();
        expected.goalMinutes = 12345;
        
        sleepDurationGoalDao.updateSleepDurationGoal(expected);
        
        LiveData<SleepDurationGoalEntity> currentGoal =
                sleepDurationGoalDao.getCurrentSleepDurationGoal();
        TestUtils.InstrumentationLiveDataSynchronizer<SleepDurationGoalEntity> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(currentGoal);
        
        // REFACTOR [21-03-8 10:19PM] -- maybe use a custom equals() here? although it's awkward
        //  because you don't want to compare the id here.
        assertThat(currentGoal.getValue().editTime, is(equalTo(expected.editTime)));
        assertThat(currentGoal.getValue().goalMinutes, is(equalTo(expected.goalMinutes)));
    }
}
