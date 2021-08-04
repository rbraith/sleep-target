package com.rbraithwaite.sleepapp.di;

import com.rbraithwaite.sleepapp.core.models.overlap_checker.SleepSessionOverlapChecker;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;

@Module
@InstallIn(ApplicationComponent.class)
public class ModelDependenciesModule
{
//*********************************************************
// api
//*********************************************************

    @Provides
    public static SleepSessionOverlapChecker provideSleepSessionOverlapChecker(
            SleepSessionRepository sleepSessionRepository)
    {
        return new SleepSessionOverlapChecker(sleepSessionRepository);
    }
}
