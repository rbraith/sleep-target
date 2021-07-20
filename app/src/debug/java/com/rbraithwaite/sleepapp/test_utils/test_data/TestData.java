package com.rbraithwaite.sleepapp.test_utils.test_data;

import com.rbraithwaite.sleepapp.test_utils.test_data.builders.CurrentSessionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.MoodBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.PostSleepDataBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.StoppedSessionDataBuilder;

public class TestData
{
//*********************************************************
// constructors
//*********************************************************

    private TestData() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static CurrentSessionBuilder aCurrentSession()
    {
        return new CurrentSessionBuilder();
    }
    
    public static DateBuilder aDate()
    {
        return new DateBuilder();
    }
    
    public static MoodBuilder aMood()
    {
        return new MoodBuilder();
    }
    
    public static InterruptionBuilder anInterruption()
    {
        return new InterruptionBuilder();
    }
    
    public static PostSleepDataBuilder aPostSleepData()
    {
        return new PostSleepDataBuilder();
    }
    
    public static StoppedSessionDataBuilder aStoppedSessionData()
    {
        return new StoppedSessionDataBuilder();
    }
}
