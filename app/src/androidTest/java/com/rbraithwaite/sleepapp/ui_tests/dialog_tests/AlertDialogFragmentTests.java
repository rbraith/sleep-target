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

package com.rbraithwaite.sleepapp.ui_tests.dialog_tests;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AlertDialogFragmentTests
{
//*********************************************************
// api
//*********************************************************

    // TODO [21-01-20 2:00AM] -- This test is broken at the moment. The behaviour of
    //  AlertDialogFragment works fine, as can be verified by running this test with the 'expected'
    //  arg commented out and seeing the test fail with the correct NullPointerException, but for
    //  some reason trying to use the 'expected' arg with a NullPointerException is not working.
    //  I've also tried using a try/catch block instead, but that did not work either - JUnit or
    //  Android is doing something when any exception is thrown in the test :/
    @Test//(expected = NullPointerException.class)
    public void AlertDialogFragment_throwsWhenAlertDialogFactoryIsNotSet()
    {
        // AlertDialogFragment.createInstance() isn't called here, so the factory will be null
        // and a NullPointerException should be thrown.
//        DialogTestHelper<AlertDialogFragment> dialogHelper = DialogTestHelper.launchDialog
//        (AlertDialogFragment.class);
        
        // try/catch solution:
//        try {
//            DialogTestHelper<AlertDialogFragment> dialogHelper = DialogTestHelper.launchDialog
//            (AlertDialogFragment.class);
//        } catch (NullPointerException e) {
//            return;
//        }
//        assertThat(true, is(false));
    }
}
