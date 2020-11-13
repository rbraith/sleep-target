package com.rbraithwaite.sleepapp.data.database.tables.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;

import org.junit.experimental.theories.PotentialAssignment;

import java.util.Date;
import java.util.List;

@Dao
public abstract class SleepSessionDao {
    // atm this long retval is only useful for tests
    @Insert
    public abstract long addSleepSession(SleepSessionEntity sleepSession);
}
