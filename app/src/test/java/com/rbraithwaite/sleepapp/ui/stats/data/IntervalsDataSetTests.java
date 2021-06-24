package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.IntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class IntervalsDataSetTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void isEmpty_returnsTrueIfDataSetIsEmpty()
    {
        IntervalsDataSet dataSet = new IntervalsDataSet.Generator().generateFromConfig(
                new ArrayList<>(), // no sleep sessions means empty data set
                createArbitraryConfig());
        
        assertThat(dataSet.isEmpty(), is(true));
    }
    
    // clarifying AChartEngine behaviour for myself
    @Test
    public void RangeCategorySeries_toXYSeries_createsPairedXPoints()
    {
        RangeCategorySeries testSeries = new RangeCategorySeries("test");
        // data point #1
        testSeries.add(1, 4.5); // this becomes [0](1, 1.0), [1](1, 4.5)
        // data point #2
        testSeries.add(3, 5.4); // this becomes [2](2, 3.0), [3](2, 5.4)

        XYSeries testXYSeries = testSeries.toXYSeries();

        assertThat(testXYSeries.getItemCount(), is(equalTo(4)));

        // range data point #1
        assertThat((int) testXYSeries.getX(0), is(equalTo(1)));
        assertThat(testXYSeries.getY(0), is(equalTo(1.0)));
        assertThat((int) testXYSeries.getX(1), is(equalTo(1)));
        assertThat(testXYSeries.getY(1), is(equalTo(4.5)));

        // range data point #2
        assertThat((int) testXYSeries.getX(2), is(equalTo(2)));
        assertThat(testXYSeries.getY(2), is(equalTo(3.0)));
        assertThat((int) testXYSeries.getX(3), is(equalTo(2)));
        assertThat(testXYSeries.getY(3), is(equalTo(5.4)));
    }

    @Test
    public void generateFromConfig_generatesCorrectData()
    {
        // setup test data
        TimeUtils timeUtils = new TimeUtils();

        DateRange dateRange = new DateRange(
                new GregorianCalendar(2021, 2, 20).getTime(),
                new GregorianCalendar(2021, 2, 22).getTime());

        // 1 hr over into 02/20
        GregorianCalendar start1 = new GregorianCalendar(2021, 2, 19, 23, 0);
        SleepSession sleepSession1 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession1.setStart(start1.getTime());
        sleepSession1.setDurationMillis(timeUtils.hoursToMillis(2));

        // 3 hrs in 02/21
        GregorianCalendar start2 = new GregorianCalendar(2021, 2, 21, 1, 0);
        SleepSession sleepSession2 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession2.setStart(start2.getTime());
        sleepSession2.setDurationMillis(timeUtils.hoursToMillis(3));

        // 1 hr in 02/21, later in the day
        GregorianCalendar start3 = new GregorianCalendar(2021, 2, 21, 5, 0);
        SleepSession sleepSession3 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession3.setStart(start3.getTime());
        sleepSession3.setDurationMillis(timeUtils.hoursToMillis(1));

        IntervalsDataSet.Config config = new IntervalsDataSet.Config(
                dateRange,
                1000, // arbitrary offset, just to test this behaviour
                false,
                IntervalsDataSet.Resolution.WEEK);

        List<SleepSession> sleepSessions = Arrays.asList(
                sleepSession1,
                sleepSession2,
                sleepSession3);

        // SUT
        IntervalsDataSet.Generator generator = new IntervalsDataSet.Generator();
        IntervalsDataSet sleepIntervals = generator.generateFromConfig(sleepSessions, config);

        // verifying expected point data
        assertThat(sleepIntervals.isEmpty(), is(false));

        XYMultipleSeriesDataset dataSet = sleepIntervals.dataSet;

        //           02/20                  02/21
        // series 1: [(1, 0), (1, <1hr>),   (2, <1hr>),  (2, <4hrs>)]
        // series 2: [(1,0),  (1,0),        (2, <5hrs>), (2, <6hr>)]
        double[][][] expected = {
                // series 1
                {
                        {1.0, 0},
                        {1.0, 1.0},

                        {2.0, 1.0},
                        {2.0, 4.0}
                },
                // series 2
                {
                        {1.0, 0},
                        {1.0, 0},

                        {2.0, 5.0},
                        {2.0, 6.0}
                }
        };

        assertThat(dataSet.getSeriesCount(), is(equalTo(expected.length)));

        double errorThreshold = 0.001;
        for (int seriesIdx = 0; seriesIdx < dataSet.getSeriesCount(); seriesIdx++) {
            XYSeries series = dataSet.getSeriesAt(seriesIdx);
            for (int pointIdx = 0; pointIdx < series.getItemCount(); pointIdx++) {
                assertThat(series.getX(pointIdx),
                           is(closeTo(expected[seriesIdx][pointIdx][0], errorThreshold)));
                assertThat(series.getY(pointIdx),
                           is(closeTo(expected[seriesIdx][pointIdx][1], errorThreshold)));
            }
        }
    }
    
//*********************************************************
// private methods
//*********************************************************

    private IntervalsDataSet.Config createArbitraryConfig()
    {
        return new IntervalsDataSet.Config(
                new DateRange(
                        new GregorianCalendar(2021, 2, 20).getTime(),
                        new GregorianCalendar(2021, 2, 22).getTime()),
                1000,
                false,
                IntervalsDataSet.Resolution.WEEK);
    }
}
