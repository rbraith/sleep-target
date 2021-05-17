package com.rbraithwaite.sleepapp.ui.stats.chart_durations;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.stats.common.RangeSelectorController;
import com.rbraithwaite.sleepapp.utils.LiveDataFuture;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.chart.LineChart;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.List;
import java.util.concurrent.Executor;

public class DurationsChartController
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "DurationsChartControlle";
    private final int LEFT_Y_AXIS = 0;
    private final int RIGHT_Y_AXIS = 1;
    private final View mRoot;
    private final FrameLayout mChartLayout;
    private final DurationsChartViewModel mViewModel;
    private final RangeSelectorController mRangeSelectorController;
    private final Executor mExecutor;
    private final LifecycleOwner mLifeCycleOwner;
    
//*********************************************************
// constructors
//*********************************************************

    public DurationsChartController(
            View root,
            DurationsChartViewModel viewModel,
            LifecycleOwner lifecycleOwner,
            Executor executor)
    {
        mRoot = root;
        mChartLayout = root.findViewById(R.id.stats_durations_layout);
        
        mLifeCycleOwner = lifecycleOwner;
        mExecutor = executor;
        
        mViewModel = viewModel;
        
        mViewModel.getDataSet().observe(
                lifecycleOwner,
                this::displayData);
        
        mRangeSelectorController = new RangeSelectorController(
                mRoot.findViewById(R.id.stats_durations_range_selector))
        {
            @Override
            public int getMenuId() { return R.menu.stats_durations_popup_menu; }
            
            @Override
            public void onPopupMenuInflated(Menu popupMenu)
            {
                MenuItem previouslyChecked = popupMenu.findItem(
                        getResolutionMenuItemId(mViewModel.getRangeDistance()));
                previouslyChecked.setChecked(true);
            }
            
            @Override
            public boolean onPopupMenuItemClicked(MenuItem item)
            {
                switch (item.getItemId()) {
                case R.id.stats_durations_resolution_10:
                    item.setChecked(true);
                    mViewModel.setRangeDistance(10);
                    return true;
                case R.id.stats_durations_resolution_50:
                    item.setChecked(true);
                    mViewModel.setRangeDistance(50);
                    return true;
                case R.id.stats_durations_resolution_100:
                    item.setChecked(true);
                    mViewModel.setRangeDistance(100);
                    return true;
                default:
                    return false;
                }
            }
            
            @Override
            public void onBackPressed()
            {
                mViewModel.stepRangeBack();
            }
            
            @Override
            public void onForwardPressed()
            {
                mViewModel.stepRangeForward();
            }
        };
        
        mViewModel.getRangeText().observe(lifecycleOwner, mRangeSelectorController::setText);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private XYMultipleSeriesRenderer createMultipleSeriesRenderer()
    {
        final int TWO_Y_AXES = 2;
        XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer(TWO_Y_AXES);
        multipleSeriesRenderer.addSeriesRenderer(createDurationsSeriesRenderer());
        multipleSeriesRenderer.addSeriesRenderer(createRatingsSeriesRenderer());
        
        // Y axis
        // -----------------------------------------------------
        multipleSeriesRenderer.setYLabelsPadding(50);
        multipleSeriesRenderer.setShowCustomTextGridY(true);
        multipleSeriesRenderer.setYLabels(0);
        
        // left y axis (durations)
        // REFACTOR [21-05-15 2:01PM] -- hardcoded string.
        multipleSeriesRenderer.setYTitle("Duration", LEFT_Y_AXIS);
        multipleSeriesRenderer.setYAxisAlign(Paint.Align.LEFT, LEFT_Y_AXIS);
        multipleSeriesRenderer.setYAxisMin(0, LEFT_Y_AXIS);
        
        // right y axis (ratings)
        // REFACTOR [21-05-15 2:01PM] -- hardcoded string.
        multipleSeriesRenderer.setYTitle("Rating", RIGHT_Y_AXIS);
        multipleSeriesRenderer.setYAxisAlign(Paint.Align.RIGHT, RIGHT_Y_AXIS);
        multipleSeriesRenderer.setYLabelsAlign(Paint.Align.RIGHT, RIGHT_Y_AXIS);
        
        multipleSeriesRenderer.setYLabelsColor(1, Color.RED);
        multipleSeriesRenderer.setYAxisMax(6.0, RIGHT_Y_AXIS);
        
        for (int i = 0; i < 5; i++) {
            multipleSeriesRenderer.addYTextLabel(i + 1, Integer.toString(i + 1), RIGHT_Y_AXIS);
        }
//            multipleSeriesRenderer.setShowLabels(true);
        
        
        // X axis
        // -----------------------------------------------------
        multipleSeriesRenderer.setBarSpacing(0.1);
        // this is so that the bars at the far edges of the chart are not cut off
        multipleSeriesRenderer.setXAxisMin(-0.5f);
        multipleSeriesRenderer.setXAxisMax(mViewModel.getRangeDistance() - 0.5f);
        
        // misc
        // -----------------------------------------------------
        // REFACTOR [21-05-15 12:47AM] -- a lot of this duplicates the intervals chart renderer
        //  setup in SleepIntervalsRendererHelper - these should be consistent with each other.
        multipleSeriesRenderer.setLabelsTextSize(30);
        multipleSeriesRenderer.setAxisTitleTextSize(50);
        
        multipleSeriesRenderer.setPanEnabled(false);
        
        multipleSeriesRenderer.setMargins(new int[] {35, 100, 0, 100}); // top, left, bottom, right
        multipleSeriesRenderer.setShowLegend(false);
        
        // hide black background
        multipleSeriesRenderer.setMarginsColor(Color.argb(0xff, 0xff, 0xff, 0xff));
        
        multipleSeriesRenderer.setLabelsColor(Color.BLACK);
        multipleSeriesRenderer.setAxesColor(Color.BLACK);
        
        return multipleSeriesRenderer;
    }
    
    private XYSeriesRenderer createDurationsSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.BLUE);
        return renderer;
    }
    
    private XYSeriesRenderer createRatingsSeriesRenderer()
    {
        XYSeriesRenderer renderer = new XYSeriesRenderer();
        renderer.setColor(Color.RED);
//            renderer.setFillPoints(true);
        renderer.setDisplayBoundingPoints(true);
        renderer.setLineWidth(6);
        renderer.setPointStyle(PointStyle.CIRCLE);
        renderer.setPointStrokeWidth(30);
//            renderer.setDisplayChartValues(true);
//            renderer.setChartValuesTextSize(40);
//            renderer.setDisplayChartValuesDistance(50);
//            renderer.setChartValuesSpacing(40);
//            renderer.setChartValuesFormat(new DecimalFormat("#0.0"));
        return renderer;
    }
    
    private int getResolutionMenuItemId(int rangeDistance)
    {
        switch (rangeDistance) {
        case 10:
            return R.id.stats_durations_resolution_10;
        case 50:
            return R.id.stats_durations_resolution_50;
        case 100:
            return R.id.stats_durations_resolution_100;
        default:
            // TODO [21-05-15 2:55PM] -- throw exception here.
            return -1;
        }
    }
    
    private void displayData(List<DurationsChartViewModel.DataPoint> dataSet)
    {
        if (dataSet == null || dataSet.isEmpty()) {
            displayNoDataMessage();
        } else {
            updateChartWithData(dataSet);
        }
    }
    
    private void displayNoDataMessage()
    {
        // TODO [21-05-17 3:41PM] -- this should be from xml ideally.
        TextView noDataMessage = new TextView(mRoot.getContext());
        // REFACTOR [21-05-17 3:42PM] -- hardcoded string.
        noDataMessage.setText("No Data");
        mChartLayout.removeAllViews();
        mChartLayout.addView(noDataMessage);
    }
    
    /**
     * Assumes the dataset is not empty or null.
     */
    private void updateChartWithData(List<DurationsChartViewModel.DataPoint> dataSet)
    {
        MutableLiveData<ChartParams> chartParams = new MutableLiveData<>();
        
        LiveData<View> chartView = Transformations.map(
                chartParams,
                params -> ChartFactory.getCombinedXYChartView(
                        mRoot.getContext(),
                        params.dataset,
                        params.renderer,
                        params.types));
        
        LiveDataFuture.getValue(chartView, mLifeCycleOwner, view -> {
            mChartLayout.removeAllViews();
            mChartLayout.addView(view);
        });
        
        // create the params for the new chart view
        mExecutor.execute(() -> {
            // REFACTOR [21-05-14 11:28PM] -- hardcoded strings.
            XYSeries durationSeries = new XYSeries("Sleep Duration", LEFT_Y_AXIS);
            XYSeries ratingSeries = new XYSeries("Sleep Rating", RIGHT_Y_AXIS);
            
            int distance = mViewModel.getRangeDistance();
            for (int i = 0; i < dataSet.size(); i++) {
                DurationsChartViewModel.DataPoint dataPoint = dataSet.get(i);
                
                // this makes the data appear from right to left
                float chartPosition = distance - i - 1;
                
                durationSeries.add(chartPosition, dataPoint.sleepDurationHours);
                ratingSeries.add(chartPosition, dataPoint.sleepRating);
            }
            
            XYMultipleSeriesDataset multipleSeriesDataset = new XYMultipleSeriesDataset();
            XYMultipleSeriesRenderer multipleSeriesRenderer = createMultipleSeriesRenderer();
            
            // set up the X labels (one at each end of the chart)
            if (!dataSet.isEmpty()) {
                // REFACTOR [21-05-15 5:50PM] -- it would be better if this was in
                //  createMultipleSeriesRenderer()
                //  as well.
                multipleSeriesRenderer.setXLabels(0);
                // The first datapoint label is added to the right side of the chart, and the last
                // datapoint is added to the left side.
                multipleSeriesRenderer.addXTextLabel(distance - 1, dataSet.get(0).label);
                if (dataSet.size() > 1) {
                    multipleSeriesRenderer.addXTextLabel(distance - dataSet.size(),
                                                         dataSet.get(dataSet.size() - 1).label);
                }
            }
            
            // first add the duration, then the rating, so that the rating is overlaid on top
            multipleSeriesDataset.addSeries(durationSeries);
            multipleSeriesDataset.addSeries(ratingSeries);
            
            chartParams.postValue(new ChartParams(
                    multipleSeriesRenderer,
                    multipleSeriesDataset,
                    new CombinedXYChart.XYCombinedChartDef[] {
                            new CombinedXYChart.XYCombinedChartDef(BarChart.TYPE, 0),
                            new CombinedXYChart.XYCombinedChartDef(LineChart.TYPE, 1)
                    }));
        });
    }
    
//*********************************************************
// private helpers
//*********************************************************

    private static class ChartParams
    {
        XYMultipleSeriesRenderer renderer;
        XYMultipleSeriesDataset dataset;
        CombinedXYChart.XYCombinedChartDef[] types;
        
        public ChartParams(
                XYMultipleSeriesRenderer renderer,
                XYMultipleSeriesDataset dataset,
                CombinedXYChart.XYCombinedChartDef[] types)
        {
            this.renderer = renderer;
            this.dataset = dataset;
            this.types = types;
        }
    }
}
