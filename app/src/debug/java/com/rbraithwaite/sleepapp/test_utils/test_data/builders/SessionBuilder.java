package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.session.Session;
import com.rbraithwaite.sleepapp.utils.TimeUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

import java.util.Date;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aDate;

public class SessionBuilder
        implements BuilderOf<Session>
{
//*********************************************************
// private properties
//*********************************************************

    private Date mStart;
    private Date mEnd;
    private long mDurationMillis;
    
//*********************************************************
// constructors
//*********************************************************

    public SessionBuilder()
    {
        mStart = aDate().build();
        mDurationMillis = 123456;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Session build()
    {
        Session session = new Session(mStart, mDurationMillis);
        if (mEnd != null) {
            session.setStart(new TimeUtils().getDateFromMillis(
                    mEnd.getTime() - mDurationMillis));
        }
        return session;
    }
    
//*********************************************************
// api
//*********************************************************

    public SessionBuilder withStart(DateBuilder start)
    {
        mStart = start.build();
        return this;
    }
    
    /**
     * This takes priority over start.
     */
    public SessionBuilder withEnd(DateBuilder end)
    {
        mEnd = end.build();
        return this;
    }
    
    public SessionBuilder withNoDuration()
    {
        return withDuration(0);
    }
    
    public SessionBuilder withDuration(int durationMillis)
    {
        mDurationMillis = durationMillis;
        return this;
    }
}
