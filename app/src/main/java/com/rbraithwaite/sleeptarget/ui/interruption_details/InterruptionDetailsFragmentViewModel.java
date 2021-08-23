/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.interruption_details;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.ui.common.views.details_fragment.DetailsFragmentViewModel;
import com.rbraithwaite.sleeptarget.ui.common.views.session_times.SessionTimesViewModel;
import com.rbraithwaite.sleeptarget.utils.LiveDataUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Date;
import java.util.Optional;

public class InterruptionDetailsFragmentViewModel
        extends DetailsFragmentViewModel<InterruptionDetailsData>
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Interruption> mInterruption = new MutableLiveData<>();
    private SleepSession mParentSleepSession;
    private TimeUtils mTimeUtils;
    private boolean mInitialized = false;
    
//*********************************************************
// public helpers
//*********************************************************

    public static class OverlappingInterruptionException
            extends RuntimeException
    {
        public final String overlappedStart;
        public final String overlappedEnd;
        
        public OverlappingInterruptionException(String overlappedStart, String overlappedEnd)
        {
            this.overlappedStart = overlappedStart;
            this.overlappedEnd = overlappedEnd;
        }
    }
    
    public static class OutOfBoundsInterruptionException
            extends RuntimeException
    {
        public final String sessionStart;
        public final String sessionEnd;
        
        public OutOfBoundsInterruptionException(String sessionStart, String sessionEnd)
        {
            this.sessionStart = sessionStart;
            this.sessionEnd = sessionEnd;
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public InterruptionDetailsFragmentViewModel(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
    
//*********************************************************
// overrides
//*********************************************************

    // TEST NEEDED [21-07-27 12:22AM] -- .
    @Override
    public InterruptionDetailsData getResult()
    {
        return new InterruptionDetailsData(mInterruption.getValue(), mParentSleepSession);
    }
    
    // TEST NEEDED [21-07-27 12:22AM] -- .
    // REFACTOR [21-07-23 5:44PM] -- the logic in these overrides duplicates
    //  SessionDetailsFragmentViewModel - should I move this logic to DetailsFragmentViewModel
    //  somehow?
    @Override
    public void initData(InterruptionDetailsData data)
    {
        if (!mInitialized) {
            mInterruption.setValue(data.getInterruption());
            mParentSleepSession = data.getParentSleepSession();
            mInitialized = true;
        }
    }
    
    // TEST NEEDED [21-07-27 12:22AM] -- .
    @Override
    public void clearData()
    {
        mInterruption.setValue(null);
        mInitialized = false;
    }
    
//*********************************************************
// api
//*********************************************************

    public SessionTimesViewModel getSessionTimesViewModel()
    {
        return new SessionTimesViewModel(getOptionalInterruption().orElse(null), mTimeUtils);
    }
    
    public LiveData<String> getReason()
    {
        return Transformations.map(mInterruption, interruption -> {
            String reason =
                    Optional.ofNullable(interruption).map(Interruption::getReason).orElse(null);
            return InterruptionDetailsFormatting.formatReason(reason);
        });
    }
    
    public void setReason(String reason)
    {
        getOptionalInterruption().ifPresent(interruption -> {
            // This change does not need to refresh the LiveData
            interruption.setReason(reason);
        });
    }
    
    public void setStart(Date start)
    {
        getOptionalInterruption().ifPresent(interruption -> {
            interruption.setStartFixed(start);
            refreshInterruption();
        });
    }
    
    public void setEnd(Date end)
    {
        getOptionalInterruption().ifPresent(interruption -> {
            interruption.setEndFixed(end);
            refreshInterruption();
        });
    }
    
    public void checkForValidResult()
    {
        // REFACTOR [21-07-31 8:26PM] -- Should this be async, like in
        //  SessionDetailsFragmentViewModel.checkResultForSessionOverlap?
        getOptionalInterruption().ifPresent(interruption -> {
            checkForInterruptionOutOfSessionBounds(interruption);
            checkForInterruptionOverlap(interruption);
        });
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void checkForInterruptionOverlap(Interruption interruption)
    {
        Interruption overlapping =
                mParentSleepSession.checkForInterruptionOverlapExclusive(interruption);
        if (overlapping != null) {
            throw new OverlappingInterruptionException(
                    InterruptionDetailsFormatting.formatFullDate(overlapping.getStart()),
                    InterruptionDetailsFormatting.formatFullDate(overlapping.getEnd()));
        }
    }
    
    private void checkForInterruptionOutOfSessionBounds(Interruption interruption)
    {
        if (interruption.getStart().getTime() < mParentSleepSession.getStart().getTime() ||
            interruption.getEnd().getTime() > mParentSleepSession.getEnd().getTime()) {
            throw new OutOfBoundsInterruptionException(
                    InterruptionDetailsFormatting.formatFullDate(mParentSleepSession.getStart()),
                    InterruptionDetailsFormatting.formatFullDate(mParentSleepSession.getEnd()));
        }
    }
    
    private Optional<Interruption> getOptionalInterruption()
    {
        return Optional.ofNullable(mInterruption.getValue());
    }
    
    private void refreshInterruption()
    {
        LiveDataUtils.refresh(mInterruption);
    }
}
