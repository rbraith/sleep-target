package com.rbraithwaite.sleepapp.ui.stats.charts;

import android.graphics.Color;

import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.data.SleepIntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.inject.Inject;

public class SleepIntervalsRendererHelper
{
//*********************************************************
// package properties
//*********************************************************

    TimeUtils mTimeUtils;
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepIntervalsRendererHelper()
    {
        mTimeUtils = createTimeUtils();
    }
    
//*********************************************************
// api
//*********************************************************

    public XYMultipleSeriesRenderer createRangeRenderer(SleepIntervalsDataSet dataSet)
    {
        int hoursOffset = computeHoursOffset(dataSet.getConfig().offsetMillis);
        XYMultipleSeriesRenderer renderer = createBaseRenderer(dataSet, hoursOffset);
        
        // X axis
        // -----------------------------------------------------
        DateRange range = dataSet.getConfig().dateRange;
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(range.getStart());
        if (hoursOffset > 12) {
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        for (int i = 0; i < range.getDifferenceInDays(); i++) {
            // labels need to start at 1 since that's what the series starts at apparently :/
            renderer.addXTextLabel(i + 1, StatsFormatting.formatIntervalsXLabelDate(cal.getTime()));
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        
        renderer.setXLabels(0);
        
        renderer.setXAxisMin(0.5f);
        renderer.setXAxisMax(range.getDifferenceInDays() + 0.5f);
        
        return renderer;
    }
    
    public XYMultipleSeriesRenderer createMonthRenderer(SleepIntervalsDataSet dataSet, int month)
    {
        int hoursOffset = computeHoursOffset(dataSet.getConfig().offsetMillis);
        XYMultipleSeriesRenderer renderer = createBaseRenderer(dataSet, hoursOffset);
        
        // X axis
        // -----------------------------------------------------
        DateRange range = dataSet.getConfig().dateRange;
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(range.getStart());
        
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        while (cal.get(Calendar.MONTH) == month) {
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            renderer.addXTextLabel(
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
        
        renderer.setXLabels(0);
        
        renderer.setXAxisMin(0.5f);
        renderer.setXAxisMax(range.getDifferenceInDays() + 0.5f);
        
        return renderer;
    }
    
    public XYMultipleSeriesRenderer createYearRenderer(SleepIntervalsDataSet dataSet, int year)
    {
        int hoursOffset = computeHoursOffset(dataSet.getConfig().offsetMillis);
        XYMultipleSeriesRenderer renderer = createBaseRenderer(dataSet, hoursOffset);
        
        // X axis
        // -----------------------------------------------------
        DateRange range = dataSet.getConfig().dateRange;
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(range.getStart());
        
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_YEAR, 1);
        
        while (cal.get(Calendar.YEAR) == year) {
            renderer.addXTextLabel(
                    cal.get(Calendar.DAY_OF_YEAR),
                    StatsFormatting.formatIntervalsXLabelMonth(cal.getTime()));
            cal.add(Calendar.MONTH, 1);
        }
        
        renderer.setXLabels(0);
        
        renderer.setXAxisMin(0.5f);
        renderer.setXAxisMax(range.getDifferenceInDays() + 0.5f);
        
        return renderer;
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
     * Sets up basically everything except the X-axis labels.
     */
    private XYMultipleSeriesRenderer createBaseRenderer(
            SleepIntervalsDataSet dataSet,
            int hoursOffset)
    {
        SleepIntervalsDataSet.Config config = dataSet.getConfig();
        
        XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        
        for (XYSeries series : dataSet.getDataSet().getSeries()) {
            multipleSeriesRenderer.addSeriesRenderer(new XYSeriesRenderer());
        }
        
        // Y axis
        // -----------------------------------------------------
        int sign = config.invert ? -1 : 1;
        
        for (int i = 1; i < 25; i++) {
            if (i % 2 == 0) {
                multipleSeriesRenderer.addYTextLabel(
                        sign * (i + hoursOffset),
                        StatsFormatting.formatIntervalsYLabel(i + hoursOffset));
            }
        }
        multipleSeriesRenderer.addYTextLabel(
                sign * hoursOffset, StatsFormatting.formatIntervalsYLabel(hoursOffset));
        multipleSeriesRenderer.setYLabels(0);
        double yMin;
        double yMax;
        if (config.invert) {
            yMin = sign * (24 + hoursOffset);
            yMax = sign * hoursOffset;
        } else {
            yMin = sign * hoursOffset;
            yMax = sign * (24 + hoursOffset);
        }
        multipleSeriesRenderer.setYAxisMin(yMin);
        multipleSeriesRenderer.setYAxisMax(yMax);
        
        
        // HACK [21-02-22 11:23PM] -- i need to find a better way than this lol.
        //  likely solution: derive a renderer and override something.
        // midnight line
        // -----------------------------------------------------
        if (hoursOffset != 0) {
            // fake a grid line with a series lol
            RangeCategorySeries midnightLine = new RangeCategorySeries("midnight");
            int midnight = 24;
            double width = 0.2f;
            for (int i = 0; i < config.dateRange.getDifferenceInDays(); i++) {
                midnightLine.add(sign * midnight, sign * (midnight + width));
            }
            dataSet.getDataSet().addSeries(midnightLine.toXYSeries());
            XYSeriesRenderer midnightRenderer = new XYSeriesRenderer();
            midnightRenderer.setColor(Color.RED);
            multipleSeriesRenderer.addSeriesRenderer(midnightRenderer);
        }
        
        // misc
        // -----------------------------------------------------
        multipleSeriesRenderer.setLabelsTextSize(30);
        multipleSeriesRenderer.setAxisTitleTextSize(50);
        multipleSeriesRenderer.setBackgroundColor(Color.GREEN);
        multipleSeriesRenderer.setShowCustomTextGridY(true);
        multipleSeriesRenderer.setGridColor(Color.RED);
        
        multipleSeriesRenderer.setPanEnabled(false);
        
        multipleSeriesRenderer.setYLabelsPadding(50);
        multipleSeriesRenderer.setMargins(new int[] {35, 100, 0, 15}); // top, left, bottom, right
        multipleSeriesRenderer.setShowLegend(false);
        
        return multipleSeriesRenderer;
    }
    
    private int computeHoursOffset(long offsetMillis)
    {
        int hoursOffset = (int) mTimeUtils.millisToHours(offsetMillis);
        
        if (hoursOffset < 0) {
            hoursOffset += 24;
        }
        
        return hoursOffset;
    }
}
