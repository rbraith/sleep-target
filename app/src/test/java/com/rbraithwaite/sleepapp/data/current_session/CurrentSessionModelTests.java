package com.rbraithwaite.sleepapp.data.current_session;

import com.rbraithwaite.sleepapp.TestUtils;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
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
}
