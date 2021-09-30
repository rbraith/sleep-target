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

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.models.session.Session;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.DateRange;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

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
     * @param wakeTimeGoals The wake-time goals relevant to the provided date range.
     * @param config        The configuration for the data - e.g. the date range used to control how
     *                      the sleep sessions are split up, and whether or not the data should be
     *                      inverted
     *
     * @return a new {@link IntervalsDataSet} instance
     */
    public IntervalsDataSet generateFromConfig(
            List<SleepSession> sleepSessions,
            List<WakeTimeGoal> wakeTimeGoals,
            IntervalsDataSet.Config config)
    {
        IntervalsDataSet dataSet = new IntervalsDataSet();
        dataSet.config = config;
        dataSet.sleepSessionDataSet = createDataSetFromSessions(sleepSessions, config);
        dataSet.interruptionsDataSet =
                createDataSetFromSessions(flatMapInterruptionsOf(sleepSessions), config);
        // TEST NEEDED [21-09-29 3:47PM] -- wakeTimeGoalsDataSet value.
        dataSet.wakeTimeGoalsDataSet = createDataSetFromWakeTimeGoals(wakeTimeGoals, config);
        
        return dataSet;
    }
    
//*********************************************************
// private methods
//*********************************************************
    
    /**
     * This assumes the wake-time goals are ordered by edit time.
     */
    private XYMultipleSeriesDataset createDataSetFromWakeTimeGoals(
            List<WakeTimeGoal> wakeTimeGoals,
            IntervalsDataSet.Config config)
    {
        if (wakeTimeGoals == null || wakeTimeGoals.isEmpty()) {
            return null;
        }
        
        // init the data points (key = 24hr window start, val = Y chart val of the goal)
        // ------------------------------------------------------------
        TreeMap<Long, Long> dataPoints = new TreeMap<>();
        
        long startMillis = config.dateRange.getStart().getTime();
        long endMillis = config.dateRange.getEnd().getTime();
        long currentMillis = startMillis;
        
        // This is for days which don't have any goal edits on them. In these cases, the current
        // goal from previous days should carry forward.
        final long NO_EDITS = -1L;
        // This is for edits where a goal is unset.
        final long NO_GOAL = -2L;
        
        while (currentMillis < endMillis) {
            dataPoints.put(currentMillis, NO_EDITS);
            currentMillis += TimeUtils.MILLIS_24_HOURS;
        }
        
        // add the goals to their related days
        // ------------------------------------------------------------
        for (WakeTimeGoal goal : wakeTimeGoals) {
            long rangeDay;
            if (goal.getEditTime().getTime() <= startMillis) {
                rangeDay = startMillis;
            } else {
                // assumes the wake-time goals are ordered by edit time. The last goal defined on a
                // rangeDay ends up being the one that is used.
                rangeDay = TimeUtils.startMillisOf(goal.getEditTime()) + config.offsetMillis;
            }
            
            long goalValue;
            if (goal.isSet()) {
                goalValue = goal.getGoalMillis() - config.offsetMillis;
                // If goalValue is larger than the 24hr window if will be hidden off the end of the
                // chart. The solution is to wrap it back around to the start by subtracting 24hrs.
                if (goalValue > TimeUtils.MILLIS_24_HOURS) {
                    goalValue -= TimeUtils.MILLIS_24_HOURS;
                }
            } else {
                goalValue = NO_GOAL;
            }
            dataPoints.put(rangeDay, goalValue);
        }
    
        // create the data set
        // ------------------------------------------------------------
        // adjacent list elems with like y values are merged into single lines
        // each line is a separate series, this is so that they are not connected in the chart
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        List<Long> goalValues = new ArrayList<>(dataPoints.values());
        // skip initial NO_EDIT and NO_GOAL values
        int x = 0; // x values == the indices of goalValues
        long y; // y values are in millis (the values in goalValues)
        for (; x < goalValues.size(); x++) {
            y = goalValues.get(x);
            if (!(y == NO_EDITS || y == NO_GOAL)) {
                break;
            }
        }
        if (x == goalValues.size()) {
            // no actual goal values are in this range
            return dataSet;
        }
        // process any actual goal values
        String seriesTitle = "Wake-Time Target";
        XYSeries series = new XYSeries(seriesTitle);
        int sign = config.invert ? -1 : 1;
        y = goalValues.get(x);
        addWakeTimePoint(series, x, y, sign);
        // ++x since we handle the first elem outside the loop to initialize y
        for (++x; x < goalValues.size(); x++) {
            long yNew = goalValues.get(x);
            
            if (yNew == NO_EDITS) {
                continue;
            }
            
            if (yNew == NO_GOAL) {
                if (y != NO_GOAL) {
                    addWakeTimePoint(series, x, y, sign);
                    dataSet.addSeries(series);
                    y = NO_GOAL;
                }
                continue;
            }
            
            if (y == NO_GOAL) {
                series = new XYSeries(seriesTitle);
                addWakeTimePoint(series, x, yNew, sign);
                y = yNew;
                continue;
            }
            
            if (yNew != y) {
                addWakeTimePoint(series, x, y, sign);
                dataSet.addSeries(series);
                
                series = new XYSeries(seriesTitle);
                addWakeTimePoint(series, x, yNew, sign);
                y = yNew;
            }
        }
        // TEST NEEDED [21-09-29 3:46PM] -- where the last goal in the range is NO_GOAL.
        // don't forget the last point
        if (y != NO_GOAL) {
            int xLast = goalValues.size();
            addWakeTimePoint(series, xLast, y, sign);
            dataSet.addSeries(series);
        }

        return dataSet;
    }
    
    private void addWakeTimePoint(XYSeries series, int x, long yMillis, int sign)
    {
        // SMELL [21-09-29 2:43PM] -- This is 0.5f is too much knowledge - it knows that
        //  IntervalsChartParamsFactory.initRendererProperties offsets the x axis by 0.5.
        series.add(x + 0.5f, sign * mTimeUtils.millisToHours(yMillis));
    }

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
