package com.rbraithwaite.sleepapp.test_utils.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;

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
