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

package com.rbraithwaite.sleeptarget.ui.stats.chart_durations;

import android.content.Context;
import android.graphics.Paint;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.ui.stats.StatsFormatting;
import com.rbraithwaite.sleeptarget.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleeptarget.ui.utils.AppColors;
import com.rbraithwaite.sleeptarget.utils.AsyncUtils;

import org.achartengine.chart.BarChart;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class DurationsChartParamsFactory
{
//*********************************************************
// private properties
//*********************************************************

    private Executor mExecutor;
    private Context mContext;

//*********************************************************
// private constants
//*********************************************************

    private final AsyncUtils.AsyncFactory<CombinedChartViewFactory.Params> mParamsAsyncFactory;
    
    private final int LEFT_Y_AXIS = 0;
    private final int RIGHT_Y_AXIS = 1;
    
    private final AppColors mAppColors;
    
//*********************************************************
// constructors
//*********************************************************

    public DurationsChartParamsFactory(Executor executor, Context context)
    {
        mExecutor = executor;
        mContext = context;

        mParamsAsyncFactory = new AsyncUtils.AsyncFactory<>(executor);

        mAppColors = AppColors.from(mContext);
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<CombinedChartViewFactory.Params> createParams(
            List<DurationsChartViewModel.DataPoint> dataSet,
            int rangeDistance)
    {
        return mParamsAsyncFactory.createAsync(() -> _createParams(dataSet, rangeDistance));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private CombinedChartViewFactory.Params _createParams(
            List<DurationsChartViewModel.DataPoint> dataSet,
            int rangeDistance)
    {
        // REFACTOR [21-05-14 11:28PM] -- hardcoded strings.
        XYSeries durationSeries = new XYSeries("Sleep Duration", LEFT_Y_AXIS);
    
        List<XYSeries> ratingSeriesList = new ArrayList<>();
        // rating series helpers
        XYSeries[] currentRatingSeries = { null };
        boolean[] inRatingLine = { false };
    
        List<XYSeries> targetSeriesList = new ArrayList<>();
        // target series helpers
        XYSeries[] targetSeries = { null };
        Double[] prevTargetDurationHours = { null };

        // process the data set
        for (int i = 0; i < dataSet.size(); i++) {
            DurationsChartViewModel.DataPoint dataPoint = dataSet.get(i);
            // this makes the data appear from right to left
            float chartPosition = rangeDistance - i - 1;

            durationSeries.add(chartPosition, dataPoint.sleepDurationHours);
            
            processNextRating(
                    chartPosition,
                    dataPoint.sleepRating,
                    currentRatingSeries,
                    ratingSeriesList,
                    inRatingLine);
            
            processNextTargetDuration(
                    chartPosition,
                    dataPoint.targetDurationHours,
                    prevTargetDurationHours,
                    targetSeries,
                    targetSeriesList);

        }
        // add last target series
        int ref = 0;
        if (prevTargetDurationHours[ref] != null && targetSeries[ref] != null) {
            targetSeries[ref].add(0, prevTargetDurationHours[ref]);
            targetSeriesList.add(targetSeries[ref]);
        }
        

        CombinedChartViewFactory.Params params = new CombinedChartViewFactory.Params(
                createMultipleSeriesRenderer(calculateMaxY(durationSeries, targetSeriesList), rangeDistance),
                new XYMultipleSeriesDataset(),
                new ArrayList<>());

        // set up the X labels (one at each end of the chart)
        if (!dataSet.isEmpty()) {
            // REFACTOR [21-05-15 5:50PM] -- it would be better if this was in
            //  createMultipleSeriesRenderer() as well.
            // The first data point label is added to the right side of the chart, and the last
            // data point is added to the left side.
            params.renderer.addXTextLabel(rangeDistance - 1, dataSet.get(0).label);
            // the rangeDistance check helps prevent visual overlap of the labels
            // REFACTOR [21-08-10 12:43AM] -- I could call that doesLabelHaveEnoughRoom().
            if (dataSet.size() > 1 &&
                (rangeDistance / dataSet.size()) <= 10)
            {
                params.renderer.addXTextLabel(rangeDistance - dataSet.size(),
                                              dataSet.get(dataSet.size() - 1).label);
            }
        }

        // add durations to params
        params.dataSet.addSeries(durationSeries);
        params.renderer.addSeriesRenderer(createDurationsSeriesRenderer());
        params.types.add(new CombinedXYChart.XYCombinedChartDef(BarChart.TYPE, 0));
        
        // add duration target to params
        if (!targetSeriesList.isEmpty()) {
            addSeriesListToParams(
                    targetSeriesList,
                    params,
                    LineChart.TYPE,
                    createTargetSeriesRenderer());
        }
    
        // add ratings to params
        addSeriesListToParams(
                ratingSeriesList,
                params,
                LineChart.TYPE,
                createRatingsSeriesRenderer());
        
        return params;
    }
    
    /**
     * The max Y value might be a bar or it might be a target line.
     */
    private double calculateMaxY(XYSeries durationSeries, List<XYSeries> targetSeriesList)
    {
        double maxTargetSeriesY = 0;
        for (XYSeries series : targetSeriesList) {
            maxTargetSeriesY = Math.max(maxTargetSeriesY, series.getMaxY());
        }
        
        return Math.max(
                durationSeries.getMaxY(),
                maxTargetSeriesY);
    }
    
    // REFACTOR [21-09-29 9:19PM] -- this is similar to
    //  IntervalsChartParamsFactory.applyDataSetToParams()
    private void addSeriesListToParams(
            List<XYSeries> seriesList,
            CombinedChartViewFactory.Params params,
            String chartType,
            XYSeriesRenderer renderer)
    {
        int[] seriesIndices = new int[seriesList.size()];
        int indicesOffset = params.dataSet.getSeriesCount();
        for (int i = 0; i < seriesList.size(); i++) {
            params.dataSet.addSeries(seriesList.get(i));
            params.renderer.addSeriesRenderer(renderer);
            seriesIndices[i] = i + indicesOffset;
        }
        params.types.add(new CombinedXYChart.XYCombinedChartDef(chartType, seriesIndices));
    }
    
    // if the ratings data is 0, break the line
    private void processNextRating(
            float chartPosition,
            float sleepRating,
            XYSeries[] currentRatingSeries,
            List<XYSeries> ratingSeriesList,
            boolean[] inRatingLine)
    {
        int ref = 0;
        // transitioning into a rating line
        if (!inRatingLine[ref] && sleepRating > 0) {
            currentRatingSeries[ref] = new XYSeries("Sleep Rating", RIGHT_Y_AXIS);
            ratingSeriesList.add(currentRatingSeries[ref]);
            currentRatingSeries[ref].add(chartPosition, sleepRating);
            inRatingLine[ref] = true;
        } else if (inRatingLine[ref]) {
            // transitioning out of a rating line
            if (sleepRating == 0) {
                inRatingLine[ref] = false;
            }
            // in a rating line
            else {
                currentRatingSeries[ref].add(chartPosition, sleepRating);
            }
        }
    }
    
    // The target durations are processed such that data points with like durations are merged into
    // single horizontal lines. When there is a delta in the target duration value, a new line is
    // started, causing a stepwise pattern in the chart. Unset target durations are shown as
    // empty spaces.
    private void processNextTargetDuration(
            float chartPosition,
            Double targetDurationHours,
            Double[] prevTargetDurationHours,
            XYSeries[] targetSeries,
            List<XYSeries> targetSeriesList)
    {
        int ref = 0;
        float offsetChartPosition = chartPosition + 0.5f;
        
        if (targetSeries[ref] == null && targetDurationHours == null) {
            // skip any initial unset or unedited values (targetSeries is only null at the start)
            return;
        } else if (targetSeries[ref] == null) {
            // first actual target value
            targetSeries[ref] = new XYSeries("Target");
            targetSeries[ref].add(offsetChartPosition, targetDurationHours);
            prevTargetDurationHours[ref] = targetDurationHours;
            return;
        }
    
        if (targetDurationHours == null) {
            if (prevTargetDurationHours[ref] != null) {
                // transitioning from set target to unset target
                targetSeries[ref].add(offsetChartPosition, prevTargetDurationHours[ref]);
                targetSeriesList.add(targetSeries[ref]);
                prevTargetDurationHours[ref] = targetDurationHours;
            }
            return;
        }
    
        if (prevTargetDurationHours[ref] == null) {
            // transitioning from unset target to a set target
            targetSeries[ref] = new XYSeries("Target");
            targetSeries[ref].add(offsetChartPosition, targetDurationHours);
            prevTargetDurationHours[ref] = targetDurationHours;
            return;
        }
    
        if (!targetDurationHours.equals(prevTargetDurationHours[ref])) {
            // end of one line, start of another with a different y value
            targetSeries[ref].add(offsetChartPosition, prevTargetDurationHours[ref]);
            targetSeriesList.add(targetSeries[ref]);
            targetSeries[ref] = new XYSeries("Target");
            targetSeries[ref].add(offsetChartPosition, targetDurationHours);
            prevTargetDurationHours[ref] = targetDurationHours;
        }
    }
    
    // REFACTOR [21-05-15 12:47AM] -- a lot of this duplicates the intervals chart renderer
    //  setup in IntervalsChartParamsFactory - these should be consistent with each other.
    private XYMultipleSeriesRenderer createMultipleSeriesRenderer(
            double maxDataY,
            int rangeDistance)
    {
        final int TWO_Y_AXES = 2;
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer(TWO_Y_AXES);
        
        // Y axis
        // -----------------------------------------------------
        renderer.setYLabelsPadding(15);
        renderer.setYLabels(0);
        
        // left y axis (durations)
        renderer.setYAxisAlign(Paint.Align.LEFT, LEFT_Y_AXIS);
        renderer.setYLabelsAlign(Paint.Align.RIGHT, LEFT_Y_AXIS);
        renderer.setYAxisMin(0, LEFT_Y_AXIS);
        // the chart max Y is the nearest full hour greater than the data
        double maxY = Math.ceil(maxDataY);
        renderer.setYAxisMax(maxY, LEFT_Y_AXIS);
        setupDurationYLabels(renderer, (int) maxY);
        
        // right y axis (ratings)
        renderer.setYAxisAlign(Paint.Align.RIGHT, RIGHT_Y_AXIS);
        renderer.setYLabelsAlign(Paint.Align.LEFT, RIGHT_Y_AXIS);
        renderer.setYAxisMin(0.0, RIGHT_Y_AXIS);
        renderer.setYAxisMax(6.0, RIGHT_Y_AXIS);
        for (int i = 0; i < 5; i++) {
            // HACK [21-06-7 8:59PM] -- These spaces in 'text' are to compensate for the y labels
            //  padding
            //  set above. There's definitely a better way to do this.
            renderer.addYTextLabel(i + 1, "    " + (i + 1), RIGHT_Y_AXIS);
        }
        
        // X axis
        // -----------------------------------------------------
        renderer.setXLabels(0);
        renderer.setBarSpacing(0.1);
        // this is so that the bars at the far edges of the chart are not cut off
        renderer.setXAxisMin(-0.5f);
        renderer.setXAxisMax(rangeDistance - 0.5f);
        
        // misc
        // -----------------------------------------------------
        renderer.setPanEnabled(false);
        renderer.setShowLegend(false);
        renderer.setMargins(new int[] {
                35, calculateLeftMargin(maxY), 0, 45  // top, left, bottom, right
        });
        
        
        // REFACTOR [21-06-7 8:00PM] -- This styling duplicates the intervals chart styling.
        // styling
        // -----------------------------------------------------
        // colours
        renderer.setMarginsColor(mAppColors.colorPrimarySurface);
        renderer.setBackgroundColor(mAppColors.appColorBackground);
        renderer.setApplyBackgroundColor(true);
        renderer.setAxesColor(mAppColors.colorSecondary);
        renderer.setXLabelsColor(mAppColors.appColorOnPrimarySurface2);
        
        renderer.setYLabelsColor(RIGHT_Y_AXIS, mAppColors.appColorOnPrimarySurface2);
        renderer.setYLabelsColor(LEFT_Y_AXIS, mAppColors.appColorOnPrimarySurface2);
        
        // text sizes
        renderer.setLabelsTextSize(30);
        renderer.setAxisTitleTextSize(40);
        
        return renderer;
    }
    
    /**
     * This is to account for left y-labels having different possible character lengths. E.g. the
     * edge cases where a user has 3 or 4 digit hour sleep durations.
     */
    private int calculateLeftMargin(double maxY)
    {
        int numberOfCharacters = String.valueOf((int) maxY).length() + 1; // +1 for 'h'
        return 22 * numberOfCharacters;
    }
    
    private void setupDurationYLabels(XYMultipleSeriesRenderer renderer, int maxY)
    {
        // Note: maxY is in hours, representing the nearest full hour greater than the max of
        // the data.
        
        if (maxY <= 12) {
            // add every hour
            for (int i = 0; i < maxY; i++) {
                addDurationYLabel(renderer, i + 1);
            }
        } else {
            // for larger amounts, display the 1/4 marks only
    
            // round up not down, this avoids an issue where quarter * 4 ends up less than maxY,
            //  giving you 5 labels
            int quarter = (int) Math.ceil((float) maxY / 4.0f);
            
            for (int i = quarter; i < maxY; i += quarter) {
                addDurationYLabel(renderer, i);
            }
            addDurationYLabel(renderer, maxY);
        }
    }
    
    private void addDurationYLabel(XYMultipleSeriesRenderer renderer, int hour)
    {
        renderer.addYTextLabel(
                hour,
                StatsFormatting.formatDurationsYLabel(hour),
                LEFT_Y_AXIS);
    }
    
    private XYSeriesRenderer createDurationsSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(mAppColors.colorPrimaryDark);
        return renderer;
    }
    
    private XYSeriesRenderer createRatingsSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(mAppColors.colorSecondary);
        renderer.setDisplayBoundingPoints(true);
        renderer.setLineWidth(6);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(20);
        return renderer;
    }
    
    private XYSeriesRenderer createTargetSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        // REFACTOR [21-09-30 6:53PM] -- This hardcoded width is meant to be the same as the one
        //  for the intervals chart wake-time lines.
        renderer.setLineWidth(8f);
        renderer.setColor(mAppColors.appColorTriadic2);
        return renderer;
    }
}
