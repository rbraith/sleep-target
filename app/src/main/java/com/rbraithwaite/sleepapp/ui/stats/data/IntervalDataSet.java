package com.rbraithwaite.sleepapp.ui.stats.data;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class IntervalDataSet
{
//*********************************************************
// package properties
//*********************************************************

    XYMultipleSeriesDataset mDataSet;
    
//*********************************************************
// constructors
//*********************************************************

    private IntervalDataSet() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    
    /**
     * Each series X-value of the returned data set represents a 24hr window into which sleep
     * sessions are placed, with the first window beginning on range.getStart(). Sleep sessions
     * which span multiple 24hr windows are divided into separate data points at the window
     * boundaries. Some Y-values are "filler" with min/max values of 0 - this happens when sleep
     * sessions are not evenly distributed between the days of the range, and so these filler values
     * are used to ensure sleep session data occurs at the right series index (the right day).
     *
     * @param sleepSessions The sleep sessions to convert
     * @param range         The range to define how the sleep sessions are split up
     * @param invert        Whether or not the sign of the returned data should be inverted. This is
     *                      used to display data going from top to bottom in charts.
     *
     * @return a new IntervalDataSet instance
     */
    public static IntervalDataSet fromSleepSessionRange(
            List<SleepSessionModel> sleepSessions,
            DateRange range,
            boolean invert)
    {
        IntervalDataSet dataSet = new IntervalDataSet();
        dataSet.mDataSet = convertBucketsToDataSet(
                splitSleepSessionRangeToBuckets(sleepSessions, range, invert));
        return dataSet;
    }
    
    public XYMultipleSeriesDataset getDataSet()
    {
        return mDataSet;
    }
    
//*********************************************************
// private methods
//*********************************************************

    // TODO [21-02-21 3:35PM] what do i do about overlapping sleep sessions? - i should probably
    //  prevent  them from being possible to create in the first place.
    private static XYMultipleSeriesDataset convertBucketsToDataSet(
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
    private static TreeMap<Long, List<IntervalDataPoint>> createMapWithEmptyBuckets(DateRange range)
    {
        TreeMap<Long, List<IntervalDataPoint>> dataPointBuckets = new TreeMap<>();
        
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(range.getStart());
        long key = cal.getTimeInMillis();
        long rangeEnd = range.getEnd().getTime();
        while (key < rangeEnd) {
            dataPointBuckets.put(key, new ArrayList<IntervalDataPoint>());
            key += TimeUtils.MILLIS_24_HOURS;
        }
        
        return dataPointBuckets;
    }
    
    private static boolean keyIsInRange(long key, DateRange range)
    {
        return (key >= range.getStart().getTime() && key <= range.getEnd().getTime());
    }
    
    /**
     * This method converts the provided sleep sessions into
     * {@link IntervalDataSet.IntervalDataPoint
     * IntervalDataPoints} and splits them up into 24hr-buckets, starting from {@link
     * DateRange#getStart() range.getStart()}. If a sleep session does not fit entirely into one of
     * the buckets, it is divided up until the pieces fit.
     *
     * @param sleepSessions The sleep sessions to put into buckets
     * @param range         The range with which to define the buckets
     * @param invert        Whether or not the sign of the data should be inverted. Used for
     *                      displaying downwards bar charts.
     *
     * @return The sleep sessions converted to data points and placed into buckets
     */
    private static Map<Long, List<IntervalDataPoint>> splitSleepSessionRangeToBuckets(
            List<SleepSessionModel> sleepSessions,
            DateRange range,
            boolean invert)
    {
        Map<Long, List<IntervalDataPoint>> dataPointBuckets = createMapWithEmptyBuckets(range);
        
        // the keys are based on range.getStart()
        long relKeyTime = TimeUtils.getTimeOfDay(range.getStart());
        // this is used to split up sleep sessions which extend past their 24hr buckets
        long relNextKeyTime =
                relKeyTime + TimeUtils.MILLIS_24_HOURS; // (rel)ative to the 24hr clock
        
        GregorianCalendar cal = new GregorianCalendar();
        for (SleepSessionModel sleepSession : sleepSessions) {
            cal.setTime(sleepSession.getStart());
            long relStartTime = TimeUtils.getTimeOfDay(cal);
            
            // setting the calendar to the key's time of day so that I can extract the
            // correct absolute key value
            TimeUtils.setCalendarTimeOfDay(cal, relKeyTime);
            long key = cal.getTimeInMillis();
            long offset = 0;
            if (relStartTime < relKeyTime) {
                // If the range start time is greater than (comes after) the sleep session start
                // time, it means the key time is from the previous calendar day. The sleep session
                // start time falls somewhere between its key and the next key - a 24hr window. In
                // addition, in these cases you must also offset the start & ends of the data
                // points by 24hrs. This is because if the range does not start at midnight, one
                // 24hr bucket will be comprised of hours from 2 different days, and you need to
                // have the start & end times relative to the correct day to display properly.
                key -= TimeUtils.MILLIS_24_HOURS;
                offset = TimeUtils.MILLIS_24_HOURS;
            }
            int sign = invert ? -1 : 1;
            
            long remainingDuration = sleepSession.getDuration();
            while (true) {
                long relEndTime = relStartTime + remainingDuration;
                if (relEndTime > relNextKeyTime) {
                    // If the remaining duration extends past the next key, then
                    // keep looping
                    remainingDuration = relEndTime - relNextKeyTime;
                    relEndTime = relNextKeyTime;
                    // The first sleep session segment might occur before the range start, or the
                    // last sleep session segment might occur after range end, this check
                    // effectively prunes these segments
                    if (keyIsInRange(key, range)) {
                        dataPointBuckets.get(key).add(new IntervalDataPoint(
                                sign * (relStartTime + offset),
                                sign * (relEndTime + offset)));
                    }
                    
                    // setup next loop
                    relStartTime = relKeyTime;
                    key += TimeUtils.MILLIS_24_HOURS;
                } else {
                    // otherwise just add the data point
                    if (keyIsInRange(key, range)) {
                        dataPointBuckets.get(key).add(new IntervalDataPoint(
                                sign * (relStartTime + offset),
                                sign * (relEndTime + offset)));
                    }
                    break;
                }
            }
        }
        return dataPointBuckets;
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
