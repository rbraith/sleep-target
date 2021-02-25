package com.rbraithwaite.sleepapp.ui.stats;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.data.IntervalsDataSetGenerator;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.RangeBarChart;
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
    
    private IntervalsDataSetGenerator.Config mIntervalsConfig;
    
    private RangeBarChart mIntervalsChart;

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
        initIntervalsChart(view);
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
// api
//*********************************************************

    public DateRange getIntervalsDateRange()
    {
        if (mIntervalsConfig == null) {
            return null;
        }
        return mIntervalsConfig.dateRange;
    }
    
    public void setIntervalsDateRange(DateRange intervalsDateRange)
    {
        mIntervalsConfig.dateRange = new DateRange(intervalsDateRange);
        getViewModel().configureIntervalsDataSet(mIntervalsConfig);
    }

//*********************************************************
// private methods
//*********************************************************

    private void initIntervalsChart(final View fragmentRoot)
    {
        final StatsFragmentViewModel viewModel = getViewModel();
        
        final DateRange defaultRange = viewModel.getDefaultIntervalsDateRange();
        
        // TODO [21-02-24 12:19AM] -- I will need to account for fragment recreation here
        //  (retained state)
        //  The viewmodel can store the config.
        mIntervalsConfig = new IntervalsDataSetGenerator.Config(
                defaultRange,
                mIsIntervalDataInverted);
        
        viewModel.configureIntervalsDataSet(mIntervalsConfig);
        
        viewModel.getIntervalsDataSet().observe(
                getViewLifecycleOwner(),
                new Observer<XYMultipleSeriesDataset>()
                {
                    @Override
                    public void onChanged(XYMultipleSeriesDataset dataSet)
                    {
                        XYMultipleSeriesRenderer multipleSeriesRenderer = createRenderer(
                                dataSet,
                                defaultRange,
                                StatsFragmentViewModel.DEFAULT_INTERVALS_OFFSET_HOURS,
                                mIsIntervalDataInverted);
                        
                        GraphicalView intervalsChart = ChartFactory.getRangeBarChartView(
                                requireContext(),
                                dataSet,
                                multipleSeriesRenderer,
                                BarChart.Type.STACKED);
                        
                        FrameLayout intervalsLayout =
                                fragmentRoot.findViewById(R.id.stats_intervals_layout);
                        intervalsLayout.removeAllViews();
                        intervalsLayout.addView(intervalsChart);
                    }
                });
        
        View intervalsTimePeriodSelector =
                fragmentRoot.findViewById(R.id.stats_intervals_time_period_selector);
        
        // time period back
        ImageButton intervalsBack =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_back);
        intervalsBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mIntervalsConfig.dateRange.offsetDays(
                        mIntervalsConfig.dateRange.getDifferenceInDays() * -1);
                viewModel.configureIntervalsDataSet(mIntervalsConfig);
            }
        });
        
        // time period forward
        ImageButton intervalsForward =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_forward);
        intervalsForward.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mIntervalsConfig.dateRange.offsetDays(mIntervalsConfig.dateRange.getDifferenceInDays());
                viewModel.configureIntervalsDataSet(mIntervalsConfig);
            }
        });
        
        // time period value text
        final TextView intervalsValueText =
                intervalsTimePeriodSelector.findViewById(R.id.stats_time_period_value);
        viewModel.getIntervalsValueText().observe(
                getViewLifecycleOwner(),
                new Observer<String>()
                {
                    @Override
                    public void onChanged(String s)
                    {
                        if (s != null) {
                            intervalsValueText.setText(s);
                        }
                    }
                });
    }
    
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
