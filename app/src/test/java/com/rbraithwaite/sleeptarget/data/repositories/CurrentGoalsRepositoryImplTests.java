/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class CurrentGoalsRepositoryImplTests
{
//*********************************************************
// package properties
//*********************************************************

    WakeTimeGoalDao mockWakeTimeGoalDao;
    SleepDurationGoalDao mockSleepDurationGoalDao;
    CurrentGoalsRepositoryImpl repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockWakeTimeGoalDao = mock(WakeTimeGoalDao.class);
        mockSleepDurationGoalDao = mock(SleepDurationGoalDao.class);
        // REFACTOR [21-03-8 11:15PM] -- time utils should be mocked.
        repository = new CurrentGoalsRepositoryImpl(
                mockWakeTimeGoalDao,
                mockSleepDurationGoalDao,
                new TimeUtils(),
                new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        repository = null;
    }
    
    @Test
    public void getSleepDurationGoalHistory_returnsEmptyListIfNoHistory()
    {
        when(mockSleepDurationGoalDao.getSleepDurationGoalHistory()).thenReturn(
                new MutableLiveData<List<SleepDurationGoalEntity>>(new ArrayList<SleepDurationGoalEntity>()));
        
        LiveData<List<SleepDurationGoal>> goalHistory =
                repository.getSleepDurationGoalHistory();
        TestUtils.activateLocalLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(equalTo(0)));
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
        
        when(mockSleepDurationGoalDao.getSleepDurationGoalHistory()).thenReturn(new MutableLiveData<>(
                expected));
        
        // SUT
        LiveData<List<SleepDurationGoal>> goalHistory =
                repository.getSleepDurationGoalHistory();
        
        // verify
        TestUtils.activateLocalLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            SleepDurationGoalEntity entity = expected.get(i);
            SleepDurationGoal model = goalHistory.getValue().get(i);
            
            assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
            assertThat(model.inMinutes(), is(equalTo(entity.goalMinutes)));
        }
    }
    
    @Test
    public void getWakeTimeGoalHistory_returnsEmptyListIfNoHistory()
    {
        when(mockWakeTimeGoalDao.getWakeTimeGoalHistory()).thenReturn(
                new MutableLiveData<List<WakeTimeGoalEntity>>(new ArrayList<WakeTimeGoalEntity>()));
        
        LiveData<List<WakeTimeGoal>> goalHistory = repository.getWakeTimeGoalHistory();
        TestUtils.activateLocalLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(equalTo(0)));
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
        goal2.wakeTimeGoal = 500;
        
        List<WakeTimeGoalEntity> expected = Arrays.asList(goal1, goal2);
        
        when(mockWakeTimeGoalDao.getWakeTimeGoalHistory()).thenReturn(new MutableLiveData<>(expected));
        
        // SUT
        LiveData<List<WakeTimeGoal>> goalHistory = repository.getWakeTimeGoalHistory();
        
        // verify
        TestUtils.activateLocalLiveData(goalHistory);
        assertThat(goalHistory.getValue().size(), is(expected.size()));
        for (int i = 0; i < expected.size(); i++) {
            WakeTimeGoalEntity entity = expected.get(i);
            WakeTimeGoal model = goalHistory.getValue().get(i);
            
            assertThat(model.getEditTime(), is(equalTo(entity.editTime)));
            assertThat(model.getGoalMillis(), is(equalTo(entity.wakeTimeGoal)));
        }
    }
    
    @Test
    public void clearWakeTimeGoal_updatesDatabase()
    {
        repository.clearWakeTimeGoal();
        verify(mockWakeTimeGoalDao, times(1)).updateWakeTimeGoal(any(WakeTimeGoalEntity.class));
    }
    
    @Test
    public void clearSleepDurationGoal_updatesDatabase()
    {
        repository.clearSleepDurationGoal();
        verify(mockSleepDurationGoalDao, times(1))
                .updateSleepDurationGoal(any(SleepDurationGoalEntity.class));
    }
    
    @Test
    public void getWakeTimeGoal_updatesFromDatabase()
    {
        LiveData<WakeTimeGoalEntity> expected =
                new MutableLiveData<>(TestUtils.ArbitraryData.getWakeTimeGoalEntity());
        when(mockWakeTimeGoalDao.getCurrentWakeTimeGoal()).thenReturn(expected);
        
        LiveData<WakeTimeGoal> wakeTimeGoal = repository.getWakeTimeGoal();
        TestUtils.activateLocalLiveData(wakeTimeGoal);
        assertThat(wakeTimeGoal.getValue().getEditTime(),
                   is(equalTo(expected.getValue().editTime)));
        assertThat(wakeTimeGoal.getValue().getGoalMillis(),
                   is(equalTo(expected.getValue().wakeTimeGoal)));
    }
    
    @Test
    public void setWakeTimeGoal_updatesDatabase()
    {
        WakeTimeGoal expected = TestUtils.ArbitraryData.getWakeTimeGoal();
        
        repository.setWakeTimeGoal(expected);
        
        ArgumentCaptor<WakeTimeGoalEntity> arg = ArgumentCaptor.forClass(WakeTimeGoalEntity.class);
        verify(mockWakeTimeGoalDao, times(1)).updateWakeTimeGoal(arg.capture());
        WakeTimeGoalEntity entity = arg.getValue();
        assertThat(entity.id, is(0));
        assertThat(entity.editTime, is(equalTo(expected.getEditTime())));
        assertThat(entity.wakeTimeGoal, is(equalTo(expected.getGoalMillis())));
    }
    
    @Test
    public void getSleepDurationGoal_updatesFromDatabase()
    {
        LiveData<SleepDurationGoalEntity> expected =
                new MutableLiveData<>(TestUtils.ArbitraryData.getSleepDurationGoalEntity());
        when(mockSleepDurationGoalDao.getCurrentSleepDurationGoal()).thenReturn(expected);
        
        // SUT
        LiveData<SleepDurationGoal> sleepDurationGoal = repository.getSleepDurationGoal();
        
        TestUtils.activateLocalLiveData(sleepDurationGoal);
        assertThat(sleepDurationGoal.getValue().inMinutes(), is(expected.getValue().goalMinutes));
        assertThat(sleepDurationGoal.getValue().getEditTime(),
                   is(equalTo(expected.getValue().editTime)));
    }
    
    @Test
    public void setSleepDurationGoal_updatesDatabase()
    {
        SleepDurationGoal model = TestUtils.ArbitraryData.getSleepDurationGoal();
        repository.setSleepDurationGoal(model);
        
        ArgumentCaptor<SleepDurationGoalEntity> arg =
                ArgumentCaptor.forClass(SleepDurationGoalEntity.class);
        verify(mockSleepDurationGoalDao, times(1)).updateSleepDurationGoal(arg.capture());
        SleepDurationGoalEntity entity = arg.getValue();
        
        assertThat(entity.id, is(0));
        assertThat(entity.editTime, is(model.getEditTime()));
        assertThat(entity.goalMinutes, is(model.inMinutes()));
    }
}
