package com.rbraithwaite.sleepapp.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StatsFragment
        extends BaseFragment<StatsFragmentViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private boolean mIsIntervalDataInverted = true;
    
//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.stats_fragment, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState)
    {
        // unless state was retained, use default week
        //  (default week is the week that today is a part of (starting on mon))
        
        StatsFragmentViewModel viewModel = getViewModel();
        // TODO [21-02-20 1:27AM] -- I will need to account for retained state here.
        // REFACTOR [21-02-21 7:08PM] -- eventually it might be better to use that switchmap
        //  pattern where you pass a key to the viewmodel and have a value livedata that updates.
        final DateRange defaultRange = viewModel.getDefaultIntervalsDateRange();
        LiveData<XYMultipleSeriesDataset> intervalDataSet =
                viewModel.getIntervalDataSetFromDateRange(defaultRange, mIsIntervalDataInverted);
        
        LiveDataFuture.getValue(
                intervalDataSet,
                getViewLifecycleOwner(),
                new LiveDataFuture.OnValueListener<XYMultipleSeriesDataset>()
                {
                    @Override
                    public void onValue(XYMultipleSeriesDataset dataSet)
                    {
                        XYMultipleSeriesRenderer multipleSeriesRenderer = createRenderer(
                                dataSet,
                                defaultRange,
                                StatsFragmentViewModel.DEFAULT_CHART_OFFSET_HOURS,
                                mIsIntervalDataInverted);
                        
                        GraphicalView rangeBarChart = ChartFactory.getRangeBarChartView(
                                requireContext(),
                                dataSet,
                                multipleSeriesRenderer,
                                BarChart.Type.STACKED);
                        
                        FrameLayout intervalsLayout =
                                view.findViewById(R.id.stats_sleep_intervals_layout);
                        intervalsLayout.addView(rangeBarChart);
                    }
                });
    }
    
    @Override
    protected boolean getBottomNavVisibility()
    {
        return true;
    }
    
    @Override
    protected Class<StatsFragmentViewModel> getViewModelClass()
    {
        return StatsFragmentViewModel.class;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private XYMultipleSeriesRenderer createRenderer(
            XYMultipleSeriesDataset dataSet,
            DateRange range,
            int hoursOffset,
            boolean invert)
    {
        XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        
        for (XYSeries series : dataSet.getSeries()) {
            multipleSeriesRenderer.addSeriesRenderer(new XYSeriesRenderer());
        }
        
        // X axis
        // -----------------------------------------------------
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(range.getStart());
        // If the chart's offset is > 12, more of the next day will be displayed in the column
        // than the current day, so mark the chart with the next day instead
        if (hoursOffset > 12) {
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        // REFACTOR [21-02-22 10:34PM] -- I shouldn't be using SimpleDateFormat here.
        SimpleDateFormat xLabelFormatter = new SimpleDateFormat("M/d", Locale.CANADA);
        for (int i = 0; i < range.getDifferenceInDays(); i++) {
            // labels need to start at 1 since that's what the series starts at apparently :/
            multipleSeriesRenderer.addXTextLabel(i + 1, xLabelFormatter.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        multipleSeriesRenderer.setXLabels(0);
        multipleSeriesRenderer.setXAxisMin(0.5f);
        multipleSeriesRenderer.setXAxisMax(7.5f);
        
        // Y axis
        // -----------------------------------------------------
        int sign = invert ? -1 : 1;
        
        for (int i = 1; i < 25; i++) {
            if (i % 2 == 0) {
                multipleSeriesRenderer.addYTextLabel(
                        sign * TimeUtils.hoursToMillis(i + hoursOffset),
                        getHourLabel(i + hoursOffset));
            }
        }
        multipleSeriesRenderer.addYTextLabel(
                sign * TimeUtils.hoursToMillis(hoursOffset), getHourLabel(hoursOffset));
        multipleSeriesRenderer.setYLabels(0);
        double yMin;
        double yMax;
        if (invert) {
            yMin = sign * TimeUtils.hoursToMillis(24 + hoursOffset);
            yMax = sign * TimeUtils.hoursToMillis(hoursOffset);
        } else {
            yMin = sign * TimeUtils.hoursToMillis(hoursOffset);
            yMax = sign * TimeUtils.hoursToMillis(24 + hoursOffset);
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
            long midnight = TimeUtils.MILLIS_24_HOURS;
            // 10000 is just a good factor for allowing the coarseness of adjustment with the first
            // factor that I wanted
            double width = 30 * 10000;
            for (int i = 0; i < range.getDifferenceInDays(); i++) {
                midnightLine.add(sign * midnight, sign * (midnight + width));
            }
            dataSet.addSeries(midnightLine.toXYSeries());
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
    
    private String getHourLabel(int hour)
    {
        hour = hour % 24;
        String period = "am";
        if (hour >= 12) {
            if (hour < 24) { period = "pm"; }
            if (hour > 12) { hour -= 12; }
        }
        if (hour == 0) { hour = 12; }
        return hour + period;
    }
}
