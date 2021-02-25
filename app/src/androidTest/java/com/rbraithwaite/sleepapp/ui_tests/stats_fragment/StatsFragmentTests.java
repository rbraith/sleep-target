package com.rbraithwaite.sleepapp.ui_tests.stats_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragment;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class StatsFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void intervalTimePeriodSelector_updatesRangeProperly()
    {
        HiltFragmentTestHelper<StatsFragment> helper =
                HiltFragmentTestHelper.launchFragment(StatsFragment.class);
        
        Date testDate = TestUtils.ArbitraryData.getDate();
        final DateRange testDateRange = DateRange.asWeekOf(testDate);
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                fragment.setIntervalsDateRange(testDateRange);
            }
        });
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getIntervalsDateRange(), is(equalTo(testDateRange)));
            }
        });
        
        onView(withId(R.id.stats_time_period_back)).perform(click());
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getIntervalsDateRange(),
                           is(equalTo(testDateRange.offsetDays(-7))));
            }
        });
        
        onView(withId(R.id.stats_time_period_forward)).perform(click());
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getIntervalsDateRange(),
                           is(equalTo(testDateRange.offsetDays(7))));
            }
        });
    }
}
