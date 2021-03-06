/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.data.database.AppDatabase;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class WakeTimeGoalDaoTests
{
    // REFACTOR [21-03-8 10:12PM] -- this setup stuff is boilerplate, duplicated in
    //  SleepSessionDaoTests & other dao test classes
    //  make a common base test class for this stuff if that's possible - look into test
    //  inheritance.

//*********************************************************
// private properties
//*********************************************************

    private AppDatabase database;
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
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        wakeTimeGoalDao = database.getWakeTimeGoalDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    @Test
    public void getWakeTimeGoalHistory_returnsEmptyListWhenThereIsNoHistory()
    {
        LiveData<List<WakeTimeGoalEntity>> wakeTimeGoalHistory =
                wakeTimeGoalDao.getWakeTimeGoalHistory();
        TestUtils.activateInstrumentationLiveData(wakeTimeGoalHistory);
        assertThat(wakeTimeGoalHistory.getValue().isEmpty(), is(true));
    }
    
    @Test
    public void getWakeTimeGoalHistory_returnsHistory()
    {
        // setup
        GregorianCalendar cal = new GregorianCalendar(2021, 2, 14);
        WakeTimeGoalEntity goal1 = new WakeTimeGoalEntity();
        goal1.editTime = cal.getTime();
        goal1.wakeTimeGoal = 500;
        
        cal.add(Calendar.HOUR, 4);
        WakeTimeGoalEntity goal2 = new WakeTimeGoalEntity();
        goal2.editTime = cal.getTime();
        goal2.wakeTimeGoal = 800;
        
        wakeTimeGoalDao.updateWakeTimeGoal(goal1);
        wakeTimeGoalDao.updateWakeTimeGoal(goal2);
        
        // SUT
        LiveData<List<WakeTimeGoalEntity>> goalHistory = wakeTimeGoalDao.getWakeTimeGoalHistory();
        
        // verify
        TestUtils.activateInstrumentationLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(2));
        List<WakeTimeGoalEntity> expected = Arrays.asList(goal1, goal2);
        for (int i = 0; i < expected.size(); i++) {
            WakeTimeGoalEntity entity = goalHistory.getValue().get(i);
            WakeTimeGoalEntity expectedEntity = expected.get(i);
            // shouldn't use equals(), because the ids would not be the same
            assertThat(entity.editTime, is(equalTo(expectedEntity.editTime)));
            assertThat(entity.wakeTimeGoal, is(equalTo(expectedEntity.wakeTimeGoal)));
        }
    }
    
    @Test
    public void getCurrentWakeTimeGoal_reflects_updateWakeTimeGoal()
    {
        WakeTimeGoalEntity expected = new WakeTimeGoalEntity();
        expected.editTime = TestUtils.ArbitraryData.getDate();
        expected.wakeTimeGoal = 12345;
        
        wakeTimeGoalDao.updateWakeTimeGoal(expected);
        
        LiveData<WakeTimeGoalEntity> currentWakeTimeGoal = wakeTimeGoalDao.getCurrentWakeTimeGoal();
        TestUtils.activateInstrumentationLiveData(currentWakeTimeGoal);
        
        // REFACTOR [21-03-8 10:19PM] -- maybe use a custom equals() here? although it's awkward
        //  because you don't want to compare the id here.
        assertThat(currentWakeTimeGoal.getValue().editTime, is(equalTo(expected.editTime)));
        assertThat(currentWakeTimeGoal.getValue().wakeTimeGoal, is(equalTo(expected.wakeTimeGoal)));
    }
}
