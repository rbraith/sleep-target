package com.rbraithwaite.sleepapp.core.models.overlap_checker;

import com.rbraithwaite.sleepapp.core.models.session.Session;

import java.util.Date;

public abstract class SessionOverlapChecker<SessionType extends Session>
{
//*********************************************************
// abstract
//*********************************************************

    
    /**
     * @return The first session starting before the provided date, or else null if there are none.
     */
    protected abstract SessionType getFirstStartingBefore(Date date);
    /**
     * @return The first session starting after the provided date, or else null if there are none.
     */
    protected abstract SessionType getFirstStartingAfter(Date date);
    
    // SMELL [21-07-31 9:05PM] -- This isn't great - a better solution might be to have a
    //  SessionWithId that SleepSession and Interruption derive from, and SessionType extends that,
    //  or better yet a base DomainEntity class that has id data.
    
    /**
     * Return whether or not the provided session is distinct with the possibly-overlapping session
     * (e.g. they have different ids).
     */
    protected abstract boolean isDistinct(SessionType session, SessionType possibleOverlap);

//*********************************************************
// api
//*********************************************************

    
    /**
     * If there is any overlap with a session, return the offending session, otherwise return null.
     * "Exclusive" means skipping the session which matches the id of the provided session (this is
     * useful for checking updated sessions - you don't care if they overlap with their old
     * selves).
     */
    public SessionType checkForOverlapExclusive(SessionType session)
    {
        // Check that this start doesn't fall within the previous existing session's start & end,
        // and that the next existing session's start doesn't fall within this session's start &
        // end.
        
        // check behind
        long startMillis = session.getStart().getTime();
        SessionType possibleOverlapBehind = getFirstStartingBefore(session.getStart());
        
        // First check for id match - that means session is an update of possibleOverlapBehind
        // and any overlap should be ignored. possibleOverlapBehind will be null if this
        // session is the earliest.
        if (possibleOverlapBehind != null &&
            isDistinct(session, possibleOverlapBehind) &&
            session.getStart().getTime() <= possibleOverlapBehind.getEnd().getTime()) {
            // this session is overlapping the previous session
            return possibleOverlapBehind;
        }
        
        // check ahead
        SessionType possibleOverlapAhead = getFirstStartingAfter(session.getStart());
        
        // If the existing session is this session, find instead the next one after that. Otherwise
        // it's possible to have an overlap with that next session. Ahead will be null if this
        // sleep session is the latest.
        if (possibleOverlapAhead != null &&
            !isDistinct(session, possibleOverlapAhead)) {
            possibleOverlapAhead = getFirstStartingAfter(possibleOverlapAhead.getEnd());
        }
        
        // still need to re-check the ids here, as its possible the second session also happens
        // to be this session (if this session was zero-duration and its end wasn't edited, then
        // that end would equal its existing start)
        if (possibleOverlapAhead != null &&
            isDistinct(session, possibleOverlapAhead) &&
            possibleOverlapAhead.getStart().getTime() <= session.getEnd().getTime()) {
            // this session is overlapping with the next session
            return possibleOverlapAhead;
        }
        
        // no overlaps
        return null;
    }
}
