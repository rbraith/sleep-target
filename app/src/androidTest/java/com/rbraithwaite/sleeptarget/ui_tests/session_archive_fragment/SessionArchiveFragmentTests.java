/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui_tests.session_archive_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.ApplicationTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.SessionArchiveTestDriver;
import com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleeptarget.ui.session_archive.SessionArchiveFragment;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;
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
        sleepSession.setInterruptions(new Interruptions(aListOf(
                anInterruption(),
                anInterruption())));
        
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
        sleepSession.setStart(twoDaysAgo());
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
    
    @Test
    public void overlappingSessionDisplaysErrorDialog()
    {
        TimeUtils timeUtils = new TimeUtils();
        
        SleepSession overlappedSession = TestUtils.ArbitraryData.getSleepSession();
        overlappedSession.setDurationMillis(TimeUtils.hoursToMillis(4));
        SleepSession sleepSession = new SleepSession(
                timeUtils.addDurationToDate(
                        overlappedSession.getEnd(),
                        (int) TimeUtils.hoursToMillis(2)),
                0);
        
        database.addSleepSession(overlappedSession);
        database.addSleepSession(sleepSession);
        
        ApplicationTestDriver app = startAppInArchive();
        
        // open sleepSession's details (its the latest, so its the top of the list)
        app.getSessionArchive().openSessionDetailsFor(0);
        // this causes an overlap of 2hrs (sleepSession was 2hrs ahead, & overlappedSession
        // is 4hrs duration
        app.getSessionDetails().setStart(timeUtils.addDurationToDate(
                sleepSession.getStart(),
                -1 * (int) TimeUtils.hoursToMillis(4)));
        
        // HACK [21-07-3 2:03AM] -- this method was to get around the app driver listener
        //  changing the destination and nullifying the session details driver, probably not the
        //  best way to handle this lol.
        app.getSessionDetails().confirmSilently();
        app.getSessionDetails().assertThat().overlapErrorDialogIsDisplayed();
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
    
    @Test
    public void interruptionsCrudTest()
    {
        DateBuilder date = aDate();
        
        // init archive with a sleep session
        SleepSession sleepSession = aSleepSession()
                .withStart(aDate())
                .withDurationHours(12)
                .withNoInterruptions()
                .build();
        database.addSleepSession(sleepSession);
        database.assertThat.interruptionCountIs(0);
        
        ApplicationTestDriver app = startAppInArchive();
        
        // add
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().pressAddNewInterruptionButton();
        InterruptionBuilder interruption = anInterruption()
                .withStart(date.addMinutes(15))
                .withDuration(1, 23, 0)
                .withReason("interruptionsCrudTest");
        app.getInterruptionDetails().setValuesTo(interruption);
        app.getInterruptionDetails().confirm();
        app.getSessionDetails().assertThat().interruptionDetailsMatch(new Interruptions(aListOf(interruption)));
        app.getSessionDetails().confirm();
        database.assertThat.interruptionCountIs(1);
        
        // update
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().openInterruptionDetailsFor(0);
        InterruptionBuilder expectedUpdate = anInterruption()
                .withStart(date.addMinutes(123))
                .withDuration(5, 43, 0)
                .withReason("interruptionsCrudTest updated");
        app.getInterruptionDetails().setValuesTo(expectedUpdate);
        app.getInterruptionDetails().assertThat().valuesMatch(expectedUpdate);
        app.getInterruptionDetails().confirm();
        app.getSessionDetails().assertThat().interruptionAtPosition(0).matches(expectedUpdate);
        app.getSessionDetails().confirm();
        
        // delete
        app.getSessionArchive().openSessionDetailsFor(0);
        app.getSessionDetails().openInterruptionDetailsFor(0);
        app.getInterruptionDetails().deleteInterruption();
        app.getSessionDetails().assertThat().noInterruptionsAreDisplayed();
        app.getSessionDetails().confirm();
        database.assertThat.interruptionCountIs(0);
    }
    
    // TODO [20-12-16 9:01PM] -- addSession_hasCorrectValuesAfterBackPress
    //  the behaviour for this already exists, so it should just be green >_>

//*********************************************************
// private methods
//*********************************************************

    private Date twoDaysAgo()
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR, -48);
        return cal.getTime();
    }
    
    private SleepSession editSession(SleepSession originalSession)
    {
        TimeUtils timeUtils = new TimeUtils();
        
        // 25 hours so that both the day & time of day are affected
        int hours25 = 25 * 60 * 60 * 1000;
        
        return new SleepSession(
                0,
                timeUtils.addDurationToDate(originalSession.getStart(), -2 * hours25),
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
