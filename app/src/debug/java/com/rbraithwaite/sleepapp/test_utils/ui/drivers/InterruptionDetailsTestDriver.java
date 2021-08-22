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

package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.common.views.session_times.SessionTimesFormatting;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.time.Day;
import com.rbraithwaite.sleepapp.utils.time.TimeOfDay;

import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.setDatePickerTo;
import static com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils.setTimeOfDayPickerTo;
import static org.hamcrest.Matchers.allOf;

// REFACTOR [21-07-27 1:56AM] -- a lot of this duplicates SessionDetailsTestDriver.
public class InterruptionDetailsTestDriver
        extends BaseFragmentTestDriver<InterruptionDetailsFragment,
        InterruptionDetailsTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private OnNegativeActionListener mOnNegativeActionListener;
    private OnPositiveActionListener mOnPositiveActionListener;

//*********************************************************
// public helpers
//*********************************************************

    public interface OnNegativeActionListener
    {
        void onNegativeAction();
    }
    
    public interface OnPositiveActionListener
    {
        void onPositiveAction();
    }
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<InterruptionDetailsTestDriver,
            InterruptionDetailsFragmentViewModel>
    {
        public Assertions(InterruptionDetailsTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void valuesMatch(InterruptionBuilder interruption)
        {
            valuesMatch(interruption.build());
        }
        
        public void valuesMatch(Interruption interruption)
        {
            startTimesMatch(interruption.getStart());
            endTimesMatch(interruption.getEnd());
            reasonMatches(interruption.getReason());
        }
        
        public void startTimesMatch(Date expectedStart)
        {
            startDayMatches(Day.of(expectedStart));
            startTimeOfDayMatches(TimeOfDay.of(expectedStart));
        }
        
        public void startTimeOfDayMatches(TimeOfDay startTimeOfDay)
        {
            onStartTimeOfDay().check(matches(withText(SessionTimesFormatting.formatTimeOfDay(
                    startTimeOfDay.hourOfDay, startTimeOfDay.minute))));
        }
        
        public void startDayMatches(Day startDay)
        {
            onStartDay().check(matches(withText(SessionTimesFormatting.formatDate(
                    startDay.year, startDay.month, startDay.dayOfMonth))));
        }
        
        public void endTimesMatch(Date end)
        {
            endDayMatches(Day.of(end));
            endTimeOfDayMatches(TimeOfDay.of(end));
        }
        
        public void endTimeOfDayMatches(TimeOfDay endTimeOfDay)
        {
            onEndTimeOfDay().check(matches(withText(SessionTimesFormatting.formatTimeOfDay(
                    endTimeOfDay.hourOfDay, endTimeOfDay.minute))));
        }
        
        public void endDayMatches(Day endDay)
        {
            onEndDay().check(matches(withText(SessionTimesFormatting.formatDate(
                    endDay.year, endDay.month, endDay.dayOfMonth))));
        }
        
        public void reasonMatches(String reason)
        {
            onView(withId(R.id.interruptions_details_reason)).check(matches(withText(reason)));
        }
        
        private ViewInteraction onStartDay()
        {
            return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                                withId(R.id.date)));
        }
        
        private ViewInteraction onStartTimeOfDay()
        {
            return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                                withId(R.id.time)));
        }
        
        private ViewInteraction onEndDay()
        {
            return onView(allOf(withParent(withId(R.id.common_session_times_end)),
                                withId(R.id.date)));
        }
        
        private ViewInteraction onEndTimeOfDay()
        {
            return onView(allOf(withParent(withId(R.id.common_session_times_end)),
                                withId(R.id.time)));
        }
    }

//*********************************************************
// constructors
//*********************************************************

    private InterruptionDetailsTestDriver() {}

//*********************************************************
// api
//*********************************************************

    public static InterruptionDetailsTestDriver inApplication(ApplicationFragmentTestHelper<InterruptionDetailsFragment> helper)
    {
        InterruptionDetailsTestDriver driver = new InterruptionDetailsTestDriver();
        driver.init(helper, new Assertions(driver));
        return driver;
    }
    
    public void setOnNegativeActionListener(OnNegativeActionListener onNegativeActionListener)
    {
        mOnNegativeActionListener = onNegativeActionListener;
    }
    
    public void setOnPositiveActionListener(OnPositiveActionListener onPositiveActionListener)
    {
        mOnPositiveActionListener = onPositiveActionListener;
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}. This assumes that this
     * InterruptionDetailsFragment was accessed in an edit-mode from the session details.
     */
    public void deleteInterruption()
    {
        if (mOnNegativeActionListener != null) {
            mOnNegativeActionListener.onNegativeAction();
        }
        pressNegativeButton();
        DialogTestUtils.pressPositiveButton();
    }
    
    public void setValuesTo(InterruptionBuilder interruptionBuilder)
    {
        setValuesTo(interruptionBuilder.build());
    }
    
    public void setValuesTo(Interruption interruption)
    {
        Interruption current = getCurrentInterruption();
        if (interruption.getEnd().getTime() > current.getEnd().getTime()) {
            // the new interruption end comes after the current one, so we can safely set the end
            // first
            setEndDayTo(Day.of(interruption.getEnd()));
            setEndTimeOfDayTo(TimeOfDay.of(interruption.getEnd()));
            setStartDayTo(Day.of(interruption.getStart()));
            setStartTimeOfDayTo(TimeOfDay.of(interruption.getStart()));
        } else {
            // otherwise we set the start first
            setStartDayTo(Day.of(interruption.getStart()));
            setStartTimeOfDayTo(TimeOfDay.of(interruption.getStart()));
            setEndDayTo(Day.of(interruption.getEnd()));
            setEndTimeOfDayTo(TimeOfDay.of(interruption.getEnd()));
        }
        setReasonTo(interruption.getReason());
    }
    
    public void setReasonTo(String reason)
    {
        UITestUtils.typeOnMultilineEditText(reason,
                                            onView(withId(R.id.interruptions_details_reason)));
    }
    
    public void setEndDayTo(Day endDay)
    {
        onEndDayView().perform(click());
        setDatePickerTo(endDay);
    }
    
    public void setEndTimeOfDayTo(TimeOfDay endTimeOfDay)
    {
        onEndTimeOfDayView().perform(click());
        setTimeOfDayPickerTo(endTimeOfDay);
    }
    
    public void setStartDayTo(Day startDay)
    {
        onStartDayView().perform(click());
        setDatePickerTo(startDay);
    }
    
    public void setStartTimeOfDayTo(TimeOfDay startTimeOfDay)
    {
        onStartTimeOfDayView().perform(click());
        setTimeOfDayPickerTo(startTimeOfDay);
    }
    
    public Interruption getCurrentInterruption()
    {
        AtomicReference<Interruption> current = new AtomicReference<>();
        getHelper().performSyncedFragmentAction(fragment -> {
            current.set(fragment.getViewModel().getResult().getInterruption());
        });
        return current.get();
    }
    
    public void confirm()
    {
        if (mOnPositiveActionListener != null) {
            mOnPositiveActionListener.onPositiveAction();
        }
        onView(withId(R.id.action_positive)).perform(click());
    }
    
//*********************************************************
// private methods
//*********************************************************

    private ViewInteraction onStartTimeOfDayView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                            withId(R.id.time)));
    }
    
    private ViewInteraction onStartDayView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_start)),
                            withId(R.id.date)));
    }
    
    private ViewInteraction onEndTimeOfDayView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_end)),
                            withId(R.id.time)));
    }
    
    private ViewInteraction onEndDayView()
    {
        return onView(allOf(withParent(withId(R.id.common_session_times_end)),
                            withId(R.id.date)));
    }

    private void pressNegativeButton()
    {
        onView(withId(R.id.action_negative)).perform(click());
    }
}
