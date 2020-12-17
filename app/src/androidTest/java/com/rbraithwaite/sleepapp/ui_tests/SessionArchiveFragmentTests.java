package com.rbraithwaite.sleepapp.ui_tests;

import android.widget.TextView;

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
import com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment.SessionArchiveFragmentTestUtils;
import com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;

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

    // REFACTOR [20-12-16 1:50AM] -- I shouldn't need to do this test through
    //  SessionArchiveFragment.
    //  This problem is related to any return to the SessionEditFragment after changing it (eg going
    //  to an edit session screen for a different session, or to the add session screen).
    //  This test should be isolated to SessionEditFragment - basically I need a tool for testing
    //  entering and exiting a fragment - maybe in HiltFragmentTestHelper.
    // regression test for #46
    @Test
    public void addSession_hasCorrectValuesAfterEditingSession()
    {
        // GIVEN the user is in the archive with an existing session
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        // add a session
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        UITestNavigate.fromHome_toSessionArchive();
        
        // AND the user has edited that session
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        onView(withText("Edit")).perform(click());
        
        // new date is guaranteed to be different from default add session date (which should be
        // current time)
        GregorianCalendar newStartDateTime = new GregorianCalendar(2015, 4, 3, 2, 1);
        SessionEditFragmentTestUtils.setStartDateTime(newStartDateTime);
        SessionEditFragmentTestUtils.pressConfirm();
        
        // WHEN the user goes to add a new session
        UITestNavigate.fromSessionArchive_toAddSession();
        
        // THEN the add session screen displays the correct default values
        // screen should not be displaying previous changed values
        SessionEditFragmentTestUtils.checkStartDateTimeDoesNotMatch(newStartDateTime);
    }
    
    // regression test for #47
    @Test
    public void addSession_hasCorrectValuesAfterUpPress()
    {
        // GIVEN the user is in the edit screen
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        // add a session so that it can be edited
        SessionArchiveFragmentTestUtils.addSession(TestUtils.ArbitraryData.getSessionEditData());
        
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        onView(withText("Edit")).perform(click());
        
        // WHEN the user changes some values then presses the up button
        // new date is guaranteed to be different from default add session date (which should be
        // current time)
        GregorianCalendar newStartDateTime = new GregorianCalendar(2015, 4, 3, 2, 1);
        SessionEditFragmentTestUtils.setStartDateTime(newStartDateTime);
        UITestNavigate.up();
        
        // THEN the add session screen displays the correct default values
        UITestNavigate.fromSessionArchive_toAddSession();
        // screen should not be displaying previous changed values
        SessionEditFragmentTestUtils.checkStartDateTimeDoesNotMatch(newStartDateTime);
    }
    
    // TODO [20-12-16 9:01PM] -- addSession_hasCorrectValuesAfterBackPress
    //  the behaviour for this already exists, so it should just be green >_>
    
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
        
        SessionEditFragmentTestUtils.pressConfirm();
        
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
    
    @Test
    public void editSession_editsSessionOnConfirm()
    {
        // GIVEN the user is on the session archive with an existing session
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        SessionArchiveFragmentTestUtils.addSession(TestUtils.ArbitraryData.getSessionEditData());
        
        // record current values
        final TestUtils.DoubleRef<String> originalStartDateText = new TestUtils.DoubleRef<>(null);
        final TestUtils.DoubleRef<String> originalEndDateText = new TestUtils.DoubleRef<>(null);
        TestUtils.performSyncedActivityAction(
                scenario, new TestUtils.SyncedActivityAction<MainActivity>()
                {
                    @Override
                    public void perform(MainActivity activity)
                    {
                        TextView startDate =
                                activity.findViewById(R.id.session_archive_list_item_start_VALUE);
                        TextView endDate =
                                activity.findViewById(R.id.session_archive_list_item_stop_VALUE);
                        originalStartDateText.ref = startDate.getText().toString();
                        originalEndDateText.ref = endDate.getText().toString();
                    }
                }
        );
        
        // WHEN the user edits that session and confirms
        // REFACTOR [20-12-16 5:24PM] -- maybe SessionArchiveFragmentTestUtils.editSession(position)
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        onView(withText("Edit")).perform(click());
        
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        SessionEditFragmentTestUtils.setStartDateTime(calendar);
        SessionEditFragmentTestUtils.pressConfirm();
        
        // THEN that session's values are properly updated in the archive
        onView(withId(R.id.session_archive_list_item_start_VALUE))
                .check(matches(withText(new DateTimeFormatter().formatFullDate(calendar.getTime()))));
        onView(withId(R.id.session_archive_list_item_stop_VALUE)).check(matches(withText(
                originalEndDateText.ref)));
    }

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
