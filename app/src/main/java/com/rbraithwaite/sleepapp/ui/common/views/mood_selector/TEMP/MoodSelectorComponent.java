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

package com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rbraithwaite.sleepapp.R;

// REFACTOR [21-05-24 3:17AM] -- move MoodSelectorController functionality into here, move this
//  out of TEMP package and delete that package.
public class MoodSelectorComponent
        extends ConstraintLayout
{
//*********************************************************
// constructors
//*********************************************************

    public MoodSelectorComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public MoodSelectorComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent(context);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initComponent(Context context)
    {
        inflate(context, R.layout.mood_selector, this);
    }
}
