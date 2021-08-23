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

import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.core.repositories.TagRepository;
import com.rbraithwaite.sleeptarget.data.repositories.CurrentGoalsRepositoryImpl;
import com.rbraithwaite.sleeptarget.data.repositories.CurrentSessionRepositoryImpl;
import com.rbraithwaite.sleeptarget.data.repositories.SleepSessionRepositoryImpl;
import com.rbraithwaite.sleeptarget.data.repositories.TagRepositoryImpl;

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
