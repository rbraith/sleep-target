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

package com.rbraithwaite.sleeptarget.ui.session_details;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.models.overlap_checker.SleepSessionOverlapChecker;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.interruption_details.InterruptionDetailsData;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
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
        
        assertThat(viewModel.getInterruptionDetailsData(expectedId).getInterruption(),
                   is(equalTo(expectedInterruption)));
        
        viewModel.deleteInterruption(new InterruptionDetailsData(expectedInterruption, null));
        
        assertThat(viewModel.getInterruptionDetailsData(expectedId).getInterruption(),
                   is(nullValue()));
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
        SleepSession sleepSession = valueOf(aSleepSession().withDurationHours(123).withNoInterruptions());
        viewModel.initData(new SleepSessionWrapper(sleepSession));
        LiveData<List<InterruptionListItem>> listItems = viewModel.getInterruptionListItems();
        TestUtils.activateLocalLiveData(listItems);
        
        assertThat(viewModel.hasNoInterruptions(), is(true));
        assertThat(listItems.getValue().isEmpty(), is(true));
        assertThat(viewModel.getInterruptionsCountText(), is(equalTo("0")));
        assertThat(viewModel.getInterruptionsTotalTimeText(), is(equalTo("0h 00m 00s")));
        
        sleepSession.setInterruptions(new Interruptions(aListOf(
                anInterruption().withStart(sleepSession.getStart()).withDuration(1, 10, 0),
                anInterruption().withStart(sleepSession.getStart()).withDuration(10, 5, 5))));
        viewModel.setData(new SleepSessionWrapper(sleepSession));
        
        assertThat(viewModel.hasNoInterruptions(), is(false));
        assertThat(listItems.getValue().size(), is(2));
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
    public void initData_initializesDataOnValidInput()
    {
        SleepSession initialData = TestUtils.ArbitraryData.getSleepSession();
        viewModel.initData(new SleepSessionWrapper(initialData));
        assertThat(viewModel.getResult().getModel(), is(equalTo(initialData)));
    }
    
    @Test
    public void clearData_clearsData()
    {
        viewModel.initData(
                new SleepSessionWrapper(TestUtils.ArbitraryData.getSleepSession()));
        
        viewModel.clearData();
        
        assertThat(viewModel.getResult().getModel(), is(nullValue()));
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
        viewModel.setStart(cal.getTime());
        
        cal.setTime(expected.getEnd());
        viewModel.setEnd(cal.getTime());
        
        viewModel.setAdditionalComments(expected.getAdditionalComments());
        viewModel.setRating(expected.getRating());
        viewModel.setMood(ConvertMood.toUiData(expected.getMood()));
        
        // SUT
        SleepSession result = viewModel.getResult().getModel();
        
        assertThat(result, is(equalTo(expected)));
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
