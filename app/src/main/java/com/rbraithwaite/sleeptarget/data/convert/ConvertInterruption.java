/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.data.convert;

import com.rbraithwaite.sleeptarget.core.models.Interruption;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

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
        return toEntity(interruption, 0);
    }
    
    public static SleepInterruptionEntity toEntity(
            Interruption interruption,
            int parentSleepSessionId)
    {
        if (interruption == null) {
            return null;
        }
        return new SleepInterruptionEntity(
                interruption.getId(),
                parentSleepSessionId,
                interruption.getStart(),
                // REFACTOR [21-07-31 12:04AM] the entity should just use a long instead of this
                //  cast.
                (int) interruption.getDurationMillis(),
                interruption.getReason());
    }
    
    // TEST NEEDED [21-07-20 5:22PM]
    public static Interruption fromEntity(SleepInterruptionEntity entity)
    {
        return new Interruption(
                entity.id,
                entity.startTime,
                entity.durationMillis,
                entity.reason);
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
    
    public static List<SleepInterruptionEntity> listToEntityList(List<Interruption> interruptions)
    {
        return listToEntityList(interruptions, 0);
    }
    
    public static List<SleepInterruptionEntity> listToEntityList(
            List<Interruption> interruptions,
            int parentSleepSessionId)
    {
        return interruptions == null ?
                null :
                interruptions.stream()
                        .map(interruption -> toEntity(interruption, parentSleepSessionId))
                        .collect(Collectors.toList());
    }
}
