package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.os.Looper;

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
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.MockRepositoryUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.sleep_goals.SleepGoalsFormatting;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.CurrentSessionUiData;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
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
    public void OnKeepSessionListener_hears_keepStoppedSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(new TimeUtils())));
        
        TestUtils.DoubleRef<Boolean> listenerWasCalled = new TestUtils.DoubleRef<>(false);
        viewModel.setOnKeepSessionListener(() -> listenerWasCalled.ref = true);
        
        viewModel.startSleepSession();
        viewModel.stopSleepSession();
        viewModel.keepStoppedSession(null);
        
        assertThat(listenerWasCalled.ref, is(true));
    }
    
    @Test
    public void getStoppedSessionData_usesLocalValues()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                new CurrentSession(
                        TestUtils.ArbitraryData.getDate(),
                        "persisted comment",
                        Mood.fromIndex(1),
                        Arrays.asList(1, 2),
                        new TimeUtils())));
        
        List<TagUiData> tags = Arrays.asList(
                new TagUiData(3, "meh"),
                new TagUiData(4, "meh"));
        List<Integer> expectedTagIds = tags.stream()
                .map(tagUiData -> tagUiData.tagId)
                .collect(Collectors.toList());
        viewModel.setLocalSelectedTags(tags);
        
        String expectedComment = "local comment";
        viewModel.setLocalAdditionalComments(expectedComment);
        
        MoodUiData expectedMood = new MoodUiData(7);
        viewModel.setLocalMood(expectedMood);
        
        // SUT
        viewModel.stopSleepSession();
        CurrentSessionUiData uiData = viewModel.getStoppedSessionData();
        
        // verify
        assertThat(uiData.mood, is(equalTo(expectedMood)));
        assertThat(uiData.additionalComments, is(equalTo(expectedComment)));
        assertThat(uiData.tagIds, is(equalTo(expectedTagIds)));
    }
    
    @Test
    public void stopCurrentSession_clearsSessionDataWithoutClearingPersistedData()
    {
        CurrentSession persistedCurrentSession = TestUtils.ArbitraryData.getCurrentSession();
        MutableLiveData<CurrentSession> currentSessionLive =
                new MutableLiveData<>(persistedCurrentSession);
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(currentSessionLive);
        
        LiveData<String> comments = viewModel.getPersistedAdditionalComments();
        TestUtils.activateLocalLiveData(comments);
        
        assertThat(comments.getValue(),
                   is(equalTo(persistedCurrentSession.getAdditionalComments())));
        
        // SUT
        viewModel.stopSleepSession();
        
        assertThat(comments.getValue(), is(nullValue()));
        verify(mockCurrentSessionRepository, times(0)).clearCurrentSession();
    }
    
    @Test
    public void getPostSleepData_reflects_setPostSleepData()
    {
        PostSleepData expected = new PostSleepData(2.5f);
        viewModel.setPostSleepData(expected);
        assertThat(viewModel.getPostSleepData(), is(equalTo(expected)));
    }
    
    @Test
    public void getInitialTagData_getsTagDataFromCurrentSession()
    {
        List<Integer> expectedIdsList = Arrays.asList(1, 2, 3);
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                new CurrentSession(null, null, null, expectedIdsList, new TimeUtils())));
        
        LiveData<SleepTrackerFragmentViewModel.InitialTagData> initialTagData =
                viewModel.getInitialTagData();
        TestUtils.activateLocalLiveData(initialTagData);
        
        assertThat(initialTagData.getValue().selectedTagIds, is(equalTo(expectedIdsList)));
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
                        expected,
                        new TimeUtils())));
        
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
    public void getPersistedMood_returnsPersistedMood()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                // REFACTOR [21-04-19 7:48PM] -- add CurrentSession(Mood) ctor.
                new MutableLiveData<>(new CurrentSession(
                        null,
                        null,
                        new Mood(0),
                        null,
                        new TimeUtils())));
        
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
                new MutableLiveData<>(new CurrentSession(expectedStart, new TimeUtils())));
        
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
        WakeTimeGoal model = TestUtils.ArbitraryData.getWakeTimeGoalModel();
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
                new MutableLiveData<>(new CurrentSession(testDate, new TimeUtils())));
        
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
    public void getCurrentSessionDuration_isZeroWhenNoSession()
    {
        String expected = SleepTrackerFormatting.formatDuration(0);
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(null)));
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.activateLocalLiveData(currentSessionDuration);
        
        assertThat(currentSessionDuration.getValue(), is(equalTo(expected)));
    }
    
    // SMELL [20-11-18 9:03PM] -- tests shouldn't be this complex.
    @Test
    public void getCurrentSessionDuration_updatesWhenInSession()
    {
        final MutableLiveData<CurrentSession> mockCurrentSessionStart =
                new MutableLiveData<>(new CurrentSession(null));
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(mockCurrentSessionStart);
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration();
        TestUtils.LocalLiveDataSynchronizer<String> currentSessionDurationSynchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(currentSessionDuration);
        
        // start sleep session
        mockCurrentSessionStart.setValue(
                new CurrentSession(TestUtils.ArbitraryData.getDate(), new TimeUtils()));
        
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
                   is(not(equalTo(SleepTrackerFormatting.formatDuration(0)))));
    }
    
    @Test
    public void inSleepSession_matchesSessionState()
    {
        // set up the repo behaviours
        final MutableLiveData<CurrentSession> mockStartTime =
                new MutableLiveData<>(new CurrentSession(new TimeUtils()));
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
    public void startSleepSession_startsSession()
    {
        // setup
        String expectedComments = "test!";
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                new CurrentSession(
                        new GregorianCalendar(2021, 2, 30).getTime(),
                        expectedComments,
                        new TimeUtils())));
        TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return TestUtils.ArbitraryData.getDate();
            }
        };
        viewModel.setTimeUtils(stubTimeUtils);
        
        // SUT
        viewModel.startSleepSession();
        
        // verify
        ArgumentCaptor<CurrentSession> arg = ArgumentCaptor.forClass(CurrentSession.class);
        verify(mockCurrentSessionRepository, times(1))
                .setCurrentSession(arg.capture());
        
        CurrentSession currentSession = arg.getValue();
        assertThat(currentSession.getStart(), is(equalTo(stubTimeUtils.getNow())));
        assertThat(currentSession.getAdditionalComments(), is(equalTo(expectedComments)));
    }
    
    @Test
    public void keepStoppedSession_doesNothingIfNoSession()
    {
        viewModel.keepStoppedSession(null);
        verify(mockSleepSessionRepository, times(0))
                .addSleepSession(any());
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
        // SUT
        viewModel.keepStoppedSession(postSleepData);
        
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
