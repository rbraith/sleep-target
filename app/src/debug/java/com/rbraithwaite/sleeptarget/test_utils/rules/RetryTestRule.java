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

import android.util.Log;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

// https://stackoverflow.com/a/8301639
public class RetryTestRule
        implements TestRule
{
//*********************************************************
// public helpers
//*********************************************************

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Retry
    {
        int count() default 3;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public Statement apply(
            Statement base, Description description)
    {
        return new Statement()
        {
            private static final String TAG = "RetryTestsRule";
            
            @Override
            public void evaluate() throws Throwable
            {
                RetryTestRule.Retry retry = description.getAnnotation(RetryTestRule.Retry.class);
                
                if (retry != null) {
                    runRetries(retry.count());
                } else {
                    // not a retry-able test
                    base.evaluate();
                }
            }
            
            private void runRetries(int retryCount) throws Throwable
            {
                Throwable caughtThrowable = null;
                
                for (int i = 0; i < retryCount; i++) {
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        onRetryFailed(t, i + 1, description);
                    }
                }
                
                Log.e("test",
                      description.getDisplayName() + ": giving up after " + retryCount +
                      " failures");
                throw caughtThrowable;
            }
        };
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected void onRetryFailed(Throwable t, int retryCount, Description description)
    {
        Log.e("test", description.getDisplayName() + ": run " + retryCount + " failed");
    }
}
