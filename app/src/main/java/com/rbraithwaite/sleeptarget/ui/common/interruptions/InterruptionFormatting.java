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
package com.rbraithwaite.sleeptarget.ui.common.interruptions;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.ui.Constants;
import com.rbraithwaite.sleeptarget.ui.common.CommonFormatting;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InterruptionFormatting
{
//*********************************************************
// constructors
//*********************************************************

    private InterruptionFormatting() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static String formatListItemStart(Date start)
    {
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("h:mm a, MMM d", Constants.STANDARD_LOCALE);
        return dateFormat.format(start);
    }
    
    public static String formatInterruptionsCount(List<Interruption> interruptions)
    {
        if (interruptions == null) {
            return "0";
        }
        return String.valueOf(interruptions.size());
    }
    
    public static String formatDuration(long durationMillis)
    {
        return CommonFormatting.formatDurationMillis(durationMillis);
    }
    
    public static String formatListItemReason(String reason)
    {
        return reason == null || reason.isEmpty() ? "- - -" : reason;
    }
    
    public static String formatOutOfBoundsDuration(long durationMillis)
    {
        return formatDuration(durationMillis);
    }
}
