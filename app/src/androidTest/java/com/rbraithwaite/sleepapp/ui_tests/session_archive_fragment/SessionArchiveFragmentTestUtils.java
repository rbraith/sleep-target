package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SessionArchiveFragmentTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private SessionArchiveFragmentTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void addSession(SleepSession sleepSession)
    {
        UITestNavigate.fromSessionArchive_toAddSession();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(sleepSession.getStart());
        SessionDataFragmentTestUtils.setStartDateTime(calendar);
        
        calendar.setTime(sleepSession.getEnd());
        SessionDataFragmentTestUtils.setEndDateTime(calendar);
        SessionDataFragmentTestUtils.pressPositive();
    }
}
