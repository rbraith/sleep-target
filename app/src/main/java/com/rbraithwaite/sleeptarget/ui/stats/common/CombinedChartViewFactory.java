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

package com.rbraithwaite.sleeptarget.ui.stats.common;

import android.content.Context;
import android.view.View;

import org.achartengine.ChartFactory;
import org.achartengine.chart.CombinedXYChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import java.util.ArrayList;
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
        
        public Params()
        {
            this(new XYMultipleSeriesRenderer(), new XYMultipleSeriesDataset(), new ArrayList<>());
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
