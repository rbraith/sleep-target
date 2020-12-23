package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.utils.DateUtils;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;

//import java.util.logging.Handler;

public class SleepTrackerFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppRepository mRepository;
    
    private LiveData<Date> mCurrentSleepSession;
    private LiveData<Boolean> mInSleepSession;
    private LiveData<String> mCurrentSleepSessionDuration;

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SleepTrackerFragmentViewModel(SleepAppRepository repository)
    {
        mRepository = repository;
    }


//*********************************************************
// api
//*********************************************************

    public LiveData<Boolean> inSleepSession(Context context)
    {
        if (mInSleepSession == null) {
            mInSleepSession = Transformations.map(
                    getCurrentSleepSession(context),
                    new Function<Date, Boolean>()
                    {
                        @Override
                        public Boolean apply(Date input)
                        {
                            return (input != null);
                        }
                    }
            );
        }
        return mInSleepSession;
    }
    
    /**
     * If a new session is started while one is currently ongoing, the ongoing session is discarded.
     * If you want to retain that session instead, call
     * {@link SleepTrackerFragmentViewModel#endSleepSession(Context)}
     * .
     *
     * @param context This is needed to persist the current session information
     */
    public void startSleepSession(Context context)
    {
        mRepository.setCurrentSession(context, DateUtils.getNow());
    }
    
    public void endSleepSession(Context context)
    {
        // TODO [20-11-14 5:43PM] -- add an internal observer to inSleepSession to
        //  guarantee its active here?
        if (inSleepSession(context).getValue()) {
            Date currentSessionStart = getCurrentSleepSession(context).getValue();
            long durationMillis = calculateDurationMillis(currentSessionStart, DateUtils.getNow());
            
            SleepSessionData sleepSessionData = new SleepSessionData();
            sleepSessionData.startTime = currentSessionStart;
            sleepSessionData.duration = durationMillis;
            
            mRepository.addSleepSessionData(sleepSessionData);
            
            mRepository.clearCurrentSession(context);
        }
    }
    
    public LiveData<String> getCurrentSleepSessionDuration(Context context)
    {
        if (mCurrentSleepSessionDuration != null) {
            return mCurrentSleepSessionDuration;
        }
        
        // This works with the TickingLiveData because MediatorLiveData "correctly propagates its
        // active/inactive states down to source LiveData objects."
        // https://developer.android.com/reference/androidx/lifecycle/MediatorLiveData
        mCurrentSleepSessionDuration = Transformations.switchMap(
                getCurrentSleepSession(context),
                new Function<Date, androidx.lifecycle.LiveData<String>>()
                {
                    @Override
                    public androidx.lifecycle.LiveData<String> apply(Date input)
                    {
                        final Date currentSleepSessionStart = input;
                        final DurationFormatter durationFormatter = new DurationFormatter();
                        
                        if (currentSleepSessionStart == null) {
                            return new MutableLiveData<>(new DurationFormatter().formatDurationMillis(
                                    0));
                        } else {
                            return new TickingLiveData<String>()
                            {
                                @Override
                                public String onTick()
                                {
                                    return durationFormatter.formatDurationMillis(
                                            calculateDurationMillis(
                                                    currentSleepSessionStart,
                                                    DateUtils.getNow()
                                            ));
                                }
                            };
                        }
                    }
                }
        );
        
        return mCurrentSleepSessionDuration;
    }
    
    public String getSessionStartTime(Context context)
    {
        Date startTime = getCurrentSleepSession(context).getValue();
        if (startTime == null) {
            return null;
        }
        return new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                    Constants.STANDARD_LOCALE).format(startTime);
    }
    
    public LiveData<String> getWakeTime()
    {
        return Transformations.map(
                mRepository.getWakeTimeGoal(),
                new Function<Long, String>()
                {
                    @Override
                    public String apply(Long wakeTimeGoal)
                    {
                        if (wakeTimeGoal == null) {
                            return null;
                        } else {
                            // REFACTOR [20-12-23 5:22PM] -- I have way too many
                            //  DateTimeFormatter instances
                            //  floating around, I should be injecting these with Hilt - I could
                            //  have
                            //  a standard one, and fragment-specific ones.
                            return new DateTimeFormatter().formatTimeOfDay(DateUtils.getDateFromMillis(
                                    wakeTimeGoal));
                        }
                    }
                });
    }


//*********************************************************
// private methods
//*********************************************************

    private long calculateDurationMillis(Date start, Date end)
    {
        return end.getTime() - start.getTime();
    }
    
    private LiveData<Date> getCurrentSleepSession(Context context)
    {
        if (mCurrentSleepSession == null) {
            mCurrentSleepSession = mRepository.getCurrentSession(context);
        }
        return mCurrentSleepSession;
    }
}
