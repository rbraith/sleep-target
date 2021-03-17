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

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
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
    public void getSleepDurationGoalHistory_returnsEmptyListWhenThereIsNoHistory()
    {
        LiveData<List<SleepDurationGoalEntity>> goalHistory =
                sleepDurationGoalDao.getSleepDurationGoalHistory();
        TestUtils.activateInstrumentationLiveData(goalHistory);
        assertThat(goalHistory.getValue().isEmpty(), is(true));
    }
    
    @Test
    public void getSleepDurationGoalHistory_returnsHistory()
    {
        // setup
        GregorianCalendar cal = new GregorianCalendar(2021, 2, 14);
        SleepDurationGoalEntity goal1 = new SleepDurationGoalEntity();
        goal1.editTime = cal.getTime();
        goal1.goalMinutes = 123;
        
        cal.add(Calendar.HOUR, 4);
        SleepDurationGoalEntity goal2 = new SleepDurationGoalEntity();
        goal2.editTime = cal.getTime();
        goal2.goalMinutes = 123;
        
        List<SleepDurationGoalEntity> expected = Arrays.asList(goal1, goal2);
        
        sleepDurationGoalDao.updateSleepDurationGoal(goal1);
        sleepDurationGoalDao.updateSleepDurationGoal(goal2);
        
        // SUT
        LiveData<List<SleepDurationGoalEntity>> goalHistory =
                sleepDurationGoalDao.getSleepDurationGoalHistory();
        
        // verify
        TestUtils.activateInstrumentationLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            SleepDurationGoalEntity entity = goalHistory.getValue().get(i);
            SleepDurationGoalEntity expectedEntity = expected.get(i);
            // shouldn't use equals(), because the ids would not be the same
            assertThat(entity.editTime, is(equalTo(expectedEntity.editTime)));
            assertThat(entity.goalMinutes, is(equalTo(expectedEntity.goalMinutes)));
        }
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
