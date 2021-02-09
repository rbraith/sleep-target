package com.rbraithwaite.sleepapp.data.current_session;

import com.rbraithwaite.sleepapp.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

public class CurrentSessionModelTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void isSet_returnsCorrectValue()
    {
        CurrentSessionModel currentSessionModel = new CurrentSessionModel();
        assertThat(currentSessionModel.isSet(), is(false));
        
        currentSessionModel.setStart(TestUtils.ArbitraryData.getDate());
        assertThat(currentSessionModel.isSet(), is(true));
    }
    
    @Test
    public void setStart_nullInputUnsets()
    {
        CurrentSessionModel currentSessionModel = new CurrentSessionModel(
                TestUtils.ArbitraryData.getDate());
        assertThat(currentSessionModel.isSet(), is(true));
        
        currentSessionModel.setStart(null);
        assertThat(currentSessionModel.isSet(), is(false));
    }
    
    @Test
    public void getOngoingDurationMillis_isDynamic()
    {
        CurrentSessionModel currentSessionModel = new CurrentSessionModel(
                TestUtils.ArbitraryData.getDate());
        
        long duration1 = currentSessionModel.getOngoingDurationMillis();
        // let some time pass
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long duration2 = currentSessionModel.getOngoingDurationMillis();
        
        assertThat(duration2, is(greaterThan(duration1)));
    }
}
