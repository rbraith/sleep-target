package com.rbraithwaite.sleepapp.data.repositories.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.ArrayList;
import java.util.List;

public class ConvertSleepSession
{
//*********************************************************
// api
//*********************************************************

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
        return entity;
    }
    
    public static SleepSession fromEntity(SleepSessionEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        return new SleepSession(
                entity.id,
                entity.startTime,
                entity.duration);
    }
    
    // REFACTOR [21-03-24 11:01PM] -- This doesn't quite fit with the singular class name, find
    //  another place for this logic?
    public static List<SleepSession> fromEntities(List<SleepSessionEntity> entities)
    {
        // List.stream() requires api 24+ :/
        List<SleepSession> result = new ArrayList<>();
        for (SleepSessionEntity entity : entities) {
            result.add(ConvertSleepSession.fromEntity(
                    entity));
        }
        return result;
    }
}
