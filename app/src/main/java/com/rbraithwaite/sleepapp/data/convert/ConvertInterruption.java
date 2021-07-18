package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ConvertInterruption
{
//*********************************************************
// private constants
//*********************************************************

    private static final String JSON_START = "start";
    private static final String JSON_DURATION = "duration";
    private static final String JSON_REASON = "reason";
    
//*********************************************************
// api
//*********************************************************

    public static SleepInterruptionEntity toEntity(Interruption interruption)
    {
        if (interruption == null) {
            return null;
        }
        return new SleepInterruptionEntity(
                interruption.getStart().getTime(),
                interruption.getDurationMillis(),
                interruption.getReason());
    }
    
    // TEST NEEDED [21-07-17 9:08PM] -- .
    public static String toJson(Interruption interruption)
    {
        if (interruption == null) {
            return null;
        }
        
        try {
            JSONObject json = new JSONObject();
            // REFACTOR [21-07-17 5:02PM] -- hardcoded strings.
            json.put(JSON_START, interruption.getStart().getTime());
            json.put(JSON_DURATION, interruption.getDurationMillis());
            json.put(JSON_REASON, interruption.getReason() == null ? "" : interruption.getReason());
            return json.toString();
        } catch (JSONException e) {
            // REFACTOR [21-07-17 5:01PM] -- It would be better to throw a custom exception.
            e.printStackTrace();
            return null;
        }
    }
    
    // TEST NEEDED [21-07-17 9:08PM] -- .
    public static Interruption fromJson(String json)
    {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            JSONObject parsed = new JSONObject(json);
            long start = parsed.getLong(JSON_START);
            int duration = parsed.getInt(JSON_DURATION);
            String reason = parsed.getString(JSON_REASON);
            
            return new Interruption(
                    new TimeUtils().getDateFromMillis(start),
                    duration,
                    "".equals(reason) ? null : reason);
        } catch (JSONException e) {
            // REFACTOR [21-07-17 5:01PM] -- It would be better to throw a custom exception.
            e.printStackTrace();
            return null;
        }
    }
    
    // TEST NEEDED [21-07-17 9:08PM] -- .
    public static List<Interruption> listFromJsonSet(Set<String> jsonSet)
    {
        if (jsonSet == null) {
            return null;
        }
        
        return jsonSet.stream()
                .map(ConvertInterruption::fromJson)
                .collect(Collectors.toList());
    }
}
