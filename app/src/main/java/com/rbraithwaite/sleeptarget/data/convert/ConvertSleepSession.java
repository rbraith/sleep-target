/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.data.convert;

import android.util.Log;

import com.rbraithwaite.sleeptarget.core.models.Interruptions;
import com.rbraithwaite.sleeptarget.core.models.Mood;
import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.data.SleepSessionWithExtras;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.data.SleepSessionWithTags;

import java.util.List;
import java.util.stream.Collectors;

public class ConvertSleepSession
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "ConvertSleepSession";

//*********************************************************
// constructors
//*********************************************************

    private ConvertSleepSession() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    // TEST NEEDED [21-05-14 2:24PM] -- update tests with rating.
    public static SleepSessionEntity toEntity(SleepSession model)
    {
        if (model == null) {
            return null;
        }
        SleepSessionEntity entity = new SleepSessionEntity();
        entity.id = model.getId();
        entity.startTime = model.getStart();
        entity.endTime = model.getEnd();
        entity.duration = model.getDurationMillis();
        entity.additionalComments = model.getAdditionalComments();
        entity.moodIndex = model.getMood() == null ? null : model.getMood().asIndex();
        entity.rating = model.getRating();
        return entity;
    }
    
    public static SleepSession fromEntity(SleepSessionEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        SleepSession sleepSession = new SleepSession(
                entity.id,
                entity.startTime,
                entity.duration,
                entity.additionalComments,
                Mood.fromIndex(entity.moodIndex));
        
        // REFACTOR [21-05-11 11:54PM] -- use a builder here.
        sleepSession.setRating(entity.rating);
        
        return sleepSession;
    }
    
    // REFACTOR [21-03-24 11:01PM] -- This doesn't quite fit with the singular class name, find
    //  another place for this logic?
    public static List<SleepSession> fromEntities(List<SleepSessionEntity> entities)
    {
        Log.d(TAG, "fromEntities: entity count = " + entities.size());
        return entities.stream()
                .map(ConvertSleepSession::fromEntity)
                .collect(Collectors.toList());
    }
    
    // TEST NEEDED [21-07-20 5:22PM] -- .
    public static SleepSession fromEntityWithExtras(SleepSessionWithExtras entityWithExtras)
    {
        if (entityWithExtras == null) {
            return null;
        }
        
        SleepSession sleepSession = fromEntity(entityWithExtras.sleepSession);
        
        sleepSession.setTags(entityWithExtras.tags.stream()
                                     .map(ConvertTag::fromEntity)
                                     .collect(Collectors.toList()));
        
        sleepSession.setInterruptions(new Interruptions(
                entityWithExtras.interruptions.stream()
                        .map(ConvertInterruption::fromEntity)
                        .collect(Collectors.toList())));
        
        return sleepSession;
    }
    
    // REFACTOR [21-07-20 2:42PM] -- replace with fromEntityWithExtras.
    @Deprecated
    public static SleepSession fromEntityWithTags(SleepSessionWithTags entityWithTags)
    {
        if (entityWithTags == null) {
            return null;
        }
        
        SleepSession sleepSession = fromEntity(entityWithTags.sleepSession);
        sleepSession.setTags(entityWithTags.tags.stream()
                                     .map(ConvertTag::fromEntity)
                                     .collect(Collectors.toList()));
        
        return sleepSession;
    }
    
    public static List<SleepSession> listFromEntityWithExtrasList(List<SleepSessionWithExtras> sleepSessionsWithExtras)
    {
        return sleepSessionsWithExtras.stream()
                .map(ConvertSleepSession::fromEntityWithExtras)
                .collect(Collectors.toList());
    }
}
