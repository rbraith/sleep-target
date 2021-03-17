package com.rbraithwaite.sleepapp.ui.sleep_goals.goal_success;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SleepDurationGoalSuccess
{
//*********************************************************
// private properties
//*********************************************************

    private TimeUtils mTimeUtils;
    private SleepSessionRepository mSleepSessionRepository;
    private List<SleepDurationGoalModel> mGoalHistory;
    
    private List<Date> mSucceededDates;

//*********************************************************
// private constants
//*********************************************************

    private static final int GOAL_LENIENCY_MINUTES = 10;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepDurationGoalSuccess(
            List<SleepDurationGoalModel> goalHistory,
            TimeUtils timeUtils,
            SleepSessionRepository sleepSessionRepository)
    {
        mTimeUtils = timeUtils;
        mSleepSessionRepository = sleepSessionRepository;
        mGoalHistory = goalHistory;
        
        mSucceededDates = computeSucceededDates(goalHistory);
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

    private List<Date> computeSucceededDates(List<SleepDurationGoalModel> goalHistory)
    {
        ArrayList<Date> result = new ArrayList<>();
        
        GregorianCalendar goalCal = new GregorianCalendar();
        GregorianCalendar end = new GregorianCalendar();
        for (int i = 0; i < goalHistory.size(); i++) {
            SleepDurationGoalModel currentGoal = goalHistory.get(i);
            if (!currentGoal.isSet()) {
                continue;
            }
            
            boolean notLastElem = i != goalHistory.size() - 1;
            
            // Account for multiple goal edits being on the same day - in this case,
            // take the latest edit made on that day (skip past the others)
            if (notLastElem &&
                (mTimeUtils.asAbsoluteDay(currentGoal.getEditTime()) ==
                 mTimeUtils.asAbsoluteDay(goalHistory.get(i + 1).getEditTime()))) {
                continue;
            }
            
            goalCal.setTime(currentGoal.getEditTime());
            // If there is another wake-time goal, 'currentGoal' is only relevant up
            // to the day that new goal is set.
            if (notLastElem) {
                SleepDurationGoalModel next = goalHistory.get(i + 1);
                end.setTime(next.getEditTime());
            } else {
                end.setTime(mTimeUtils.getNow());
                end.add(Calendar.DAY_OF_YEAR, 1); // otherwise today is not included
            }
            
            // set up the range of sleep sessions to filter by
            mTimeUtils.setCalendarTimeOfDay(goalCal, 0);
            
            // check each date which uses this goal
            int absoluteGoalEndDay = mTimeUtils.asAbsoluteDay(end.getTime());
            while (mTimeUtils.asAbsoluteDay(goalCal.getTime()) < absoluteGoalEndDay) {
                if (dateSucceeded(goalCal, currentGoal)) {
                    result.add(goalCal.getTime());
                }
                goalCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        
        return result;
    }
    
    private boolean dateSucceeded(GregorianCalendar date, SleepDurationGoalModel goal)
    {
        GregorianCalendar sessionRangeEnd = getSleepSessionRangeEnd(date);
        
        List<SleepSessionModel> possibleSessions =
                mSleepSessionRepository.getSleepSessionsInRangeSynced(
                        date.getTime(),
                        sessionRangeEnd.getTime());
        
        // Determine whether the sleep duration goal was met by looking at a possible set of
        // sleep sessions and comparing the longest uninterrupted duration session from
        // that set.
        // The possible sleep session are those which:
        //      - start on the day of the goal and end on a different day
        //          - for example, a sleep session from 11pm to 7am the next day
        //      - start & end on the next day after the goal
        //          - for example, a sleep session from 1am -> 9am should count towards the
        //          previous day's goal.
        
        List<SleepSessionModel> filteredPossibleSessions = filterPossibleSessions(
                possibleSessions, date.getTime(), date.get(Calendar.DAY_OF_YEAR));
        
        if (!filteredPossibleSessions.isEmpty()) {
            int longestDurationMinutes = findLongestDurationMinutes(filteredPossibleSessions);
            return Math.abs(longestDurationMinutes - goal.inMinutes()) < GOAL_LENIENCY_MINUTES;
        }
        
        return false;
    }
    
    private int findLongestDurationMinutes(List<SleepSessionModel> sessions)
    {
        long longestDurationMillis = sessions.get(0).getDuration();
        for (SleepSessionModel session : sessions) {
            longestDurationMillis = Math.max(session.getDuration(), longestDurationMillis);
        }
        
        return (int) (longestDurationMillis / 60) / 1000; // convert from millis to minutes
    }
    
    private GregorianCalendar getSleepSessionRangeEnd(GregorianCalendar start)
    {
        GregorianCalendar rangeEnd = new GregorianCalendar();
        rangeEnd.setTime(start.getTime());
        rangeEnd.add(Calendar.DAY_OF_MONTH, 2);
        return rangeEnd;
    }
    
    private List<SleepSessionModel> filterPossibleSessions(
            List<SleepSessionModel> possibleSessions,
            Date goalDate,
            int goalDayOfYear)
    {
        // Filters possible sleep session which:
        //      - start on the day of the goal and end on a different day
        //          - for example, a sleep session from 11pm to 7am the next day
        //      - start & end on the next day after the goal
        //          - for example, a sleep session from 1am -> 9am should count towards the
        //          previous day's goal.
        
        List<SleepSessionModel> filteredPossibleSessions = new ArrayList<>();
        GregorianCalendar filterCal = new GregorianCalendar();
        for (SleepSessionModel session : possibleSessions) {
            filterCal.setTime(session.getStart());
            int startDay = filterCal.get(Calendar.DAY_OF_YEAR);
            filterCal.setTime(session.getEnd());
            int endDay = filterCal.get(Calendar.DAY_OF_YEAR);
            // I need to do it this way since the year might roll over
            filterCal.setTime(goalDate);
            filterCal.add(Calendar.DAY_OF_YEAR, 1);
            int goalDayPlusOne = filterCal.get(Calendar.DAY_OF_YEAR);
            
            if ((goalDayOfYear == startDay && goalDayOfYear != endDay) ||
                (goalDayPlusOne == startDay && goalDayPlusOne == endDay)) {
                filteredPossibleSessions.add(session);
            }
        }
        return filteredPossibleSessions;
    }
}
