package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

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
    private static final int WAKETIME_GOAL_LENIENCY = 5 * 60 * 1000;

//*********************************************************
// constructors
//*********************************************************

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
        
        GregorianCalendar goalCal = new GregorianCalendar();
        GregorianCalendar end = new GregorianCalendar();
        for (int i = 0; i < wakeTimeGoalHistory.size(); i++) {
            WakeTimeGoal current = wakeTimeGoalHistory.get(i);
            if (!current.isSet()) {
                continue;
            }
            
            boolean notLastElem = i != wakeTimeGoalHistory.size() - 1;
            
            // Account for multiple goal edits being on the same day - in this case,
            // take the latest edit made on that day (skip past the others)
            if (notLastElem &&
                (mTimeUtils.asAbsoluteDay(current.getEditTime()) ==
                 mTimeUtils.asAbsoluteDay(wakeTimeGoalHistory.get(i + 1).getEditTime()))) {
                continue;
            }
            
            goalCal.setTime(current.getEditTime());
            
            // If there is another wake-time goal, 'current' is only relevant up
            // to the day that new goal is set.
            if (notLastElem) {
                WakeTimeGoal next = wakeTimeGoalHistory.get(i + 1);
                end.setTime(next.getEditTime());
            } else {
                end.setTime(mTimeUtils.getNow());
                end.add(Calendar.DAY_OF_YEAR, 1); // since the while-loop below uses <
            }
            
            // check each date which uses this goal
            int absoluteEndDay = mTimeUtils.asAbsoluteDay(end.getTime());
            while (mTimeUtils.asAbsoluteDay(goalCal.getTime()) < absoluteEndDay) {
                // set the goal calendar time of day to the goal wake-time
                mTimeUtils.setCalendarTimeOfDay(goalCal, current.getGoalMillis());
                
                // SMELL [21-03-26 1:26AM] -- Domain entities should not be depending on
                //  repositories. Turn this into a callback or else find a different way of
                //  calculating succeeded wake-time goal dates.
                SleepSession sleepSession =
                        mSleepSessionRepository.getFirstSleepSessionStartingBefore(goalCal.getTimeInMillis());
                
                if (sleepSession != null && wakeTimeGoalWasMet(sleepSession, goalCal.getTime())) {
                    // the dates need to have 0 time of day
                    mTimeUtils.setCalendarTimeOfDay(goalCal, 0);
                    result.add(goalCal.getTime());
                }
                
                goalCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        
        return result;
    }
    
    /**
     * @param sleepSession The sleep session to check for goal success.
     * @param wakeTimeGoal The wake-time goal for a particular day. (The date's time of day being
     *                     set to the wake-time goal)
     *
     * @return Whether or not the goal was met.
     */
    private boolean wakeTimeGoalWasMet(SleepSession sleepSession, Date wakeTimeGoal)
    {
        // Absolute difference, since the actual wake-time might be under or over the goal
        long diffMillis = Math.abs(sleepSession.getEnd().getTime() - wakeTimeGoal.getTime());
        return diffMillis <= WAKETIME_GOAL_LENIENCY;
    }
}
