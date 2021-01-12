package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
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

    public void addSleepSession(SleepSessionWrapper sleepSession)
    {
        mRepository.addSleepSession(sleepSession.entity);
    }
    
    public void updateSleepSession(SleepSessionWrapper sleepSession)
    {
        mRepository.updateSleepSession(sleepSession.entity);
    }
    
    public int deleteSession(SleepSessionWrapper sessionToDelete)
    {
        mRepository.deleteSleepSession(sessionToDelete.entity.id);
        return sessionToDelete.entity.id;
    }
    
    public LiveData<SessionArchiveListItem> getListItemData(int id)
    {
        // convert from db form to ui form
        return Transformations.map(
                mRepository.getSleepSession(id),
                new Function<SleepSessionEntity, SessionArchiveListItem>()
                {
                    @Override
                    public SessionArchiveListItem apply(SleepSessionEntity input)
                    {
                        return convertSleepSessionToListItem(input);
                    }
                });
    }
    
    public LiveData<List<Integer>> getAllSleepSessionIds()
    {
        if (mSleepSessionDataIds == null) {
            mSleepSessionDataIds = mRepository.getAllSleepSessionIds();
        }
        return mSleepSessionDataIds;
    }
    
    public LiveData<SleepSessionWrapper> getInitialAddSessionData()
    {
        // REFACTOR [21-01-5 9:14PM] -- consider making this lazy init & storing the value in a
        //  field
        //  (avoid re-instantiation of the mapped LiveData?)
        // LiveData retval & mapping are needed because the wake-time goal is returned from the
        // repository asynchronously.
        return Transformations.map(
                mRepository.getWakeTimeGoal(),
                new Function<Long, SleepSessionWrapper>()
                {
                    @Override
                    public SleepSessionWrapper apply(Long wakeTimeGoal)
                    {
                        return new SleepSessionWrapper(
                                SleepSessionEntity.create(
                                        DateUtils.getNow(),
                                        0,
                                        wakeTimeGoal == null ?
                                                null : DateUtils.getDateFromMillis(wakeTimeGoal)));
                    }
                });
    }
    
    public LiveData<SleepSessionWrapper> getSleepSession(int id)
    {
        return Transformations.map(
                mRepository.getSleepSession(id),
                new Function<SleepSessionEntity, SleepSessionWrapper>()
                {
                    @Override
                    public SleepSessionWrapper apply(SleepSessionEntity input)
                    {
                        return new SleepSessionWrapper(input);
                    }
                });
    }


//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [20-11-15 3:54PM] -- consider extracting this method?
    private SessionArchiveListItem convertSleepSessionToListItem(SleepSessionEntity sleepSession)
    {
        if (sleepSession == null) {
            return null;
        }
        
        SimpleDateFormat sleepSessionTimeFormat =
                new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                     Constants.STANDARD_LOCALE);
        return SessionArchiveListItem.create(
                sleepSessionTimeFormat.format(sleepSession.startTime),
                sleepSessionTimeFormat.format(calculateEndTime(sleepSession.startTime,
                                                               sleepSession.duration)),
                new DurationFormatter().formatDurationMillis(sleepSession.duration));
    }
    
    private Date calculateEndTime(Date startTime, long durationMillis)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(startTime.getTime() + durationMillis);
        return calendar.getTime();
    }
}
