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
