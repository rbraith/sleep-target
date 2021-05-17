package com.rbraithwaite.sleepapp.ui.stats.chart_intervals;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

public class SleepIntervalsDataSet
{
//*********************************************************
// private properties
//*********************************************************

    private XYMultipleSeriesDataset mDataSet;
    private Config mConfig;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SleepIntervalsDataSet";

//*********************************************************
// public helpers
//*********************************************************

    public static class Config
    {
        public DateRange dateRange;
        public boolean invert;
        // The date range's offset
        public int offsetMillis;
        
        public Config(DateRange dateRange, int offsetMillis, boolean invert)
        {
            this.dateRange = dateRange;
            this.invert = invert;
            this.offsetMillis = offsetMillis;
        }
        
        @Override
        public int hashCode()
        {
            int hash = 7;
            int prime = 13;
            hash = prime * hash + (invert ? 1 : 0);
            hash = prime * hash + dateRange.hashCode();
            hash = prime * hash + offsetMillis;
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
                   offsetMillis == config.offsetMillis;
        }
    }
    
    // REFACTOR [21-02-28 11:00PM] -- Generator is way larger than SleepIntervalsDataSet,
    //  maybe extract this.
    public static class Generator
    {
        private static int INVALID_KEY = -1;
        TimeUtils mTimeUtils;
        
        @Inject
        public Generator()
        {
            mTimeUtils = createTimeUtils();
        }


        
        /**
         * Each series X-value of the returned data set represents a 24hr window into which sleep
         * sessions are placed, with the first window beginning on config.dateRange.getStart().
         * Sleep sessions which span multiple 24hr windows are divided into separate data points at
         * the window boundaries. Some Y-values are "filler" with min/max values of 0 - this happens
         * when sleep sessions are not evenly distributed between the days of the range, and so
         * these filler values are used to ensure sleep session data occurs at the right series
         * index (the right day).
         *
         * @param sleepSessions The sleep sessions to convert
         * @param config        The configuration for the data - e.g. the date range used to control
         *                      how the sleep sessions are split up, and whether or not the data
         *                      should be inverted
         *
         * @return a new {@link SleepIntervalsDataSet} instance
         */
        public SleepIntervalsDataSet generateFromConfig(
                List<SleepSession> sleepSessions,
                Config config)
        {
            SleepIntervalsDataSet dataSet = new SleepIntervalsDataSet();
            dataSet.mConfig = config;
            dataSet.mDataSet = convertBucketsToDataSet(splitSleepSessionRangeToBuckets(
                    sleepSessions,
                    config.dateRange,
                    config.invert));
            
            return dataSet;
        }
        
        protected TimeUtils createTimeUtils()
        {
            return new TimeUtils();
        }


        
        /**
         * <p>
         * Pre-populate a Map with empty "buckets" (ie lists of points) for a given range. Each
         * bucket represents one day in the range, and that bucket's key is the 0th hour of that day
         * in millis (absolute from the epoch). In other words each key is 24hr ahead of the
         * previous key.
         * </p><p>
         * Having pre-existing buckets of the right days is important so that sleep session data
         * that skips days is not compressed down into a contiguous sequence of days.
         * </p><p>
         * A tree map is returned to ensure that the keys remain sorted in ascending order.
         * </p>
         */
        private TreeMap<Long, List<IntervalDataPoint>> createMapWithEmptyBuckets(DateRange range)
        {
            TreeMap<Long, List<IntervalDataPoint>> dataPointBuckets = new TreeMap<>();
            long key = range.getStart().getTime();
            long rangeEnd = range.getEnd().getTime();
            while (key < rangeEnd) {
                dataPointBuckets.put(key, new ArrayList<>());
                key += TimeUtils.MILLIS_24_HOURS;
            }
            return dataPointBuckets;
        }
        
        private boolean keyIsInRange(long key, DateRange range)
        {
            if (key == INVALID_KEY) {
                return false;
            }
            return (key >= range.getStart().getTime() && key <= range.getEnd().getTime());
        }
        
        /**
         * This method converts the provided sleep sessions into {@link
         * SleepIntervalsDataSet.IntervalDataPoint IntervalDataPoints} and splits them up into
         * 24hr-buckets, starting from {@link DateRange#getStart() range.getStart()}. If a sleep
         * session does not fit entirely into one of the buckets, it is divided up until the pieces
         * fit.
         *
         * @param sleepSessions The sleep sessions to put into buckets
         * @param range         The range with which to define the buckets
         * @param invert        Whether or not the sign of the data should be inverted. Used for
         *                      displaying downwards bar charts.
         *
         * @return The sleep sessions converted to data points and placed into buckets
         */
        private Map<Long, List<IntervalDataPoint>> splitSleepSessionRangeToBuckets(
                List<SleepSession> sleepSessions,
                DateRange range,
                boolean invert)
        {
            // the keys are based on range.getStart()
            long relKeyTime = mTimeUtils.getTimeOfDay(range.getStart());
            
            TreeMap<Long, List<IntervalDataPoint>> dataPointBuckets =
                    createMapWithEmptyBuckets(range);
            
            // this is used to split up sleep sessions which extend past their 24hr buckets
            long relNextKeyTime =
                    relKeyTime + TimeUtils.MILLIS_24_HOURS; // (rel)ative to the 24hr clock
            
            for (SleepSession sleepSession : sleepSessions) {
                long key = getKeyForSession(sleepSession.getStart().getTime(), dataPointBuckets);
                long relStartTime = mTimeUtils.getTimeOfDay(sleepSession.getStart());
                
                // If the relative session start time is less than the relative key time, you
                // need to
                // offset the data by 24hrs in order for it to display properly. This is because
                // when the range does not start at midnight, one 24hr buckets will contain time
                // from
                // 2 different days, and you must select the right day to display the data
                // relative to
                // (eg 0400 or 2800).
                long offset = relStartTime < relKeyTime ? TimeUtils.MILLIS_24_HOURS : 0;
                
                int sign = invert ? -1 : 1;
                
                long remainingDuration = sleepSession.getDurationMillis();
                while (true) {
                    long relEndTime = relStartTime + remainingDuration;
                    if (relEndTime > relNextKeyTime) {
                        // If the remaining duration extends past the next key, then
                        // keep looping
                        remainingDuration = relEndTime - relNextKeyTime;
                        relEndTime = relNextKeyTime;
                        // The first sleep session segment might occur before the range start, or
                        // the last sleep session segment might occur after range end, this check
                        // effectively prunes these segments
                        if (keyIsInRange(key, range)) {
                            dataPointBuckets.get(key).add(new IntervalDataPoint(
                                    sign * mTimeUtils.millisToHours(relStartTime + offset),
                                    sign * mTimeUtils.millisToHours(relEndTime + offset)));
                        }
                        
                        // setup next loop
                        relStartTime = relKeyTime;
                        key += TimeUtils.MILLIS_24_HOURS;
                    } else {
                        // otherwise just add the data point
                        if (keyIsInRange(key, range)) {
                            dataPointBuckets.get(key).add(new IntervalDataPoint(
                                    sign * mTimeUtils.millisToHours(relStartTime + offset),
                                    sign * mTimeUtils.millisToHours(relEndTime + offset)));
                        }
                        break;
                    }
                }
            }
            return dataPointBuckets;
        }
        
        /**
         * The key returned is the largest one in the map that is less than sessionStartTime.
         */
        private long getKeyForSession(
                long sessionStartTime,
                TreeMap<Long, List<IntervalDataPoint>> buckets)
        {
            Long key = buckets.floorKey(sessionStartTime);
            return key == null ? INVALID_KEY : key;
        }
        
        // TODO [21-02-21 3:35PM] what do i do about overlapping sleep sessions? - i should probably
        //  prevent them from being possible to create in the first place.
        private XYMultipleSeriesDataset convertBucketsToDataSet(
                Map<Long, List<IntervalDataPoint>> dataPointBuckets)
        {
            final int BUCKET_COUNT = dataPointBuckets.size();
            int emptyBuckets;
            XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
            while (true) {
                emptyBuckets = 0;
                RangeCategorySeries series = new RangeCategorySeries("Interval");
                for (Map.Entry<Long, List<IntervalDataPoint>> entry : dataPointBuckets.entrySet()) {
                    List<IntervalDataPoint> bucket = entry.getValue();
                    if (bucket.isEmpty()) {
                        emptyBuckets++;
                        if (emptyBuckets == BUCKET_COUNT) {
                            return dataSet;
                        }
                        // add filler data
                        series.add(0, 0);
                    } else {
                        IntervalDataPoint dataPoint = bucket.remove(0);
                        series.add(dataPoint.startTime, dataPoint.endTime);
                    }
                }
                dataSet.addSeries(series.toXYSeries());
            }
        }
    }


//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepIntervalsDataSet()
    {
    }

//*********************************************************
// api
//*********************************************************

    public XYMultipleSeriesDataset getDataSet()
    {
        return mDataSet;
    }
    
    public Config getConfig()
    {
        return mConfig;
    }


//*********************************************************
// private helpers
//*********************************************************

    private static class IntervalDataPoint
    {
        double startTime;
        double endTime;
        
        public IntervalDataPoint(double startTime, double endTime)
        {
            this.startTime = startTime;
            this.endTime = endTime;
        }
    }
}

