package com.rbraithwaite.sleepapp.data.database.views;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

import java.util.Date;

// todo right now this is just getting all the sleep_session columns,
//  but this will eventually have data from other tables eg comment data

@DatabaseView(
        viewName = "view_sleep_session_data",
        value = "SELECT " +
                "sleep_sessions.id AS session_id, " +
                "sleep_sessions.start_time AS start_time, " +
                "sleep_sessions.duration AS duration " +
                "FROM sleep_sessions")
public class SleepSessionData
{
//*********************************************************
// public properties
//*********************************************************

    @ColumnInfo(name = "session_id")
    public int id;
    
    @ColumnInfo(name = "start_time")
    public Date startTime;
    
    @ColumnInfo(name = "duration")
    public long duration;
}
