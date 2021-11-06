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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.CurrentSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.repositories.TagRepository;
import com.rbraithwaite.sleeptarget.test_utils.TestEqualities;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.CurrentSessionBuilder;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.PostSleepData;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aCurrentSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aStoppedSessionData;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PostSleepViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    TagRepository mockTagRepository;
    PostSleepViewModel viewModel;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockTagRepository = mock(TagRepository.class);
        viewModel = new PostSleepViewModel(mockTagRepository);
    }
    
    @After
    public void teardown()
    {
        mockTagRepository = null;
        viewModel = null;
    }
    
    @Test
    public void hasAdditionalComments_returnsWhetherThereAreAdditionalComments()
    {
        viewModel.init(valueOf(aStoppedSessionData().with(aCurrentSession().withNoComments())));
        
        assertThat(viewModel.hasAdditionalComments(), is(false));
        
        viewModel.discardData();
        viewModel.init(valueOf(aStoppedSessionData().with(aCurrentSession().withAdditionalComments(
                "some comments"))));
        
        assertThat(viewModel.hasAdditionalComments(), is(true));
    }
    
    @Test
    public void getInterruptionsCountText_returnsCorrectCount()
    {
        CurrentSessionBuilder thisCurrentSession = aCurrentSession()
                .withNoCurrentInterruption()
                .withInterruptions(
                        anInterruption(),
                        anInterruption(),
                        anInterruption());
        
        viewModel.init(valueOf(aStoppedSessionData().with(thisCurrentSession)));
        
        assertThat(viewModel.getInterruptionsCountText(), is(equalTo("3")));
    }
    
    @Test
    public void getInterruptionsTotalTimeText_returnsCorrectTime()
    {
        CurrentSessionBuilder thisCurrentSession = aCurrentSession()
                .withNoCurrentInterruption()
                .withInterruptions(
                        anInterruption().withDuration(1, 20, 0),
                        anInterruption().withDuration(0, 3, 45));
        
        viewModel.init(valueOf(aStoppedSessionData().with(thisCurrentSession)));
        
        assertThat(viewModel.getInterruptionsTotalTimeText(), is(equalTo("1h 23m 45s")));
    }
    
    @Test
    public void getInterruptionsListItems_returnsCorrectListItems()
    {
        CurrentSessionBuilder thisCurrentSession = aCurrentSession()
                .withNoCurrentInterruption()
                .withInterruptions(
                        anInterruption()
                                .withStart(aDate().withValue(2021, 7, 19, 15, 45))
                                .withDuration(1, 23, 45)
                                .withReason("first reason"),
                        anInterruption()
                                .withStart(aDate().withValue(2021, 6, 18, 14, 44))
                                .withDuration(2, 34, 56)
                                .withReason("second reason"));
        
        viewModel.init(valueOf(aStoppedSessionData().with(thisCurrentSession)));
        
        List<InterruptionListItem> listItems = viewModel.getInterruptionsListItems();
        
        assertThat(listItems.size(), is(2));
        
        InterruptionListItem listItem = listItems.get(0);
        assertThat(listItem.start, is(equalTo("3:45 PM, Jul 19")));
        assertThat(listItem.duration, is(equalTo("1h 23m 45s")));
        assertThat(listItem.reason, is(equalTo("first reason")));
        
        listItem = listItems.get(1);
        assertThat(listItem.start, is(equalTo("2:44 PM, Jun 18")));
        assertThat(listItem.duration, is(equalTo("2h 34m 56s")));
        assertThat(listItem.reason, is(equalTo("second reason")));
    }
    
    @Test
    public void hasNoInterruptions_returnsTrueWhenThereAreNoInterruptions()
    {
        viewModel.init(valueOf(aStoppedSessionData().with(aCurrentSession().withNoInterruptions())));
        assertThat(viewModel.hasNoInterruptions(), is(true));
    }
    
    @Test
    public void setRating_updates_getPostSleepData()
    {
        viewModel.init(new StoppedSessionData(null, new PostSleepData(5f)));
        
        LiveData<PostSleepData> postSleepData = viewModel.getPostSleepData();
        TestUtils.activateLocalLiveData(postSleepData);
        
        // SUT
        float expectedRating = 1f;
        viewModel.setRating(expectedRating);
        
        assertThat(postSleepData.getValue().rating, is(equalTo(expectedRating)));
    }
    
    @Test
    public void ratingIsZeroWhenPostSleepDataIsNull()
    {
        viewModel.init(new StoppedSessionData(null, null));
        assertThat(viewModel.getRating(), is(0f));
    }
    
    @Test
    public void getStartText_reflectsSnapshot()
    {
        CurrentSession currentSession = new CurrentSession(
                new GregorianCalendar(2021, 5, 27, 3, 45).getTime());
        
        viewModel.init(new StoppedSessionData(
                currentSession.createSnapshot(new TimeUtils()), null));
        
        assertThat(viewModel.getStartText(), is(equalTo("3:45 AM, Jun 27 2021")));
    }
    
    @Test
    public void getEndText_reflectsSnapshot()
    {
        CurrentSession currentSession = new CurrentSession(
                new GregorianCalendar(2021, 5, 27, 3, 45).getTime());
        
        TimeUtils fakeNow = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return new GregorianCalendar(2021, 5, 27, 4, 0).getTime();
            }
        };
        
        viewModel.init(new StoppedSessionData(currentSession.createSnapshot(fakeNow), null));
        
        assertThat(viewModel.getEndText(), is(equalTo("4:00 AM, Jun 27 2021")));
    }
    
    @Test
    public void getDurationText_reflectsSnapshot()
    {
        CurrentSession currentSession = new CurrentSession(
                new GregorianCalendar(2021, 5, 27, 3, 45).getTime());
        
        TimeUtils fakeNow = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return new GregorianCalendar(2021, 5, 27, 5, 5).getTime();
            }
        };
        
        viewModel.init(new StoppedSessionData(currentSession.createSnapshot(fakeNow),
                                              null));
        
        assertThat(viewModel.getDurationText(), is(equalTo("1h 20m 00s")));
    }
    
    @Test
    public void getMood_reflectsSnapshot()
    {
        CurrentSession currentSession = TestUtils.ArbitraryData.getCurrentSession();
        
        viewModel.init(new StoppedSessionData(
                currentSession.createSnapshot(new TimeUtils()), null));
        
        assertThat(viewModel.getMood(),
                   is(equalTo(ConvertMood.toUiData(currentSession.getMood()))));
    }
    
    @Test
    public void getAdditionalComments_reflectsSnapshot()
    {
        CurrentSession currentSession = TestUtils.ArbitraryData.getCurrentSession();
        
        viewModel.init(new StoppedSessionData(
                currentSession.createSnapshot(new TimeUtils()), null));
        
        assertThat(viewModel.getAdditionalComments(),
                   is(equalTo(currentSession.getAdditionalComments())));
    }
    
    @Test
    public void getTags_reflectsSnapshot()
    {
        CurrentSession currentSession = TestUtils.ArbitraryData.getCurrentSession();
        
        List<Integer> tagIds = Arrays.asList(1, 2, 3);
        currentSession.setSelectedTagIds(tagIds);
        
        LiveData<List<Tag>> expected = new MutableLiveData<>(Arrays.asList(
                new Tag(1, "tag1"),
                new Tag(2, "tag2"),
                new Tag(3, "tag3")));
        when(mockTagRepository.getTagsWithIds(tagIds)).thenReturn(expected);
        
        viewModel.init(new StoppedSessionData(
                currentSession.createSnapshot(new TimeUtils()), null));
        
        // SUT
        LiveData<List<TagUiData>> tagUiDataList = viewModel.getTags();
        TestUtils.activateLocalLiveData(tagUiDataList);
        
        assertThat(tagUiDataList.getValue().size(), is(expected.getValue().size()));
        for (int i = 0; i < tagUiDataList.getValue().size(); i++) {
            Tag expectedTag = expected.getValue().get(i);
            TagUiData uiData = tagUiDataList.getValue().get(i);
            assertThat(TestEqualities.TagUiData_equals_Tag(uiData, expectedTag), is(true));
        }
    }
}
