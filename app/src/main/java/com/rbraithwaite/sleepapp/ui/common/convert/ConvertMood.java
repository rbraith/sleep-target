package com.rbraithwaite.sleepapp.ui.common.convert;

import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

import java.util.Arrays;
import java.util.List;

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
        return new Mood(convertUiTypeToType(mood.type));
    }
    
    public static MoodUiData toUiData(Mood mood)
    {
        if (mood == null) {
            return null;
        }
        return new MoodUiData(convertTypeToUiType(mood.getType()));
    }

//*********************************************************
// private methods
//*********************************************************

    private static MoodUiData.Type convertTypeToUiType(Mood.Type moodType)
    {
        List<Mood.Type> moodTypes = Arrays.asList(Mood.Type.values());
        return MoodUiData.Type.values()[moodTypes.indexOf(moodType)];
    }
    
    private static Mood.Type convertUiTypeToType(MoodUiData.Type moodUiType)
    {
        List<MoodUiData.Type> moodUiTypes = Arrays.asList(MoodUiData.Type.values());
        return Mood.Type.values()[moodUiTypes.indexOf(moodUiType)];
    }
}
