package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.MoodSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagSelectorViewModel;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFormatting;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.Day;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.EspressoActions.setDatePickerDate;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onDatePicker;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.onTimePicker;
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
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<SessionDetailsTestDriver,
            SessionDetailsFragmentViewModel>
    {
        public Assertions(SessionDetailsTestDriver owningDriver)
        {
            super(owningDriver);
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
            getOwningDriver().getHelper().performSyncedFragmentAction(fragment -> {
                hamcrestAssertThat(fragment.getRatingBar().getRating(), is(equalTo(rating)));
            });
        }
        
        public void selectedTagsMatch(List<Tag> tags)
        {
            getOwningDriver().mTagSelector.assertThat.selectedTagsMatch(getTagIdsOf(tags));
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
            onView(withId(R.id.session_details_duration)).check(matches(withText(
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
            onView(withText(R.string.session_details_future_time_error)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
        }
        
        public void overlapErrorDialogIsDisplayed()
        {
            onView(withId(R.id.session_details_overlap_message)).inRoot(isDialog())
                    .check(matches(isDisplayed()));
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
        SessionDetailsTestDriver sessionDetails = new SessionDetailsTestDriver();
        // REFACTOR [21-05-11 10:57PM] -- maybe I should just generally inject the helper.
        HiltFragmentTestHelper<SessionDetailsFragment> helper =
                HiltFragmentTestHelper.launchFragmentWithArgs(
                        SessionDetailsFragment.class,
                        SessionDetailsFragment.createArguments(
                                new SessionDetailsFragment.ArgsBuilder(new SleepSessionWrapper(
                                        sleepSession))
                                        .build()));
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
        onView(withId(R.id.session_data_action_positive)).perform(click());
    }
    
    /**
     * Confirm without notifying the listener. Idk why you would want to use this...
     */
    public void confirmSilently()
    {
        onView(withId(R.id.session_data_action_positive)).perform(click());
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
        setDayInPicker(day);
    }
    
    public void setStartTimeOfDay(TimeOfDay timeOfDay)
    {
        onStartTimeTextView().perform(click());
        setTimeOfDayInPicker(timeOfDay);
    }
    
    public void setEndDay(Day day)
    {
        onEndDateTextView().perform(click());
        setDayInPicker(day);
    }
    
    public void setEndTimeOfDay(TimeOfDay timeOfDay)
    {
        onEndTimeTextView().perform(click());
        setTimeOfDayInPicker(timeOfDay);
    }
    
    public void closeErrorDialog()
    {
        DialogTestUtils.pressPositiveButton();
    }

//*********************************************************
// private methods
//*********************************************************

    private void pressNegativeButton()
    {
        onView(withId(R.id.session_data_action_negative)).perform(click());
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
    
    private void setTimeOfDayInPicker(TimeOfDay timeOfDay)
    {
        onTimePicker().perform(PickerActions.setTime(timeOfDay.hourOfDay, timeOfDay.minute));
        DialogTestUtils.pressPositiveButton();
    }
    
    private void setDayInPicker(Day day)
    {
        onDatePicker().perform(setDatePickerDate(
                day.year,
                day.month,
                day.dayOfMonth));
        DialogTestUtils.pressPositiveButton();
    }
    
    private ViewInteraction onStartDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_details_start_time)),
                            withId(R.id.date)));
    }
    
    private ViewInteraction onEndDateTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_details_end_time)), withId(R.id.date)));
    }
    
    private ViewInteraction onStartTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_details_start_time)),
                            withId(R.id.time)));
    }
    
    private ViewInteraction onEndTimeTextView()
    {
        return onView(allOf(withParent(withId(R.id.session_details_end_time)), withId(R.id.time)));
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
