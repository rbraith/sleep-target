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

package com.rbraithwaite.sleepapp.utils;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class LiveDataUtils
{
//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * Used with {@link LiveDataUtils#merge(LiveData, LiveData, Merger)} to apply the merging of the
     * 2 provided values into a new type.
     */
    public interface Merger<A, B, C>
    {
        C applyMerge(A a, B b);
    }

//*********************************************************
// constructors
//*********************************************************

    private LiveDataUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static <T> void refresh(MutableLiveData<T> liveData)
    {
        liveData.setValue(liveData.getValue());
    }
    
    // REFACTOR [21-07-14 8:56PM] -- replace instances of this with MergedLiveData.
    
    /**
     * Merge the values of 2 LiveData instances into a new LiveData type. Same behavioural rules
     * apply as {@link Transformations#switchMap(LiveData, Function)} (value isn't computed until
     * observed, etc)
     */
    @Deprecated
    public static <A, B, C> LiveData<C> merge(
            LiveData<A> a,
            final LiveData<B> b,
            final Merger<A, B, C> merger)
    {
        // idea from https://stackoverflow.com/a/57819928
        return Transformations.switchMap(
                a,
                inputA -> Transformations.map(
                        b,
                        inputB -> merger.applyMerge(inputA, inputB)));
    }
    
    // IDEA [21-02-7 1:32AM] -- mergeMany(LiveData<?>...)
    //  impl: cascade w/ merge() - the result type for all but the last merge is a List<Object> or
    //  something - so the intermediate merges happen like this: List<Object> + SomeType =
    //  List<Object>,
    //  its like a LiveData value collector.
    //  The last merge is an interface where the client takes the final List<Object> and merges it
    //  down into some return type. It wouldn't be ideal to require the client to cast back their
    //  supplied types, but it might be necessary?
}
