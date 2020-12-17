package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.data.UISleepSessionData;
import com.rbraithwaite.sleepapp.ui.session_edit.SessionEditData;
import com.rbraithwaite.sleepapp.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SessionArchiveFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppRepository mRepository;
    private LiveData<List<Integer>> mSleepSessionDataIds;
    
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragViewMod";

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionArchiveFragmentViewModel(SleepAppRepository repository)
    {
        mRepository = repository;
    }
    
//*********************************************************
// api
//*********************************************************

    public void addSessionFromResult(SessionEditData result)
    {
        mRepository.addSleepSessionData(result.toSleepSessionData());
    }
    
    public void updateSessionFromResult(SessionEditData result)
    {
        mRepository.updateSleepSessionData(result.toSleepSessionData());
    }
    
    public LiveData<UISleepSessionData> getSleepSessionData(int id)
    {
        // convert from db form to ui form
        return Transformations.map(
                mRepository.getSleepSessionData(id),
                new Function<SleepSessionData, UISleepSessionData>()
                {
                    @Override
                    public UISleepSessionData apply(SleepSessionData input)
                    {
                        return convertSleepSessionDataToUI(input);
                    }
                });
    }
    
    public LiveData<List<Integer>> getAllSleepSessionDataIds()
    {
        if (mSleepSessionDataIds == null) {
            mSleepSessionDataIds = mRepository.getAllSleepSessionDataIds();
        }
        return mSleepSessionDataIds;
    }
    
    public SessionEditData getDefaultAddSessionData()
    {
        long now = DateUtils.getNow().getTime();
        return new SessionEditData(now, now);
    }
    
    public LiveData<SessionEditData> getInitialEditSessionData(int sessionIdForEditing)
    {
        // I've having trouble verifying this, but i think this: https://stackoverflow
        // .com/a/48498660
        // means that each LiveData instance returned by a Room DAO contains within it the database
        // Observer. Meaning that each LiveData returned independently observes the database and
        // getting new LiveData instances from the DAO shouldn't stop older ones from continuing
        // observation.
        // --
        // All this to say that I think the separate calls to mRepository.getSleepSessionData()
        // here and in
        // SessionArchiveFragment.getSleepSessionData() shouldn't conflict?
        return Transformations.map(
                mRepository.getSleepSessionData(sessionIdForEditing),
                new Function<SleepSessionData, SessionEditData>()
                {
                    @Override
                    public SessionEditData apply(SleepSessionData sleepSessionData)
                    {
                        long startTime = sleepSessionData.startTime.getTime();
                        long endTime = startTime + sleepSessionData.duration;
                        return new SessionEditData(sleepSessionData.id, startTime, endTime);
                    }
                });
    }


//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [20-11-15 3:54PM] -- consider extracting this method?
    private UISleepSessionData convertSleepSessionDataToUI(SleepSessionData data)
    {
        if (data == null) {
            return null;
        }
        
        SimpleDateFormat sleepSessionTimeFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                     Constants.STANDARD_LOCALE);
        return UISleepSessionData.create(
                sleepSessionTimeFormat.format(data.startTime),
                sleepSessionTimeFormat.format(calculateEndTime(data.startTime, data.duration)),
                new DurationFormatter().formatDurationMillis(data.duration));
    }
    
    private Date calculateEndTime(Date startTime, long durationMillis)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startTime.getTime() + durationMillis);
        return calendar.getTime();
    }
}
