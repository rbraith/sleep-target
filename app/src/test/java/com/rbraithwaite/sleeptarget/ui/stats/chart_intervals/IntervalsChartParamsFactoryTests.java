/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet;
import com.rbraithwaite.sleeptarget.ui.stats.common.CombinedChartViewFactory;

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
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class IntervalsChartParamsFactoryTests
{
//*********************************************************
// package properties
//*********************************************************

    IntervalsChartParamsFactory paramsFactory;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        paramsFactory = new IntervalsChartParamsFactory(
                new TestUtils.SynchronizedExecutor(),
                TestUtils.getContext());
    }
    
    @After
    public void teardown()
    {
        paramsFactory = null;
    }
    
    // AChartEngine integration test
    @Test
    public void createRangeParams_returnsCorrectLabels()
    {
        // set up
        int offsetMillis = -1 * (1000 * 60 * 60) * 4; // -4 hrs
        GregorianCalendar date = new GregorianCalendar(2021, 2, 4);
        DateRange dateRange = DateRange.asWeekOf(date.getTime(), offsetMillis);
        IntervalsDataSet.Config config = new IntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true,
                IntervalsDataSet.Resolution.WEEK);
        
        IntervalsDataSet mockDataSet = setupDataSet(config);
        
        // SUT
        LiveData<CombinedChartViewFactory.Params> params =
                paramsFactory.createRangeParams(mockDataSet);
        
        TestUtils.activateLocalLiveData(params);
        shadowOf(Looper.getMainLooper()).idle();
        XYMultipleSeriesRenderer renderer = params.getValue().renderer;
        
        // verify
        // Y labels
        assertYLabelsAreCorrect(renderer);
        
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
        IntervalsDataSet.Config config = new IntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true,
                IntervalsDataSet.Resolution.MONTH);
        
        IntervalsDataSet mockDataSet = setupDataSet(config);
        
        // SUT
        LiveData<CombinedChartViewFactory.Params> params = paramsFactory.createMonthParams(
                mockDataSet, date.get(Calendar.MONTH));
        
        TestUtils.activateLocalLiveData(params);
        shadowOf(Looper.getMainLooper()).idle();
        XYMultipleSeriesRenderer renderer = params.getValue().renderer;
        
        // verify
        // Y labels
        assertYLabelsAreCorrect(renderer);
        
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
        IntervalsDataSet.Config config = new IntervalsDataSet.Config(
                dateRange,
                offsetMillis,
                true,
                IntervalsDataSet.Resolution.YEAR);
        
        IntervalsDataSet mockDataSet = setupDataSet(config);
        
        // SUT
        LiveData<CombinedChartViewFactory.Params> params = paramsFactory.createYearParams(
                mockDataSet, date.get(Calendar.YEAR));
        
        TestUtils.activateLocalLiveData(params);
        shadowOf(Looper.getMainLooper()).idle();
        XYMultipleSeriesRenderer renderer = params.getValue().renderer;
        
        // verify
        // Y labels
        assertYLabelsAreCorrect(renderer);
        
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

    private void assertYLabelsAreCorrect(XYMultipleSeriesRenderer renderer)
    {
        Object[][] expectedYLabels = {
                {"8pm", 0.0},
                {"10pm", -2.0},
                {"12am", -4.0},
                {"2am", -6.0},
                {"4am", -8.0},
                {"6am", -10.0},
                {"8am", -12.0},
                {"10am", -14.0},
                {"12pm", -16.0},
                {"2pm", -18.0},
                {"4pm", -20.0},
                {"6pm", -22.0},
                {"8pm", -24.0},
        };
        for (Object[] expectedYLabelData : expectedYLabels) {
            String yLabel = (String) expectedYLabelData[0];
            double yLabelLocation = (double) expectedYLabelData[1];
            assertThat(renderer.getYTextLabel(yLabelLocation), is(equalTo(yLabel)));
        }
    }
    
    private IntervalsDataSet setupDataSet(IntervalsDataSet.Config config)
    {
        IntervalsDataSet intervalsDataSet = new IntervalsDataSet();
        
        RangeCategorySeries series = new RangeCategorySeries("test");
        series.add(-1.2, -3.4);
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        dataSet.addSeries(series.toXYSeries());
        intervalsDataSet.config = config;
        intervalsDataSet.sleepSessionDataSet = dataSet;
        
        return intervalsDataSet;
    }
}
