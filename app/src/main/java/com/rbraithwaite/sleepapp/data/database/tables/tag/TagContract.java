package com.rbraithwaite.sleepapp.data.database.tables.tag;

import android.provider.BaseColumns;

public class TagContract
{
//*********************************************************
// public constants
//*********************************************************

    public static final String TABLE_NAME = "tags";
    
//*********************************************************
// public helpers
//*********************************************************

    public static class Columns
    {
        public static final String ID = BaseColumns._ID;
        public static final String TAG_TEXT = "tag_text";
    }
    
//*********************************************************
// constructors
//*********************************************************

    private TagContract() {/* No instantiation */}
}
