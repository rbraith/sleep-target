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
package com.rbraithwaite.sleeptarget.utils;


/**
 * Used for LiveData 'events', that is notifications where you don't care about any value, just that
 * the notification occurred in the first place. You can use LiveDataEvents to ensure the same event
 * is not consumed twice (e.g. on a config change, after re-applying an observer). Clients should
 * only respond to 'fresh' events.
 */
public class SimpleLiveDataEvent
{
//*********************************************************
// private properties
//*********************************************************

    private boolean mIsFresh = true;

//*********************************************************
// api
//*********************************************************

    
    /**
     * Check for whether the event is fresh. Doing so considers the event consumed & no longer
     * fresh.
     */
    public boolean isFresh()
    {
        boolean temp = mIsFresh;
        mIsFresh = false;
        return temp;
    }
}
