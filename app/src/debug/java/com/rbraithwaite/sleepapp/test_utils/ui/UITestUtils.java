package com.rbraithwaite.sleepapp.test_utils.ui;

import android.widget.DatePicker;

import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewInteraction;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.SimpleCompletableFuture;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import java.util.concurrent.ExecutionException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.equalTo;

public class UITestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private UITestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static ViewInteraction onDatePicker()
    {
        return onView(withClassName(equalTo(DatePicker.class.getName())));
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
