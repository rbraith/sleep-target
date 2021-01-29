package com.rbraithwaite.sleepapp.data.current_goals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
    CurrentGoalsRepository repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        repository = new CurrentGoalsRepository(mockPrefs);
    }
    
    @After
    public void teardown()
    {
        mockPrefs = null;
        repository = null;
    }
    
    @Test
    public void clearWakeTimeGoal_updatesPrefs()
    {
        repository.clearWakeTimeGoal();
        verify(mockPrefs, times(1)).clearWakeTimeGoal();
    }
    
    @Test
    public void getWakeTimeGoal_updatesFromPrefs()
    {
        LiveData<Long> expected = new MutableLiveData<>(12345L);
        when(mockPrefs.getWakeTimeGoal()).thenReturn(expected);
        
        LiveData<Long> wakeTimeGoal = repository.getWakeTimeGoal();
        assertThat(wakeTimeGoal, is(expected));
    }
    
    @Test
    public void setWakeTimeGoal_updatesPrefs()
    {
        long expectedWakeTime = 12345L;
        
        repository.setWakeTimeGoal(expectedWakeTime);
        
        verify(mockPrefs, times(1)).setWakeTimeGoal(expectedWakeTime);
    }
    
    @Test
    public void getSleepDurationGoal_updatesFromPrefs()
    {
        LiveData<Integer> expected = new MutableLiveData<>(12345);
        when(mockPrefs.getSleepDurationGoal()).thenReturn(expected);
        
        // SUT
        LiveData<SleepDurationGoalModel> sleepDurationGoal = repository.getSleepDurationGoal();
        
        TestUtils.activateLocalLiveData(sleepDurationGoal);
        assertThat(sleepDurationGoal.getValue().inMinutes(), is(expected.getValue()));
    }
}
