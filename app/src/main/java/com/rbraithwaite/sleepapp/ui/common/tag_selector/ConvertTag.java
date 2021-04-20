package com.rbraithwaite.sleepapp.ui.common.tag_selector;

import com.rbraithwaite.sleepapp.core.models.Tag;

public class ConvertTag
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertTag() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static TagUiData toUiData(Tag tag)
    {
        if (tag == null) {
            return null;
        }
        
        return new TagUiData(tag.getTagId(), tag.getText());
    }
    
    public static Tag fromUiData(TagUiData tagUiData)
    {
        if (tagUiData == null) {
            return null;
        }
        
        return new Tag(tagUiData.tagId, tagUiData.text);
    }
}
