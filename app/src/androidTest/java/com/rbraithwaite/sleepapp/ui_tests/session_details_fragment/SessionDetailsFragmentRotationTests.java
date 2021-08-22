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

package com.rbraithwaite.sleepapp.ui_tests.session_details_fragment;

import android.content.pm.ActivityInfo;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.drivers.SessionDetailsTestDriver;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SessionDetailsFragmentRotationTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void displayedValuesPersistAcrossOrientationChange()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        SessionDetailsTestDriver sessionDetails =
                SessionDetailsTestDriver.startingWith(sleepSession);
        
        sessionDetails.rotateScreen(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        
        sessionDetails.assertThat().displayedValuesMatch(sleepSession);
    }
}
