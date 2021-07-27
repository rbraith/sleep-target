package com.rbraithwaite.sleepapp.test_utils.test_data.builders;

import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.utils.interfaces.BuilderOf;

public class SleepInterruptionEntityBuilder
        implements BuilderOf<SleepInterruptionEntity>
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public SleepInterruptionEntity build()
    {
        return new SleepInterruptionEntity(
                TestUtils.ArbitraryData.getDate(),
                12345,
                "reason 1");
    }
}
