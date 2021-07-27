package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.rbraithwaite.sleepapp.test_utils.TestUtils.ArbitraryData.getCalendar;
import static com.rbraithwaite.sleepapp.test_utils.TestUtils.ArbitraryData.getDurationMillis;

public class SleepSessionEntityBuilder
        implements BuilderOf<SleepSessionEntity>
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public SleepSessionEntity build()
    {
        SleepSessionEntity sleepSessionEntity = new SleepSessionEntity();
        
        GregorianCalendar cal = getCalendar();
        sleepSessionEntity.startTime = cal.getTime();
        sleepSessionEntity.duration = getDurationMillis();
        
        cal.add(Calendar.MILLISECOND, (int) sleepSessionEntity.duration);
        sleepSessionEntity.endTime = cal.getTime();
        
        sleepSessionEntity.additionalComments = "lol!";
        
        sleepSessionEntity.rating = 2.5f;
        
        return sleepSessionEntity;
    }
}
