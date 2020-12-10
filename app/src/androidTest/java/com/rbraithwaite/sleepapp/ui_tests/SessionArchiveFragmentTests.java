package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void getRecyclerViewAdapterTest()
    {
        HiltFragmentTestHelper<SessionArchiveFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SessionArchiveFragment.class);
        testHelper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<SessionArchiveFragment>()
        {
            @Override
            public void perform(SessionArchiveFragment fragment)
            {
                assertThat(fragment.getRecyclerViewAdapter(), is(notNullValue()));
            }
        });
    }
    
    @Test
    public void addSession_isCancelledProperly()
    {
        // GIVEN the user goes to add a new session from the session archive
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        
        // REFACTOR [20-12-10 5:26PM] -- fromHome_toAddSession.
        UITestNavigate.fromHome_toSessionArchive();
        UITestNavigate.fromSessionArchive_toAddSession();
        
        // WHEN the operation is cancelled
        cancelAddSessionOperation();
        
        // THEN the user returns to the session archive
        onView(withId(R.id.session_archive_layout)).check(matches(isDisplayed()));
        // AND no new session is added
        assertRecyclerViewIsEmpty();
    }
    
    // TODO [20-12-10 5:41PM] -- assert that when going into add session, it always starts with the
    //  current time (artificially set the clock?).
    
    // TODO [20-12-10 5:19PM] -- test for empty list message.
    
//*********************************************************
// private methods
//*********************************************************

    private void cancelAddSessionOperation()
    {
        onView(withId(R.id.session_edit_menuitem_cancel)).perform(click());
    }
    
    private void assertRecyclerViewIsEmpty()
    {
        onView(withId(R.id.session_archive_list_item_card)).check(doesNotExist());
    }
}
