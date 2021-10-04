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

package com.rbraithwaite.sleeptarget.ui;

import java.util.Locale;

public class Constants
{
//*********************************************************
// public constants
//*********************************************************

    // e.g. 1:23 AM
    public static final String STANDARD_FORMAT_TIME_OF_DAY = "h:mm a";
    
    // e.g. Jan 17 2021
    public static final String STANDARD_FORMAT_DATE = "MMM d yyyy";
    
    // e.g. 1:23 AM, Jan 17 2021
    public static final String STANDARD_FORMAT_FULL_DATE = "h:mm a, MMM d yyyy";
    
    // e.g. 1h 23m 45s
    public static final String STANDARD_FORMAT_DURATION = "%dh %02dm %02ds";
    
    public static final Locale STANDARD_LOCALE = Locale.CANADA;


//*********************************************************
// constructors
//*********************************************************

    private Constants() {/* No instantiation */}
}
