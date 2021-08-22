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

package com.rbraithwaite.sleepapp.ui.stats.chart_durations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DurationsChartFormatting
{
//*********************************************************
// api
//*********************************************************

    public static String formatDataLabel(Date start)
    {
        // REFACTOR [21-05-17 2:56PM] -- hardcoded locale.
        SimpleDateFormat labelFormat = new SimpleDateFormat("M/dd", Locale.CANADA);
        return labelFormat.format(start);
    }
}
