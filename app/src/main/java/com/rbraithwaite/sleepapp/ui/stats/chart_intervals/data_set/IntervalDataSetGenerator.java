package com.rbraithwaite.sleepapp.ui.stats.chart_intervals.data_set;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.session.Session;
import com.rbraithwaite.sleepapp.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

public class IntervalDataSetGenerator
{
//*********************************************************
// package properties
//*********************************************************

    TimeUtils mTimeUtils;
    
//*********************************************************
// constructors
//*********************************************************

    // REFACTOR [21-06-17 4:04AM] I should add TimeUtils to Hilt and put it as a ctor
    //  actually since it has no deps itself I should be able to inject it as is.
    @Inject
    public IntervalDataSetGenerator(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }


//*********************************************************
// api
//*********************************************************

    
    /**
     * Each series X-value of the returned data set represents a 24hr window into which sleep
     * sessions are placed, with the first window beginning on config.dateRange.getStart(). Sleep
     * sessions which span multiple 24hr windows are divided into separate data points at the window
     * boundaries. Some Y-values are "filler" with min/max values of 0 - this happens when sleep
     * sessions are not evenly distributed between the days of the range, and so these filler values
     * are used to ensure sleep session data occurs at the right series index (the right day).
     *
     * @param sleepSessions The sleep sessions to convert
     * @param config        The configuration for the data - e.g. the date range used to control how
     *                      the sleep sessions are split up, and whether or not the data should be
     *                      inverted
     *
     * @return a new {@link IntervalsDataSet} instance
     */
    public IntervalsDataSet generateFromConfig(
            List<SleepSession> sleepSessions,
            IntervalsDataSet.Config config)
    {
        IntervalsDataSet dataSet = new IntervalsDataSet();
        dataSet.config = config;
        dataSet.sleepSessionDataSet = createDataSetFromSessions(sleepSessions, config);
        dataSet.interruptionsDataSet =
                createDataSetFromSessions(flatMapInterruptionsOf(sleepSessions), config);
        
        return dataSet;
    }
    
//*********************************************************
// private methods
//*********************************************************

    private <S extends Session> XYMultipleSeriesDataset createDataSetFromSessions(
            List<S> sessions, IntervalsDataSet.Config config)
    {
        return convertBucketsToDataSet(
                splitSessionRangeToBuckets(sessions, config.dateRange),
                config.invert);
    }
    
    // REFACTOR [21-08-4 5:10PM] -- I should extract this somewhere.
    private List<Interruption> flatMapInterruptionsOf(List<SleepSession> sleepSessions)
    {
        return sleepSessions.stream()
                .flatMap(sleepSession -> sleepSession.hasNoInterruptions() ?
                        Stream.empty() :
                        sleepSession.getInterruptions().asList().stream())
                .collect(Collectors.toList());
    }
    
    /**
     * <p>
     * Pre-populate a Map with empty "buckets" (ie lists of points) for a given range. Each bucket
     * represents one day in the range, and that bucket's key is the 0th hour of that day in millis
     * (absolute from the epoch). In other words each key is 24hr ahead of the previous key.
     * </p><p>
     * Having pre-existing buckets of the right days is important so that sleep session data that
     * skips days is not compressed down into a contiguous sequence of days.
     * </p><p>
     * A tree map is returned to ensure that the keys remain sorted in ascending order.
     * </p>
     */
    private TreeMap<Long, List<IntervalDataPoint>> createEmptyDayBucketsFrom(DateRange range)
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
    
    private IntervalDataPoint toAbsoluteInterval(Session session)
    {
        return new IntervalDataPoint(
                session.getStart().getTime(),
                session.getEnd().getTime());
    }
    
    /**
     * This method converts the provided sessions into {@link IntervalDataPoint IntervalDataPoints}
     * and splits them up into 24hr-buckets, starting from {@link DateRange#getStart()
     * range.getStart()}. If a session does not fit entirely into one of the buckets, it is divided
     * up until the pieces fit.
     *
     * @param sessions The sessions to put into buckets
     * @param range    The range with which to define the buckets
     *
     * @return The sleep sessions converted to data points and placed into buckets
     */
    private <S extends Session> Map<Long, List<IntervalDataPoint>> splitSessionRangeToBuckets(
            List<S> sessions,
            DateRange range)
    {
        TreeMap<Long, List<IntervalDataPoint>> dayBuckets =
                createEmptyDayBucketsFrom(range);
        
        for (Session session : sessions) {
            IntervalDataPoint absInterval = toAbsoluteInterval(session);
            absInterval.clipTo(range);
            
            Long intervalKey = getDayBucketKey(absInterval, dayBuckets);
            if (intervalKey == null) {
                // TODO [21-06-4 7:03PM] -- raise an exception here?
                //  This means there was no key <= the interval start, which should be
                //  impossible since the interval is clipped...
            }
            
            IntervalDataPoint relInterval = absInterval.relativeTo(intervalKey);
            
            long availableTimeInDay = TimeUtils.MILLIS_24_HOURS - relInterval.startTime;
            
            if (relInterval.getDuration() <= availableTimeInDay) {
                // the interval fits within the 24 hr bucket, no need to split it up
                dayBuckets.get(intervalKey).add(relInterval);
                continue;
            }
            // the interval doesn't fit it the 24 hr bucket and needs to be split
            while (relInterval.getDuration() > availableTimeInDay) {
                long newEndTime = relInterval.startTime + availableTimeInDay;
                IntervalDataPoint remainingInterval = new IntervalDataPoint(
                        0, relInterval.endTime - newEndTime);
                
                relInterval.endTime = newEndTime;
                dayBuckets.get(intervalKey).add(relInterval);
                
                // reset for next iter
                availableTimeInDay = TimeUtils.MILLIS_24_HOURS;
                relInterval = remainingInterval;
                intervalKey += TimeUtils.MILLIS_24_HOURS;
            }
            // add last split part
            if (relInterval.getDuration() > 0) {
                dayBuckets.get(intervalKey).add(relInterval);
            }
        }
        return dayBuckets;
    }
    
    /**
     * The key returned is the largest one in the map that is less than the interval start.
     */
    private Long getDayBucketKey(
            IntervalDataPoint interval,
            TreeMap<Long, List<IntervalDataPoint>> buckets)
    {
        return buckets.floorKey(interval.startTime);
    }
    
    // TODO [21-02-21 3:35PM] what do i do about overlapping sleep sessions? - i should probably
    //  prevent them from being possible to create in the first place.
    private XYMultipleSeriesDataset convertBucketsToDataSet(
            Map<Long, List<IntervalDataPoint>> dayBuckets,
            boolean invert)
    {
        final int BUCKET_COUNT = dayBuckets.size();
        int emptyBuckets;
        int sign = invert ? -1 : 1;
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        while (true) {
            emptyBuckets = 0;
            RangeCategorySeries series = new RangeCategorySeries("Interval");
            for (Map.Entry<Long, List<IntervalDataPoint>> entry : dayBuckets.entrySet()) {
                List<IntervalDataPoint> bucket = entry.getValue();
                if (bucket.isEmpty()) {
                    emptyBuckets++;
                    if (emptyBuckets == BUCKET_COUNT) {
                        return dataSet;
                    }
                    // add filler data
                    series.add(0, 0);
                } else {
                    IntervalDataPoint interval = bucket.remove(0);
                    series.add(
                            sign * mTimeUtils.millisToHours(interval.startTime),
                            sign * mTimeUtils.millisToHours(interval.endTime));
                }
            }
            dataSet.addSeries(series.toXYSeries());
        }
    }
}
