/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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
package com.rbraithwaite.sleeptarget.test_utils.rules;

import android.app.Activity;
import android.util.Log;

import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.runner.Description;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;



/**
 * Subclass of RetryTestRule which automatically clears instrumented app data after a failed retry.
 *
 * Note: Despite everything I've tried it still appears that some state is being retained
 * between retry attempts, so this isn't suitable for all tests at the moment. I'm keeping it
 * around because it might still be useful for certain tests, and its the best solution I can
 * find right now.
 */
public class RetryInstrumentedTestRule
        extends RetryTestRule
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "RetryInstrumentedTestRu";
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected void onRetryFailed(Throwable t, int retryCount, Description description)
    {
        super.onRetryFailed(t, retryCount, description);
        TestUtils.resetAppData();
        finishActivities();
    }
    
//*********************************************************
// private methods
//*********************************************************

    // copied from androidx.test.runner.MonitoringInstrumentation.ActivityFinisher
    private void finishActivities()
    {
        TestUtils.runOnMainSync(() -> {
            List<Activity> activities = new ArrayList<>();
            
            for (Stage s : EnumSet.range(Stage.CREATED, Stage.STOPPED)) {
                activities.addAll(ActivityLifecycleMonitorRegistry.getInstance()
                                          .getActivitiesInStage(s));
            }
            
            if (activities.size() > 0) {
                Log.i(TAG, "Activities that are still in CREATED to STOPPED: " + activities.size());
            }
            
            for (Activity activity : activities) {
                if (!activity.isFinishing()) {
                    try {
                        Log.i(TAG, "Finishing activity: " + activity);
                        activity.finish();
                    } catch (RuntimeException e) {
                        Log.e(TAG, "Failed to finish activity.", e);
                    }
                }
            }
        });
    }
}
