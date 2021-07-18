package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

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
                Arrays.asList(1, 2, 3));
        // TODO [21-07-17 9:05PM] -- interruptions & currentInteruption data should be tested
        //  here too.
        
        CurrentSessionPrefsData result = ConvertCurrentSession.toPrefsData(currentSession);
        
        assertThat(result.start, is(equalTo(currentSession.getStart().getTime())));
        assertThat(result.additionalComments, is(equalTo(currentSession.getAdditionalComments())));
        assertThat(result.moodIndex, is(currentSession.getMood().asIndex()));
        // TODO [21-07-17 9:05PM] -- This should test the id values as well.
        assertThat(result.selectedTagIds.size(),
                   is(equalTo(currentSession.getSelectedTagIds().size())));
    }
    
    @Test
    public void fromPrefsData_nullInput()
    {
        assertThat(ConvertCurrentSession.fromPrefsData(null), is(nullValue()));
    }
    
    @Test
    public void fromPrefsData_positiveInput()
    {
        CurrentSessionPrefsData data = new CurrentSessionPrefsData(
                TestUtils.ArbitraryData.getDate().getTime(),
                "test",
                2,
                new HashSet<>(Arrays.asList("1", "2", "3")),
                null,
                null);
        // TODO [21-07-17 9:07PM] -- this should test interruption data.
        
        CurrentSession currentSession = ConvertCurrentSession.fromPrefsData(data);
        
        assertThat(currentSession.getAdditionalComments(), is(equalTo(data.additionalComments)));
        assertThat(currentSession.getStart().getTime(), is(equalTo(data.start)));
        assertThat(currentSession.getMood().asIndex(), is(data.moodIndex));
        // TODO [21-07-17 9:07PM] -- this should test the actual values.
        assertThat(currentSession.getSelectedTagIds().size(),
                   is(equalTo(data.selectedTagIds.size())));
    }
}
