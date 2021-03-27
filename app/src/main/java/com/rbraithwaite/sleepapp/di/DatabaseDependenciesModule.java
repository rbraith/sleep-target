package com.rbraithwaite.sleepapp.di;

import android.content.Context;

import androidx.room.Room;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;

@Module
@InstallIn(ApplicationComponent.class)
public class DatabaseDependenciesModule
{
//*********************************************************
// api
//*********************************************************

    @Singleton
    @Provides
    public static SleepAppDatabase provideSleepAppDatabase(@ApplicationContext Context context)
    {
        return Room.databaseBuilder(context, SleepAppDatabase.class, SleepAppDatabase.NAME).build();
    }
    
    @Singleton
    @Provides
    public static SleepSessionDao provideSleepSessionDao(SleepAppDatabase database)
    {
        return database.getSleepSessionDao();
    }
    
    @Singleton
    @Provides
    public static WakeTimeGoalDao provideWakeTimeGoalDao(SleepAppDatabase database)
    {
        return database.getWakeTimeGoalDao();
    }
    
    @Singleton
    @Provides
    public static SleepDurationGoalDao provideSleepDurationGoalDao(SleepAppDatabase database)
    {
        return database.getSleepDurationGoalDao();
    }
    
    // REFACTOR [21-03-9 2:16AM] -- I should add a qualifier to make it explicit that this is async.
    @Singleton
    @Provides
    public static Executor provideExecutorService()
    {
        return Executors.newFixedThreadPool(4);
    }
}
