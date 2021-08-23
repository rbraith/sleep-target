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

package com.rbraithwaite.sleeptarget.test_utils;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.ui.common.views.tag_selector.TagUiData;

public class TestEqualities
{
//*********************************************************
// constructors
//*********************************************************

    private TestEqualities() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static boolean TagUiData_equals_Tag(TagUiData uiData, Tag tag)
    {
        return uiData.tagId == tag.getTagId() &&
               uiData.text.equals(tag.getText());
    }
    
    public static boolean SleepSession_equals_Entity(
            SleepSession session,
            SleepSessionEntity entity)
    {
        return entity.id == session.getId() &&
               entity.startTime.equals(session.getStart()) &&
               entity.endTime.equals(session.getEnd()) &&
               entity.duration == session.getDurationMillis() &&
               ((entity.additionalComments == null && session.getAdditionalComments() == null) ||
                entity.additionalComments.equals(session.getAdditionalComments())) &&
               ((entity.moodIndex == null && session.getMood() == null) ||
                entity.moodIndex == session.getMood().asIndex()) &&
               entity.rating == session.getRating();
    }
}
