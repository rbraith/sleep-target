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

package com.rbraithwaite.sleeptarget.test_utils.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;

import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class MockRepositoryUtils
{
//*********************************************************
// constructors
//*********************************************************

    private MockRepositoryUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void setupCurrentGoalsRepositoryWithState(
            CurrentGoalsRepository mockRepo,
            LiveData<WakeTimeGoal> wakeTimeGoal,
            LiveData<SleepDurationGoal> sleepDurationGoal)
    {
        when(mockRepo.getWakeTimeGoal()).thenReturn(wakeTimeGoal);
        when(mockRepo.getSleepDurationGoal()).thenReturn(sleepDurationGoal);
    }
    
    /**
     * Sets up the mock repo so that the provided MutableLiveData is updated when repo methods are
     * called.
     */
    public static void setupCurrentSessionRepositoryWithState(
            CurrentSessionRepository mockRepo,
            final MutableLiveData<CurrentSession> currentSession)
    {
        when(mockRepo.getCurrentSession()).thenReturn(currentSession);
        doAnswer((Answer<Void>) invocation -> {
            CurrentSession newCurrentSession =
                    invocation.getArgumentAt(0, CurrentSession.class);
            currentSession.setValue(newCurrentSession);
            return null;
        }).when(mockRepo).setCurrentSession(any(CurrentSession.class));
        doAnswer((Answer<Void>) invocation -> {
            currentSession.setValue(new CurrentSession());
            return null;
        }).when(mockRepo).clearCurrentSession();
    }
}
