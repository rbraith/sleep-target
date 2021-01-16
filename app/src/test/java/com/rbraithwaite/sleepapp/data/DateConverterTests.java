package com.rbraithwaite.sleepapp.data;

import com.rbraithwaite.sleepapp.data.database.convert.DateConverter;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class DateConverterTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void twoWayConversionTest()
    {
        Date testDate = new GregorianCalendar(2019, 8, 7).getTime();
        
        Date resultDate = DateConverter.convertDateFromMillis(
                DateConverter.convertDateToMillis(testDate));
        
        assertThat(resultDate, is(equalTo(testDate)));
    }
    
    @Test
    public void nullInputTest()
    {
        Date result = DateConverter.convertDateFromMillis(DateConverter.convertDateToMillis(null));
        assertThat(result, is(nullValue()));
    }
}
