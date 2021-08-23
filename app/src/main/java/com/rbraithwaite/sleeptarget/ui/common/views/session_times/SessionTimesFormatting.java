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

package com.rbraithwaite.sleeptarget.ui.common.views.session_times;

import com.rbraithwaite.sleeptarget.ui.common.CommonFormatting;

public class SessionTimesFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private SessionTimesFormatting() {/* No instantiation */}
    
    
//*********************************************************
// api
//*********************************************************

    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatTimeOfDay(int hourOfDay, int minute)
    {
        return CommonFormatting.formatTimeOfDay(hourOfDay, minute);
    }
    
    public static String formatDate(int year, int month, int dayOfMonth)
    {
        return CommonFormatting.formatDate(year, month, dayOfMonth);
    }
}
