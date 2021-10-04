/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui_tests.sleep_tracker;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.StoppedSessionDataBuilder;
import com.rbraithwaite.sleeptarget.test_utils.ui.drivers.PostSleepTestDriver;

import org.junit.Test;
import org.junit.runner.RunWith;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aCurrentSession;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aMood;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aPostSleepData;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aStoppedSessionData;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.anInterruption;

@RunWith(AndroidJUnit4.class)
public class PostSleepFragmentTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void postSleepDisplaysCorrectValuesWhenDetailsAreUnset()
    {
        PostSleepTestDriver postSleep = PostSleepTestDriver.startingWith(
                aStoppedSessionData().with(aCurrentSession().withNoDetails()));
        
        postSleep.assertThat().commentsAreUnset();
        postSleep.assertThat().moodIsUnset();
        postSleep.assertThat().tagsAreUnset();
    }
    
    @Test
    public void postSleepOpensWithCorrectValues()
    {
        DateBuilder start = aDate().now().subtractDays(10);
        DateBuilder end = start.copy().addHours(1);
        
        StoppedSessionDataBuilder stoppedSessionData = aStoppedSessionData()
                .with(aPostSleepData().withRating(2f))
                .with(aCurrentSession()
                              .withStart(start)
                              .withMood(aMood().withIndex(2))
                              .withSelectedTagIds(1, 2)
                              .withAdditionalComments("some comments"))
                .with(TestUtils.timeUtilsFixedAt(end));
        
        PostSleepTestDriver postSleep = PostSleepTestDriver.startingWith(stoppedSessionData);
        
        postSleep.assertThat().valuesMatch(stoppedSessionData);
    }
    
    @Test
    public void postSleepDisplaysNoInterruptionsWhenNoInterruptions()
    {
        PostSleepTestDriver postSleep = PostSleepTestDriver.startingWith(
                aStoppedSessionData().with(aCurrentSession().withNoInterruptions()));
        
        postSleep.assertThat().interruptionsAreUnset();
    }
    
    @Test
    public void postSleepDisplaysInterruptions()
    {
        PostSleepTestDriver postSleep = PostSleepTestDriver.startingWith(
                aStoppedSessionData().with(aCurrentSession()
                                                   .withNoCurrentInterruption()
                                                   .withInterruptions(
                                                           anInterruption(),
                                                           anInterruption())));
        
        postSleep.assertThat().hasInterruptionCount(2);
    }
}
