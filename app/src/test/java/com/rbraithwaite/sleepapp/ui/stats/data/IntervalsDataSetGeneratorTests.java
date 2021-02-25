package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.junit.Test;

import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;

import static com.rbraithwaite.sleepapp.utils.TimeUtils.hoursToMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class IntervalsDataSetGeneratorTests
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
        // 1 hr over into 02/20
        GregorianCalendar start1 = new GregorianCalendar(2021, 1, 19, 23, 0);
        long duration1 = hoursToMillis(2);
        // 3 hrs in 02/21
        GregorianCalendar start2 = new GregorianCalendar(2021, 1, 21, 1, 0);
        long duration2 = hoursToMillis(3);
        // 1 hr in 02/21
        GregorianCalendar start3 = new GregorianCalendar(2021, 1, 21, 5, 0);
        long duration3 = hoursToMillis(1);
        
        SleepSessionModel sleepSession1 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession1.setStart(start1.getTime());
        sleepSession1.setDuration(duration1);
        
        SleepSessionModel sleepSession2 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession2.setStart(start2.getTime());
        sleepSession2.setDuration(duration2);
        
        SleepSessionModel sleepSession3 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession3.setStart(start3.getTime());
        sleepSession3.setDuration(duration3);
        
        IntervalsDataSetGenerator.Config config = new IntervalsDataSetGenerator.Config(
                new DateRange(
                        new GregorianCalendar(2021, 2, 20).getTime(),
                        new GregorianCalendar(2021, 2, 22).getTime()),
                false);
        
        List<SleepSessionModel> sleepSessions = Arrays.asList(
                sleepSession1,
                sleepSession2,
                sleepSession3);
        
        IntervalsDataSetGenerator generator = new IntervalsDataSetGenerator();
        // SUT
        XYMultipleSeriesDataset dataSet = generator.generateFromConfig(sleepSessions, config);
        
        // verifying expected point data
        //
        //           02/20                  02/21
        // series 1: [(1, 0), (1, <1hr>),   (2, <1hr>),  (2, <4hrs>)]
        // series 2: [(1,0),  (1,0),        (2, <5hrs>), (2, <6hr>)]
        double[][][] expected = {
                // series 1
                {
                        {1.0, 0},
                        {1.0, (double) hoursToMillis(1)},
                        
                        {2.0, (double) hoursToMillis(1)},
                        {2.0, (double) hoursToMillis(4)}
                },
                // series 2
                {
                        {1.0, 0},
                        {1.0, 0},
                        
                        {2.0, (double) hoursToMillis(5)},
                        {2.0, (double) hoursToMillis(6)}
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
