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

package com.rbraithwaite.sleeptarget.data.database.convert;

import androidx.room.TypeConverter;

import java.util.Date;
import java.util.GregorianCalendar;

public class ConvertDate
{
//*********************************************************
// api
//*********************************************************

    @TypeConverter
    public static Date fromMillis(Long millis)
    {
        if (millis == null) {
            return null;
        }
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(millis);
        
        return calendar.getTime();
    }
    
    @TypeConverter
    public static Long toMillis(Date date)
    {
        if (date == null) {
            return null;
        }
        return date.getTime();
    }
}
