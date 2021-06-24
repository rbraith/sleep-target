package com.rbraithwaite.sleepapp.ui.stats.common;

import android.content.Context;
import android.view.View;

import org.achartengine.ChartFactory;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.List;

public class CombinedChartViewFactory
{
//*********************************************************
// public helpers
//*********************************************************

    public static class Params
    {
        public XYMultipleSeriesRenderer renderer;
        public XYMultipleSeriesDataset dataSet;
        // This is a List here instead of an array just because Lists are easier to work with
        public List<CombinedXYChart.XYCombinedChartDef> types;
        
        public Params(
                XYMultipleSeriesRenderer renderer,
                XYMultipleSeriesDataset dataSet,
                List<CombinedXYChart.XYCombinedChartDef> types)
        {
            this.renderer = renderer;
            this.dataSet = dataSet;
            this.types = types;
        }
    }

//*********************************************************
// api
//*********************************************************

    public View createFrom(Context context, Params params)
    {
        return ChartFactory.getCombinedXYChartView(
                context,
                params.dataSet,
                params.renderer,
                params.types.toArray(
                        new CombinedXYChart.XYCombinedChartDef[0]));
    }
}
