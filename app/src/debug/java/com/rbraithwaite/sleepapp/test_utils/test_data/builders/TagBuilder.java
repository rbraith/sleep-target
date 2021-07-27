package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

public class TagBuilder
        implements BuilderOf<Tag>
{
//*********************************************************
// private properties
//*********************************************************

    private int mId;
    private String mText;
    
//*********************************************************
// constructors
//*********************************************************

    public TagBuilder()
    {
        mId = 0;
        mText = "some tag";
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Tag build()
    {
        return new Tag(mId, mText);
    }
    
//*********************************************************
// api
//*********************************************************

    public TagBuilder withId(int id)
    {
        mId = id;
        return this;
    }
    
    public TagBuilder withText(String text)
    {
        mText = text;
        return this;
    }
}
