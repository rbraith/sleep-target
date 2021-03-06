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
import android.content.res.TypedArray;
import android.view.View;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;
import com.rbraithwaite.sleeptarget.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StreakCalendar
{
//*********************************************************
// package properties
//*********************************************************

    BackgroundDecorator mWakeTimeGoalDecorator;
    BackgroundDecorator mSleepDurationGoalDecorator;
    BackgroundDecorator mBothGoalsDecorator;
    
    NoSelectionDecorator mNoSelectDecorator;
    
    // SMELL [21-03-12 5:58PM] -- This context reference is ok for how StreakCalendar is currently
    //  used in SleepGoalsFragment, but it shouldn't be making assumptions about its usage like
    //  that.
    Context mContext;
    MaterialCalendarView mView;
    // OPTIMIZE [21-03-16 4:57PM] -- make these Sets instead?
    List<Date> mWakeTimeGoalDates;
    List<Date> mSleepDurationGoalDates;
    List<Date> mBothGoalsDates;
    
    public interface OnMonthChangedListener
    {
        // TODO [21-10-4 8:40PM] -- This will likely need to pass data about which month was
        //  changed to.
        void onMonthChanged();
    }
    
    private OnMonthChangedListener mOnMonthChangedListener;


//*********************************************************
// constructors
//*********************************************************

    public StreakCalendar(Context context, OnMonthChangedListener onMonthChangedListener)
    {
        mContext = context;
        
        mOnMonthChangedListener = onMonthChangedListener;
        
        int goalTextColor = getGoalTextColorFrom(mContext);
        
        mWakeTimeGoalDecorator = new BackgroundDecorator(
                mContext, R.drawable.ic_goalstreakcal_waketime_24, goalTextColor);
        mSleepDurationGoalDecorator = new BackgroundDecorator(
                mContext, R.drawable.ic_goalstreakcal_sleepdur_24, goalTextColor);
        mBothGoalsDecorator = new BackgroundDecorator(
                context, R.drawable.ic_goalstreakcal_both_24, goalTextColor);
        
        mNoSelectDecorator = new NoSelectionDecorator(mContext);
        
        mWakeTimeGoalDates = new ArrayList<>();
        mSleepDurationGoalDates = new ArrayList<>();
        mBothGoalsDates = new ArrayList<>();
    }

//*********************************************************
// api
//*********************************************************

    public View getView()
    {
        if (mView == null) {
            mView = new MaterialCalendarView(mContext);
            
            if (mOnMonthChangedListener != null) {
                mView.setOnMonthChangedListener((widget, date) -> mOnMonthChangedListener.onMonthChanged());
            }
            
            mView.setLeftArrow(R.drawable.ic_goalstreakcal_arrow_left);
            mView.setRightArrow(R.drawable.ic_goalstreakcal_arrow_right);
            
            mView.addDecorators(
                    mNoSelectDecorator,
                    mWakeTimeGoalDecorator,
                    mSleepDurationGoalDecorator,
                    mBothGoalsDecorator);
            
            // disable remaining selection behaviour
            mView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
        }
        return mView;
    }
    
    // TODO [21-03-16 9:36PM] -- this should be tested.
    public void setSucceededGoalDates(
            List<Date> wakeTimeGoalDates,
            List<Date> sleepDurationGoalDates)
    {
        // REFACTOR [21-03-16 4:45PM] -- call this clearAllDates().
        mWakeTimeGoalDates.clear();
        mSleepDurationGoalDates.clear();
        mBothGoalsDates.clear();
        
        // wake-time & sleep duration are the differences
        // both is the intersection
        mSleepDurationGoalDates.addAll(sleepDurationGoalDates);
        for (Date d : wakeTimeGoalDates) {
            if (sleepDurationGoalDates.contains(d)) {
                mBothGoalsDates.add(d);
                mSleepDurationGoalDates.remove(d);
            } else {
                mWakeTimeGoalDates.add(d);
            }
        }
        
        mWakeTimeGoalDecorator.setDates(mWakeTimeGoalDates);
        mSleepDurationGoalDecorator.setDates(mSleepDurationGoalDates);
        mBothGoalsDecorator.setDates(mBothGoalsDates);
        mView.invalidateDecorators();
    }

//*********************************************************
// private methods
//*********************************************************

    private int getGoalTextColorFrom(Context context)
    {
        // goal text is using colorOnPrimary
        TypedArray ta = context.obtainStyledAttributes(new int[] {R.attr.colorOnPrimary});
        int goalTextColor = ta.getColor(0, -1);
        ta.recycle();
        return goalTextColor;
    }
}
