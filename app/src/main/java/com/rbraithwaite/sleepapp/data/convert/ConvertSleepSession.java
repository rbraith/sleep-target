package com.rbraithwaite.sleepapp.data.convert;

import android.util.Log;

import com.rbraithwaite.sleepapp.core.models.Interruptions;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithExtras;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;

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
}
