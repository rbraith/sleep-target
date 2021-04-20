package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertTagTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void fromEntity_nullInput()
    {
        assertThat(ConvertTag.fromEntity(null), is(nullValue()));
    }
    
    @Test
    public void fromEntity_positiveInput()
    {
        TagEntity entity = new TagEntity();
        entity.id = 2;
        entity.text = "test";
        
        Tag tag = ConvertTag.fromEntity(entity);
        
        assertThat(tag.getTagId(), is(equalTo(entity.id)));
        assertThat(tag.getText(), is(equalTo(entity.text)));
    }
    
    @Test
    public void toEntity_nullInput()
    {
        assertThat(ConvertTag.toEntity(null), is(nullValue()));
    }
    
    @Test
    public void toEntity_positiveInput()
    {
        Tag tag = new Tag(2, "test");
        TagEntity entity = ConvertTag.toEntity(tag);
        
        assertThat(entity.id, is(tag.getTagId()));
        assertThat(entity.text, is(equalTo(tag.getText())));
    }
}
