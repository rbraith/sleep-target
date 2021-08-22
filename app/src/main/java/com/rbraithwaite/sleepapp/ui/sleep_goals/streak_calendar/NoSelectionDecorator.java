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

package com.rbraithwaite.sleepapp.ui.sleep_goals.streak_calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.content.res.AppCompatResources;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;



/**
 * MaterialCalendarView decorator for disabling selection highlighting on a calendar.
 */
public class NoSelectionDecorator
        implements DayViewDecorator
{
//*********************************************************
// private constants
//*********************************************************

    private final Drawable mNoSelectDrawable;

//*********************************************************
// constructors
//*********************************************************

    public NoSelectionDecorator(Context context)
    {
        mNoSelectDrawable = AppCompatResources.getDrawable(context, android.R.color.transparent);
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public boolean shouldDecorate(CalendarDay day)
    {
        // apply to all days
        return true;
    }
    
    @Override
    public void decorate(DayViewFacade view)
    {
        view.setSelectionDrawable(mNoSelectDrawable);
    }
}
