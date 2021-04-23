package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.MoodSelectorTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.TagSelectorTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils;

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
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveFragmentTests
{
//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-04-22 12:47AM] -- Not sure where to put this so I'm putting it here: the
    //  layouts for sleep_tracker_more_context.xml and session_data_fragment.xml are very similar.
    //  Consider extracting this group of [mood, tags, comments, etc] as a separate layout.
    @Test
    public void tagList_displaysProperly()
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        
        // add a new session with no tags to the archive
        SessionArchiveFragmentTestUtils.addSession(new SleepSession(
                TestUtils.ArbitraryData.getDate(),
                TestUtils.ArbitraryData.getDurationMillis()));
        
        // verify that the tag list for the list item is not displayed
        onView(withId(R.id.session_archive_list_item_tags)).check(matches(not(isDisplayed())));
        
        // add some tags to that session
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        // REFACTOR [21-04-22 1:39AM] -- I need a better way of setting up data for UI tests,
        //  because
        //  this takes forever.
        for (int i = 0; i < 2; i++) {
            TagSelectorTestUtils.addTag("test " + i);
            TagSelectorTestUtils.toggleTagSelection(i);
        }
        SessionDataFragmentTestUtils.pressPositive();
        
        // verify that the tag list for the list item is now displayed
        onView(withId(R.id.session_archive_list_item_tags)).check(matches(isDisplayed()));
    }
    
    @Test
    public void moodIcon_displaysProperly()
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        
        // add a new session with no mood to the archive
        SessionArchiveFragmentTestUtils.addSession(new SleepSession(
                TestUtils.ArbitraryData.getDate(),
                TestUtils.ArbitraryData.getDurationMillis(),
                "arbitrary comment",
                null));
        
        // verify that the list item mood is not displayed
        onView(withId(R.id.session_archive_list_item_mood_frame)).check(matches(not(isDisplayed())));
        
        // edit that session and add a mood
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        MoodSelectorTestUtils.addMood(withId(R.id.session_data_mood), 1);
        SessionDataFragmentTestUtils.pressPositive();
        
        // verify that the list item mood is displayed
        onView(withId(R.id.session_archive_list_item_mood_frame)).check(matches(isDisplayed()));
    }
    
    @Test
    public void additionalCommentsIcon_displaysProperly()
    {
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        
        // add a new session with no comments to the archive
        SessionArchiveFragmentTestUtils.addSession(new SleepSession(
                TestUtils.ArbitraryData.getDate(),
                TestUtils.ArbitraryData.getDurationMillis()));
        
        // verify that the list item icon is not displayed
        onView(withId(R.id.session_archive_list_item_comment_icon)).check(matches(not(isDisplayed())));
        
        // edit that session and add a comment
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        UITestUtils.typeOnMultilineEditText("test!", onView(withId(R.id.session_data_comments)));
        SessionDataFragmentTestUtils.pressPositive();
        
        // verify that the list item icon is displayed
        onView(withId(R.id.session_archive_list_item_comment_icon)).check(matches(isDisplayed()));
    }
    
    @Test
    public void deleteSession_deletesSessionOnDialogConfirm()
    {
        // REFACTOR [20-12-17 7:18PM] -- this test could potentially be isolated to the
        //  SessionArchiveFragment (eg try using HiltFragmentTestHelper instead).
        // GIVEN the user goes to delete a session from the session archive
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        // REFACTOR [20-12-17 7:24PM] -- this is just serving to initialize the archive, it
        //  would be better to have specific dev/debug tools for this purpose instead.
        SessionArchiveFragmentTestUtils.addSession(TestUtils.ArbitraryData.getSleepSession());
        
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        SessionDataFragmentTestUtils.pressNegative();
        
        // WHEN the user confirms the dialog positively
        DialogTestUtils.pressOK();
        
        // THEN the session is deleted from the archive
        assertRecyclerViewIsEmpty();
    }
    
    // TODO [20-12-17 7:14PM] -- deleteSession_doesNothingOnDialogCancel.
    
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
        // REFACTOR [20-12-17 7:19PM] -- use SessionArchiveFragmentTestUtils.addSession instead
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        onView(withId(R.id.sleep_tracker_button)).perform(click());
        
        UITestNavigate.fromHome_toSessionArchive();
        
        // AND the user has edited that session.
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        
        // new date is guaranteed to be different from default add session date (which should be
        // current time)
        GregorianCalendar newStartDateTime = new GregorianCalendar(2015, 4, 3, 2, 1);
        SessionDataFragmentTestUtils.setStartDateTime(newStartDateTime);
        SessionDataFragmentTestUtils.pressPositive();
        
        // WHEN the user goes to add a new session
        UITestNavigate.fromSessionArchive_toAddSession();
        
        // THEN the add session screen displays the correct default values
        // screen should not be displaying previous changed values
        SessionDataFragmentTestUtils.checkStartDateTimeDoesNotMatch(newStartDateTime);
    }
    
    // regression test for #47
    @Test
    public void addSession_hasCorrectValuesAfterUpPress()
    {
        // GIVEN the user is in the edit screen
        ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class);
        UITestNavigate.fromHome_toSessionArchive();
        // add a session so that it can be edited
        SessionArchiveFragmentTestUtils.addSession(TestUtils.ArbitraryData.getSleepSession());
        
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        
        // WHEN the user changes some values then presses the up button
        // new date is guaranteed to be different from default add session date (which should be
        // current time)
        GregorianCalendar newStartDateTime = new GregorianCalendar(2015, 4, 3, 2, 1);
        SessionDataFragmentTestUtils.setStartDateTime(newStartDateTime);
        UITestNavigate.up();
        
        // THEN the add session screen displays the correct default values
        UITestNavigate.fromSessionArchive_toAddSession();
        // screen should not be displaying previous changed values
        SessionDataFragmentTestUtils.checkStartDateTimeDoesNotMatch(newStartDateTime);
    }
    
    // TODO [20-12-16 9:01PM] -- addSession_hasCorrectValuesAfterBackPress
    //  the behaviour for this already exists, so it should just be green >_>
    
    @Test
    public void getRecyclerViewAdapterTest()
    {
        HiltFragmentTestHelper<SessionArchiveFragment> testHelper =
                HiltFragmentTestHelper.launchFragment(SessionArchiveFragment.class);
        testHelper.performSyncedFragmentAction(fragment -> assertThat(fragment.getRecyclerViewAdapter(),
                                                                      is(notNullValue())));
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
        SessionDataFragmentTestUtils.pressNegative();
        
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
        GregorianCalendar expectedStart = TestUtils.ArbitraryData.getCalendar();
        GregorianCalendar expectedEnd = new GregorianCalendar();
        expectedEnd.setTime(expectedStart.getTime());
        expectedEnd.add(Calendar.DAY_OF_WEEK, 1);
        expectedEnd.add(Calendar.HOUR, 1);
        
        SessionDataFragmentTestUtils.setStartDateTime(expectedStart);
        SessionDataFragmentTestUtils.setEndDateTime(expectedEnd);
        
        SessionDataFragmentTestUtils.pressPositive();
        
        // THEN a new session is added with the correct values in the archive
        // these checks work because there will be only one list item at this point
        // REFACTOR [21-03-26 5:24PM] -- This should be SessionArchiveFormatting.
        DateTimeFormatter formatter = new DateTimeFormatter();
        String expectedStartDateTimeText = formatter.formatFullDate(expectedStart.getTime());
        String expectedEndDateTimeText = formatter.formatFullDate(expectedEnd.getTime());
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
        SessionArchiveFragmentTestUtils.addSession(TestUtils.ArbitraryData.getSleepSession());
        
        // record current values
        final TestUtils.DoubleRef<String> originalStartDateText = new TestUtils.DoubleRef<>(null);
        final TestUtils.DoubleRef<String> originalEndDateText = new TestUtils.DoubleRef<>(null);
        TestUtils.performSyncedActivityAction(
                scenario, activity -> {
                    TextView startDate =
                            activity.findViewById(R.id.session_archive_list_item_start_VALUE);
                    TextView endDate =
                            activity.findViewById(R.id.session_archive_list_item_stop_VALUE);
                    originalStartDateText.ref = startDate.getText().toString();
                    originalEndDateText.ref = endDate.getText().toString();
                }
        );
        
        // WHEN the user edits that session and confirms
        // REFACTOR [20-12-16 5:24PM] -- maybe SessionArchiveFragmentTestUtils.editSession(position)
        onView(withId(R.id.session_archive_list_item_card)).perform(click());
        GregorianCalendar calendar = TestUtils.ArbitraryData.getCalendar();
        calendar.add(Calendar.DAY_OF_MONTH, -5);
        SessionDataFragmentTestUtils.setStartDateTime(calendar);
        SessionDataFragmentTestUtils.pressPositive();
        
        // THEN that session's values are properly updated in the archive
        onView(withId(R.id.session_archive_list_item_start_VALUE))
                .check(matches(withText(
                        new DateTimeFormatter().formatFullDate(calendar.getTime()))));
        onView(withId(R.id.session_archive_list_item_stop_VALUE))
                .check(matches(withText(originalEndDateText.ref)));
    }

//*********************************************************
// private methods
//*********************************************************

    private void assertRecyclerViewIsEmpty()
    {
        onView(withId(R.id.session_archive_list_item_card)).check(doesNotExist());
    }
}
