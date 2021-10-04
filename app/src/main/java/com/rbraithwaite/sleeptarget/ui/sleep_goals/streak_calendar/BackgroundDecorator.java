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

package com.rbraithwaite.sleeptarget.ui.sleep_goals.streak_calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;

import androidx.appcompat.content.res.AppCompatResources;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;

public class BackgroundDecorator
        implements DayViewDecorator
{
//*********************************************************
// private properties
//*********************************************************

    private HashSet<CalendarDay> mDates;

//*********************************************************
// private constants
//*********************************************************

    private final Drawable mBackgroundDrawable;
    private final int mTextColor;

//*********************************************************
// constructors
//*********************************************************

    public BackgroundDecorator(Context context, int backgroundDrawable, int textColor)
    {
        mBackgroundDrawable = AppCompatResources.getDrawable(context, backgroundDrawable);
        mTextColor = textColor;
        mDates = new HashSet<>();
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public boolean shouldDecorate(CalendarDay day)
    {
        return mDates.contains(day);
    }
    
    @Override
    public void decorate(DayViewFacade view)
    {
        view.setBackgroundDrawable(mBackgroundDrawable);
        view.addSpan(new ForegroundColorSpan(mTextColor));
    }

//*********************************************************
// api
//*********************************************************

    // TODO [21-03-16 9:07PM] -- test this?
    public void setDates(List<Date> dates)
    {
        mDates.clear();
        GregorianCalendar cal = new GregorianCalendar();
        for (Date d : dates) {
            cal.setTime(d);
            // REFACTOR [21-03-12 4:35PM] -- extract conversion logic for Calendar to CalendarDay.
            mDates.add(CalendarDay.from(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1, // CalendarDay months start from 1
                    cal.get(Calendar.DAY_OF_MONTH)));
        }
    }
}
