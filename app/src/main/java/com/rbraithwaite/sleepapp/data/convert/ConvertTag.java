package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

public class ConvertTag
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertTag() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static Tag fromEntity(TagEntity entity)
    {
        if (entity == null) {
            return null;
        }
        
        return new Tag(entity.id, entity.text);
    }
    
    public static TagEntity toEntity(Tag tag)
    {
        if (tag == null) {
            return null;
        }
        
        TagEntity entity = new TagEntity();
        entity.id = tag.getTagId();
        entity.text = tag.getText();
        
        return entity;
    }
}
