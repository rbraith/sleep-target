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

package com.rbraithwaite.sleeptarget.ui.sleep_goals;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoalSuccess;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoalSuccess;
import com.rbraithwaite.sleeptarget.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.MergedLiveData;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SleepGoalsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private CurrentGoalsRepository mCurrentGoalsRepository;
    private LiveData<WakeTimeGoal> mWakeTimeGoalModel;
    
    private LiveData<SleepDurationGoal> mSleepDurationGoalModel;
    
    private TimeUtils mTimeUtils;
    
    private Executor mExecutor;
    
    private SleepSessionRepository mSleepSessionRepository;
    private LiveData<List<Date>> mSucceededWakeTimeGoalDates;
    private LiveData<List<Date>> mSucceededSleepDurationGoalDates;

//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_WAKETIME_HOUR = 8;
    
    public static final int DEFAULT_WAKETIME_MINUTE = 0;
    
    public static final int DEFAULT_SLEEP_DURATION_HOUR = 8;
    public static final int DEFAULT_SLEEP_DURATION_MINUTE = 0;

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SleepGoalsFragmentViewModel(
            CurrentGoalsRepository currentGoalsRepository,
            SleepSessionRepository sleepSessionRepository,
            Executor executor)
    {
        mCurrentGoalsRepository = currentGoalsRepository;
        mSleepSessionRepository = sleepSessionRepository;
        mExecutor = executor;
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public long getDefaultWakeTime()
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(Calendar.HOUR_OF_DAY, DEFAULT_WAKETIME_HOUR);
        calendar.set(Calendar.MINUTE, DEFAULT_WAKETIME_MINUTE);
        return calendar.getTimeInMillis();
    }
    
    public LiveData<String> getWakeTimeText()
    {
        return Transformations.map(
                getWakeTimeGoalModel(),
                SleepGoalsFormatting::formatWakeTimeGoal);
    }
    
    public void setWakeTime(int hourOfDay, int minute)
    {
        mCurrentGoalsRepository.setWakeTimeGoal(new WakeTimeGoal(
                mTimeUtils.getNow(),
                (int) mTimeUtils.timeToMillis(hourOfDay, minute, 0, 0)
        ));
    }
    
    public LiveData<Boolean> hasWakeTime()
    {
        return Transformations.map(
                getWakeTimeGoalModel(),
                wakeTimeGoal -> (wakeTimeGoal != null && wakeTimeGoal.isSet()));
    }
    
    public LiveData<Long> getWakeTimeGoalDateMillis()
    {
        return Transformations.map(
                getWakeTimeGoalModel(),
                wakeTimeGoal -> wakeTimeGoal.asDate().getTime());
    }
    
    public void clearWakeTime()
    {
        mCurrentGoalsRepository.clearWakeTimeGoal();
    }
    
    public LiveData<Boolean> hasSleepDurationGoal()
    {
        return Transformations.map(
                getSleepDurationGoalModel(),
                sleepDurationGoal -> (sleepDurationGoal != null && sleepDurationGoal.isSet()));
    }
    
    public SleepDurationGoalUIData getDefaultSleepDurationGoal()
    {
        return new SleepDurationGoalUIData(DEFAULT_SLEEP_DURATION_HOUR,
                                           DEFAULT_SLEEP_DURATION_MINUTE);
    }
    
    public void setSleepDurationGoal(int hours, int minutes)
    {
        mCurrentGoalsRepository.setSleepDurationGoal(new SleepDurationGoal(hours, minutes));
    }
    
    public LiveData<String> getSleepDurationGoalText()
    {
        return Transformations.map(
                getSleepDurationGoalModel(),
                // REFACTOR [21-02-2 7:01PM] -- should this instead be an injected object?
                //  eg SleepGoalsFormatter, a more OOP approach allowing for multiple
                //  formats
                //  --
                //  or maybe as a DurationFormatter implementation - the reason I didn't
                //  choose this path is because I wouldn't need
                //  DurationFormatter.formatDurationMillis() here (i would need to implement
                //  that or leave it stubbed, and neither option seemed great).
                SleepGoalsFormatting::formatSleepDurationGoal);
    }
    
    public LiveData<SleepDurationGoalUIData> getSleepDurationGoal()
    {
        return Transformations.map(
                getSleepDurationGoalModel(),
                input -> {
                    // REFACTOR [21-02-3 6:15PM] -- extract this conversion logic.
                    return new SleepDurationGoalUIData(
                            input.getHours(),
                            input.getRemainingMinutes());
                });
    }
    
    public void clearSleepDurationGoal()
    {
        mCurrentGoalsRepository.clearSleepDurationGoal();
    }
    
    public static class SucceededTargetDates
    {
        List<Date> wakeTimeDates;
        List<Date> durationDates;
    
        public SucceededTargetDates(
                List<Date> wakeTimeDates,
                List<Date> durationDates)
        {
            this.wakeTimeDates = wakeTimeDates;
            this.durationDates = durationDates;
        }
    }
    
    public void onCalendarMonthChanged()
    {
        LiveDataUtils.refresh(mSucceededTargetDates);
    }
    
    private MutableLiveData<SucceededTargetDates> mSucceededTargetDates;
    
    public LiveData<SucceededTargetDates> getSucceededTargetDates()
    {
        mSucceededTargetDates = CommonUtils.lazyInit(mSucceededTargetDates, () -> {
            MediatorLiveData<SucceededTargetDates> mediator = new MediatorLiveData<>();
            mediator.addSource(
                    new MergedLiveData(
                            true,
                            getSucceededWakeTimeGoalDates(),
                            getSucceededSleepDurationGoalDates()),
                    mergedUpdate -> {
                        List<Date> wakeTimeDates = (List<Date>) mergedUpdate.values.get(0);
                        List<Date> durationDates = (List<Date>) mergedUpdate.values.get(1);
                        mediator.setValue(new SucceededTargetDates(wakeTimeDates, durationDates));
                    });
            return mediator;
        });
        return mSucceededTargetDates;
    }
    
    public LiveData<List<Date>> getSucceededSleepDurationGoalDates()
    {
        if (mSucceededSleepDurationGoalDates == null) {
            mSucceededSleepDurationGoalDates = Transformations.switchMap(
                    mCurrentGoalsRepository.getSleepDurationGoalHistory(),
                    sleepDurationGoalHistory -> {
                        // REFACTOR [21-03-25 12:01AM] -- I forget if I did this already, but
                        //  this async initialization logic should be extracted as a general
                        //  utility.
                        final MutableLiveData<List<Date>> liveData = new MutableLiveData<>();
                        mExecutor.execute(() -> {
                            SleepDurationGoalSuccess success = new SleepDurationGoalSuccess(
                                    sleepDurationGoalHistory,
                                    mTimeUtils,
                                    mSleepSessionRepository);
                            liveData.postValue(success.getSucceededDates());
                        });
                        return liveData;
                    });
        }
        return mSucceededSleepDurationGoalDates;
    }
    
    // OPTIMIZE [21-03-16 9:28PM] -- Instead of fully recomputing the success streaks whenever the
    //  history is updated, it would be much better to cache the streak data directly as streak
    //  start/end times in a db table - this way I would only need to compute the new days since the
    //  last computation (although I would need to track whether the sleep session record became
    //  invalidated through manually editing or adding a sleep session - then in that case I *would*
    //  need to fully rebuild the streak data)
    
    /**
     * Returns a list of dates which successfully met their related historical wake-time goal (ie
     * the wake-time goal as it was on that day). If there is no wake-time goal history or there are
     * no sleep sessions, this will return an empty list.
     */
    public LiveData<List<Date>> getSucceededWakeTimeGoalDates()
    {
        if (mSucceededWakeTimeGoalDates == null) {
            mSucceededWakeTimeGoalDates = Transformations.switchMap(
                    mCurrentGoalsRepository.getWakeTimeGoalHistory(),
                    wakeTimeGoalHistory -> {
                        final MutableLiveData<List<Date>> liveData = new MutableLiveData<>();
                        mExecutor.execute(() -> {
                            // SMELL [21-03-26 1:31AM] -- Is it weird for
                            //  WakeTimeGoalSuccess
                            //  and SleepDurationGoalSuccess (domain models) to be
                            //  depending on TimeUtils? Should there be some kind of
                            //  time utility or service in the domain instead? Research
                            //  conventional solutions to time.
                            WakeTimeGoalSuccess success = new WakeTimeGoalSuccess(
                                    wakeTimeGoalHistory,
                                    mSleepSessionRepository,
                                    mTimeUtils);
                            liveData.postValue(success.getSucceededDates());
                        });
                        return liveData;
                    });
        }
        return mSucceededWakeTimeGoalDates;
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    private LiveData<WakeTimeGoal> getWakeTimeGoalModel()
    {
        if (mWakeTimeGoalModel == null) {
            mWakeTimeGoalModel = mCurrentGoalsRepository.getWakeTimeGoal();
        }
        return mWakeTimeGoalModel;
    }
    
    // Retain a private reference to the current sleep duration goal, so that a new instance doesn't
    // need to be retrieved from the repo every time the fragment restarts.
    private LiveData<SleepDurationGoal> getSleepDurationGoalModel()
    {
        if (mSleepDurationGoalModel == null) {
            mSleepDurationGoalModel = mCurrentGoalsRepository.getSleepDurationGoal();
        }
        return mSleepDurationGoalModel;
    }
}
