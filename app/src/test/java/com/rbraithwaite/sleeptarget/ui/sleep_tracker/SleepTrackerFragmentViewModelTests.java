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
package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentSessionRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.test_helpers.RobolectricUtils;
import com.rbraithwaite.sleeptarget.test_utils.TestEqualities;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.CurrentSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.SleepGoalsFormatting;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.LiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.SimpleLiveDataEvent;
import com.rbraithwaite.sleeptarget.utils.TickingLiveData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

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

import static com.rbraithwaite.sleeptarget.test_utils.LiveDataTestUtils.activateLocally;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aCurrentSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aPostSleepData;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepDurationGoal;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aStoppedSessionData;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aWakeTimeGoal;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
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
    private ArgumentCaptor<SleepSessionRepository.NewSleepSessionData> addSleepSessionArg;
    
    private CurrentSessionRepository mockCurrentSessionRepository;
    private ArgumentCaptor<CurrentSession> setCurrentSessionArg;
    
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
        addSleepSessionArg = null;
        
        mockCurrentSessionRepository = null;
        setCurrentSessionArg = null;
        
        mockCurrentGoalsRepository = null;
        
        viewModel = null;
    }
    
    @Test
    public void onInterruptionRecorded_triggersAfterRecordingAnInterruption()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<SimpleLiveDataEvent> interruptionRecorded =
                activateLocally(viewModel.onInterruptionRecorded());
        
        assertThat(interruptionRecorded.getValue(), is(nullValue()));
        
        // start recording interruption
        viewModel.clickTrackingButton();
        viewModel.clickInterruptionButton();
        
        assertThat(interruptionRecorded.getValue(), is(nullValue()));
        
        // record the interruption
        viewModel.clickInterruptionButton();
        
        assertThat(interruptionRecorded.getValue(), is(notNullValue()));
    }
    
    @Test
    public void getInterruptButtonText_returnsCorrectText()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<Integer> textId = activateLocally(viewModel.getInterruptButtonText());
        
        viewModel.clickTrackingButton();
        
        assertThat(textId.getValue(), is(R.string.tracker_interrupt_btn_interrupt));
        
        viewModel.clickInterruptionButton();
        
        assertThat(textId.getValue(), is(R.string.tracker_interrupt_btn_resume));
        
        viewModel.clickInterruptionButton();
        
        assertThat(textId.getValue(), is(R.string.tracker_interrupt_btn_interrupt));
    }
    
    @Test
    public void getSleepTrackingButtonText_returnsCorrectText()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<Integer> textId = activateLocally(viewModel.getSleepTrackingButtonText());
        
        assertThat(textId.getValue(), is(R.string.sleep_tracker_button_start));
        
        viewModel.clickTrackingButton();
        
        assertThat(textId.getValue(), is(R.string.sleep_tracker_button_stop));
    }
    
    @Test
    public void getInterruptionsVisibility_returnsCorrectVisibility()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<Integer> visibility = activateLocally(viewModel.getInterruptionsVisibility());
        
        assertThat(visibility.getValue(), is(View.GONE));
        
        // start tracking a session
        viewModel.clickTrackingButton();
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
        
        // stop tracking a session
        discardCurrentSession();
        
        assertThat(visibility.getValue(), is(View.GONE));
    }
    
    @Test
    public void getInterruptionsTotalVisibility_returnCorrectVisibility()
    {
        // When there is no current session, the interruption total is GONE
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<Integer> visibility = activateLocally(viewModel.getInterruptionsTotalVisibility());
        
        assertThat(visibility.getValue(), is(View.GONE));
        
        // When there is a current session, but no interruptions, the interruption total is GONE
        viewModel.clickTrackingButton();
        
        assertThat(visibility.getValue(), is(View.GONE));
        
        // When there is a current session that has interruptions, the interruption total
        // is VISIBLE.
        viewModel.clickInterruptionButton();
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
        
        // now no current interruption, but still in a current session that has interruptions
        viewModel.clickInterruptionButton();
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
    }
    
    @Test
    public void clickTrackingButton_tracksSleep()
    {
        CurrentSessionBuilder currentSession = aCurrentSession().notStarted().withNoInterruptions();
        giveViewModelInitialCurrentSession(currentSession);
        DateBuilder sessionStart = aDate();
        DateBuilder sessionEnd = sessionStart.copy().addDays(1);
        viewModel.setTimeUtils(TestUtils.timeUtilsWithNowSequence(
                sessionStart, sessionEnd));
        
        LiveData<LiveDataEvent<StoppedSessionData>> navToPostSleepEvent =
                activateLocally(viewModel.onNavToPostSleep());
        assertThat(navToPostSleepEvent.getValue(), is(nullValue()));
        
        // first click begins tracking a sleep session
        viewModel.clickTrackingButton();
        currentSession.withStart(sessionStart); // manually update the test current session
        
        // second click triggers 'navigate to post-sleep' event with correct data
        viewModel.clickTrackingButton();
        
        assertThat(navToPostSleepEvent.getValue(), is(not(nullValue())));
        StoppedSessionData stoppedSession = navToPostSleepEvent.getValue().getExtra();
        assertThat(stoppedSession.postSleepData, is(nullValue()));
        assertThat(TestEqualities.CurrentSession_equals_CurrentSessionSnapshot(
                valueOf(currentSession),
                stoppedSession.currentSessionSnapshot
        ), is(true));
    }
    
    @Test
    public void getNoGoalsMessageVisibility_returnsCorrectVisibility()
    {
        // REFACTOR [21-10-20 12:28AM] -- this duplicates hasAnyGoal_returnsCorrectValues.
        MutableLiveData<WakeTimeGoal> wakeTimeGoalLive = new MutableLiveData<>(null);
        MutableLiveData<SleepDurationGoal> sleepDurationGoalLive = new MutableLiveData<>(null);
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(wakeTimeGoalLive);
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(sleepDurationGoalLive);
        
        // SUT
        LiveData<Integer> visibility = activateLocally(viewModel::getNoGoalsMessageVisibility);
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
        
        wakeTimeGoalLive.setValue(valueOf(aWakeTimeGoal()));
        
        assertThat(visibility.getValue(), is(View.GONE));
    }
    
    @Test
    public void getSleepDurationGoalVisibility_is_VISIBLE_whenThereIsGoal()
    {
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(new MutableLiveData<>(
                valueOf(aSleepDurationGoal())));
        
        LiveData<Integer> visibility = activateLocally(viewModel::getSleepDurationGoalVisibility);
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
    }
    
    @Test
    public void getSleepDurationGoalVisibility_is_GONE_whenNoGoal()
    {
        when(mockCurrentGoalsRepository.getSleepDurationGoal()).thenReturn(new MutableLiveData<>(
                valueOf(aSleepDurationGoal().withNoGoalSet())));
        
        LiveData<Integer> visibility = activateLocally(viewModel::getSleepDurationGoalVisibility);
        
        assertThat(visibility.getValue(), is(View.GONE));
    }
    
    @Test
    public void getWakeTimeGoalVisibility_is_GONE_whenNoGoal()
    {
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<>(
                valueOf(aWakeTimeGoal().withNoGoalSet())));
        
        LiveData<Integer> visibility = activateLocally(viewModel::getWakeTimeGoalVisibility);
        
        assertThat(visibility.getValue(), is(View.GONE));
    }
    
    @Test
    public void getWakeTimeGoalVisibility_is_VISIBLE_whenThereIsGoal()
    {
        when(mockCurrentGoalsRepository.getWakeTimeGoal()).thenReturn(new MutableLiveData<>(
                valueOf(aWakeTimeGoal())));
        
        LiveData<Integer> visibility = activateLocally(viewModel::getWakeTimeGoalVisibility);
        
        assertThat(visibility.getValue(), is(View.VISIBLE));
    }
    
    @Test
    public void handleAnyReturnFromPostSleep_keepsSession()
    {
        // setup
        // existing session is required first
        giveViewModelInitialCurrentSession();
        LiveData<Boolean> inSleepSession = activateLocally(viewModel::inSleepSession);
        
        PostSleepViewModel stubPostSleepViewModel = mock(PostSleepViewModel.class);
        
        when(stubPostSleepViewModel.consumeAction()).thenReturn(PostSleepViewModel.KEEP);
        
        StoppedSessionData stoppedSessionData = valueOf(aStoppedSessionData()
                                                                .with(TestUtils.timeUtilsFixedAt(
                                                                        aDate().now()
                                                                                .subtractDays(1)))
                                                                .with(aPostSleepData())
                                                                .with(aCurrentSession()));
        when(stubPostSleepViewModel.consumeData()).thenReturn(stoppedSessionData);
        
        // SUT
        viewModel.handleAnyReturnFromPostSleep(stubPostSleepViewModel);
        
        // verify
        addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionRepository.NewSleepSessionData.class);
        verify(mockSleepSessionRepository).addSleepSession(addSleepSessionArg.capture());
        
        assertThat("reason", TestEqualities.StoppedSessionData_equals_NewSleepSessionData(
                stoppedSessionData,
                addSleepSessionArg.getValue()));
        
        // session is also cleared
        assertThat(inSleepSession.getValue(), is(false));
    }
    
    @Test
    public void handleAnyReturnFromPostSleep_discardsSession()
    {
        // setup
        // existing session is required first
        giveViewModelInitialCurrentSession();
        LiveData<Boolean> inSleepSession = activateLocally(viewModel::inSleepSession);
        assertThat(inSleepSession.getValue(), is(true));
        
        PostSleepViewModel mockPostSleepViewModel = mock(PostSleepViewModel.class);
        
        when(mockPostSleepViewModel.consumeAction()).thenReturn(PostSleepViewModel.DISCARD);
        
        // SUT
        viewModel.handleAnyReturnFromPostSleep(mockPostSleepViewModel);
        
        // verify
        verify(mockCurrentSessionRepository).clearCurrentSession();
        verify(mockPostSleepViewModel).discardData();
        
        assertThat(inSleepSession.getValue(), is(false));
    }
    
    @Test
    public void handleAnyReturnFromPostSleep_doesNothingIfNoAction()
    {
        // setup
        // existing session is required first
        giveViewModelInitialCurrentSession();
        LiveData<Boolean> inSleepSession = activateLocally(viewModel::inSleepSession);
        
        PostSleepViewModel stubPostSleepViewModel = mock(PostSleepViewModel.class);
        
        when(stubPostSleepViewModel.consumeAction()).thenReturn(PostSleepViewModel.NO_ACTION);
        
        viewModel.handleAnyReturnFromPostSleep(stubPostSleepViewModel);
        
        assertThat(inSleepSession.getValue(), is(true));
    }
    
    @Test(expected = RuntimeException.class)
    public void handleAnyReturnFromPostSleep_throwsIfInvalidAction()
    {
        PostSleepViewModel stubPostSleepViewModel = mock(PostSleepViewModel.class);
        
        int invalidAction = 1234;
        when(stubPostSleepViewModel.consumeAction()).thenReturn(invalidAction);
        
        viewModel.handleAnyReturnFromPostSleep(stubPostSleepViewModel);
    }
    
    @Test(expected = NullPointerException.class)
    public void handleAnyReturnFromPostSleep_requiresNotNull()
    {
        viewModel.handleAnyReturnFromPostSleep(null);
    }
    
    @Test
    public void getInterruptionsTotal_returnsCorrectValue()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession()));
        
        LiveData<String> interruptionsTotal = viewModel.getInterruptionsTotalText();
        TestUtils.activateLocalLiveData(interruptionsTotal);
        
        assertThat(interruptionsTotal.getValue(), is(nullValue()));
        
        viewModel.clickTrackingButton();
        viewModel.clickInterruptionButton();
        
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
        
        viewModel.clickTrackingButton();
        viewModel.clickInterruptionButton();
        
        assertThat(isSleepSessionInterrupted.getValue(), is(true));
        
        viewModel.clickInterruptionButton();
        
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
    public void getSleepSessionSnapshot_usesLocalValues()
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
        viewModel.setSelectedTags(tags);
        
        // local comment
        String expectedComment = "local comment";
        viewModel.setAdditionalComments(expectedComment);
        
        // local mood
        MoodUiData expectedMood = new MoodUiData(7);
        viewModel.setMood(expectedMood);
        
        // local interruption reason
        String expectedInterruptionReason = "local interruption reason";
        viewModel.setInterruptionReason(expectedInterruptionReason);
        
        // SUT
        LiveData<StoppedSessionData> stoppedSessionLive = viewModel.getSleepSessionSnapshot();
        TestUtils.activateLocalLiveData(stoppedSessionLive);
        StoppedSessionData stoppedSession = stoppedSessionLive.getValue();
        
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
    public void getInitialSelectedTagIds_returnsCorrectData()
    {
        List<Integer> expected = Arrays.asList(1, 2, 3);
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                // REFACTOR [21-04-19 7:48PM] -- add CurrentSession(selected tag ids) ctor.
                new MutableLiveData<>(new CurrentSession(
                        null,
                        null,
                        null,
                        expected)));
        
        LiveData<List<Integer>> selectedTagIds = viewModel.getInitialTagIds();
        
        TestUtils.activateLocalLiveData(selectedTagIds);
        assertThat(selectedTagIds.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void setMood_nullInput_clearsMood()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().withMood(aMood().withIndex(1)));
        
        // SUT
        viewModel.setMood(null);
        
        // verify
        viewModel.onPause();
        
        setCurrentSessionArg = ArgumentCaptor.forClass(CurrentSession.class);
        verify(mockCurrentSessionRepository).setCurrentSession(setCurrentSessionArg.capture());
        assertThat(setCurrentSessionArg.getValue().getMood(), is(nullValue()));
    }
    
    @Test
    public void setMood_setsMood()
    {
        giveViewModelInitialCurrentSession(aCurrentSession().withMood(aMood().withIndex(1)));
        
        // SUT
        viewModel.setMood(new MoodUiData(2));
        
        // verify
        viewModel.onPause();
        
        setCurrentSessionArg = ArgumentCaptor.forClass(CurrentSession.class);
        verify(mockCurrentSessionRepository).setCurrentSession(setCurrentSessionArg.capture());
        assertThat(setCurrentSessionArg.getValue().getMood().asIndex(), is(2));
    }
    
    @Test
    public void getInitialInterruptionReason_returnsCorrectValues()
    {
        TimeUtils timeUtils = new TimeUtils();
        
        CurrentSession currentSession = new CurrentSession(timeUtils.getNow());
        currentSession.interrupt(timeUtils); // begin interrupting so that setting the reason works
        
        String expectedReason = "reason";
        currentSession.setInterruptionReason(expectedReason);
        
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                currentSession));
        
        LiveData<String> persistedReason = viewModel.getInitialInterruptionReason();
        
        TestUtils.activateLocalLiveData(persistedReason);
        assertThat(persistedReason.getValue(), is(equalTo(expectedReason)));
    }
    
    @Test
    public void getInitialMood_returnsPersistedMood()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                // REFACTOR [21-04-19 7:48PM] -- add CurrentSession(Mood) ctor.
                new MutableLiveData<>(new CurrentSession(
                        null,
                        null,
                        new Mood(0),
                        null)));
        
        LiveData<MoodUiData> moodUiData = viewModel.getInitialMood();
        
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
        
        viewModel.setAdditionalComments(expectedComments);
        viewModel.setMood(expectedMood);
        viewModel.setSelectedTags(expectedSelectedTags);
        
        viewModel.onPause();
        
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
        
        LiveData<String> testStartTime = viewModel.getStartTimeText();
        
        TestUtils.activateLocalLiveData(testStartTime);
        assertThat(testStartTime.getValue(), is(equalTo(expected)));
    }
    
    @Test
    public void getSessionStartTime_returnsNullWhenNoSession()
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(
                new MutableLiveData<>(new CurrentSession(null)));
        
        LiveData<String> testStartTime = viewModel.getStartTimeText();
        
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
        
        viewModel.clickTrackingButton();
        viewModel.clickInterruptionButton();
        
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
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDurationText();
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
        
        viewModel.clickTrackingButton();
        
        LiveData<String> currentDuration = viewModel.getCurrentSleepSessionDurationText();
        TestUtils.activateLocalLiveData(currentDuration);
        // have the duration text update at least once
        RobolectricUtils.getLooperForThread(TickingLiveData.THREAD_NAME).idle();
        RobolectricUtils.idleMainLooper();
        
        viewModel.clickInterruptionButton();
        
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
        LiveData<String> currentDuration = viewModel.getCurrentSleepSessionDurationText();
        TestUtils.activateLocalLiveData(currentDuration);
        
        // REFACTOR [21-07-11 2:53AM] -- hardcoded string.
        assertThat(currentDuration.getValue(), is(equalTo("Error")));
        
        viewModel.clickTrackingButton();
        
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
        giveViewModelInitialCurrentSession(aCurrentSession().fresh());
        
        LiveData<Boolean> inSleepSession = activateLocally(viewModel.inSleepSession());
        assertThat(inSleepSession.getValue(), is(false));
        
        viewModel.clickTrackingButton();
        assertThat(inSleepSession.getValue(), is(true));
        
        discardCurrentSession();
        assertThat(inSleepSession.getValue(), is(false));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void discardCurrentSession()
    {
        PostSleepViewModel stubPostSleep = mock(PostSleepViewModel.class);
        when(stubPostSleep.consumeAction()).thenReturn(PostSleepViewModel.DISCARD);
        viewModel.handleAnyReturnFromPostSleep(stubPostSleep);
    }
    
    private void giveViewModelInitialCurrentSession()
    {
        giveViewModelInitialCurrentSession(aCurrentSession());
    }
    
    private void giveViewModelInitialCurrentSession(CurrentSessionBuilder currentSession)
    {
        when(mockCurrentSessionRepository.getCurrentSession()).thenReturn(new MutableLiveData<>(
                valueOf(currentSession)));
    }

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
