package com.rbraithwaite.sleepapp.ui.common.convert;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

public class ConvertMood
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertMood() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static Mood fromUiData(MoodUiData mood)
    {
        if (mood == null) {
            return null;
        }
        return new Mood(mood.asIndex());
    }
    
    public static MoodUiData toUiData(Mood mood)
    {
        if (mood == null) {
            return null;
        }
        return new MoodUiData(mood.asIndex());
    }
}
