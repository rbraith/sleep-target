package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.session_archive.convert.ConvertSessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_details.SessionDetailsFormatting;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
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
    private final Executor mExecutor;

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

    @ViewModelInject
    public SessionArchiveFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            Executor executor)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mExecutor = executor;
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
        
        // REFACTOR [21-05-10 3:46PM] -- extract this conversion logic.
        SleepSessionRepository.NewSleepSessionData newSleepSession =
                new SleepSessionRepository.NewSleepSessionData(
                        model.getStart(),
                        model.getEnd(),
                        model.getDurationMillis(),
                        model.getAdditionalComments(),
                        model.getMood(),
                        model.getTags().stream().map(Tag::getTagId).collect(Collectors.toList()),
                        // TODO [21-07-8 11:58PM] -- this new list is a placeholder so that things
                        //  don't break - SleepSession needs a list of its interruptions (this
                        //  feature will be a part of adding interruptions manually in the
                        //  details screen).
                        new ArrayList<>(),
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
    
    /**
     * This will throw an OverlappingSessionException if there is an overlapping session. This will
     * return false if the check failed for a reason related to an execution problem, or true if the
     * check passed.
     */
    public boolean checkResultForSessionOverlap(SleepSessionWrapper result)
    {
        FutureTask<SleepSession> overlapTask = new FutureTask<>(
                () -> checkSleepSessionForOverlap(result.getModel()));
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
                    // SMELL [21-07-2 12:16AM] -- its kinda weird to be using
                    //  SessionDetailsFormatting
                    //  here, maybe I should move formatFullDate into SessionArchiveFormatting.
                    SessionDetailsFormatting.formatFullDate(overlappingSession.getStart()),
                    SessionDetailsFormatting.formatFullDate(overlappingSession.getEnd()));
        }
        return true;
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


//*********************************************************
// private methods
//*********************************************************

    
    /**
     * If there is any overlap, return the offending sleep session, otherwise return null. (Since
     * this is accessing the repo, this method should be called only from a background thread.)
     */
    private SleepSession checkSleepSessionForOverlap(SleepSession sleepSession)
    {
        // Check that this start doesn't fall within the previous existing session's start & end,
        // and that the next existing session's start doesn't fall within this session's start &
        // end.
        
        // check behind
        long startMillis = sleepSession.getStart().getTime();
        SleepSession possibleOverlapBehind =
                mSleepSessionRepository.getFirstSleepSessionStartingBefore(
                        startMillis);
        
        // First check for id match - that means sleepSession is an edit of possibleOverlapBehind
        // and any overlap should be ignored. possibleOverlapBehind will be null if this sleep
        // session is the earliest.
        if (possibleOverlapBehind != null &&
            sleepSession.getId() != possibleOverlapBehind.getId() &&
            startMillis <= possibleOverlapBehind.getEnd().getTime()) {
            // this session is overlapping the previous session
            return possibleOverlapBehind;
        }
        
        // check ahead
        SleepSession possibleOverlapAhead =
                mSleepSessionRepository.getFirstSleepSessionStartingAfter(
                        startMillis);
        
        // If the existing session is this session, find instead the next one after that. Otherwise
        // it's possible to have an overlap with that next session. Ahead will be null if this
        // sleep session is the latest.
        if (possibleOverlapAhead != null &&
            sleepSession.getId() == possibleOverlapAhead.getId()) {
            possibleOverlapAhead = mSleepSessionRepository.getFirstSleepSessionStartingAfter(
                    possibleOverlapAhead.getEnd().getTime());
        }
        
        // still need to re-check the ids here, as its possible the second session also happens
        // to be this session (if this session was zero-duration and its end wasn't edited, then
        // that end would equal its existing start)
        if (possibleOverlapAhead != null &&
            sleepSession.getId() != possibleOverlapAhead.getId() &&
            possibleOverlapAhead.getStart().getTime() <= sleepSession.getEnd().getTime()) {
            // this session is overlapping with the next session
            return possibleOverlapAhead;
        }
        
        // no overlaps
        return null;
    }
}
