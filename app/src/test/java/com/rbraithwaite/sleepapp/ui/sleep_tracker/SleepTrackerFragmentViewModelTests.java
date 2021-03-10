package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.current_goals.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_goals.WakeTimeGoalModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionModel;
import com.rbraithwaite.sleepapp.data.current_session.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.MockRepositoryUtils;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFormatting;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;

import java.util.Collection;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepTrackerFragmentViewModel viewModel;
    //    private SleepAppRepository mockRepository;
    private SleepSessionRepository mockSleepSessionRepository;
    private CurrentSessionRepository mockCurrentSessionRepository;
    private CurrentGoalsRepository mockCurrentGoalsRepository;
    private DateTimeFormatter dateTimeFormatter;
    
    // TODO [20-11-18 8:23PM] -- try replacing uses of livedata synchronizers (TestUtils) with
    //  InstantTaskExecutorRule
    //  requires this dep in gradle: testImplementation "android.arch.core:core-testing:1.1.1"
    //  ---
    //  see:
    //  https://proandroiddev.com/how-to-unit-test-livedata-and-lifecycle-components-8a0af41c90d9
    //  https://developer.android.com/reference/androidx/arch/core/executor/testing
    //  /InstantTaskExecutorRule
    //  https://stackoverflow.com/a/57843898
//    @Rule
//    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        mockCurrentSessionRepository = mock(CurrentSessionRepository.class);
        mockCurrentGoalsRepository = mock(CurrentGoalsRepository.class);
        // REFACTOR [21-01-11 10:36PM] -- I need to be mocking DateTimeFormatter.
        dateTimeFormatter = new DateTimeFormatter();
        viewModel = new SleepTrackerFragmentViewModel(
                mockSleepSessionRepository,
                mockCurrentSessionRepository,
                mockCurrentGoalsRepository,
                dateTimeFormatter);
    }
    
    @After
    public void teardown()
    {
        mockSleepSessionRepository = null;
        mockCurrentSessionRepository = null;
        mockCurrentGoalsRepository = null;
        viewModel = null;
    }
    
    @Test
    public void getSleepDurationGoalText_getsSleepDurationGoalText()
    {
        SleepDurationGoalModel goal = new SleepDurationGoalModel(123);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(new MutableLiveData<>(
                goal));
        
        LiveData<String> goalText = viewModel.getSleepDurationGoalText();
        
        TestUtils.activateLocalLiveData(goalText);
        assertThat(goalText.getValue(),
                   is(equalTo(SleepGoalsFormatting.formatSleepDurationGoal(goal))));
    }
    
    @Test
    public void getWakeTimeText_getsWakeTimeText()
    {
        WakeTimeGoalModel model = TestUtils.ArbitraryData.getWakeTimeGoalModel();
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<>(model));
        
        LiveData<String> wakeTime = viewModel.getWakeTimeGoalText();
        
        TestUtils.activateLocalLiveData(wakeTime);
        assertThat(wakeTime.getValue(),
                   is(equalTo(dateTimeFormatter.formatTimeOfDay(model.asDate()))));
    }
    
    @Test
    public void getSessionStartTime_returnsTheStartTimeWhenThereIsASession()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSessionModel(testDate)));
        
        String expected = dateTimeFormatter.formatFullDate(testDate);
        
        LiveData<String> testStartTime = viewModel.getSessionStartTime();
        
        TestUtils.activateLocalLiveData(testStartTime);
        assertThat(testStartTime.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void getSessionStartTime_returnsNullWhenNoSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSessionModel(null)));
        
        LiveData<String> testStartTime = viewModel.getSessionStartTime();
        
        TestUtils.activateLocalLiveData(testStartTime);
        assertThat(testStartTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSessionDuration_isZeroWhenNoSession()
    {
        String expected = new DurationFormatter().formatDurationMillis(0);
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSessionModel(null)));
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.activateLocalLiveData(currentSessionDuration);
        
        assertThat(currentSessionDuration.getValue(), is(equalTo(expected)));
    }
    
    // SMELL [20-11-18 9:03PM] -- tests shouldn't be this complex.
    @Test
    public void getCurrentSessionDuration_updatesWhenInSession()
    {
        final MutableLiveData<CurrentSessionModel> mockCurrentSessionStart =
                new MutableLiveData<>(new CurrentSessionModel(null));
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(mockCurrentSessionStart);
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.LocalLiveDataSynchronizer<String> currentSessionDurationSynchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(currentSessionDuration);
        
        // start sleep session
        mockCurrentSessionStart.setValue(
                new CurrentSessionModel(TestUtils.ArbitraryData.getDate()));
        
        // REFACTOR [20-11-18 9:04PM] -- call this TestUtils.getShadowLooper(threadName).
        // It was necessary to manually manipulate the TickingLiveData's looper like this,
        // as it was not looping otherwise - it was not running the posted tick runnable, and
        // as a result mockCurrentSessionStart was never updated, meaning the sync() call below
        // would block forever.
        // ---
        // more information:
        // http://robolectric.org/blog/2019/06/04/paused-looper/
        // https://github.com/robolectric/robolectric/blob
        // /e197c5b9ed83dfd0d2ea6a74cf189f7b39463adc/robolectric/src/test/java/org/robolectric
        // /shadows/ShadowPausedLooperTest.java#L95
        // https://github.com/robolectric/robolectric/issues/1993
        // https://stackoverflow.com/a/39122515
        Collection<Looper> loopers = ShadowLooper.getAllLoopers();
        ShadowLooper shadowLooper = null;
        for (Looper looper : loopers) {
            if (looper.getThread().getName().equals(TickingLiveData.THREAD_NAME)) {
                shadowLooper = Shadow.extract(looper);
                break;
            }
        }
        assertThat(shadowLooper, is(notNullValue()));
        // no need to tick constantly, only need first runnable to update the value (first task)
        shadowLooper.runOneTask();
        shadowOf(Looper.getMainLooper()).idle(); // idk why this makes this test work but it does
        
        // assert current session duration reflects state of being in a session
        currentSessionDurationSynchronizer.sync();
        assertThat(currentSessionDuration.getValue(),
                   is(not(equalTo(new DurationFormatter().formatDurationMillis(0)))));
    }
    
    @Test
    public void inSleepSession_matchesSessionState()
    {
        // set up the repo behaviours
        final MutableLiveData<CurrentSessionModel> mockStartTime =
                new MutableLiveData<>(new CurrentSessionModel(null));
        MockRepositoryUtils.setupCurrentSessionRepositoryWithState(
                mockCurrentSessionRepository,
                mockStartTime);
        MockRepositoryUtils.setupCurrentGoalsRepositoryWithState(
                mockCurrentGoalsRepository,
                new MutableLiveData<WakeTimeGoalModel>(null),
                new MutableLiveData<>(SleepDurationGoalModel.createWithNoGoal()));
        
        // run the test
        LiveData<Boolean> inSleepSession = viewModel.inSleepSession();
        TestUtils.activateLocalLiveData(inSleepSession);
        assertThat(inSleepSession.getValue(), is(false));
        
        viewModel.startSleepSession();
        assertThat(inSleepSession.getValue(), is(true));
        
        viewModel.endSleepSession();
        assertThat(inSleepSession.getValue(), is(false));
    }
    
    @Test
    public void startSleepSession_startsSession()
    {
        viewModel.startSleepSession();
        verify(mockCurrentSessionRepository, times(1))
                .setCurrentSession(any(Date.class));
    }
    
    @Test
    public void endSleepSession_doesNothingIfNoSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession())
                .thenReturn(new MutableLiveData<>(new CurrentSessionModel(null)));
        
        // SMELL [20-11-15 12:45AM] -- Right now, endSleepSession() fails if inSleepSession()
        //  is not being observed (ie is not activated). Consider other solutions besides
        //  adding an internal observer.
        TestUtils.activateLocalLiveData(viewModel.inSleepSession());
        
        viewModel.endSleepSession();
        verify(mockSleepSessionRepository, times(0))
                .addSleepSession(any(SleepSessionModel.class));
    }
    
    @Test
    public void endSleepSession_recordsNewSession() throws InterruptedException
    {
        LiveData<CurrentSessionModel> startTime = new MutableLiveData<>(
                new CurrentSessionModel(TestUtils.ArbitraryData.getDate()));
        LiveData<WakeTimeGoalModel> wakeTimeGoal =
                new MutableLiveData<>(TestUtils.ArbitraryData.getWakeTimeGoalModel());
        LiveData<SleepDurationGoalModel> sleepDurationGoal =
                new MutableLiveData<>(TestUtils.ArbitraryData.getSleepDurationGoalModel());
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(startTime);
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(wakeTimeGoal);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(sleepDurationGoal);
        
        // This is needed for endSleepSession's inSleepSession() call to work
        TestUtils.activateLocalLiveData(viewModel.inSleepSession());
        
        viewModel.startSleepSession();
        viewModel.endSleepSession();
        
        ArgumentCaptor<SleepSessionModel> addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionModel.class);
        verify(mockSleepSessionRepository, times(1))
                .addSleepSession(addSleepSessionArg.capture());
        
        SleepSessionModel addSleepSession = addSleepSessionArg.getValue();
        assertThat(addSleepSession.getStart(), is(equalTo(startTime.getValue().getStart())));
        assertThat(addSleepSession.getWakeTimeGoal(),
                   is(equalTo(wakeTimeGoal.getValue().asDate())));
        assertThat(addSleepSession.getSleepDurationGoal(),
                   is(equalTo(sleepDurationGoal.getValue())));
    }

//*********************************************************
// private methods
//*********************************************************

    private void setupMockCurrentSessionRepositoryWithState(
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
