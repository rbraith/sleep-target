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

import com.rbraithwaite.sleeptarget.core.models.WakeTimeGoal;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;

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
