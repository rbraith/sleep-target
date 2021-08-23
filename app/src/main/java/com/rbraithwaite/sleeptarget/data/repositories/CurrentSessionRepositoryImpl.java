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

package com.rbraithwaite.sleeptarget.data.repositories;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleeptarget.data.convert.ConvertCurrentSession;
import com.rbraithwaite.sleeptarget.data.prefs.CurrentSessionPrefs;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CurrentSessionRepositoryImpl
        implements CurrentSessionRepository
{
//*********************************************************
// private constants
//*********************************************************

    private final CurrentSessionPrefs mCurrentSessionPrefs;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public CurrentSessionRepositoryImpl(CurrentSessionPrefs currentSessionPrefs)
    {
        mCurrentSessionPrefs = currentSessionPrefs;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public void clearCurrentSession()
    {
        mCurrentSessionPrefs.clearCurrentSession();
    }
    
    @Override
    public LiveData<CurrentSession> getCurrentSession()
    {
        return Transformations.map(
                mCurrentSessionPrefs.getCurrentSession(),
                ConvertCurrentSession::fromPrefsData);
    }
    
    @Override
    public void setCurrentSession(@NonNull CurrentSession currentSession)
    {
        // REFACTOR [21-03-29 11:15PM] -- should the asynchronicity be here instead of down in the
        //  prefs? Or even higher in the view model?
        mCurrentSessionPrefs.setCurrentSession(ConvertCurrentSession.toPrefsData(currentSession));
    }
}
