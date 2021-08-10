package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.time.Day;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * A wake-time goal succeeds on a particular date when an eligible session is found whose end time
 * falls within the bounds of the leniency of the goal.
 * <p>
 * Eligible sleep sessions for a particular date are those whose start times fall between the goal
 * wake-time on this day and the goal wake-time for the next day. A sleep session succeeds if its
 * end time falls within the leniency bounds of the goal wake-time on the next day.
 */
public class WakeTimeGoalSuccess
{
//*********************************************************
// private properties
//*********************************************************

    private TimeUtils mTimeUtils;
    private SleepSessionRepository mSleepSessionRepository;
    
    private List<Date> mSucceededDates;

//*********************************************************
// private constants
//*********************************************************

    // 5 minutes in millis
    private static final int WAKETIME_GOAL_LENIENCY_MILLIS = 5 * 60 * 1000;

//*********************************************************
// constructors
//*********************************************************

    // SMELL [21-08-9 8:22PM] -- why am I doing it this way lmao
    public WakeTimeGoalSuccess(
            List<WakeTimeGoal> goalHistory,
            SleepSessionRepository sleepSessionRepository,
            TimeUtils timeUtils)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mTimeUtils = timeUtils;
        mSucceededDates = computeAllSucceededDates(goalHistory);
    }

//*********************************************************
// api
//*********************************************************

    public List<Date> getSucceededDates()
    {
        return mSucceededDates;
    }

//*********************************************************
// private methods
//*********************************************************

    private List<Date> computeAllSucceededDates(List<WakeTimeGoal> wakeTimeGoalHistory)
    {
        ArrayList<Date> result = new ArrayList<>();
        
        // the "goal range" is the range of dates where a particular goal is relevant
        GregorianCalendar goalRangeStart = new GregorianCalendar();
        GregorianCalendar goalRangeEnd = new GregorianCalendar();
        int historySize = wakeTimeGoalHistory.size();
        int lastElemIndex = historySize - 1;
        for (int i = 0; i < historySize; i++) {
            WakeTimeGoal currentGoal = wakeTimeGoalHistory.get(i);
            
            if (!currentGoal.isSet()) {
                continue;
            }
            
            boolean isNotLastElem = i != lastElemIndex;
            
            // Account for multiple goal edits being on the same day - in this case,
            // take the latest edit made on that day (skip past the others)
            if (isNotLastElem &&
                hasSameEditDay(currentGoal, wakeTimeGoalHistory.get(i + 1))) {
                continue;
            }
            
            goalRangeStart.setTime(currentGoal.getEditTime());
            // the dates returned need to have 0 time of day
            mTimeUtils.setCalendarTimeOfDay(goalRangeStart, 0);
            
            // If there is another wake-time goal, 'goalRangeStart' is only relevant up
            // to the day before that new goal is set.
            if (isNotLastElem) {
                goalRangeEnd.setTime(wakeTimeGoalHistory.get(i + 1).getEditTime());
            } else {
                // Otherwise 'goalRangeStart' is relevant up to now.
                // Note that today's goal cannot be checked yet, as it relies on comparing against
                // tomorrow's wake-time, which of course hasn't happened yet.
                goalRangeEnd.setTime(mTimeUtils.getNow());
            }
            
            result.addAll(computeSucceededDatesInGoalRange(
                    currentGoal, goalRangeStart, goalRangeEnd));
        }
        
        return result;
    }
    
    private List<Date> computeSucceededDatesInGoalRange(
            WakeTimeGoal goal,
            GregorianCalendar rangeStart,
            GregorianCalendar rangeEnd)
    {
        ArrayList<Date> result = new ArrayList<>();
        
        Day endDay = Day.of(rangeEnd);
        long goalTimeOfDay = goal.getGoalMillis();
        while (Day.of(rangeStart).isLessThan(endDay)) {
            if (dateHasSucceedingSleepSession(rangeStart, goalTimeOfDay)) {
                result.add(rangeStart.getTime());
            }
            incrementDayOf(rangeStart);
        }
        
        return result;
    }
    
    private boolean dateHasSucceedingSleepSession(GregorianCalendar date, long goalWakeTimeMillis)
    {
        GregorianCalendar bounds = TimeUtils.copyOf(date);
        // for the success of this day, you check against the wake-time of the next day
        incrementDayOf(bounds);
        mTimeUtils.setCalendarTimeOfDay(bounds, goalWakeTimeMillis);
        
        bounds.add(Calendar.MILLISECOND, -1 * WAKETIME_GOAL_LENIENCY_MILLIS);
        Date lowerBound = bounds.getTime();
        bounds.add(Calendar.MILLISECOND, 2 * WAKETIME_GOAL_LENIENCY_MILLIS);
        Date upperBound = bounds.getTime();
        
        return mSleepSessionRepository.getLatestSleepSessionEndingInRangeSynced(lowerBound,
                                                                                upperBound) != null;
    }
    
    private void incrementDayOf(Calendar cal)
    {
        cal.add(Calendar.DAY_OF_MONTH, 1);
    }
    
    private boolean hasSameEditDay(WakeTimeGoal a, WakeTimeGoal b)
    {
        return mTimeUtils.toDayInt(a.getEditTime()) ==
               mTimeUtils.toDayInt(b.getEditTime());
    }
}
