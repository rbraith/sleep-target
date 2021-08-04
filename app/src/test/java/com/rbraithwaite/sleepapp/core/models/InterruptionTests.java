package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;

import org.junit.Test;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class InterruptionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getEnd_returnsCorrectValue()
    {
        DateBuilder date = aDate();
        Interruption interruption = anInterruption()
                .withStart(date)
                .withDurationMinutes(1234)
                .build();
        
        Date end = interruption.getEnd();
        
        assertThat(end, is(equalTo(date.addMinutes(1234).build())));
    }
}
