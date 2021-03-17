package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.ArrayList;
import java.util.List;

public class SleepSessionModelConverter
{
//*********************************************************
// api
//*********************************************************

    public static SleepSessionEntity convertModelToEntity(SleepSessionModel model)
    {
        if (model == null) {
            return null;
        }
        SleepSessionEntity entity = new SleepSessionEntity();
        entity.id = model.getId();
        entity.startTime = model.getStart();
        entity.endTime = model.getEnd();
        entity.duration = model.getDuration();
        return entity;
    }
    
    public static SleepSessionModel convertEntityToModel(SleepSessionEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        return new SleepSessionModel(
                entity.id,
                entity.startTime,
                entity.duration);
    }
    
    public static List<SleepSessionModel> convertAllEntitiesToModels(List<SleepSessionEntity> entities)
    {
        // List.stream() requires api 24+ :/
        List<SleepSessionModel> result = new ArrayList<>();
        for (SleepSessionEntity entity : entities) {
            result.add(SleepSessionModelConverter.convertEntityToModel(
                    entity));
        }
        return result;
    }
}
