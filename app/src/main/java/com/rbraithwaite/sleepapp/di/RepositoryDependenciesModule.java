package com.rbraithwaite.sleepapp.di;

import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.TagRepository;
import com.rbraithwaite.sleepapp.data.repositories.CurrentGoalsRepositoryImpl;
import com.rbraithwaite.sleepapp.data.repositories.CurrentSessionRepositoryImpl;
import com.rbraithwaite.sleepapp.data.repositories.SleepSessionRepositoryImpl;
import com.rbraithwaite.sleepapp.data.repositories.TagRepositoryImpl;

import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

@Module
@InstallIn(ApplicationComponent.class)
public abstract class RepositoryDependenciesModule
{
//*********************************************************
// abstract
//*********************************************************

    @Binds
    public abstract SleepSessionRepository bindSleepSessionRepository(
            SleepSessionRepositoryImpl sleepSessionRepository);
    
    @Binds
    public abstract CurrentGoalsRepository bindCurrentGoalsRepository(
            CurrentGoalsRepositoryImpl currentGoalsRepository);
    
    @Binds
    public abstract CurrentSessionRepository bindCurrentSessionRepository(
            CurrentSessionRepositoryImpl currentSessionRepository);
    
    @Binds
    public abstract TagRepository bindTagRepository(TagRepositoryImpl tagRepository);
}
