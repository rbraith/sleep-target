package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui.session_data.SessionEditData;
import com.rbraithwaite.sleepapp.ui_tests.session_data_fragment.SessionDataFragmentTestUtils;

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

    public static void addSession(SessionEditData sessionData)
    {
        UITestNavigate.fromSessionArchive_toAddSession();
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(sessionData.startDateTime);
        SessionDataFragmentTestUtils.setStartDateTime(calendar);
        calendar.setTimeInMillis(sessionData.endDateTime);
        SessionDataFragmentTestUtils.setEndDateTime(calendar);
        
        SessionDataFragmentTestUtils.pressPositive();
    }
}
