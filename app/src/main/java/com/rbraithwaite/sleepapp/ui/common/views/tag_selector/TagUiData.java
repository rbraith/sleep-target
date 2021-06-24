package com.rbraithwaite.sleepapp.ui.common.views.tag_selector;

public class TagUiData
{
//*********************************************************
// public properties
//*********************************************************

    public String text;
    public int tagId;

//*********************************************************
// constructors
//*********************************************************

    public TagUiData(int tagId, String text)
    {
        this.tagId = tagId;
        this.text = text;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int hash = 7;
        int prime = 13;
        hash = prime * hash + tagId;
        hash = prime * hash + (text == null ? 0 : text.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        TagUiData tagUiData = (TagUiData) o;
        return tagId == tagUiData.tagId &&
               ((text == null && tagUiData.text == null) ||
                (text != null && text.equals(tagUiData.text)));
    }
}
