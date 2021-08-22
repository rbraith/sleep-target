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

package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.rbraithwaite.sleepapp.test_utils.TestUtils.ArbitraryData.getCalendar;
import static com.rbraithwaite.sleepapp.test_utils.TestUtils.ArbitraryData.getDurationMillis;

public class SleepSessionEntityBuilder
        implements BuilderOf<SleepSessionEntity>
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public SleepSessionEntity build()
    {
        SleepSessionEntity sleepSessionEntity = new SleepSessionEntity();
        
        GregorianCalendar cal = getCalendar();
        sleepSessionEntity.startTime = cal.getTime();
        sleepSessionEntity.duration = getDurationMillis();
        
        cal.add(Calendar.MILLISECOND, (int) sleepSessionEntity.duration);
        sleepSessionEntity.endTime = cal.getTime();
        
        sleepSessionEntity.additionalComments = "lol!";
        
        sleepSessionEntity.rating = 2.5f;
        
        return sleepSessionEntity;
    }
}
