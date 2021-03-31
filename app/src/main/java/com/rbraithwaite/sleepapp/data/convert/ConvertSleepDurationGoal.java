package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.SleepDurationGoal;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;

public class ConvertSleepDurationGoal
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertSleepDurationGoal() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static SleepDurationGoalEntity toEntity(SleepDurationGoal model)
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
    
    public static SleepDurationGoal fromEntity(SleepDurationGoalEntity entity)
    {
        if (entity == null) {
            return null;
        }
        if (entity.goalMinutes == SleepDurationGoalEntity.NO_GOAL) {
            return SleepDurationGoal.createWithNoGoal(entity.editTime);
        }
        return new SleepDurationGoal(
                entity.editTime,
                entity.goalMinutes);
    }
}
