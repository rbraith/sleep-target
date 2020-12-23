package com.rbraithwaite.sleepapp.ui_tests.session_archive_fragment;

import android.content.DialogInterface;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.DialogTestHelper;
import com.rbraithwaite.sleepapp.test_utils.ui.UITestUtils;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveDeleteDialog;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveDeleteDialogTests
{
//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);
    
//*********************************************************
// api
//*********************************************************

    // TODO [20-12-21 10:09PM] -- is there an async problem with this test?
    @Test
    public void onPositiveButtonClick_receivesCorrectValuesFrom_createInstance()
    {
        final int expectedSessionPosition = 5;
        
        // using a double ref here so that I can "forward declare" the dialog and
        // reference it in the listener below. I need the dialog reference to assert
        // in the listener, but I need the listener instance to create the dialog instance
        // with createInstance()
        final TestUtils.DoubleRef<SessionArchiveDeleteDialog> testDialog =
                new TestUtils.DoubleRef<>(null);
        
        final TestUtils.ThreadBlocker blocker = new TestUtils.ThreadBlocker();
        SessionArchiveDeleteDialog.OnPositiveButtonClickListener listener =
                new SessionArchiveDeleteDialog.OnPositiveButtonClickListener()
                {
                    @Override
                    public void onPositiveButtonClick(
                            DialogInterface dialog,
                            int sessionPosition)
                    {
                        assertThat(sessionPosition, is(expectedSessionPosition));
                        blocker.unblockThread();
                    }
                };
        
        testDialog.ref = SessionArchiveDeleteDialog.createInstance(
                SessionArchiveDeleteDialog.createArguments(expectedSessionPosition),
                listener);
        
        DialogTestHelper<SessionArchiveDeleteDialog> helper =
                DialogTestHelper.launchProvidedInstance(testDialog.ref);
        
        UITestUtils.pressDialogOK();
        blocker.blockThread();
    }
}
