package com.rbraithwaite.sleepapp.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.HiltFragmentScenarioWorkaround;
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
        ActivityScenario<HiltFragmentScenarioWorkaround> scenario =
                HiltFragmentScenarioWorkaround.launchFragmentInHiltContainer(SessionArchiveFragment.class);
        
        scenario.onActivity(new ActivityScenario.ActivityAction<HiltFragmentScenarioWorkaround>()
        {
            @Override
            public void perform(HiltFragmentScenarioWorkaround activity)
            {
                // REFACTOR [20-11-14 4:27PM] -- use HiltFragmentScenarioWorkaround to abstract the
                //  fragment finding boilerplate?
                //  eg HiltFragmentScenarioWorkaround.onFragment(ActivityScenario scenario,
                //  HiltFragmentCallback callback)
                //  Actually make HiltFragmentScenarioWorkaround instantiable and have it hide
                //  the activity scenario.
                SessionArchiveFragment fragment = (SessionArchiveFragment) activity.getFragment();
                assertThat(fragment.getRecyclerViewAdapter(), is(notNullValue()));
            }
        });
    }
}
