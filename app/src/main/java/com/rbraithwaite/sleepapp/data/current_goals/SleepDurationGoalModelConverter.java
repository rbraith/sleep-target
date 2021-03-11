package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;

public class SleepDurationGoalModelConverter
{
//*********************************************************
// constructors
//*********************************************************

    private SleepDurationGoalModelConverter() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static SleepDurationGoalEntity convertModelToEntity(SleepDurationGoalModel model)
    {
        if (model == null) {
            return null;
        }
        SleepDurationGoalEntity entity = new SleepDurationGoalEntity();
        // TODO [21-03-9 9:12PM] -- the id should be set as well.
        entity.editTime = model.getEditTime();
        entity.goalMinutes = model.inMinutes();
        return entity;
    }
    
    public static SleepDurationGoalModel convertEntityToModel(SleepDurationGoalEntity entity)
    {
        if (entity == null) {
            return null;
        }
        if (entity.goalMinutes == SleepDurationGoalEntity.NO_GOAL) {
            return SleepDurationGoalModel.createWithNoGoal(entity.editTime);
        }
        return new SleepDurationGoalModel(
                entity.editTime,
                entity.goalMinutes);
    }
}
