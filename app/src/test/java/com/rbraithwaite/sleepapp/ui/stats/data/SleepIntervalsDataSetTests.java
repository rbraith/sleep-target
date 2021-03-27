package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.junit.Test;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SleepIntervalsDataSetTests
{
//*********************************************************
// api
//*********************************************************

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
        // 1 hr over into 02/20
        GregorianCalendar start1 = new GregorianCalendar(2021, 1, 19, 23, 0);
        long duration1 = timeUtils.hoursToMillis(2);
        // 3 hrs in 02/21
        GregorianCalendar start2 = new GregorianCalendar(2021, 1, 21, 1, 0);
        long duration2 = timeUtils.hoursToMillis(3);
        // 1 hr in 02/21
        GregorianCalendar start3 = new GregorianCalendar(2021, 1, 21, 5, 0);
        long duration3 = timeUtils.hoursToMillis(1);
        
        SleepSession sleepSession1 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession1.setStart(start1.getTime());
        sleepSession1.setDurationMillis(duration1);
        
        SleepSession sleepSession2 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession2.setStart(start2.getTime());
        sleepSession2.setDurationMillis(duration2);
        
        SleepSession sleepSession3 = TestUtils.ArbitraryData.getSleepSession();
        sleepSession3.setStart(start3.getTime());
        sleepSession3.setDurationMillis(duration3);
        
        SleepIntervalsDataSet.Config config = new SleepIntervalsDataSet.Config(
                new DateRange(
                        new GregorianCalendar(2021, 2, 20).getTime(),
                        new GregorianCalendar(2021, 2, 22).getTime()),
                1000,
                false);
        
        List<SleepSession> sleepSessions = Arrays.asList(
                sleepSession1,
                sleepSession2,
                sleepSession3);
        
        SleepIntervalsDataSet.Generator generator = new SleepIntervalsDataSet.Generator();
        
        // SUT
        SleepIntervalsDataSet sleepIntervals = generator.generateFromConfig(sleepSessions, config);
        XYMultipleSeriesDataset dataSet = sleepIntervals.getDataSet();
        
        // verifying expected point data
        //
        //           02/20                  02/21
        // series 1: [(1, 0), (1, <1hr>),   (2, <1hr>),  (2, <4hrs>)]
        // series 2: [(1,0),  (1,0),        (2, <5hrs>), (2, <6hr>)]
        double[][][] expected = {
                // series 1
                {
                        {1.0, 0},
                        {1.0, (double) timeUtils.hoursToMillis(1)},
                        
                        {2.0, (double) timeUtils.hoursToMillis(1)},
                        {2.0, (double) timeUtils.hoursToMillis(4)}
                },
                // series 2
                {
                        {1.0, 0},
                        {1.0, 0},
                        
                        {2.0, (double) timeUtils.hoursToMillis(5)},
                        {2.0, (double) timeUtils.hoursToMillis(6)}
                }
        };
        
        for (int seriesIdx = 0; seriesIdx < dataSet.getSeriesCount(); seriesIdx++) {
            XYSeries series = dataSet.getSeriesAt(seriesIdx);
            for (int pointIdx = 0; pointIdx < series.getItemCount(); pointIdx++) {
                assertThat(series.getX(pointIdx), is(equalTo(expected[seriesIdx][pointIdx][0])));
                assertThat(series.getY(pointIdx), is(equalTo(expected[seriesIdx][pointIdx][1])));
            }
        }
    }
}
