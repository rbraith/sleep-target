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
package com.rbraithwaite.sleeptarget.ui.session_details;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.models.overlap_checker.SleepSessionOverlapChecker;
import com.rbraithwaite.sleeptarget.core.models.session.Session;
import com.rbraithwaite.sleeptarget.ui.common.convert.ConvertMood;
import com.rbraithwaite.sleeptarget.ui.common.data.MoodUiData;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.ConvertInterruption;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionFormatting;
import com.rbraithwaite.sleeptarget.ui.common.interruptions.InterruptionListItem;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsFragmentViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.ConvertTag;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;
import com.rbraithwaite.sleeptarget.ui.interruption_details.InterruptionDetailsData;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

// REFACTOR [21-01-5 2:30AM] -- passing around Dates and longs for times-of-day or dates is too
//  much information. Instead there should be TimeOfDay and Date which are simple POJOs and
//  contain eg hourOfDay & minute fields. Then I can use Date & Calendar in the impls to convert
//  for storage or display
@HiltViewModel
public class SessionDetailsFragmentViewModel
        extends DetailsFragmentViewModel<SleepSessionWrapper>
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<SleepSession> mSleepSession = new MutableLiveData<>();
    private TimeUtils mTimeUtils;
    
    private SleepSessionOverlapChecker mOverlapChecker;
    private Executor mExecutor;
    
    private boolean mInitialized = false;

//*********************************************************
// public helpers
//*********************************************************

    public static class OverlappingSessionException
            extends RuntimeException
    {
        public final String start;
        public final String end;
        
        public OverlappingSessionException(String start, String end)
        {
            super(OverlappingSessionException.composeSuperMessage(start, end));
            
            this.start = start;
            this.end = end;
        }
        
        private static String composeSuperMessage(String start, String end)
        {
            return String.format(
                    Locale.CANADA,
                    "Overlap of session with start='%s' and end='%s'",
                    start, end);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    @Inject
    public SessionDetailsFragmentViewModel(
            TimeUtils timeUtils,
            SleepSessionOverlapChecker overlapChecker,
            Executor executor)
    {
        mOverlapChecker = overlapChecker;
        mExecutor = executor;
        mTimeUtils = timeUtils;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public SleepSessionWrapper getResult()
    {
        return new SleepSessionWrapper(mSleepSession.getValue());
    }
    
    /**
     * Set the data only if the view model is clear (ie no data has been set yet, or clearData was
     * called).
     */
    @Override
    public void initData(SleepSessionWrapper data)
    {
        if (!mInitialized) {
            mSleepSession.setValue(data.getModel());
            mInitialized = true;
        }
    }
    
    @Override
    public void clearData()
    {
        mSleepSession.setValue(null);
        mInitialized = false;
    }

//*********************************************************
// api
//*********************************************************

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
    
    public LiveData<List<InterruptionListItem>> getInterruptionListItems()
    {
        // OPTIMIZE [21-09-14 2:24PM] -- Right now this is refreshing the interruptions on any
        //  change to the sleep session - preferably this would only refresh if the start/end times
        //  changed.
        
        return Transformations.map(getSleepSession(), sleepSession -> {
            if (sleepSession == null) {
                return new ArrayList<>();
            }
    
            Interruptions interruptions = sleepSession.getInterruptions();
            return interruptions == null || interruptions.isEmpty() ?
                    new ArrayList<>() :
                    interruptions.asList().stream()
                            .map(interruption ->
                                         ConvertInterruption.toListItem(interruption, sleepSession))
                            .collect(Collectors.toList());
        });
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
                                sleepSession.getInterruptions()
                                        .getTotalDurationInBounds(sleepSession)))
                .orElseGet(() -> InterruptionFormatting.formatDuration(0));
    }
    
    /**
     * This will throw an OverlappingSessionException if there is an overlapping session. This will
     * return false if the check failed for a reason related to an execution problem, or true if the
     * check passed.
     */
    public boolean checkResultForSessionOverlap()
    {
        FutureTask<SleepSession> overlapTask = new FutureTask<>(
                () -> mOverlapChecker.checkForOverlapExclusive(getResult().getModel()));
        mExecutor.execute(overlapTask);
        
        SleepSession overlappingSession;
        try {
            overlappingSession = overlapTask.get();
        } catch (ExecutionException e) {
            // TODO [21-07-3 1:20AM] -- I need a proper logging system lol!
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        if (overlappingSession != null) {
            throw new OverlappingSessionException(
                    SessionDetailsFormatting.formatFullDate(overlappingSession.getStart()),
                    SessionDetailsFormatting.formatFullDate(overlappingSession.getEnd()));
        }
        return true;
    }
    
    public InterruptionDetailsData getInterruptionDetailsData(int interruptionId)
    {
        return getOptionalSleepSession()
                .map(sleepSession -> {
                    Interruption interruption = sleepSession.getInterruption(interruptionId);
                    if (interruption != null) {
                        interruption = interruption.shallowCopy();
                    }
                    return new InterruptionDetailsData(interruption, sleepSession);
                })
                .orElse(null);
    }
    
    public InterruptionDetailsData getNewInterruptionDetailsData()
    {
        return getOptionalSleepSession()
                .map(sleepSession -> new InterruptionDetailsData(
                        new Interruption(sleepSession.getStart()),
                        sleepSession))
                .orElse(null);
    }
    
    public void deleteInterruption(InterruptionDetailsData interruption)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> {
            sleepSession.deleteInterruption(interruption.getInterruption().getId());
            notifySessionChanged();
        });
    }
    
    public void setStart(Date start)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> {
            sleepSession.setStartFixed(start);
            LiveDataUtils.refresh(mSleepSession);
        });
    }
    
    public void setEnd(Date end)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> {
            sleepSession.setEndFixed(end);
            LiveDataUtils.refresh(mSleepSession);
        });
    }
    
    public void updateInterruption(InterruptionDetailsData interruptionDetailsData)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> {
            sleepSession.updateInterruption(interruptionDetailsData.getInterruption());
            notifySessionChanged();
        });
    }
    
    public void addInterruption(InterruptionDetailsData data)
    {
        getOptionalSleepSession().ifPresent(sleepSession -> {
            sleepSession.addInterruption(data.getInterruption());
            notifySessionChanged();
        });
    }
    
    public Session getSession()
    {
        return getOptionalSleepSession().orElse(null);
    }



//*********************************************************
// private methods
//*********************************************************

    
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
