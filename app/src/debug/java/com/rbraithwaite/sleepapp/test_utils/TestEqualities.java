package com.rbraithwaite.sleepapp.test_utils;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.common.views.tag_selector.TagUiData;

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
