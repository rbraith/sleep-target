package com.rbraithwaite.sleepapp.core.models;

import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;

public class SleepSessionOverlapChecker
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionRepository mSleepSessionRepository;
    
//*********************************************************
// constructors
//*********************************************************

    public SleepSessionOverlapChecker(SleepSessionRepository sleepSessionRepository)
    {
        mSleepSessionRepository = sleepSessionRepository;
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * If there is any overlap with a sleep session in the repo, return the offending sleep session,
     * otherwise return null.
     */
    public SleepSession checkForOverlap(SleepSession sleepSession)
    {
        // Check that this start doesn't fall within the previous existing session's start & end,
        // and that the next existing session's start doesn't fall within this session's start &
        // end.
        
        // check behind
        long startMillis = sleepSession.getStart().getTime();
        SleepSession possibleOverlapBehind =
                mSleepSessionRepository.getFirstSleepSessionStartingBefore(startMillis);
        
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
                mSleepSessionRepository.getFirstSleepSessionStartingAfter(startMillis);
        
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
