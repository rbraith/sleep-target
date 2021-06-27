package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragment;
import com.rbraithwaite.sleepapp.ui.stats.StatsFragmentViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsChartViewModel;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class StatsTestDriver
        extends BaseFragmentTestDriver<StatsFragment, StatsTestDriver.Assertions>
{
//*********************************************************
// private constants
//*********************************************************

    private static final int OFFSET_MILLIS = (int) TimeUtils.hoursToMillis(
            IntervalsChartViewModel.DEFAULT_INTERVALS_OFFSET_HOURS);
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<StatsTestDriver, StatsFragmentViewModel>
    {
        public Assertions(StatsTestDriver owningDriver)
        {
            super(owningDriver);
        }
        
        public void intervalsChartDisplaysMonthOf(Date date)
        {
            intervalsChartValueTextMatches(StatsFormatting.formatIntervalsMonthOf(date));
            intervalsRangeMatches(DateRange.asMonthOf(date, OFFSET_MILLIS));
        }
        
        public void intervalsChartDisplaysWeekOf(Date date)
        {
            DateRange expectedRange = DateRange.asWeekOf(date, OFFSET_MILLIS);
            intervalsRangeMatches(expectedRange);
            // add 1 day so it displays mon-sun, instead of sun-sun
            // see IntervalsChartViewModel.getIntervalsValueText
            expectedRange.offsetStartDays(1);
            intervalsChartValueTextMatches(StatsFormatting.formatIntervalsRange(expectedRange));
        }
        
        public void intervalsChartDisplaysYearOf(Date date)
        {
            intervalsChartValueTextMatches(StatsFormatting.formatIntervalsYearOf(date));
            intervalsRangeMatches(DateRange.asYearOf(date, OFFSET_MILLIS));
        }
        
        public void intervalsChartValueTextMatches(String expected)
        {
            onView(allOf(
                    withId(R.id.stats_range_selector_value),
                    withParent(withId(R.id.stats_intervals_time_period_selector)))).check(matches(
                    withText(expected)));
        }
        
        private void intervalsRangeMatches(DateRange expected)
        {
            getOwningDriver().getHelper()
                    .performSyncedFragmentAction(fragment -> hamcrestAssertThat(
                            expected,
                            is(equalTo(fragment.getIntervalsChartViewModel()
                                               .getIntervalsDateRange()))));
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    public StatsTestDriver(FragmentTestHelper<StatsFragment> helper)
    {
        init(helper, new Assertions(this));
    }
    
//*********************************************************
// api
//*********************************************************

    public void setNow(Date now)
    {
        TimeUtils stubTimeUtils = new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return now;
            }
        };
        
        IntervalsDataSet.Config config = new IntervalsDataSet.Config(
                DateRange.asWeekOf(now, OFFSET_MILLIS),
                OFFSET_MILLIS,
                IntervalsChartViewModel.DEFAULT_INTERVALS_INVERT,
                IntervalsDataSet.Resolution.WEEK);
        
        getHelper().performSyncedFragmentAction(fragment -> {
            fragment.getIntervalsChartViewModel().setTimeUtils(stubTimeUtils);
            fragment.getIntervalsChartViewModel().setIntervalsDataSetConfig(config);
        });
    }
    
    public void setIntervalsChartResolution(IntervalsDataSet.Resolution resolution)
    {
        openIntervalsMoreMenu();
        
        // REFACTOR [21-03-5 1:55AM] -- hardcoded strings.
        switch (resolution) {
        case WEEK:
            onView(withText("Week")).inRoot(isPlatformPopup()).perform(click());
            break;
        case MONTH:
            onView(withText("Month")).inRoot(isPlatformPopup()).perform(click());
            break;
        case YEAR:
            onView(withText("Year")).inRoot(isPlatformPopup()).perform(click());
            break;
        }
    }
    
    public void pressIntervalsRangeBack()
    {
        onView(allOf(isDescendantOfA(withId(R.id.stats_intervals)),
                     withId(R.id.stats_range_selector_back))).perform(click());
    }
    
    public void pressIntervalsRangeForward()
    {
        onView(allOf(isDescendantOfA(withId(R.id.stats_intervals)),
                     withId(R.id.stats_range_selector_forward))).perform(click());
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void openIntervalsMoreMenu()
    {
        onView(allOf(
                withId(R.id.stats_range_selector_more),
                withParent(withId(R.id.stats_intervals_time_period_selector)))).perform(click());
    }
}
