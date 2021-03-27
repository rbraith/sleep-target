package com.rbraithwaite.sleepapp.di;

import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ActivityRetainedComponent;

@Module
@InstallIn(ActivityRetainedComponent.class)
public class UIDependenciesModule
{
//*********************************************************
// public helpers
//*********************************************************

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionDataDateTimeFormatter {}
    
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SleepGoalsDateTimeFormatter {}
    
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SleepTrackerDateTimeFormatter {}
    
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SessionArchiveDateTimeFormatter {}

//*********************************************************
// api
//*********************************************************

    @SessionArchiveDateTimeFormatter
    @Provides
    public static DateTimeFormatter providesSessionArchiveDateTimeFormatter()
    {
        // This just uses the default DateTimeFormatter, but that could change in the future
        return new DateTimeFormatter();
    }
    
    @SessionDataDateTimeFormatter
    @Provides
    public static DateTimeFormatter providesSessionDataDateTimeFormatter()
    {
        // This just uses the default DateTimeFormatter, but that could change in the future
        return new DateTimeFormatter();
    }
    
    @SleepGoalsDateTimeFormatter
    @Provides
    public static DateTimeFormatter providesSleepGoalsDateTimeFormatter()
    {
        // This just uses the default DateTimeFormatter, but that could change in the future
        return new DateTimeFormatter();
    }
    
    @SleepTrackerDateTimeFormatter
    @Provides
    public static DateTimeFormatter provideSleepTrackerDateTimeFormatter()
    {
        // This just uses the default DateTimeFormatter, but that could change in the future
        return new DateTimeFormatter();
    }
}
