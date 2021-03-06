package com.rbraithwaite.sleepapp.ui.stats.charts;

import android.content.Context;
import android.view.View;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleepapp.ui.stats.data.SleepIntervalsDataSet;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.concurrent.Executor;

import javax.inject.Inject;

public class SleepIntervalsChartFactory
{
//*********************************************************
// package properties
//*********************************************************

    Executor mExecutor;
    SleepIntervalsRendererHelper mRendererHelper;
    
//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepIntervalsChartFactory(
            Executor executor,
            SleepIntervalsRendererHelper rendererHelper)
    {
        mExecutor = executor;
        mRendererHelper = rendererHelper;
    }
    
//*********************************************************
// api
//*********************************************************

    public LiveData<View> createRangeChartAsync(
            final Context context,
            final SleepIntervalsDataSet dataSet)
    {
        return createChartAsync(context, dataSet, new Factory<XYMultipleSeriesRenderer>()
        {
            @Override
            public XYMultipleSeriesRenderer create()
            {
                return mRendererHelper.createRangeRenderer(dataSet);
            }
        });
    }
    
    public LiveData<View> createMonthChartAsync(
            Context context,
            final SleepIntervalsDataSet dataSet,
            final int month)
    {
        return createChartAsync(context, dataSet, new Factory<XYMultipleSeriesRenderer>()
        {
            @Override
            public XYMultipleSeriesRenderer create()
            {
                return mRendererHelper.createMonthRenderer(dataSet, month);
            }
        });
    }
    
    public LiveData<View> createYearChartAsync(
            Context context,
            final SleepIntervalsDataSet dataSet,
            final int year)
    {
        return createChartAsync(context, dataSet, new Factory<XYMultipleSeriesRenderer>()
        {
            @Override
            public XYMultipleSeriesRenderer create()
            {
                return mRendererHelper.createYearRenderer(dataSet, year);
            }
        });
    }
    
//*********************************************************
// private methods
//*********************************************************

    private LiveData<View> createChartAsync(
            final Context context,
            final SleepIntervalsDataSet dataSet,
            Factory<XYMultipleSeriesRenderer> rendererFactory)
    {
        return Transformations.map(
                createAsync(rendererFactory),
                new Function<XYMultipleSeriesRenderer, View>()
                {
                    @Override
                    public View apply(XYMultipleSeriesRenderer renderer)
                    {
                        return ChartFactory.getRangeBarChartView(
                                context,
                                dataSet.getDataSet(),
                                renderer,
                                BarChart.Type.STACKED);
                    }
                });
    }
    
    // REFACTOR [21-03-4 3:08AM] -- extract this as a general utility
    private <T> LiveData<T> createAsync(final Factory<T> factory)
    {
        final MutableLiveData<T> liveData = new MutableLiveData<>();
        
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                liveData.postValue(factory.create());
            }
        });
        
        return liveData;
    }
    
//*********************************************************
// private helpers
//*********************************************************

    private interface Factory<T>
    {
        T create();
    }
}
