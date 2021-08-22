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

package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.data_set.IntervalsDataSet;
import com.rbraithwaite.sleepapp.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleepapp.ui.utils.AppColors;
import com.rbraithwaite.sleepapp.utils.AsyncUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.Factory;

import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.RangeStackedBarChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;

// REFACTOR [21-06-5 5:46PM] -- Replace SleepIntervalsRendererHelper with this - once that's done
//  move this out of TEMP.
public class IntervalsChartParamsFactory
{
//*********************************************************
// private properties
//*********************************************************

    private AsyncUtils.AsyncFactory<CombinedChartViewFactory.Params> mParamsAsyncFactory;
    private TimeUtils mTimeUtils;
    private AppColors mAppColors;

//*********************************************************
// constructors
//*********************************************************

    public IntervalsChartParamsFactory(Executor executor, Context context)
    {
        // REFACTOR [21-06-7 4:02PM] -- instead of injecting the executor, I should directly inject
        //  the async factory? (The executor isn't used for anything except to init the factory)
        //  (same with app colors?).
        mParamsAsyncFactory = new AsyncUtils.AsyncFactory<>(executor);
        mAppColors = AppColors.from(context);
        
        mTimeUtils = createTimeUtils();
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Create chart params for a generic ranged data set (commonly one week). The params are created
     * asynchronously.
     *
     * @param dataSet The data to create the chart params from.
     *
     * @return a LiveData of the chart params which will be updated asynchronously.
     */
    public LiveData<CombinedChartViewFactory.Params> createRangeParams(IntervalsDataSet dataSet)
    {
        return mParamsAsyncFactory.createAsync(() -> {
            CombinedChartViewFactory.Params params = createChartParamsFrom(dataSet);
            
            // x-axis
            // REFACTOR [21-06-7 3:26PM] -- I could extract this x-axis setup to a method.
            DateRange range = dataSet.config.dateRange;
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(range.getStart());
            // OPTIMIZE [21-06-7 3:48PM] -- this hoursOffset computation is redundant with
            //  initChartParams.
            int hoursOffset = computeHoursOffset(dataSet.config.offsetMillis);
            if (hoursOffset > 12) {
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            for (int i = 0; i < range.getDifferenceInDays(); i++) {
                // labels need to start at 1 since that's what the series starts at apparently :/
                params.renderer.addXTextLabel(i + 1,
                                              StatsFormatting.formatIntervalsXLabelDate(cal.getTime()));
                cal.add(Calendar.DAY_OF_WEEK, 1);
            }
            
            return params;
        });
    }
    
    public LiveData<CombinedChartViewFactory.Params> createMonthParams(
            IntervalsDataSet dataSet,
            int month)
    {
        return mParamsAsyncFactory.createAsync(() -> {
            CombinedChartViewFactory.Params params = createChartParamsFrom(dataSet);
            
            // x axis
            DateRange range = dataSet.config.dateRange;
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(range.getStart());
            
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            
            while (cal.get(Calendar.MONTH) == month) {
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                params.renderer.addXTextLabel(
                        dayOfMonth,
                        StatsFormatting.formatIntervalsXLabelDate(cal.getTime()));
                if (dayOfMonth == 1) {
                    cal.add(Calendar.DAY_OF_MONTH, 4);
                } else if (dayOfMonth == 25) {
                    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                } else {
                    cal.add(Calendar.DAY_OF_MONTH, 5);
                }
            }
            
            return params;
        });
    }
    
    public LiveData<CombinedChartViewFactory.Params> createYearParams(
            IntervalsDataSet dataSet,
            int year)
    {
        return mParamsAsyncFactory.createAsync(() -> {
            CombinedChartViewFactory.Params params = createChartParamsFrom(dataSet);
            
            // X axis
            // -----------------------------------------------------
            DateRange range = dataSet.config.dateRange;
            
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(range.getStart());
            
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.DAY_OF_YEAR, 1);
            
            // add an X label for each month
            while (cal.get(Calendar.YEAR) == year) {
                params.renderer.addXTextLabel(
                        cal.get(Calendar.DAY_OF_YEAR),
                        StatsFormatting.formatIntervalsXLabelMonth(cal.getTime()));
                cal.add(Calendar.MONTH, 1);
            }
            
            return params;
        });
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }


//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Init everything but the x-axis properties for the renderer, so that the x-axis can be
     * customized as needed.
     */
    private CombinedChartViewFactory.Params createChartParamsFrom(IntervalsDataSet dataSet)
    {
        CombinedChartViewFactory.Params params = new CombinedChartViewFactory.Params();
        
        int hoursOffset = computeHoursOffset(dataSet.config.offsetMillis);
        initRendererProperties(
                params.renderer,
                dataSet.config.invert,
                hoursOffset,
                dataSet.config.dateRange.getDifferenceInDays());
        styleRenderer(params.renderer);
        
        applyIntervalsToParams(dataSet, params);
        
        addMidnightLine(params, dataSet.config.invert, hoursOffset);
        
        return params;
    }
    
    /**
     * Set general properties & properties relating to the Y-axis for this renderer.
     *
     * @param renderer    The renderer to initialize.
     * @param invert      Whether the related intervals chart is inverted or not. For example this
     *                    affects how the Y labels are displayed.
     * @param hoursOffset The vertical hours offset of the y-axis
     * @param maxDataX    The maximum x value of the related data
     */
    private void initRendererProperties(
            XYMultipleSeriesRenderer renderer,
            boolean invert,
            int hoursOffset,
            int maxDataX)
    {
        // general
        // -----------------------------------------------------
        renderer.setShowCustomTextGridY(true);
        renderer.setPanEnabled(false);
        renderer.setShowLegend(false);
        
        renderer.setYLabelsPadding(50);
        renderer.setMargins(new int[] {35, 100, 0, 15}); // top, left, bottom, right
        
        // X axis
        // -----------------------------------------------------
        // offset 0.5 so that the bars at the far edges of the chart are not cut off
        renderer.setXAxisMin(0.5f);
        renderer.setXAxisMax(maxDataX + 0.5f);
        
        // disable the default labels
        renderer.setXLabels(0);
        
        // Y axis
        // -----------------------------------------------------
        int sign = invert ? -1 : 1;
        
        // add a label for the hours along the Y-axis, on every even hour
        for (int i = 0; i <= 24; i++) {
            if (i % 2 == 0) {
                renderer.addYTextLabel(
                        sign * i,
                        StatsFormatting.formatIntervalsYLabel(i + hoursOffset));
            }
        }
        // disable the default labels
        renderer.setYLabels(0);
        // setup the min/max Y display range
        double yMin;
        double yMax;
        if (invert) {
            yMin = sign * 24;
            yMax = 0;
        } else {
            yMin = 0;
            yMax = sign * 24;
        }
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
    }
    
    // REFACTOR [21-06-5 4:42PM] How can I share these styles with the durations chart renderer?
    private void styleRenderer(XYMultipleSeriesRenderer renderer)
    {
        // colours
        renderer.setMarginsColor(mAppColors.colorPrimarySurface);
        renderer.setBackgroundColor(mAppColors.appColorBackground);
        renderer.setApplyBackgroundColor(true);
        renderer.setAxesColor(mAppColors.colorSecondary);
        renderer.setGridColor(mAppColors.appColorOnPrimarySurface2);
        renderer.setXLabelsColor(mAppColors.appColorOnPrimarySurface2);
        renderer.setYLabelsColor(0, mAppColors.appColorOnPrimarySurface2);
        
        // text sizes
        // REFACTOR [21-06-5 5:25PM] -- hardcoded text size
        renderer.setLabelsTextSize(30);
    }
    
    private void applyIntervalsToParams(
            IntervalsDataSet dataSet,
            CombinedChartViewFactory.Params params)
    {
        int sleepSessionSeriesCount = dataSet.sleepSessionDataSet.getSeriesCount();
        if (sleepSessionSeriesCount > 0) {
            applyDataSetToParams(
                    dataSet.sleepSessionDataSet,
                    params,
                    RangeStackedBarChart.TYPE,
                    this::createSleepSessionBarRenderer,
                    0);
            
            // add interruptions second, so that they visually overlap the sleep sessions
            if (dataSet.hasInterruptionData()) {
                applyDataSetToParams(
                        dataSet.interruptionsDataSet,
                        params,
                        RangeStackedBarChart.TYPE,
                        this::createInterruptionsBarRenderer,
                        sleepSessionSeriesCount);
            }
        }
    }
    
    private void applyDataSetToParams(
            XYMultipleSeriesDataset dataSet,
            CombinedChartViewFactory.Params params,
            String chartType,
            Factory<XYSeriesRenderer> rendererFactory,
            int seriesStartingIndexOffset)
    {
        params.dataSet.addAllSeries(Arrays.asList(dataSet.getSeries()));
        int[] seriesIndices = new int[dataSet.getSeriesCount()];
        for (int i = seriesStartingIndexOffset;
             i < dataSet.getSeriesCount() + seriesStartingIndexOffset; i++) {
            seriesIndices[i - seriesStartingIndexOffset] = i;
            params.renderer.addSeriesRenderer(rendererFactory.create());
        }
        params.types.add(new CombinedXYChart.XYCombinedChartDef(
                chartType,
                seriesIndices));
    }
    
    private XYSeriesRenderer createSleepSessionBarRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(mAppColors.appColorTriadic3);
        return renderer;
    }
    
    private XYSeriesRenderer createInterruptionsBarRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(mAppColors.appColorInterruptionDark);
        return renderer;
    }
    
    private void addMidnightLine(
            CombinedChartViewFactory.Params params,
            boolean invert,
            int hoursOffset)
    {
        if (hoursOffset != 0) {
            int sign = invert ? -1 : 1;
            int midnight = sign * (24 - hoursOffset);
            XYSeries midnightLineSeries = new XYSeries("Midnight Line");
            midnightLineSeries.add(
                    params.renderer.getXAxisMin(),
                    midnight);
            midnightLineSeries.add(
                    params.renderer.getXAxisMax(),
                    midnight);
            
            XYSeriesRenderer midnightRenderer = new XYSeriesRenderer();
            midnightRenderer.setColor(mAppColors.colorSecondary);
            midnightRenderer.setLineWidth(5f);
            
            params.dataSet.addSeries(midnightLineSeries);
            params.renderer.addSeriesRenderer(midnightRenderer);
            params.types.add(new CombinedXYChart.XYCombinedChartDef(
                    LineChart.TYPE,
                    params.renderer.getSeriesRendererCount() - 1));
        }
    }
    
    // IDEA [21-06-5 7:40PM] -- I may want this to return a float value in the future, if I ever
    //  implement chart panning.
    // Note that this floors the millis to the nearest hour.
    private int computeHoursOffset(long offsetMillis)
    {
        int hoursOffset = (int) mTimeUtils.millisToHours(offsetMillis);
        
        if (hoursOffset < 0) {
            hoursOffset += 24;
        }
        
        return hoursOffset;
    }
}
