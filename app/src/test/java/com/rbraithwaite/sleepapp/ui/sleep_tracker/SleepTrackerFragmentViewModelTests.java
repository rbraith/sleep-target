package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_helpers.RobolectricUtils;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.MockRepositoryUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepTrackerFragmentViewModel viewModel;
    private SleepSessionRepository mockSleepSessionRepository;
    private CurrentSessionRepository mockCurrentSessionRepository;
    private CurrentGoalsRepository mockCurrentGoalsRepository;
    
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
        viewModel = new SleepTrackerFragmentViewModel(
                mockSleepSessionRepository,
                mockCurrentSessionRepository,
                mockCurrentGoalsRepository);
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
    public void getInterruptionsTotal_returnsCorrectValue()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession()));
        
        LiveData<String> interruptionsTotal = viewModel.getInterruptionsTotal();
        TestUtils.activateLocalLiveData(interruptionsTotal);
        
        assertThat(interruptionsTotal.getValue(), is(nullValue()));
        
        viewModel.startSleepSession();
        viewModel.interruptSleepSession();
        
        RobolectricUtils.getLooperForThread(TickingLiveData.THREAD_NAME).runOneTask();
        RobolectricUtils.idleMainLooper();
        
        // SMELL [21-07-14 11:47PM] -- This can be better.
        assertThat(interruptionsTotal.getValue(), is(not(nullValue())));
    }
    
    @Test
    public void isSleepSessionInterrupted_matchesInterruptionState()
    {
        // setup
        MutableLiveData<CurrentSession> currentSession = new MutableLiveData<>(
                TestUtils.ArbitraryData.getCurrentSession());
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(currentSession);
        
        LiveData<Boolean> isSleepSessionInterrupted = viewModel.isSleepSessionInterrupted();
        TestUtils.activateLocalLiveData(isSleepSessionInterrupted);
        
        // test
        assertThat(isSleepSessionInterrupted.getValue(), is(false));
        
        viewModel.startSleepSession();
        viewModel.interruptSleepSession();
        
        assertThat(isSleepSessionInterrupted.getValue(), is(true));
        
        viewModel.resumeSleepSession();
        
        assertThat(isSleepSessionInterrupted.getValue(), is(false));
    }
    
    @Test
    public void hasAnyGoal_returnsCorrectValue()
    {
        MutableLiveData<WakeTimeGoal> wakeTimeGoalLive = new MutableLiveData<>(null);
        MutableLiveData<SleepDurationGoal> sleepDurationGoalLive = new MutableLiveData<>(null);
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(wakeTimeGoalLive);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(sleepDurationGoalLive);
        
        LiveData<Boolean> hasAnyGoal = viewModel.hasAnyGoal();
        TestUtils.activateLocalLiveData(hasAnyGoal);
        
        // none
        assertThat(hasAnyGoal.getValue(), is(false));
        
        // only wake time
        wakeTimeGoalLive.setValue(new WakeTimeGoal(null, 1234));
        assertThat(hasAnyGoal.getValue(), is(true));
        
        // both
        sleepDurationGoalLive.setValue(new SleepDurationGoal(123));
        assertThat(hasAnyGoal.getValue(), is(true));
        
        // only sleep duration
        wakeTimeGoalLive.setValue(null);
        assertThat(hasAnyGoal.getValue(), is(true));
    }
    
    @Test
    public void discardSleepSession_discardsPersistedDataIfSessionIsStopped()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(TestUtils.ArbitraryData.getCurrentSession()));
        viewModel.stopSleepSession();
        viewModel.discardSleepSession();
        verify(mockCurrentSessionRepository, times(1)).clearCurrentSession();
    }
    
    @Test
    public void discardSleepSession_doesNothingIsSessionIsNotStopped()
    {
        viewModel.discardSleepSession();
        verify(mockCurrentSessionRepository, times(0)).clearCurrentSession();
    }
    
    @Test
    public void getStoppedSessionData_usesLocalValues()
    {
        CurrentSession currentSession = new CurrentSession(
                TestUtils.ArbitraryData.getDate(),
                "persisted comment",
                new Mood(1),
                Arrays.asList(1, 2));
        currentSession.interrupt(new TimeUtils());
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(currentSession));
        
        // local tags
        List<TagUiData> tags = Arrays.asList(
                new TagUiData(3, "meh"),
                new TagUiData(4, "meh"));
        List<Integer> expectedTagIds = tags.stream()
                .map(tagUiData -> tagUiData.tagId)
                .collect(Collectors.toList());
        viewModel.setLocalSelectedTags(tags);
        
        // local comment
        String expectedComment = "local comment";
        viewModel.setLocalAdditionalComments(expectedComment);
        
        // local mood
        MoodUiData expectedMood = new MoodUiData(7);
        viewModel.setLocalMood(expectedMood);
        
        // local interruption reason
        String expectedInterruptionReason = "local interruption reason";
        viewModel.setLocalInterruptionReason(expectedInterruptionReason);
        
        // SUT
        viewModel.stopSleepSession();
        StoppedSessionData stoppedSession = viewModel.getStoppedSessionData();
        
        // verify
        assertThat(stoppedSession.currentSessionSnapshot.mood,
                   is(equalTo(ConvertMood.fromUiData(expectedMood))));
        assertThat(stoppedSession.currentSessionSnapshot.additionalComments,
                   is(equalTo(expectedComment)));
        assertThat(stoppedSession.currentSessionSnapshot.selectedTagIds,
                   is(equalTo(expectedTagIds)));
        
        assertThat(stoppedSession.currentSessionSnapshot.interruptions.size(), is(1));
        assertThat(stoppedSession.currentSessionSnapshot.interruptions.get(0).getReason(),
                   is(equalTo(expectedInterruptionReason)));
    }
    
    @Test
    public void getPersistedSelectedTagIds_returnsCorrectData()
    {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                // REFACTOR [21-04-19 7:48PM] -- add CurrentSession(selected tag ids) ctor.
                new MutableLiveData<>(new CurrentSession(
                        null,
                        null,
                        null,
                        expected)));
        
        LiveData<List<Integer>> selectedTagIds = viewModel.getPersistedSelectedTagIds();
        
        TestUtils.activateLocalLiveData(selectedTagIds);
        assertThat(selectedTagIds.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void clearLocalMood_clearsMood()
    {
        CurrentSession testCurrentSession = TestUtils.ArbitraryData.getCurrentSession();
        testCurrentSession.setMood(new Mood(1));
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(testCurrentSession));
        
        viewModel.setLocalMood(new MoodUiData(0));
        // SUT
        viewModel.clearLocalMood();
        
        viewModel.persistLocalValues();
        
        assertOnPersistingCurrentSession(currentSession -> {
            assertThat(currentSession.getMood(), is(nullValue()));
        });
    }
    
    @Test
    public void getPersistedInterruptionReason_returnsCorrectValues()
    {
        TimeUtils timeUtils = new TimeUtils();
        
        CurrentSession currentSession = new CurrentSession(timeUtils.getNow());
        currentSession.interrupt(timeUtils); // begin interrupting so that setting the reason works
        
        String expectedReason = "reason";
        currentSession.setInterruptionReason(expectedReason);
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                currentSession));
        
        LiveData<String> persistedReason = viewModel.getPersistedInterruptionReason();
        
        TestUtils.activateLocalLiveData(persistedReason);
        assertThat(persistedReason.getValue(), is(equalTo(expectedReason)));
    }
    
    @Test
    public void getPersistedMood_returnsPersistedMood()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                // REFACTOR [21-04-19 7:48PM] -- add CurrentSession(Mood) ctor.
                new MutableLiveData<>(new CurrentSession(
                        null,
                        null,
                        new Mood(0),
                        null)));
        
        LiveData<MoodUiData> moodUiData = viewModel.getPersistedMood();
        
        TestUtils.activateLocalLiveData(moodUiData);
        assertThat(moodUiData.getValue().asIndex(), is(equalTo(0)));
    }
    
    @Test
    public void persist_persistsCorrectData()
    {
        Date expectedStart = TestUtils.ArbitraryData.getDate();
        String expectedComments = "test";
        MoodUiData expectedMood = new MoodUiData(1);
        List<TagUiData> expectedSelectedTags = Arrays.asList(
                new TagUiData(3, "what"));
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(expectedStart)));
        
        viewModel.setLocalAdditionalComments(expectedComments);
        viewModel.setLocalMood(expectedMood);
        viewModel.setLocalSelectedTags(expectedSelectedTags);
        
        viewModel.persistLocalValues();
        
        assertOnPersistingCurrentSession(currentSession -> {
            assertThat(currentSession.getStart(), is(equalTo(expectedStart)));
            assertThat(currentSession.getAdditionalComments(), is(equalTo(expectedComments)));
            assertThat(currentSession.getMood().asIndex(), is(1));
            assertThat(currentSession.getSelectedTagIds(), is(equalTo(
                    expectedSelectedTags.stream()
                            .map(tagUiData -> tagUiData.tagId)
                            .collect(Collectors.toList()))));
        });
    }
    
    @Test
    public void getSleepDurationGoalText_getsSleepDurationGoalText()
    {
        SleepDurationGoal goal = new SleepDurationGoal(123);
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
        WakeTimeGoal model = TestUtils.ArbitraryData.getWakeTimeGoal();
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<>(model));
        
        LiveData<String> wakeTime = viewModel.getWakeTimeGoalText();
        
        TestUtils.activateLocalLiveData(wakeTime);
        assertThat(wakeTime.getValue(),
                   is(equalTo(SleepTrackerFormatting.formatWakeTimeGoal(model))));
    }
    
    @Test
    public void getSessionStartTime_returnsTheStartTimeWhenThereIsASession()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(testDate)));
        
        String expected = SleepTrackerFormatting.formatSessionStartTime(testDate);
        
        LiveData<String> testStartTime = viewModel.getSessionStartTime();
        
        TestUtils.activateLocalLiveData(testStartTime);
        assertThat(testStartTime.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void getSessionStartTime_returnsNullWhenNoSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(null)));
        
        LiveData<String> testStartTime = viewModel.getSessionStartTime();
        
        TestUtils.activateLocalLiveData(testStartTime);
        assertThat(testStartTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void getOngoingInterruptionDuration_isErrorWhenNotInterrupted()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession()));
        
        LiveData<String> interruptionDuration = viewModel.getOngoingInterruptionDuration();
        TestUtils.activateLocalLiveData(interruptionDuration);
        
        // REFACTOR [21-07-12 9:41PM] -- hardcoded string.
        assertThat(interruptionDuration.getValue(), is(equalTo("Error")));
    }
    
    @Test
    public void getOngoingInterruptionDuration_updatesWhenInterrupted()
    {
        // setup
        MutableLiveData<CurrentSession> currentSessionLive =
                new MutableLiveData<>(new CurrentSession());
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(currentSessionLive);
        
        Date fakeNow = new GregorianCalendar(2021, 6, 11).getTime();
        viewModel.setTimeUtils(TestUtils.timeUtilsFixedAt(fakeNow));
        
        // SUT
        LiveData<String> interruptionDuration = viewModel.getOngoingInterruptionDuration();
        TestUtils.activateLocalLiveData(interruptionDuration);
        
        viewModel.startSleepSession();
        viewModel.interruptSleepSession();
        
        // verify
        // update the ticking duration text once
        RobolectricUtils.getLooperForThread(TickingLiveData.THREAD_NAME).runOneTask();
        RobolectricUtils.idleMainLooper(); // idk why this makes this test work but it does
        
        assertThat(interruptionDuration.getValue(),
                   is(equalTo(SleepTrackerFormatting.formatDuration(0))));
    }
    
    @Test
    public void getCurrentSessionDuration_isErrorWhenNoSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession()));
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.activateLocalLiveData(currentSessionDuration);
        
        // REFACTOR [21-07-11 3:06AM] -- hardcoded string.
        assertThat(currentSessionDuration.getValue(), is(equalTo("Error")));
    }
    
    @Test
    public void getCurrentSessionDuration_isPausedWhenSessionIsInterrupted()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession()));
        
        GregorianCalendar cal = new GregorianCalendar();
        Date now = cal.getTime();
        viewModel.setTimeUtils(TestUtils.timeUtilsFixedAt(now));
        
        viewModel.startSleepSession();
        
        LiveData<String> currentDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.activateLocalLiveData(currentDuration);
        // have the duration text update at least once
        RobolectricUtils.getLooperForThread(TickingLiveData.THREAD_NAME).idle();
        RobolectricUtils.idleMainLooper();
        
        viewModel.interruptSleepSession();
        
        // if the session weren't paused, this would cause the duration ticker to output 5min ahead
        // of where the session started. Meanwhile we are expecting it to output 0.
        cal.add(Calendar.MINUTE, 5);
        Date updated = cal.getTime();
        viewModel.setTimeUtils(TestUtils.timeUtilsFixedAt(updated));
        
        assertThat(currentDuration.getValue(),
                   is(equalTo(SleepTrackerFormatting.formatDuration(0))));
    }
    
    @Test
    public void getCurrentSessionDuration_updatesWhenInSession()
    {
        // setup
        MutableLiveData<CurrentSession> currentSessionLive =
                new MutableLiveData<>(new CurrentSession());
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(currentSessionLive);
        
        Date fakeNow = new GregorianCalendar(2021, 6, 11).getTime();
        TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return fakeNow;
            }
        };
        viewModel.setTimeUtils(stubTimeUtils);
        
        // SUT
        LiveData<String> currentDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.activateLocalLiveData(currentDuration);
        
        // REFACTOR [21-07-11 2:53AM] -- hardcoded string.
        assertThat(currentDuration.getValue(), is(equalTo("Error")));
        
        viewModel.startSleepSession();
        
        // verify
        // update the ticking duration text once
        RobolectricUtils.getLooperForThread(TickingLiveData.THREAD_NAME).runOneTask();
        RobolectricUtils.idleMainLooper(); // idk why this makes this test work but it does
        
        assertThat(currentDuration.getValue(),
                   is(equalTo(SleepTrackerFormatting.formatDuration(0))));
    }
    
    @Test
    public void inSleepSession_matchesSessionState()
    {
        // set up the repo behaviours
        final MutableLiveData<CurrentSession> mockStartTime =
                new MutableLiveData<>(new CurrentSession());
        MockRepositoryUtils.setupCurrentSessionRepositoryWithState(
                mockCurrentSessionRepository,
                mockStartTime);
        MockRepositoryUtils.setupCurrentGoalsRepositoryWithState(
                mockCurrentGoalsRepository,
                new MutableLiveData<>(null),
                new MutableLiveData<>(SleepDurationGoal.createWithNoGoal()));
        
        // run the test
        LiveData<Boolean> inSleepSession = viewModel.inSleepSession();
        TestUtils.activateLocalLiveData(inSleepSession);
        assertThat(inSleepSession.getValue(), is(false));
        
        viewModel.startSleepSession();
        assertThat(inSleepSession.getValue(), is(true));
        
        viewModel.stopSleepSession();
        assertThat(inSleepSession.getValue(), is(false));
    }
    
    @Test
    public void keepStoppedSession_recordsNewSession()
    {
        CurrentSession currentSession = TestUtils.ArbitraryData.getCurrentSession();
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                currentSession));
        
        viewModel.startSleepSession();
        viewModel.stopSleepSession();
        
        PostSleepData postSleepData = new PostSleepData(4.5f);
        viewModel.setPostSleepData(postSleepData);
        // SUT
        viewModel.keepStoppedSession(viewModel.getStoppedSessionData());
        
        assertOnRepoAddSleepSessionArg(arg -> {
            assertThat(arg.start, is(equalTo(currentSession.getStart())));
            assertThat(arg.additionalComments, is(equalTo(currentSession.getAdditionalComments())));
            assertThat(arg.mood, is(equalTo(currentSession.getMood())));
            assertThat(arg.tagIds, is(equalTo(currentSession.getSelectedTagIds())));
            assertThat(arg.rating, is(equalTo(postSleepData.rating)));
            // TODO [21-05-9 11:18PM] -- Right now its awkward to check the end & duration values of
            //  the arg, since those come from the current session *snapshot*, which is buried in
            //  the view model.
        });
    }

//*********************************************************
// private methods
//*********************************************************

    private void assertOnPersistingCurrentSession(AssertOn<CurrentSession> assertion)
    {
        ArgumentCaptor<CurrentSession> arg = ArgumentCaptor.forClass(CurrentSession.class);
        verify(mockCurrentSessionRepository, times(1)).setCurrentSession(arg.capture());
        
        CurrentSession currentSession = arg.getValue();
        
        assertion.assertOn(currentSession);
    }
    
    private void assertOnRepoAddSleepSessionArg(AssertOn<SleepSessionRepository.NewSleepSessionData> assertion)
    {
        ArgumentCaptor<SleepSessionRepository.NewSleepSessionData> arg =
                ArgumentCaptor.forClass(SleepSessionRepository.NewSleepSessionData.class);
        verify(mockSleepSessionRepository, times(1))
                .addSleepSession(arg.capture());
        
        SleepSessionRepository.NewSleepSessionData newSleepSessionData = arg.getValue();
        assertion.assertOn(newSleepSessionData);
    }
}
