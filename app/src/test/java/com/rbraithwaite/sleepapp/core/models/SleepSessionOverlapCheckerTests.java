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

import com.rbraithwaite.sleepapp.core.models.overlap_checker.SleepSessionOverlapChecker;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.test_data.builders.SleepSessionBuilder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSession;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SleepSessionOverlapCheckerTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepSessionRepository mockSleepSessionRepository;
    SleepSessionOverlapChecker overlapChecker;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        overlapChecker = new SleepSessionOverlapChecker(mockSleepSessionRepository);
    }
    
    @After
    public void teardown()
    {
        overlapChecker = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void checkForOverlap_returnsNullWhenThereIsNoOverlap()
    {
        SleepSessionBuilder sleepSession = aSleepSession()
                .withStart(aDate().withValue(2021, 7, 26, 12, 0))
                .withDurationHours(1);
        
        SleepSession nonOverlappingSession = sleepSession.build();
        
        SleepSession before = sleepSession.offsetStartByHours(-2).build();
        SleepSession after = sleepSession.offsetStartByHours(4).build();
        
        when(mockSleepSessionRepository.getFirstSleepSessionStartingBefore(nonOverlappingSession.getStart()
                                                                                   .getTime()))
                .thenReturn(before);
        when(mockSleepSessionRepository.getFirstSleepSessionStartingAfter(nonOverlappingSession.getStart()
                                                                                  .getTime()))
                .thenReturn(after);
        
        assertThat(overlapChecker.checkForOverlapExclusive(nonOverlappingSession), is(nullValue()));
    }
    
    @Test
    public void checkForOverlap_findsOverlapsBehind()
    {
        SleepSessionBuilder sleepSession = aSleepSession()
                .withStart(aDate().withValue(2021, 7, 26, 12, 0))
                .withDurationHours(3);
        
        SleepSession overlappingSession = sleepSession.build();
        
        SleepSession before = sleepSession
                .withId(overlappingSession.getId() + 1)
                .offsetStartByHours(-1)
                .withDurationHours(2)
                .build();
        
        when(mockSleepSessionRepository.getFirstSleepSessionStartingBefore(overlappingSession.getStart()
                                                                                   .getTime()))
                .thenReturn(before);
        
        assertThat(overlapChecker.checkForOverlapExclusive(overlappingSession),
                   is(equalTo(before)));
    }
    
    @Test
    public void checkForOverlap_findsOverlapAhead()
    {
        SleepSessionBuilder sleepSession = aSleepSession()
                .withStart(aDate().withValue(2021, 7, 26, 12, 0))
                .withDurationHours(3);
        
        SleepSession overlappingSession = sleepSession.build();
        
        SleepSession ahead = sleepSession
                .withId(overlappingSession.getId() + 1)
                .offsetStartByHours(1)
                .build();
        
        when(mockSleepSessionRepository.getFirstSleepSessionStartingAfter(overlappingSession.getStart()
                                                                                  .getTime()))
                .thenReturn(ahead);
        
        assertThat(overlapChecker.checkForOverlapExclusive(overlappingSession), is(equalTo(ahead)));
    }
    
    @Test
    public void checkForOverlap_ignoresSameId()
    {
        SleepSessionBuilder sleepSession = aSleepSession()
                .withStart(aDate().withValue(2021, 7, 26, 12, 0))
                .withDurationHours(3);
        
        SleepSession overlappingSession = sleepSession.build();
        
        SleepSession sameId = sleepSession.offsetStartByHours(-1).build();
        
        when(mockSleepSessionRepository.getFirstSleepSessionStartingBefore(overlappingSession.getStart()
                                                                                   .getTime()))
                .thenReturn(sameId);
        
        assertThat(overlapChecker.checkForOverlapExclusive(overlappingSession), is(nullValue()));
    }
}
