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

package com.rbraithwaite.sleeptarget.core.models;

import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;


/**
 * A sleep duration goal for some date succeeds when an eligible sleep session is found whose
 * duration (minus interruptions) falls within the bounds of the leniency of the goal. Eligible
 * sleep sessions for a particular date are those which:
 * <ul>
 *     <li>Start on that date and end on another (eg from 11pm to 7am the next day)</li>
 *     <li>Start and end on the next day after that date (eg from 1am to 9am the next morning)</li>
 * </ul>
 * <p>
 * Note that if there are multiple goals defined on one day, only the last goal defined is
 * considered.
 */
public class SleepDurationGoalSuccess
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

    private static final int GOAL_LENIENCY_MINUTES = 10;

//*********************************************************
// constructors
//*********************************************************

    // SMELL [21-08-9 8:22PM] -- why am I doing it this way lmao
    public SleepDurationGoalSuccess(
            List<SleepDurationGoal> goalHistory,
            TimeUtils timeUtils,
            SleepSessionRepository sleepSessionRepository)
    {
        mTimeUtils = timeUtils;
        mSleepSessionRepository = sleepSessionRepository;
        
        mSucceededDates = computeSucceededDates(goalHistory);
    }

//*********************************************************
// api
//*********************************************************

    // TEST NEEDED [21-08-9 11:08PM]
    public List<Date> getSucceededDates()
    {
        return mSucceededDates;
    }

//*********************************************************
// private methods
//*********************************************************

    private List<Date> computeSucceededDates(List<SleepDurationGoal> goalHistory)
    {
        ArrayList<Date> result = new ArrayList<>();
        
        GregorianCalendar goalCal = new GregorianCalendar();
        GregorianCalendar end = new GregorianCalendar();
        for (int i = 0; i < goalHistory.size(); i++) {
            SleepDurationGoal currentGoal = goalHistory.get(i);
            if (!currentGoal.isSet()) {
                continue;
            }
            
            // REFACTOR [21-08-7 3:42PM] -- I should iterate backwards so I don't need to deal
            //  with this.
            boolean notLastElem = i != goalHistory.size() - 1;
            
            // Account for multiple goal edits being on the same day - in this case,
            // take the latest edit made on that day (skip past the others)
            if (notLastElem &&
                (mTimeUtils.toDayInt(currentGoal.getEditTime()) ==
                 mTimeUtils.toDayInt(goalHistory.get(i + 1).getEditTime()))) {
                continue;
            }
            
            goalCal.setTime(currentGoal.getEditTime());
            // If there is another wake-time goal, 'currentGoal' is only relevant up
            // to the day that new goal is set.
            if (notLastElem) {
                SleepDurationGoal next = goalHistory.get(i + 1);
                end.setTime(next.getEditTime());
            } else {
                end.setTime(mTimeUtils.getNow());
                end.add(Calendar.DAY_OF_YEAR, 1); // otherwise today is not included
            }
            
            // set up the range of sleep sessions to filter by
            mTimeUtils.setCalendarTimeOfDay(goalCal, 0);
            
            // check each date which uses this goal
            int absoluteGoalEndDay = mTimeUtils.toDayInt(end.getTime());
            while (mTimeUtils.toDayInt(goalCal.getTime()) < absoluteGoalEndDay) {
                if (dateSucceeded(goalCal, currentGoal)) {
                    result.add(goalCal.getTime());
                }
                goalCal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        
        return result;
    }
    
    private boolean dateSucceeded(GregorianCalendar date, SleepDurationGoal goal)
    {
        GregorianCalendar sessionRangeEnd = getSleepSessionRangeEnd(date);
        
        List<SleepSession> possibleSessions =
                // SMELL [21-03-26 1:25AM] -- Domain entities should not be depending on
                //  repositories.
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
        
        List<SleepSession> filteredPossibleSessions = filterPossibleSessions(
                possibleSessions, date.getTime(), date.get(Calendar.DAY_OF_YEAR));
        
        for (SleepSession session : filteredPossibleSessions) {
            if (sessionHitsTarget(session, goal)) {
                return true;
            }
        }
        return false;
    }
    
    // REFACTOR [21-09-2 10:06PM] -- This should belong to SleepDurationGoal or SleepSession.
    private boolean sessionHitsTarget(SleepSession sleepSession, SleepDurationGoal target) {
        int durationMinutes = (int) ((sleepSession.getNetDurationMillis() / 60) / 1000);
        return Math.abs(durationMinutes - target.inMinutes()) < GOAL_LENIENCY_MINUTES;
    }
    
    private GregorianCalendar getSleepSessionRangeEnd(GregorianCalendar start)
    {
        GregorianCalendar rangeEnd = new GregorianCalendar();
        rangeEnd.setTime(start.getTime());
        rangeEnd.add(Calendar.DAY_OF_MONTH, 2);
        return rangeEnd;
    }
    
    private List<SleepSession> filterPossibleSessions(
            List<SleepSession> possibleSessions,
            Date goalDate,
            int goalDayOfYear)
    {
        // Filters possible sleep session which:
        //      - start on the day of the goal and end on a different day
        //          - for example, a sleep session from 11pm to 7am the next day
        //      - start & end on the next day after the goal
        //          - for example, a sleep session from 1am -> 9am should count towards the
        //          previous day's goal.
        
        List<SleepSession> filteredPossibleSessions = new ArrayList<>();
        GregorianCalendar filterCal = new GregorianCalendar();
        for (SleepSession session : possibleSessions) {
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
