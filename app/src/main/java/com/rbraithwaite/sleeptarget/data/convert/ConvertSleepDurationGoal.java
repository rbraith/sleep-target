/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

import com.rbraithwaite.sleeptarget.core.models.SleepDurationGoal;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;

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
