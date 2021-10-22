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
package com.rbraithwaite.sleeptarget.test_utils.test_data;

import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.CalendarBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.CurrentSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.InterruptionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.MoodBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.PostSleepDataBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepDurationGoalBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepInterruptionEntityBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.SleepSessionEntityBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.StoppedSessionDataBuilder;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.TagBuilder;
import com.rbraithwaite.sleeptarget.utils.interfaces.BuilderOf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TestData
{
//*********************************************************
// constructors
//*********************************************************

    private TestData() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static CurrentSessionBuilder aCurrentSession()
    {
        return new CurrentSessionBuilder();
    }
    
    public static DateBuilder aDate()
    {
        return new DateBuilder();
    }
    
    public static MoodBuilder aMood()
    {
        return new MoodBuilder();
    }
    
    public static InterruptionBuilder anInterruption()
    {
        return new InterruptionBuilder();
    }
    
    public static PostSleepDataBuilder aPostSleepData()
    {
        return new PostSleepDataBuilder();
    }
    
    public static StoppedSessionDataBuilder aStoppedSessionData()
    {
        return new StoppedSessionDataBuilder();
    }
    
    public static SleepSessionBuilder aSleepSession()
    {
        return new SleepSessionBuilder();
    }
    
    public static TagBuilder aTag()
    {
        return new TagBuilder();
    }
    
    public static SleepSessionEntityBuilder aSleepSessionEntity()
    {
        return new SleepSessionEntityBuilder();
    }
    
    public static SleepInterruptionEntityBuilder aSleepInterruptionEntity()
    {
        return new SleepInterruptionEntityBuilder();
    }
    
    public static SessionBuilder aSession()
    {
        return new SessionBuilder();
    }
    
    public static CalendarBuilder aCalendar()
    {
        return new CalendarBuilder();
    }
    
    public static WakeTimeGoalBuilder aWakeTimeGoal()
    {
        return new WakeTimeGoalBuilder();
    }
    
    public static SleepDurationGoalBuilder aSleepDurationGoal()
    {
        return new SleepDurationGoalBuilder();
    }
    
    public static <T> List<T> aListOf(BuilderOf<T>... builders)
    {
        return new ArrayList<>(Arrays.asList(builders)).stream()
                .map(BuilderOf::build)
                .collect(Collectors.toList());
    }
    
    /**
     * Some better syntax for getting the actual value of a test data builder (the syntax flows
     * better in the tests imo)
     */
    public static <T> T valueOf(BuilderOf<T> builder)
    {
        return builder.build();
    }
}
