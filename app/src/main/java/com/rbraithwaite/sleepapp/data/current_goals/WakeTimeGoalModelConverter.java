package com.rbraithwaite.sleepapp.data.current_goals;

import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;

public class WakeTimeGoalModelConverter
{
//*********************************************************
// constructors
//*********************************************************

    private WakeTimeGoalModelConverter() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    // REFACTOR [21-03-9 2:14AM] -- should this return an unset model if its null?
    public static WakeTimeGoalModel convertEntityToModel(WakeTimeGoalEntity entity)
    {
        if (entity == null) {
            return null;
        }
        if (entity.wakeTimeGoal == WakeTimeGoalEntity.NO_GOAL) {
            return WakeTimeGoalModel.createWithNoGoal(entity.editTime);
        }
        return new WakeTimeGoalModel(entity.editTime, entity.wakeTimeGoal);
    }
    
    public static WakeTimeGoalEntity convertModelToEntity(WakeTimeGoalModel model)
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
