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
        return mRepository.getAllSleepSessionDataIds();
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
                new SimpleDateFormat(Constants.STANDARD_DATE_FORMAT, Constants.STANDARD_LOCALE);
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
