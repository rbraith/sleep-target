package com.rbraithwaite.sleepapp.core.models;

public class Tag
{
//*********************************************************
// private properties
//*********************************************************

    private int mTagId;
    private String mText;
    
//*********************************************************
// constructors
//*********************************************************

    public Tag(int tagId, String text)
    {
        mTagId = tagId;
        mText = text;
    }
    
    public Tag(String text)
    {
        this(0, text);
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + mTagId;
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        Tag tag = (Tag) o;
        return mTagId == tag.mTagId;
    }
    
//*********************************************************
// api
//*********************************************************

    public int getTagId()
    {
        return mTagId;
    }
    
    public String getText()
    {
        return mText;
    }
    
    public void setText(String text)
    {
        mText = text;
    }
}
