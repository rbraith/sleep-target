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

package com.rbraithwaite.sleeptarget.test_utils.ui.drivers;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.RecyclerListItemAssertions;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFormatting;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFragmentViewModel;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveListItemTagsAdapter;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Date;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.withRating;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

public class SessionArchiveTestDriver
        extends BaseFragmentTestDriver<SessionArchiveFragment, SessionArchiveTestDriver.Assertions>
{
//*********************************************************
// private constants
//*********************************************************

    private final OnOpenSessionDetailsListener mOnOpenSessionDetailsListener;


//*********************************************************
// public helpers
//*********************************************************

    public interface OnOpenSessionDetailsListener
    {
        void onOpenSessionDetails();
    }
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<SessionArchiveTestDriver,
            SessionArchiveFragmentViewModel>
    {
        public Assertions(SessionArchiveTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public ListItemAssertions listItemAtIndex(int listItemIndex)
        {
            return new ListItemAssertions(listItemIndex, R.id.session_archive_list);
        }
        
        public void listIsEmpty()
        {
            onView(withId(R.id.session_archive_list_item_card)).check(doesNotExist());
            onView(withId(R.id.session_archive_no_data_text)).check(matches(isDisplayed()));
        }
    }
    
    public static class ListItemAssertions
            extends RecyclerListItemAssertions
    {
        private ListItemAssertions(int listItemIndex, int recyclerId)
        {
            super(listItemIndex, recyclerId);
        }
        
        public void hasValuesMatching(SleepSession sleepSession)
        {
            hasStartMatching(sleepSession.getStart());
            hasEndMatching(sleepSession.getEnd());
            hasDurationMatching(sleepSession.getDurationMillis());
            hasTagsMatching(sleepSession.getTags());
            hasRatingMatching(sleepSession.getRating());
            hasMoodMatching(sleepSession.getMood());
            hasAdditionalComments(sleepSession.getAdditionalComments() != null &&
                                  !sleepSession.getAdditionalComments().isEmpty());
            hasInterruptionsMatching(sleepSession.getInterruptions());
        }
        
        public void hasValuesMatching(SleepSessionBuilder sleepSession)
        {
            hasValuesMatching(sleepSession.build());
        }
        
        public void hasInterruptionsMatching(Interruptions interruptions)
        {
            ViewInteraction onInterruptionsText =
                    onView(withId(R.id.session_archive_list_item_interruptions_VALUE));
            
            if (interruptions == null || interruptions.isEmpty()) {
                onInterruptionsText.check(matches(not(isDisplayed())));
            } else {
                onInterruptionsText.check(matches(allOf(
                        isDisplayed(),
                        withText(SessionArchiveFormatting.formatInterruptions(interruptions)))));
            }
        }
        
        public void hasAdditionalComments(boolean shouldHaveAdditionalComments)
        {
            Matcher<View> displayedMatcher = shouldHaveAdditionalComments ?
                    isDisplayed() : not(isDisplayed());
            
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_comment_icon), displayedMatcher));
        }
        
        public void hasStartMatching(Date start)
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_start_VALUE),
                    withText(SessionArchiveFormatting.formatFullDate(start))));
        }
        
        public void hasEndMatching(Date end)
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_stop_VALUE),
                    withText(SessionArchiveFormatting.formatFullDate(end))));
        }
        
        public void hasDurationMatching(long durationMillis)
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_duration_VALUE),
                    withText(SessionArchiveFormatting.formatDuration(durationMillis))));
        }
        
        public void hasRatingMatching(float rating)
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_rating),
                    withRating(rating)));
        }
        
        public void hasMoodMatching(Mood mood)
        {
            if (mood == null) {
                hasNoMood();
            } else {
                checkThatThisListItemHasContentsMatching(allOf(
                        withId(R.id.session_archive_list_item_mood),
                        isDisplayed(),
                        withMoodIndex(mood.asIndex())));
            }
        }
        
        public void hasNoMood()
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.session_archive_list_item_mood),
                    not(isDisplayed())));
        }
        
        public void hasTagsMatching(List<Tag> tags)
        {
            if (tags == null || tags.isEmpty()) {
                checkThatThisListItemHasContentsMatching(allOf(
                        withId(R.id.session_archive_list_item_tags),
                        not(isDisplayed())));
            } else {
                checkThatThisListItemHasContentsMatching(allOf(
                        withId(R.id.session_archive_list_item_tags),
                        isDisplayed(),
                        withTags(tags)));
            }
        }
        
        private Matcher<View> withTags(List<Tag> tags)
        {
            return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
            {
                @Override
                protected boolean matchesSafely(RecyclerView item)
                {
                    SessionArchiveListItemTagsAdapter adapter =
                            (SessionArchiveListItemTagsAdapter) item.getAdapter();
                    
                    List<String> adapterTags = adapter.getTags();
                    if (adapterTags.size() != tags.size()) { return false; }
                    for (Tag tag : tags) {
                        if (!adapterTags.contains(tag.getText())) {
                            return false;
                        }
                    }
                    return true;
                }
                
                @Override
                public void describeTo(Description description)
                {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("has tags matching: ");
                    
                    if (tags == null || tags.isEmpty()) {
                        stringBuilder.append("No tags");
                    } else {
                        for (int i = 0; i < tags.size() - 1; i++) {
                            stringBuilder.append(tags.get(i).getText() + ", ");
                        }
                        stringBuilder.append(tags.get(tags.size() - 1).getText());
                    }
                    description.appendText(stringBuilder.toString());
                }
            };
        }
        
        // REFACTOR [21-06-25 3:06AM] -- I could extract this as a general utility.
        private Matcher<View> withMoodIndex(int moodIndex)
        {
            return new BoundedMatcher<View, MoodView>(MoodView.class)
            {
                @Override
                protected boolean matchesSafely(MoodView item)
                {
                    return item.getMoodIndex() == moodIndex;
                }
                
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("mood index matches: " + moodIndex);
                }
            };
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveTestDriver(
            FragmentTestHelper<SessionArchiveFragment> helper,
            OnOpenSessionDetailsListener onOpenSessionDetailsListener)
    {
        mOnOpenSessionDetailsListener = onOpenSessionDetailsListener;
        init(helper, new Assertions(this));
    }
    
    public SessionArchiveTestDriver(FragmentTestHelper<SessionArchiveFragment> helper)
    {
        this(helper, null);
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}
     */
    public void pressAddNewSessionButton()
    {
        if (mOnOpenSessionDetailsListener != null) {
            mOnOpenSessionDetailsListener.onOpenSessionDetails();
        }
        onView(withId(R.id.session_archive_fab)).perform(click());
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}
     */
    public void openSessionDetailsFor(int listItemIndex)
    {
        if (mOnOpenSessionDetailsListener != null) {
            mOnOpenSessionDetailsListener.onOpenSessionDetails();
        }
        onRecyclerView()
                .perform(RecyclerViewActions.actionOnItemAtPosition(listItemIndex, click()));
    }

//*********************************************************
// private methods
//*********************************************************

    private ViewInteraction onRecyclerView()
    {
        return onView(withId(R.id.session_archive_list));
    }
}
