package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.ui.session_archive.data.UISleepSessionData;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class SessionArchiveFragmentViewModel extends ViewModel {

    private SleepAppRepository mRepository;

    @ViewModelInject
    public SessionArchiveFragmentViewModel(SleepAppRepository repository) {
        mRepository = repository;
    }

    public LiveData<UISleepSessionData> getSleepSessionData(int id)
    {
        // convert from db form to ui form
        return Transformations.map(
                mRepository.getSleepSessionData(id),
                new Function<SleepSessionData, UISleepSessionData>() {
                    @Override
                    public UISleepSessionData apply(SleepSessionData input) {
                        return convertSleepSessionDataToUI(input);
                    }
                });
    }

    public LiveData<List<Integer>> getAllSleepSessionDataIds() {
        return mRepository.getAllSleepSessionDataIds();
    }

//*********************************************************
// private
//*********************************************************

    private UISleepSessionData convertSleepSessionDataToUI(SleepSessionData data) {
        if (data == null) {
            return null;
        }

        SimpleDateFormat sleepSessionTimeFormat =
                new SimpleDateFormat("h:mm a, MMM d yyyy", Locale.CANADA);

        UISleepSessionData uiData = new UISleepSessionData();
        uiData.startTime = sleepSessionTimeFormat.format(data.startTime);

        // calculate end date
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(data.startTime.getTime() + data.duration);

        uiData.endTime = sleepSessionTimeFormat.format(calendar.getTime());

        uiData.sessionDuration = formatSessionDuration(data.duration);

        return uiData;
    }

    private String formatSessionDuration(long sessionDurationMillis) {
        long durationAsSeconds = sessionDurationMillis / 1000L;

        long durationAsMinutes = durationAsSeconds / 60;
        long seconds = durationAsSeconds % 60;
        long minutes = durationAsMinutes % 60;
        long hours = durationAsMinutes / 60;

        return String.format(Locale.CANADA, "%dh %02dm %02ds", hours, minutes, seconds);
    }

}
