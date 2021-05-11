package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

public class ConvertCurrentSession
{
//*********************************************************
// constructors
//*********************************************************

    private ConvertCurrentSession() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static CurrentSessionPrefsData toPrefsData(CurrentSession currentSession)
    {
        if (currentSession == null) {
            return null;
        }
        return new CurrentSessionPrefsData(
                currentSession.getStart(),
                currentSession.getAdditionalComments(),
                currentSession.getMood() == null ?
                        CurrentSessionPrefsData.NO_MOOD :
                        currentSession.getMood().toIndex(),
                currentSession.getSelectedTagIds());
    }
    
    public static CurrentSession fromPrefsData(
            CurrentSessionPrefsData prefsData,
            TimeUtils timeUtils)
    {
        if (prefsData == null) {
            return null;
        }
        CurrentSession currentSession = new CurrentSession(
                prefsData.start,
                prefsData.additionalComments,
                prefsData.moodIndex == CurrentSessionPrefsData.NO_MOOD ?
                        null :
                        Mood.fromIndex(prefsData.moodIndex),
                prefsData.selectedTagIds,
                timeUtils);
        return currentSession;
    }
}