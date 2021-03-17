package com.rbraithwaite.sleepapp.ui.sleep_goals.streak_calendar;

import android.content.Context;
import android.view.View;

import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.rbraithwaite.sleepapp.R;

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
    

//*********************************************************
// constructors
//*********************************************************

    public StreakCalendar(Context context)
    {
        mContext = context;
        
        mWakeTimeGoalDecorator =
                new BackgroundDecorator(context, R.drawable.ic_goalstreakcal_waketime_24);
        mSleepDurationGoalDecorator =
                new BackgroundDecorator(context, R.drawable.ic_goalstreakcal_sleepdur_24);
        mBothGoalsDecorator = new BackgroundDecorator(context, R.drawable.ic_goalstreakcal_both_24);
        
        mNoSelectDecorator = new NoSelectionDecorator(context);
        
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
            
            mView.addDecorators(
                    mNoSelectDecorator,
                    mWakeTimeGoalDecorator,
                    mSleepDurationGoalDecorator,
                    mBothGoalsDecorator);
            
            // disable remaining selection behaviour
            mView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_NONE);
            mView.setDateTextAppearance(R.style.MaterialCalendarNoSelectionTextAppearance);
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
}
