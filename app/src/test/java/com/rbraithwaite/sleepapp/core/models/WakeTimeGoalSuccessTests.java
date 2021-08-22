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

package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Date;
import java.util.List;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSession;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aWakeTimeGoal;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.valueOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WakeTimeGoalSuccessTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepSessionRepository mockSleepSessionRepository;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
    }
    
    @After
    public void teardown()
    {
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void getSucceededDates_returnsCorrectDates()
    {
        DateBuilder date = aDate();
        
        Date now = valueOf(date);
        
        DateBuilder sixDaysAgo = date.copy().subtractDays(6);
        DateBuilder threeDaysAgo = date.copy().subtractDays(3);
        
        List<WakeTimeGoal> goalHistory = aListOf(
                aWakeTimeGoal()
                        .withEditTime(sixDaysAgo)
                        .withGoal(8, 0),
                aWakeTimeGoal()
                        .withEditTime(threeDaysAgo)
                        .withGoal(9, 0));
        
        date = sixDaysAgo.atMidnight();
        DateBuilder session1Start = date.addDays(1).copy();
        // subtract since the goal will succeed for the previous day
        DateBuilder expectedSuccess1 = session1Start.copy().subtractDays(1);
        DateBuilder session2Start = date.addDays(2).copy(); // this one should fail
        DateBuilder session3Start = date.addDays(3).copy();
        DateBuilder expectedSuccess2 = session3Start.copy().subtractDays(1);
        
        List<SleepSession> sleepSessions = aListOf(
                // succeed first goal (+4 to test leniency)
                aSleepSession()
                        .withStart(session1Start)
                        .withDurationMinutes(8 * 60 + 4),
                // fail first goal
                aSleepSession()
                        .withStart(session2Start)
                        .withDurationMinutes(8 * 60 - 15),
                // succeed second goal (-4 for leniency check)
                aSleepSession()
                        .withStart(session3Start)
                        .withDurationMinutes((9 * 60 - 4)));
        
        setupRepoWith_getLatestSleepSessionEndingInRangeSynced(sleepSessions);
        TimeUtils fakeNow = createFakeNow(now);
        
        WakeTimeGoalSuccess wakeTimeGoalSuccess = new WakeTimeGoalSuccess(
                goalHistory,
                mockSleepSessionRepository,
                fakeNow);
        
        List<Date> succeededDates = wakeTimeGoalSuccess.getSucceededDates();
        assertThat(succeededDates.size(), is(2));
        assertThat(succeededDates.get(0), is(equalTo(valueOf(expectedSuccess1))));
        assertThat(succeededDates.get(1), is(equalTo(valueOf(expectedSuccess2))));
    }
    
    @Test
    public void whenMultipleGoalsWereSetOnTheSameDay_thenTheLatestIsUsed()
    {
        DateBuilder date = aDate().withValue(2021, 6, 5, 4, 3);
        
        // 2 goals on the same day
        List<WakeTimeGoal> goalHistory = aListOf(
                aWakeTimeGoal()
                        .withEditTime(date)
                        .withGoal(5, 0),
                aWakeTimeGoal()
                        .withEditTime(date.addHours(1))
                        .withGoal(8, 0));
        
        setupRepoWith_getLatestSleepSessionEndingInRangeSynced(aListOf(
                aSleepSession()
                        .withStart(date.addDays(1).atMidnight())
                        .withDurationHours(8)));
        TimeUtils fakeNow = createFakeNow(valueOf(date.addHours(9)));
        
        WakeTimeGoalSuccess wakeTimeGoalSuccess = new WakeTimeGoalSuccess(
                goalHistory,
                mockSleepSessionRepository,
                fakeNow);
        
        List<Date> succeededDates = wakeTimeGoalSuccess.getSucceededDates();
        
        assertThat(succeededDates.size(), is(1));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void setupRepoWith_getLatestSleepSessionEndingInRangeSynced(List<SleepSession> sleepSessions)
    {
        GetLatestSleepSessionEndingInRange getLatestSleepSessionEndingInRange =
                new GetLatestSleepSessionEndingInRange(sleepSessions);
        when(mockSleepSessionRepository.getLatestSleepSessionEndingInRangeSynced(Matchers.any(Date.class),
                                                                                 Matchers.any(Date.class)))
                .thenAnswer(invocation -> getLatestSleepSessionEndingInRange.invoke(
                        (Date) invocation.getArguments()[0],
                        (Date) invocation.getArguments()[1]));
    }
    
    // REFACTOR [21-08-9 8:58PM] -- extract this.
    private TimeUtils createFakeNow(Date now)
    {
        return new TimeUtils()
        {
            @Override
            public Date getNow()
            {
                return now;
            }
        };
    }
    
//*********************************************************
// private helpers
//*********************************************************

    private static class GetLatestSleepSessionEndingInRange
    {
        List<SleepSession> mSleepSessions;
        
        public GetLatestSleepSessionEndingInRange(List<SleepSession> sleepSessions)
        {
            mSleepSessions = sleepSessions;
        }
        
        public SleepSession invoke(Date rangeStart, Date rangeEnd)
        {
            return mSleepSessions.stream()
                    .filter(sleepSession -> {
                        Date end = sleepSession.getEnd();
                        return rangeStart.getTime() <= end.getTime() &&
                               end.getTime() <= rangeEnd.getTime();
                    })
                    .max((o1, o2) -> (int) (o1.getStart().getTime() - o2.getStart().getTime()))
                    .orElse(null);
        }
    }
}
