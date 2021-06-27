package com.rbraithwaite.sleepapp.ui_tests.stats_fragment;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.data.database.DatabaseTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.StatsTestDriver;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.HiltFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragment;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsDataSet;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@RunWith(AndroidJUnit4.class)
public class StatsFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void intervalsTimePeriodResolutionUpdatesProperlyFromMenu()
    {
        // add some arbitrary data, so that the chart displays
        DatabaseTestDriver databaseDriver = new DatabaseTestDriver();
        databaseDriver.addSleepSession(TestUtils.ArbitraryData.getSleepSession());
        
        StatsTestDriver stats =
                new StatsTestDriver(HiltFragmentTestHelper.launchFragment(StatsFragment.class));
        
        Date fakeNow = new GregorianCalendar(2021, 2, 4).getTime();
        stats.setNow(fakeNow);
        stats.assertThat().intervalsChartDisplaysWeekOf(fakeNow);
        
        stats.setIntervalsChartResolution(IntervalsDataSet.Resolution.MONTH);
        stats.assertThat().intervalsChartDisplaysMonthOf(fakeNow);
        
        stats.setIntervalsChartResolution(IntervalsDataSet.Resolution.YEAR);
        stats.assertThat().intervalsChartDisplaysYearOf(fakeNow);
    }
    
    @Test
    public void intervalTimePeriodSelector_updatesRangeProperly()
    {
        // add some arbitrary data, so that the chart displays
        DatabaseTestDriver databaseDriver = new DatabaseTestDriver();
        databaseDriver.addSleepSession(TestUtils.ArbitraryData.getSleepSession());
        
        StatsTestDriver stats =
                new StatsTestDriver(HiltFragmentTestHelper.launchFragment(StatsFragment.class));
        
        GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        stats.setNow(date.getTime());
        
        stats.assertThat().intervalsChartDisplaysWeekOf(date.getTime());
        
        stats.pressIntervalsRangeBack();
        
        date.add(Calendar.DAY_OF_WEEK, -7);
        stats.assertThat().intervalsChartDisplaysWeekOf(date.getTime());
        
        stats.pressIntervalsRangeForward();
        
        date.add(Calendar.DAY_OF_WEEK, 7);
        stats.assertThat().intervalsChartDisplaysWeekOf(date.getTime());
    }
}
