package com.rbraithwaite.sleepapp.core.models.overlap_checker;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;

import java.util.Date;

public class SleepSessionOverlapChecker
        extends SessionOverlapChecker<SleepSession>
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
// overrides
//*********************************************************

    @Override
    protected SleepSession getFirstStartingBefore(Date date)
    {
        return mSleepSessionRepository.getFirstSleepSessionStartingBefore(date.getTime());
    }
    
    @Override
    protected SleepSession getFirstStartingAfter(Date date)
    {
        return mSleepSessionRepository.getFirstSleepSessionStartingAfter(date.getTime());
    }
    
    @Override
    protected boolean isDistinct(
            SleepSession session, SleepSession possibleOverlap)
    {
        return session.getId() != possibleOverlap.getId();
    }
}
