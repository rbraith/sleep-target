package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

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
}
