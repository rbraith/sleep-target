package com.rbraithwaite.sleepapp.ui_tests.stats_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragment;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
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
    public void intervalsTimePeriodResolution_updatesProperlyFromMenu()
    {
        // GIVEN the user is on the stats screen
        // AND the resolution of the sleep intervals data viz. is set to "week"
        HiltFragmentTestHelper<StatsFragment> helper =
                HiltFragmentTestHelper.launchFragment(StatsFragment.class);
        
        final GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        final TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return date.getTime();
            }
        };
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                fragment.getViewModel().setTimeUtils(stubTimeUtils);
            }
        });
        
        // WHEN the user changes the resolution to "month"
        StatsFragmentTestUtils.changeIntervalsResolution(StatsFragmentViewModel.Resolution.MONTH);
        
        // THEN the time period text updates
        StatsFragmentTestUtils.checkIntervalsTextMatches(
                StatsFormatting.formatIntervalsMonthOf(date.getTime()));
        // AND the range is set to the month of the originally displayed week
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(
                        DateRange.asMonthOf(date.getTime(), stubTimeUtils.hoursToMillis(
                                StatsFragmentViewModel.DEFAULT_INTERVALS_OFFSET_HOURS)),
                        is(equalTo(fragment.getViewModel().getIntervalsDateRange())));
            }
        });
    }
    
    @Test
    public void intervalsTimePeriodText_updatesProperly()
    {
        HiltFragmentTestHelper<StatsFragment> helper =
                HiltFragmentTestHelper.launchFragment(StatsFragment.class);
        
        final TestUtils.DoubleRef<DateRange> testDateRange = new TestUtils.DoubleRef<>(null);
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                testDateRange.ref = fragment.getViewModel().getIntervalsDateRange();
            }
        });
        
        // add 1 day, for mon-sun instead of sun-sun
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(testDateRange.ref.getStart());
        cal.add(Calendar.DAY_OF_WEEK, 1);
        
        onView(withId(R.id.stats_time_period_value)).check(matches(withText(
                StatsFormatting.formatIntervalsRange(new DateRange(cal.getTime(),
                                                                   testDateRange.ref.getEnd())))));
    }
    
    @Test
    public void intervalTimePeriodSelector_updatesRangeProperly()
    {
        // setup
        HiltFragmentTestHelper<StatsFragment> helper =
                HiltFragmentTestHelper.launchFragment(StatsFragment.class);
        
        // TODO [21-03-9 3:07PM] -- Currently this test is using the actual now, because the
        //  intervals config is initialized before I have a chance to inject a stub TimeUtils.
        //  This isn't a huge problem - the test would only flake in the extremely rare case that
        //  the week were to change between the time it takes to init the config when the fragment
        //  launches and the time it takes to instantiate testDateRange below.
//        final GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
//        final TimeUtils stubTimeUtils = new TimeUtils()
//        {
//            @Override
//            public Date getNow()
//            {
//                return date.getTime();
//            }
//        };
//        helper.performSyncedFragmentAction(new HiltFragmentTestHelper
//        .SyncedFragmentAction<StatsFragment>()
//        {
//            @Override
//            public void perform(StatsFragment fragment)
//            {
//                fragment.getViewModel().setTimeUtils(stubTimeUtils);
//            }
//        });
        
        TimeUtils timeUtils = new TimeUtils();
        final DateRange testDateRange = DateRange.asWeekOf(
                timeUtils.getNow(),
                (int) timeUtils.hoursToMillis(
                        StatsFragmentViewModel.DEFAULT_INTERVALS_OFFSET_HOURS));
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getViewModel().getIntervalsDateRange(),
                           is(equalTo(testDateRange)));
            }
        });
        
        // SUT - exercising the time period selectors
        onView(withId(R.id.stats_time_period_back)).perform(click());
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getViewModel().getIntervalsDateRange(),
                           is(equalTo(testDateRange.offsetDays(-7))));
            }
        });
        
        onView(withId(R.id.stats_time_period_forward)).perform(click());
        
        helper.performSyncedFragmentAction(new HiltFragmentTestHelper.SyncedFragmentAction<StatsFragment>()
        {
            @Override
            public void perform(StatsFragment fragment)
            {
                assertThat(fragment.getViewModel().getIntervalsDateRange(),
                           is(equalTo(testDateRange.offsetDays(7))));
            }
        });
    }
}
