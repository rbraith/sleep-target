package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.ApplicationTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SessionArchiveTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveFragmentTests
{
//*********************************************************
// private properties
//*********************************************************

    private DatabaseTestDriver database;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        database = new DatabaseTestDriver();
    }
    
    @After
    public void teardown()
    {
        database = null;
    }
    
    @Test
    public void listItemValuesDisplayProperly()
    {
        // REFACTOR [21-05-14 3:36PM] -- put sessionArchive & database in a setup method.
        //  update all tests to use these drivers.
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        database.addSleepSession(sleepSession);
        
        // REFACTOR [21-05-14 3:40PM] -- It would be better if the activity didn't launch
        //  immediately, so I could instantiate the driver in setup, but still pre-define data in
        //  the database in the test itself
        //  ---
        //  solution: inject a ProviderOf<FragmentTestHelper> instead, and have a
        //  driver.launch() method - this could be in a generic base driver.
        SessionArchiveTestDriver sessionArchive = new SessionArchiveTestDriver(
                HiltFragmentTestHelper.launchFragment(SessionArchiveFragment.class));
        
        sessionArchive.assertThat().listItemAtIndex(0).hasValuesMatching(sleepSession);
    }
    
    // BUG [21-06-30 6:40PM] -- flaky test?
    @Test
    public void sessionArchiveCrudTest()
    {
        ApplicationTestDriver app = startAppInArchive();
        
        app.getSessionArchive().assertThat().listIsEmpty();
        
        // create
        app.getSessionArchive().pressAddNewSessionButton();
        // SMELL [21-07-1 1:22AM] -- I need to update Arbitrary.getDate to be closer to the
        //  current date, its far enough away that its causing problems when it gets converted
        //  to an int (actually maybe I just shouldn't be using int ever instead of long :p)
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        sleepSession.setStart(new TimeUtils().getNow());
        app.getSessionDetails().setValuesTo(sleepSession);
        app.getSessionDetails().confirm();
        
        app.getSessionArchive().assertThat().listItemAtIndex(0).hasValuesMatching(sleepSession);
        
        // edit
        SleepSession editedSession = editSession(sleepSession);
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().assertThat().displayedValuesMatch(sleepSession);
        app.getSessionDetails().setValuesTo(editedSession);
        app.getSessionDetails().confirm();
        
        app.getSessionArchive().assertThat().listItemAtIndex(0).hasValuesMatching(editedSession);
        
        // delete
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().deleteSession();
        
        app.getSessionArchive().assertThat().listIsEmpty();
    }
    
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
        SleepSession originalSession = TestUtils.ArbitraryData.getSleepSession();
        database.addSleepSession(originalSession);
        
        ApplicationTestDriver app = startAppInArchive();
        
        // edit
        app.getSessionArchive().openSessionDetailsFor(0);
        Date newEnd =
                new TimeUtils().addDurationToDate(originalSession.getEnd(), 25 * 60 * 60 * 1000);
        app.getSessionDetails().setEnd(newEnd);
        app.getSessionDetails().confirm();
        
        // check add session
        app.getSessionArchive().pressAddNewSessionButton();
        app.getSessionDetails().assertThat().endDoesNotMatch(newEnd);
    }
    
    // regression test for #47
    @Test
    public void addSession_hasCorrectValuesAfterUpPress()
    {
        SleepSession originalSession = TestUtils.ArbitraryData.getSleepSession();
        database.addSleepSession(originalSession);
        
        ApplicationTestDriver app = startAppInArchive();
        
        app.getSessionArchive().openSessionDetailsFor(0);
        Date newEnd =
                new TimeUtils().addDurationToDate(originalSession.getEnd(), 25 * 60 * 60 * 1000);
        app.getSessionDetails().setEnd(newEnd);
        app.navigateUp();
        
        // check add session
        app.getSessionArchive().pressAddNewSessionButton();
        app.getSessionDetails().assertThat().endDoesNotMatch(newEnd);
    }
    
    // TODO [20-12-17 7:14PM] -- deleteSession_doesNothingOnDialogCancel.
    
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
        ApplicationTestDriver app = startAppInArchive();
        
        app.getSessionArchive().pressAddNewSessionButton();
        app.getSessionDetails().cancel();
        
        app.getSessionArchive().assertThat().listIsEmpty();
    }
    
    // TODO [20-12-16 9:01PM] -- addSession_hasCorrectValuesAfterBackPress
    //  the behaviour for this already exists, so it should just be green >_>

//*********************************************************
// private methods
//*********************************************************

    private SleepSession editSession(SleepSession originalSession)
    {
        TimeUtils timeUtils = new TimeUtils();
        
        // 25 hours so that both the day & time of day are affected
        int hours25 = 25 * 60 * 60 * 1000;
        
        return new SleepSession(
                0,
                timeUtils.addDurationToDate(originalSession.getStart(), -1 * hours25),
                // setting the end date to 25 hours past the original
                originalSession.getDurationMillis() + hours25 + hours25,
                null,
                null,
                null,
                originalSession.getRating() + 1.0f);
    }
    
    private ApplicationTestDriver startAppInArchive()
    {
        ApplicationTestDriver app = new ApplicationTestDriver();
        app.navigateTo(ApplicationTestDriver.Destination.ARCHIVE);
        return app;
    }
    
    // TODO [20-12-11 2:43PM] -- assert that 'add session' screen cancels properly from up
    //  button.
    
    // TODO [20-12-10 5:41PM] -- assert that when going into add session, it always starts with the
    //  current time (artificially set the clock?).
    
    // TODO [20-12-10 5:19PM] -- test for empty list message.
}
