package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.CurrentSession;
import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.core.models.Mood;
import com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData.NOT_STARTED;
import static com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData.NO_COMMENTS;
import static com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData.NO_CURRENT_INTERRUPTION;
import static com.rbraithwaite.sleepapp.data.prefs.CurrentSessionPrefsData.NO_MOOD;

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
        
        long started = currentSession.isStarted() ?
                currentSession.getStart().getTime() : NOT_STARTED;
        
        String additionalComments = currentSession.getAdditionalComments();
        additionalComments = additionalComments == null ? NO_COMMENTS : additionalComments;
        
        int moodIndex =
                currentSession.getMood() == null ? NO_MOOD : currentSession.getMood().asIndex();
        
        Set<String> selectedTagIds = currentSession.getSelectedTagIds().stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
        
        Set<String> interruptions = currentSession.getInterruptions().stream()
                .map(ConvertInterruption::toJson).collect(Collectors.toSet());
        
        String currentInterruption = currentSession.isInterrupted() ?
                ConvertInterruption.toJson(currentSession.createCurrentInterruptionSnapshot(null)) :
                NO_CURRENT_INTERRUPTION;
        
        return new CurrentSessionPrefsData(
                started,
                additionalComments,
                moodIndex,
                selectedTagIds,
                interruptions,
                currentInterruption);
    }
    
    public static CurrentSession fromPrefsData(CurrentSessionPrefsData prefsData)
    {
        if (prefsData == null) {
            return null;
        }
        
        Date start = prefsData.start == NOT_STARTED ?
                null :
                new TimeUtils().getDateFromMillis(prefsData.start);
        
        String additionalComments = prefsData.additionalComments == NO_COMMENTS ?
                null :
                prefsData.additionalComments;
        
        Mood mood = prefsData.moodIndex == NO_MOOD ? null : new Mood(prefsData.moodIndex);
        
        List<Integer> selectedTagIds = prefsData.selectedTagIds.stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
        
        List<Interruption> interruptions =
                ConvertInterruption.listFromJsonSet(prefsData.interruptions);
        
        Interruption currentInterruption =
                ConvertInterruption.fromJson(prefsData.currentInterruption);
        
        return new CurrentSession(
                start,
                additionalComments,
                mood,
                selectedTagIds,
                interruptions,
                currentInterruption);
    }
}
