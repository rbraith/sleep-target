package com.rbraithwaite.sleepapp.test_utils.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.data.current_goals.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionRepository;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;

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
            LiveData<Long> wakeTimeGoal,
            LiveData<SleepDurationGoalModel> sleepDurationGoal)
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
            final MutableLiveData<CurrentSessionModel> currentSession)
    {
        when(mockRepo.getCurrentSession()).thenReturn(currentSession);
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                Date newStart = invocation.getArgumentAt(0, Date.class);
                currentSession.setValue(new CurrentSessionModel(newStart));
                return null;
            }
        }).when(mockRepo).setCurrentSession(any(Date.class));
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                currentSession.setValue(new CurrentSessionModel(null));
                return null;
            }
        }).when(mockRepo).clearCurrentSession();
    }
}
