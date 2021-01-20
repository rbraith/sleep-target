package com.rbraithwaite.sleepapp.test_utils.ui;

import android.app.Activity;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.matcher.ViewMatchers;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.SimpleCompletableFuture;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
    
    public static void pressDialogOK()
    {
        // button1 is dialog positive btn
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
    }
    
    // TODO [20-12-5 8:09PM] -- pressDialogCancel()
    
    public static ViewInteraction onDatePicker()
    {
        return onView(withClassName(equalTo(DatePicker.class.getName())));
    }
    
    public static ViewInteraction onTimePicker()
    {
        return onView(withClassName(equalTo(TimePicker.class.getName())));
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
        scenario.onActivity(new ActivityScenario.ActivityAction<MainActivity>()
        {
            @Override
            public void perform(MainActivity activity)
            {
                // https://stackoverflow.com/a/59279744
                Fragment navHostFragment =
                        activity.getSupportFragmentManager().findFragmentById(R.id.main_navhost);
                SessionArchiveFragment fragment =
                        (SessionArchiveFragment) navHostFragment.getChildFragmentManager()
                                .getFragments()
                                .get(0);
                
                sessionArchiveCount.complete(fragment.getRecyclerViewAdapter().getItemCount());
            }
        });
        return sessionArchiveCount.get();
    }
}
