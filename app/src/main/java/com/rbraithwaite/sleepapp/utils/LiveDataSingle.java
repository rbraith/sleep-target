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

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.rbraithwaite.sleepapp.utils.interfaces.OneWayConverter;


/**
 * Simply a mediator which removes its sources the moment they generate a value.
 */
public class LiveDataSingle<T>
        extends MediatorLiveData<T>
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public <S> void addSource(
            @NonNull LiveData<S> source, @NonNull Observer<? super S> onChanged)
    {
        super.addSource(source, val -> {
            this.removeSource(source);
            onChanged.onChanged(val);
        });
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * Returns a LiveDataSingle which sets itself to the provided source using the provided
     * converter.
     */
    public static <T, S> LiveDataSingle<T> withSource(
            @NonNull LiveData<S> source,
            @NonNull OneWayConverter<S, T> converter)
    {
        LiveDataSingle<T> single = new LiveDataSingle<>();
        single.addSource(source, val -> {
            single.setValue(converter.convert(val));
        });
        return single;
    }
    
    public static <S> LiveDataSingle<S> withSource(@NonNull LiveData<S> source)
    {
        return LiveDataSingle.withSource(source, val -> val);
    }
}
