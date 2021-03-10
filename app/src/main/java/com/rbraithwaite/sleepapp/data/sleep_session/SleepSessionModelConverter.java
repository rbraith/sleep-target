package com.rbraithwaite.sleepapp.data.sleep_session;

import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

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
        entity.wakeTimeGoal = model.getWakeTimeGoal();
        SleepDurationGoalModel sleepDurationGoal = model.getSleepDurationGoal();
        entity.sleepDurationGoalMinutes =
                sleepDurationGoal == null ? null : sleepDurationGoal.inMinutes();
        return entity;
    }
    
    public static SleepSessionModel convertEntityToModel(SleepSessionEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        SleepDurationGoalModel sleepDurationGoal = (entity.sleepDurationGoalMinutes == null) ?
                SleepDurationGoalModel.createWithNoGoal() :
                new SleepDurationGoalModel(entity.sleepDurationGoalMinutes);
        
        return new SleepSessionModel(
                entity.id,
                entity.startTime,
                entity.duration,
                entity.wakeTimeGoal,
                sleepDurationGoal);
    }
}
