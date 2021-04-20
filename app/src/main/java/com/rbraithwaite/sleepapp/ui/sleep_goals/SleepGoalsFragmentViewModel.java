package com.rbraithwaite.sleepapp.ui.sleep_goals;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepDurationGoalSuccess;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoalSuccess;
import com.rbraithwaite.sleepapp.core.repositories.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.di.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_goals.data.SleepDurationGoalUIData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

public class SleepGoalsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private CurrentGoalsRepository mCurrentGoalsRepository;
    private LiveData<WakeTimeGoal> mWakeTimeGoalModel;
    
    private DateTimeFormatter mDateTimeFormatter;
    
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

    @ViewModelInject
    public SleepGoalsFragmentViewModel(
            CurrentGoalsRepository currentGoalsRepository,
            SleepSessionRepository sleepSessionRepository,
            @UIDependenciesModule.SleepGoalsDateTimeFormatter DateTimeFormatter dateTimeFormatter,
            Executor executor)
    {
        mCurrentGoalsRepository = currentGoalsRepository;
        mSleepSessionRepository = sleepSessionRepository;
        mDateTimeFormatter = dateTimeFormatter;
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
                wakeTimeGoal -> {
                    // REFACTOR [21-02-2 9:08PM] -- put all this logic into
                    //  SleepGoalsFormatting?
                    if (wakeTimeGoal == null || !wakeTimeGoal.isSet()) {
                        return null;
                    }
                    return mDateTimeFormatter.formatTimeOfDay(wakeTimeGoal.asDate());
                });
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
