package com.rbraithwaite.sleepapp.ui.stats.chart_intervals.data_set;

import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.DateRange;

import org.achartengine.model.XYMultipleSeriesDataset;

import javax.inject.Inject;

public class IntervalsDataSet
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SleepIntervalsDataSet";

//*********************************************************
// public properties
//*********************************************************

    public XYMultipleSeriesDataset sleepSessionDataSet;
    public XYMultipleSeriesDataset interruptionsDataSet;
    
    public Config config;

//*********************************************************
// public helpers
//*********************************************************

    public enum Resolution
    {
        WEEK,
        MONTH,
        YEAR
    }
    
    public static class Config
    {
        public DateRange dateRange;
        public boolean invert;
        // The date range's offset
        public int offsetMillis;
        
        public Resolution resolution;
        // if the resolution is WEEK, this is 0,
        // if the resolution is MONTH, this is the month,
        // if the resolution is YEAR, this is the year.
        public int resolutionValue;
        
        public Config(
                DateRange dateRange,
                int offsetMillis,
                boolean invert,
                Resolution resolution)
        {
            this(dateRange, offsetMillis, invert, resolution, 0);
        }
        
        public Config(
                DateRange dateRange,
                int offsetMillis,
                boolean invert,
                Resolution resolution, int resolutionValue)
        {
            this.dateRange = dateRange;
            this.invert = invert;
            this.offsetMillis = offsetMillis;
            this.resolution = resolution;
            this.resolutionValue = resolutionValue;
        }
        
        @Override
        public int hashCode()
        {
            int hash = 7;
            int prime = 13;
            hash = prime * hash + (invert ? 1 : 0);
            hash = prime * hash + dateRange.hashCode();
            hash = prime * hash + offsetMillis;
            hash = prime * hash + resolution.hashCode();
            return hash;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            Config config = (Config) o;
            return invert == config.invert &&
                   dateRange.equals(config.dateRange) &&
                   offsetMillis == config.offsetMillis &&
                   resolution == config.resolution;
        }
    }


//*********************************************************
// constructors
//*********************************************************

    @Inject
    public IntervalsDataSet()
    {
    }

//*********************************************************
// api
//*********************************************************

    public boolean isEmpty()
    {
        return sleepSessionDataSet == null ||
               sleepSessionDataSet.getSeriesCount() == 0;
    }
    
    public boolean hasInterruptionData()
    {
        return interruptionsDataSet != null && interruptionsDataSet.getSeriesCount() > 0;
    }
}

