package com.rbraithwaite.sleepapp.ui.session_details;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.SleepSessionOverlapChecker;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionWrapper;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.anInterruption;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

// REFACTOR [20-12-8 8:52PM] -- consider splitting this into separate test classes?
@RunWith(AndroidJUnit4.class)
public class SessionDetailsFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SessionDetailsFragmentViewModel viewModel;
    SleepSessionRepository mockSleepSessionRepository;
    SleepSessionOverlapChecker sleepSessionOverlapChecker;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        sleepSessionOverlapChecker = new SleepSessionOverlapChecker(mockSleepSessionRepository);
        viewModel = new SessionDetailsFragmentViewModel(
                new TimeUtils(),
                sleepSessionOverlapChecker,
                new TestUtils.SynchronizedExecutor());
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        sleepSessionOverlapChecker = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void deleteInterruption_affects_getInterruption()
    {
        int expectedId = 123;
        InterruptionBuilder expected = anInterruption().withId(expectedId);
        Interruption expectedInterruption = expected.build();
        viewModel.initData(new SleepSessionWrapper(
                aSleepSession().withInterruptions(expected).build()));
        
        assertThat(viewModel.getInterruption(expectedId).getData(),
                   is(equalTo(expectedInterruption)));
        
        viewModel.deleteInterruption(new InterruptionWrapper(expectedInterruption));
        
        assertThat(viewModel.getInterruption(expectedId).getData(), is(nullValue()));
    }
    
    @Test
    public void getResult_isNullWhenNotInitialized()
    {
        // starts null when not init'd
        assertThat(viewModel.getResult().getModel(), is(nullValue()));
        
        // result matches data
        SleepSession expected = aSleepSession().build();
        viewModel.initData(new SleepSessionWrapper(expected));
        assertThat(viewModel.getResult().getModel(), is(equalTo(expected)));
        
        // clearing makes the result null
        viewModel.clearData();
        assertThat(viewModel.getResult().getModel(), is(nullValue()));
    }
    
    @Test
    public void initData_onlyWorksWhenNotInitialized()
    {
        SleepSessionBuilder sleepSession = aSleepSession();
        SleepSession expected = sleepSession.build();
        viewModel.initData(new SleepSessionWrapper(expected));
        
        viewModel.initData(new SleepSessionWrapper(
                sleepSession.withComments("not expected").build()));
        // result should not have changed, and still match expected
        assertThat(viewModel.getResult().getModel(), is(equalTo(expected)));
        
        // clear the data, now we can re-initialized
        viewModel.clearData();
        SleepSession expected2 = sleepSession.withComments("expected 2").build();
        viewModel.initData(new SleepSessionWrapper(expected2));
        assertThat(viewModel.getResult().getModel(), is(equalTo(expected2)));
    }
    
    @Test
    public void interruptionsReflectSessionData()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        viewModel.initData(new SleepSessionWrapper(sleepSession));
        
        assertThat(viewModel.hasNoInterruptions(), is(true));
        assertThat(viewModel.getInterruptionListItems().isEmpty(), is(true));
        assertThat(viewModel.getInterruptionsCountText(), is(equalTo("0")));
        assertThat(viewModel.getInterruptionsTotalTimeText(), is(equalTo("0h 00m 00s")));
        
        sleepSession.setInterruptions(new Interruptions(aListOf(
                anInterruption().withDuration(1, 10, 0),
                anInterruption().withDuration(10, 5, 5))));
        viewModel.setData(new SleepSessionWrapper(sleepSession));
        
        assertThat(viewModel.hasNoInterruptions(), is(false));
        assertThat(viewModel.getInterruptionListItems().size(), is(2));
        assertThat(viewModel.getInterruptionsCountText(), is(equalTo("2")));
        assertThat(viewModel.getInterruptionsTotalTimeText(), is(equalTo("11h 15m 05s")));
    }
    
    
    @Test
    public void getRating_reflects_setRating()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        viewModel.initData(new SleepSessionWrapper(sleepSession));
        
        assertThat(viewModel.getRating(), is(equalTo(sleepSession.getRating())));
        
        // SUT
        float expectedRating = sleepSession.getRating() + 0.5f;
        viewModel.setRating(expectedRating);
        
        assertThat(viewModel.getRating(), is(equalTo(expectedRating)));
    }
    
    @Test
    public void setTags_affectsResult()
    {
        viewModel.initData(new SleepSessionWrapper(
                TestUtils.ArbitraryData.getSleepSession()));
        
        List<TagUiData> expected = Arrays.asList(new TagUiData(2, "what"));
        // SUT
        viewModel.setTags(expected);
        
        // verify
        SleepSession result = viewModel.getResult().getModel();
        assertThat(result.getTags().size(), is(expected.size()));
        assertThat(ConvertTag.toUiData(result.getTags().get(0)), is(equalTo(expected.get(0))));
    }
    
    @Test
    public void getTagIds_reflectsSessionData()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setTags(Arrays.asList(
                new Tag(1, "arbitrary"),
                new Tag(2, "arbitrary")));
        
        viewModel.initData(new SleepSessionWrapper(sleepSession));
        
        // SUT
        List<Integer> tagIds = viewModel.getTagIds();
        
        assertThat(tagIds.size(), is(sleepSession.getTags().size()));
        for (int i = 0; i < tagIds.size(); i++) {
            assertThat(tagIds.get(i), is(sleepSession.getTags().get(i).getTagId()));
        }
    }
    
    @Test
    public void clearMood_setsMoodToNull()
    {
        SleepSession initialData = TestUtils.ArbitraryData.getSleepSession();
        initialData.setMood(Mood.fromIndex(1));
        viewModel.initData(new SleepSessionWrapper(initialData));
        
        viewModel.clearMood();
        
        assertThat(viewModel.getMood(), is(nullValue()));
    }
    
    @Test
    public void getMood_reflects_setMood()
    {
        SleepSession initialData = TestUtils.ArbitraryData.getSleepSession();
        initialData.setMood(Mood.fromIndex(1));
        viewModel.initData(new SleepSessionWrapper(initialData));
        
        assertThat(viewModel.getMood(), is(equalTo(ConvertMood.toUiData(initialData.getMood()))));
        
        MoodUiData expected = new MoodUiData(3);
        viewModel.setMood(expected);
        
        assertThat(viewModel.getMood(), is(equalTo(expected)));
    }
    
    @Test
    public void initSessionData_initializesDataOnValidInput()
    {
        SleepSession initialData = TestUtils.ArbitraryData.getSleepSession();
        viewModel.initData(new SleepSessionWrapper(initialData));
        
        LiveData<GregorianCalendar> start = viewModel.getStartCalendar();
        LiveData<GregorianCalendar> end = viewModel.getEndCalendar();
        // REFACTOR [20-12-16 7:23PM] -- consider making activateLocalLiveData variadic.
        TestUtils.activateLocalLiveData(start);
        TestUtils.activateLocalLiveData(end);
        assertThat(start.getValue().getTime(), is(equalTo(initialData.getStart())));
        assertThat(end.getValue().getTime(), is(equalTo(initialData.getEnd())));
    }
    
    @Test
    public void clearData_clearsData()
    {
        viewModel.initData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSession()));
        
        viewModel.clearData();
        
        LiveData<GregorianCalendar> startDateTime = viewModel.getStartCalendar();
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.activateLocalLiveData(startDateTime);
        TestUtils.activateLocalLiveData(endDateTime);
        assertThat(startDateTime.getValue(), is(nullValue()));
        assertThat(endDateTime.getValue(), is(nullValue()));
    }
    
    @Test
    public void getResult_matchesViewModelState()
    {
        TimeUtils timeUtils = new TimeUtils();
        
        SleepSession initial = new SleepSession(TestUtils.ArbitraryData.getDate(), 0);
        viewModel.initData(new SleepSessionWrapper(initial));
        
        // set the view model state
        SleepSession expected = new SleepSession(
                initial.getId(),
                timeUtils.addDurationToDate(initial.getStart(), -1 * 10 * 60 * 1000), // -10min
                5 * 60 * 1000, // 5 min
                "some comments",
                Mood.fromIndex(2),
                // TODO [21-05-10 11:33PM] -- the view model takes TagUiData, so it kind
                //  of a pain to test them right now.
                null,
                4.5f);
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(expected.getStart());
        viewModel.setStartDate(cal.get(Calendar.YEAR),
                               cal.get(Calendar.MONTH),
                               cal.get(Calendar.DAY_OF_MONTH));
        viewModel.setStartTimeOfDay(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        
        cal.setTime(expected.getEnd());
        viewModel.setEndDate(cal.get(Calendar.YEAR),
                             cal.get(Calendar.MONTH),
                             cal.get(Calendar.DAY_OF_MONTH));
        viewModel.setEndTimeOfDay(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        
        viewModel.setAdditionalComments(expected.getAdditionalComments());
        viewModel.setRating(expected.getRating());
        viewModel.setMood(ConvertMood.toUiData(expected.getMood()));
        
        // SUT
        SleepSession result = viewModel.getResult().getModel();
        
        assertThat(result, is(equalTo(expected)));
    }
    
    @Test(expected = SessionDetailsFragmentViewModel.InvalidDateTimeException.class)
    public void setEndTime_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set end before start
        calendar.add(Calendar.HOUR_OF_DAY, -5);
        viewModel.setEndTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }
    
    @Test
    public void setEndTime_leavesDateUnchanged()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // begin watching end date
        LiveData<GregorianCalendar> endDate = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDate);
        
        GregorianCalendar originalEnd = endDate.getValue();
        
        // update end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                  calendar.get(Calendar.MINUTE));
        
        // assert end date did not change
        synchronizer.sync();
        assertDatesAreTheSame(endDate.getValue(), originalEnd);
    }
    
    @Test
    public void setEndTime_updatesEnd()
    {
        // set end datetime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch end datetime
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        // set end time
        calendar.add(Calendar.MINUTE, 15);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                  calendar.get(Calendar.MINUTE));
        
        // end datetime changes
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar)));
    }
    
    @Test
    public void setEndDate_updatesEnd()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> endDateTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endDateTime);
        
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(endDateTime.getValue(), is(equalTo(calendar)));
    }
    
    @Test
    public void setEndDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> endTime = viewModel.getEndCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(endTime);
        
        GregorianCalendar originalEnd = endTime.getValue();
        
        // update the end date
        calendar.add(Calendar.DAY_OF_MONTH, 3);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertTimesOfDayAreTheSame(endTime.getValue(), originalEnd);
    }
    
    @Test(expected = SessionDetailsFragmentViewModel.InvalidDateTimeException.class)
    public void setEndDate_throwsIfEndIsBeforeStart()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set end before start
        calendar.add(Calendar.MONTH, -1);
        viewModel.setEndDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setStartDate_leavesTimeOfDayUnchanged()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> start = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(start);
        
        GregorianCalendar originalStart = start.getValue();
        
        // update the start date
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertTimesOfDayAreTheSame(start.getValue(), originalStart);
    }
    
    @Test
    public void setStartDate_updatesStart()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<GregorianCalendar> startDate = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        
        synchronizer.sync();
        assertThat(startDate.getValue(), is(equalTo(calendar)));
    }
    
    @Test(expected = SessionDetailsFragmentViewModel.InvalidDateTimeException.class)
    public void setStartDate_throwsIfStartIsAfterEnd()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set start after end
        calendar.add(Calendar.MONTH, 1);
        viewModel.setStartDate(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
    }
    
    @Test
    public void setStartTime_leavesDateUnchanged()
    {
        // set startDateTime
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // begin watching getStartDate
        LiveData<GregorianCalendar> startDate = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startDate);
        
        GregorianCalendar originalStart = startDate.getValue();
        
        // update setStartTime
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE));
        
        // assert getStartDate is the same
        synchronizer.sync();
        assertDatesAreTheSame(startDate.getValue(), originalStart);
    }
    
    @Test
    public void setStartTime_updatesStart()
    {
        // set startdatetime
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // watch start time
        LiveData<GregorianCalendar> startTime = viewModel.getStartCalendar();
        TestUtils.LocalLiveDataSynchronizer<GregorianCalendar> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(startTime);
        
        // set start time
        calendar.add(Calendar.MINUTE, -15);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR_OF_DAY),
                                    calendar.get(Calendar.MINUTE));
        
        // start time changes
        synchronizer.sync();
        assertThat(startTime.getValue(), is(equalTo(calendar)));
    }
    
    // TODO [20-12-1 12:01AM] -- test needed for setStartDate being called without
    //  setStartDateTime first being called.
    // TODO [20-12-6 8:20PM] -- test needed for setStartTime being called without
    //  setStartDateTime first being called.
    
    @Test(expected = SessionDetailsFragmentViewModel.InvalidDateTimeException.class)
    public void setStartTime_throwsIfStartIsAfterEnd()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(TestUtils.ArbitraryData.getDate());
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        // set start after end
        calendar.add(Calendar.HOUR_OF_DAY, 1);
        viewModel.setStartTimeOfDay(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE));
    }
    
    @Test
    public void sessionDuration_updatesWhenStartAndEndChange()
    {
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        Date start = calendar.getTime(); // saving this here, as the calendar is moved to the end
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(calendar.getTime(), 0)));
        
        LiveData<String> sessionDurationText = viewModel.getSessionDurationText();
        TestUtils.LocalLiveDataSynchronizer<String> synchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(sessionDurationText);
        
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(SessionDetailsFormatting.formatDuration(0))));
        
        // change end, check for duration update
        int endOffsetSeconds = 120; // 2 min
        calendar.add(GregorianCalendar.SECOND, endOffsetSeconds);
        viewModel.setEndTimeOfDay(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
        synchronizer.sync();
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(SessionDetailsFormatting.formatDuration(endOffsetSeconds * 1000))));
        
        // change start, check for duration update
        calendar.setTime(start);
        int startOffsetSeconds = 60; // 1 min
        calendar.add(GregorianCalendar.SECOND, startOffsetSeconds);
        viewModel.setStartTimeOfDay(calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE));
        
        synchronizer.sync();
        assertThat(sessionDurationText.getValue(),
                   is(equalTo(SessionDetailsFormatting.formatDuration(
                           (endOffsetSeconds - startOffsetSeconds) * 1000))));
    }
    
    @Test
    public void getSessionDuration_positiveInput()
    {
        // ________________________________________ init the data
        Date startDateTime = TestUtils.ArbitraryData.getDate();
        
        int testDurationMillis = 300000; // 5 min
        
        viewModel.initData(new SleepSessionWrapper(
                new SleepSession(startDateTime, testDurationMillis)));
        
        LiveData<String> sessionDuration = viewModel.getSessionDurationText();
        TestUtils.activateLocalLiveData(sessionDuration);
        
        String expected = SessionDetailsFormatting.formatDuration(testDurationMillis);
        assertThat(sessionDuration.getValue(), is(equalTo(expected)));
    }

//*********************************************************
// private methods
//*********************************************************

    private void assertDatesAreTheSame(GregorianCalendar a, GregorianCalendar b)
    {
        assertThat(a.get(Calendar.YEAR), is(equalTo(b.get(Calendar.YEAR))));
        assertThat(a.get(Calendar.MONTH), is(equalTo(b.get(Calendar.MONTH))));
        assertThat(a.get(Calendar.DAY_OF_MONTH), is(equalTo(b.get(Calendar.DAY_OF_MONTH))));
    }
    
    private void assertTimesOfDayAreTheSame(GregorianCalendar a, GregorianCalendar b)
    {
        assertThat(a.get(Calendar.HOUR_OF_DAY), is(equalTo(b.get(Calendar.HOUR_OF_DAY))));
        assertThat(a.get(Calendar.MINUTE), is(equalTo(b.get(Calendar.MINUTE))));
    }
}
