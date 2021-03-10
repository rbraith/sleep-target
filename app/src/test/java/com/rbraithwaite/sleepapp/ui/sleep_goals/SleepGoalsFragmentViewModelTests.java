package com.rbraithwaite.sleepapp.ui.sleep_goals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.current_goals.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_goals.WakeTimeGoalModel;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.data.SleepDurationGoalUIData;

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
    CurrentGoalsRepository mockCurrentGoalsRepository;
    DateTimeFormatter dateTimeFormatter;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockCurrentGoalsRepository = mock(CurrentGoalsRepository.class);
        // REFACTOR [21-01-11 10:36PM] -- I need to be mocking DateTimeFormatter.
        dateTimeFormatter = new DateTimeFormatter();
        viewModel = new SleepGoalsFragmentViewModel(mockCurrentGoalsRepository, dateTimeFormatter);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockCurrentGoalsRepository = null;
        dateTimeFormatter = null;
    }
    
    @Test
    public void hasSleepDurationGoal_updatesProperly()
    {
        MutableLiveData<SleepDurationGoalModel> mockSleepDurationGoal = new MutableLiveData<>(
                SleepDurationGoalModel.createWithNoGoal());
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(mockSleepDurationGoal);
        
        // SUT
        LiveData<Boolean> hasSleepDurationGoal = viewModel.hasSleepDurationGoal();
        TestUtils.activateLocalLiveData(hasSleepDurationGoal);
        
        // REFACTOR [21-01-29 1:47PM] -- should this be broken off into a separate test?
        //  hasSleepDurationGoal_isFalseWhenNoGoal
        assertThat(hasSleepDurationGoal.getValue(), is(false));
        
        // simulate setting a new goal
        mockSleepDurationGoal.setValue(new SleepDurationGoalModel(123));
        assertThat(hasSleepDurationGoal.getValue(), is(true));
    }
    
    @Test
    public void getSleepDurationGoalText_updatesFromRepository()
    {
        int testMinutes = 123;
        SleepDurationGoalModel testSleepDurationGoal = new SleepDurationGoalModel(testMinutes);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(
                new MutableLiveData<>(testSleepDurationGoal));
        
        LiveData<String> sleepDurationGoalText = viewModel.getSleepDurationGoalText();
        TestUtils.activateLocalLiveData(sleepDurationGoalText);
        
        assertThat(sleepDurationGoalText.getValue(), is(equalTo(
                SleepGoalsFormatting.formatSleepDurationGoal(new SleepDurationGoalModel(testMinutes)))));
    }
    
    @Test
    public void getSleepDurationGoal_callRepository()
    {
        LiveData<SleepDurationGoalModel> expected =
                new MutableLiveData<>(new SleepDurationGoalModel(123));
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(expected);
        LiveData<SleepDurationGoalUIData> goal = viewModel.getSleepDurationGoal();
        TestUtils.activateLocalLiveData(goal);
        assertThat(goal.getValue().hours, is(expected.getValue().getHours()));
        assertThat(goal.getValue().remainingMinutes, is(expected.getValue().getRemainingMinutes()));
    }
    
    @Test
    public void clearSleepDurationGoal_callRepo()
    {
        viewModel.clearSleepDurationGoal();
        verify(mockCurrentGoalsRepository, times(1)).clearSleepDurationGoal();
    }
    
    @Test
    public void clearWakeTime_callsRepo()
    {
        viewModel.clearWakeTime();
        verify(mockCurrentGoalsRepository, times(1)).clearWakeTimeGoal();
    }
    
    @Test
    public void hasWakeTime_updatesProperly()
    {
        MutableLiveData<WakeTimeGoalModel> mockWakeTimeGoal = new MutableLiveData<>(null);
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(mockWakeTimeGoal);
        
        LiveData<Boolean> hasWakeTime = viewModel.hasWakeTime();
        TestUtils.LocalLiveDataSynchronizer<Boolean> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(hasWakeTime);
        
        assertThat(hasWakeTime.getValue(), is(false));
        
        mockWakeTimeGoal.setValue(TestUtils.ArbitraryData.getWakeTimeGoalModel());
        synchronizer.sync();
        assertThat(hasWakeTime.getValue(), is(true));
        
        // TODO [20-12-23 2:10AM] -- check with clearWakeTime().
    }
    
    @Test
    public void setWakeTime_updatesRepository()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.setWakeTime(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        
        ArgumentCaptor<WakeTimeGoalModel> arg = ArgumentCaptor.forClass(WakeTimeGoalModel.class);
        verify(mockCurrentGoalsRepository, times(1))
                .setWakeTimeGoal(arg.capture());
        
        WakeTimeGoalModel wakeTime = arg.getValue();
        GregorianCalendar calendar2 = new GregorianCalendar();
        calendar2.setTime(wakeTime.asDate());
        assertThat(calendar.get(Calendar.HOUR_OF_DAY), is(calendar2.get(Calendar.HOUR_OF_DAY)));
        assertThat(calendar.get(Calendar.MINUTE), is(calendar2.get(Calendar.MINUTE)));
    }
    
    @Test
    public void getWakeTimeText_updatesFromRepository()
    {
        WakeTimeGoalModel expectedWakeTime = TestUtils.ArbitraryData.getWakeTimeGoalModel();
        
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(
                new MutableLiveData<>(expectedWakeTime));
        
        LiveData<String> wakeTimeText = viewModel.getWakeTimeText();
        TestUtils.activateLocalLiveData(wakeTimeText);
        
        assertThat(wakeTimeText.getValue(),
                   is(equalTo(dateTimeFormatter.formatTimeOfDay(expectedWakeTime.asDate()))));
    }
    
    @Test
    public void getWakeTimeGoalDateMillis_callsRepository()
    {
        LiveData<WakeTimeGoalModel> expected =
                new MutableLiveData<>(TestUtils.ArbitraryData.getWakeTimeGoalModel());
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(expected);
        
        LiveData<Long> wakeTimeGoalMillis = viewModel.getWakeTimeGoalDateMillis();
        TestUtils.activateLocalLiveData(wakeTimeGoalMillis);
        assertThat(wakeTimeGoalMillis.getValue(),
                   is(equalTo((long) expected.getValue().asDate().getTime())));
    }
    
    @Test
    public void getDefaultWakeTime_returnsCorrectValue()
    {
        long defaultWakeTime = viewModel.getDefaultWakeTime();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_HOUR);
        calendar.set(Calendar.MINUTE, SleepGoalsFragmentViewModel.DEFAULT_WAKETIME_MINUTE);
        
        GregorianCalendar defaultWakeTimeCalendar = new GregorianCalendar();
        defaultWakeTimeCalendar.setTimeInMillis(defaultWakeTime);
        
        assertThat(defaultWakeTimeCalendar.get(Calendar.HOUR_OF_DAY),
                   is(equalTo(calendar.get(Calendar.HOUR_OF_DAY))));
        assertThat(defaultWakeTimeCalendar.get(Calendar.MINUTE),
                   is(equalTo(calendar.get(Calendar.MINUTE))));
    }
    
    @Test
    public void setSleepDurationGoal_callsRepository()
    {
        SleepDurationGoalModel testGoal = new SleepDurationGoalModel(1234);
        
        ArgumentCaptor<SleepDurationGoalModel> repoArg =
                ArgumentCaptor.forClass(SleepDurationGoalModel.class);
        viewModel.setSleepDurationGoal(testGoal.getHours(), testGoal.getRemainingMinutes());
        verify(mockCurrentGoalsRepository, times(1))
                .setSleepDurationGoal(repoArg.capture());
        
        SleepDurationGoalModel sleepDurationGoal = repoArg.getValue();
        assertThat(sleepDurationGoal.inMinutes(), is(equalTo(testGoal.inMinutes())));
    }
}
