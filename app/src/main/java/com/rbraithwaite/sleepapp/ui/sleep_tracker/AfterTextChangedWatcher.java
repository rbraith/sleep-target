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

package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.text.TextWatcher;

// REFACTOR [21-07-5 3:24AM] -- move this somewhere more general.


/**
 * A TextWatcher which only cares about the text *after* it has changed.
 */
public abstract class AfterTextChangedWatcher
        implements TextWatcher
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        // do nothing
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        // do nothing
    }
}
