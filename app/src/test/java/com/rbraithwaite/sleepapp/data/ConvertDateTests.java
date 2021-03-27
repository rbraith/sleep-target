package com.rbraithwaite.sleepapp.data;

import com.rbraithwaite.sleepapp.data.database.convert.ConvertDate;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertDateTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void twoWayConversionTest()
    {
        Date testDate = new GregorianCalendar(2019, 8, 7).getTime();
        
        Date resultDate = ConvertDate.fromMillis(
                ConvertDate.toMillis(testDate));
        
        assertThat(resultDate, is(equalTo(testDate)));
    }
    
    @Test
    public void nullInputTest()
    {
        Date result = ConvertDate.fromMillis(ConvertDate.toMillis(null));
        assertThat(result, is(nullValue()));
    }
}
