package com.rbraithwaite.sleepapp.test_utils.test_data;

import com.rbraithwaite.sleepapp.test_utils.test_data.builders.CalendarBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.CurrentSessionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.MoodBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.PostSleepDataBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SessionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepInterruptionEntityBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepSessionEntityBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.StoppedSessionDataBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.TagBuilder;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
    
    public static SleepSessionBuilder aSleepSession()
    {
        return new SleepSessionBuilder();
    }
    
    public static TagBuilder aTag()
    {
        return new TagBuilder();
    }
    
    public static SleepSessionEntityBuilder aSleepSessionEntity()
    {
        return new SleepSessionEntityBuilder();
    }
    
    public static SleepInterruptionEntityBuilder aSleepInterruptionEntity()
    {
        return new SleepInterruptionEntityBuilder();
    }
    
    public static SessionBuilder aSession()
    {
        return new SessionBuilder();
    }
    
    public static CalendarBuilder aCalendar()
    {
        return new CalendarBuilder();
    }
    
    public static <T> List<T> aListOf(BuilderOf<T>... builders)
    {
        return new ArrayList<>(Arrays.asList(builders)).stream()
                .map(BuilderOf::build)
                .collect(Collectors.toList());
    }
    
    /**
     * Some better syntax for getting the actual value of a test data builder (the syntax flows
     * better in the tests imo)
     */
    public static <T> T valueOf(BuilderOf<T> builder)
    {
        return builder.build();
    }
}
