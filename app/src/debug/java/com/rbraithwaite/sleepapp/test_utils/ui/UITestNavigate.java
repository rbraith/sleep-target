package com.rbraithwaite.sleepapp.test_utils.ui;

import com.rbraithwaite.sleepapp.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;



/**
 * Use this class for when the *means* of the navigation is not important.
 */
public class UITestNavigate
{
//*********************************************************
// constructors
//*********************************************************

    private UITestNavigate() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void fromHome_toGoals()
    {
        onView(withId(R.id.nav_sleepgoals)).perform(click());
    }
    
    public static void fromHome_toAddSession()
    {
        fromHome_toSessionArchive();
        fromSessionArchive_toAddSession();
    }
    
    public static void fromHome_toSessionArchive()
    {
        onView(withId(R.id.nav_session_archive)).perform(click());
    }
    
    public static void fromSessionArchive_toAddSession()
    {
        onView(withId(R.id.session_archive_fab)).perform(click());
    }
    
    public static void up()
    {
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click());
    }
}
