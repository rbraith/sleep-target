package com.rbraithwaite.sleepapp.ui_tests;

import androidx.lifecycle.ViewModelProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
    
    @Test
    public void addSession_addsSessionOnConfirm()
    {
        // GIVEN the user is on the 'add session' screen
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toAddSession();
        
        // WHEN the user confirms a session
        onView(withId(R.id.session_edit_menuitem_confirm)).perform(click());
        
        // SMELL [20-12-11 2:57PM] -- this doesn't seem like a great way to do this, maybe consider
        //  getters in the session edit fragment instead? or something idk - maybe getting the
        //  expected values from the displayed strings in the session edit screen? - i would need to
        //  parse these separate date & time-of-day strings back into Date objs, or else somehow
        //  combine the strings properly.
        //  Or maybe consider redesigning this test.
        // retrieve the start and end datetimes from the session edit screen
        final TestUtils.DoubleRef<Long> startDateTime = new TestUtils.DoubleRef<>(null);
        final TestUtils.DoubleRef<Long> endDateTime = new TestUtils.DoubleRef<>(null);
        TestUtils.performSyncedActivityAction(
                scenario,
                new TestUtils.SyncedActivityAction<MainActivity>()
                {
                    @Override
                    public void perform(MainActivity activity)
                    {
                        SessionEditFragmentViewModel viewModel =
                                new ViewModelProvider(activity).get(SessionEditFragmentViewModel.class);
                        startDateTime.ref = viewModel.getStartDateTime().getValue();
                        endDateTime.ref = viewModel.getEndDateTime().getValue();
                    }
                });
        
        // THEN a new session is added with the correct values in the archive
        // these checks work because there will be only one list item at this point
        DateTimeFormatter formatter = new DateTimeFormatter();
        String expectedStartDateTimeText =
                formatter.formatFullDate(DateUtils.getDateFromMillis(startDateTime.ref));
        String expectedEndDateTimeText =
                formatter.formatFullDate(DateUtils.getDateFromMillis(endDateTime.ref));
        onView(withId(R.id.session_archive_list_item_card)).check(matches(isDisplayed()));
        onView(withId(R.id.session_archive_list_item_start_VALUE)).check(matches(withText(
                expectedStartDateTimeText)));
        onView(withId(R.id.session_archive_list_item_stop_VALUE)).check(matches(withText(
                expectedEndDateTimeText)));
    }
    
    // TODO [20-12-11 2:43PM] -- assert that 'add session' screen cancels properly from up
    //  button.
    
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
