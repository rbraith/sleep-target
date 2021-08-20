package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;



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
    private InterruptionDetailsTestDriver mInterruptionDetails;
    private PostSleepTestDriver mPostSleep;

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
        POST_SLEEP,
        SESSION_DETAILS,
        INTERRUPTION_DETAILS
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
        mSleepTracker = CommonUtils.lazyInit(mSleepTracker, () -> {
            SleepTrackerTestDriver driver = new SleepTrackerTestDriver(
                    new ApplicationFragmentTestHelper<>(mScenarioCallbacks));
            
            driver.setOnNavToPostSleepListener(() -> mCurrentLocation = Destination.POST_SLEEP);
            
            return driver;
        });
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
            
            sessionDetails.setOpenInterruptionDetailsListener(() -> {
                mCurrentLocation = Destination.INTERRUPTION_DETAILS;
            });
            
            return sessionDetails;
        });
        return mCurrentLocation == Destination.SESSION_DETAILS ? mSessionDetails : null;
    }
    
    public InterruptionDetailsTestDriver getInterruptionDetails()
    {
        mInterruptionDetails = CommonUtils.lazyInit(mInterruptionDetails, () -> {
            InterruptionDetailsTestDriver driver = InterruptionDetailsTestDriver.inApplication(
                    new ApplicationFragmentTestHelper<>(mScenarioCallbacks));
            driver.setOnNegativeActionListener(() -> {
                mCurrentLocation = Destination.SESSION_DETAILS;
            });
            driver.setOnPositiveActionListener(() -> {
                mCurrentLocation = Destination.SESSION_DETAILS;
            });
            return driver;
        });
        return mCurrentLocation == Destination.INTERRUPTION_DETAILS ? mInterruptionDetails : null;
    }
    
    public PostSleepTestDriver getPostSleep()
    {
        assertThat(mCurrentLocation, is(equalTo(Destination.POST_SLEEP)));
        
        mPostSleep = CommonUtils.lazyInit(mPostSleep, () -> {
            PostSleepTestDriver driver = PostSleepTestDriver.inApplication(
                    new ApplicationFragmentTestHelper<>(mScenarioCallbacks));
            
            driver.setNavCallbacks(new PostSleepTestDriver.NavCallbacks()
            {
                @Override
                public void onKeep()
                {
                    mCurrentLocation = Destination.SLEEP_TRACKER;
                }
                
                @Override
                public void onDiscard()
                {
                    mCurrentLocation = Destination.SLEEP_TRACKER;
                }
                
                @Override
                public void onUp()
                {
                    mCurrentLocation = Destination.SLEEP_TRACKER;
                }
            });
            
            return driver;
        });
        
        return mPostSleep;
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
    
    /**
     * Starting on the tracker screen, record all the details of this session, using the flow of
     * tracker -> post-sleep -> tracker
     */
    public void recordSpecificSession(SleepSessionBuilder sleepSession)
    {
        assertThat(mCurrentLocation, is(equalTo(Destination.SLEEP_TRACKER)));
        
        SleepSession value = sleepSession.build();
        
        getSleepTracker().recordSpecificSession(value);
        getPostSleep().setRating(value.getRating());
        getPostSleep().keep();
    }
    
    public void recordArbitrarySleepSession()
    {
        assertThat(mCurrentLocation, is(equalTo(Destination.SLEEP_TRACKER)));
        getSleepTracker().recordArbitrarySession();
        getPostSleep().keep();
    }
    
    public void stopAndKeepSessionManually()
    {
        assertThat(mCurrentLocation, is(equalTo(Destination.SLEEP_TRACKER)));
        
        getSleepTracker().stopSessionManually();
        getPostSleep().keep();
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
