package com.rbraithwaite.sleepapp.ui.common.views.tag_selector;

import com.rbraithwaite.sleepapp.core.models.Tag;

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
    public void toUiData_nullInput()
    {
        assertThat(ConvertTag.toUiData(null), is(nullValue()));
    }
    
    @Test
    public void toUiData_positiveInput()
    {
        Tag testTag = new Tag(2, "test");
        TagUiData tagUiData = ConvertTag.toUiData(testTag);
        
        assertThat(tagUiData.tagId, is(testTag.getTagId()));
        assertThat(tagUiData.text, is(equalTo(testTag.getText())));
    }
    
    @Test
    public void fromUiData_nullInput()
    {
        assertThat(ConvertTag.fromUiData(null), is(nullValue()));
    }
    
    @Test
    public void fromUiData_positiveInput()
    {
        TagUiData tagUiData = new TagUiData(2, "test");
        Tag tag = ConvertTag.fromUiData(tagUiData);
        
        assertThat(tag.getTagId(), is(tagUiData.tagId));
        assertThat(tag.getText(), is(equalTo(tagUiData.text)));
    }
}
