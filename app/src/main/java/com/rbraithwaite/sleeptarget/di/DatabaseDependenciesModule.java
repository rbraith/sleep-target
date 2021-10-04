/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.di;

import android.content.Context;

import androidx.room.Room;

import com.rbraithwaite.sleeptarget.data.database.AppDatabase;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalDao;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagDao;

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
    public static AppDatabase provideAppDatabase(@ApplicationContext Context context)
    {
        return Room.databaseBuilder(context, AppDatabase.class, AppDatabase.NAME)
                .createFromAsset("databases/prepopulated.db")
                .build();
    }
    
    @Singleton
    @Provides
    public static SleepSessionDao provideSleepSessionDao(AppDatabase database)
    {
        return database.getSleepSessionDao();
    }
    
    @Singleton
    @Provides
    public static WakeTimeGoalDao provideWakeTimeGoalDao(AppDatabase database)
    {
        return database.getWakeTimeGoalDao();
    }
    
    @Singleton
    @Provides
    public static SleepDurationGoalDao provideSleepDurationGoalDao(AppDatabase database)
    {
        return database.getSleepDurationGoalDao();
    }
    
    @Singleton
    @Provides
    public static SleepInterruptionDao provideSleepInterruptionDao(AppDatabase database)
    {
        return database.getSleepInterruptionDao();
    }
    
    @Singleton
    @Provides
    public static TagDao provideTagDao(AppDatabase database)
    {
        return database.getTagDao();
    }
    
    // REFACTOR [21-03-9 2:16AM] -- I should add a qualifier to make it explicit that this is async.
    @Singleton
    @Provides
    public static Executor provideExecutorService()
    {
        return Executors.newFixedThreadPool(4);
    }
}
