package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class CurrentSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void createSnapshot_createsCorrectSnapshot()
    {
        GregorianCalendar cal = new GregorianCalendar(2021, 4, 9, 12, 34);
        long durationMillis = 10 * 60 * 1000; // 10 min
        Date start = cal.getTime();
        cal.add(Calendar.MILLISECOND, (int) durationMillis);
        Date end = cal.getTime();
        
        CurrentSession currentSession = new CurrentSession(
                start,
                "some comments",
                Mood.fromIndex(1),
                Arrays.asList(1, 2, 3));
        
        TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return end;
            }
        };
        
        // SUT
        CurrentSession.Snapshot snapshot = currentSession.createSnapshot(stubTimeUtils);
        
        assertThat(snapshot.start, is(equalTo(start)));
        assertThat(snapshot.end, is(equalTo(end)));
        assertThat(snapshot.durationMillis, is(equalTo(durationMillis)));
        assertThat(snapshot.additionalComments,
                   is(equalTo(currentSession.getAdditionalComments())));
        assertThat(snapshot.mood, is(equalTo(currentSession.getMood())));
        assertThat(snapshot.selectedTagIds, is(equalTo(currentSession.getSelectedTagIds())));
    }
    
    @Test
    public void isSet_returnsCorrectValue()
    {
        CurrentSession currentSession = new CurrentSession();
        assertThat(currentSession.isStarted(), is(false));
        
        currentSession.setStart(TestUtils.ArbitraryData.getDate());
        assertThat(currentSession.isStarted(), is(true));
    }
    
    @Test
    public void setStart_nullInputUnsets()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate());
        assertThat(currentSession.isStarted(), is(true));
        
        currentSession.setStart(null);
        assertThat(currentSession.isStarted(), is(false));
    }
    
    @Test
    public void getOngoingDurationMillis_isDynamic()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate());
        
        TimeUtils timeUtils = new TimeUtils();
        
        long duration1 = currentSession.getOngoingDurationMillis(timeUtils);
        // let some time pass
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long duration2 = currentSession.getOngoingDurationMillis(timeUtils);
        
        assertThat(duration2, is(greaterThan(duration1)));
    }
}
