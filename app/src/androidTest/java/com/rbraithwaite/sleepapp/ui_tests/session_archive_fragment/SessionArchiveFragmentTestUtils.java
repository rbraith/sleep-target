package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import com.rbraithwaite.sleepapp.test_utils.ui.UITestNavigate;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;
import com.rbraithwaite.sleepapp.ui_tests.session_edit_fragment.SessionEditFragmentTestUtils;

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
        SessionEditFragmentTestUtils.setStartDateTime(calendar);
        calendar.setTimeInMillis(sessionData.endDateTime);
        SessionEditFragmentTestUtils.setEndDateTime(calendar);
        
        SessionEditFragmentTestUtils.pressConfirm();
    }
}
