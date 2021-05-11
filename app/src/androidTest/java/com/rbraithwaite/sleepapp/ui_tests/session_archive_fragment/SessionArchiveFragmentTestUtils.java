package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui_tests.session_details_fragment.SessionDetailsFragmentTestUtils;

import java.util.GregorianCalendar;

public class SessionArchiveFragmentTestUtils
{
    private SessionArchiveFragmentTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void addSession(SleepSession sleepSession)
    {
        UITestNavigate.fromSessionArchive_toAddSession();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(sleepSession.getStart());
        SessionDetailsFragmentTestUtils.setStartDateTime(calendar);
        
        calendar.setTime(sleepSession.getEnd());
        SessionDetailsFragmentTestUtils.setEndDateTime(calendar);
        SessionDetailsFragmentTestUtils.pressPositive();
        
        // TODO [21-04-21 8:16PM] -- this needs to add the other data as well:
        //      - mood
        //      - comments
        //      - tags.
    }
}
