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

package com.rbraithwaite.sleepapp.test_utils;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.utils.interfaces.ProviderOf;

public class LiveDataTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private LiveDataTestUtils() {/* No instantiation */}
    
    
//*********************************************************
// api
//*********************************************************

    public static <T> LiveData<T> activateLocally(ProviderOf<LiveData<T>> provider)
    {
        LiveData<T> ld = provider.provide();
        // REFACTOR [21-07-31 3:04AM] -- move activateLocalLiveData and all other LiveData-related
        //  test utils into here.
        TestUtils.activateLocalLiveData(ld);
        return ld;
    }
}
