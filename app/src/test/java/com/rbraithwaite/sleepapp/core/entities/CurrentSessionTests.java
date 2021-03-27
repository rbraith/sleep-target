package com.rbraithwaite.sleepapp.core.entities;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class CurrentSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void isSet_returnsCorrectValue()
    {
        CurrentSession currentSession = new CurrentSession();
        assertThat(currentSession.isSet(), is(false));
        
        currentSession.setStart(TestUtils.ArbitraryData.getDate());
        assertThat(currentSession.isSet(), is(true));
    }
    
    @Test
    public void setStart_nullInputUnsets()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate());
        assertThat(currentSession.isSet(), is(true));
        
        currentSession.setStart(null);
        assertThat(currentSession.isSet(), is(false));
    }
    
    @Test
    public void getOngoingDurationMillis_isDynamic()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate());
        
        long duration1 = currentSession.getOngoingDurationMillis();
        // let some time pass
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long duration2 = currentSession.getOngoingDurationMillis();
        
        assertThat(duration2, is(greaterThan(duration1)));
    }
}
