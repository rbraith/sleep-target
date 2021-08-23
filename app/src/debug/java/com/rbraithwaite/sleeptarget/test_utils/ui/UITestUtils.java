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

package com.rbraithwaite.sleeptarget.test_utils.ui;

import android.app.Activity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.contrib.PickerActions;
import androidx.test.espresso.matcher.ViewMatchers;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.test_utils.SimpleCompletableFuture;
import com.rbraithwaite.sleeptarget.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleeptarget.ui.MainActivity;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleeptarget.utils.time.Day;
import com.rbraithwaite.sleeptarget.utils.time.TimeOfDay;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.rbraithwaite.sleeptarget.test_utils.ui.EspressoActions.setDatePickerDate;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class UITestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private UITestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static <T extends Activity> ActivityScenario<T> restartApp(
            ActivityScenario<T> scenario,
            Class<T> activityClass)
    {
        scenario.close();
        return ActivityScenario.launch(activityClass);
    }
    
    public static void checkBottomNavIsDisplayed(boolean shouldBeDisplayed)
    {
        if (shouldBeDisplayed) {
            onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
        } else {
            onView(withId(R.id.main_bottomnav)).check(matches(not(isDisplayed())));
        }
    }
    
    public static void checkSnackbarIsDisplayedWithMessage(int stringId)
    {
        // https://stackoverflow.com/a/39915776
        // https://stackoverflow.com/a/33245290
        onView(allOf(withId(com.google.android.material.R.id.snackbar_text), withText(stringId)))
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
    
    public static ViewInteraction onDatePicker()
    {
        return onView(withClassName(equalTo(DatePicker.class.getName())));
    }
    
    public static ViewInteraction onTimePicker()
    {
        return onView(withClassName(equalTo(TimePicker.class.getName())));
    }
    
    public static void setDatePickerTo(Day day)
    {
        onDatePicker().perform(setDatePickerDate(
                day.year,
                day.month,
                day.dayOfMonth));
        DialogTestUtils.pressPositiveButton();
    }
    
    public static void setTimeOfDayPickerTo(TimeOfDay timeOfDay)
    {
        onTimePicker().perform(PickerActions.setTime(timeOfDay.hourOfDay, timeOfDay.minute));
        DialogTestUtils.pressPositiveButton();
    }
    
    public static void typeOnMultilineEditText(String text, ViewInteraction editText)
    {
        editText.perform(replaceText(text)).perform(closeSoftKeyboard());
    }
    
    /**
     * Assumes that the session archive fragment is displayed.
     */
    public static int getSessionArchiveCount(ActivityScenario<MainActivity> scenario) throws
            InterruptedException,
            ExecutionException
    {
        final SimpleCompletableFuture<Integer> sessionArchiveCount =
                new SimpleCompletableFuture<>();
        // assumes session archive fragment is open
        scenario.onActivity(activity -> {
            // https://stackoverflow.com/a/59279744
            Fragment navHostFragment =
                    activity.getSupportFragmentManager().findFragmentById(R.id.main_navhost);
            SessionArchiveFragment fragment =
                    (SessionArchiveFragment) navHostFragment.getChildFragmentManager()
                            .getFragments()
                            .get(0);
            
            sessionArchiveCount.complete(fragment.getRecyclerViewAdapter().getItemCount());
        });
        return sessionArchiveCount.get();
    }
    
    /**
     * This blocks its thread until a displaying snackbar has finished. It does not work with
     * snackbars using Snackbar.LENGTH_INDEFINITE
     */
    public static void waitForSnackbarToFinish()
    {
        // HACK [21-02-8 9:48PM] -- This is a ducktape solution, Thread.sleep = bad bad bad. Figure
        //  out a way to query the state of the snackbar and block the thread accordingly.
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
