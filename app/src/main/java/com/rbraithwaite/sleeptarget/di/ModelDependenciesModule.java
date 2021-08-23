/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

import com.rbraithwaite.sleeptarget.core.models.overlap_checker.SleepSessionOverlapChecker;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;

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
