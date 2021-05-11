package com.rbraithwaite.sleepapp.test_utils.ui.dialog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class DialogTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DialogTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void pressPositiveButton()
    {
        // button1 is dialog positive btn
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
    }
    
    public static void pressNegativeButton()
    {
        onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click());
    }
}
