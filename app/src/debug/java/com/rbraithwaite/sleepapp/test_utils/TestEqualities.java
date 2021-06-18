package com.rbraithwaite.sleepapp.test_utils;

import com.rbraithwaite.sleepapp.core.models.Tag;
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
}
