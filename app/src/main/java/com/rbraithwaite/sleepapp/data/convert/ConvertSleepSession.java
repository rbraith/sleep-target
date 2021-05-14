package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;

import java.util.List;
import java.util.stream.Collectors;

public class ConvertSleepSession
{
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
        entity.moodIndex = model.getMood() == null ? null : model.getMood().toIndex();
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
        return entities.stream()
                .map(ConvertSleepSession::fromEntity)
                .collect(Collectors.toList());
    }
    
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
