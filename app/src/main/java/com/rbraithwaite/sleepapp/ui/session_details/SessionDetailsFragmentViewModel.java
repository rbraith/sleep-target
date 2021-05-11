package com.rbraithwaite.sleepapp.ui.session_details;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.ConvertTag;
import com.rbraithwaite.sleepapp.ui.common.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// REFACTOR [21-01-5 2:30AM] -- passing around Dates and longs for times-of-day or dates is too
//  much information. Instead there should be TimeOfDay and Date which are simple POJOs and
//  contain eg hourOfDay & minute fields. Then I can use Date & Calendar in the impls to convert
//  for storage or display
public class SessionDetailsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private LiveData<String> mSessionDurationText;
    
    private MutableLiveData<SleepSession> mSleepSession = new MutableLiveData<>();

//*********************************************************
// public helpers
//*********************************************************

    public static class InvalidDateTimeException
            extends RuntimeException
    {
        public InvalidDateTimeException(String message)
        {
            super(message);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionDetailsFragmentViewModel()
    {
    }

//*********************************************************
// api
//*********************************************************

    public SleepSessionWrapper getResult()
    {
        return new SleepSessionWrapper(mSleepSession.getValue());
    }
    
    public LiveData<String> getSessionDurationText()
    {
        // REFACTOR [21-03-25 12:11AM] -- This should be SessionDataFormatting.
        final DurationFormatter formatter = new DurationFormatter();
        
        if (mSessionDurationText == null) {
            final MediatorLiveData<String> mediatorLiveData = new MediatorLiveData<>();
            mediatorLiveData.addSource(getSleepSession(), sleepSession -> {
                if (sleepSession == null) {
                    mediatorLiveData.setValue(null);
                } else {
                    mediatorLiveData.setValue(
                            formatter.formatDurationMillis(sleepSession.getDurationMillis()));
                }
            });
            mSessionDurationText = mediatorLiveData;
        }
        
        return mSessionDurationText;
    }
    
    public void setStartDate(int year, int month, int dayOfMonth)
    {
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getStart());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        try {
            mSleepSession.getValue().setStartFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setEndDate(int year, int month, int dayOfMonth)
    {
        // OPTIMIZE [20-11-30 11:19PM] -- consider doing nothing if the new date
        //  matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getEnd());
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        try {
            mSleepSession.getValue().setEndFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setStartTimeOfDay(int hourOfDay, int minute)
    {
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getStart());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        try {
            mSleepSession.getValue().setStartFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void setEndTimeOfDay(int hourOfDay, int minute)
    {
        // OPTIMIZE [20-12-6 8:36PM] -- consider doing nothing if the new time matches the old.
        
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(mSleepSession.getValue().getEnd());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        try {
            mSleepSession.getValue().setEndFixed(calendar);
            notifySessionChanged();
        } catch (SleepSession.InvalidDateError e) {
            // REFACTOR [21-03-25 12:55AM] -- Is this exception conversion necessary, or is it
            //  alright to have the view handle a domain exception?
            throw new InvalidDateTimeException((e.getMessage()));
        }
    }
    
    public void clearSessionData()
    {
        mSleepSession.setValue(null);
    }
    
    public void setSessionData(SleepSessionWrapper sessionData)
    {
        mSleepSession.setValue(sessionData.getModel());
    }
    
    public LiveData<GregorianCalendar> getStartCalendar()
    {
        return Transformations.map(
                getSleepSession(),
                sleepSession -> {
                    if (sleepSession == null) {
                        return null;
                    }
                    // REFACTOR [21-03-31 4:13PM] -- call this ConvertCalendar.fromDate().
                    GregorianCalendar result = new GregorianCalendar();
                    result.setTime(sleepSession.getStart());
                    return result;
                });
    }
    
    public LiveData<GregorianCalendar> getEndCalendar()
    {
        return Transformations.map(
                getSleepSession(),
                sleepSession -> {
                    if (sleepSession == null) {
                        return null;
                    }
                    GregorianCalendar result = new GregorianCalendar();
                    result.setTime(sleepSession.getEnd());
                    return result;
                });
    }
    
    public LiveData<String> getAdditionalComments()
    {
        return Transformations.map(
                getSleepSession(),
                sleepSession -> {
                    if (sleepSession == null) {
                        return null;
                    }
                    return sleepSession.getAdditionalComments();
                });
    }
    
    /**
     * Set the additional comments. Note that this does not automatically update the LiveData
     * provided by getAdditionalComments.
     */
    public void setAdditionalComments(String additionalComments)
    {
        if (additionalComments != null && additionalComments.equals("")) {
            additionalComments = null;
        }
        // note: this change is not broadcast with notifySessionChanged(), as there is no reason to
        // update the UI when the EditText will retain the text value while the fragment is
        // displayed. When the fragment restarts it will then use this value to initialize itself,
        // from getAdditionalComments().
        String commentsToSet = additionalComments;
        // the sleep session might be null if clearSleepSession() was called (or if it was set
        // to null manually)
        getOptionalSleepSession().ifPresent(sleepSession -> sleepSession.setAdditionalComments(
                commentsToSet));
    }
    
    public MoodUiData getMood()
    {
        // REFACTOR [21-04-7 9:04PM] -- consider returning LiveData instead.
        return getOptionalSleepSession()
                .map(sleepSession -> ConvertMood.toUiData(sleepSession.getMood()))
                .orElse(null);
    }
    
    public void setMood(MoodUiData mood)
    {
        // note: this change is not broadcast with notifySessionChanged(), as the mood selector will
        // handle its own UI updates
        getOptionalSleepSession().ifPresent(sleepSession -> sleepSession.setMood(ConvertMood.fromUiData(
                mood)));
    }
    
    public void clearMood()
    {
        // note: this change is not broadcast with notifySessionChanged(), as the mood selector will
        // handle its own UI updates
        getOptionalSleepSession().ifPresent(sleepSession -> sleepSession.setMood(null));
    }
    
    public void setTags(List<TagUiData> tags)
    {
        getOptionalSleepSession().ifPresent(
                sleepSession -> sleepSession.setTags(tags.stream()
                                                             .map(ConvertTag::fromUiData)
                                                             .collect(Collectors.toList())));
    }
    
    public List<Integer> getTagIds()
    {
        return getOptionalSleepSession()
                .map(sleepSession -> sleepSession.getTags()
                        .stream()
                        .map(Tag::getTagId)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

//*********************************************************
// protected api
//*********************************************************

    // REFACTOR [21-03-5 12:53AM] -- consider ctor injecting this instead?
    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    private Optional<SleepSession> getOptionalSleepSession()
    {
        return Optional.ofNullable(getSleepSession().getValue());
    }
    
    private LiveData<SleepSession> getSleepSession()
    {
        return mSleepSession;
    }
    
    private void notifySessionChanged()
    {
        mSleepSession.setValue(mSleepSession.getValue());
    }
}
