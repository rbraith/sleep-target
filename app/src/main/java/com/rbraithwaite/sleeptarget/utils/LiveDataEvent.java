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
package com.rbraithwaite.sleeptarget.utils;

public class LiveDataEvent<ExtraType>
        extends SimpleLiveDataEvent
{
//*********************************************************
// private properties
//*********************************************************

    private ExtraType mExtra;
    
//*********************************************************
// constructors
//*********************************************************

    public LiveDataEvent()
    {
    }
    
    public LiveDataEvent(ExtraType extra)
    {
        mExtra = extra;
    }
    
//*********************************************************
// api
//*********************************************************

    public ExtraType getExtra()
    {
        return mExtra;
    }
    
    public void setExtra(ExtraType extra)
    {
        mExtra = extra;
    }
}
