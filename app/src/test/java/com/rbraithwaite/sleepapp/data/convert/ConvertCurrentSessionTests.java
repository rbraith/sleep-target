package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertCurrentSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void toPrefsData_nullInput()
    {
        assertThat(ConvertCurrentSession.toPrefsData(null), is(nullValue()));
    }
    
    @Test
    public void toPrefsData_positiveInput()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate(),
                "test",
                Mood.fromIndex(2),
                Arrays.asList(1, 2, 3),
                new TimeUtils());
        
        CurrentSessionPrefsData result = ConvertCurrentSession.toPrefsData(currentSession);
        
        assertThat(result.start, is(equalTo(currentSession.getStart())));
        assertThat(result.additionalComments, is(equalTo(currentSession.getAdditionalComments())));
        assertThat(result.moodIndex, is(currentSession.getMood().toIndex()));
        assertThat(result.selectedTagIds, is(equalTo(currentSession.getSelectedTagIds())));
    }
    
    @Test
    public void fromPrefsData_nullInput()
    {
        assertThat(ConvertCurrentSession.fromPrefsData(null, new TimeUtils()), is(nullValue()));
    }
    
    @Test
    public void fromPrefsData_positiveInput()
    {
        CurrentSessionPrefsData data = new CurrentSessionPrefsData(
                TestUtils.ArbitraryData.getDate(),
                "test",
                2,
                Arrays.asList(1, 2, 3));
        
        CurrentSession currentSession = ConvertCurrentSession.fromPrefsData(data, new TimeUtils());
        
        assertThat(currentSession.getAdditionalComments(), is(equalTo(data.additionalComments)));
        assertThat(currentSession.getStart(), is(equalTo(data.start)));
        assertThat(currentSession.getMood().toIndex(), is(data.moodIndex));
        assertThat(currentSession.getSelectedTagIds(), is(equalTo(data.selectedTagIds)));
    }
}
