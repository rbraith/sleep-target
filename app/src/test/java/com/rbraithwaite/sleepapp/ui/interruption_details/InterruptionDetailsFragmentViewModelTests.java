package com.rbraithwaite.sleepapp.ui.interruption_details;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class InterruptionDetailsFragmentViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private TimeUtils mockTimeUtils;
    private InterruptionDetailsFragmentViewModel viewModel;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockTimeUtils = mock(TimeUtils.class);
        viewModel = new InterruptionDetailsFragmentViewModel(mockTimeUtils);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockTimeUtils = null;
    }
    
    
    @Test(expected = InterruptionDetailsFragmentViewModel.OutOfBoundsInterruptionException.class)
    public void checkForValidResult_throwsOnOutOfBoundsInterruption()
    {
        DateBuilder date = aDate();
        InterruptionBuilder interruption = anInterruption().withStart(date);
        
        viewModel.initData(new InterruptionDetailsData(
                valueOf(interruption),
                // add an hour so that the interruption starts before the session bounds
                valueOf(aSleepSession().withStart(date.addHours(1)))));
        
        viewModel.checkForValidResult();
    }
    
    @Test(expected = InterruptionDetailsFragmentViewModel.OverlappingInterruptionException.class)
    public void checkForValidResult_throwsOnOverlappingInterruption()
    {
        DateBuilder date = aDate();
        
        DateBuilder sessionStart = date.copy();
        
        InterruptionBuilder overlapped =
                anInterruption().withStart(date.addHours(1)).withDurationHours(2).withId(1);
        InterruptionBuilder overlapping = anInterruption().withStart(date.addHours(1)).withId(2);
        
        viewModel.initData(new InterruptionDetailsData(
                valueOf(overlapping),
                valueOf(aSleepSession()
                                .withStart(sessionStart)
                                .withDurationHours(6)
                                .withInterruptions(overlapped, overlapping))));
        
        viewModel.checkForValidResult();
    }
}