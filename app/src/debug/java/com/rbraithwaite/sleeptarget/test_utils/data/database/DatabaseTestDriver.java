/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.test_utils.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Room;

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.data.convert.ConvertInterruption;
import com.rbraithwaite.sleeptarget.data.convert.ConvertSleepDurationGoal;
import com.rbraithwaite.sleeptarget.data.convert.ConvertSleepSession;
import com.rbraithwaite.sleeptarget.data.convert.ConvertTag;
import com.rbraithwaite.sleeptarget.data.convert.ConvertWakeTimeGoal;
import com.rbraithwaite.sleeptarget.data.database.AppDatabase;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.AssertionFailed;
import com.rbraithwaite.sleeptarget.test_utils.ui.assertion_utils.ValueAssertions;

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

    private AppDatabase mDatabase;

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
        // BUG [21-09-6 7:51PM] -- This actually doesn't work right now with sleep sessions that
        //  have tags - because the db is prepopulated with tags, trying to add tags w/ the
        //  same ids here causes a crash. I would need to update the Tag's id as well based on
        //  what addTag() returns.
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

    private AppDatabase findDatabase()
    {
        return Room.databaseBuilder(
                TestUtils.getContext(),
                AppDatabase.class,
                AppDatabase.NAME).build();
    }
}
