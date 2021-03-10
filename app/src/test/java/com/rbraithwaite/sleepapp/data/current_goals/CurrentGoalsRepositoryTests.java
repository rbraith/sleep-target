package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class CurrentGoalsRepositoryTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppDataPrefs mockPrefs;
    WakeTimeGoalDao mockWakeTimeGoalDao;
    SleepDurationGoalDao mockSleepDurationGoalDao;
    CurrentGoalsRepository repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        mockWakeTimeGoalDao = mock(WakeTimeGoalDao.class);
        mockSleepDurationGoalDao = mock(SleepDurationGoalDao.class);
        // REFACTOR [21-03-8 11:15PM] -- time utils should be mocked.
        repository = new CurrentGoalsRepository(
                mockPrefs,
                mockWakeTimeGoalDao,
                mockSleepDurationGoalDao,
                new TimeUtils(),
                new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        mockPrefs = null;
        repository = null;
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
        
        LiveData<WakeTimeGoalModel> wakeTimeGoal = repository.getWakeTimeGoal();
        TestUtils.activateLocalLiveData(wakeTimeGoal);
        assertThat(wakeTimeGoal.getValue().getEditTime(),
                   is(equalTo(expected.getValue().editTime)));
        assertThat(wakeTimeGoal.getValue().getGoalMillis(),
                   is(equalTo(expected.getValue().wakeTimeGoal)));
    }
    
    @Test
    public void setWakeTimeGoal_updatesDatabase()
    {
        WakeTimeGoalModel expected = TestUtils.ArbitraryData.getWakeTimeGoalModel();
        
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
        LiveData<SleepDurationGoalModel> sleepDurationGoal = repository.getSleepDurationGoal();
        
        TestUtils.activateLocalLiveData(sleepDurationGoal);
        assertThat(sleepDurationGoal.getValue().inMinutes(), is(expected.getValue().goalMinutes));
        assertThat(sleepDurationGoal.getValue().getEditTime(),
                   is(equalTo(expected.getValue().editTime)));
    }
    
    @Test
    public void setSleepDurationGoal_updatesDatabase()
    {
        SleepDurationGoalModel model = TestUtils.ArbitraryData.getSleepDurationGoalModel();
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
