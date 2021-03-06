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

package com.rbraithwaite.sleeptarget.test_helpers;

import android.os.Looper;

import com.rbraithwaite.sleeptarget.utils.TickingLiveData;

import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;

import java.util.Collection;

import static org.robolectric.Shadows.shadowOf;

public class RobolectricUtils
{
//*********************************************************
// constructors
//*********************************************************

    private RobolectricUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static ShadowLooper getLooperForThread(String threadName)
    {
        // http://robolectric.org/blog/2019/06/04/paused-looper/
        // https://github.com/robolectric/robolectric/blob
        // /e197c5b9ed83dfd0d2ea6a74cf189f7b39463adc/robolectric/src/test/java/org/robolectric
        // /shadows/ShadowPausedLooperTest.java#L95
        // https://github.com/robolectric/robolectric/issues/1993
        // https://stackoverflow.com/a/39122515
        Collection<Looper> loopers = ShadowLooper.getAllLoopers();
        ShadowLooper shadowLooper = null;
        for (Looper looper : loopers) {
            if (looper.getThread().getName().equals(TickingLiveData.THREAD_NAME)) {
                shadowLooper = Shadow.extract(looper);
                break;
            }
        }
        return shadowLooper;
    }
    
    
    public static void idleMainLooper()
    {
        shadowOf(Looper.getMainLooper()).idle();
    }
}
