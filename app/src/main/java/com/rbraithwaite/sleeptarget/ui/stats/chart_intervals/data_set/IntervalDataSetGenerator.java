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
import java.util.GregorianCalendar;
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
    
    private static class WakeTimeLine
    {
        double startX;
        double lengthX;
        double hoursY;
        
        XYSeries toSeries()
        {
            XYSeries series = new XYSeries("Target");
            series.add(startX, hoursY);
            series.add(startX + lengthX, hoursY);
            return series;
        }
    }
    
    private static class WakeTimeLineFactory
    {
        private int sign;
        private long offsetMillis;
        private TimeUtils mTimeUtils;
    
        public WakeTimeLineFactory(
                int sign,
                long offsetMillis,
                TimeUtils timeUtils)
        {
            this.sign = sign;
            this.offsetMillis = offsetMillis;
            mTimeUtils = timeUtils;
        }
    
        WakeTimeLine create(double chartX, WakeTimeGoal goal)
        {
            WakeTimeLine line = new WakeTimeLine();
            line.startX = chartX;
            line.lengthX = 1;
            line.hoursY = toHoursY(goal.getGoalMillis());
            if (Math.abs(line.hoursY) > 24) {
                // This is for cases where hoursY would extend off the end of the chart
                // shift the line over to the next chart X position
                line.hoursY = sign * (Math.abs(line.hoursY) - 24);
                line.startX++;
            }
            return line;
        }
    
        private double toHoursY(int millis)
        {
            return sign * mTimeUtils.millisToHours(millis - offsetMillis);
        }
    }
    
    /**
     * This assumes the wake-time goals are ordered by edit time.
     */
    private XYMultipleSeriesDataset createDataSetFromWakeTimeGoals(
            List<WakeTimeGoal> wakeTimeGoals,
            IntervalsDataSet.Config config)
    {
        XYMultipleSeriesDataset dataSet = new XYMultipleSeriesDataset();
        
        if (wakeTimeGoals == null || wakeTimeGoals.isEmpty()) {
            return dataSet;
        }

        List<WakeTimeLine> wakeTimeLines = new ArrayList<>();
        
        DateRange goalRange = getGoalRangeFrom(config.dateRange);
        TreeMap<Long, WakeTimeGoal> relevantGoalsForDays = getRelevantGoalsForRange(wakeTimeGoals, goalRange);
        
        long chartStartMillis = config.dateRange.getStart().getTime();
        long chartEndMillis = config.dateRange.getEnd().getTime();
        
        int sign = config.invert ? -1 : 1;
        
        WakeTimeLine currentLine = null;
        WakeTimeLineFactory lineFactory = new WakeTimeLineFactory(
                sign,
                config.offsetMillis,
                mTimeUtils);
    
        // Compensating for the data being offset by 0.5 in
        // IntervalsChartParamsFactory.initRendererProperties().
        double chartX = 0.5;
        
        List<Map.Entry<Long, WakeTimeGoal>> entriesList = new ArrayList<>(relevantGoalsForDays.entrySet());
        
        for (int i = 0; i < entriesList.size(); i++) {
            long dayStartMillis = entriesList.get(i).getKey();
            WakeTimeGoal goal = entriesList.get(i).getValue();
            
            // REFACTOR [21-10-1 8:20PM] -- extract this check of the first element out of the loop.
            if (dayStartMillis < chartStartMillis) {
                // handle first partial day, before first midnight in the chart
                long chartStartTimeOfDay = mTimeUtils.getTimeOfDayOf(config.dateRange.getStart());
                if (!(goal == null || goal.isUnset()) && goal.getGoalMillis() > chartStartTimeOfDay) {
                    // have the factory start at -0.5 instead, so the line for the partial day
                    // displays properly.
                    currentLine = lineFactory.create(-0.5, goal);
                }
                continue;
            }

            // handle fully displayed days & last partial day
            if (goal == null || goal.isUnset()) {
                if (currentLine != null) {
                    // transition out of line
                    wakeTimeLines.add(currentLine);
                    currentLine = null;
                }
            }
            else if (currentLine == null) {
                // transition into line
                currentLine = lineFactory.create(chartX, goal);
            }
            else if (lineFactory.create(chartX, goal).hoursY != currentLine.hoursY) {
                // transition to new line
                wakeTimeLines.add(currentLine);
                currentLine = lineFactory.create(chartX, goal);
            } else {
                // goal has same y, increment the line length
                currentLine.lengthX++;
            }
    
            chartX++;
        }
        if (currentLine != null) {
            wakeTimeLines.add(currentLine);
        }
        
        for (WakeTimeLine line : wakeTimeLines) {
            dataSet.addSeries(line.toSeries());
        }
        
        return dataSet;
    }
    
    // REFACTOR [21-09-30 9:24PM] -- this duplicates IntervalsChartViewModel.getRelevantGoalsFor()
    private DateRange getGoalRangeFrom(DateRange configDateRange)
    {
        // Extend the range to the start & end times of the start & end days of the range
        GregorianCalendar goalRangeStart = TimeUtils.getCalendarFrom(configDateRange.getStart());
        GregorianCalendar goalRangeEnd = TimeUtils.getCalendarFrom(configDateRange.getEnd());
        mTimeUtils.setCalendarTimeOfDay(goalRangeStart, 0);
        mTimeUtils.setCalendarTimeOfDay(goalRangeEnd, TimeUtils.MILLIS_24_HOURS);
        return new DateRange(goalRangeStart.getTime(), goalRangeEnd.getTime());
    }
    
    /**
     * This determines which goal is relevant for each day in a range. The relevant goal will be
     * the latest goal defined on or before that day. If no goal is defined for the day null is used.
     */
    private TreeMap<Long, WakeTimeGoal> getRelevantGoalsForRange(List<WakeTimeGoal> goals, DateRange range)
    {
        // set up day keys
        TreeMap<Long, WakeTimeGoal> days = new TreeMap<>();
        long startMillis = range.getStart().getTime();
        long endMillis = range.getEnd().getTime();
        long currentMillis = startMillis;
        while (currentMillis < endMillis) {
            WakeTimeGoal goal = getLatestGoalBeforeTime(currentMillis + TimeUtils.MILLIS_24_HOURS, goals);
            days.put(currentMillis, goal);
            currentMillis += TimeUtils.MILLIS_24_HOURS;
        }
        return days;
    }
    
    private WakeTimeGoal getLatestGoalBeforeTime(long timeMillis, List<WakeTimeGoal> goals)
    {
        WakeTimeGoal result = null;
        for (WakeTimeGoal goal : goals) {
            if (goal.getEditTime().getTime() > timeMillis) {
                continue;
            }
            if (result == null) {
                result = goal;
                continue;
            }
            if (result.getEditTime().getTime() < goal.getEditTime().getTime()) {
                result = goal;
            }
        }
        return result;
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
