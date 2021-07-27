package com.rbraithwaite.sleepapp.ui_tests.session_details_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SessionDetailsTestDriver;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.Day;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;

@RunWith(AndroidJUnit4.class)
public class SessionDetailsFragmentTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(20, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Test
    public void startUpdatesCorrectly()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        sessionDetails.assertThat().displayedValuesMatch(sleepSession);
        
        // offset by 24 hours and 5 minutes so that the date and time are both updated
        SleepSession editedSession = SleepSession.copyOf(sleepSession);
        editedSession.offsetStartFixed(-24, -5);
        sessionDetails.setStart(editedSession.getStart());
        
        // might as well check all values, to make sure nothing else was updated accidentally
        sessionDetails.assertThat().displayedValuesMatch(editedSession);
    }
    
    @Test
    public void endUpdatesCorrectly()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        // offset by 24 hours and 5 minutes so that the date and time are both updated
        SleepSession editedSession = SleepSession.copyOf(sleepSession);
        editedSession.offsetEndFixed(24, 5);
        sessionDetails.setEnd(editedSession.getEnd());
        
        // might as well check all values, to make sure nothing else was updated accidentally
        sessionDetails.assertThat().displayedValuesMatch(editedSession);
    }
    
    @Test
    public void settingFutureTimeShowsError()
    {
        TimeUtils timeUtils = new TimeUtils();
        SleepSession sleepSession = new SleepSession(timeUtils.getNow(), 0);
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        Day today = Day.of(sleepSession.getEnd());
        Day tomorrow = today.nextDay();
        
        sessionDetails.setEndDay(tomorrow);
        
        sessionDetails.assertThat().futureTimeErrorDialogIsDisplayed();
    }
    
    @Test
    public void invalidStartShowsErrors()
    {
        SleepSession sleepSession = sleepSessionWithZeroDuration();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        GregorianCalendar sessionTime = TimeUtils.getCalendarFrom(sleepSession.getStart());
        
        // invalid start day
        GregorianCalendar invalidStart = new GregorianCalendar();
        invalidStart.setTime(sessionTime.getTime());
        invalidStart.add(Calendar.DAY_OF_MONTH, 1);
        
        Day invalidDay = Day.of(invalidStart);
        sessionDetails.setStartDay(invalidDay);
        sessionDetails.assertThat().invalidStartErrorDialogIsDisplayed();
        sessionDetails.closeErrorDialog();
        
        // invalid start time of day
        invalidStart.add(Calendar.MINUTE, 123);
        
        TimeOfDay invalidTimeOfDay = TimeOfDay.of(invalidStart);
        sessionDetails.setStartTimeOfDay(invalidTimeOfDay);
        sessionDetails.assertThat().invalidStartErrorDialogIsDisplayed();
        sessionDetails.closeErrorDialog();
        
        // verify that the session was not changed
        sessionDetails.assertThat().displayedValuesMatch(sleepSession);
    }
    
    @Test
    public void invalidEndShowsErrors()
    {
        SleepSession sleepSession = sleepSessionWithZeroDuration();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        GregorianCalendar sessionTime = TimeUtils.getCalendarFrom(sleepSession.getStart());
        
        // invalid end day
        GregorianCalendar invalidEnd = new GregorianCalendar();
        invalidEnd.setTime(sessionTime.getTime());
        invalidEnd.add(Calendar.DAY_OF_MONTH, -1);
        
        Day invalidDay = Day.of(invalidEnd);
        sessionDetails.setEndDay(invalidDay);
        sessionDetails.assertThat().invalidEndErrorDialogIsDisplayed();
        sessionDetails.closeErrorDialog();
        
        // invalid start time of day
        invalidEnd.add(Calendar.MINUTE, -123);
        
        TimeOfDay invalidTimeOfDay = TimeOfDay.of(invalidEnd);
        sessionDetails.setEndTimeOfDay(invalidTimeOfDay);
        sessionDetails.assertThat().invalidEndErrorDialogIsDisplayed();
        sessionDetails.closeErrorDialog();
        
        // verify that the session was not changed
        sessionDetails.assertThat().displayedValuesMatch(sleepSession);
    }
    
    @Test
    public void interruptionsDisplayProperly()
    {
        SleepSession sleepSession = sleepSessionWithZeroDuration();
        sleepSession.setInterruptions(new Interruptions(aListOf(
                anInterruption(), anInterruption())));
        
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        sessionDetails.assertThat().interruptionDetailsMatch(sleepSession.getInterruptions());
    }

//*********************************************************
// private methods
//*********************************************************

    private SleepSession sleepSessionWithZeroDuration()
    {
        return new SleepSession(TestUtils.ArbitraryData.getDate(), 0);
    }
    
    // TODO [20-11-28 10:17PM] -- test fragment arg variations
    //  start null, end null, both null, start after end
}
