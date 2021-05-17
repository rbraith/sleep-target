package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepIntervalsRendererHelperTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepIntervalsRendererHelper rendererHelper;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        rendererHelper = new SleepIntervalsRendererHelper();
    }
    
    @After
    public void teardown()
    {
        rendererHelper = null;
    }
    
    // AChartEngine integration test
    @Test
    public void createRangeRenderer_returnsCorrectLabels()
    {
        // set up
        int offsetMillis = -1 * (1000 * 60 * 60) * 4;
        GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        DateRange dateRange = DateRange.asWeekOf(date.getTime(), offsetMillis);
        SleepIntervalsDataSet.Config config = new SleepIntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true);
        
        SleepIntervalsDataSet mockDataSet = setupMockDataSet(config);
        
        // SUT
        XYMultipleSeriesRenderer renderer = rendererHelper.createRangeRenderer(mockDataSet);
        
        // verify
        // Y labels
        Double[] yTextLabelLocations = renderer.getYTextLabelLocations();
        assertThat(yTextLabelLocations.length, is(equalTo(13)));
        for (double location : yTextLabelLocations) {
            // -1 because invert is true above ^^^
            assertThat(renderer.getYTextLabel(location),
                       is(equalTo(StatsFormatting.formatIntervalsYLabel(-1 * (int) location))));
        }
        // X labels
        Double[] xTextLabelLocations = renderer.getXTextLabelLocations();
        // REFACTOR [21-03-4 9:20PM] -- hardcoded formatting here.
        String[] expected = {
                "03/01", "03/02", "03/03", "03/04", "03/05", "03/06", "03/07"
        };
        assertThat(xTextLabelLocations.length, is(equalTo(expected.length)));
        for (double location : xTextLabelLocations) {
            assertThat(renderer.getXTextLabel(location), is(equalTo(expected[(int) location - 1])));
        }
    }
    
    @Test
    public void createMonthRenderer_returnsCorrectLabels()
    {
        // set up
        int offsetMillis = -1 * (1000 * 60 * 60) * 4;
        GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        DateRange dateRange = DateRange.asMonthOf(date.getTime(), offsetMillis);
        SleepIntervalsDataSet.Config config = new SleepIntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true);
        
        SleepIntervalsDataSet mockDataSet = setupMockDataSet(config);
        
        // SUT
        XYMultipleSeriesRenderer renderer = rendererHelper.createMonthRenderer(
                mockDataSet,
                date.get(Calendar.MONTH));
        
        // verify
        // Y labels
        Double[] yTextLabelLocations = renderer.getYTextLabelLocations();
        assertThat(yTextLabelLocations.length, is(equalTo(13)));
        for (double location : yTextLabelLocations) {
            // -1 because invert is true above ^^^
            assertThat(renderer.getYTextLabel(location),
                       is(equalTo(StatsFormatting.formatIntervalsYLabel(-1 * (int) location))));
        }
        // X labels
        Double[] xTextLabelLocations = renderer.getXTextLabelLocations();
        // REFACTOR [21-03-4 9:20PM] -- hardcoded formatting here.
        Map<Double, String> expected = new HashMap<>();
        expected.put(1.0, "03/01");
        expected.put(5.0, "03/05");
        expected.put(10.0, "03/10");
        expected.put(15.0, "03/15");
        expected.put(20.0, "03/20");
        expected.put(25.0, "03/25");
        expected.put(31.0, "03/31");
        assertThat(xTextLabelLocations.length, is(equalTo(expected.size())));
        for (double location : xTextLabelLocations) {
            assertThat(renderer.getXTextLabel(location), is(equalTo(expected.get(location))));
        }
    }
    
    @Test
    public void createYearRenderer_returnsCorrectLabels()
    {
        // set up
        int offsetMillis = -1 * (1000 * 60 * 60) * 4;
        GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        DateRange dateRange = DateRange.asYearOf(date.getTime(), offsetMillis);
        SleepIntervalsDataSet.Config config = new SleepIntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true);
        
        SleepIntervalsDataSet mockDataSet = setupMockDataSet(config);
        
        // SUT
        XYMultipleSeriesRenderer renderer = rendererHelper.createYearRenderer(
                mockDataSet,
                date.get(Calendar.YEAR));
        
        // verify
        // Y labels
        Double[] yTextLabelLocations = renderer.getYTextLabelLocations();
        assertThat(yTextLabelLocations.length, is(equalTo(13)));
        for (double location : yTextLabelLocations) {
            // -1 because invert is true above ^^^
            assertThat(renderer.getYTextLabel(location),
                       is(equalTo(StatsFormatting.formatIntervalsYLabel(-1 * (int) location))));
        }
        // X labels
        Double[] xTextLabelLocations = renderer.getXTextLabelLocations();
        // REFACTOR [21-03-4 9:20PM] -- hardcoded formatting here.
        Map<Double, String> expected = new HashMap<>();
        expected.put(1.0, "01");
        expected.put(32.0, "02");
        expected.put(60.0, "03");
        expected.put(91.0, "04");
        expected.put(121.0, "05");
        expected.put(152.0, "06");
        expected.put(182.0, "07");
        expected.put(213.0, "08");
        expected.put(244.0, "09");
        expected.put(274.0, "10");
        expected.put(305.0, "11");
        expected.put(335.0, "12");
        assertThat(xTextLabelLocations.length, is(equalTo(expected.size())));
        for (double location : xTextLabelLocations) {
            assertThat(renderer.getXTextLabel(location), is(equalTo(expected.get(location))));
        }
    }

//*********************************************************
// private methods
//*********************************************************

    private SleepIntervalsDataSet setupMockDataSet(SleepIntervalsDataSet.Config config)
    {
        SleepIntervalsDataSet mockDataSet = mock(SleepIntervalsDataSet.class);
        
        RangeCategorySeries series = new RangeCategorySeries("test");
        series.add(-1.2, -3.4);
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        dataSet.addSeries(series.toXYSeries());
        when(mockDataSet.getConfig()).thenReturn(config);
        when(mockDataSet.getDataSet()).thenReturn(dataSet);
        
        return mockDataSet;
    }
}
