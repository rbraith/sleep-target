package com.rbraithwaite.sleepapp.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepGoalsFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepGoalsFragmentViewModel viewModel;
    SleepAppRepository mockRepository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockRepository = mock(SleepAppRepository.class);
        viewModel = new SleepGoalsFragmentViewModel(mockRepository);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockRepository = null;
    }
    
    @Test
    public void hasWakeTime_updatesProperly()
    {
        MutableLiveData<Long> mockWakeTimeGoal = new MutableLiveData<>(null);
        when(mockRepository.getWakeTimeGoal()).thenReturn(mockWakeTimeGoal);
        
        LiveData<Boolean> hasWakeTime = viewModel.hasWakeTime();
        TestUtils.LocalLiveDataSynchronizer<Boolean> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(hasWakeTime);
        
        assertThat(hasWakeTime.getValue(), is(false));
        
        mockWakeTimeGoal.setValue(12345L);
        synchronizer.sync();
        assertThat(hasWakeTime.getValue(), is(true));
        
        // TODO [20-12-23 2:10AM] -- check with clearWakeTime().
    }
    
    @Test
    public void setWakeTime_updatesRepository()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setWakeTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        ArgumentCaptor<Long> setWakeTimeGoalArg = ArgumentCaptor.forClass(Long.class);
        verify(mockRepository, times(1)).setWakeTimeGoal(setWakeTimeGoalArg.capture());
        
        Long wakeTime = setWakeTimeGoalArg.getValue();
        GregorianCalendar calendar2 = new GregorianCalendar();
        calendar2.setTimeInMillis(wakeTime);
        assertThat(calendar.get(Calendar.HOUR_OF_DAY), is(calendar2.get(Calendar.HOUR_OF_DAY)));
        assertThat(calendar.get(Calendar.MINUTE), is(calendar2.get(Calendar.MINUTE)));
    }
    
    @Test
    public void getWakeTime_updatesFromRepository()
    {
        long expectedWakeTimeMillis = TestUtils.ArbitraryData.getDate().getTime();
        
        when(mockRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<Long>(
                expectedWakeTimeMillis));
        
        LiveData<String> wakeTime = viewModel.getWakeTime();
        TestUtils.activateLocalLiveData(wakeTime);
        
        assertThat(wakeTime.getValue(),
                   is(equalTo(new DateTimeFormatter().formatTimeOfDay(
                           DateUtils.getDateFromMillis(expectedWakeTimeMillis)))));
    }
    
    @Test
    public void getDefaultWakeTime_returnsCorrectValue()
    {
        long defaultWakeTime = viewModel.getDefaultWakeTime();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_HOUR);
        calendar.set(Calendar.MINUTE, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_MINUTE);
        
        assertThat(defaultWakeTime, is(equalTo(calendar.getTimeInMillis())));
    }
}