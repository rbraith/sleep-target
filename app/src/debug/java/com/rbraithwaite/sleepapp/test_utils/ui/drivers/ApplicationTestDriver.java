package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;



/**
 * Drives the entire application. Starts on the sleep tracker screen.
 */
public class ApplicationTestDriver
{
//*********************************************************
// private properties
//*********************************************************

    // MainActivity starts on the sleep tracker
    private Destination mCurrentLocation = Destination.SLEEP_TRACKER;
    private SleepTrackerTestDriver mSleepTracker;
    private SessionArchiveTestDriver mSessionArchive;
    private SessionDetailsTestDriver mSessionDetails;

//*********************************************************
// package properties
//*********************************************************

    ApplicationFragmentTestHelper.ScenarioCallbacks mScenarioCallbacks;


//*********************************************************
// public helpers
//*********************************************************

    public enum Destination
    {
        ARCHIVE,
        SLEEP_TRACKER,
        SESSION_DETAILS
    }

//*********************************************************
// constructors
//*********************************************************

    public ApplicationTestDriver()
    {
        mScenarioCallbacks = createScenarioCallbacks();
    }

//*********************************************************
// api
//*********************************************************

    public SleepTrackerTestDriver getSleepTracker()
    {
        mSleepTracker = CommonUtils.lazyInit(mSleepTracker,
                                             () -> new SleepTrackerTestDriver(new ApplicationFragmentTestHelper<>(
                                                     mScenarioCallbacks)));
        return mCurrentLocation == Destination.SLEEP_TRACKER ? mSleepTracker : null;
    }
    
    public SessionArchiveTestDriver getSessionArchive()
    {
        mSessionArchive = CommonUtils.lazyInit(mSessionArchive, () -> new SessionArchiveTestDriver(
                new ApplicationFragmentTestHelper<>(mScenarioCallbacks),
                () -> mCurrentLocation = Destination.SESSION_DETAILS));
        return mCurrentLocation == Destination.ARCHIVE ? mSessionArchive : null;
    }
    
    public SessionDetailsTestDriver getSessionDetails()
    {
        mSessionDetails = CommonUtils.lazyInit(mSessionDetails, () -> {
            SessionDetailsTestDriver sessionDetails =
                    SessionDetailsTestDriver.inApplication(new ApplicationFragmentTestHelper<>(
                            mScenarioCallbacks));
            sessionDetails.setOnConfirmListener(() -> mCurrentLocation = Destination.ARCHIVE);
            sessionDetails.setOnNegativeActionListener(() -> mCurrentLocation =
                    Destination.ARCHIVE);
            return sessionDetails;
        });
        return mCurrentLocation == Destination.SESSION_DETAILS ? mSessionDetails : null;
    }
    
    public void navigateTo(Destination destination)
    {
        if (destination == mCurrentLocation) {
            return;
        }
        
        switch (destination) {
        case ARCHIVE:
            navigateToArchiveFrom(mCurrentLocation);
            break;
        default:
            throw new RuntimeException("Invalid destination: " + destination.toString());
        }
    }
    
    public void navigateUp()
    {
        if (mCurrentLocation == Destination.SLEEP_TRACKER) {
            return;
        } else if (mCurrentLocation == Destination.SESSION_DETAILS) {
            mCurrentLocation = Destination.ARCHIVE;
        } else {
            mCurrentLocation = Destination.SLEEP_TRACKER;
        }
        
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click());
    }

//*********************************************************
// private methods
//*********************************************************

    private ApplicationFragmentTestHelper.ScenarioCallbacks createScenarioCallbacks()
    {
        return new ApplicationFragmentTestHelper.ScenarioCallbacks()
        {
            ActivityScenario<MainActivity> mScenario = ActivityScenario.launch(MainActivity.class);
            
            @Override
            public ActivityScenario<MainActivity> getScenario()
            {
                return mScenario;
            }
            
            @Override
            public void recreateScenario()
            {
                mScenario = ActivityScenario.launch(MainActivity.class);
            }
        };
    }
    
    private void navigateToArchiveFrom(Destination location)
    {
        mCurrentLocation = Destination.ARCHIVE;
        
        switch (location) {
        case SLEEP_TRACKER:
            onView(withId(R.id.nav_session_archive)).perform(click());
            break;
        case ARCHIVE:
            break;
        }
    }
}
