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

package com.rbraithwaite.sleeptarget.ui.session_archive;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.ui.session_archive.convert.ConvertSessionArchiveListItem;
import com.rbraithwaite.sleeptarget.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleeptarget.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionArchiveFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionRepository mSleepSessionRepository;
    private TimeUtils mTimeUtils;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragViewMod";

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionArchiveFragmentViewModel(SleepSessionRepository sleepSessionRepository)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public void addSleepSession(SleepSessionWrapper sleepSession)
    {
        // SMELL [21-05-10 4:19PM] -- It feels wrong to be using a SleepSession model here -
        //  SessionDataFragment should be storing its data in a POJO instead?
        SleepSession model = sleepSession.getModel();
    
        Interruptions interruptions = model.getInterruptions();
        List<Interruption> interruptionList = interruptions == null ?
                new ArrayList<>() : interruptions.asList();
        
        // REFACTOR [21-05-10 3:46PM] -- extract this conversion logic.
        SleepSessionRepository.NewSleepSessionData newSleepSession =
                new SleepSessionRepository.NewSleepSessionData(
                        model.getStart(),
                        model.getEnd(),
                        model.getDurationMillis(),
                        model.getAdditionalComments(),
                        model.getMood(),
                        model.getTags().stream().map(Tag::getTagId).collect(Collectors.toList()),
                        interruptionList,
                        model.getRating());
        
        mSleepSessionRepository.addSleepSession(newSleepSession);
    }
    
    public void updateSleepSession(SleepSessionWrapper sleepSession)
    {
        mSleepSessionRepository.updateSleepSession(sleepSession.getModel());
    }
    
    public int deleteSession(SleepSessionWrapper sessionToDelete)
    {
        int id = sessionToDelete.getModel().getId();
        mSleepSessionRepository.deleteSleepSession(id);
        return id;
    }
    
    // REFACTOR [21-07-23 2:27PM] -- why is this LiveData?
    public LiveData<SleepSessionWrapper> getInitialAddSessionData()
    {
        // REFACTOR [21-01-5 9:14PM] -- consider making this lazy init & storing the value in a
        //  field (avoid re-instantiation of the mapped LiveData?)
        // REFACTOR [21-03-10 8:29PM] -- Returning a LiveData here is legacy behaviour, due to
        //  the sleep sessions previously using wake-time & sleep duration goal data which needed
        //  to be retrieved asynchronously from a CurrentGoalsRepository.
        return new MutableLiveData<>(new SleepSessionWrapper(new SleepSession(
                mTimeUtils.getNow(),
                0)));
    }
    
    public LiveData<SleepSessionWrapper> getSleepSession(int id)
    {
        return Transformations.map(
                mSleepSessionRepository.getSleepSession(id),
                SleepSessionWrapper::new);
    }
    
    public LiveData<List<SessionArchiveListItem>> getAllListItems()
    {
        return Transformations.map(
                mSleepSessionRepository.getAllSleepSessions(),
                sleepSessions -> sleepSessions.stream()
                        .map(ConvertSessionArchiveListItem::fromSleepSession)
                        .collect(Collectors.toList()));
    }
    
    // SMELL [21-07-2 12:52AM] -- this algo generally feels kind of clunky, revisit this &
    //  search for a more elegant solution maybe.

//*********************************************************
// protected api
//*********************************************************

    // REFACTOR [21-03-5 12:52AM] -- consider ctor injecting this instead? the main reason I'm using
    //  a factory method here is so that I didn't need to update the test class.
    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
