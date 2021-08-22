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

import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;
import java.util.GregorianCalendar;

public class CalendarBuilder
        implements BuilderOf<GregorianCalendar>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mDate;
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public GregorianCalendar build()
    {
        GregorianCalendar cal = new GregorianCalendar();
        if (mDate != null) {
            cal.setTime(mDate);
        }
        return cal;
    }
    
//*********************************************************
// api
//*********************************************************

    public CalendarBuilder with(DateBuilder date)
    {
        mDate = date.build();
        return this;
    }
}
