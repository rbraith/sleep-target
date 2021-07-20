package com.rbraithwaite.sleepapp.test_utils.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.data.convert.ConvertInterruption;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepDurationGoal;
import com.rbraithwaite.sleepapp.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleepapp.data.convert.ConvertTag;
import com.rbraithwaite.sleepapp.data.convert.ConvertWakeTimeGoal;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.AssertionFailed;
import com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils.ValueAssertions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            LiveData<List<SleepSessionEntity>> sessions =
                    mOwner.mDatabase.getSleepSessionDao().getAllSleepSessions();
            TestUtils.activateInstrumentationLiveData(sessions);
            
            assertThat(sessions.getValue().size(), is(equalTo(count)));
        }
        
        public void interruptionCountIs(int count)
        {
            List<SleepInterruptionEntity> interruptions =
                    mOwner.mDatabase.getSleepInterruptionDao().getAll();
            
            assertThat(interruptions.size(), is(count));
        }
        
        public ValueAssertions<SleepInterruptionEntity> interruptionWithId(int id)
        {
            List<SleepInterruptionEntity> interruptions =
                    mOwner.mDatabase.getSleepInterruptionDao().getAll();
            
            // REFACTOR [21-07-9 12:42AM] -- this would be easier w/ just a getInterruption method
            //  in the dao lol.
            for (SleepInterruptionEntity interruption : interruptions) {
                if (interruption.id == id) {
                    return new ValueAssertions<>(interruption);
                }
            }
            throw new AssertionFailed(String.format(Locale.ROOT,
                                                    "No interruption found with id: %d",
                                                    id));
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

    // REFACTOR [21-05-14 3:58PM] -- This duplicates the SleepSessionRepository code, it
    //  would be better to use that repo directly
    //  see https://developer.android.com/training/dependency-injection/hilt-testing#testing
    //  -dependencies.
    public void addSleepSession(SleepSession sleepSession)
    {
        // make sure the tags exist in the db before adding the sleep session w/ tags
        for (Tag tag : sleepSession.getTags()) {
            mDatabase.getTagDao().addTag(ConvertTag.toEntity(tag));
        }
        
        List<SleepInterruptionEntity> interruptionEntities = new ArrayList<>();
        if (sleepSession.getInterruptions() != null) {
            interruptionEntities = sleepSession.getInterruptions().asList().stream()
                    .map(ConvertInterruption::toEntity)
                    .collect(Collectors.toList());
        }
        
        mDatabase.getSleepSessionDao().addSleepSessionWithExtras(
                ConvertSleepSession.toEntity(sleepSession),
                sleepSession.getTags().stream()
                        .map(Tag::getTagId).collect(Collectors.toList()),
                interruptionEntities);
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
