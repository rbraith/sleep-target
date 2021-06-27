package com.rbraithwaite.sleepapp.test_utils.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepDurationGoal;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleepapp.data.convert.ConvertTag;
import com.rbraithwaite.sleepapp.data.convert.ConvertWakeTimeGoal;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class DatabaseTestDriver
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase mDatabase;

//*********************************************************
// public constants
//*********************************************************

    public final Assertions assertThat;

//*********************************************************
// public helpers
//*********************************************************

    public static class Assertions
    {
        private DatabaseTestDriver mOwner;
        
        private Assertions(DatabaseTestDriver owner)
        {
            mOwner = owner;
        }
        
        public void sleepSessionCountIs(int count)
        {
            LiveData<List<Integer>> sessionIds =
                    mOwner.mDatabase.getSleepSessionDao().getAllSleepSessionIds();
            TestUtils.activateInstrumentationLiveData(sessionIds);
            
            assertThat(sessionIds.getValue().size(), is(equalTo(count)));
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public DatabaseTestDriver()
    {
        assertThat = new Assertions(this);
        mDatabase = findDatabase();
    }

//*********************************************************
// api
//*********************************************************

    public void addSleepSession(SleepSession sleepSession)
    {
        // make sure the tags exist in the db before adding the sleep session w/ tags
        for (Tag tag : sleepSession.getTags()) {
            mDatabase.getTagDao().addTag(ConvertTag.toEntity(tag));
        }
        
        // REFACTOR [21-05-14 3:58PM] -- This duplicates the SleepSessionRepository code, it
        //  would be
        //  better to use that repo directly
        //  see https://developer.android.com/training/dependency-injection/hilt-testing#testing
        //  -dependencies.
        mDatabase.getSleepSessionDao().addSleepSessionWithTags(
                ConvertSleepSession.toEntity(sleepSession),
                sleepSession.getTags().stream().map(Tag::getTagId).collect(Collectors.toList()));
    }
    
    public void setWakeTimeGoal(WakeTimeGoal wakeTimeGoal)
    {
        mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(
                ConvertWakeTimeGoal.toEntity(wakeTimeGoal));
    }
    
    public void setSleepDurationGoal(SleepDurationGoal sleepDurationGoal)
    {
        mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(
                ConvertSleepDurationGoal.toEntity(sleepDurationGoal));
    }

//*********************************************************
// private methods
//*********************************************************

    private SleepAppDatabase findDatabase()
    {
        return Room.databaseBuilder(
                TestUtils.getContext(),
                SleepAppDatabase.class,
                SleepAppDatabase.NAME).build();
    }
}
