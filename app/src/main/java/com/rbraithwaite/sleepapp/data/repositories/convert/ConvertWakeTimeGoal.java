package com.rbraithwaite.sleepapp.data.repositories.convert;

import com.rbraithwaite.sleepapp.core.models.WakeTimeGoal;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;

public class ConvertWakeTimeGoal
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertWakeTimeGoal() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-03-9 2:14AM] -- should this return an unset model if its null?
    public static WakeTimeGoal fromEntity(WakeTimeGoalEntity entity)
    {
        if (entity == null) {
            return null;
        }
        if (entity.wakeTimeGoal == WakeTimeGoalEntity.NO_GOAL) {
            return WakeTimeGoal.createWithNoGoal(entity.editTime);
        }
        return new WakeTimeGoal(entity.editTime, entity.wakeTimeGoal);
    }
    
    public static WakeTimeGoalEntity toEntity(WakeTimeGoal model)
    {
        if (model == null) {
            return null;
        }
        WakeTimeGoalEntity entity = new WakeTimeGoalEntity();
        entity.editTime = model.getEditTime();
        entity.wakeTimeGoal = model.isSet() ? model.getGoalMillis() : WakeTimeGoalEntity.NO_GOAL;
        return entity;
    }
}
