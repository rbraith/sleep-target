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
package com.rbraithwaite.sleeptarget.test_utils.ui.drivers;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.StoppedSessionDataBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.AssertOn;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepFormatting;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepFragment;
import com.rbraithwaite.sleeptarget.ui.post_sleep.PostSleepViewModel;
import com.rbraithwaite.sleeptarget.ui.sleep_tracker.data.StoppedSessionData;
import com.rbraithwaite.sleeptarget.utils.LiveDataFuture;

import java.util.Date;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoActions.betterScrollTo;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.withMoodIndex;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.withRating;
import static com.rbraithwaite.sleeptarget.test_utils.ui.RecyclerMatchers.withItemAt;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class PostSleepTestDriver
        extends BaseFragmentTestDriver<PostSleepFragment, PostSleepTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private NavCallbacks mNavCallbacks;

//*********************************************************
// public helpers
//*********************************************************

    public interface NavCallbacks
    {
        void onKeep();
        void onDiscard();
        void onUp();
    }
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<PostSleepTestDriver, PostSleepViewModel>
    {
        public Assertions(PostSleepTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void commentsAreUnset()
        {
            commentsMatch(TestUtils.getString(R.string.postsleepdialog_nocomments));
        }
        
        public void moodIsUnset()
        {
            onView(withParent(withId(R.id.postsleep_mood_frame))).check(matches(withText(R.string.postsleepdialog_nomood)));
        }
        
        public void tagsAreUnset()
        {
            onView(withId(R.id.postsleep_tags_recycler)).check(matches(withItemAt(0).matching(
                    withText(R.string.no_tags))));
        }
        
        public void commentsMatch(String expectedComments)
        {
            onView(withParent(withId(R.id.postsleep_comments_scroll))).check(matches(withText(
                    expectedComments)));
        }
        
        public void interruptionsAreUnset()
        {
            onView(withId(R.id.post_sleep_interruptions_content)).perform(betterScrollTo());
            onView(withId(R.id.post_sleep_interruptions_nodata)).check(matches(isDisplayed()));
        }
        
        public void hasInterruptionCount(int expectedCount)
        {
            onView(withId(R.id.post_sleep_interruptions_content)).perform(betterScrollTo());
            onView(withId(R.id.common_interruptions_count)).check(matches(withText(String.valueOf(
                    expectedCount))));
            
            assertOnFragment(fragment -> {
                int itemCount = fragment.getInterruptionsRecycler().getAdapter().getItemCount();
                hamcrestAssertThat(itemCount, is(expectedCount));
            });
        }
        
        public void moodMatches(Mood mood)
        {
            if (mood == null) {
                moodIsUnset();
            } else {
                onView(withParent(withId(R.id.postsleep_mood_frame))).check(matches(withMoodIndex(
                        mood.asIndex())));
            }
        }
        
        public void selectedTagIdsMatch(List<Integer> expectedSelectedTagIds)
        {
            assertOnFragment(fragment -> {
                LiveDataFuture.getValue(fragment.getViewModel().getTags(), tags -> {
                    hamcrestAssertThat(tags, is(notNullValue()));
                    hamcrestAssertThat(tags.isEmpty(), is(false));
                    hamcrestAssertThat(tags.size(), is(equalTo(expectedSelectedTagIds.size())));
                    for (int i = 0; i < tags.size(); i++) {
                        hamcrestAssertThat(tags.get(i).tagId,
                                           is(equalTo(expectedSelectedTagIds.get(i))));
                    }
                });
            });
        }
        
        public void durationMatches(long expectedDurationMillis)
        {
            onView(withId(R.id.postsleep_duration)).check(matches(withText(
                    PostSleepFormatting.formatDuration(expectedDurationMillis))));
        }
        
        public void ratingIsUnset()
        {
            ratingMatches(0f);
        }
        
        public void startMatches(Date start)
        {
            onView(withId(R.id.postsleep_start_value)).check(matches(withText(PostSleepFormatting.formatDate(
                    start))));
        }
        
        public void endMatches(Date end)
        {
            onView(withId(R.id.postsleep_stop_value)).check(matches(withText(PostSleepFormatting.formatDate(
                    end))));
        }
        
        public void ratingMatches(float rating)
        {
            onView(withId(R.id.postsleep_star_rating)).check(matches(withRating(rating)));
        }
        
        public void valuesMatch(StoppedSessionDataBuilder builder)
        {
            StoppedSessionData stoppedSessionData = builder.build();
            
            startMatches(stoppedSessionData.currentSessionSnapshot.start);
            endMatches(stoppedSessionData.currentSessionSnapshot.end);
            moodMatches(stoppedSessionData.currentSessionSnapshot.mood);
            selectedTagIdsMatch(stoppedSessionData.currentSessionSnapshot.selectedTagIds);
            commentsMatch(stoppedSessionData.currentSessionSnapshot.additionalComments);
            durationMatches(stoppedSessionData.currentSessionSnapshot.durationMillis);
            ratingMatches(stoppedSessionData.postSleepData.rating);
        }
        
        private void assertOnFragment(AssertOn<PostSleepFragment> assertion)
        {
            getOwningDriver().getHelper().performSyncedFragmentAction(assertion::assertOn);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    private PostSleepTestDriver() {}

//*********************************************************
// api
//*********************************************************

    public static PostSleepTestDriver startingWith(StoppedSessionDataBuilder builder)
    {
        return startingWith(builder.build());
    }
    
    public static PostSleepTestDriver startingWith(StoppedSessionData data)
    {
        PostSleepFragment.Args args = new PostSleepFragment.Args(data);
        
        PostSleepTestDriver driver = new PostSleepTestDriver();
        HiltFragmentTestHelper<PostSleepFragment> helper =
                HiltFragmentTestHelper.launchFragmentWithArgs(
                        PostSleepFragment.class,
                        PostSleepFragment.createArguments(args));
        driver.init(helper, new Assertions(driver));
        
        return driver;
    }
    
    public static PostSleepTestDriver inApplication(ApplicationFragmentTestHelper<PostSleepFragment> helper)
    {
        PostSleepTestDriver driver = new PostSleepTestDriver();
        driver.init(helper, new Assertions(driver));
        return driver;
    }
    
    public void setNavCallbacks(NavCallbacks navCallbacks)
    {
        mNavCallbacks = navCallbacks;
    }
    
    public void keep()
    {
        if (mNavCallbacks != null) {
            mNavCallbacks.onKeep();
        }
        onView(withId(R.id.action_positive)).perform(click());
    }
    
    public void discard()
    {
        clickDiscardButton();
        confirmDiscard();
    }
    
    
    public void up()
    {
        if (mNavCallbacks != null) {
            mNavCallbacks.onUp();
        }
        
        UITestNavigate.up();
    }
    
    public void setRating(float rating)
    {
        getHelper().performSyncedFragmentAction(fragment -> fragment.getViewModel()
                .setRating(rating));
    }
    
    public void clickDiscardButton()
    {
        onView(withId(R.id.action_negative)).perform(click());
    }
    
    public void confirmDiscard()
    {
        if (mNavCallbacks != null) {
            mNavCallbacks.onDiscard();
        }
        DialogTestUtils.pressPositiveButton();
    }
}
