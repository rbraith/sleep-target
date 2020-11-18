package com.rbraithwaite.sleepapp.data.database.views;

import androidx.room.ColumnInfo;
import androidx.room.DatabaseView;

import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionContract;

import java.util.Date;

// TODO right now this is just getting all the sleep_session columns,
//  but this will eventually have data from other tables eg comment data

// SELECT
// <sleep-session-table>.<id> AS <session-id>,
// <sleep-session-table>.<start-time> AS <start-time>,
// <sleep-session-table>.<duration> AS <duration>
// FROM
// <sleep-session-table>
@DatabaseView(
        viewName = SleepSessionDataContract.VIEW_NAME,
        //@formatter:off
        value = "SELECT " +
                SleepSessionContract.TABLE_NAME + "." + SleepSessionContract.Columns.ID +
                    " AS " + SleepSessionDataContract.Columns.SESSION_ID + ", " +
                SleepSessionContract.TABLE_NAME + "." + SleepSessionContract.Columns.START_TIME +
                    " AS " + SleepSessionDataContract.Columns.START_TIME + ", " +
                SleepSessionContract.TABLE_NAME + "." + SleepSessionContract.Columns.DURATION +
                    " AS " + SleepSessionDataContract.Columns.DURATION +
                " FROM " + SleepSessionContract.TABLE_NAME)
        //@formatter:on
public class SleepSessionData
{
//*********************************************************
// public properties
//*********************************************************

    @ColumnInfo(name = SleepSessionDataContract.Columns.SESSION_ID)
    public int id;
    
    @ColumnInfo(name = SleepSessionDataContract.Columns.START_TIME)
    public Date startTime;
    
    @ColumnInfo(name = SleepSessionDataContract.Columns.DURATION)
    public long duration;
}
