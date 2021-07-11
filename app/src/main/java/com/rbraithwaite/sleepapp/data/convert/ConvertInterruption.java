package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;

public class ConvertInterruption
{
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
}
