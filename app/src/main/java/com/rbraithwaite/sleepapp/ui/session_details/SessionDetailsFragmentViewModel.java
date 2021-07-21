package com.rbraithwaite.sleepapp.ui.session_details;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;
import com.rbraithwaite.sleepapp.ui.common.interruptions.ConvertInterruption;
import com.rbraithwaite.sleepapp.ui.common.interruptions.InterruptionFormatting;
import com.rbraithwaite.sleepapp.ui.common.interruptions.InterruptionListItem;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    private TimeUtils mTimeUtils;

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
    
    public static class FutureDateTimeException
            extends RuntimeException
    {
        public FutureDateTimeException(String message)
        {
            super(message);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionDetailsFragmentViewModel(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
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
        if (mSessionDurationText == null) {
            final MediatorLiveData<String> mediatorLiveData = new MediatorLiveData<>();
            mediatorLiveData.addSource(getSleepSession(), sleepSession -> {
                if (sleepSession == null) {
                    mediatorLiveData.setValue(null);
                } else {
                    mediatorLiveData.setValue(
                            SessionDetailsFormatting.formatDuration(sleepSession.getDurationMillis()));
                }
            });
            mSessionDurationText = mediatorLiveData;
        }
        
        return mSessionDurationText;
    }
    
    public void setStartDate(int year, int month, int dayOfMonth)
    {
        GregorianCalendar calendar = new GregorianCalendar();
        Date oldStart = mSleepSession.getValue().getStart();
        calendar.setTime(oldStart);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        if (calendar.getTime().equals(oldStart)) {
            return;
        }
        
        checkIfDateIsInTheFuture(calendar.getTime());
        
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
        GregorianCalendar calendar = new GregorianCalendar();
        Date oldEnd = mSleepSession.getValue().getEnd();
        calendar.setTime(oldEnd);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        
        if (calendar.getTime().equals(oldEnd)) {
            return;
        }
        
        checkIfDateIsInTheFuture(calendar.getTime());
        
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
        GregorianCalendar calendar = new GregorianCalendar();
        Date oldStart = mSleepSession.getValue().getStart();
        calendar.setTime(oldStart);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        if (calendar.getTime().equals(oldStart)) {
            return;
        }
        
        checkIfDateIsInTheFuture(calendar.getTime());
        
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
        GregorianCalendar calendar = new GregorianCalendar();
        Date oldEnd = mSleepSession.getValue().getEnd();
        calendar.setTime(oldEnd);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        
        if (calendar.getTime().equals(oldEnd)) {
            return;
        }
        
        checkIfDateIsInTheFuture(calendar.getTime());
        
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
    
    public float getRating()
    {
        return getOptionalSleepSession()
                .map(SleepSession::getRating)
                .orElse(0f);
    }
    
    public void setRating(float rating)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> { sleepSession.setRating(rating); });
    }
    
    public List<InterruptionListItem> getInterruptionListItems()
    {
        return getOptionalSleepSession()
                .map(sleepSession -> {
                    Interruptions interruptions = sleepSession.getInterruptions();
                    return interruptions == null || interruptions.isEmpty() ?
                            new ArrayList<InterruptionListItem>() :
                            interruptions.asList().stream()
                                    .map(ConvertInterruption::toListItem)
                                    .collect(Collectors.toList());
                })
                .orElse(new ArrayList<>());
    }
    
    public boolean hasNoInterruptions()
    {
        return getOptionalSleepSession()
                .map(SleepSession::hasNoInterruptions)
                .orElse(true);
    }
    
    public String getInterruptionsCountText()
    {
        return getOptionalSleepSession()
                .map(sleepSession -> InterruptionFormatting.formatInterruptionsCount(
                        sleepSession.hasNoInterruptions() ? null :
                                sleepSession.getInterruptions().asList()))
                .orElse("0");
    }
    
    public String getInterruptionsTotalTimeText()
    {
        return getOptionalSleepSession()
                .map(sleepSession -> InterruptionFormatting.formatDuration(
                        sleepSession.hasNoInterruptions() ? 0 :
                                sleepSession.getInterruptions().getTotalDuration()))
                .orElseGet(() -> InterruptionFormatting.formatDuration(0));
    }


//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Throw a FutureDateTimeException if the date is in the future.
     */
    private void checkIfDateIsInTheFuture(Date date)
    {
        if (mTimeUtils.getNow().getTime() < date.getTime()) {
            throw new FutureDateTimeException(date.toString());
        }
    }
    
    
    /**
     * Note: changes made to the sleep session from this method do not notify observers. Use {@link
     * #notifySessionChanged()} in this case.
     */
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
