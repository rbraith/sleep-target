package com.rbraithwaite.sleepapp.ui.stats.chart_durations;

import android.content.Context;
import android.graphics.Paint;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.ui.stats.StatsFormatting;
import com.rbraithwaite.sleepapp.ui.utils.AppColors;
import com.rbraithwaite.sleepapp.ui.stats.common.CombinedChartViewFactory;
import com.rbraithwaite.sleepapp.utils.AsyncUtils;

import org.achartengine.chart.BarChart;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class DurationsChartParamsFactory
{
    private Executor mExecutor;
    private Context mContext;
    private final AsyncUtils.AsyncFactory<CombinedChartViewFactory.Params> mParamsAsyncFactory;
    
    private final int LEFT_Y_AXIS = 0;
    private final int RIGHT_Y_AXIS = 1;
    
    private final AppColors mAppColors;
    
    public DurationsChartParamsFactory(Executor executor, Context context)
    {
        mExecutor = executor;
        mContext = context;
    
        mParamsAsyncFactory = new AsyncUtils.AsyncFactory<>(executor);
    
        mAppColors = AppColors.from(mContext);
    }
    
    public LiveData<CombinedChartViewFactory.Params> createParams(List<DurationsChartViewModel.DataPoint> dataSet, int rangeDistance)
    {
        return mParamsAsyncFactory.createAsync(() -> _createParams(dataSet, rangeDistance));
    }
    
    private CombinedChartViewFactory.Params _createParams(List<DurationsChartViewModel.DataPoint> dataSet, int rangeDistance)
    {
        // REFACTOR [21-05-14 11:28PM] -- hardcoded strings.
        XYSeries durationSeries = new XYSeries("Sleep Duration", LEFT_Y_AXIS);
    
        // add the durations data
        for (int i = 0; i < dataSet.size(); i++) {
            DurationsChartViewModel.DataPoint dataPoint = dataSet.get(i);
        
            // this makes the data appear from right to left
            float chartPosition = rangeDistance - i - 1;
        
            durationSeries.add(chartPosition, dataPoint.sleepDurationHours);
        }
    
        // add the ratings data
        // (if the ratings data is 0, break the line)
        // OPTIMIZE [21-06-8 3:10PM] -- I could combine this loop with the above loop, though I
        //  felt combining them would reduce readability.
        List<XYSeries> ratingSeriesList = new ArrayList<>();
        XYSeries currentRatingSeries = null;
        boolean inRatingLine = false;
        for (int i = 0; i < dataSet.size(); i++) {
            // this makes the data appear from right to left
            float chartPosition = rangeDistance - i - 1;
        
            DurationsChartViewModel.DataPoint dataPoint = dataSet.get(i);
        
            // transitioning into a rating line
            if (!inRatingLine && dataPoint.sleepRating > 0) {
                currentRatingSeries = new XYSeries("Sleep Rating", RIGHT_Y_AXIS);
                ratingSeriesList.add(currentRatingSeries);
                currentRatingSeries.add(chartPosition, dataPoint.sleepRating);
                inRatingLine = true;
            } else if (inRatingLine) {
                // transitioning out of a rating line
                if (dataPoint.sleepRating == 0) {
                    inRatingLine = false;
                }
                // in a rating line
                else {
                    currentRatingSeries.add(chartPosition, dataPoint.sleepRating);
                }
            }
        }
    
        CombinedChartViewFactory.Params params = new CombinedChartViewFactory.Params(
                createMultipleSeriesRenderer(durationSeries.getMaxY(), rangeDistance),
                new XYMultipleSeriesDataset(),
                new ArrayList<>());
    
        // set up the X labels (one at each end of the chart)
        if (!dataSet.isEmpty()) {
            // REFACTOR [21-05-15 5:50PM] -- it would be better if this was in
            //  createMultipleSeriesRenderer() as well.
            // The first data point label is added to the right side of the chart, and the last
            // data point is added to the left side.
            params.renderer.addXTextLabel(rangeDistance - 1, dataSet.get(0).label);
            if (dataSet.size() > 1) {
                params.renderer.addXTextLabel(rangeDistance - dataSet.size(),
                                              dataSet.get(dataSet.size() - 1).label);
            }
        }
    
        // add durations to params
        params.dataSet.addSeries(durationSeries);
        params.renderer.addSeriesRenderer(createDurationsSeriesRenderer());
        params.types.add(new CombinedXYChart.XYCombinedChartDef(BarChart.TYPE, 0));
    
        // add ratings to params
        XYSeriesRenderer ratingSeriesRenderer = createRatingsSeriesRenderer();
        int[] ratingSeriesIndices = new int[ratingSeriesList.size()];
        for (int i = 0; i < ratingSeriesList.size(); i++) {
            params.dataSet.addSeries(ratingSeriesList.get(i));
            params.renderer.addSeriesRenderer(ratingSeriesRenderer);
            ratingSeriesIndices[i] = i + 1; // +1 since the durations bar chart is the first series
        }
        params.types.add(new CombinedXYChart.XYCombinedChartDef(LineChart.TYPE,
                                                                ratingSeriesIndices));
    
        return params;
    }
    
    // REFACTOR [21-05-15 12:47AM] -- a lot of this duplicates the intervals chart renderer
    //  setup in IntervalsChartParamsFactory - these should be consistent with each other.
    private XYMultipleSeriesRenderer createMultipleSeriesRenderer(double maxDataY, int rangeDistance)
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
        for (int i = 0; i < (int) maxY; i++) {
            renderer.addYTextLabel(i + 1,
                                   StatsFormatting.formatDurationsYLabel(i + 1),
                                   LEFT_Y_AXIS);
        }
        
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
        renderer.setMargins(new int[] {35, 65, 0, 45}); // top, left, bottom, right
        
        
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
    
    private XYSeriesRenderer createDurationsSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(mAppColors.appColorComplementary);
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
}
