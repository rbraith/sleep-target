package com.rbraithwaite.sleepapp.data.database;

import android.content.Context;

import androidx.room.Room;

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
    public static Executor provideExecutorService()
    {
        return Executors.newFixedThreadPool(4);
    }
}
