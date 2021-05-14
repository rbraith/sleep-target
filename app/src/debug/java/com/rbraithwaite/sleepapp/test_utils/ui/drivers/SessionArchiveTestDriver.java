package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.test.espresso.contrib.RecyclerViewActions;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class SessionArchiveTestDriver
{
//*********************************************************
// private constants
//*********************************************************

    private final FragmentTestHelper<SessionArchiveFragment> mHelper;
    private final OnOpenSessionDetailsListener mOnOpenSessionDetailsListener;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnOpenSessionDetailsListener
    {
        void onOpenSessionDetails();
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveTestDriver(
            FragmentTestHelper<SessionArchiveFragment> helper,
            OnOpenSessionDetailsListener onOpenSessionDetailsListener)
    {
        mOnOpenSessionDetailsListener = onOpenSessionDetailsListener;
        mHelper = helper;
    }
    
//*********************************************************
// api
//*********************************************************

    public void openSessionDetailsFor(int listItemIndex)
    {
        if (mOnOpenSessionDetailsListener != null) {
            mOnOpenSessionDetailsListener.onOpenSessionDetails();
        }
        onView(withId(R.id.session_archive_list))
                .perform(RecyclerViewActions.actionOnItemAtPosition(listItemIndex, click()));
    }
}
