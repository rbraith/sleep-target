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
import android.widget.TextView;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.RecyclerListItemAssertions;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionFormatting;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleeptarget.ui.session_details.SessionDetailsFormatting;
import com.rbraithwaite.sleeptarget.ui.session_details.SessionDetailsFragment;
import com.rbraithwaite.sleeptarget.ui.session_details.SessionDetailsFragmentViewModel;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoMatchers.recyclerViewWithCount;
import static com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils.setDatePickerTo;
import static com.rbraithwaite.sleeptarget.test_utils.ui.UITestUtils.setTimeOfDayPickerTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

public class SessionDetailsTestDriver
        extends BaseFragmentTestDriver<SessionDetailsFragment, SessionDetailsTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private TagSelectorDriver mTagSelector;
    private MoodSelectorDriver mMoodSelector;
    private OnConfirmListener mOnConfirmListener;
    private OnNegativeActionListener mOnNegativeActionListener;
    
    private OpenInterruptionDetailsListener mOpenInterruptionDetailsListener;

//*********************************************************
// public helpers
//*********************************************************

    // REFACTOR [21-06-25 5:11PM] -- rename this OnPositivePressListener.
    public interface OnConfirmListener
    {
        void onConfirm();
    }
    
    public interface OnNegativeActionListener
    {
        void onNegativeAction();
    }
    
    public interface OpenInterruptionDetailsListener
    {
        void onOpenInterruptionDetails();
    }
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<SessionDetailsTestDriver,
            SessionDetailsFragmentViewModel>
    {
        public Assertions(SessionDetailsTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void displayedValuesMatch(SleepSessionBuilder sleepSession)
        {
            displayedValuesMatch(sleepSession.build());
        }
        
        public void displayedValuesMatch(SleepSession sleepSession)
        {
            startDateAndTimeMatch(sleepSession.getStart());
            endDateAndTimeMatch(sleepSession.getEnd());
            durationMatches(sleepSession.getDurationMillis());
            additionalCommentsMatch(sleepSession.getAdditionalComments());
            moodMatches(sleepSession.getMood());
            selectedTagsMatch(sleepSession.getTags());
            ratingMatches(sleepSession.getRating());
            // TODO [21-07-21 2:41PM] -- this should include interruptions.
        }
        
        public void ratingMatches(float rating)
        {
            getOwningDriver().getHelper().performSyncedFragmentAction(fragment -> {
                hamcrestAssertThat(fragment.getRatingBar().getRating(), is(equalTo(rating)));
            });
        }
        
        public void selectedTagsMatch(List<Tag> tags)
        {
            getOwningDriver().mTagSelector.assertThat.selectedTagsMatchText(
                    tags.stream().map(Tag::getText).collect(Collectors.toList()));
        }
        
        public void moodMatches(Mood mood)
        {
            getOwningDriver().mMoodSelector.assertThat.selectedMoodMatches(mood);
        }
        
        public void additionalCommentsMatch(String additionalComments)
        {
            additionalComments = additionalComments == null ? "" : additionalComments;
            onView(withId(R.id.session_details_comments)).check(matches(withText(additionalComments)));
        }
        
        public void durationMatches(long durationMillis)
        {
            onView(withId(R.id.common_session_times_duration)).check(matches(withText(
                    SessionDetailsFormatting.formatDuration(durationMillis))));
        }
        
        public void endDateAndTimeMatch(Date end)
        {
            GregorianCalendar cal = TimeUtils.getCalendarFrom(end);
            
            endDateMatches(cal.get(Calendar.YEAR),
                           cal.get(Calendar.MONTH),
                           cal.get(Calendar.DAY_OF_MONTH));
            endTimeOfDayMatches(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        
        public void endTimeOfDayMatches(int hourOfDay, int minute)
        {
            onEndTimeOfDay().check(matches(withText(SessionDetailsFormatting.formatTimeOfDay(
                    hourOfDay,
                    minute))));
        }
        
        public void endDateMatches(int year, int month, int dayOfMonth)
        {
            onEndDate().check(matches(withText(SessionDetailsFormatting.formatDate(year,
                                                                                   month,
                                                                                   dayOfMonth))));
        }
        
        public void startDateAndTimeMatch(Date start)
        {
            GregorianCalendar cal = TimeUtils.getCalendarFrom(start);
            
            startDateMatches(cal.get(Calendar.YEAR),
                             cal.get(Calendar.MONTH),
                             cal.get(Calendar.DAY_OF_MONTH));
            startTimeOfDayMatches(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
        }
        
        public void startTimeOfDayMatches(int hourOfDay, int minute)
        {
            onStartTimeOfDay().check(matches(withText(SessionDetailsFormatting.formatTimeOfDay(
                    hourOfDay,
                    minute))));
        }
        
        public void startDateMatches(int year, int month, int dayOfMonth)
        {
            onStartDate().check(matches(withText(SessionDetailsFormatting.formatDate(year,
                                                                                     month,
                                                                                     dayOfMonth))));
        }
        
        public void invalidStartErrorDialogIsDisplayed()
        {
            onView(withText(R.string.error_session_edit_start_datetime)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
        
        public void invalidEndErrorDialogIsDisplayed()
        {
            onView(withText(R.string.error_session_edit_end_datetime)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
        
        public void endDoesNotMatch(Date end)
        {
            GregorianCalendar cal = TimeUtils.getCalendarFrom(end);
            
            // REFACTOR [21-06-25 6:20PM] -- extract these to endDateDoesNotMatch and
            //  endTimeOfDayDoesNotMatch.
            // REFACTOR [21-06-25 6:20PM] -- maybe make a checkEndDay(day, shouldMatch)
            //  - then inside use not() if shouldMatch is false
            //  - repeat for endTimeOfDat, & for start day/timeOfDay.
            onEndTimeOfDay().check(matches(not(withText(SessionDetailsFormatting.formatTimeOfDay(
                    cal.get(Calendar.HOUR_OF_DAY),
                    cal.get(Calendar.MINUTE))))));
            
            onEndDate().check(matches(not(withText(SessionDetailsFormatting.formatDate(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH))))));
        }
        
        public void futureTimeErrorDialogIsDisplayed()
        {
            onView(withText(R.string.session_future_time_error)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
        
        public void overlapErrorDialogIsDisplayed()
        {
            onView(withId(R.id.session_details_overlap_message)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
        
        public void interruptionDetailsMatch(Interruptions interruptions)
        {
            if (interruptions == null || interruptions.isEmpty()) {
                noInterruptionsAreDisplayed();
            } else {
                interruptionsCountMatches(interruptions.getCount());
                interruptionsTimeMatches(interruptions.getTotalDuration());
                interruptionsListMatches(interruptions.asList());
            }
        }
        
        public void noInterruptionsAreDisplayed()
        {
            getOwningDriver().scrollToInterruptions();
            
            interruptionsCountMatches(0);
            interruptionsTimeMatches(0);
            
            onView(withId(R.id.common_interruptions_listitem_start)).check(doesNotExist());
        }
        
        public void interruptionsListMatches(List<Interruption> interruptionsList)
        {
            getOwningDriver().scrollToInterruptions();
            
            onView(withId(R.id.common_interruptions_recycler)).check(matches(
                    // + 1 for add button
                    recyclerViewWithCount(interruptionsList.size() + 1)));
            
            for (int i = 0; i < interruptionsList.size(); i++) {
                interruptionAtPosition(i).matches(interruptionsList.get(i));
            }
        }
        
        public InterruptionListItemAssertions interruptionAtPosition(int position)
        {
            // +1 to account for the add button
            return new InterruptionListItemAssertions(position + 1,
                                                      R.id.common_interruptions_recycler);
        }
        
        public void interruptionsCountMatches(int expectedCount)
        {
            getOwningDriver().scrollToInterruptions();
            
            onView(withId(R.id.common_interruptions_count)).check(matches(withText(
                    String.valueOf(expectedCount))));
        }
        
        public void interruptionsTimeMatches(long durationMillis)
        {
            getOwningDriver().scrollToInterruptions();
            
            onView(withId(R.id.common_interruptions_total)).check(matches(withText(
                    InterruptionFormatting.formatDuration(durationMillis))));
        }
        
        // REFACTOR [21-05-14 3:40AM] -- extract this, it duplicates getIdsFromTags()
        //  in SleepTrackerViewModel.
        private List<Integer> getTagIdsOf(List<Tag> tags)
        {
            return tags.stream().map(Tag::getTagId).collect(Collectors.toList());
        }
        
        private ViewInteraction onStartDate()
        {
            return getOwningDriver().onStartDateTextView();
        }
        
        private ViewInteraction onEndDate()
        {
            return getOwningDriver().onEndDateTextView();
        }
        
        private ViewInteraction onStartTimeOfDay()
        {
            return getOwningDriver().onStartTimeTextView();
        }
        
        private ViewInteraction onEndTimeOfDay()
        {
            return getOwningDriver().onEndTimeTextView();
        }
    }
    
    public static class InterruptionListItemAssertions
            extends RecyclerListItemAssertions
    {
        public InterruptionListItemAssertions(int listItemIndex, int recyclerId)
        {
            super(listItemIndex, recyclerId);
        }
        
        public void matches(InterruptionBuilder interruption)
        {
            matches(interruption.build());
        }
        
        public void matches(Interruption interruption)
        {
            hasStartMatching(interruption.getStart());
            // REFACTOR [21-07-31 12:05AM] this should all just be longs.
            hasDurationMatching((int) interruption.getDurationMillis());
            hasReasonMatching(interruption.getReason());
        }
        
        public void hasStartMatching(Date start)
        {
            checkThatThisListItemHasContentsMatching(listItemWithStart(start));
        }
        
        public void hasDurationMatching(int durationMillis)
        {
            checkThatThisListItemHasContentsMatching(allOf(
                    withId(R.id.common_interruptions_listitem_duration),
                    withText(InterruptionFormatting.formatDuration(durationMillis))));
        }
        
        public void hasReasonMatching(String reason)
        {
            checkThatThisListItemHasContentsMatching(listItemWithReason(reason));
        }
        
        private Matcher<View> listItemWithStart(Date start)
        {
            return new BoundedMatcher<View, TextView>(TextView.class)
            {
                @Override
                protected boolean matchesSafely(TextView item)
                {
                    return item.getText().equals(InterruptionFormatting.formatListItemStart(start));
                }
                
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("has start '" + start.toString() + "'");
                }
            };
        }
        
        private Matcher<View> listItemWithReason(String reason)
        {
            return new BoundedMatcher<View, TextView>(TextView.class)
            {
                @Override
                protected boolean matchesSafely(TextView item)
                {
                    return item.getText()
                            .equals(InterruptionFormatting.formatListItemReason(reason));
                }
                
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("has reason '" + reason + "'");
                }
            };
        }
    }


//*********************************************************
// constructors
//*********************************************************

    private SessionDetailsTestDriver()
    {
    }

//*********************************************************
// api
//*********************************************************

    public static SessionDetailsTestDriver startingWith(SleepSession sleepSession)
    {
        SessionDetailsFragment.Args args = new SessionDetailsFragment.Args();
        args.initialData = new SleepSessionWrapper(sleepSession);
        
        SessionDetailsTestDriver sessionDetails = new SessionDetailsTestDriver();
        // REFACTOR [21-05-11 10:57PM] -- maybe I should just generally inject the helper.
        HiltFragmentTestHelper<SessionDetailsFragment> helper =
                HiltFragmentTestHelper.launchFragmentWithArgs(
                        SessionDetailsFragment.class,
                        SessionDetailsFragment.createArguments(args));
        sessionDetails.init(helper, new SessionDetailsTestDriver.Assertions(sessionDetails));
        sessionDetails.initSelectors();
        return sessionDetails;
    }
    
    public static SessionDetailsTestDriver inApplication(ApplicationFragmentTestHelper<SessionDetailsFragment> helper)
    {
        SessionDetailsTestDriver sessionDetails = new SessionDetailsTestDriver();
        sessionDetails.init(helper, new SessionDetailsTestDriver.Assertions(sessionDetails));
        sessionDetails.initSelectors();
        return sessionDetails;
    }
    
    public void scrollToInterruptions()
    {
        onView(withId(R.id.session_details_interruptions_card)).perform(scrollTo());
    }
    
    public void setRating(float rating)
    {
        getHelper().performSyncedFragmentAction(fragment -> fragment.getRatingBar()
                .setRating(rating));
    }
    
    public void setOnConfirmListener(OnConfirmListener onConfirmListener)
    {
        mOnConfirmListener = onConfirmListener;
    }
    
    public void setOnNegativeActionListener(OnNegativeActionListener onNegativeActionListener)
    {
        mOnNegativeActionListener = onNegativeActionListener;
    }
    
    public void confirm()
    {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onConfirm();
        }
        onView(withId(R.id.action_positive)).perform(click());
    }
    
    /**
     * Confirm without notifying the listener. Idk why you would want to use this...
     */
    public void confirmSilently()
    {
        onView(withId(R.id.action_positive)).perform(click());
    }
    
    public void setStart(Date start)
    {
        GregorianCalendar cal = TimeUtils.getCalendarFrom(start);
        setStartDay(Day.of(cal));
        setStartTimeOfDay(TimeOfDay.of(cal));
    }
    
    public void setEnd(Date end)
    {
        GregorianCalendar cal = TimeUtils.getCalendarFrom(end);
        setEndDay(Day.of(cal));
        setEndTimeOfDay(TimeOfDay.of(cal));
    }
    
    public void setValuesTo(SleepSession sleepSession)
    {
        // REFACTOR [21-07-29 3:22PM] -- use InterruptionDetailsTestDriver.setValuesTo's strat
        //  for safely setting the times instead.
        // set start back one day at first to give the end space to be set
        GregorianCalendar cal = TimeUtils.getCalendarFrom(sleepSession.getStart());
        cal.add(Calendar.DAY_OF_MONTH, -1);
        setStart(cal.getTime());
        
        setEnd(sleepSession.getEnd());
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        setStartDay(Day.of(cal));
        
        setMood(sleepSession.getMood());
        setSelectedTags(sleepSession.getTags());
        setAdditionalComments(sleepSession.getAdditionalComments());
        setRating(sleepSession.getRating());
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}. This assumes that this
     * SessionDetailsFragment was accessed in an edit-mode from the archive.
     */
    public void deleteSession()
    {
        if (mOnNegativeActionListener != null) {
            mOnNegativeActionListener.onNegativeAction();
        }
        pressNegativeButton();
        DialogTestUtils.pressPositiveButton();
    }
    
    public void confirmDelete()
    {
        if (mOnNegativeActionListener != null) {
            mOnNegativeActionListener.onNegativeAction();
        }
        DialogTestUtils.pressPositiveButton();
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}. This assumes that this
     * SessionDetailsFragment was accessed in an add-mode from the archive.
     */
    public void cancel()
    {
        if (mOnNegativeActionListener != null) {
            mOnNegativeActionListener.onNegativeAction();
        }
        pressNegativeButton();
    }
    
    public void setStartDay(Day day)
    {
        onStartDateTextView().perform(click());
        setDatePickerTo(day);
    }
    
    public void setStartTimeOfDay(TimeOfDay timeOfDay)
    {
        onStartTimeTextView().perform(click());
        setTimeOfDayPickerTo(timeOfDay);
    }
    
    public void setEndDay(Day day)
    {
        onEndDateTextView().perform(click());
        setDatePickerTo(day);
    }
    
    public void setEndTimeOfDay(TimeOfDay timeOfDay)
    {
        onEndTimeTextView().perform(click());
        setTimeOfDayPickerTo(timeOfDay);
    }
    
    public void closeErrorDialog()
    {
        DialogTestUtils.pressPositiveButton();
    }
    
    public void setOpenInterruptionDetailsListener(OpenInterruptionDetailsListener openInterruptionDetailsListener)
    {
        mOpenInterruptionDetailsListener = openInterruptionDetailsListener;
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}. 0 is the first
     * interruptions index (i.e. you don't need to account for the add button)
     */
    public void openInterruptionDetailsFor(int interruptionsListIndex)
    {
        if (mOpenInterruptionDetailsListener != null) {
            mOpenInterruptionDetailsListener.onOpenInterruptionDetails();
        }
        scrollToInterruptions();
        onView(withId(R.id.common_interruptions_recycler)).perform(RecyclerViewActions.actionOnItemAtPosition(
                interruptionsListIndex + 1,
                click()));
    }
    
    public void pressAddNewInterruptionButton()
    {
        if (mOpenInterruptionDetailsListener != null) {
            mOpenInterruptionDetailsListener.onOpenInterruptionDetails();
        }
        scrollToInterruptions();
        onView(withId(R.id.session_details_interruptions_addbtn)).perform(click());
    }

//*********************************************************
// private methods
//*********************************************************

    public void pressNegativeButton()
    {
        onView(withId(R.id.action_negative)).perform(click());
    }
    
    private void setMood(Mood mood)
    {
        mMoodSelector.setMood(mood);
    }
    
    private void setSelectedTags(List<Tag> tags)
    {
        mTagSelector.setSelectedTags(tags);
    }
    
    private void setAdditionalComments(String additionalComments)
    {
        additionalComments = additionalComments == null ? "" : additionalComments;
        UITestUtils.typeOnMultilineEditText(additionalComments,
                                            onView(withId(R.id.session_details_comments)));
    }
    
    private ViewInteraction onStartDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                            withId(R.id.date)));
    }
    
    private ViewInteraction onEndDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_end)), withId(R.id.date)));
    }
    
    private ViewInteraction onStartTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                            withId(R.id.time)));
    }
    
    private ViewInteraction onEndTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_end)), withId(R.id.time)));
    }
    
    private MoodSelectorViewModel getMoodSelectorViewModel()
    {
        TestUtils.DoubleRef<MoodSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        getHelper().performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getMoodSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private TagSelectorViewModel getTagSelectorViewModel()
    {
        TestUtils.DoubleRef<TagSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        getHelper().performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getTagSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private void initSelectors()
    {
        mTagSelector = new TagSelectorDriver(
                getHelper(),
                withId(R.id.session_details_tags),
                getTagSelectorViewModel());
        mMoodSelector = new MoodSelectorDriver(
                withId(R.id.session_details_mood),
                getMoodSelectorViewModel());
    }
}
