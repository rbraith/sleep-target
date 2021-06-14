package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.common.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFormatting;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SessionDetailsTestDriver
{
//*********************************************************
// private properties
//*********************************************************

    private FragmentTestHelper<SessionDetailsFragment> mHelper;
    private TagSelectorDriver mTagSelector;
    private MoodSelectorDriver mMoodSelector;
    private OnConfirmListener mOnConfirmListener;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;

//*********************************************************
// public helpers
//*********************************************************

    public interface OnConfirmListener
    {
        void onConfirm();
    }
    
    public static class Assertions
    {
        private SessionDetailsTestDriver mOwningSessionDetails;
        
        private Assertions(SessionDetailsTestDriver owningSessionDetails)
        {
            mOwningSessionDetails = owningSessionDetails;
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
        }
        
        public void ratingMatches(float rating)
        {
            mOwningSessionDetails.mHelper.performSyncedFragmentAction(fragment -> {
                assertThat(fragment.getRatingBar().getRating(), is(equalTo(rating)));
            });
        }
        
        public void selectedTagsMatch(List<Tag> tags)
        {
            mOwningSessionDetails.mTagSelector.assertThat.selectedTagsMatch(getTagIdsOf(tags));
        }
        
        public void moodMatches(Mood mood)
        {
            mOwningSessionDetails.mMoodSelector.assertThat.selectedMoodMatches(mood.asIndex());
        }
        
        public void additionalCommentsMatch(String additionalComments)
        {
            onView(withId(R.id.session_details_comments)).check(matches(withText(additionalComments)));
        }
        
        public void durationMatches(long durationMillis)
        {
            onView(withId(R.id.session_details_duration)).check(matches(withText(
                    // REFACTOR [21-05-10 10:37PM] -- This should be SessionDetailsFormatting.
                    new DurationFormatter().formatDurationMillis(durationMillis))));
        }
        
        public void endDateAndTimeMatch(Date end)
        {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(end);
            
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
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(start);
            
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
        
        // REFACTOR [21-05-14 3:40AM] -- extract this, it duplicates getIdsFromTags()
        //  in SleepTrackerViewModel.
        private List<Integer> getTagIdsOf(List<Tag> tags)
        {
            return tags.stream().map(Tag::getTagId).collect(Collectors.toList());
        }
        
        private ViewInteraction onStartDate()
        {
            return onView(allOf(withParent(withId(R.id.session_details_start_time)),
                                withId(R.id.date)));
        }
        
        private ViewInteraction onEndDate()
        {
            return onView(allOf(withParent(withId(R.id.session_details_end_time)),
                                withId(R.id.date)));
        }
        
        private ViewInteraction onStartTimeOfDay()
        {
            return onView(allOf(withParent(withId(R.id.session_details_start_time)),
                                withId(R.id.time)));
        }
        
        private ViewInteraction onEndTimeOfDay()
        {
            return onView(allOf(withParent(withId(R.id.session_details_end_time)),
                                withId(R.id.time)));
        }
    }

//*********************************************************
// constructors
//*********************************************************

    private SessionDetailsTestDriver()
    {
        assertThat = new Assertions(this);
    }

//*********************************************************
// api
//*********************************************************

    public static SessionDetailsTestDriver startingWith(SleepSession sleepSession)
    {
        SessionDetailsTestDriver sessionDetails = new SessionDetailsTestDriver();
        // REFACTOR [21-05-11 10:57PM] -- maybe I should just generally inject the helper.
        sessionDetails.mHelper = HiltFragmentTestHelper.launchFragmentWithArgs(
                SessionDetailsFragment.class,
                SessionDetailsFragment.createArguments(
                        new SessionDetailsFragment.ArgsBuilder(new SleepSessionWrapper(sleepSession))
                                .build()));
        sessionDetails.initSelectors();
        return sessionDetails;
    }
    
    public static SessionDetailsTestDriver inApplication(ApplicationFragmentTestHelper<SessionDetailsFragment> helper)
    {
        SessionDetailsTestDriver sessionDetails = new SessionDetailsTestDriver();
        sessionDetails.mHelper = helper;
        sessionDetails.initSelectors();
        return sessionDetails;
    }
    
    public void setRating(float rating)
    {
        mHelper.performSyncedFragmentAction(fragment -> fragment.getRatingBar().setRating(rating));
    }
    
    public void setOnConfirmListener(OnConfirmListener onConfirmListener)
    {
        mOnConfirmListener = onConfirmListener;
    }
    
    public void confirm()
    {
        if (mOnConfirmListener != null) {
            mOnConfirmListener.onConfirm();
        }
        onView(withId(R.id.session_data_action_positive)).perform(click());
    }

//*********************************************************
// private methods
//*********************************************************

    private MoodSelectorViewModel getMoodSelectorViewModel()
    {
        TestUtils.DoubleRef<MoodSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        mHelper.performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getMoodSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private TagSelectorViewModel getTagSelectorViewModel()
    {
        TestUtils.DoubleRef<TagSelectorViewModel> viewModel = new TestUtils.DoubleRef<>(null);
        mHelper.performSyncedFragmentAction(fragment -> {
            viewModel.ref = fragment.getTagSelectorViewModel();
        });
        return viewModel.ref;
    }
    
    private void initSelectors()
    {
        mTagSelector = new TagSelectorDriver(
                // REFACTOR [21-05-14 3:39AM] -- change remaining instances of "session_data" to
                //  "session_details".
                withId(R.id.session_details_tags),
                getTagSelectorViewModel());
        mMoodSelector = new MoodSelectorDriver(
                withId(R.id.session_details_mood),
                getMoodSelectorViewModel());
    }
}
