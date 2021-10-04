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

package com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions;

import android.provider.BaseColumns;

public class SleepInterruptionContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "interruptions";

//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String SESSION_ID = "session_id";
        public static final String START_TIME = "start_time";
        public static final String DURATION_MILLIS = "duration";
        public static final String REASON = "reason";
    }

//*********************************************************
// constructors
//*********************************************************

    private SleepInterruptionContract() {/* No instantiation */}
}
